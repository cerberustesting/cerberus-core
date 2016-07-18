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
package org.cerberus.engine.gwt.impl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
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
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionData;
import org.cerberus.crud.service.ICountryEnvironmentDatabaseService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ISoapLibraryService;
import org.cerberus.crud.service.ISqlLibraryService;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.crud.service.ITestDataService;
import org.cerberus.crud.service.impl.TestDataLibService;
import org.cerberus.engine.entity.SOAPExecution;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.PropertyTypeEnum;
import org.cerberus.enums.TestDataLibTypeEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.engine.execution.IIdentifierService;
import org.cerberus.service.json.IJsonService;
import org.cerberus.engine.gwt.IPropertyService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.service.soap.ISoapService;
import org.cerberus.service.webdriver.IWebDriverService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.engine.entity.TestDataLibResult;
import org.cerberus.engine.entity.TestDataLibResultSOAP;
import org.cerberus.engine.entity.TestDataLibResultSQL;
import org.cerberus.engine.entity.TestDataLibResultStatic;
import org.cerberus.service.groovy.IGroovyService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.FileUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SoapUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.XmlUtil;
import org.cerberus.util.XmlUtilException;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

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
    private ITestCaseExecutionDataDAO testCaseExecutionDataDAO;
    @Autowired
    private IJsonService jsonService;
    @Autowired
    private IGroovyService groovyService;
    @Autowired
    private IIdentifierService identifierService;
    @Autowired
    private IRecorderService recorderService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService;

    /**
     * The property variable {@link Pattern}
     */
    public static final Pattern PROPERTY_VARIABLE_PATTERN = Pattern.compile("%[^%]+%");

    @Override
    public String decodeValueWithExistingProperties(String stringToDecode, TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {
        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        TestCaseStepExecution tCSExecution = testCaseStepActionExecution.getTestCaseStepExecution();
        String test = testCaseStepActionExecution.getTest();
        String testCase = testCaseStepActionExecution.getTestCase();
        String usedTest = tCSExecution.getUseStepTest();
        String usedTestCase = tCSExecution.getUseStepTestCase();
        String country = tCExecution.getCountry();
        long now = new Date().getTime();
        String stringToDecodeInit = stringToDecode;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to decode string : : " + stringToDecode);
        }

        /**
         * Decode System Variable.
         */
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to decode (system variable) string : " + stringToDecode);
        }
        stringToDecode = decodeStringWithSystemVariable(stringToDecode, tCExecution);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished to decode (system variable). Result : " + stringToDecode);
        }

        if (!(stringToDecode.contains("%"))) { // We escape if property do not contain at least 2 % char
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode String (no more properties to decode after system variable replace) : '" + stringToDecodeInit + "' to :'" + stringToDecode + "'");
            }
            return stringToDecode;
        }

        /**
         * Look at all the potencial properties still contained in
         * StringToDecode (considering that properties are between %).
         */
        List<String> internalPropertiesFromStringToDecode = this.getPropertiesListFromString(stringToDecode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Internal potencial properties still found inside property '" + stringToDecode + "' : " + internalPropertiesFromStringToDecode);
        }

        if (internalPropertiesFromStringToDecode.isEmpty()) { // We escape if no property found on the string to decode
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (no properties detected in string) : . result : '" + stringToDecodeInit + "' to :'" + stringToDecode + "'");
            }
            return stringToDecode;
        }

        /**
         * Get the list of properties needed to calculate the required property
         */
        List<TestCaseCountryProperties> tcProperties = tCExecution.getTestCaseCountryPropertyList();
        List<TestCaseCountryProperties> linkedProperties = new ArrayList();
        for (String internalProperty : internalPropertiesFromStringToDecode) { // Looping on potential properties in string to decode.
            List<TestCaseCountryProperties> newLinkedProperties = new ArrayList();
            newLinkedProperties = this.getListOfPropertiesLinkedToProperty(test, testCase, country, internalProperty, usedTest, usedTestCase, new ArrayList(), tcProperties);
            linkedProperties.addAll(newLinkedProperties);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + internalProperty + " need calculation of these (" + newLinkedProperties.size() + ") property(ies) " + newLinkedProperties);
            }
        }

        /**
         * For all linked properties, calculate it if needed.
         */
        for (TestCaseCountryProperties eachTccp : linkedProperties) {
            TestCaseExecutionData tecd;
            /**
             * First create testCaseExecutionData object
             */
            now = new Date().getTime();
            tecd = factoryTestCaseExecutionData.create(tCExecution.getId(), eachTccp.getProperty(), eachTccp.getDescription(), null, eachTccp.getType(),
                    eachTccp.getValue1(), eachTccp.getValue2(), null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING));
            tecd.setTestCaseCountryProperties(eachTccp);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Trying to calculate Property : '" + tecd.getProperty() + "' " + tecd);
            }

            List<TestCaseExecutionData> dataList = tCExecution.getTestCaseExecutionDataList();

            /*  First check if property has already been calculated 
                 *  if action is calculateProperty, then set isKnownData to false. 
             */
            tecd = getExecutionDataFromList(dataList, eachTccp, forceCalculation, tecd);

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
                calculateProperty(tecd, testCaseStepActionExecution, eachTccp, forceCalculation);
                //saves the result 
                try {
                    testCaseExecutionDataService.insertOrUpdateTestCaseExecutionData(tecd);
                } catch (CerberusException cex) {
                    LOG.error(cex.getMessage(), cex);
                }
            }

            //if the property result message indicates that we need to stop the test action, then the action is notified               
            //or if the property was not successfully calculated, either because it was not defined for the country or because it does not exist
            //then we notify the execution
            if (tecd.getPropertyResultMessage().isStopTest()
                    || tecd.getPropertyResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION.getCode()) {
                testCaseStepActionExecution.setStopExecution(tecd.isStopExecution());
                testCaseStepActionExecution.setActionResultMessage(tecd.getPropertyResultMessage());
                testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(tecd.getPropertyResultMessage().getMessage()));
            }
