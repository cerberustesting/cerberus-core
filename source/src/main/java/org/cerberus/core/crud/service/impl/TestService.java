/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.core.crud.service.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestDAO;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/01/2013
 * @since 0.9.0
 */
@Service
public class TestService implements ITestService {

    @Autowired
    private ITestDAO testDao;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(TestService.class);

    @Override
    public AnswerItem<Test> readByKey(String test) {
        return AnswerUtil.convertToAnswerItem(() -> testDao.readByKey(test));
    }

    @Override
    public AnswerList<Test> readDistinctBySystem(String system) {
        return testDao.readDistinctBySystem(system);
    }

    @Override
    public AnswerList<Test> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return testDao.readByCriteria(start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public boolean exist(String Object) {
        AnswerItem objectAnswer = readByKey(Object);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(Test test) {
        Answer ans = testDao.create(test);
        return ans;
    }

    @Override
    public Answer update(String keyTest, Test test) {
        Answer ans = testDao.update(keyTest, test);
        return ans;
    }

    @Override
    public Answer delete(Test test) {
        Answer ans = testDao.delete(test);
        return ans;
    }

    @Override
    public Test convert(AnswerItem<Test> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Test> convert(AnswerList<Test> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        return testDao.readDistinctValuesByCriteria(searchTerm, individualSearch, columnName);
    }

    @Override
    public Answer deleteIfNotUsed(String test) {

        Answer ans = new Answer();

        AnswerItem resp = readByKey(test);

        if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
            // Object could not be found. We stop here and report the error.
            ans.setResultMessage(
                    new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED)
                            .resolveDescription("ITEM", "Test")
                            .resolveDescription("OPERATION", "Delete")
                            .resolveDescription("REASON", "Test does not exist")
            );
        } else {
            // The service was able to perform the query and confirm the object exist
            Test testData = (Test) resp.getItem();

            // Check if there is no associated Test Cases defining Step which is used OUTSIDE of the deleting Test
            try {
                final Collection<TestCaseStep> externallyUsedTestCaseSteps = externallyUsedTestCaseSteps(testData);
                if (!externallyUsedTestCaseSteps.isEmpty()) {
                    String cerberusUrlTemp = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
                    if (StringUtil.isEmptyOrNull(cerberusUrlTemp)) {
                        cerberusUrlTemp = parameterService.getParameterStringByKey("cerberus_url", "", "");
                    }
                    final String cerberusUrl = cerberusUrlTemp;

                    ans.setResultMessage(
                            new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED)
                                    .resolveDescription("ITEM", "Test")
                                    .resolveDescription("OPERATION", "Delete")
                                    .resolveDescription(
                                            "REASON", "You are trying to remove a Test which contains Test Case Steps which are currently used by other Test Case Steps outside of the removing Test. Please remove this link before to proceed: "
                                            + Collections2.transform(externallyUsedTestCaseSteps, (@Nullable final TestCaseStep input) -> String.format(
                                                    "<a href='%s/TestCaseScript.jsp?test=%s&testcase=%s&step=%s'>%s/%s#%s</a>",
                                                    cerberusUrl,
                                                    input.getTest(),
                                                    input.getTestcase(),
                                                    input.getStepId(),
                                                    input.getTest(),
                                                    input.getTestcase(),
                                                    input.getStepId()
                                            ))
                                    )
                    );
                } else {
                    // Test seems clean, process to delete
                    ans = delete(testData);
                }
            } catch (final CerberusException e) {
                LOG.error(e.getMessage(), e);
                ans.setResultMessage(new MessageEvent(
                        MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED)
                        .resolveDescription("DESCRIPTION", "Unexpected error: " + e.getMessage())
                );
            }
        }
        return ans;
    }

    /**
     * Get {@link TestCaseStep} which are using an other {@link TestCaseStep}
     * from the given {@link Test} but which are NOT included into this
     * {@link Test}
     *
     * @param test the {@link Test} from which getting externally used
     * {@link TestCaseStep}s
     * @return a {@link Collection} of {@link TestCaseStep} which are using an
     * other {@link TestCaseStep} from the given {@link Test} but which are NOT
     * included into this {@link Test}
     * @throws CerberusException if an unexpected error occurred
     */
    private Collection<TestCaseStep> externallyUsedTestCaseSteps(final Test test) throws CerberusException {

        final List<TestCaseStep> stepsInUse = testCaseStepService.getTestCaseStepsUsingTestInParameter(test.getTest());

        // Filter the retrieved list to only retain those which are not included from the given Test
        return Collections2.filter(stepsInUse, new Predicate<TestCaseStep>() {
            @Override
            public boolean apply(@Nullable final TestCaseStep input) {
                return !input.getTest().equals(test.getTest());
            }

            @Override
            public boolean test(TestCaseStep t) {
                return Predicate.super.test(t); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    @Override
    public Answer updateIfExists(String originalTest, Test test) {
        Answer ans = new Answer();
        AnswerItem resp = this.readByKey(originalTest);

        if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
            /**
             * Object could not be found. We stop here and report the error.
             */
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Application")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Test does not exist."));
            ans.setResultMessage(msg);

        } else {
            /**
             * The service was able to perform the query and confirm the object
             * exist, then we can update it.
             */
            Test testData = (Test) resp.getItem();
            testData.setTest(test.getTest());
            testData.setDescription(test.getDescription());
            testData.setActive(test.getActive());
            ans = this.update(originalTest, testData);

        }
        return ans;
    }

}
