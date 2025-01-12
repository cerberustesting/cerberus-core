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
package org.cerberus.core.servlet.crud.test.testcase;

import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseDep;
import org.cerberus.core.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.core.crud.factory.IFactoryTestCaseLabel;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseDepService;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractCreateUpdateTestCase extends AbstractCrudTestCase {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AbstractCreateUpdateTestCase.class);
    @Autowired
    private IFactoryTestCaseLabel testCaseLabelFactory;
    @Autowired
    private IFactoryTestCaseCountry testCaseCountryFactory;
    @Autowired
    private ITestCaseLabelService testCaseLabelService;
    @Autowired
    private ITestCaseCountryService testCaseCountryService;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private ITestCaseStepActionService testCaseStepActionService;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private ITestCaseDepService testCaseDepService;

    protected abstract String getTypeOperation();

    protected abstract void fireLogEvent(String keyTest, String keyTestCase, TestCase tc, HttpServletRequest request, HttpServletResponse response);

    protected abstract TestCase getTestCaseBeforeTraitment(String keyTest, String keyTestCase) throws CerberusException, UnsupportedEncodingException;

    protected abstract void updateTestCase(String originalTest, String originalTestCase, TestCase tc) throws CerberusException;

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, CerberusException, JSONException {

        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        String test = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("test"), "");
        String testcase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("testcase"), "");
        String originalTest = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("originalTest"), "");
        String originalTestCase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("originalTestcase"), "");

        boolean primaryKeyChanged = !(originalTest != null && originalTestCase != null && originalTest.equals(test) && originalTestCase.equals(testcase));

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isEmptyOrNull(test) && StringUtil.isEmptyOrNull(testcase)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", this.getTypeOperation())
                    .replace("%REASON%", "mandatory fields (test or testcase) are missing."));
            finalAnswer.setResultMessage(msg);

        } else {
            try {
                TestCase tc = getTestCaseFromRequest(request, getTestCaseBeforeTraitment(originalTest, originalTestCase));
                updateTestCase(originalTest, originalTestCase, tc);

                if (StringUtil.isEmptyOrNull(originalTest)) {
                    fireLogEvent(test, testcase, tc, request, response);
                } else {
                    fireLogEvent(originalTest, originalTestCase, tc, request, response);
                }

                // Update labels
                if (request.getParameter("labels") != null) {
                    JSONArray objLabelArray = new JSONArray(request.getParameter("labels"));
                    List<TestCaseLabel> labelList = getLabelListFromRequest(request, test, testcase, objLabelArray);

                    // Update the Database with the new list.
                    ans = testCaseLabelService.compareListAndUpdateInsertDeleteElements(tc.getTest(), tc.getTestcase(), labelList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                }

                // Update Countries
                if (request.getParameter("countries") != null) {
                    JSONArray objCountryArray = new JSONArray(request.getParameter("countries"));
                    List<TestCaseCountry> tccList = getCountryListFromRequest(request, test, testcase, objCountryArray);

                    // Update the Database with the new list.
                    ans = testCaseCountryService.compareListAndUpdateInsertDeleteElements(tc.getTest(), tc.getTestcase(), tccList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                }

                // Update Countries
                if (request.getParameter("countries") != null) {
                    JSONArray objCountryArray = new JSONArray(request.getParameter("countries"));
                    List<TestCaseCountry> tccList = getCountryListFromRequest(request, test, testcase, objCountryArray);

                    // Update the Database with the new list.
                    ans = testCaseCountryService.compareListAndUpdateInsertDeleteElements(test, testcase, tccList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);

                    // Duplicate other objects.
                    List<TestCaseCountryProperties> tccpList;
                    List<TestCaseCountryProperties> newTccpList = new ArrayList<>();
                    if (primaryKeyChanged && !tccList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        tccpList = testCaseCountryPropertiesService.findListOfPropertyPerTestTestCase(originalTest, originalTestCase);
                        //Build a new list with the countries that exist for the testcaseId.
                        for (TestCaseCountryProperties curTccp : tccpList) {
                            if (testCaseCountryService.exist(test, testcase, curTccp.getCountry())) {
                                newTccpList.add(curTccp);
                            }
                        }
                        if (!newTccpList.isEmpty()) {
                            ans = testCaseCountryPropertiesService.duplicateList(newTccpList, test, testcase);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                        }
                    }

                }

                // update testcaseId dependency
                if (request.getParameter("dependencies") != null) {
                    List<TestCaseDep> testcaseDependencies = getDependencyFromRequest(request, tc);

                    testCaseDepService.compareListAndUpdateInsertDeleteElements(tc.getTest(), tc.getTestcase(), testcaseDependencies);
                }

                if (primaryKeyChanged) {
                    List<TestCaseStep> tcsList = new ArrayList<>();
                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        tcsList = testCaseStepService.getListOfSteps(originalTest, originalTestCase);
                        if (!tcsList.isEmpty()) {
                            ans = testCaseStepService.duplicateList(tcsList, test, testcase);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                        }
                    }

                    List<TestCaseStepAction> tcsaList = new ArrayList<>();
                    if (!tcsList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        tcsaList = testCaseStepActionService.readByTestTestCase(originalTest, originalTestCase).getDataList();
                        if (!tcsaList.isEmpty()) {
                            ans = testCaseStepActionService.duplicateList(tcsaList, test, testcase);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                        }
                    }

                    if (!tcsList.isEmpty() && !tcsaList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        List<TestCaseStepActionControl> tcsacList = testCaseStepActionControlService.findControlByTestTestCase(originalTest, originalTestCase);
                        if (!tcsacList.isEmpty()) {
                            ans = testCaseStepActionControlService.duplicateList(tcsacList, test, testcase);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                        }
                    }
                }
            } catch (CerberusException ex) {
                LOG.error(" Exception :" + ex.toString(), ex);

                MessageEvent msgEx = new MessageEvent(MessageEventEnum.GENERIC_ERROR);
                msgEx.setDescription(msg.getDescription() + " " + ex.getMessageError().getDescription());
                finalAnswer.setResultMessage(msgEx);
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();

    }

    private List<TestCaseCountry> getCountryListFromRequest(HttpServletRequest request, String test, String testCase, JSONArray json) throws CerberusException, JSONException, UnsupportedEncodingException {
        List<TestCaseCountry> tdldList = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            String country = objectJson.getString("value");
            // Parameter that needs to be secured --> We SECURE+DECODE them
            // NONE
            // Parameter that we cannot secure as we need the html --> We DECODE them

            if (!delete) {
                TestCaseCountry tcc = testCaseCountryFactory.create(test, testCase, country);
                tdldList.add(tcc);
            }
        }
        return tdldList;
    }

    protected List<TestCaseLabel> getLabelListFromRequest(HttpServletRequest request, String test, String testCase, JSONArray json) throws CerberusException, JSONException, UnsupportedEncodingException {
        List<TestCaseLabel> labelList = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            Integer labelId = objectJson.getInt("labelId");

            Timestamp creationDate = new Timestamp(new Date().getTime());

            if (!delete) {
                labelList.add(testCaseLabelFactory.create(0, test, testCase, labelId, request.getRemoteUser(), creationDate, request.getRemoteUser(), creationDate, null));
            }
        }
        return labelList;
    }

    protected List<TestCaseDep> getDependencyFromRequest(HttpServletRequest request, TestCase testcase) throws JSONException {
        List<TestCaseDep> testcaseDependencies = new LinkedList<>();
        jsonArrayFoEach(request, "dependencies", (jsonObj) -> {
            String testcaseId = jsonObj.getString("testcase");
            Long testcaseDependencyId = jsonObj.getLong("id");
            Integer testcaseDependencyDelay = Integer.parseInt(jsonObj.getString("depDelay"));
            String test = jsonObj.getString("test");
            String type = jsonObj.getString("type");
            String description = jsonObj.getString("description");
            boolean isActive = jsonObj.getBoolean("isActive");
            Timestamp creationDate = new Timestamp(new Date().getTime());

            testcaseDependencies.add(
                    TestCaseDep.builder()
                            .id(testcaseDependencyId).test(testcase.getTest()).testcase(testcase.getTestcase())
                            .dependencyTest(test).dependencyTestcase(testcaseId).dependencyTCDelay(testcaseDependencyDelay)
                            .type(type).isActive(isActive)
                            .description(description)
                            .dateCreated(creationDate).dateModif(creationDate).usrCreated(request.getRemoteUser()).usrModif(request.getRemoteUser())
                            .build());
        });
        return testcaseDependencies;
    }

    @FunctionalInterface
    protected interface JsonFunction<T> {

        void foreach(T t) throws JSONException;
    }

    protected void jsonArrayFoEach(HttpServletRequest request, String jsonArrayName, JsonFunction<JSONObject> fct) throws JSONException {
        JSONArray json = new JSONArray(request.getParameter(jsonArrayName));

        for (int i = 0; i < json.length(); i++) {
            fct.foreach(json.getJSONObject(i));
        }

    }
}
