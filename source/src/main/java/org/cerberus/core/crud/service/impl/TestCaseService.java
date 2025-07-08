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
import java.text.SimpleDateFormat;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.FailedInsertOperationException;
import org.cerberus.core.api.exceptions.InvalidRequestException;
import org.cerberus.core.crud.dao.ITestCaseDAO;
import org.cerberus.core.crud.entity.CampaignLabel;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseDep;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.factory.IFactoryTest;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.service.ICampaignLabelService;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseDepService;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.dto.TestCaseListDTO;
import org.cerberus.core.dto.TestListDTO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.execution.IExecutionCheckService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.event.IEventService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.TestCaseHisto;
import org.cerberus.core.crud.service.ITestCaseHistoService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    @Autowired
    private IEventService eventService;
    @Autowired
    private ITestCaseHistoService testCaseHistoService;

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
                        List<TestCaseStepActionControl> tcsac = testCaseStepActionControlService.findControlByTestTestCaseStepIdActionId(action.getTest(), action.getTestcase(), action.getStepId(), action.getActionId());
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

            List<TestCaseDep> testCaseDependendies = testCaseDepService.readByTestAndTestcase(test, testCase);
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
            countryInvariants = (HashMap<String, Invariant>) invariantService.readByIdNameToHash("COUNTRY");

            answerTestCase.getItem().setInvariantCountries(invariantService.convertCountryPropertiesToCountryInvariants(testCaseCountries, countryInvariants));
            answerTestCase.getItem().setDependencies(testCaseDepService.readByTestAndTestcase(answerTestCase.getItem().getTest(), answerTestCase.getItem().getTestcase()));
            answerTestCase.getItem().setLabels(labelService.findLabelsFromTestCase(test, testCase, null).get(test + "##" + testCase));
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
    public Integer getnbtc(List<String> systems) {
        return testCaseDao.getnbtc(systems);
    }

    @Override
    public AnswerList<TestCase> findTestCasesByTestByCriteriaWithDependencies(List<String> system, String test, int startPosition, int length, String sortInformation, String searchParameter, Map<String, List<String>> individualSearch, boolean isCalledFromListPage) throws CerberusException {

        AnswerList<TestCase> testCases = this.readByTestByCriteria(system, test, startPosition, length, sortInformation, searchParameter, individualSearch);

        if (testCases.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && !testCases.getDataList().isEmpty() && isCalledFromListPage) {//the service was able to perform the query, then we should get all values

            HashMap<String, Invariant> countryInvariants = (HashMap<String, Invariant>) invariantService.readByIdNameToHash("COUNTRY");
            List<TestCaseCountry> testCaseCountries = testCaseCountryService.readByTestTestCase(system, test, null, testCases.getDataList()).getDataList();
            HashMap<String, HashMap<String, TestCaseCountry>> testCaseCountryHash = testCaseCountryService.convertListToHashMapTestTestCaseAsKey(testCaseCountries);
            List<TestCaseDep> testCaseDependencies = testCaseDepService.readByTestAndTestcase(testCases.getDataList());
            HashMap<String, List<TestCaseDep>> testCaseDependenciesHash = testCaseDepService.convertTestCaseDependencyListToHash(testCaseDependencies);
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
    public String getNextAvailableTestcaseId(String test) {
        String result = testCaseDao.getMaxTestcaseIdByTestFolder(test);
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
        AnswerList<TestCase> result;
        String[] status = null;
        String[] system = null;
        String[] application = null;
        String[] priority = null;
        String[] type = null;
        String[] testFolder = null;

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
                case CampaignParameter.TYPE_PARAMETER:
                    type = valeur.toArray(new String[valeur.size()]);
                    break;
                case CampaignParameter.TYPE_TESTFOLDER:
                    testFolder = valeur.toArray(new String[valeur.size()]);
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

        result = testCaseDao.findTestCaseByCampaignNameAndCountries(campaign, countries, labelIdList, status, system, application, priority, type, testFolder, maxReturn);

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
        return list.stream().anyMatch(testCaseListDTO -> testCaseListDTO.getTestCaseNumber().equals(number));
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
            TestCase tc = ai.getItem();
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
    public Answer update(String keyTest, String keyTestcase, TestCase testcase) {
        // We first create the corresponding test if it doesn,'t exist.
        if (testcase.getTest() != null && !testService.exist(testcase.getTest())) {
            testService.create(factoryTest.create(testcase.getTest(), "", true, null, testcase.getUsrModif(), null, "", null));
        }

        Answer ans = testCaseDao.update(keyTest, keyTestcase, testcase);
        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            eventService.triggerEvent(EventHook.EVENTREFERENCE_TESTCASE_UPDATE, testcase, keyTest, keyTestcase, null);
        }
        return ans;
    }

    @Override
    public void updateApplicationObject(String application, String oldObject, String newObject) {
        try {
            testCaseDao.updateApplicationObject("ConditionValue1", application, oldObject, newObject);
            testCaseDao.updateApplicationObject("ConditionValue2", application, oldObject, newObject);
            testCaseDao.updateApplicationObject("ConditionValue3", application, oldObject, newObject);

            testCaseDao.updateApplicationObject("Description", application, oldObject, newObject);
        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
        }
    }

    @Override
    public void updateLastExecuted(String test, String testCase, Timestamp lastExecuted) {
        try {
            testCaseDao.updateLastExecuted(test, testCase, lastExecuted);
        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
        }
    }

    @Override
    public boolean isBugAlreadyOpen(TestCase tc) {
        try {
            JSONArray bugList = tc.getBugs();
            for (int n = 0; n < bugList.length(); n++) {
                JSONObject bug = bugList.getJSONObject(n);
                if (bug.has("act")) {
                    if (bug.getBoolean("act")) {
                        return true;
                    }
                }
            }
        } catch (JSONException ex) {
            LOG.warn(ex, ex);
        }
        return false;
    }

    @Override
    public JSONObject addNewBugEntry(TestCase tc, String testFolder, String testCase, String bugKey, String bugURL, String description) {
        JSONObject bugCreated = new JSONObject();
        try {
            JSONArray bugList = tc.getBugs();
            JSONObject newBug = new JSONObject();
            newBug.put("act", true);
            newBug.put("dateCreated", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(new java.util.Date()));
            newBug.put("id", bugKey);
            newBug.put("dateClosed", "1970-01-01T00:00:00.000Z");
            newBug.put("desc", description);
            newBug.put("url", bugURL);
            bugList.put(newBug);
            tc.setBugs(bugList);
            testCaseDao.updateBugList(testFolder, testCase, bugList.toString());
            return newBug;
        } catch (JSONException | CerberusException ex) {
            LOG.warn(ex, ex);
        }
        return bugCreated;
    }

    @Override
    public Answer create(TestCase testCase) {
        // We first create the corresponding test if it doesn,'t exist.
        if (testCase.getTest() != null && !testService.exist(testCase.getTest())) {
            testService.create(factoryTest.create(testCase.getTest(), "", true, null, testCase.getUsrCreated(), null, "", null));

        }
        Answer ans = testCaseDao.create(testCase);
        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            eventService.triggerEvent(EventHook.EVENTREFERENCE_TESTCASE_CREATE, testCase, null, null, null);
        }
        return ans;
    }

    @Override
    public Answer createAPI(TestCase testCase) {
        // We first create the corresponding test if it doesn,'t exist.
        if (testCase.getTest() != null && !testService.exist(testCase.getTest())) {
            testService.create(factoryTest.create(testCase.getTest(), "", true, null, testCase.getUsrCreated(), null, "", null));
        }
        return testCaseDao.create(testCase);
    }

    @Override
    public Answer delete(TestCase testCase) {
        Answer ans = testCaseDao.delete(testCase);
        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            eventService.triggerEvent(EventHook.EVENTREFERENCE_TESTCASE_DELETE, testCase, null, null, null);
        }
        return ans;
    }

    @Override
    public TestCase convert(AnswerItem<TestCase> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCase> convert(AnswerList<TestCase> answerList) throws CerberusException {
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
    public boolean hasPermissionsRead(TestCase testCase, HttpServletRequest request) {
        // Access right calculation.
        return true;
    }

    @Override
    public boolean hasPermissionsUpdate(TestCase testCase, HttpServletRequest request) {
        // Access right calculation.
        if (testCase.getStatus().equalsIgnoreCase(TestCase.TESTCASE_STATUS_WORKING)) { // If testcase is WORKING only TestAdmin can update it
            return request.isUserInRole("TestAdmin");
        } else {
            return request.isUserInRole("Test");
        }
    }

    @Override
    public boolean hasPermissionsUpdateFromStatus(String status, HttpServletRequest request) {
        // Access right calculation.
        if (status.equalsIgnoreCase(TestCase.TESTCASE_STATUS_WORKING)) { // If testcase is WORKING only TestAdmin can update it
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
    public void createTestcaseWithDependencies(TestCase testCase) throws CerberusException {

        //insert testcase
        Answer newTestcase = this.create(testCase);

        if (!newTestcase.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
            MessageGeneral msg = new MessageGeneral(MessageGeneralEnum.GENERIC_ERROR);
            msg.setDescription(newTestcase.getResultMessage().getDescription());
            throw new CerberusException(msg);
        }

        //for tcstep, insert steps
        for (TestCaseStep tcs : testCase.getSteps()) {
            tcs.setTest(testCase.getTest());
            tcs.setTestcase(testCase.getTestcase());
            Answer newTestcaseStep = testCaseStepService.create(tcs);
            if (!newTestcaseStep.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                throw new CerberusException(new MessageGeneral(newTestcaseStep.getResultMessage().getMessage()));
            }

            for (TestCaseStepAction tcsa : tcs.getActions()) {
                tcsa.setTest(testCase.getTest());
                tcsa.setTestcase(testCase.getTestcase());
                Answer newTestcaseStepAction = testCaseStepActionService.create(tcsa);
                if (!newTestcaseStepAction.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                    throw new CerberusException(new MessageGeneral(newTestcaseStepAction.getResultMessage().getMessage()));
                }

                for (TestCaseStepActionControl tcsac : tcsa.getControls()) {
                    tcsac.setTest(testCase.getTest());
                    tcsac.setTestcase(testCase.getTestcase());
                    Answer newTestcaseStepActionControl = testCaseStepActionControlService.create(tcsac);
                    if (!newTestcaseStepActionControl.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                        throw new CerberusException(new MessageGeneral(newTestcaseStepActionControl.getResultMessage().getMessage()));
                    }
                }
            }
        }

        //insert tccountry, insert countries
        for (TestCaseCountry tcc : testCase.getTestCaseCountries()) {
            tcc.setTest(testCase.getTest());
            tcc.setTestcase(testCase.getTestcase());
            Answer newTestcaseCountry = testCaseCountryService.create(tcc);
            if (!newTestcaseCountry.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                throw new CerberusException(new MessageGeneral(newTestcaseCountry.getResultMessage().getMessage()));
            }

            for (TestCaseCountryProperties tccp : tcc.getTestCaseCountryProperty()) {
                tccp.setTest(testCase.getTest());
                tccp.setTestcase(testCase.getTestcase());
                Answer newTestcaseCountryProperties = testCaseCountryPropertiesService.create(tccp);
                if (!newTestcaseCountryProperties.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                    throw new CerberusException(new MessageGeneral(newTestcaseCountryProperties.getResultMessage().getMessage()));
                }
            }
        }

        //insert testcasedependencies
        for (TestCaseDep tcd : testCase.getDependencies()) {
            tcd.setTest(testCase.getTest());
            tcd.setTestcase(testCase.getTestcase());
            testCaseDepService.create(tcd);
        }

        //insert testcaselabel
        for (TestCaseLabel tcl : testCase.getTestCaseLabels()) {
            tcl.setTest(testCase.getTest());
            tcl.setTestcase(testCase.getTestcase());
            testCaseLabelService.create(tcl);
        }

    }

    @Override
    public TestCase createTestcaseWithDependenciesAPI(TestCase newTestcase) throws CerberusException {

        final String FAILED_TO_INSERT = "Failed to insert the testcase in the database";

        if (StringUtil.isEmptyOrNull(newTestcase.getTest())) {
            throw new InvalidRequestException("testFolderId required to create Testcase");
        }

        if (StringUtil.isEmptyOrNull(newTestcase.getApplication())) {
            throw new InvalidRequestException("application is required to create a Testcase");
        }

        if (StringUtil.isEmptyOrNull(newTestcase.getTestcase())) {
            newTestcase.setTestcase(this.getNextAvailableTestcaseId(newTestcase.getTest()));
        }
        Answer testcaseCreationAnswer = this.create(newTestcase);

        if (!testcaseCreationAnswer.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
            throw new FailedInsertOperationException(FAILED_TO_INSERT);
        }

        //for tcstep, insert steps
        if (newTestcase.getSteps() != null) {
            int stepId = 0;
            for (TestCaseStep step : newTestcase.getSteps()) {
                step.setTest(newTestcase.getTest());
                step.setTestcase(newTestcase.getTestcase());
                step.setStepId(stepId++);
                Answer newTestcaseStep = testCaseStepService.create(step);
                if (!newTestcaseStep.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                    throw new FailedInsertOperationException(FAILED_TO_INSERT);
                }

                if (step.getActions() != null && !step.isUsingLibraryStep()) {
                    int actionId = 0;
                    for (TestCaseStepAction action : step.getActions()) {
                        action.setTest(newTestcase.getTest());
                        action.setTestcase(newTestcase.getTestcase());
                        action.setStepId(step.getStepId());
                        action.setActionId(actionId++);
                        Answer newTestcaseStepAction = testCaseStepActionService.create(action);
                        if (!newTestcaseStepAction.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                            throw new FailedInsertOperationException(FAILED_TO_INSERT);
                        }

                        if (action.getControls() != null) {
                            int controlId = 0;
                            for (TestCaseStepActionControl control : action.getControls()) {
                                control.setTest(newTestcase.getTest());
                                control.setTestcase(newTestcase.getTestcase());
                                control.setStepId(step.getStepId());
                                control.setActionId(action.getActionId());
                                control.setControlId(controlId++);
                                Answer newTestcaseStepActionControl = testCaseStepActionControlService.create(control);
                                if (!newTestcaseStepActionControl.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                                    throw new FailedInsertOperationException(FAILED_TO_INSERT);
                                }
                            }
                        }
                    }
                }
            }
        }

        this.fillTestcaseCountriesFromInvariantsCountry(newTestcase);
        if (CollectionUtils.isNotEmpty(newTestcase.getTestCaseCountryProperties())) {
            newTestcase.setTestCaseCountryProperties(
                    this.testCaseCountryPropertiesService.getFlatListOfTestCaseCountryPropertiesFromAggregate(
                            newTestcase.getTestCaseCountryProperties()
                    )
            );
        }

        for (TestCaseCountry tcc : newTestcase.getTestCaseCountries()) {
            tcc.setTest(newTestcase.getTest());
            tcc.setTestcase(newTestcase.getTestcase());
            Answer newTestcaseCountry = testCaseCountryService.create(tcc);
            if (!newTestcaseCountry.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                throw new FailedInsertOperationException(FAILED_TO_INSERT);
            }

            if (tcc.getTestCaseCountryProperty() != null) {
                for (TestCaseCountryProperties tccp : tcc.getTestCaseCountryProperty()) {
                    tccp.setTest(newTestcase.getTest());
                    tccp.setTestcase(newTestcase.getTestcase());
                    Answer newTestcaseCountryProperties = testCaseCountryPropertiesService.create(tccp);
                    if (!newTestcaseCountryProperties.getResultMessage().getSource().equals(MessageEventEnum.DATA_OPERATION_OK)) {
                        throw new FailedInsertOperationException(FAILED_TO_INSERT);
                    }
                }
            }
        }

        //insert testcasedependencies
        if (CollectionUtils.isNotEmpty(newTestcase.getDependencies())) {
            for (TestCaseDep tcd : newTestcase.getDependencies()) {
                tcd.setTest(newTestcase.getTest());
                tcd.setTestcase(newTestcase.getTestcase());
                try {
                    testCaseDepService.create(tcd);
                } catch (CerberusException ex) {
                    throw new FailedInsertOperationException(ex.getMessage());
                }
            }
        }

        //insert testcaselabel
        if (CollectionUtils.isNotEmpty(newTestcase.getLabels())) {
            newTestcase.setTestCaseLabels(
                    this.getTestcaseLabelsFromLabels(
                            newTestcase.getLabels(), newTestcase.getTest(), newTestcase.getTestcase(), newTestcase.getUsrCreated()
                    )
            );
            this.testCaseLabelService.createList(newTestcase.getTestCaseLabels());
        }

        return this.findTestCaseByKeyWithDependencies(newTestcase.getTest(), newTestcase.getTestcase(), true).getItem();
    }

    @Override
    public TestCase updateTestcaseAPI(String testFolderId, String testcaseId, TestCase newTestcaseVersion) throws CerberusException {

        if (testFolderId == null || testFolderId.isEmpty()) {
            throw new InvalidRequestException("testFolderId is required to update a testcase");
        }

        if (testcaseId == null || testcaseId.isEmpty()) {
            throw new InvalidRequestException("testcaseId is required to update a testcase");
        }

        TestCase oldTestcaseVersion = new TestCase();
        try {
            oldTestcaseVersion = this.findTestCaseByKeyWithDependencies(newTestcaseVersion.getTest(), newTestcaseVersion.getTestcase(), true).getItem();
            if (oldTestcaseVersion == null) {
                throw new EntityNotFoundException(TestCase.class, "testcaseFolderId", newTestcaseVersion.getTest(), "testcaseId", newTestcaseVersion.getTestcase());
            }
        } catch (CerberusException e) {
            LOG.warn(e.getMessage());
        }

        if (!newTestcaseVersion.equals(oldTestcaseVersion)) {
            newTestcaseVersion.setVersion(newTestcaseVersion.getVersion() + 1);
            LOG.debug(this.updateTestCaseInformation(newTestcaseVersion));
        }

        if (CollectionUtils.isNotEmpty(newTestcaseVersion.getSteps())) {
            this.testCaseStepService.compareListAndUpdateInsertDeleteElements(newTestcaseVersion.getSteps(), oldTestcaseVersion.getSteps(), false);

            List<TestCaseStepAction> newActions = this.getAllActionsFromTestcase(newTestcaseVersion);
            List<TestCaseStepAction> oldActions = this.testCaseStepActionService.readByTestTestCase(testFolderId, testcaseId).getDataList();
            this.testCaseStepActionService.compareListAndUpdateInsertDeleteElements(newActions, oldActions, false);

            List<TestCaseStepActionControl> newControls = this.getAllControlsFromTestcase(newTestcaseVersion);
            List<TestCaseStepActionControl> oldControls = this.testCaseStepActionControlService.findControlByTestTestCase(testFolderId, testcaseId);
            this.testCaseStepActionControlService.compareListAndUpdateInsertDeleteElements(newControls, oldControls, false);
        }

        this.fillTestcaseCountriesFromInvariantsCountry(newTestcaseVersion);
        this.testCaseCountryService.compareListAndUpdateInsertDeleteElements(
                newTestcaseVersion.getTest(),
                newTestcaseVersion.getTestcase(),
                newTestcaseVersion.getTestCaseCountries()
        );

        // Save histo entry
        this.testCaseHistoService.create(TestCaseHisto.builder()
                .test(oldTestcaseVersion.getTest())
                .testCase(oldTestcaseVersion.getTestcase())
                .version(oldTestcaseVersion.getVersion())
                .usrCreated(newTestcaseVersion.getUsrCreated())
                .testCaseContent(new JSONObject())
                .description("")
                .build());

        if (CollectionUtils.isNotEmpty(newTestcaseVersion.getTestCaseCountryProperties())) {
            newTestcaseVersion.setTestCaseCountryProperties(
                    this.testCaseCountryPropertiesService
                            .getFlatListOfTestCaseCountryPropertiesFromAggregate(newTestcaseVersion.getTestCaseCountryProperties())
            );
        }

        if (CollectionUtils.isNotEmpty(newTestcaseVersion.getTestCaseCountryProperties())) {
            this.testCaseCountryPropertiesService.compareListAndUpdateInsertDeleteElements(
                    newTestcaseVersion.getTest(),
                    newTestcaseVersion.getTestcase(),
                    newTestcaseVersion.getTestCaseCountryProperties()
            );
        }

        if (CollectionUtils.isNotEmpty(newTestcaseVersion.getDependencies())) {
            this.testCaseDepService.compareListAndUpdateInsertDeleteElements(
                    newTestcaseVersion.getTest(),
                    newTestcaseVersion.getTestcase(),
                    newTestcaseVersion.getDependencies()
            );
        }

        if (CollectionUtils.isNotEmpty(newTestcaseVersion.getLabels())) {
            newTestcaseVersion.setTestCaseLabels(
                    this.getTestcaseLabelsFromLabels(
                            newTestcaseVersion.getLabels(),
                            newTestcaseVersion.getTest(),
                            newTestcaseVersion.getTestcase(),
                            newTestcaseVersion.getUsrCreated()
                    )
            );
            this.testCaseLabelService.compareListAndUpdateInsertDeleteElements(
                    newTestcaseVersion.getTest(),
                    newTestcaseVersion.getTestcase(),
                    newTestcaseVersion.getTestCaseLabels()
            );
        }
        return this.findTestCaseByKeyWithDependencies(newTestcaseVersion.getTest(), newTestcaseVersion.getTestcase(), true).getItem();
    }

    @Override
    public String getRefOriginUrl(String origin, String refOrigin, String system) {
        String url = "";
        switch (origin) {
            case TestCase.TESTCASE_ORIGIN_JIRACLOUD:
                url = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_jiracloud_url, system, "");
                if (StringUtil.isNotEmptyOrNull(url)) {
                    return StringUtil.addSuffixIfNotAlready(url, "/") + "browse/" + refOrigin;
                } else {
                    return "";
                }
            case TestCase.TESTCASE_ORIGIN_JIRADC:
                url = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_jiradc_url, system, "");
                if (StringUtil.isNotEmptyOrNull(url)) {
                    return StringUtil.addSuffixIfNotAlready(url, "/") + "browse/" + refOrigin;
                } else {
                    return "";
                }
            default:
                return null;
        }
    }

    private void fillTestcaseCountriesFromInvariantsCountry(TestCase testcase) {
        if (testcase.getInvariantCountries() == null || testcase.getInvariantCountries().isEmpty()) {
            try {
                testcase.setInvariantCountries(this.invariantService.readByIdName("COUNTRY"));
            } catch (CerberusException e) {
                LOG.warn("Unable to retrieve countries from invariant table" + e);
            }
        }

        testcase.getInvariantCountries()
                .forEach(invariantCountry -> testcase.appendTestCaseCountries(
                TestCaseCountry.builder()
                        .test(testcase.getTest())
                        .testcase(testcase.getTestcase())
                        .country(invariantCountry.getValue())
                        .build()
        ));
    }

    private List<TestCaseLabel> getTestcaseLabelsFromLabels(List<Label> labels, String testFolderId, String testcaseId, String userCreated) {
        return labels
                .stream()
                .map(label -> TestCaseLabel.builder()
                .test(testFolderId)
                .testcase(testcaseId)
                .labelId(label.getId())
                .usrCreated(userCreated)
                .label(label)
                .build())
                .collect(Collectors.toList());
    }

    private List<TestCaseStepAction> getAllActionsFromTestcase(TestCase testcase) {
        return testcase.getSteps()
                .stream()
                .filter(step -> !step.isUsingLibraryStep())
                .flatMap(testCaseStep -> testCaseStep.getActions().stream())
                .collect(Collectors.toList());
    }

    private List<TestCaseStepActionControl> getAllControlsFromTestcase(TestCase testcase) {
        return testcase.getSteps()
                .stream()
                .filter(step -> !step.isUsingLibraryStep())
                .flatMap(testCaseStep -> testCaseStep.getActions()
                .stream()
                .flatMap(testCaseStepAction -> testCaseStepAction.getControls().stream())
                )
                .collect(Collectors.toList());
    }
}
