/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.crud.service.ICampaignContentService;
import org.cerberus.crud.service.ICampaignLabelService;
import org.cerberus.crud.service.ICampaignParameterService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 * @author tbernardes
 */
@Service
public class TestCaseService implements ITestCaseService {

    private static final Logger LOG = LogManager.getLogger(TestCaseService.class);

    @Autowired
    private ITestCaseDAO testCaseDao;
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
    private IFactoryTestCase factoryTCase;
    @Autowired
    private ICampaignLabelService campaignLabelService;
    @Autowired
    private ICampaignContentService campaignContentService;
    @Autowired
    private ICampaignParameterService campaignParameterService;
    @Autowired
    private IParameterService parameterService;
    

    @Override
    public TestCase findTestCaseByKey(String test, String testCase) throws CerberusException {
        return testCaseDao.findTestCaseByKey(test, testCase);
    }

    @Override
    public TestCase findTestCaseByKeyWithDependency(String test, String testCase) throws CerberusException {
        TestCase newTcase;
        newTcase = findTestCaseByKey(test, testCase);
        if (newTcase == null) {
            //TODO:FN temporary debug messages
            LOG.warn("test case is null - test: " + test + " testcase: " + testCase);
        } else {
            List<TestCaseCountry> testCaseCountry = testCaseCountryService.findTestCaseCountryByTestTestCase(test, testCase);
            List<TestCaseCountry> testCaseCountryToAdd = new ArrayList();
            for (TestCaseCountry tcc : testCaseCountry) {
                List<TestCaseCountryProperties> properties = testCaseCountryPropertiesService.findListOfPropertyPerTestTestCaseCountry(test, testCase, tcc.getCountry());
                tcc.setTestCaseCountryProperty(properties);
                testCaseCountryToAdd.add(tcc);
            }
            newTcase.setTestCaseCountry(testCaseCountryToAdd);

            String initialTest = test;
            String initialTc = testCase;
            List<TestCaseStep> tcs = testCaseStepService.getListOfSteps(test, testCase);
            List<TestCaseStep> tcsToAdd = new ArrayList();
            for (TestCaseStep step : tcs) {
                int stepNumber = step.getStep();
                int initialStep = step.getStep();
                if (step.getUseStep().equals("Y")) {
                    test = step.getUseStepTest();
                    testCase = step.getUseStepTestCase();
                    stepNumber = step.getUseStepStep();
                }
                List<TestCaseStepAction> tcsa = testCaseStepActionService.getListOfAction(test, testCase, stepNumber);
                List<TestCaseStepAction> tcsaToAdd = new ArrayList();
                for (TestCaseStepAction action : tcsa) {
                    List<TestCaseStepActionControl> tcsac = testCaseStepActionControlService.findControlByTestTestCaseStepSequence(test, testCase, stepNumber, action.getSequence());
                    List<TestCaseStepActionControl> tcsacToAdd = new ArrayList();
                    for (TestCaseStepActionControl control : tcsac) {
                        control.setTest(initialTest);
                        control.setTestCase(initialTc);
                        control.setStep(initialStep);
                        tcsacToAdd.add(control);
                    }
                    action.setTestCaseStepActionControl(tcsacToAdd);
                    action.setTest(initialTest);
                    action.setTestCase(initialTc);
                    action.setStep(initialStep);
                    tcsaToAdd.add(action);
                }
                step.setTestCaseStepAction(tcsaToAdd);
                tcsToAdd.add(step);
            }
            newTcase.setTestCaseStep(tcsToAdd);
        }
        return newTcase;
    }

    @Override
    public List<TestCase> findTestCaseByTest(String test) {
        return testCaseDao.findTestCaseByTest(test);
    }

    @Override
    public List<TestCase> findTestCaseByTestSystem(String test, String system) {
        return testCaseDao.findTestCaseByTestSystem(test, system);
    }

    @Override
    public List<TestCase> findTestCaseByApplication(final String application) {
        return testCaseDao.findTestCaseByApplication(application);
    }

    @Override
    public boolean updateTestCaseInformation(TestCase testCase) {
        return testCaseDao.updateTestCaseInformation(testCase);
    }

    @Override
    public boolean updateTestCaseInformationCountries(TestCase tc) {
        return testCaseDao.updateTestCaseInformationCountries(tc);
    }

    @Override
    public boolean createTestCase(TestCase testCase) throws CerberusException {
        return testCaseDao.createTestCase(testCase);
    }

