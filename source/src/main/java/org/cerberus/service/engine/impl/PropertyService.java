/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.service.engine.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.Identifier;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.Property;
import org.cerberus.crud.entity.SoapLibrary;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.entity.TestCaseSubDataAccessProperty;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionData;
import org.cerberus.crud.service.ISoapLibraryService;
import org.cerberus.crud.service.ISqlLibraryService;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.crud.service.ITestDataService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.PropertyTypeEnum;
import org.cerberus.enums.SystemPropertyEnum;
import org.cerberus.enums.TestDataLibTypeEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.engine.IIdentifierService;
import org.cerberus.service.engine.IJsonService;
import org.cerberus.service.engine.IPropertyService;
import org.cerberus.service.engine.ISQLService;
import org.cerberus.service.engine.ISoapService;
import org.cerberus.service.engine.IWebDriverService;
import org.cerberus.service.engine.IXmlUnitService;
import org.cerberus.service.engine.testdata.TestDataLibResult;
import org.cerberus.service.engine.testdata.TestDataLibResultSOAP;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.MessageEventUtil;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @since 0.9.0
 */
@Service
public class PropertyService implements IPropertyService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PropertyService.class);

    @Autowired
    private IWebDriverService webdriverService;
    @Autowired
    private ISqlLibraryService sqlLibraryService;
    @Autowired
    private ISoapLibraryService soapLibraryService;
    @Autowired
    private ITestDataService testDataService;
    @Autowired
    private ISoapService soapService;
    @Autowired
    private ISQLService sQLService;
    @Autowired
    private IXmlUnitService xmlUnitService;
    @Autowired
    private ITestDataLibService testDataLibService;
    @Autowired
    private ITestDataLibDataService testDataLibDataService;
    @Autowired
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;
    @Autowired
    private IFactoryTestCaseCountryProperties factoryTCCountryProperties;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private IJsonService jsonService;
    @Autowired
    private IIdentifierService identifierService;

    private static final Pattern GETFROMDATALIB_PATTERN = Pattern.compile("^[_A-Za-z0-9]+\\([_A-Za-z0-9]+\\)$");
    private static final String GETFROMDATALIB_SPLIT = "\\s+|\\(\\s*|\\)";

    private String replaceWithCalculatedProperty(String stringToReplace, TestCaseExecution tCExecution) {
        for (TestCaseExecutionData tced : tCExecution.getTestCaseExecutionDataList()) {
            stringToReplace = StringUtil.replaceAllProperties(stringToReplace, "%" + tced.getProperty() + "%", tced.getValue());
        }
        return stringToReplace;
    }

    private void calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseStepActionExecution testCaseStepActionExecution,
            TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        testCaseExecutionData.setStart(new Date().getTime());
        MessageEvent res;

        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();

        /**
         * Decode Property replacing properties encapsulated with %
         */
        if (testCaseCountryProperty.getValue1().contains("%")) {
            String decodedValue = decodeValue(testCaseCountryProperty.getValue1(), tCExecution);
            decodedValue = this.replaceWithCalculatedProperty(decodedValue, tCExecution);
            testCaseExecutionData.setValue1(decodedValue);
        }

        if (testCaseCountryProperty.getValue2() != null && testCaseCountryProperty.getValue2().contains("%")) {
            String decodedValue = decodeValue(testCaseCountryProperty.getValue2(), tCExecution);
            decodedValue = this.replaceWithCalculatedProperty(decodedValue, tCExecution);
            testCaseExecutionData.setValue2(decodedValue);
        }

        /**
         * Calculate Property regarding the type
         */
        if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.EXECUTE_SQL_FROM_LIB.getPropertyName())) {
            testCaseExecutionData = this.executeSqlFromLib(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.EXECUTE_SQL.getPropertyName())) {
            testCaseExecutionData = this.executeSql(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.TEXT.getPropertyName())) {
            testCaseExecutionData = this.calculateText(testCaseExecutionData, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_HTML_VISIBLE.getPropertyName())) {
            testCaseExecutionData = this.getFromHtmlVIsible(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_HTML.getPropertyName())) {
            testCaseExecutionData = this.getFromHTML(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_JS.getPropertyName())) {
            testCaseExecutionData = this.getFromJS(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_TEST_DATA.getPropertyName())) {
            testCaseExecutionData = this.getFromTestData(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_ATTRIBUTE_FROM_HTML.getPropertyName())) {
            testCaseExecutionData = this.getAttributeFromHtml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_COOKIE.getPropertyName())) {
            testCaseExecutionData = this.getFromCookie(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_XML.getPropertyName())) {
            testCaseExecutionData = this.getFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_JSON.getPropertyName())) {
            testCaseExecutionData = this.getFromJson(testCaseExecutionData, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.EXECUTE_SOAP_FROM_LIB.getPropertyName())) {
            testCaseExecutionData = this.executeSoapFromLib(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_DATALIB.getPropertyName())) {
            testCaseExecutionData = this.getFromDataLib(testCaseExecutionData, tCExecution, testCaseStepActionExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_DIFFERENCES_FROM_XML.getPropertyName())) {
            testCaseExecutionData = this.getDifferencesFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.ACCESS_SUBDATA.getPropertyName())) {
            testCaseExecutionData = accessSubData(testCaseCountryProperty, tCExecution, testCaseStepActionExecution, forceRecalculation, testCaseExecutionData);
        } else {
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_UNKNOWNPROPERTY);
            res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseCountryProperty.getType()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }

        testCaseExecutionData.setEnd(new Date().getTime());
    }

    /**
     * Auxiliary method that calculates the access to a sub-data entry value.
     * E.g., Entry(Name).
     *
     * @param testCaseCountryProperty
     * @param tCExecution
     * @param testCaseStepActionExecution
     * @param forceRecalculation
     * @param testCaseExecutionData
     * @return
     * @throws NumberFormatException
     */
    private TestCaseExecutionData accessSubData(TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution, boolean forceRecalculation, TestCaseExecutionData testCaseExecutionData) throws NumberFormatException {

        MessageEvent res;

        TestCaseCountryProperties inner = ((TestCaseSubDataAccessProperty) testCaseCountryProperty).getPropertyLibEntry();

        //creates an auxiliary object that will store the value for computation
        TestCaseExecutionData tecdAuxiliary = factoryTestCaseExecutionData.create(tCExecution.getId(), inner.getProperty(), inner.getType(),
                inner.getValue1(), inner.getValue2(), new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB));

        //the testdatalibrary needs to be re-calculated
        if (testCaseCountryProperty.getValue1().isEmpty() || forceRecalculation) {//if empty, it means that the testdatalib was not retrieved yet
            tecdAuxiliary.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_PENDING));
            //needs to calculate the entry lib in order to retrieve the subdata
            //we force calculation because if the this line is reached, it means that the property does not exist.
            //or that exists but we are forcing it to be re-calculated
            tecdAuxiliary = getFromDataLib(tecdAuxiliary, tCExecution, testCaseStepActionExecution, inner, forceRecalculation);

            //adds the property to the data list
            tCExecution.getTestCaseExecutionDataList().add(tecdAuxiliary);

            try {
                testCaseExecutionDataService.insertOrUpdateTestCaseExecutionData(tecdAuxiliary);
            } catch (CerberusException cex) {
                LOG.error(cex.getMessage(), cex);
            }

            //the value for value 1 for the subdata access proprerty is the id of the library entry
            testCaseExecutionData.setValue1(tecdAuxiliary.getValue());
        }

        if (tecdAuxiliary.getPropertyResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB.getCode()) {
            //check if it should get the testdatalib subdata entry
            //if the property that is being calculated is the value from a subdata entry, then we need to calculate it e.g., format: Entry(Key)
            //if(eachTccp instanceof TestCaseSubDataAccessProperty){
            //after calculating the property base we can access the subdata entry
            calculateSubDataEntry(tCExecution, testCaseExecutionData, inner.getProperty());//calculates the subdata entry
        } else {
            //if the getFromDataLib does not succeed than it means that we are not able to perform the sub-data access 
            if (tecdAuxiliary.getPropertyResultMessage().getCode() == MessageEventEnum.TESTDATALIB_NOT_FOUND_ERROR.getCode()
                    || tecdAuxiliary.getPropertyResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GENERIC.getCode() //same code as PROPERTY_FAILED_GETFROMDATALIB_NODATA
                    || tecdAuxiliary.getPropertyResultMessage().getCode() == MessageEventEnum.ACTION_FAILED_CALLSOAP.getCode()) { //error related with the soap call 
                //redefinition of the error message
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SUBDATAACCESS);
                res.setDescription(res.getDescription().replaceAll("%SUBDATAACCCESS%", testCaseCountryProperty.getProperty()));
                res.setDescription(res.getDescription().replaceAll("%PROPERTY%", inner.getProperty()));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                //the result message is the same returned by the getFromDataLib operation
                testCaseExecutionData.setPropertyResultMessage(tecdAuxiliary.getPropertyResultMessage());
            }
        }

        return testCaseExecutionData;
    }

    @Override
    public String getValue(String stringToDecode, TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {
        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        TestCaseStepExecution tCSExecution = testCaseStepActionExecution.getTestCaseStepExecution();
        String test = testCaseStepActionExecution.getTest();
        String testCase = testCaseStepActionExecution.getTestCase();
        String usedTest = tCSExecution.getUseStepTest();
        String usedTestCase = tCSExecution.getUseStepTestCase();
        String country = tCExecution.getCountry();
        long now = new Date().getTime();

        /**
         * Find All properties of the testcase
         */
        List<TestCaseCountryProperties> tcProperties = tCExecution.getTestCaseCountryPropertyList();
        /**
         * Decode System Variable
         */
        if (LOG.isDebugEnabled()) {
            LOG.debug("Value before system variable decode : " + stringToDecode);
        }
        stringToDecode = decodeValue(stringToDecode, tCExecution);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Value after system variable decode : " + stringToDecode);
        }

        /**
         * Look at the internal properties contained in StringToDecode.
         */
        List<String> internalProperties = StringUtil.getAllProperties(stringToDecode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Internal Properties found inside property '" + stringToDecode + "' : " + internalProperties);
        }
        /**
         * If no property found, return stringToDecode
         */
        if (internalProperties.isEmpty()) {
            return stringToDecode;
        }
        /**
         * Get the list of properties needed to calculate the required property
         */
        List<TestCaseCountryProperties> linkedProperties = new ArrayList();
        for (String internalProperty : internalProperties) {
            linkedProperties.addAll(this.getListOfPropertiesLinkedToProperty(test, testCase, country, internalProperty, usedTest, usedTestCase, new ArrayList(), tcProperties));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + internalProperty + " need calculation of these properties " + linkedProperties);
            }
        }

        //list of getFromDataLib properties that failed
        List<TestCaseExecutionData> failedCalls = new ArrayList<TestCaseExecutionData>();
        /**
         * For all linked properties, calculate it if needed.
         */
        for (TestCaseCountryProperties eachTccp : linkedProperties) {
            TestCaseExecutionData tecd;
            /**
             * First create testCaseExecutionData object
             */
            now = new Date().getTime();
            tecd = factoryTestCaseExecutionData.create(tCExecution.getId(), eachTccp.getProperty(), null, eachTccp.getType(),
                    eachTccp.getValue1(), eachTccp.getValue2(), null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING));
            tecd.setTestCaseCountryProperties(eachTccp);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Calculating Property " + tecd);
            }

            List<TestCaseExecutionData> dataList = tCExecution.getTestCaseExecutionDataList();

            if (eachTccp instanceof TestCaseSubDataAccessProperty) {
                tecd = getSubDataExecutionDataFromList(dataList, eachTccp, tecd, forceCalculation);
            } else {
                /*  First check if property has already been calculated 
                 *  if action is calculateProperty, then set isKnownData to false. 
                 */
                tecd = getExecutionDataFromList(dataList, eachTccp, forceCalculation, tecd);
            }

            /**
             * If testcasecountryproperty not defined, set ExecutionData with
             * the same resultMessage
             */
            if (eachTccp.getResult() != null) {
                tecd.setPropertyResultMessage(eachTccp.getResult());
            }
            /*
             * If not already calculated, or calculateProperty, then calculate it and insert or update it.
             */
            if (tecd.getPropertyResultMessage().getCode() == MessageEventEnum.PROPERTY_PENDING.getCode()) {
                //if the internal property that we want to calculate has a getFromDataLib that lauched an error
                MessageEvent mes = containsFailedInnerProperties(internalProperties, tecd, failedCalls);
                //if the property does not contain failed inner properties than it can be calculated
                if (mes.getCode() == MessageEventEnum.PROPERTY_SUCCESS.getCode()) {
                    calculateProperty(tecd, testCaseStepActionExecution, eachTccp, forceCalculation);
                } else {
                    //this is an internal property, and an error was found while calculating a getFromDataLib entry                    
                    tecd.setPropertyResultMessage(mes);
                }
                //saves the result 
                try {
                    testCaseExecutionDataService.insertOrUpdateTestCaseExecutionData(tecd);
                } catch (CerberusException cex) {
                    LOG.error(cex.getMessage(), cex);
                }
            }

            //if is not a system property, then check if it was calculated with success
            //system properties are decoded before these instructions or (when using calculateProperty)
            //in the calculateProperty action, therefore these are not properties that would stop/fail the execution
            if (!SystemPropertyEnum.contains(tecd.getProperty())) {
                //if the property result message indicates that we need to stop the test action, then the action is notified               
                //or if the property was not successfully calculated, either because it was not defined for the country or because it does not exist
                //then we notify the execution
                if (tecd.getPropertyResultMessage().isStopTest()
                        || tecd.getPropertyResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION.getCode()) {
                    testCaseStepActionExecution.setStopExecution(tecd.isStopExecution());
                    testCaseStepActionExecution.setActionResultMessage(tecd.getPropertyResultMessage());
                    testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(tecd.getPropertyResultMessage().getMessage()));
                }
            }
            /**
             * Add TestCaseExecutionData in TestCaseExecutionData List of the
             * TestCaseExecution
             */
            tCExecution.getTestCaseExecutionDataList().add(tecd);
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, "Update data list: " + eachTccp.getProperty() + ":" + eachTccp.getValue1() + ":" + tecd.getValue());

            /**
             * After calculation, replace properties by value calculated
             */
            stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%" + eachTccp.getProperty() + "%", tecd.getValue());
            //if a getFromDataLib fails or a property that tries to make a subdata access fails
            //then all properties that use it (as inner properties) should fail and its calculation should not proceed
            if (tecd.getPropertyResultMessage().getCode() == MessageEventEnum.TESTDATALIB_NOT_FOUND_ERROR.getCode() //lib was not found
                    || tecd.getPropertyResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_SUBDATAACCESS.getCode() //a problem occurred while accesing a sub-data entry
                    || tecd.getPropertyResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIBDATA_INVALID_COLUMN.getCode() //a problem occurred while accesing a sub-data entry
                    || tecd.getPropertyResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GENERIC.getCode()//the same code as PROPERTY_FAILED_GETFROMDATALIB_NODATA
                    || tecd.getPropertyResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIBDATA_XMLEXCEPTION.getCode()) { //the same code as PROPERTY_FAILED_GETFROMDATALIBDATA_XML_NOTFOUND and PROPERTY_FAILED_GETFROMDATALIBDATA_CHECK_XPATH
                //if is to stop the calculating process 
                failedCalls.add(tecd);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + eachTccp.getProperty() + " calculated with Value = " + tecd.getValue() + ", Value1 = " + tecd.getValue1() + ", Value2 = " + tecd.getValue2());
            }
        }
        return stringToDecode;
    }

    /**
     * Auxiliary method that verifies if a property, which belongs to the list
     * of internal properties, uses a property that failed to be calculated.
     *
     * @param internalProperties list of internal properties
     * @param tecd property that is being evaluated
     * @param failedCalls list of properties that were not calculated with
     * success, only the ones related to getFromDataLib calls are being
     * considered.
     * @return message indicating success or error
     */
    private MessageEvent containsFailedInnerProperties(List<String> internalProperties, TestCaseExecutionData tecd, List<TestCaseExecutionData> failedCalls) {

        MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        StringBuilder errorMessage = new StringBuilder();
        if (internalProperties.contains(tecd.getProperty()) && !failedCalls.isEmpty()) {
            //check if the internal property contains some property that failed
            for (TestCaseExecutionData failedData : failedCalls) {
                if (tecd.getValue1().contains("%" + failedData.getProperty() + "%")
                        || tecd.getValue2().contains("%" + failedData.getProperty() + "%")) {
                    //this is an internal property
                    errorMessage.append("[").append(failedData.getProperty()).append("]");
                }
            }
        }
        if (errorMessage.length() > 0) {
            mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_INNERPROPERTY_GETFROMDATALIB_NOTFOUND);
            mes.setDescription(mes.getDescription().replace("%PROPERTY%", tecd.getProperty()).replace("%ITEM%", errorMessage.toString()));
        }
        return mes;
    }

    /**
     * Auxiliary method that returns the execution data for a property.
     *
     * @param dataList list of execution data
     * @param eachTccp property to be calculated
     * @param forceCalculation indicates whether a property must be
     * re-calculated if it was already computed in previous steps
     * @param tecd execution data for the property
     * @return the updated execution data for the property
     */
    private TestCaseExecutionData getExecutionDataFromList(List<TestCaseExecutionData> dataList, TestCaseCountryProperties eachTccp, boolean forceCalculation,
            TestCaseExecutionData tecd) {
        for (int iterator = 0; iterator < dataList.size(); iterator++) {
            if (dataList.get(iterator).getProperty().equalsIgnoreCase(eachTccp.getProperty())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Property " + eachTccp + " has already been calculated");
                }
                if (!forceCalculation) {
                    //If Calculation not forced , set tecd to the previous property already calculated.
                    tecd = dataList.get(iterator);
                }
                dataList.remove(iterator);
                break;
            }
        }
        return tecd;
    }

    /**
     * Auxiliary method that checks if the property holds the library for the
     * sub-data access was already calculated.
     *
     * @param dataList list of execution data
     * @param eachTccp property to be calculated
     * @param tecd execution data for the property
     * @param forceRecalculation indicates whether a property must be
     * re-calculated if it was already computed in previous steps
     * @return the updated execution data for the property
     */
    private TestCaseExecutionData getSubDataExecutionDataFromList(List<TestCaseExecutionData> dataList, TestCaseCountryProperties eachTccp, TestCaseExecutionData tecd, boolean forceRecalculation) {

        TestCaseSubDataAccessProperty eachTccpSubData = (TestCaseSubDataAccessProperty) eachTccp;

        String libName = eachTccpSubData.getLibraryValue();

        //if we need to recalculate 
        for (TestCaseExecutionData item : dataList) {
            if (item.getProperty().equalsIgnoreCase(tecd.getProperty())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Property " + eachTccp + " was already calculated");
                }
                if (forceRecalculation) {
                    dataList.remove(item);
                    break;
                } else {
                    //value exists in the data list
                    eachTccp.setResult(item.getPropertyResultMessage());
                    tecd.setValue1(item.getValue1());
                    tecd.setValue(item.getValue());
                    return tecd;
                }

            }
        }

        for (TestCaseExecutionData item : dataList) {
            if (item.getProperty().equalsIgnoreCase(libName)) {
                //id da library                 
                tecd.setValue1(item.getValue1()); //value1 is the value determined by the previous calculation (if it happened) 
                eachTccp.setValue1(item.getProperty()); //property name used to retrieve the data from the execution

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Property " + eachTccp + " was already calculated");
                }
                if (forceRecalculation) {
                    dataList.remove(item);
                }
                break;
            }
        }

        return tecd;
    }

    /**
     * Calculates the sub-data entry value for a test data library.
     *
     * @param tCExecution execution environment
     * @param tecd data execution for the sub-data access property
     * @param keyDataList key used to retrieve the information from the library
     * that was previously calculated.
     */
    private void calculateSubDataEntry(TestCaseExecution tCExecution, TestCaseExecutionData tecd, String keyDataList) {
        //we are going to retrieve the subdata entry
        //means that is library + subdata call -> LIBRARY(ATTRIBUTE) and that we have already the subdata property
        //gets the value for the library entry with basis on
        //the subdataentry specified
        HashMap<String, TestDataLibResult> currentListResults = tCExecution.getDataLibraryExecutionDataList();

        TestDataLibResult result = currentListResults.get(keyDataList);
        String subDataValue = result.getValue(tecd.getValue2()); //temporary use of the value2

        String value = "";
        MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIBDATA);
        if (subDataValue == null) {
            //the entry does not exist yet so we need to calculate it
            //gets the entry that we want to collect 
            TestDataLibData subDataEntry;
            
            AnswerItem ansSubDataEntry = testDataLibDataService.readByKey(Integer.parseInt(tecd.getValue1()), tecd.getValue2());
            subDataEntry = (TestDataLibData) ansSubDataEntry.getItem();
            if (subDataEntry != null) {
                //extract the subdata entry value
                AnswerItem ansFetchData = testDataLibDataService.fetchSubData(result, subDataEntry);
                value = (String) ansFetchData.getItem();
                if (value == null) {
                    //if value is null then the key was valid but 
                    res = ansFetchData.getResultMessage();
                }
            } else {
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIBDATA);
            }
        }
        res.setDescription(res.getDescription().replace("%VALUE1%", tecd.getValue1()).replace("%VALUE2%", tecd.getValue2()));
        //retrieved the information
        //we add the entry value into the execution data
        tecd.setValue(value);

        //updates the result data
        tCExecution.getDataLibraryExecutionDataList().put(keyDataList, result);
        //updates the execution data list
        //tCExecution.setDataLibraryExecutionDataList(currentListResults);

        tecd.setPropertyResultMessage(res);
    }

    @Override
    public List<TestCaseCountryProperties> getListOfPropertiesLinkedToProperty(String test, String testCase, String country, String property, String usedTest, String usedTestCase, List<String> crossedProperties, List<TestCaseCountryProperties> propertieOfTestcase) {
        List<TestCaseCountryProperties> result = new ArrayList();
        TestCaseCountryProperties testCaseCountryProperty = null;
        /*
         * Check if property is not already known (recursive case).
         */
        if (crossedProperties.contains(property)) {
            return result;
        }
        crossedProperties.add(property);

        /*
         * Check if property is defined for this testcase
         */
        AnswerItem ansSearch = findMatchingTestCaseCountryProperty(property, country, propertieOfTestcase);
        testCaseCountryProperty = (TestCaseCountryProperties) ansSearch.getItem();

        if (testCaseCountryProperty == null) {
            //if the property does not exists, then a dummy property with the error message is defined and returned to the TC's execution
            MessageEvent msg = ansSearch.getResultMessage();
            TestCaseCountryProperties tccpToReturn = new TestCaseCountryProperties();
            tccpToReturn.setProperty(property);
            tccpToReturn.setResult(msg);
            result.add(tccpToReturn);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + property + " not defined : " + msg.getDescription());
            }
            return result;
        }

        /* 
         * Check if property value1 and value2 contains internal properties
         */
        List<String> allProperties = new ArrayList();

        if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.EXECUTE_SQL.getPropertyName())
                || testCaseCountryProperty.getType().equals(PropertyTypeEnum.EXECUTE_SQL_FROM_LIB.getPropertyName())) {

            List<String> propertiesSql = new ArrayList();
            //check the properties specified in the test
            for (String propSqlName : StringUtil.getAllProperties(testCaseCountryProperty.getValue1())) {
                for (TestCaseCountryProperties pr : propertieOfTestcase) {
                    if (pr.getProperty().equals(propSqlName)) {
                        propertiesSql.add(propSqlName);
                        break;
                    }
                }
            }
            allProperties.addAll(propertiesSql);
        } else {
            allProperties.addAll(StringUtil.getAllProperties(testCaseCountryProperty.getValue1()));
            allProperties.addAll(StringUtil.getAllProperties(testCaseCountryProperty.getValue2()));
        }

        for (String internalProperty : allProperties) {
            result.addAll(getListOfPropertiesLinkedToProperty(test, testCase, country, internalProperty, usedTest, usedTestCase, crossedProperties, propertieOfTestcase));
        }
        result.add(testCaseCountryProperty);

        return result;
    }

    private String decodeValue(String stringToDecode, TestCaseExecution tCExecution) {
        /**
         * Trying to replace by system environment variables .
         */
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_SYSTEM%", tCExecution.getApplication().getSystem());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_APPLI%", tCExecution.getApplication().getApplication());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_APP_DOMAIN%", tCExecution.getCountryEnvironmentParameters().getDomain());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_APP_HOST%", tCExecution.getCountryEnvironmentParameters().getIp());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_ENV%", tCExecution.getEnvironmentData());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_ENVGP%", tCExecution.getEnvironmentDataObj().getGp1());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_COUNTRY%", tCExecution.getCountry());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_COUNTRYGP1%", tCExecution.getCountryObj().getGp1());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_SSIP%", tCExecution.getSeleniumIP());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_SSPORT%", tCExecution.getSeleniumPort());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_TAG%", tCExecution.getTag());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_EXECUTIONID%", String.valueOf(tCExecution.getId()));

        /**
         * Trying to replace date variables .
         */
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_TODAY-yyyy%", DateUtil.getTodayFormat("yyyy"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_TODAY-MM%", DateUtil.getTodayFormat("MM"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_TODAY-dd%", DateUtil.getTodayFormat("dd"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_TODAY-doy%", DateUtil.getTodayFormat("D"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_TODAY-HH%", DateUtil.getTodayFormat("HH"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_TODAY-mm%", DateUtil.getTodayFormat("mm"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_TODAY-ss%", DateUtil.getTodayFormat("ss"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_YESTERDAY-yyyy%", DateUtil.getYesterdayFormat("yyyy"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_YESTERDAY-MM%", DateUtil.getYesterdayFormat("MM"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_YESTERDAY-dd%", DateUtil.getYesterdayFormat("dd"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_YESTERDAY-doy%", DateUtil.getYesterdayFormat("D"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_YESTERDAY-HH%", DateUtil.getYesterdayFormat("HH"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_YESTERDAY-mm%", DateUtil.getYesterdayFormat("mm"));
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_YESTERDAY-ss%", DateUtil.getYesterdayFormat("ss"));

        /**
         * Trying to replace timing variables .
         */
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_ELAPSED-EXESTART%", "To Be Implemented");
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_ELAPSED-STEPSTART%", "To Be Implemented");

        return stringToDecode;
    }

    private TestCaseExecutionData executeSqlFromLib(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution, boolean forceCalculation) {
        try {
            String script = this.sqlLibraryService.findSqlLibraryByKey(testCaseExecutionData.getValue1()).getScript();
            testCaseExecutionData.setValue1(script); //TODO use the new library 

        } catch (CerberusException ex) {
            Logger.getLogger(PropertyService.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_SQLLIB_NOTEXIT);

            res.setDescription(res.getDescription().replaceAll("%SQLLIB%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);

            testCaseExecutionData.setEnd(
                    new Date().getTime());
            return testCaseExecutionData;
        }
        testCaseExecutionData = this.executeSql(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceCalculation);
        return testCaseExecutionData;
    }

    private TestCaseExecutionData executeSql(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution, boolean forceCalculation) {
        return sQLService.calculateOnDatabase(testCaseExecutionData, testCaseCountryProperty, tCExecution);
    }

    private TestCaseExecutionData calculateText(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        if (Property.NATURE_RANDOM.equals(testCaseCountryProperty.getNature())
                //TODO CTE Voir avec B. Civel "RANDOM_NEW"
                || (testCaseCountryProperty.getNature().equals(Property.NATURE_RANDOMNEW))) {
            if (testCaseCountryProperty.getLength() == 0) {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TEXTRANDOMLENGHT0);
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                String charset;
                if (testCaseExecutionData.getValue1() != null && !"".equals(testCaseExecutionData.getValue1().trim())) {
                    charset = testCaseExecutionData.getValue1();
                } else {
                    charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                }
                String value = StringUtil.getRandomString(testCaseCountryProperty.getLength(), charset);
                testCaseExecutionData.setValue(value);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_RANDOM);
                res.setDescription(res.getDescription().replaceAll("%FORCED%", forceRecalculation == true ? "Re-" : ""));
                res.setDescription(res.getDescription().replaceAll("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
                testCaseExecutionData.setPropertyResultMessage(res);
//                    if (testCaseCountryProperty.getNature().equals("RANDOM_NEW")) {
//                        //TODO check if value exist on DB ( used in another test case of the revision )
//                    }

            }
        } else {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, "Setting value : " + testCaseExecutionData.getValue1());
            String value = testCaseExecutionData.getValue1();

            testCaseExecutionData.setValue(value);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_TEXT);

            res.setDescription(res.getDescription().replaceAll("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromHTML(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(testCaseExecutionData.getValue1());
            String valueFromHTML = this.webdriverService.getValueFromHTML(tCExecution.getSession(), identifier);
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTML);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (NoSuchElementException exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ELEMENTDONOTEXIST);

            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromJS(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {

        String script = testCaseExecutionData.getValue1();
        String valueFromJS;
        String message = "";
        try {
            valueFromJS = this.webdriverService.getValueFromJS(tCExecution.getSession(), script);
        } catch (Exception e) {
            message = e.getMessage().split("\n")[0];
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, "Exception Running JS Script :" + message);
            valueFromJS = null;
        }
        if (valueFromJS != null) {
            testCaseExecutionData.setValue(valueFromJS);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTML);
            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%VALUE%", script));
            testCaseExecutionData.setPropertyResultMessage(res);
        } else {
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_JS_EXCEPTION);
            res.setDescription(res.getDescription().replaceAll("%EXCEPTION%", message));
            testCaseExecutionData.setPropertyResultMessage(res);
        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromTestData(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        String propertyValue = "";

        try {
            propertyValue = testCaseExecutionData.getValue1();
            String valueFromTestData = testDataService.findTestDataByKey(propertyValue, tCExecution.getApplication().getApplication(),
                    tCExecution.getEnvironment(), tCExecution.getCountry()).getValue();
            if (valueFromTestData != null) {
                testCaseExecutionData.setValue(valueFromTestData);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_TESTDATA);
                res.setDescription(res.getDescription().replaceAll("%PROPERTY%", propertyValue));
                res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromTestData));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (CerberusException exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, "Exception Getting value from TestData for data :'" + propertyValue + "'\n" + exception.getMessageError().getDescription());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);

            res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getAttributeFromHtml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        MessageEvent res;
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(testCaseExecutionData.getValue1());
            String valueFromHTML = this.webdriverService.getAttributeFromHtml(tCExecution.getSession(), identifier, testCaseExecutionData.getValue2());
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETATTRIBUTEFROMHTML);
                res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
            } else {
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ATTRIBUTEDONOTEXIST);
            }
            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%ATTRIBUTE%", testCaseExecutionData.getValue2()));

        } catch (NoSuchElementException exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, exception.toString());
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);

            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseExecutionData.getValue1()));
        }
        testCaseExecutionData.setPropertyResultMessage(res);
        return testCaseExecutionData;
    }

    private TestCaseExecutionData executeSoapFromLib(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            SoapLibrary soapLib = this.soapLibraryService.findSoapLibraryByKey(testCaseExecutionData.getValue1());
            if (soapLib != null) {
                String attachement = "";//TODO implement this feature
                //TODO implement the executeSoapFromLib
                /*if (!testCaseExecutionData.getValue2().isEmpty()){
                 attachement = testCaseExecutionData.getValue2();
                 }else{
                 attachement = soapLib.getAttachmentUrl();
                 }*/
                soapService.callSOAPAndStoreResponseInMemory(tCExecution.getExecutionUUID(), soapLib.getEnvelope(), soapLib.getServicePath(), soapLib.getMethod(), attachement, false);
                String result = xmlUnitService.getFromXml(tCExecution.getExecutionUUID(), null, soapLib.getParsingAnswer());
                if (result != null) {
                    testCaseExecutionData.setValue(result);
                    testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SOAP));
                }
            }
        } catch (CerberusException exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.ERROR, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);

            res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromHtmlVIsible(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(testCaseExecutionData.getValue1());
            String valueFromHTML = this.webdriverService.getValueFromHTMLVisible(tCExecution.getSession(), identifier);
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTMLVISIBLE);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (NoSuchElementException exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);

            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromXml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            String valueFromXml = xmlUnitService.getFromXml(tCExecution.getExecutionUUID(), testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (valueFromXml != null) {
                testCaseExecutionData.setValue(valueFromXml);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMXML);
                res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (Exception ex) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, ex.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMXML);

            res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromCookie(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            String valueFromCookie = this.webdriverService.getFromCookie(tCExecution.getSession(), testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (valueFromCookie != null) {
                if (!valueFromCookie.equals("cookieNotFound")) {
                    testCaseExecutionData.setValue(valueFromCookie);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMCOOKIE);
                    res.setDescription(res.getDescription().replaceAll("%COOKIE%", testCaseExecutionData.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseExecutionData.getValue2()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromCookie));
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_COOKIENOTFOUND);
                    res.setDescription(res.getDescription().replaceAll("%COOKIE%", testCaseExecutionData.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseExecutionData.getValue2()));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_PARAMETERNOTFOUND);
                res.setDescription(res.getDescription().replaceAll("%COOKIE%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (Exception exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_COOKIENOTFOUND);

            res.setDescription(res.getDescription().replaceAll("%COOKIE%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;

    }

    /**
     * Execution method for the <code>getDifferencesFromXml</code> property
     * service
     *
     * @param testCaseExecutionData
     * @param tCExecution
     * @param testCaseCountryProperty
     * @return the {@link TestCaseExecutionData} added by the
     * <code>getDifferencesFromXML</code> result
     */
    private TestCaseExecutionData getDifferencesFromXml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            MyLogger.log(PropertyService.class
                    .getName(), Level.INFO, "Computing differences between " + testCaseExecutionData.getValue1() + " and " + testCaseExecutionData.getValue2());
            String differences = xmlUnitService.getDifferencesFromXml(testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (differences
                    != null) {
                MyLogger.log(PropertyService.class.getName(), Level.INFO, "Computing done.");
                testCaseExecutionData.setValue(differences);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETDIFFERENCESFROMXML);
                res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                MyLogger.log(PropertyService.class.getName(), Level.INFO, "Computing failed.");
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETDIFFERENCESFROMXML);
                res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception ex) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.INFO, ex.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETDIFFERENCESFROMXML);

            res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromJson(TestCaseExecutionData testCaseExecutionData, boolean forceRecalculation) {
        try {
            String valueFromJson = this.jsonService.getFromJson(testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (valueFromJson != null) {
                if (!"".equals(valueFromJson)) {
                    testCaseExecutionData.setValue(valueFromJson);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMJSON);
                    res.setDescription(res.getDescription().replaceAll("%URL%", testCaseExecutionData.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseExecutionData.getValue2()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromJson));
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
                    res.setDescription(res.getDescription().replaceAll("%URL%", testCaseExecutionData.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseExecutionData.getValue2()));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
                res.setDescription(res.getDescription().replaceAll("%URL%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (Exception exception) {
            if (LOG.isDebugEnabled()) {
                LOG.error(exception.toString());
            }
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);

            res.setDescription(res.getDescription().replaceAll("%URL%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromDataLib(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {

        MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB);

        HashMap<String, TestDataLibResult> currentListResults = tCExecution.getDataLibraryExecutionDataList();
        if (currentListResults == null) {
            currentListResults = new HashMap<String, TestDataLibResult>();
        }
        TestDataLib lib;
        TestDataLibResult result;
        AnswerItem answer = testDataLibService.readByNameBySystemByEnvironmentByCountry(testCaseExecutionData.getValue1(),
                tCExecution.getCountryEnvironmentParameters().getSystem(), tCExecution.getCountryEnvironmentParameters().getEnvironment(),
                tCExecution.getCountryEnvironmentParameters().getCountry());
        
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            lib = (TestDataLib) answer.getItem();

            unescapeTestDataLibrary(lib);
            //if the property was defined for all systems, then the query returned an empty value
            if (StringUtil.isNullOrEmpty(lib.getSystem())) {
                lib.setSystem(tCExecution.getCountryEnvironmentParameters().getSystem());
            }
            //if the property was defined for all environments, then the query returned an empty value
            if (StringUtil.isNullOrEmpty(lib.getEnvironment())) {
                lib.setEnvironment(tCExecution.getCountryEnvironmentParameters().getEnvironment());
            }
            //if the property was defined for all countries, then the query returned an empty value
            if (StringUtil.isNullOrEmpty(lib.getCountry())) {
                lib.setCountry(tCExecution.getCountryEnvironmentParameters().getCountry());
            }

            AnswerItem serviceAnswer;
            //result = currentListResults.get(String.valueOf(lib.getTestDataLibID()));                 
            result = currentListResults.get(testCaseCountryProperty.getProperty());
            //if is force calculation, then the entry will be recalculated
            if (forceRecalculation || (!forceRecalculation && result == null)) {

                if (forceRecalculation && result != null) {
                    currentListResults.remove(testCaseCountryProperty.getProperty());//removes the result when we are recalculating
                }

                //check if there are properties defined in the data specification
                calculateInnerProperties(lib, testCaseStepActionExecution);
                //we need to recalculate the result for the lib
                serviceAnswer = testDataLibService.fetchData(lib, testCaseCountryProperty.getRowLimit(), testCaseCountryProperty.getNature());

                if (serviceAnswer.getResultMessage().getCode() != MessageEventEnum.PROPERTY_SUCCESS.getCode()) {
                    //if the fetch data fails then we will get the message and show it
                    res = serviceAnswer.getResultMessage();
                }
                else{
                    res = createGetFromDataLibSuccessMessage(lib);
                }
                result = (TestDataLibResult) serviceAnswer.getItem(); //test data library returned by the service
            }

            if (result != null) {
                    //retrieves the information
                //fetches the empty value as the default  one                     
                AnswerItem ansSubDataEntry = testDataLibDataService.readByKey(result.getTestDataLibID(), "");
                TestDataLibData subDataEntry = (TestDataLibData) ansSubDataEntry.getItem();

                if (subDataEntry != null) {
                    AnswerItem ansFetchData = testDataLibDataService.fetchSubData(result, subDataEntry);
                    String value = (String) ansFetchData.getItem();
                    if (value == null) {
                        //if value is null then something happene while retrieving the sub-data entry value
                        res = ansFetchData.getResultMessage();
                    } else {
                        testCaseExecutionData.setValue(value);
                        testCaseExecutionData.setValue1(String.valueOf(lib.getTestDataLibID().toString()));
                    }

                } else { //no empty value is defined
                    //we add the entry value into the execution data 
                    testCaseExecutionData.setValue(String.valueOf(lib.getTestDataLibID().toString()));
                    testCaseExecutionData.setValue1(String.valueOf(lib.getTestDataLibID().toString()));
                }
                //updates the result data
                currentListResults.put(testCaseCountryProperty.getProperty(), result);
                //updates the execution data list
                tCExecution.setDataLibraryExecutionDataList(currentListResults);
            }

        } else {//no data found was returnedd            
            //the library does not exist at all
            AnswerList nameExistsAnswer = testDataLibService.readNameListByName(testCaseExecutionData.getValue1(), 1);
            if (nameExistsAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                //if the library name exists but was not available or does not exist for the current specification but exists for other countries/environments/systems
                res = new MessageEvent(MessageEventEnum.TESTDATALIB_NOT_FOUND_ERROR);
                res.setDescription(res.getDescription().replace("%ITEM%", testCaseExecutionData.getValue1()).
                        replace("%COUNTRY%", tCExecution.getCountryEnvironmentParameters().getCountry()).
                        replace("%ENVIRONMENT%", tCExecution.getCountryEnvironmentParameters().getEnvironment()).
                        replace("%SYSTEM%", tCExecution.getCountryEnvironmentParameters().getSystem()));
            } else {
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB);
            }

        }
        res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseExecutionData.getValue1()));
        testCaseExecutionData.setPropertyResultMessage(res);

        return testCaseExecutionData;
    }
    /**
     * Auxiliary method that calculates the inner properties that are defined in
     * a test data library entry
     *
     * @param lib - test data library entry
     * @param testCaseStepActionExecution step action execution
     */
    private void calculateInnerProperties(TestDataLib lib, TestCaseStepActionExecution testCaseStepActionExecution) {
        try {
            if (lib.getType().equals(TestDataLibTypeEnum.SOAP.getCode())) {
                //check if the servicepath contains properties that neeed to be calculated
                String decodedServicePath = getValue(lib.getServicePath(), testCaseStepActionExecution, false);
                lib.setServicePath(decodedServicePath);
                //check if the method contains properties that neeed to be calculated
                String decodedMethod = getValue(lib.getMethod(), testCaseStepActionExecution, false);
                lib.setMethod(decodedMethod);
                //check if the envelope contains properties that neeed to be calculated
                String decodedEnvelope = getValue(lib.getEnvelope(), testCaseStepActionExecution, false);
                lib.setEnvelope(decodedEnvelope);

            } else if (lib.getType().equals(TestDataLibTypeEnum.SQL.getCode())) {
                //check if the script contains properties that neeed to be calculated
                String decodedScript = getValue(lib.getScript(), testCaseStepActionExecution, false);
                lib.setScript(decodedScript);

            }
        } catch (CerberusEventException cex) {
            Logger.getLogger(PropertyService.class.getName()).log(java.util.logging.Level.SEVERE, "calculateInnerProperties", cex);
        }
    }

    /**
     * Auxiliary method that verifies if a property is defined in the scope of
     * the test case.
     *
     * @param property - property name
     * @param country - country were the property was implemented
     * @param propertieOfTestcase - list of properties defined for the test case
     * @return an AnswerItem that contains the property in case of success, and
     * null otherwise. also it returns a message indicating error or success.
     */
    private AnswerItem<TestCaseCountryProperties> findMatchingTestCaseCountryProperty(String property, String country, List<TestCaseCountryProperties> propertieOfTestcase) {

        AnswerItem<TestCaseCountryProperties> item = new AnswerItem<TestCaseCountryProperties>();
        boolean propertyDefined = false;
        item.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS));

        TestCaseCountryProperties testCaseCountryProperty = null;
        //searches for properties that match the propertyname (even if they use the getFromDataLib syntax)
        for (TestCaseCountryProperties tccp : propertieOfTestcase) {
            if (tccp.getProperty().equals(property)) {
                //property is defined
                propertyDefined = true;
                //check if is defined for country
                if (tccp.getCountry().equals(country)) {
                    //if is a sub data access then we create a auxiliary property
                    testCaseCountryProperty = tccp;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Property found :" + tccp);
                    }
                    break;
                }
            }
        }

        //if the property was not found, with the explicit name, and if it uses the syntax getFromDataLib then we will check for 
        //entry names + subdata entry names that match the property name
        if (testCaseCountryProperty == null) {
            //check property format
            //uses get from datalib syntax    
            boolean getFromDataLibSyntax = GETFROMDATALIB_PATTERN.matcher(property).find();

            if (getFromDataLibSyntax) {
                String[] parts = property.split(GETFROMDATALIB_SPLIT);
                String libName = parts[0];
                String subdataName = parts[1];

                for (TestCaseCountryProperties tccp : propertieOfTestcase) {
                    //check if there is a property that matches the lib name, when the syntax used is Entry(Name)
                    if (tccp.getType().equals(PropertyTypeEnum.GET_FROM_DATALIB.getPropertyName()) && tccp.getProperty().equals(libName)) {
                        //property is defined
                        propertyDefined = true;
                        if (tccp.getCountry().equals(country)) {
                            //if is a sub data access then we create a auxiliary property
                            testCaseCountryProperty = factoryTCCountryProperties.create(tccp, property, libName, subdataName);
                            //property needs to be calculated
                            testCaseCountryProperty.setResult(new MessageEvent(MessageEventEnum.PROPERTY_PENDING));

                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Property found :" + tccp + " Test Data Library subdata access syntax");
                            }
                            break;
                        }
                    }
                }
            }
        }

        /**
         * If property defined on another Country, set a specific message. If
         * property is not defined at all, trigger the end of the testcase.
         */
        if (testCaseCountryProperty == null) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION);
            if (!propertyDefined) {
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_UNKNOWNPROPERTY);
            }
            msg.setDescription(msg.getDescription().replaceAll("%COUNTRY%", country));
            msg.setDescription(msg.getDescription().replaceAll("%PROP%", property));
            item.setResultMessage(msg);
            if (LOG.isDebugEnabled()) {
                LOG.debug(msg.getDescription());
            }
        }
        item.setItem(testCaseCountryProperty);
        return item;
    }
    
    @Override
    public AnswerItem callSoapProperty(TestCaseStepActionExecution testCaseStepActionExecution, String propertyName){
        AnswerItem answerItem = new AnswerItem();
        TestCaseExecution testCaseExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        MessageEvent message;
        //obtains the property that matches the current country
        String country = testCaseExecution.getCountry();
        TestCaseCountryProperties currentProperty = null;
        for(TestCaseCountryProperties prop :  testCaseExecution.getTestCaseCountryPropertyList()){
            if(prop.getCountry().equals(country) && prop.getProperty().equals(propertyName) && prop.getType().equals("getFromDataLib_BETA")){
                currentProperty = prop;
                break;
            }
        }
       
        if(currentProperty != null){
            //property exists
            String libName = currentProperty.getValue1();           
            String system = testCaseExecution.getApplication().getSystem();
            String environment = testCaseExecution.getEnvironment();
            
            AnswerItem answerLib = testDataLibService.readByNameBySystemByEnvironmentByCountry(libName, system, environment, country);
            
            if(answerLib.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                TestDataLib lib = (TestDataLib) answerLib.getItem();
                //unescape all attributes that were escaped: description, script, method, servicepath and envelope
                unescapeTestDataLibrary(lib);                
                if(lib.getType().equals(TestDataLibTypeEnum.SOAP.getCode())){
                    //now we can call the soap entry
                    calculateInnerProperties(lib, testCaseStepActionExecution);
                    AnswerItem callAnswer = testDataLibService.fetchData(lib, currentProperty.getRowLimit(), currentProperty.getNature());
                    
                    //updates the result data
                    if(callAnswer.isCodeEquals(MessageEventEnum.PROPERTY_SUCCESS.getCode())){
                        //new need to update the results list and the execution operation
                        TestDataLibResultSOAP resultSoap = (TestDataLibResultSOAP) callAnswer.getItem();

                        HashMap<String, TestDataLibResult> currentListResults = testCaseExecution.getDataLibraryExecutionDataList();
                        if(currentListResults == null){
                            currentListResults = new HashMap<String, TestDataLibResult>();
                        }
                        if(currentListResults.get(propertyName) != null){
                            currentListResults.remove(propertyName);
                        }                        
                        currentListResults.put(propertyName, resultSoap);
                        //updates the execution data list
                        testCaseExecution.setDataLibraryExecutionDataList(currentListResults);
                    }
                    message = callAnswer.getResultMessage();
                }else{
                    message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOTSOAP);
                    message.setDescription(message.getDescription().replace("%ENTRY%", libName));
                }
            }else{
                //library entry is not defined for the specified: name + country + system + environment
                message = new MessageEvent(MessageEventEnum.TESTDATALIB_NOT_FOUND_ERROR);
                message.setDescription(message.getDescription().replace("%ITEM%", libName).
                        replace("%COUNTRY%", country).
                        replace("%ENVIRONMENT%", environment).
                        replace("%SYSTEM%", system));
            }
            
        }else{
            //property that is being used to invoke the SOAP does not exist for the current country
            message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION);
            message.setDescription(message.getDescription().replace("%PROP%", propertyName).
                        replace("%COUNTRY%", country));
        }
        
        answerItem.setResultMessage(message);
        return answerItem;
    }
    private MessageEvent createGetFromDataLibSuccessMessage(TestDataLib lib) {
        
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB); ; 
        if(lib.getType().equals("SOAP")){
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("type", "SOAP");
            options.put("description", msg.getDescription());
            options.put("request", "%REQUEST_NAME%");
            options.put("response", "%RESPONSE_NAME%");

            msg = MessageEventUtil.createMessageDescriptionJSONFormat(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB, options);
        }if(lib.getType().equals("SQL")){
            
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("type", "SQL");
            options.put("description", msg.getDescription());
            options.put("response", "%RESPONSE_NAME%");
            
            msg = MessageEventUtil.createMessageDescriptionJSONFormat(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB, options);
        }
        
        return msg;
    }

    private void unescapeTestDataLibrary(TestDataLib lib) {
        //unescape all data  
        lib.setDescription(StringEscapeUtils.unescapeHtml4(lib.getDescription()));
        //SQL
        lib.setScript(StringEscapeUtils.unescapeHtml4(lib.getScript()));

        //SOAP
        lib.setServicePath(StringEscapeUtils.unescapeHtml4(lib.getServicePath()));
        lib.setMethod(StringEscapeUtils.unescapeHtml4(lib.getMethod()));
        lib.setEnvelope(StringEscapeUtils.unescapeXml(lib.getEnvelope()));
    }
}