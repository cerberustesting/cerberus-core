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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseExecutionDAO;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionData;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.core.crud.entity.TestCaseStepExecution;
import org.cerberus.core.crud.factory.IFactoryTagSystem;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITagSystemService;
import org.cerberus.core.crud.service.ITestCaseExecutionDataService;
import org.cerberus.core.crud.service.ITestCaseExecutionFileService;
import org.cerberus.core.crud.service.ITestCaseExecutionHttpStatService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepExecutionService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author bcivel
 */
@Service
public class TestCaseExecutionService implements ITestCaseExecutionService {

    @Autowired
    private ITestCaseExecutionDAO testCaseExecutionDao;
    @Autowired
    private ITestCaseStepExecutionService testCaseStepExecutionService;
    @Autowired
    private ITestCaseExecutionFileService testCaseExecutionFileService;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ITestCaseExecutionQueueService testCaseExecutionInQueueService;
    @Autowired
    private ITestCaseExecutionQueueDepService testCaseExecutionQueueDepService;
    @Autowired
    private ITagSystemService tagSystemService;
    @Autowired
    private ITagService tagService;
    @Autowired
    private IFactoryTagSystem factoryTagSystem;
    @Autowired
    private ITestCaseExecutionHttpStatService testCaseExecutionHttpStatService;

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionService.class);

    @Override
    public long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        // We create the link between the tag and the system if it does not exist yet.
        if (!StringUtil.isEmptyOrNull(tCExecution.getTag())
                && !StringUtil.isEmptyOrNull(tCExecution.getSystem())
                && !tagSystemService.exist(tCExecution.getTag(), tCExecution.getSystem())) {
            tagSystemService.create(factoryTagSystem.create(tCExecution.getTag(), tCExecution.getSystem(), tCExecution.getUsrCreated(), null, "", null));
        }
        return testCaseExecutionDao.insertTCExecution(tCExecution);
    }

    @Override
    public void updateTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        testCaseExecutionDao.updateTCExecution(tCExecution);
    }

    @Override
    public AnswerItem<TestCaseExecution> readLastByCriteria(String application) {
        return testCaseExecutionDao.readLastByCriteria(application);
    }

    @Override
    public TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
            String build, String revision) throws CerberusException {
        return testCaseExecutionDao.findLastTCExecutionByCriteria(test, testCase, environment, country, build, revision);
    }

    @Override
    public TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
            String build, String revision, String browser, String browserVersion,
            String ip, String port, String tag) {
        return this.testCaseExecutionDao.findLastTCExecutionByCriteria(test, testCase, environment, country, build, revision, browser, browserVersion, ip, port, tag);
    }

    @Override
    public List<TestCaseExecution> findTCExecutionByCriteria1(String dateLimitFrom, String test, String testCase,
            String application, String country, String environment, String controlStatus, String status) {
        // Transform empty parameter in % in order to remove from SQL filter (thanks to the like operator).
        test = ParameterParserUtil.wildcardIfEmpty(test);
        testCase = ParameterParserUtil.wildcardIfEmpty(testCase);
        application = ParameterParserUtil.wildcardIfEmpty(application);
        country = ParameterParserUtil.wildcardIfEmpty(country);
        environment = ParameterParserUtil.wildcardIfEmpty(environment);
        controlStatus = ParameterParserUtil.wildcardIfEmpty(controlStatus);
        status = ParameterParserUtil.wildcardIfEmpty(status);
        return testCaseExecutionDao.findExecutionByCriteria1(dateLimitFrom, test, testCase, application, country, environment, controlStatus, status);
    }

    @Override
    public List<TestCaseExecution> readByCriteria(List<String> system, List<String> countries, List<String> environments,
            List<String> robotDecli, List<TestCase> testcases, Date from, Date to) throws CerberusException {
        return this.convert(testCaseExecutionDao.readByCriteria(system, countries, environments, robotDecli, testcases, from, to));
    }

    @Override
    public List<TestCaseExecution> readByCriteria(List<String> system, List<String> tags, Date from, Date to) throws CerberusException {
        return this.convert(testCaseExecutionDao.readByCriteria(system, tags, from, to));
    }

    @Override
    public Integer getNbExecutions(List<String> system) throws CerberusException {
        return testCaseExecutionDao.getNbExecutions(system);
    }

    @Override
    public long registerRunID(TestCaseExecution tCExecution) throws CerberusException {

        // Insert TestCaseExecution
        long runID;
        try {
            runID = this.insertTCExecution(tCExecution);
        } catch (CerberusException ex) {
            LOG.warn(ex.toString(), ex);
            throw new CerberusException(ex.getMessageError());
        }
        return runID;
    }

    @Override
    public TestCaseExecution findTCExecutionByKey(long id) {
        return testCaseExecutionDao.findTCExecutionByKey(id);
    }

    @Override
    public TestCaseExecution findLastTCExecutionInGroup(String test, String testCase, String environment, String country,
            String build, String revision, String browser, String browserVersion,
            String ip, String port, String tag) {
        return this.testCaseExecutionDao.findLastTCExecutionInGroup(test, testCase, environment, country, build, revision, browser, browserVersion, ip, port, tag);
    }

    @Override
    public TestCaseExecution findLastTestCaseExecutionNotPE(String test, String testCase) {
        return testCaseExecutionDao.findLastTestCaseExecutionNotPE(test, testCase);
    }

    @Override
    public List<String> findDistinctTag(boolean withUUIDTag) {
        return testCaseExecutionDao.findDistinctTag(withUUIDTag);
    }

    @Override
    public void setTagToExecution(long id, String tag) throws CerberusException {
        testCaseExecutionDao.setTagToExecution(id, tag);
    }

    @Override
    public void updateFalseNegative(long id, boolean falseNegative, String usrModif) throws CerberusException {
        testCaseExecutionDao.updateFalseNegative(id, falseNegative, usrModif);
    }

    @Override
    public void updateLastAndFlaky(long id, boolean isLast, boolean isFlaky, String usrModif) throws CerberusException {
        testCaseExecutionDao.updateLastAndFlaky(id, isLast, isFlaky, usrModif);
    }

    @Override
    public AnswerList<TestCaseExecution> readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) {
        return AnswerUtil.convertToAnswerList(() -> testCaseExecutionDao.readByTagByCriteria(tag, start, amount, sort, searchTerm, individualSearch));
    }

    @Override
    public AnswerList<TestCaseExecution> readByCriteria(int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch, List<String> individualLike, List<String> system) throws CerberusException {
        return testCaseExecutionDao.readByCriteria(start, amount, sort, searchTerm, individualSearch, individualLike, system);
    }

    @Override
    public AnswerList<TestCaseExecution> readByTag(String tag) {
        return testCaseExecutionDao.readByTag(tag);
    }

    @Override
    public int readNbByTag(String tag) throws CerberusException {
        return testCaseExecutionDao.readNbByTag(tag);
    }

    @Override
    public AnswerList<TestCaseExecution> readDistinctEnvCountryBrowserByTag(String tag) {
        return testCaseExecutionDao.readDistinctEnvCountryBrowserByTag(tag);
    }

    @Override
    public List<TestCaseExecution> createAllTestCaseExecution(List<TestCase> testCaseList, List<String> envList, List<String> countryList) {
        List<TestCaseExecution> result = new ArrayList<>();

        for (TestCase tc : testCaseList) {
            for (String environment : envList) {
                for (String country : countryList) {
                    TestCaseExecution execution = new TestCaseExecution();

                    execution.setTest(tc.getTest());
                    execution.setTestCase(tc.getTestcase());
                    execution.setEnvironment(environment);
                    execution.setCountry(country);
                    result.add(execution);
                }
            }
        }

        return result;
    }

    @Override
    public AnswerItem<TestCaseExecution> readByKey(long executionId) {
        return testCaseExecutionDao.readByKey(executionId);
    }

    @Override
    public AnswerItem<TestCaseExecution> readLastByCriteria(String test, String testCase, String country, String environment, String tag) {
        return testCaseExecutionDao.readLastByCriteria(test, testCase, country, environment, tag);
    }

    @Override
    public AnswerItem<TestCaseExecution> readByKeyWithDependency(long executionId) {
        // Get Main Execution.
        AnswerItem<TestCaseExecution> tce = this.readByKey(executionId);
        TestCaseExecution testCaseExecution = tce.getItem();

        // Get Execution Tag.
        if (!StringUtil.isEmptyOrNull(testCaseExecution.getTag())) {
            AnswerItem<Tag> ai = tagService.readByKey(testCaseExecution.getTag());
            testCaseExecution.setTagObj(ai.getItem());
        }

        // Get Test Case.
        AnswerItem<TestCase> ai = testCaseService.readByKeyWithDependency(testCaseExecution.getTest(), testCaseExecution.getTestCase());
        testCaseExecution.setTestCaseObj(ai.getItem());

        // Get Execution Data (Properties).
        try {
            List<TestCaseExecutionData> a = testCaseExecutionDataService.readByIdWithDependency(executionId);
            for (TestCaseExecutionData tced : a) {
                if (tced.getIndex() == 1) {
                    testCaseExecution.getTestCaseExecutionDataMap().put(tced.getProperty(), tced);
                }
            }
        } catch (CerberusException e) {
            LOG.error("An erreur occured while getting testcase execution data", e);
        }

        // Get Execution Dependencies.
        if (testCaseExecution.getQueueID() > 0) {
            try {
                List<TestCaseExecutionQueueDep> a = testCaseExecutionQueueDepService.convert(testCaseExecutionQueueDepService.readByExeQueueId(testCaseExecution.getQueueID()));
                testCaseExecution.setTestCaseExecutionQueueDepList(a);
            } catch (CerberusException e) {
                LOG.error("An error occurred while getting execution dependency", e);
            }
        }

        // set video if it exists
        try {
            List<TestCaseExecutionFile> videosAnswer = testCaseExecutionFileService.getListByFileDesc(executionId, "Video");
            List<String> videos = new LinkedList<>();
            videosAnswer.forEach(tcef -> videos.add(tcef.getFileName()));
            testCaseExecution.setVideos(videos);
        } catch (CerberusException e) {
            LOG.error("An error occurred while getting video file", e);
        }

        // We first add the 'Pres Testing' testcase execution steps.
        AnswerList<TestCaseStepExecution> preTestCaseSteps = testCaseStepExecutionService.readByVarious1WithDependency(executionId, Test.TEST_PRETESTING, null);
        testCaseExecution.setTestCaseStepExecutionList(preTestCaseSteps.getDataList());
        // Then we add the steps from the main testcase.
        AnswerList<TestCaseStepExecution> steps = testCaseStepExecutionService.readByVarious1WithDependency(executionId, testCaseExecution.getTest(), testCaseExecution.getTestCase());
        testCaseExecution.addStepExecutionList(steps.getDataList());
        // Then we add the Post steps .
        AnswerList<TestCaseStepExecution> postTestCaseSteps = testCaseStepExecutionService.readByVarious1WithDependency(executionId, Test.TEST_POSTTESTING, null);
        testCaseExecution.addStepExecutionList(postTestCaseSteps.getDataList());

        // Get Execution Files.
        AnswerList<TestCaseExecutionFile> files = testCaseExecutionFileService.readByVarious(executionId, "");
        testCaseExecution.setFileList(files.getDataList());

        // Get Execution Files.
        AnswerItem<TestCaseExecutionHttpStat> httpStat = testCaseExecutionHttpStatService.readByKey(executionId);
        testCaseExecution.setHttpStat(httpStat.getItem());

        // Set Final response.
        return new AnswerItem<>(testCaseExecution, tce.getResultMessage());
    }

    @Override
    public TestCaseExecution convert(AnswerItem<TestCaseExecution> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseExecution> convert(AnswerList<TestCaseExecution> answerList) throws CerberusException {
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
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return testCaseExecutionDao.readDistinctValuesByCriteria(system, test, searchParameter, individualSearch, columnName);
    }

    @Override
    public List<TestCaseExecution> readLastExecutionAndExecutionInQueueByTag(String tag) throws CerberusException {
        AnswerList<TestCaseExecution> testCaseExecution;
        AnswerList<TestCaseExecutionQueue> testCaseExecutionInQueue;

        //Get list of execution by tag
        testCaseExecution = this.readByTag(tag);
        List<TestCaseExecution> testCaseExecutions = testCaseExecution.getDataList();
        // Get list of Execution in Queue by Tag
        List<String> stateList = new ArrayList<>();
        // We select here the list of state where no execution exist yet (or will never exist).
        stateList.add(TestCaseExecutionQueue.State.QUWITHDEP.name());
        stateList.add(TestCaseExecutionQueue.State.QUEUED.name());
        stateList.add(TestCaseExecutionQueue.State.QUWITHDEP_PAUSED.name());
        stateList.add(TestCaseExecutionQueue.State.QUEUED_PAUSED.name());
        stateList.add(TestCaseExecutionQueue.State.WAITING.name());
        stateList.add(TestCaseExecutionQueue.State.STARTING.name());
        stateList.add(TestCaseExecutionQueue.State.ERROR.name());
        testCaseExecutionInQueue = testCaseExecutionInQueueService.readByVarious1(tag, stateList, true);
        List<TestCaseExecutionQueue> testCaseExecutionsInQueue = testCaseExecutionInQueue.getDataList();
        /*
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        testCaseExecutions = hashExecution(testCaseExecutions, testCaseExecutionsInQueue);

        // load all test case dependency
        testCaseExecutionQueueDepService.loadDependenciesOnTestCaseExecution(testCaseExecutions);

        return testCaseExecutions;
    }

    private List<TestCaseExecution> hashExecution(List<TestCaseExecution> testCaseExecutions, List<TestCaseExecutionQueue> testCaseExecutionsInQueue) {
        LinkedHashMap<String, TestCaseExecution> testCaseExecutionsList = new LinkedHashMap<>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String key = testCaseExecution.getRobotDecli() + "_"
                    + testCaseExecution.getCountry() + "_"
                    + testCaseExecution.getEnvironment() + "_"
                    + testCaseExecution.getTest() + "_"
                    + testCaseExecution.getTestCase();
            if ((testCaseExecutionsList.containsKey(key))) {
                testCaseExecution.setFirstExeStart(testCaseExecution.getStart() < testCaseExecutionsList.get(key).getFirstExeStart() ? testCaseExecution.getStart() : testCaseExecutionsList.get(key).getFirstExeStart());
                testCaseExecution.setLastExeStart(testCaseExecution.getEnd() > testCaseExecutionsList.get(key).getLastExeEnd() ? testCaseExecution.getStart() : testCaseExecutionsList.get(key).getLastExeStart());
                testCaseExecution.setLastExeEnd(testCaseExecution.getEnd() > testCaseExecutionsList.get(key).getLastExeEnd() ? testCaseExecution.getEnd() : testCaseExecutionsList.get(key).getLastExeEnd());

                testCaseExecution.setNbExecutions(testCaseExecutionsList.get(key).getNbExecutions() + 1);
                if ((TestCaseExecution.CONTROLSTATUS_OK.equalsIgnoreCase(testCaseExecution.getControlStatus()))
                        && (testCaseExecution.getNbExecutions() > 1)) {
                    testCaseExecution.setFlaky(true);
                }

                if (TestCaseExecution.CONTROLSTATUS_PE.equalsIgnoreCase(testCaseExecution.getControlStatus())
                        && testCaseExecutionsList.get(key) != null) {
                    testCaseExecution.setPreviousExeId(testCaseExecutionsList.get(key).getId());
                    testCaseExecution.setPreviousExeStatus(testCaseExecutionsList.get(key).getControlStatus());
                }
            } else {
                testCaseExecution.setFirstExeStart(testCaseExecution.getStart());
                testCaseExecution.setLastExeStart(testCaseExecution.getStart());
                testCaseExecution.setLastExeEnd(testCaseExecution.getEnd());
            }
            testCaseExecutionsList.put(key, testCaseExecution);
        }
        for (TestCaseExecutionQueue testCaseExecutionInQueue : testCaseExecutionsInQueue) {
            TestCaseExecution testCaseExecution = testCaseExecutionInQueueService.convertToTestCaseExecution(testCaseExecutionInQueue);
            String key = testCaseExecution.getRobotDecli() + "_"
                    + testCaseExecution.getCountry() + "_"
                    + testCaseExecution.getEnvironment() + "_"
                    + testCaseExecution.getTest() + "_"
                    + testCaseExecution.getTestCase();
            if (TestCaseExecution.CONTROLSTATUS_QU.equalsIgnoreCase(testCaseExecution.getControlStatus())
                    && testCaseExecutionsList.get(key) != null) {
                testCaseExecution.setFirstExeStart(testCaseExecutionsList.get(key).getFirstExeStart());
                testCaseExecution.setLastExeStart(testCaseExecutionsList.get(key).getLastExeStart());
                testCaseExecution.setLastExeEnd(testCaseExecutionsList.get(key).getLastExeEnd());
            }
            if (!testCaseExecutionsList.containsKey(key) || testCaseExecutionsList.get(key).getStart() < testCaseExecutionInQueue.getRequestDate().getTime()) {
                if (TestCaseExecution.CONTROLSTATUS_QU.equalsIgnoreCase(testCaseExecution.getControlStatus())
                        && testCaseExecutionsList.get(key) != null) {
                    testCaseExecution.setPreviousExeId(testCaseExecutionsList.get(key).getId());
                    testCaseExecution.setPreviousExeStatus(testCaseExecutionsList.get(key).getControlStatus());
                }
                testCaseExecutionsList.put(key, testCaseExecution);
            }
        }
        return new ArrayList<>(testCaseExecutionsList.values());
    }

    public JSONArray getLastByCriteria(String test, String testCase, String tag, Integer numberOfExecution) throws CerberusException {

        Map<String, List<String>> map = new HashMap<>();
        addElementToMap(map, "test", test);
        addElementToMap(map, "testCase", testCase);

        if (tag != null) {
            addElementToMap(map, "tag", tag);
        }

        AnswerList<TestCaseExecution> list = readByCriteria(0, numberOfExecution, " exe.`id` desc ", null, map, null, null);

        JSONArray ja = new JSONArray();
        for (TestCaseExecution tce : list.getDataList()) {
            TestCaseExecution tcex = this.readByKeyWithDependency(tce.getId()).getItem();
            ja.put(tcex.toJson(true));
        }
        return ja;
    }

    private void addElementToMap(Map<String, List<String>> map, String key, String value) {
        List<String> element = new ArrayList<>();
        element.add(value);
        map.put(key, element);
    }

}