    @Override
    public List<TestCase> findTestCaseActiveByCriteria(String test, String application, String country) {
        return testCaseDao.findTestCaseByCriteria(test, application, country, "Y");
    }

    @Override
    public List<TestCase> findTestCaseByAllCriteria(TestCase tCase, String text, String system) {
        return this.testCaseDao.findTestCaseByCriteria(tCase, text, system);
    }

    @Override
    public AnswerList<List<TestCase>> readByVarious(String[] test, String[] idProject, String[] app, String[] creator, String[] implementer, String[] system,
            String[] testBattery, String[] campaign, String[] labelid, String[] priority, String[] group, String[] status, int length) {
        return testCaseDao.readByVarious(test, idProject, app, creator, implementer, system, testBattery, campaign, labelid, priority, group, status, length);
    }

    /**
     * @param column
     * @return
     * @since 0.9.1
     */
    @Override
    public List<String> findUniqueDataOfColumn(String column) {
        return this.testCaseDao.findUniqueDataOfColumn(column);
    }

    @Override
    public List<String> findTestWithTestCaseActiveAutomatedBySystem(String system) {
        TestCase tCase = factoryTCase.create(null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, -1, null, null, null, null, null, "Y", null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        List<String> result = new ArrayList();
        List<TestCase> testCases = findTestCaseByAllCriteria(tCase, null, system);
        for (TestCase testCase : testCases) {
            if (!testCase.getGroup().equals("PRIVATE")) {
                result.add(testCase.getTest());
            }
        }
        Set<String> uniqueResult = new HashSet<String>(result);
        result = new ArrayList();
        result.addAll(uniqueResult);
        Collections.sort(result);
        return result;
    }

    @Override
    public List<TestCase> findTestCaseActiveAutomatedBySystem(String test, String system) {
        TestCase tCase = factoryTCase.create(test, null, null, null, null, null, null, null, null, null,
                null, null, null, null, -1, null, null, null, null, null, "Y", null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        List<TestCase> result = new ArrayList();
        List<TestCase> testCases = findTestCaseByAllCriteria(tCase, null, system);
        for (TestCase testCase : testCases) {
            if (!testCase.getGroup().equals("PRIVATE")) {
                result.add(testCase);
            }
        }
        return result;
    }

    @Override
    public boolean deleteTestCase(TestCase testCase) {
        return testCaseDao.deleteTestCase(testCase);
    }

    @Override
    public void updateTestCase(TestCase tc) throws CerberusException {
        testCaseDao.updateTestCase(tc);
    }

    @Override
    public String getMaxNumberTestCase(String test) {
        return this.testCaseDao.getMaxNumberTestCase(test);
    }

    @Override
    public AnswerItem<List<TestCase>> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries) {
        String[] status = null;
        String[] system = null;
        String[] application = null;
        String[] priority = null;
        

        AnswerItem<Map<String, List<String>>> parameters = campaignParameterService.parseParametersByCampaign(campaign);

        for (Map.Entry<String, List<String>> entry : parameters.getItem().entrySet()) {
            String cle = entry.getKey();
            List<String> valeur = entry.getValue();

            switch (cle) {
                case CampaignParameter.PRIORITY_PARAMETER:
                    priority = valeur.toArray(new String[valeur.size()]);
                    break;
                case CampaignParameter.STATUS_PARAMETER:
                    status = valeur.toArray(new String[valeur.size()]);
                    break;
                case CampaignParameter.SYSTEM_PARAMETER:
                    system = valeur.toArray(new String[valeur.size()]);
                    break;
                case CampaignParameter.APPLICATION_PARAMETER:
                    application = valeur.toArray(new String[valeur.size()]);
                    break;
            }
        }

        AnswerList label = campaignLabelService.readByVarious(campaign);
        //AnswerList battery = campaignContentService.readByCampaign(campaign);
        boolean ifLabel = (label.getTotalRows() > 0) ? true : false;
        //boolean ifBattery = (battery.getTotalRows() > 0) ? true : false;

        Integer maxReturn = parameterService.getParameterIntegerByKey("cerberus_campaign_maxtestcase", "", 1000);
        
        if (ifLabel /**|| ifBattery**/) {
            return this.testCaseDao.findTestCaseByCampaignNameAndCountries(campaign, countries, true, status, system, application, priority, maxReturn);
        } else {
            return this.testCaseDao.findTestCaseByCampaignNameAndCountries(campaign, countries, false, status, system, application, priority, maxReturn);
        }
    }

    @Override
    public List<TestCase> findUseTestCaseList(String test, String testCase) throws CerberusException {
        List<TestCase> result = new ArrayList();
        List<TestCaseStep> tcsList = testCaseStepService.getListOfSteps(test, testCase);
        for (TestCaseStep tcs : tcsList) {
            if (("Y").equals(tcs.getUseStep())) {
                result.add(this.findTestCaseByKey(tcs.getUseStepTest(), tcs.getUseStepTestCase()));
            }
        }
        return result;
    }

    @Override
    public List<TestCase> findByCriteria(String[] test, String[] project, String[] app, String[] active, String[] priority, String[] status, String[] group, String[] targetBuild, String[] targetRev, String[] creator, String[] implementer, String[] function, String[] campaign, String[] battery) {
        return testCaseDao.findTestCaseByCriteria(test, project, app, active, priority, status, group, targetBuild, targetRev, creator, implementer, function, campaign, battery);
    }

    @Override
    public String findSystemOfTestCase(String test, String testcase) throws CerberusException {
        return testCaseDao.findSystemOfTestCase(test, testcase);
    }

    @Override
    public AnswerList findTestCasesThatUseTestDataLib(int testDataLibId, String name, String country) {
        return testCaseCountryPropertiesService.findTestCaseCountryPropertiesByValue1(testDataLibId, name, country, TestCaseCountryProperties.TYPE_GETFROMDATALIB);
    }
    
    @Override
    public AnswerList findTestCasesThatUseService(String service) {
    	return  testCaseDao.findTestCaseByService(service);
    }

    @Override
    public AnswerList readTestCaseByStepsInLibrary(String test) {
        return testCaseDao.readTestCaseByStepsInLibrary(test);
    }

    @Override
    public AnswerList readByTestByCriteria(String system, String test, int start, int amount, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch) {
        return testCaseDao.readByTestByCriteria(system, test, start, amount, sortInformation, searchTerm, individualSearch);
    }

    @Override
    public AnswerItem readByKey(String test, String testCase) {
        return testCaseDao.readByKey(test, testCase);
    }

    @Override
    public AnswerItem readByKeyWithDependency(String test, String testCase) {
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
        AnswerItem ai = testCaseDao.readByKey(test, testCase);
        if (ai.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && ai.getItem() != null) {
            TestCase tc = (TestCase) ai.getItem();
            AnswerList al = testCaseStepService.readByTestTestCaseWithDependency(tc.getTest(), tc.getTestCase());
            if (al.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && al.getDataList() != null) {
                tc.setTestCaseStep(al.getDataList());
            }
            answer.setResultMessage(al.getResultMessage());
            answer.setItem(tc);
        }
        return answer;
    }

    @Override
    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return testCaseDao.readDistinctValuesByCriteria(system, test, searchParameter, individualSearch, columnName);
    }

