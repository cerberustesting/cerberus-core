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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.crud.entity.CampaignLabel;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.Label;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseDep;
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTest;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.crud.service.ICampaignLabelService;
import org.cerberus.crud.service.ICampaignParameterService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILabelService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseDepService;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.crud.service.ITestService;
import org.cerberus.dto.TestCaseListDTO;
import org.cerberus.dto.TestListDTO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.execution.IExecutionCheckService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
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
    private ITestService testService;
    @Autowired
    private IFactoryTest factoryTest;
    @Autowired
    private ITestCaseCountryService testCaseCountryService;
    @Autowired
    private IInvariantService invariantService;
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
    private ILabelService labelService;
    @Autowired
    private ICampaignParameterService campaignParameterService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IExecutionCheckService executionCheckService;
    @Autowired
    private ITestCaseDepService testCaseDepService;
    @Autowired
    private ITestCaseLabelService testCaseLabelService;

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
            List<TestCaseCountry> testCaseCountryToAdd = new ArrayList<>();
            for (TestCaseCountry tcc : testCaseCountry) {
                List<TestCaseCountryProperties> properties = testCaseCountryPropertiesService.findListOfPropertyPerTestTestCaseCountry(test, testCase, tcc.getCountry());
                tcc.setTestCaseCountryProperty(properties);
                testCaseCountryToAdd.add(tcc);
            }
            newTcase.setTestCaseCountries(testCaseCountryToAdd);

            List<TestCaseStep> tcs = testCaseStepService.getListOfSteps(test, testCase);
            List<TestCaseStep> tcsToAdd = new ArrayList<>();
            for (TestCaseStep step : tcs) {
                List<TestCaseStepAction> tcsaToAdd = new ArrayList<>();
                if (!step.isUsingLibraryStep()) {
                    List<TestCaseStepAction> tcsa = testCaseStepActionService.getListOfAction(step.getTest(), step.getTestcase(), step.getStepId());
                    for (TestCaseStepAction action : tcsa) {
                        List<TestCaseStepActionControl> tcsacToAdd = new ArrayList<>();
                        List<TestCaseStepActionControl> tcsac = testCaseStepActionControlService.findControlByTestTestCaseStepSequence(action.getTest(), action.getTestCase(), action.getStepId(), action.getSequence());
                        for (TestCaseStepActionControl control : tcsac) {
                            tcsacToAdd.add(control);
                        }
                        action.setControls(tcsacToAdd);
                        tcsaToAdd.add(action);
                    }
                }
                step.setActions(tcsaToAdd);
                tcsToAdd.add(step);
            }
            newTcase.setSteps(tcsToAdd);

            List<TestCaseDep> testCaseDependendies = testCaseDepService.readByTestAndTestCase(test, testCase);
            newTcase.setDependencies(testCaseDependendies);

            List<TestCaseLabel> testCaseLabel = testCaseLabelService.readByTestTestCase(test, testCase, null).getDataList();
            newTcase.setTestCaseLabels(testCaseLabel);

        }
        return newTcase;
    }

    @Override
    public AnswerItem<TestCase> findTestCaseByKeyWithDependencies(String test, String testCase, boolean withSteps) throws CerberusException {

        HashMap<String, TestCaseCountry> testCaseCountries;
        HashMap<String, Invariant> countryInvariants;

        AnswerItem<TestCase> answerTestCase = this.readByKey(test, testCase);
        if (answerTestCase.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerTestCase.getItem() != null) {

            testCaseCountries = testCaseCountryService.readByTestTestCaseToHashCountryAsKey(test, testCase);
            countryInvariants = invariantService.readByIdNameToHash("COUNTRY");

            answerTestCase.getItem().setInvariantCountries(invariantService.convertCountryPropertiesToCountryInvariants(testCaseCountries, countryInvariants));
            answerTestCase.getItem().setDependencies(testCaseDepService.readByTestAndTestCase(answerTestCase.getItem().getTest(), answerTestCase.getItem().getTestcase()));
            answerTestCase.getItem().setLabels(labelService.findLabelsFromTestCase(test, testCase, null).get(testCase));
            List<TestCase> testcases = new ArrayList<>();
            testcases.add(factoryTCase.create(test, testCase));
            answerTestCase.getItem().setTestCaseCountryProperties(testCaseCountryPropertiesService.findDistinctPropertiesOfTestCaseFromTestcaseList(testcases, countryInvariants));

            if (withSteps) {
                answerTestCase.getItem().setSteps(testCaseStepService.readByTestTestCaseStepsWithDependencies(test, testCase).getDataList());
                answerTestCase.getItem().setTestCaseInheritedProperties(testCaseCountryPropertiesService.findDistinctInheritedPropertiesOfTestCase(answerTestCase.getItem(), countryInvariants));
            }
        }
        return answerTestCase;
    }

    @Override
    public List<TestCase> findTestCaseByTest(String test) {
        return testCaseDao.findTestCaseByTest(test);
    }

    @Override
    public AnswerList<TestCase> findTestCasesByTestByCriteriaWithDependencies(List<String> system, String test, int startPosition, int length, String sortInformation, String searchParameter, Map<String, List<String>> individualSearch, boolean isCalledFromListPage) throws CerberusException {

        AnswerList<TestCase> testCases = this.readByTestByCriteria(system, test, startPosition, length, sortInformation, searchParameter, individualSearch);

        if (testCases.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && testCases.getDataList().size() > 0 && isCalledFromListPage) {//the service was able to perform the query, then we should get all values

            HashMap<String, Invariant> countryInvariants = invariantService.readByIdNameToHash("COUNTRY");
            List<TestCaseCountry> testCaseCountries = testCaseCountryService.readByTestTestCase(system, test, null, testCases.getDataList()).getDataList();
            HashMap<String, HashMap<String, TestCaseCountry>> testCaseCountryHash = testCaseCountryService.convertListToHashMapTestTestCaseAsKey(testCaseCountries);
            List<TestCaseDep> testCaseDependencies = testCaseDepService.readByTestAndTestCase(testCases.getDataList());
            HashMap<String, List<TestCaseDep>> testCaseDependenciesHash = testCaseDepService.convertTestCaseDepListToHash(testCaseDependencies);
            HashMap<String, List<Label>> labelsHash = labelService.findLabelsFromTestCase(test, null, testCases.getDataList());

            for (TestCase testCase : testCases.getDataList()) {
                if (testCaseCountryHash.containsKey(testCase.getKey())) {
                    testCase.setInvariantCountries(invariantService.convertCountryPropertiesToCountryInvariants(testCaseCountryHash.get(testCase.getKey()), countryInvariants));
                }
                if (labelsHash.containsKey(testCase.getTest() + "##" + testCase.getTestcase())) {
                    testCase.setLabels(labelsHash.get(testCase.getTest() + "##" + testCase.getTestcase()));
                }
                if (testCaseDependenciesHash.containsKey(testCase.getTestcase())) {
                    testCase.setDependencies(testCaseDependenciesHash.get(testCase.getTestcase()));
                }
            }
        }
        return testCases;
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
    public List<TestCase> getTestCaseForPrePostTesting(String test, String application, String country, String system, String build, String revision) {
        List<TestCase> tmpTests = testCaseDao.findTestCaseByCriteria(test, application, country, "Y");
        List<TestCase> resultTests = new ArrayList<>();
        for (TestCase tmpTest : tmpTests) {
            // We check here if build/revision is compatible.
            if (executionCheckService.checkRangeBuildRevision(tmpTest, build, revision, system)) {
                resultTests.add(tmpTest);
            }
        }
        return resultTests;
    }

    @Override
    public List<TestCase> findTestCaseByAllCriteria(TestCase tCase, String text, String system) {
        return this.testCaseDao.findTestCaseByCriteria(tCase, text, system);
    }

    @Override
    public AnswerList<TestCase> readByVarious(String[] test, String[] app, String[] creator, String[] implementer, String[] system,
            String[] campaign, List<Integer> labelid, String[] priority, String[] type, String[] status, int length) {
        return testCaseDao.readByVarious(test, app, creator, implementer, system, campaign, labelid, priority, type, status, length);
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
        TestCase tCase = factoryTCase.create(null, null, null, null, null, null, null, null,
                null, true, true, false, -1, null, null, null, null, true, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        List<String> result = new ArrayList<>();
        List<TestCase> testCases = findTestCaseByAllCriteria(tCase, null, system);
        for (TestCase testCase : testCases) {
            if (!testCase.getType().equals("PRIVATE")) {
                result.add(testCase.getTest());
            }
        }
        Set<String> uniqueResult = new HashSet<>(result);
        result = new ArrayList<>();
        result.addAll(uniqueResult);
        Collections.sort(result);
        return result;
    }

    @Override
    public List<TestCase> findTestCaseActiveAutomatedBySystem(String test, String system) {
        TestCase tCase = factoryTCase.create(test, null, null, null, null, null, null, null,
                null, true, true, false, -1, null, null, null, null, true, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        List<TestCase> result = new ArrayList<>();
        List<TestCase> testCases = findTestCaseByAllCriteria(tCase, null, system);
        for (TestCase testCase : testCases) {
            if (!testCase.getType().equals("PRIVATE")) {
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
    public String getMaxNumberTestCase(String test) {
        String result = testCaseDao.getMaxNumberTestCase(test);
        if (result == null) {
            return "0001A";
        }
        int resultInt = 0;
        try {
            resultInt = Integer.valueOf(result);
        } catch (NumberFormatException e) {
            LOG.debug("Could not convert '" + result + "' to Integer.");
        }
        resultInt++;
        return String.format("%04dA", resultInt);
    }

    @Override
    public AnswerList<TestCase> findTestCaseByCampaign(String campaign) {

        final AnswerItem<Map<String, List<String>>> parsedCampaignParameters = campaignParameterService.parseParametersByCampaign(campaign);

        List<String> countries = parsedCampaignParameters.getItem().get(CampaignParameter.COUNTRY_PARAMETER);
        AnswerList<TestCase> testCases = null;

        if (countries != null && !countries.isEmpty()) {
            testCases = this.findTestCaseByCampaignNameAndCountries(campaign, countries.toArray(new String[countries.size()]));
        } else {
            testCases = this.findTestCaseByCampaignNameAndCountries(campaign, null);
        }

        return testCases;
    }

    @Override
    public AnswerList<TestCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries) {
        AnswerList<TestCase> result = new AnswerList<>();
        String[] status = null;
        String[] system = null;
        String[] application = null;
        String[] priority = null;
        String[] type = null;

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
                case CampaignParameter.TESTCASE_TYPE_PARAMETER:
                    type = valeur.toArray(new String[valeur.size()]);
                    break;
            }
        }

        AnswerList<CampaignLabel> label = campaignLabelService.readByVarious(campaign);

        List<Integer> labelIdList = new ArrayList<>();
        List<CampaignLabel> labelList = label.getDataList();
        for (CampaignLabel campaignLabel : labelList) {
            labelIdList.add(campaignLabel.getLabelId());
        }

        labelIdList = labelService.enrichWithChild(labelIdList);

        Integer maxReturn = parameterService.getParameterIntegerByKey("cerberus_campaign_maxtestcase", "", 1000);

        result = testCaseDao.findTestCaseByCampaignNameAndCountries(campaign, countries, labelIdList, status, system, application, priority, type, maxReturn);

        return result;
    }

    @Override
    public List<TestCase> findUseTestCaseList(String test, String testCase) throws CerberusException {
        List<TestCase> result = new ArrayList<>();
        List<TestCaseStep> tcsList = testCaseStepService.getListOfSteps(test, testCase);
        for (TestCaseStep tcs : tcsList) {
            if (tcs.isUsingLibraryStep()) {
                /**
                 * We prepend the TestCase in order to leave at the end of the
                 * list the testcase with the higher prio (which correspond to
                 * the 1st Use Step found) #1907. That way, if inside the same
                 * testcase, you import 2 use Step that define a property that
                 * has the same name, the 1st step imported will define the
                 * property value.
                 */
                result.add(0, this.findTestCaseByKey(tcs.getLibraryStepTest(), tcs.getLibraryStepTestcase()));
            }
        }
        return result;
    }

    @Override
    public List<TestCase> findByCriteria(String[] test, String[] app, String[] active, String[] priority, String[] status, String[] type, String[] targetMajor, String[] targetMinor, String[] creator, String[] implementer, String[] campaign, String[] battery) {
        return testCaseDao.findTestCaseByCriteria(test, app, active, priority, status, type, targetMajor, targetMinor, creator, implementer, campaign);
    }

    @Override
    public String findSystemOfTestCase(String test, String testcase) throws CerberusException {
        return testCaseDao.findSystemOfTestCase(test, testcase);
    }

    @Override
    public AnswerList<TestListDTO> findTestCasesThatUseTestDataLib(int testDataLibId, String name, String country) {
        return testCaseCountryPropertiesService.findTestCaseCountryPropertiesByValue1(testDataLibId, name, country, TestCaseCountryProperties.TYPE_GETFROMDATALIB);
    }

    public boolean containsTestCase(final List<TestCaseListDTO> list, final String number) {
        return list.stream().filter(o -> o.getTestCaseNumber().equals(number)).findFirst().isPresent();
    }

    @Override
    public AnswerList<TestListDTO> findTestCasesThatUseService(String service) {

        AnswerList<TestListDTO> testCaseByServiceByDataLib = testCaseDao.findTestCaseByServiceByDataLib(service);
        AnswerList<TestListDTO> testCaseByService = testCaseDao.findTestCaseByService(service);
        List<TestListDTO> listOfTestCaseByDataLib = testCaseByServiceByDataLib.getDataList();
        List<TestListDTO> listOfTestCaseByService = testCaseByService.getDataList();
        List<TestListDTO> newTestCase = new ArrayList<>();

        if (!listOfTestCaseByDataLib.isEmpty()) {
            for (TestListDTO datalibList : listOfTestCaseByDataLib) {
                for (TestListDTO serviceList : listOfTestCaseByService) {
                    if (datalibList.getTest().equals(serviceList.getTest())) {
                        List<TestCaseListDTO> testCaseDataLibList = datalibList.getTestCaseList();
                        for (TestCaseListDTO testCaseService : serviceList.getTestCaseList()) {
                            if (!containsTestCase(testCaseDataLibList, testCaseService.getTestCaseNumber())) {
                                testCaseDataLibList.add(testCaseService);
                            }
                        }
                    } else {
                        newTestCase.add(serviceList);
                    }
                }
            }
            listOfTestCaseByDataLib.addAll(newTestCase);
            testCaseByServiceByDataLib.setDataList(listOfTestCaseByDataLib);
            return testCaseByServiceByDataLib;
        } else {
            return testCaseByService;
        }
    }

    @Override
    public AnswerList readTestCaseByStepsInLibrary(String test) {
        return testCaseDao.readTestCaseByStepsInLibrary(test);
    }

    @Override
    public AnswerList<TestCase> readByTestByCriteria(List<String> system, String test, int start, int amount, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch) {
        return testCaseDao.readByTestByCriteria(system, test, start, amount, sortInformation, searchTerm, individualSearch);
    }

    @Override
    public AnswerItem<TestCase> readByKey(String test, String testCase) {
        return testCaseDao.readByKey(test, testCase);
    }

    @Override
    public AnswerItem<TestCase> readByKeyWithDependency(String test, String testCase) {
        AnswerItem<TestCase> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
        AnswerItem<TestCase> ai = testCaseDao.readByKey(test, testCase);
        if (ai.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && ai.getItem() != null) {
            TestCase tc = (TestCase) ai.getItem();
            AnswerList<TestCaseStep> al = testCaseStepService.readByTestTestCaseStepsWithDependencies(tc.getTest(), tc.getTestcase());
            if (al.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && al.getDataList() != null) {
                tc.setSteps(al.getDataList());
            }
            answer.setResultMessage(al.getResultMessage());
            answer.setItem(tc);
        }
        return answer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return testCaseDao.readDistinctValuesByCriteria(system, test, searchParameter, individualSearch, columnName);
    }

    @Override
    public AnswerList<TestCase> readStatsBySystem(List<String> system, Date to) {
        return testCaseDao.readStatsBySystem(system, to);
    }

    @Override
    public Answer update(String keyTest, String keyTestCase, TestCase testCase) {
        // We first create the corresponding test if it doesn,'t exist.
        if (testCase.getTest() != null) {
            if (!testService.exist(testCase.getTest())) {
                testService.create(factoryTest.create(testCase.getTest(), "", true, null, testCase.getUsrModif(), null, "", null));
            }
        }
        return testCaseDao.update(keyTest, keyTestCase, testCase);
    }

    @Override
    public Answer create(TestCase testCase) {
        // We first create the corresponding test if it doesn,'t exist.
        if (testCase.getTest() != null) {
            if (!testService.exist(testCase.getTest())) {
                testService.create(factoryTest.create(testCase.getTest(), "", true, null, testCase.getUsrCreated(), null, "", null));
            }
        }
        return testCaseDao.create(testCase);
    }

    @Override
    public Answer delete(TestCase testCase) {
        return testCaseDao.delete(testCase);
    }

    @Override
    public TestCase convert(AnswerItem<TestCase> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestCase) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCase> convert(AnswerList<TestCase> answerList) throws CerberusException {
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

    @Override
    public void importWithDependency(TestCase testCase) throws CerberusException {

        //TODO ------------------------
        //Check Cerberus version compatibility. If not stop
        //-------------------------------
        //insert testcase
        Answer testCaseImported = this.create(testCase);
        if (!testCaseImported.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
            MessageGeneral msg = new MessageGeneral(MessageGeneralEnum.GENERIC_ERROR);
            msg.setDescription(testCaseImported.getResultMessage().getDescription());
            throw new CerberusException(msg);
        }

        //for tcstep, insert steps
        for (TestCaseStep tcs : testCase.getSteps()) {
            tcs.setTest(testCase.getTest());
            tcs.setTestcase(testCase.getTestcase());
            Answer testCaseStepImported = testCaseStepService.create(tcs);
            if (!testCaseStepImported.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                throw new CerberusException(new MessageGeneral(testCaseStepImported.getResultMessage().getMessage()));
            }

            for (TestCaseStepAction tcsa : tcs.getActions()) {
                tcsa.setTest(testCase.getTest());
                tcsa.setTestCase(testCase.getTestcase());
                Answer testCaseStepActionImported = testCaseStepActionService.create(tcsa);
                if (!testCaseStepActionImported.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                    throw new CerberusException(new MessageGeneral(testCaseStepActionImported.getResultMessage().getMessage()));
                }

                for (TestCaseStepActionControl tcsac : tcsa.getControls()) {
                    tcsac.setTest(testCase.getTest());
                    tcsac.setTestCase(testCase.getTestcase());
                    Answer testCaseStepActionControlImported = testCaseStepActionControlService.create(tcsac);
                    if (!testCaseStepActionControlImported.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                        throw new CerberusException(new MessageGeneral(testCaseStepActionControlImported.getResultMessage().getMessage()));
                    }
                }
            }
        }

        //insert tccountry, insert countries
        for (TestCaseCountry tcc : testCase.getTestCaseCountries()) {
            tcc.setTest(testCase.getTest());
            tcc.setTestCase(testCase.getTestcase());
            Answer testCaseCountryImported = testCaseCountryService.create(tcc);
            if (!testCaseCountryImported.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                throw new CerberusException(new MessageGeneral(testCaseCountryImported.getResultMessage().getMessage()));
            }

            for (TestCaseCountryProperties tccp : tcc.getTestCaseCountryProperty()) {
                tccp.setTest(testCase.getTest());
                tccp.setTestcase(testCase.getTestcase());
                Answer testCaseCountryPropertiesImported = testCaseCountryPropertiesService.create(tccp);
                if (!testCaseCountryPropertiesImported.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                    throw new CerberusException(new MessageGeneral(testCaseCountryPropertiesImported.getResultMessage().getMessage()));
                }
            }
        }

        //insert testcasedependencies
        for (TestCaseDep tcd : testCase.getDependencies()) {
            tcd.setTest(testCase.getTest());
            tcd.setTestCase(testCase.getTestcase());
            testCaseDepService.create(tcd);
        }

        //insert testcaselabel
        for (TestCaseLabel tcl : testCase.getTestCaseLabels()) {
            tcl.setTest(testCase.getTest());
            tcl.setTestcase(testCase.getTestcase());
            testCaseLabelService.create(tcl);
        }

    }

}
