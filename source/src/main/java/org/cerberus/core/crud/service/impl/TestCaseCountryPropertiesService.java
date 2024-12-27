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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.core.crud.dao.ITestCaseStepActionDAO;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseDepService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.dto.TestListDTO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bcivel
 * @author FNogueira
 */
@Service
public class TestCaseCountryPropertiesService implements ITestCaseCountryPropertiesService {

    @Autowired
    ITestCaseCountryPropertiesDAO testCaseCountryPropertiesDAO;
    @Autowired
    ITestCaseStepActionDAO testCaseStepActionDAO;
    @Autowired
    ITestCaseService testCaseService;
    @Autowired
    ITestCaseDepService testCaseDepService;
    @Autowired
    IParameterService parameterService;
    @Autowired
    private DatabaseSpring dbmanager;
    @Autowired
    IInvariantService invariantService;
    @Autowired
    IFactoryTestCase factoryTestCase;

    private final String OBJECT_NAME = "TestCaseCountryProperties";
    private static final String SEPARATOR = "%#/";

    private static final Logger LOG = LogManager.getLogger(TestCaseCountryPropertiesService.class);

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testCase, String country) {
        return testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCaseCountry(test, testCase, country);
    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseProperty(String test, String testcase, String oneproperty) {
        return testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCaseProperty(test, testcase, oneproperty);
    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCase(String test, String testcase) throws CerberusException {
        return testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(test, testcase);
    }

    @Override
    public List<TestCaseCountryProperties> findDistinctPropertiesOfTestCaseFromTestcaseList(List<TestCase> testCaseList, HashMap<String, Invariant> countryInvariants) throws CerberusException {
        // Getting all properties in the scope of the testcase list.
        List<TestCaseCountryProperties> allProperties = testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCaseList(testCaseList);
        // Now building the distinct values based on distinct information.
        Map<String, TestCaseCountryProperties> testCasePropHash = new HashMap<>();
        for (TestCaseCountryProperties property : allProperties) {
            String key = getPropertyDistinctKey(property.getProperty(), property.getType(), property.getDatabase(), property.getValue1(), property.getValue2(), property.getValue3(), property.getLength(), property.getRowLimit(), property.getNature());
            testCasePropHash.put(key, property);
        }

        List<TestCaseCountryProperties> properties = new ArrayList<>();
        for (Map.Entry<String, TestCaseCountryProperties> entry : testCasePropHash.entrySet()) {
            // For each distinct found, we get the list of countries that have this definition.
            // The list of string is then converted to List of invariant that is added to the property that will be added to final result.
            String key = entry.getKey();
            TestCaseCountryProperties val = entry.getValue();
            List<String> countries = new ArrayList<>();
            for (TestCaseCountryProperties property : allProperties) {
                if (key.equals(getPropertyDistinctKey(property.getProperty(), property.getType(), property.getDatabase(), property.getValue1(), property.getValue2(), property.getValue3(), property.getLength(), property.getRowLimit(), property.getNature()))) {
                    countries.add(property.getCountry());
                }
            }
            val.setInvariantCountries(invariantService.convertCountryPropertiesToCountryInvariants(countries, countryInvariants));
            properties.add(val);
        }

        return properties;
    }

    private String getPropertyDistinctKey(String property, String type, String database, String value1, String value2, String value3, String length, int rowLimit, String nature) {
        return property + SEPARATOR
                + type + SEPARATOR
                + database + SEPARATOR
                + value1 + SEPARATOR
                + value2 + SEPARATOR
                + value3 + SEPARATOR
                + length + SEPARATOR
                + String.valueOf(rowLimit) + SEPARATOR
                + nature + SEPARATOR;
    }

    @Override
    public List<TestCaseCountryProperties> findDistinctInheritedPropertiesOfTestCase(TestCase testCase, HashMap<String, Invariant> countryInvariants) throws CerberusException {
        List<TestCaseCountryProperties> inheritedProperties = new ArrayList<>();
        List<TestCase> testCaseList = new ArrayList<>();
        HashMap<String, TestCase> testCaseHash = new HashMap<>();
        // Before getting the list of properties, we first dedup the list of testcase with useStep as many steps from TestCase, could point to the same testCase library.
        for (TestCaseStep step : testCase.getSteps()) {
            if (step.isUsingLibraryStep()) {
                LOG.debug("Item to add " + step.getLibraryStepTest() + "#/" + step.getLibraryStepTestcase());
                testCaseHash.put(step.getLibraryStepTest() + "#/" + step.getLibraryStepTestcase(), factoryTestCase.create(step.getLibraryStepTest(), step.getLibraryStepTestcase()));
            }
        }
        LOG.debug("Size " + testCaseHash.size());
        for (Map.Entry<String, TestCase> entry : testCaseHash.entrySet()) {
            TestCase val = entry.getValue();
            testCaseList.add(val);
        }

        inheritedProperties.addAll(findDistinctPropertiesOfTestCaseFromTestcaseList(testCaseList, countryInvariants));

        return inheritedProperties;
    }

    @Override
    public List<String> findCountryByProperty(TestCaseCountryProperties testCaseCountryProperties) {
        return testCaseCountryPropertiesDAO.findCountryByProperty(testCaseCountryProperties);
    }

    @Override
    public TestCaseCountryProperties findTestCaseCountryPropertiesByKey(String test, String testCase, String country, String property) throws CerberusException {
        return testCaseCountryPropertiesDAO.findTestCaseCountryPropertiesByKey(test, testCase, country, property);
    }

    @Override
    public void insertTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException {
        testCaseCountryPropertiesDAO.insertTestCaseCountryProperties(testCaseCountryProperties);
    }

    @Override
    public boolean insertListTestCaseCountryProperties(List<TestCaseCountryProperties> testCaseCountryPropertiesList) {
        for (TestCaseCountryProperties tccp : testCaseCountryPropertiesList) {
            try {
                insertTestCaseCountryProperties(tccp);
            } catch (CerberusException ex) {
                LOG.warn(ex.toString());
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException {
        testCaseCountryPropertiesDAO.updateTestCaseCountryProperties(testCaseCountryProperties);
    }

    @Override
    public void updateApplicationObject(String application, String oldObject, String newObject) {
        try {
            testCaseCountryPropertiesDAO.updateApplicationObject("Value1", application, oldObject, newObject);
            testCaseCountryPropertiesDAO.updateApplicationObject("Value2", application, oldObject, newObject);

            testCaseCountryPropertiesDAO.updateApplicationObject("Description", application, oldObject, newObject);
        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
        }
    }

    @Override
    public List<String> findCountryByPropertyNameAndTestCase(String test, String testcase, String property) {
        return testCaseCountryPropertiesDAO.findCountryByPropertyNameAndTestCase(test, testcase, property);
    }

    @Override
    public void deleteListTestCaseCountryProperties(List<TestCaseCountryProperties> tccpToDelete) throws CerberusException {
        for (TestCaseCountryProperties tccp : tccpToDelete) {
            deleteTestCaseCountryProperties(tccp);
        }
    }

    @Override
    public void deleteTestCaseCountryProperties(TestCaseCountryProperties tccp) throws CerberusException {
        testCaseCountryPropertiesDAO.deleteTestCaseCountryProperties(tccp);
    }

    @Override
    public List<TestCaseCountryProperties> findAllWithDependencies(String test, String testcase, String country, String system, String build, String Revision) throws CerberusException {

        // Heritage is done at property level.
        List<TestCaseCountryProperties> tccpList = new ArrayList<>();
        List<TestCase> tcList = new ArrayList<>();
        TestCase mainTC = testCaseService.findTestCaseByKey(test, testcase);

        /**
         * We load here all the properties countries from all related testcases
         * linked with test/testcase The order the load is done is important as
         * it will define the priority of each property. properties coming from
         * Pre Testing is the lower prio then, the property coming from the
         * useStep and then, top priority is the property on the test +
         * testcase.
         */
        //find all properties of preTests
        LOG.debug("Getting properties definition from PRE-TESTING.");
        tcList.addAll(testCaseService.getTestCaseForPrePostTesting(Test.TEST_PRETESTING, mainTC.getApplication(), country, system, build, Revision));
        //find all properties of postTests
        LOG.debug("Getting properties definition from POST-TESTING.");
        tcList.addAll(testCaseService.getTestCaseForPrePostTesting(Test.TEST_POSTTESTING, mainTC.getApplication(), country, system, build, Revision));
        // find all properties of the used step
        LOG.debug("Getting properties definition from Used Step.");
        tcList.addAll(testCaseService.findUseTestCaseList(test, testcase));
        // add this TC
        tcList.add(mainTC);

        if (parameterService.getParameterBooleanByKey("cerberus_property_countrylevelheritage", "", false)) {
            List<TestCaseCountryProperties> tccpListPerCountry = new ArrayList<>();

            for (TestCase tcase : tcList) {
                tccpList.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(tcase.getTest(), tcase.getTestcase()));
                tccpListPerCountry.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCaseCountry(tcase.getTest(), tcase.getTestcase(), country));
            }

            //Keep only one property by name
            //all properties that are defined for the country are included
            HashMap<String, TestCaseCountryProperties> tccpMap = new HashMap<>();
            for (TestCaseCountryProperties tccp : tccpListPerCountry) {
                tccpMap.put(tccp.getProperty(), tccp);
            }
            //These if/else instructions are done because of the way how the propertyService verifies if
            //the properties exist for the country.
            for (TestCaseCountryProperties tccp : tccpList) {
                TestCaseCountryProperties p = tccpMap.get(tccp.getProperty());
                if (p == null) {
                    tccpMap.put(tccp.getProperty(), tccp);
                } else if (p.getCountry().compareTo(country) != 0 && tccp.getCountry().compareTo(country) == 0) {
                    tccpMap.put(tccp.getProperty(), tccp);
                }
            }

            List<TestCaseCountryProperties> result = new ArrayList<>(tccpMap.values());
            return result;

        } else {

            // find all properties of those TC
            for (TestCase tcase : tcList) {
                tccpList.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(tcase.getTest(), tcase.getTestcase()));
            }

            /**
             * We loop here the previous list, keeping by property, the last
             * value (top priority). That will define the level to consider
             * property on the test. testcase.
             */
            HashMap<String, TestCaseCountryProperties> tccpMap1 = new HashMap<>();
            for (TestCaseCountryProperties tccp : tccpList) {
                tccpMap1.put(tccp.getProperty(), tccp);
            }

            /**
             * We then loop again in order to keep the selected properties for
             * the given country and level found on the previous step by
             * property.
             */
            List<TestCaseCountryProperties> result = new ArrayList<>();
            for (TestCaseCountryProperties tccp : tccpList) {
                if (tccp.getCountry().equals(country)) {
                    TestCaseCountryProperties tccp_level = tccpMap1.get(tccp.getProperty());
                    if ((tccp_level != null)
                            && (((tccp.getTest().equals("Pre Testing")) && (tccp_level.getTest().equals("Pre Testing")))
                            || ((tccp.getTest().equals(test)) && (tccp.getTestcase().equals(testcase)) && (tccp_level.getTest().equals(test)) && (tccp_level.getTestcase().equals(testcase)))
                            || ((tccp.getTest().equals(tccp_level.getTest())) && (tccp.getTestcase().equals(tccp_level.getTestcase()))))) {
                        result.add(tccp);
                    }
                }
            }

            return result;
        }
    }

    @Override
    public AnswerList<TestListDTO> findTestCaseCountryPropertiesByValue1(int testDataLibID, String name, String country, String propertyType) {
        return testCaseCountryPropertiesDAO.findTestCaseCountryPropertiesByValue1(testDataLibID, name, country, propertyType);
    }

    @Override
    public Answer createListTestCaseCountryPropertiesBatch(List<TestCaseCountryProperties> objectList) {

        dbmanager.beginTransaction();
        Answer answer = testCaseCountryPropertiesDAO.createTestCaseCountryPropertiesBatch(objectList);

        if (!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            dbmanager.abortTransaction();
        } else {
            dbmanager.commitTransaction();
        }
        return answer;
    }

    @Override
    public Answer create(TestCaseCountryProperties object) {
        return testCaseCountryPropertiesDAO.create(object);
    }

    @Override
    public Answer delete(TestCaseCountryProperties object) {
        return testCaseCountryPropertiesDAO.delete(object);
    }

    @Override
    public Answer update(TestCaseCountryProperties object) {
        return testCaseCountryPropertiesDAO.update(object);
    }

    @Override
    public Answer createList(List<TestCaseCountryProperties> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseCountryProperties objectToCreate : objectList) {
            ans = testCaseCountryPropertiesDAO.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<TestCaseCountryProperties> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseCountryProperties objectToDelete : objectList) {
            ans = testCaseCountryPropertiesDAO.delete(objectToDelete);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseCountryProperties> newList) throws CerberusException {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<TestCaseCountryProperties> oldList = new ArrayList<>();
        oldList = this.findListOfPropertyPerTestTestCase(test, testCase);

        /**
         * Iterate on (Object From Page - Object From Database) If Object in
         * Database has same key : Update and remove from the list. If Object in
         * database does not exist : Insert it.
         */
        List<TestCaseCountryProperties> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<TestCaseCountryProperties> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        for (TestCaseCountryProperties objectDifference : listToUpdateOrInsertToIterate) {
            for (TestCaseCountryProperties objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Iterate on (Object From Database - Object From Page). If Object in
         * Page has same key : remove from the list. Then delete the list of
         * Object
         */
        List<TestCaseCountryProperties> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<TestCaseCountryProperties> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (TestCaseCountryProperties objectDifference : listToDeleteToIterate) {
            for (TestCaseCountryProperties objectInPage : newList) {
                if (objectDifference.hasSameKey(objectInPage)) {
                    listToDelete.remove(objectDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }
        return finalAnswer;
    }

    @Override
    public Answer duplicateList(List<TestCaseCountryProperties> objectList, String targetTest, String targetTestCase) {
        Answer ans;
        List<TestCaseCountryProperties> listToCreate = new ArrayList<>();
        for (TestCaseCountryProperties objectToDuplicate : objectList) {
            objectToDuplicate.setTest(targetTest);
            objectToDuplicate.setTestcase(targetTestCase);
            listToCreate.add(objectToDuplicate);
        }
        ans = createList(listToCreate);
        return ans;
    }

    public List<TestCaseCountryProperties> getFlatListOfTestCaseCountryPropertiesFromAggregate(List<TestCaseCountryProperties> testCaseCountryProperties) {
        /*
        1 prop 3 country
        1 prop 6 country
         */
        return testCaseCountryProperties
                .stream()
                .flatMap(prop -> prop.getInvariantCountries()
                        .stream()
                        .map(invariant -> TestCaseCountryProperties.builder()
                                .test(prop.getTest())
                                .testcase(prop.getTestcase())
                                .country(invariant.getValue())
                                .property(prop.getProperty())
                                .description(prop.getDescription())
                                .type(prop.getType())
                                .database(prop.getDatabase())
                                .value1(prop.getValue1())
                                .value2(prop.getValue2())
                                .value3(prop.getValue3())
                                .length(prop.getLength())
                                .rowLimit(prop.getRowLimit())
                                .nature(prop.getNature())
                                .cacheExpire(prop.getCacheExpire())
                                .retryNb(prop.getRetryNb())
                                .retryPeriod(prop.getRetryPeriod())
                                .rank(prop.getRank())
                                .usrModif(prop.getUsrModif())
                                .build()
                        )
                ).collect(Collectors.toList());
    }

}