//            }
            /**
             * Add TestCaseExecutionData in TestCaseExecutionData List of the
             * TestCaseExecution
             */
            tCExecution.getTestCaseExecutionDataList().add(tecd);
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, "Adding into Execution data list: " + eachTccp.getProperty() + " : " + eachTccp.getValue1() + " : " + tecd.getValue());

            /**
             * After calculation, replace properties by value calculated
             */
            stringToDecode = decodeStringWithAlreadyCalculatedProperties(stringToDecode, tCExecution);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + eachTccp.getProperty() + " calculated with Value = " + tecd.getValue() + ", Value1 = " + tecd.getValue1() + ", Value2 = " + tecd.getValue2());
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished to decode String : '" + stringToDecodeInit + "' to :'" + stringToDecode + "'");
        }
        return stringToDecode;
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
    private TestCaseExecutionData getExecutionDataFromList(List<TestCaseExecutionData> dataList, TestCaseCountryProperties eachTccp, boolean forceCalculation, TestCaseExecutionData tecd) {

        for (int iterator = 0; iterator < dataList.size(); iterator++) {
            if (dataList.get(iterator).getProperty().equalsIgnoreCase(eachTccp.getProperty())) {
                if (!forceCalculation) {
                    //If Calculation not forced , set tecd to the previous property already calculated.
                    tecd = dataList.get(iterator);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Property has already been calculated : " + tecd);
                    }
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("Property has already been calculated but we will force a new calculation : " + eachTccp);
                }
                dataList.remove(iterator);
                break;
            }
        }
        return tecd;

    }

    private List<TestCaseCountryProperties> getListOfPropertiesLinkedToProperty(String test, String testCase, String country, String property, String usedTest, String usedTestCase, List<String> crossedProperties, List<TestCaseCountryProperties> propertieOfTestcase) {
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
            tccpToReturn.setType("");
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

        // Value1 treatment
        List<String> propertiesValue1 = new ArrayList();
        //check the properties specified in the test
        for (String propSqlName : this.getPropertiesListFromString(testCaseCountryProperty.getValue1())) {
            for (TestCaseCountryProperties pr : propertieOfTestcase) {
                if (pr.getProperty().equals(propSqlName)) {
                    propertiesValue1.add(propSqlName);
                    break;
                }
            }
        }
        allProperties.addAll(propertiesValue1);

        // Value2 treatment :
        List<String> propertiesValue2 = new ArrayList();
        //check the properties specified in the test
        for (String propSqlName : this.getPropertiesListFromString(testCaseCountryProperty.getValue2())) {
            for (TestCaseCountryProperties pr : propertieOfTestcase) {
                if (pr.getProperty().equals(propSqlName)) {
                    propertiesValue2.add(propSqlName);
                    break;
                }
            }
        }
        allProperties.addAll(propertiesValue2);

        for (String internalProperty : allProperties) {
            result.addAll(getListOfPropertiesLinkedToProperty(test, testCase, country, internalProperty, usedTest, usedTestCase, crossedProperties, propertieOfTestcase));
        }
        result.add(testCaseCountryProperty);

        return result;
    }

    private String decodeStringWithSystemVariable(String stringToDecode, TestCaseExecution tCExecution) {
        /**
         * Trying to replace by system environment variables .
         */
        stringToDecode = stringToDecode.replace("%SYS_SYSTEM%", tCExecution.getApplication().getSystem());
        stringToDecode = stringToDecode.replace("%SYS_APPLI%", tCExecution.getApplication().getApplication());
        stringToDecode = stringToDecode.replace("%SYS_APP_DOMAIN%", tCExecution.getCountryEnvironmentParameters().getDomain());
        stringToDecode = stringToDecode.replace("%SYS_APP_HOST%", tCExecution.getCountryEnvironmentParameters().getIp());
        stringToDecode = stringToDecode.replace("%SYS_ENV%", tCExecution.getEnvironmentData());
        stringToDecode = stringToDecode.replace("%SYS_ENVGP%", tCExecution.getEnvironmentDataObj().getGp1());
        stringToDecode = stringToDecode.replace("%SYS_COUNTRY%", tCExecution.getCountry());
        stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP1%", tCExecution.getCountryObj().getGp1());
        stringToDecode = stringToDecode.replace("%SYS_SSIP%", tCExecution.getSeleniumIP());
        stringToDecode = stringToDecode.replace("%SYS_SSPORT%", tCExecution.getSeleniumPort());
        stringToDecode = stringToDecode.replace("%SYS_TAG%", tCExecution.getTag());
        stringToDecode = stringToDecode.replace("%SYS_EXECUTIONID%", String.valueOf(tCExecution.getId()));

        /**
         * Trying to replace date variables .
         */
        stringToDecode = stringToDecode.replace("%SYS_TODAY-yyyy%", DateUtil.getTodayFormat("yyyy"));
        stringToDecode = stringToDecode.replace("%SYS_TODAY-MM%", DateUtil.getTodayFormat("MM"));
        stringToDecode = stringToDecode.replace("%SYS_TODAY-dd%", DateUtil.getTodayFormat("dd"));
        stringToDecode = stringToDecode.replace("%SYS_TODAY-doy%", DateUtil.getTodayFormat("D"));
        stringToDecode = stringToDecode.replace("%SYS_TODAY-HH%", DateUtil.getTodayFormat("HH"));
        stringToDecode = stringToDecode.replace("%SYS_TODAY-mm%", DateUtil.getTodayFormat("mm"));
        stringToDecode = stringToDecode.replace("%SYS_TODAY-ss%", DateUtil.getTodayFormat("ss"));
        stringToDecode = stringToDecode.replace("%SYS_YESTERDAY-yyyy%", DateUtil.getYesterdayFormat("yyyy"));
        stringToDecode = stringToDecode.replace("%SYS_YESTERDAY-MM%", DateUtil.getYesterdayFormat("MM"));
        stringToDecode = stringToDecode.replace("%SYS_YESTERDAY-dd%", DateUtil.getYesterdayFormat("dd"));
        stringToDecode = stringToDecode.replace("%SYS_YESTERDAY-doy%", DateUtil.getYesterdayFormat("D"));
        stringToDecode = stringToDecode.replace("%SYS_YESTERDAY-HH%", DateUtil.getYesterdayFormat("HH"));
        stringToDecode = stringToDecode.replace("%SYS_YESTERDAY-mm%", DateUtil.getYesterdayFormat("mm"));
        stringToDecode = stringToDecode.replace("%SYS_YESTERDAY-ss%", DateUtil.getYesterdayFormat("ss"));

        /**
         * Trying to replace timing variables .
         */
        stringToDecode = stringToDecode.replace("%SYS_ELAPSED-EXESTART%", "To Be Implemented");
        stringToDecode = stringToDecode.replace("%SYS_ELAPSED-STEPSTART%", "To Be Implemented");

        return stringToDecode;
    }

    private String decodeStringWithAlreadyCalculatedProperties(String stringToReplace, TestCaseExecution tCExecution) {

        for (TestCaseExecutionData tced : tCExecution.getTestCaseExecutionDataList()) {

            if ((tced.getType() != null) && (tced.getType().equals(PropertyTypeEnum.GET_FROM_DATALIB.getPropertyName()))) { // Type could be null in case property do not exist.
                // Key value of the DataLib.
                if (tced.getValue() != null) {
                    stringToReplace = stringToReplace.replace("%" + tced.getProperty() + "%", tced.getValue());
                }
                // For each subdata of the getFromDataLib_BETA property, we try to replace with PROPERTY(SUBDATA).
                for (String key : tced.getDataLibRawData().keySet()) {
                    if (tced.getDataLibRawData().get(key) != null) {
                        stringToReplace = stringToReplace.replace("%" + tced.getProperty() + "(" + key + ")%", tced.getDataLibRawData().get(key));
                    }
                }
            } else if (tced.getValue() != null) {
                stringToReplace = stringToReplace.replace("%" + tced.getProperty() + "%", tced.getValue());
            }
        }

        return stringToReplace;
    }

    /**
     * Gets all properties names contained into the given {@link String}
     *
     * <p>
     * A property is defined by including its name between two '%' character.
     * </p>
     *
     * @see #PROPERTY_VARIABLE_PATTERN
     * @param str the {@link String} to get all properties
     * @return a list of properties contained into the given {@link String}
     */
    private List<String> getPropertiesListFromString(String str) {
        List<String> properties = new ArrayList<String>();
        if (str == null) {
            return properties;
        }

        Matcher propertyMatcher = PROPERTY_VARIABLE_PATTERN.matcher(str);
        while (propertyMatcher.find()) {
            String rawProperty = propertyMatcher.group();
            // Removes the first and last '%' character to only get the property name
            rawProperty = rawProperty.substring(1, rawProperty.length() - 1);
            // Removes the variable part of the property eg : (subdata)
            String[] ramProp = rawProperty.split("\\(");
            properties.add(ramProp[0]);
        }
        return properties;
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
                        LOG.debug("Property found : " + tccp);
                    }
                    break;
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
            msg.setDescription(msg.getDescription().replace("%COUNTRY%", country));
            msg.setDescription(msg.getDescription().replace("%PROP%", property));
            item.setResultMessage(msg);
            if (LOG.isDebugEnabled()) {
                LOG.debug(msg.getDescription());
            }
        }
        item.setItem(testCaseCountryProperty);
        return item;
    }

    private void calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseStepActionExecution testCaseStepActionExecution,
            TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        testCaseExecutionData.setStart(new Date().getTime());
        MessageEvent res;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to calculate Property : '" + testCaseCountryProperty.getProperty() + "'");
        }

        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();

        /**
         * Decode Property replacing properties encapsulated with %
         */
        if (testCaseCountryProperty.getValue1().contains("%")) {
            String decodedValue = decodeStringWithSystemVariable(testCaseCountryProperty.getValue1(), tCExecution);
            decodedValue = this.decodeStringWithAlreadyCalculatedProperties(decodedValue, tCExecution);
            testCaseExecutionData.setValue1(decodedValue);
        }

        if (testCaseCountryProperty.getValue2() != null && testCaseCountryProperty.getValue2().contains("%")) {
            String decodedValue = decodeStringWithSystemVariable(testCaseCountryProperty.getValue2(), tCExecution);
            decodedValue = this.decodeStringWithAlreadyCalculatedProperties(decodedValue, tCExecution);
            testCaseExecutionData.setValue2(decodedValue);
        }

        /**
         * Calculate Property regarding the type
         */
        if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.EXECUTE_SQL_FROM_LIB.getPropertyName())) {
            testCaseExecutionData = this.property_executeSqlFromLib(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.EXECUTE_SQL.getPropertyName())) {
            testCaseExecutionData = this.property_executeSql(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.TEXT.getPropertyName())) {
            testCaseExecutionData = this.property_calculateText(testCaseExecutionData, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_HTML_VISIBLE.getPropertyName())) {
            testCaseExecutionData = this.property_getFromHtmlVisible(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_HTML.getPropertyName())) {
            testCaseExecutionData = this.property_getFromHtml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_JS.getPropertyName())) {
            testCaseExecutionData = this.property_getFromJS(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_GROOVY.getPropertyName())) {
            testCaseExecutionData = this.property_getFromGroovy(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_TEST_DATA.getPropertyName())) {
            testCaseExecutionData = this.property_getFromTestData(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_ATTRIBUTE_FROM_HTML.getPropertyName())) {
            testCaseExecutionData = this.property_getAttributeFromHtml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_COOKIE.getPropertyName())) {
            testCaseExecutionData = this.property_getFromCookie(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_XML.getPropertyName())) {
            testCaseExecutionData = this.property_getFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_JSON.getPropertyName())) {
            testCaseExecutionData = this.property_getFromJson(testCaseExecutionData, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.EXECUTE_SOAP_FROM_LIB.getPropertyName())) {
            testCaseExecutionData = this.property_executeSoapFromLib(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_FROM_DATALIB.getPropertyName())) {
            testCaseExecutionData = this.property_getFromDataLib(testCaseExecutionData, tCExecution, testCaseStepActionExecution, testCaseCountryProperty, forceRecalculation);

        } else if (testCaseCountryProperty.getType().equals(PropertyTypeEnum.GET_DIFFERENCES_FROM_XML.getPropertyName())) {
            testCaseExecutionData = this.property_getDifferencesFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);

        } else {
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_UNKNOWNPROPERTY);
            res.setDescription(res.getDescription().replace("%PROPERTY%", testCaseCountryProperty.getType()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }

        testCaseExecutionData.setEnd(new Date().getTime());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished to calculate Property : '" + testCaseCountryProperty.getProperty() + "'");
        }

    }

    private TestCaseExecutionData property_executeSqlFromLib(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution, boolean forceCalculation) {
        try {
            String script = this.sqlLibraryService.findSqlLibraryByKey(testCaseExecutionData.getValue1()).getScript();
            testCaseExecutionData.setValue1(script); //TODO use the new library 

        } catch (CerberusException ex) {
            Logger.getLogger(PropertyService.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_SQLLIB_NOTEXIT);

            res.setDescription(res.getDescription().replace("%SQLLIB%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);

            testCaseExecutionData.setEnd(
                    new Date().getTime());
            return testCaseExecutionData;
        }
        testCaseExecutionData = this.property_executeSql(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceCalculation);
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_executeSql(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution, boolean forceCalculation) {
        return sQLService.calculateOnDatabase(testCaseExecutionData, testCaseCountryProperty, tCExecution);
    }

    private TestCaseExecutionData property_calculateText(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
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
                res.setDescription(res.getDescription().replace("%FORCED%", forceRecalculation == true ? "Re-" : ""));
                res.setDescription(res.getDescription().replace("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
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

            res.setDescription(res.getDescription().replace("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromHtmlVisible(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(testCaseExecutionData.getValue1());
            String valueFromHTML = this.webdriverService.getValueFromHTMLVisible(tCExecution.getSession(), identifier);
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTMLVISIBLE);
                res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%VALUE%", valueFromHTML));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);
                res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (NoSuchElementException exception) {
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);

            res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromHtml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(testCaseExecutionData.getValue1());
            String valueFromHTML = this.webdriverService.getValueFromHTML(tCExecution.getSession(), identifier);
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTML);
                res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%VALUE%", valueFromHTML));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (NoSuchElementException exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ELEMENTDONOTEXIST);

            res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromJS(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {

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
            res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%VALUE%", script));
            testCaseExecutionData.setPropertyResultMessage(res);
        } else {
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_JS_EXCEPTION);
            res.setDescription(res.getDescription().replace("%EXCEPTION%", message));
            testCaseExecutionData.setPropertyResultMessage(res);
        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromGroovy(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        // Check if script has been correctly defined
        String script = testCaseExecutionData.getValue1();
        if (script == null || script.isEmpty()) {
            testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMGROOVY_NULL));
            return testCaseExecutionData;
        }

        // Try to evaluate Groovy script
        try {
            String valueFromGroovy = groovyService.eval(script);
            testCaseExecutionData.setValue(valueFromGroovy);
            testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMGROOVY)
                    .resolveDescription("VALUE", valueFromGroovy));
        } catch (IGroovyService.IGroovyServiceException e) {
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, "Exception Running Grrovy Script :" + e.getMessage());
            testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMGROOVY_EXCEPTION).resolveDescription("REASON", e.getMessage()));
        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromTestData(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        String propertyValue = "";

        try {
            propertyValue = testCaseExecutionData.getValue1();
            String valueFromTestData = testDataService.findTestDataByKey(propertyValue, tCExecution.getApplication().getApplication(),
                    tCExecution.getEnvironmentData(), tCExecution.getCountry()).getValue();
            if (valueFromTestData != null) {
                testCaseExecutionData.setValue(valueFromTestData);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_TESTDATA);
                res.setDescription(res.getDescription().replace("%PROPERTY%", propertyValue));
                res.setDescription(res.getDescription().replace("%VALUE%", valueFromTestData));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (CerberusException exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, "Exception Getting value from TestData for data :'" + propertyValue + "'\n" + exception.getMessageError().getDescription());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);

            res.setDescription(res.getDescription().replace("%PROPERTY%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getAttributeFromHtml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        MessageEvent res;
        try {
            Identifier identifier = identifierService.convertStringToIdentifier(testCaseExecutionData.getValue1());
            String valueFromHTML = this.webdriverService.getAttributeFromHtml(tCExecution.getSession(), identifier, testCaseExecutionData.getValue2());
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETATTRIBUTEFROMHTML);
                res.setDescription(res.getDescription().replace("%VALUE%", valueFromHTML));
            } else {
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ATTRIBUTEDONOTEXIST);
            }
            res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%ATTRIBUTE%", testCaseExecutionData.getValue2()));

        } catch (NoSuchElementException exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, exception.toString());
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);

            res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
        }
        testCaseExecutionData.setPropertyResultMessage(res);
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_executeSoapFromLib(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        String result = null;
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

                //Call Soap and set LastSoapCall of the testCaseExecution.
                AnswerItem soapCall = soapService.callSOAP(soapLib.getEnvelope(), soapLib.getServicePath(), soapLib.getMethod(), attachement);
                tCExecution.setLastSOAPCalled(soapCall);

                //Record the Request and Response.
                String requestFileName = FileUtil.generateScreenshotFilename(null, null, null, null, null, testCaseExecutionData.getProperty() + "_request", "xml");
                String responseFileName = FileUtil.generateScreenshotFilename(null, null, null, null, null, testCaseExecutionData.getProperty() + "_response", "xml");
                SOAPExecution se = (SOAPExecution) soapCall.getItem();

                String requestFilePath = recorderService.recordSoapMessageAndGetPath(tCExecution.getId(), se.getSOAPRequest(), requestFileName);
                String responseFilePath = recorderService.recordSoapMessageAndGetPath(tCExecution.getId(), se.getSOAPResponse(), responseFileName);

                if (soapCall.isCodeEquals(200)) {
                    SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
                    String xmlResponse = SoapUtil.convertSoapMessageToString(lastSoapCalled.getSOAPResponse());
                    result = xmlUnitService.getFromXml(xmlResponse, null, soapLib.getParsingAnswer());
                }
                if (result != null) {
                    testCaseExecutionData.setValue(result);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SOAP);
                    res.setDescription(res.getDescription().replace("%REQUEST_PATH%", requestFilePath));
                    res.setDescription(res.getDescription().replace("%REQUEST_PATH%", responseFilePath));
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SOAPFROMLIB_NODATA);
                    res.setDescription(res.getDescription().replace("%REQUEST_PATH%", requestFilePath));
                    res.setDescription(res.getDescription().replace("%REQUEST_PATH%", responseFilePath));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            }
        } catch (CerberusException exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.ERROR, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);

            res.setDescription(res.getDescription().replace("%PROPERTY%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromXml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        String xmlResponse = "";
        try {
            /**
             * If tCExecution LastSoapCalled exist, get the response;
             */
            if (null != tCExecution.getLastSOAPCalled()) {
                SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
                xmlResponse = SoapUtil.convertSoapMessageToString(lastSoapCalled.getSOAPResponse());
            }
            String valueFromXml = xmlUnitService.getFromXml(xmlResponse, testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (valueFromXml != null) {
                testCaseExecutionData.setValue(valueFromXml);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMXML);
                res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMXML);
                res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception ex) {
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, ex.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMXML);

            res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromCookie(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            String valueFromCookie = this.webdriverService.getFromCookie(tCExecution.getSession(), testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (valueFromCookie != null) {
                if (!valueFromCookie.equals("cookieNotFound")) {
                    testCaseExecutionData.setValue(valueFromCookie);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMCOOKIE);
                    res.setDescription(res.getDescription().replace("%COOKIE%", testCaseExecutionData.getValue1()));
                    res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
                    res.setDescription(res.getDescription().replace("%VALUE%", valueFromCookie));
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_COOKIENOTFOUND);
                    res.setDescription(res.getDescription().replace("%COOKIE%", testCaseExecutionData.getValue1()));
                    res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_PARAMETERNOTFOUND);
                res.setDescription(res.getDescription().replace("%COOKIE%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (Exception exception) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.DEBUG, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_COOKIENOTFOUND);

            res.setDescription(res.getDescription().replace("%COOKIE%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;

    }

    private TestCaseExecutionData property_getDifferencesFromXml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            MyLogger.log(PropertyService.class
                    .getName(), Level.INFO, "Computing differences between " + testCaseExecutionData.getValue1() + " and " + testCaseExecutionData.getValue2());
            String differences = xmlUnitService.getDifferencesFromXml(testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (differences
                    != null) {
                MyLogger.log(PropertyService.class.getName(), Level.INFO, "Computing done.");
                testCaseExecutionData.setValue(differences);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETDIFFERENCESFROMXML);
                res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                MyLogger.log(PropertyService.class.getName(), Level.INFO, "Computing failed.");
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETDIFFERENCESFROMXML);
                res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception ex) {
            MyLogger.log(PropertyService.class
                    .getName(), Level.INFO, ex.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETDIFFERENCESFROMXML);

            res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromJson(TestCaseExecutionData testCaseExecutionData, boolean forceRecalculation) {
        try {
            String valueFromJson = this.jsonService.getFromJson(testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (valueFromJson != null) {
                if (!"".equals(valueFromJson)) {
                    testCaseExecutionData.setValue(valueFromJson);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMJSON);
                    res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue1()));
                    res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
                    res.setDescription(res.getDescription().replace("%VALUE%", valueFromJson));
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
                    res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue1()));
                    res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
                res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (Exception exception) {
            if (LOG.isDebugEnabled()) {
                LOG.error(exception.toString());
            }
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);

            res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromDataLib(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {

        MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB);

        HashMap<String, TestDataLibResult> currentListResults = tCExecution.getDataLibraryExecutionDataList();
        if (currentListResults == null) {
            currentListResults = new HashMap<String, TestDataLibResult>();
        }
        TestDataLib testDataLib;
        TestDataLibResult result;

        // We get here the correct TestDataLib entry from the Value1 (name) that better match the context on system, environment and country.
        AnswerItem answer = testDataLibService.readByNameBySystemByEnvironmentByCountry(testCaseExecutionData.getValue1(),
                tCExecution.getApplication().getSystem(), tCExecution.getEnvironmentData(),
                tCExecution.getCountry(), null);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answer.getItem() != null) {
            testDataLib = (TestDataLib) answer.getItem();

            AnswerItem serviceAnswer;
            //result = currentListResults.get(String.valueOf(lib.getTestDataLibID()));                 
            result = currentListResults.get(testCaseCountryProperty.getProperty());
            // If is force calculation, then the entry will be recalculated
            if (forceRecalculation || (!forceRecalculation && result == null)) {

                if (forceRecalculation && result != null) {
                    currentListResults.remove(testCaseCountryProperty.getProperty());//removes the result when we are recalculating
                }

                //check if there are properties defined in the data specification
                try {
                    if (testDataLib.getType().equals(TestDataLibTypeEnum.SOAP.getCode())) {
                        //check if the servicepath contains properties that neeed to be calculated
                        String decodedServicePath = decodeValueWithExistingProperties(testDataLib.getServicePath(), testCaseStepActionExecution, false);
                        testDataLib.setServicePath(decodedServicePath);
                        //check if the method contains properties that neeed to be calculated
                        String decodedMethod = decodeValueWithExistingProperties(testDataLib.getMethod(), testCaseStepActionExecution, false);
                        testDataLib.setMethod(decodedMethod);
                        //check if the envelope contains properties that neeed to be calculated
                        String decodedEnvelope = decodeValueWithExistingProperties(testDataLib.getEnvelope(), testCaseStepActionExecution, false);
                        testDataLib.setEnvelope(decodedEnvelope);

                    } else if (testDataLib.getType().equals(TestDataLibTypeEnum.SQL.getCode())) {
                        //check if the script contains properties that neeed to be calculated
                        String decodedScript = decodeValueWithExistingProperties(testDataLib.getScript(), testCaseStepActionExecution, false);
                        testDataLib.setScript(decodedScript);

                    }
                } catch (CerberusEventException cex) {
                    Logger.getLogger(PropertyService.class.getName()).log(java.util.logging.Level.SEVERE, "calculateInnerProperties", cex);
                }

                //we need to recalculate the result for the lib
                serviceAnswer = this.fetchDataFromTestDataLib(testDataLib, testCaseCountryProperty, tCExecution);

                res = serviceAnswer.getResultMessage();
                result = (TestDataLibResult) serviceAnswer.getItem(); //test data library returned by the service

            }

            if (result != null) {
                // Keeping raw data to testCaseExecutionData object.
                testCaseExecutionData.setDataLibRawData(result.getDataLibRawData());

                // Value of testCaseExecutionData object takes the master subdata entry "".
                String value = (String) result.getDataLibRawData().get("");
                testCaseExecutionData.setValue(value);

                // Updates the result data
                currentListResults.put(testCaseCountryProperty.getProperty(), result);

                // Updates the execution data list
                tCExecution.setDataLibraryExecutionDataList(currentListResults);
            }
            res.setDescription(res.getDescription().replace("%ENTRY%", testDataLib.getName()).replace("%ENTRYID%", String.valueOf(testDataLib.getTestDataLibID())));

        } else {//no TestDataLib found was returned
            //the library does not exist at all
            AnswerList nameExistsAnswer = testDataLibService.readNameListByName(testCaseExecutionData.getValue1(), 1);
            if (nameExistsAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && nameExistsAnswer.getTotalRows() > 0) {
                //if the library name exists but was not available or does not exist for the current specification but exists for other countries/environments/systems
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOT_FOUND_ERROR);
                res.setDescription(res.getDescription().replace("%ITEM%", testCaseExecutionData.getValue1()).
                        replace("%COUNTRY%", tCExecution.getCountryEnvironmentParameters().getCountry()).
                        replace("%ENVIRONMENT%", tCExecution.getCountryEnvironmentParameters().getEnvironment()).
                        replace("%SYSTEM%", tCExecution.getCountryEnvironmentParameters().getSystem()));
            } else {
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOT_EXIST_ERROR);
                res.setDescription(res.getDescription().replace("%ITEM%", testCaseExecutionData.getValue1()));
            }

        }
        res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
        testCaseExecutionData.setPropertyResultMessage(res);

        return testCaseExecutionData;
    }

    public AnswerItem fetchDataFromTestDataLib(TestDataLib lib, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        TestDataLibResult result = null;

        if (lib.getType().equals(TestDataLibTypeEnum.STATIC.getCode())) {
            AnswerItem staticResult = fetchDataSTATIC(lib, testCaseCountryProperty, tCExecution);
            result = (TestDataLibResult) staticResult.getItem();
            msg = staticResult.getResultMessage();
            answer.setItem(result);

        } else if (lib.getType().equals(TestDataLibTypeEnum.SQL.getCode())) {
            AnswerItem sqlResult = fetchDataSQL(lib, testCaseCountryProperty, tCExecution);
            result = (TestDataLibResult) sqlResult.getItem();
            msg = sqlResult.getResultMessage();
            answer.setItem(result);

        } else if (lib.getType().equals(TestDataLibTypeEnum.SOAP.getCode())) {
            AnswerItem soapResult = fetchDataSOAP(lib, testCaseCountryProperty, tCExecution);
            result = (TestDataLibResult) soapResult.getItem();
            msg = soapResult.getResultMessage();
            answer.setItem(result);
        }
        answer.setResultMessage(msg);
        return answer;
    }

    private AnswerItem<TestDataLibResult> fetchDataSTATIC(TestDataLib lib, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution) {
        AnswerItem answer;
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        TestDataLibResult result = null;

        MyLogger.log(TestDataLibService.class.getName(), Level.DEBUG, "Test data lib service STATIC.");

        //sql data needs to collect the values for the n columns
        answer = calculateOnStaticDataLibNColumns(
                lib.getSystem(),
                lib.getCountry(),
                lib.getEnvironment(),
                testCaseCountryProperty, tCExecution);

        //if the sql service returns a success message then we can process it
        if (answer.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_STATIC_STATIC.getCode()) {
            HashMap<String, String> columns = (HashMap<String, String>) answer.getItem();

            result = new TestDataLibResultStatic();
            result.setTestDataLibID((int) Integer.valueOf(columns.get("TestDataLibID"))); // ID is taken from the selected line.

            // saving the raw data to the result.
            result.setDataLibRawData(columns);

            answer.setItem(result);

        } else if (answer.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_STATIC_NODATA.getCode()) {
            //if the script does not return 
            answer.setItem(result);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_STATIC_NODATA);
            msg.setDescription(msg.getDescription().replace("%ENTRY%", lib.getName()).replace("%SQL%", lib.getScript())
                    .replace("%DATABASE%", lib.getDatabase()));
            answer.setResultMessage(msg);

        } else {
            //other error had occured
            answer.setItem(result);
            msg = answer.getResultMessage();
            msg.setDescription(msg.getDescription().replace("%ENTRY%", lib.getName()).replace("%SQL%", lib.getScript()).replace("%ENTRYID%", lib.getTestDataLibID().toString())
                    .replace("%DATABASE%", lib.getDatabase()));
            answer.setResultMessage(msg);

        }
        return answer;

    }

    private AnswerItem<TestDataLibResult> fetchDataSQL(TestDataLib lib, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution) {
        AnswerItem answer;
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        TestDataLibResult result = null;

        MyLogger.log(TestDataLibService.class.getName(), Level.INFO, "Test data lib service SQL " + lib.getScript());

        //sql data needs to collect the values for the n columns
        answer = sQLService.calculateOnDatabaseNColumns(lib.getScript(), lib.getDatabase(),
                tCExecution.getCountryEnvironmentParameters().getSystem(),
                tCExecution.getCountryEnvironmentParameters().getCountry(),
                tCExecution.getCountryEnvironmentParameters().getEnvironment(),
                testCaseCountryProperty, lib.getSubDataColumn(), tCExecution, lib.getTestDataLibID());

        //if the sql service returns a success message then we can process it
        if (answer.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_SQL.getCode()) {
            HashMap<String, String> columns = (HashMap<String, String>) answer.getItem();
            result = new TestDataLibResultSQL();
            result.setTestDataLibID(lib.getTestDataLibID());

            // saving the raw data to the result.
            ((TestDataLibResultSQL) result).setRawData(columns);

            // saving the raw data to the result.
            result.setDataLibRawData(columns);

            answer.setItem(result);

        } else if (answer.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_SQL_NODATA.getCode()) {
            //if the script does not return 
            answer.setItem(result);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_NODATA);
            msg.setDescription(msg.getDescription().replace("%ENTRY%", lib.getName()).replace("%SQL%", lib.getScript())
                    .replace("%DATABASE%", lib.getDatabase()));
            answer.setResultMessage(msg);

        } else {
            //other error had occured
            answer.setItem(result);
            msg = answer.getResultMessage();
            msg.setDescription(msg.getDescription().replace("%ENTRY%", lib.getName()).replace("%SQL%", lib.getScript()).replace("%ENTRYID%", lib.getTestDataLibID().toString())
                    .replace("%DATABASE%", lib.getDatabase()));
            answer.setResultMessage(msg);

        }
        return answer;
    }

    private AnswerItem<TestDataLibResult> fetchDataSOAP(TestDataLib lib, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg;
        TestDataLibResult result = null;

        SOAPExecution executionSoap = new SOAPExecution();
        HashMap<String, String> resultHash = new HashMap<>();
        List<HashMap<String, String>> listResult = new ArrayList<HashMap<String, String>>();

        // Temporary list of string.
        List<String> listTemp1 = null;

        // String containing the XML
        String xmlResponseString = "";

        // system that will be used to get some parameters.
        String system = tCExecution.getApplication().getSystem();

        /**
         * Before making the call we check if the Service Path is already a
         * propper URL. If it is not, we prefix with the SoapUrl defined from
         * corresponding database. This is used to get the data from the correct
         * environment.
         */
        String servicePath = lib.getServicePath();
        LOG.debug("Service Path : " + lib.getServicePath());
        if (!StringUtil.isURL(servicePath)) {
            // Url is not valid, we try to get the corresponding DatabaseURL SoapURL to prefix.
            if (!(StringUtil.isNullOrEmpty(lib.getDatabaseUrl()))) {

                try {
                    CountryEnvironmentDatabase countryEnvironmentDatabase;
                    countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system,
                            tCExecution.getCountry(), tCExecution.getEnvironmentData(), lib.getDatabaseUrl()));
                    if (countryEnvironmentDatabase == null) {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKOANDDATABASESOAPURLNOTEXIST);
                        msg.setDescription(msg.getDescription()
                                .replace("%SERVICEURL%", lib.getServicePath())
                                .replace("%SYSTEM%", system)
                                .replace("%COUNTRY%", tCExecution.getCountry())
                                .replace("%ENV%", tCExecution.getEnvironmentData())
                                .replace("%DB%", lib.getDatabaseUrl()));
                        answer.setResultMessage(msg);
                        return answer;

                    } else {
                        String soapURL = countryEnvironmentDatabase.getSoapUrl();
                        if (StringUtil.isNullOrEmpty(soapURL)) {
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKOANDDATABASESOAPURLEMPTY);
                            msg.setDescription(msg.getDescription()
                                    .replace("%SERVICEURL%", lib.getServicePath())
                                    .replace("%SYSTEM%", system)
                                    .replace("%COUNTRY%", tCExecution.getCountry())
                                    .replace("%ENV%", tCExecution.getEnvironmentData())
                                    .replace("%DB%", lib.getDatabaseUrl()));
                            answer.setResultMessage(msg);
                            return answer;
                        }
                        // soapURL from database is not empty so we prefix the Service URL with it.
                        servicePath = soapURL + lib.getServicePath();

                        if (!StringUtil.isURL(servicePath)) {
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKO);
                            msg.setDescription(msg.getDescription()
                                    .replace("%SERVICEURL%", servicePath)
                                    .replace("%SOAPURL%", soapURL)
                                    .replace("%SERVICEPATH%", lib.getServicePath())
                                    .replace("%ENTRY%", lib.getName())
                                    .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                            answer.setResultMessage(msg);
                            return answer;

                        }

                    }

                } catch (CerberusException ex) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKOANDDATABASESOAPURLNOTEXIST);
                    msg.setDescription(msg.getDescription()
                            .replace("%SERVICEURL%", lib.getServicePath())
                            .replace("%SYSTEM%", system)
                            .replace("%COUNTRY%", tCExecution.getCountry())
                            .replace("%ENV%", tCExecution.getEnvironmentData())
                            .replace("%DB%", lib.getDatabaseUrl()));
                    answer.setResultMessage(msg);
                    return answer;
                }

            } else { // URL is not valid and DatabaseUrl is not defined.
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKOANDNODATABASE);
                msg.setDescription(msg.getDescription()
                        .replace("%SERVICEURL%", lib.getServicePath())
                        .replace("%ENTRY%", lib.getName())
                        .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                answer.setResultMessage(msg);
                return answer;
            }
        }

        // SOAP Call is made here.
        String key = TestDataLibTypeEnum.SOAP.getCode() + lib.getTestDataLibID();
        AnswerItem ai = soapService.callSOAP(lib.getEnvelope(), servicePath,
                lib.getMethod(), null);
        msg = ai.getResultMessage();

        //if the call returns success then we can process the soap ressponse
        if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()) {

            executionSoap = (SOAPExecution) ai.getItem();
            Document xmlDocument = xmlUnitService.getXmlDocument(SoapUtil.convertSoapMessageToString(executionSoap.getSOAPResponse()));

            // Call successful so we can start to parse the result and build RawData per columns from subdata entries.
            try {

                // We get the content of the XML in order to report it log messages.
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                executionSoap.getSOAPResponse().writeTo(out);
                xmlResponseString = new String(out.toByteArray());

                /**
                 * This Step will calculate hashTemp1 : Hash of List from the
                 * XML.
                 */
                // Will contain the nb of row of the target list of Hash.
                int finalnbRow = 0;
                // Will contain the result of the XML parsing.
                HashMap<String, List<String>> hashTemp1 = new HashMap<>();

                // Starting loop on subdata here.
                AnswerList answerData = new AnswerList();
                answerData = testDataLibDataService.readByVarious(lib.getTestDataLibID(), null, "N");
                List<TestDataLibData> objectDataList = new ArrayList<TestDataLibData>();
                objectDataList = answerData.getDataList();

                for (TestDataLibData libData : objectDataList) { // Loop on all SubData of the TestDataLib.

                    String subDataColumnToTreat = libData.getSubData();
                    String subDataParsingAnswer = libData.getParsingAnswer();
                    listTemp1 = new ArrayList<>();

                    try {

                        // We try to parse the XML with the subdata Parsing Answer.
                        NodeList candidates = XmlUtil.evaluate(xmlDocument, subDataParsingAnswer);

                        if (candidates.getLength() > 0) {

                            for (int i = 0; i < candidates.getLength(); i++) { // Loop on all Values that match in XML.

                                //We get the value from XML
                                String value = candidates.item(i).getNodeValue();

                                if (value == null) { // No value found.
                                    if (candidates.item(i) != null) {
                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_CHECK_XPATH);
                                        msg.setDescription(msg.getDescription()
                                                .replace("%XPATH%", subDataParsingAnswer)
                                                .replace("%SUBDATA%", subDataColumnToTreat)
                                                .replace("%ENTRY%", lib.getName())
                                                .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                                    } else {
                                        //no elements were returned by the XPATH expression
                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_XML_NOTFOUND);
                                        msg.setDescription(msg.getDescription()
                                                .replace("%XPATH%", subDataParsingAnswer)
                                                .replace("%SUBDATA%", subDataColumnToTreat)
                                                .replace("%ENTRY%", lib.getName())
                                                .replace("%XMLCONTENT%", xmlResponseString)
                                                .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                                        );
                                    }
                                } else { // Value were found we add it to the current list.

                                    listTemp1.add(value);

                                }
                            }

                            // Add the Subdata with associated list in the HashMap.
                            hashTemp1.put(subDataColumnToTreat, listTemp1);

                            // Getting the nb of row of the final result. (Max of all the Subdata retrieved from the XML)
                            if (listTemp1.size() > finalnbRow) {
                                finalnbRow = listTemp1.size();
                            }

                        } else {
                            //no elements were returned by the XPATH expression
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_XML_NOTFOUND);
                            msg.setDescription(msg.getDescription()
                                    .replace("%XPATH%", subDataParsingAnswer)
                                    .replace("%SUBDATA%", subDataColumnToTreat)
                                    .replace("%ENTRY%", lib.getName())
                                    .replace("%XMLCONTENT%", xmlResponseString)
                                    .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                            );
                        }
                    } catch (XmlUtilException ex) {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_XMLEXCEPTION);
                        msg.setDescription(msg.getDescription()
                                .replace("%XPATH%", subDataParsingAnswer)
                                .replace("%SUBDATA%", subDataColumnToTreat)
                                .replace("%ENTRY%", lib.getName())
                                .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                                .replace("%REASON%", ex.toString() + " Detail answer " + xmlResponseString));
                    }
                }

                /**
                 * This Step will convert hashTemp1 (Hash of List) to target
                 * listResult (list of Hash).
                 */
                if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()) {
                    for (int i = 0; i < finalnbRow; i++) { // Loop on all Values that match in XML.
                        resultHash = new HashMap<String, String>();
                        for (TestDataLibData libData : objectDataList) { // Loop on all SubData of the TestDataLib.
                            listTemp1 = hashTemp1.get(libData.getSubData());
                            if (listTemp1 != null) {
                                if (i < listTemp1.size()) {
                                    resultHash.put(libData.getSubData(), listTemp1.get(i));
                                } else {
                                    resultHash.put(libData.getSubData(), "");
                                }
                            }
                        }
                        listResult.add(resultHash);
                    }
                }

                /**
                 * This Step will pick the correct listResult (list of Hash)
                 * from the type of Property.
                 */
                if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()) {
                    if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_STATIC)) {
                        resultHash = listResult.get(0);
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SOAP_STATIC);

                    } else if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_RANDOM)) {
                        Random r = new Random();
                        int position = r.nextInt(listResult.size());
                        resultHash = listResult.get(position);
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SOAP_RANDOM);
                        msg.setDescription(msg.getDescription()
                                .replace("%POS%", Integer.toString(position))
                                .replace("%TOTALPOS%", Integer.toString(listResult.size())));

                    } else if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_RANDOMNEW)) {

                        int initNB = listResult.size();
                        // We get the list of values that are already used.
                        List<String> pastValues = this.testCaseExecutionDataDAO.getPastValuesOfProperty(tCExecution.getId(), testCaseCountryProperty.getProperty(), tCExecution.getTest(),
                                tCExecution.getTestCase(), tCExecution.getCountryEnvParam().getBuild(), tCExecution.getEnvironmentData(),
                                tCExecution.getCountry());

                        int removedNB = 0;
                        // We save all rows that needs to be removed to listToremove.
                        List<Map<String, String>> listToremove = new ArrayList<Map<String, String>>();
                        for (String valueToRemove : pastValues) {
                            for (Map<String, String> curentRow : listResult) {
                                if (curentRow.get("").equals(valueToRemove)) {
                                    if (true) {
                                        listToremove.add(curentRow);
                                        removedNB++;
                                    }
                                }
                            }
                        }
                        // We remove all listToremove entries from list.
                        listResult.removeAll(listToremove);

                        if (listResult != null && !listResult.isEmpty()) { // We pick a random value from the left entries of the list.
                            Random r = new Random();
                            int position = r.nextInt(listResult.size());
                            resultHash = listResult.get(position);
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SOAP_RANDOMNEW);
                            msg.setDescription(msg.getDescription()
                                    .replace("%TOTNB%", Integer.toString(initNB))
                                    .replace("%REMNB%", Integer.toString(removedNB))
                                    .replace("%POS%", Integer.toString(position))
                                    .replace("%TOTALPOS%", Integer.toString(listResult.size())));
                        } else { // No more entries available.
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_RANDOMNEW_NOMORERECORD);
                            msg.setDescription(msg.getDescription().replace("%TOTNB%", Integer.toString(initNB)));
                        }

                    } else if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_NOTINUSE)) {

                        int initNB = listResult.size();
                        // We get the list of values that are already used.
                        Integer peTimeout = Integer.valueOf(parameterService.findParameterByKey("cerberus_notinuse_timeout", system).getValue());
                        List<String> pastValues = this.testCaseExecutionDataDAO.getInUseValuesOfProperty(tCExecution.getId(), testCaseCountryProperty.getProperty(), tCExecution.getEnvironmentData(), tCExecution.getCountry(), peTimeout);

                        int removedNB = 0;
                        // We save all rows that needs to be removed to listToremove.
                        List<Map<String, String>> listToremove = new ArrayList<Map<String, String>>();
                        for (String valueToRemove : pastValues) {
                            for (Map<String, String> curentRow : listResult) {
                                if (curentRow.get("").equals(valueToRemove)) {
                                    if (true) {
                                        listToremove.add(curentRow);
                                        removedNB++;
                                    }
                                }
                            }
                        }
                        // We remove all listToremove entries from list.
                        listResult.removeAll(listToremove);

                        if (listResult != null && !listResult.isEmpty()) { // We pick a random value from the left entries of the list.
                            Random r = new Random();
                            int position = r.nextInt(listResult.size());
                            resultHash = listResult.get(position);
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SOAP_NOTINUSE);
                            msg.setDescription(msg.getDescription()
                                    .replace("%TOTNB%", Integer.toString(initNB))
                                    .replace("%REMNB%", Integer.toString(removedNB))
                                    .replace("%POS%", Integer.toString(position))
                                    .replace("%TOTALPOS%", Integer.toString(listResult.size())));
                        } else { // No more entries available.
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_NOTINUSE_NOMORERECORD);
                            msg.setDescription(msg.getDescription().replace("%TOTNB%", Integer.toString(initNB)));
                        }

                    }
                }

                // If the return is successfull, we convert the result to JSON and add it to the message.
                if (!listResult.isEmpty()) {
                    msg.setDescription(msg.getDescription().replace("%RESULTVALUE%", resultHash.toString()));
                }

                // Save the result to the Lib object.
                result = new TestDataLibResultSOAP();
                result.setDataLibRawData(resultHash);
                result.setTestDataLibID(lib.getTestDataLibID());

            } catch (Exception ex) {
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_XMLEXCEPTION);
                msg.setDescription(msg.getDescription()
                        .replace("%XPATH%", lib.getSubDataParsingAnswer())
                        .replace("%SUBDATA%", "")
                        .replace("%ENTRY%", lib.getName())
                        .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                        .replace("%REASON%", ex.toString()));
            }

        } else {
            String soapError = msg.getDescription();
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_SOAPCALLFAILED);
            msg.setDescription(msg.getDescription()
                    .replace("%SOAPERROR%", soapError)
                    .replace("%ENTRY%", lib.getName())
                    .replace("%ENTRYID%", lib.getTestDataLibID().toString()));

        }
        answer.setItem(result);
        msg.setDescription(msg.getDescription()
                .replace("%ERRORDETAILMESSAGE%", " Issue when trying to get data from Testdata library " + lib.getName() + " (" + lib.getTestDataLibID() + ")")
                .replace("%ENTRY%", lib.getName())
                .replace("%SERVICE%", servicePath)
                .replace("%OPERATION%", lib.getMethod())
                .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
        answer.setResultMessage(msg);
        return answer;
    }

    private AnswerItem<HashMap<String, String>> calculateOnStaticDataLibNColumns(String system, String country, String environment, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution) {
        AnswerItem answer = new AnswerItem();
        CountryEnvironmentDatabase countryEnvironmentDatabase;
        MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_STATIC_STATIC);
        List<HashMap<String, String>> list;
        int rowLimit = testCaseCountryProperty.getRowLimit();

        try {

            if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_STATIC)) { // If Nature of the property is static, we don't need to getch more than 1 record.
                rowLimit = 1;
            }
            //performs a query that returns several rows containing n columns
            AnswerList responseList = testDataLibService.readSTATICWithSubdataByCriteria(testCaseCountryProperty.getValue1(), system, country, environment, rowLimit, tCExecution.getApplication().getSystem());

            //if the query returns sucess then we can get the data
            if (responseList.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) {
                list = responseList.getDataList();
                if (list != null && !list.isEmpty()) {

                    if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_STATIC)) {
                        answer.setItem((list.get(0)));
                        mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_STATIC_STATIC);

                    } else if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_RANDOM)) {
                        Random r = new Random();
                        int position = r.nextInt(list.size());
                        answer.setItem(list.get(position));
                        mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_STATIC_RANDOM);
                        mes.setDescription(mes.getDescription().replace("%POS%", Integer.toString(position))
                                .replace("%ENTRYSELID%", list.get(position).get("TestDataLibID"))
                                .replace("%TOTALPOS%", Integer.toString(list.size())));

                    } else if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_RANDOMNEW)) {

                        int initNB = list.size();
                        // We get the list of values that are already used.
                        List<String> pastValues = this.testCaseExecutionDataDAO.getPastValuesOfProperty(tCExecution.getId(), testCaseCountryProperty.getProperty(), tCExecution.getTest(),
                                tCExecution.getTestCase(), tCExecution.getCountryEnvParam().getBuild(), tCExecution.getEnvironmentData(),
                                tCExecution.getCountry());

                        int removedNB = 0;
                        // We save all rows that needs to be removed to listToremove.
                        List<Map<String, String>> listToremove = new ArrayList<Map<String, String>>();
                        for (String valueToRemove : pastValues) {
                            for (Map<String, String> curentRow : list) {
                                if (curentRow.get("").equals(valueToRemove)) {
                                    if (true) {
                                        listToremove.add(curentRow);
                                        removedNB++;
                                    }
                                }
                            }
                        }
                        // We remove all listToremove entries from list.
                        list.removeAll(listToremove);

                        if (list != null && !list.isEmpty()) { // We pick a random value from the left entries of the list.
                            Random r = new Random();
                            int position = r.nextInt(list.size());
                            answer.setItem(list.get(position));
                            mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_STATIC_RANDOMNEW);
                            mes.setDescription(mes.getDescription().replace("%TOTNB%", Integer.toString(initNB))
                                    .replace("%REMNB%", Integer.toString(removedNB))
                                    .replace("%POS%", Integer.toString(position))
                                    .replace("%ENTRYSELID%", list.get(position).get("TestDataLibID"))
                                    .replace("%TOTALPOS%", Integer.toString(list.size())));
                        } else { // No more entries available.
                            mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_STATIC_RANDOMNEW_NOMORERECORD);
                            mes.setDescription(mes.getDescription().replace("%TOTNB%", Integer.toString(initNB)));
                        }

                    } else if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_NOTINUSE)) {

                        int initNB = list.size();
                        // We get the list of values that are already used.
                        Integer peTimeout = Integer.valueOf(parameterService.findParameterByKey("cerberus_notinuse_timeout", system).getValue());
                        List<String> pastValues = this.testCaseExecutionDataDAO.getInUseValuesOfProperty(tCExecution.getId(), testCaseCountryProperty.getProperty(), tCExecution.getEnvironmentData(), tCExecution.getCountry(), peTimeout);

                        int removedNB = 0;
                        // We save all rows that needs to be removed to listToremove.
                        List<Map<String, String>> listToremove = new ArrayList<Map<String, String>>();
                        for (String valueToRemove : pastValues) {
                            for (Map<String, String> curentRow : list) {
                                if (curentRow.get("").equals(valueToRemove)) {
                                    if (true) {
                                        listToremove.add(curentRow);
                                        removedNB++;
                                    }
                                }
                            }
                        }
                        // We remove all listToremove entries from list.
                        list.removeAll(listToremove);

                        if (list != null && !list.isEmpty()) { // We pick a random value from the left entries of the list.
                            Random r = new Random();
                            int position = r.nextInt(list.size());
                            answer.setItem(list.get(position));
                            mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_STATIC_NOTINUSE);
                            mes.setDescription(mes.getDescription().replace("%TOTNB%", Integer.toString(initNB))
                                    .replace("%REMNB%", Integer.toString(removedNB))
                                    .replace("%POS%", Integer.toString(position))
                                    .replace("%ENTRYSELID%", list.get(position).get("TestDataLibID"))
                                    .replace("%TOTALPOS%", Integer.toString(list.size())));
                        } else { // No more entries available.
                            mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_STATIC_NOTINUSE_NOMORERECORD);
                            mes.setDescription(mes.getDescription().replace("%TOTNB%", Integer.toString(initNB)));
                        }

                    }

                    // If the return is successfull, we convert the result to JSON and add it to the message.
                    if (!list.isEmpty()) {
                        mes.setDescription(mes.getDescription().replace("%RESULTVALUE%", answer.getItem().toString()));
                    }

                } else {
                    mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_STATIC_NODATA);
                }

            } else {
                mes = responseList.getResultMessage();
            }

        } catch (CerberusException ex) {
            mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_STATIC_GENERIC);
            mes.setDescription(mes.getDescription().replace("%SYSTEM%", system).replace("%COUNTRY%", country).replace("%ENV%", environment));
        }

        answer.setResultMessage(mes);
        return answer;
    }

}