    @Override
    public Answer update(String keyTest, String keyTestCase, TestCase testCase) {
        return testCaseDao.update(keyTest, keyTestCase, testCase);
    }

    @Override
    public Answer create(TestCase testCase) {
        return testCaseDao.create(testCase);
    }

    @Override
    public Answer delete(TestCase testCase) {
        return testCaseDao.delete(testCase);
    }

    @Override
    public TestCase convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestCase) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCase> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<TestCase>) answerList.getDataList();
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
    public boolean hasPermissionsRead(TestCase testCase, HttpServletRequest request) {
        // Access right calculation.
        return true;
    }

    @Override
    public boolean hasPermissionsUpdate(TestCase testCase, HttpServletRequest request) {
        // Access right calculation.
        if (testCase.getStatus().equalsIgnoreCase("WORKING")) { // If testcase is WORKING only TestAdmin can update it
            return request.isUserInRole("TestAdmin");
        } else {
            return request.isUserInRole("Test");
        }
    }

    @Override
    public boolean hasPermissionsCreate(TestCase testCase, HttpServletRequest request) {
        // Access right calculation.
        return request.isUserInRole("Test");
    }

    @Override
    public boolean hasPermissionsDelete(TestCase testCase, HttpServletRequest request) {
        // Access right calculation.
        return request.isUserInRole("TestAdmin");
    }

}
