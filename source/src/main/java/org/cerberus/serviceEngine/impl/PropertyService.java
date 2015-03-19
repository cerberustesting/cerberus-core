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
package org.cerberus.serviceEngine.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.Property;
import org.cerberus.entity.SoapLibrary;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TestCaseExecutionData;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.entity.TestCaseStepExecution;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseExecutionData;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ISoapLibraryService;
import org.cerberus.service.ISqlLibraryService;
import org.cerberus.service.ITestCaseExecutionDataService;
import org.cerberus.service.ITestDataService;
import org.cerberus.serviceEngine.IJsonService;
import org.cerberus.serviceEngine.IPropertyService;
import org.cerberus.serviceEngine.ISQLService;
import org.cerberus.serviceEngine.ISoapService;
import org.cerberus.serviceEngine.IWebDriverService;
import org.cerberus.serviceEngine.IXmlUnitService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
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
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private IJsonService jsonService;

    private String replaceWithCalculatedProperty(String stringToReplace, TestCaseExecution tCExecution) {
        for (TestCaseExecutionData tced : tCExecution.getTestCaseExecutionDataList()) {
            stringToReplace = StringUtil.replaceAllProperties(stringToReplace, "%" + tced.getProperty() + "%", tced.getValue());
        }
        return stringToReplace;
    }

    private void calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        testCaseExecutionData.setStart(new Date().getTime());
        MessageEvent res;

        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();

        /**
         * Decode Property replacing properties encaplsulated with %
         */
        if (testCaseCountryProperty.getValue1().contains("%")) {
            String decodedValue = decodeValue(testCaseCountryProperty.getValue1(), tCExecution);
            decodedValue = this.replaceWithCalculatedProperty(decodedValue, tCExecution);
            testCaseExecutionData.setValue(decodedValue);
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
        if (testCaseCountryProperty.getType().equals("executeSqlFromLib")) {
            testCaseExecutionData = this.executeSqlFromLib(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("executeSql")) {
            testCaseExecutionData = this.executeSql(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("text")) {
            testCaseExecutionData = this.calculateText(testCaseExecutionData, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("getFromHtmlVisible")) {
            testCaseExecutionData = this.getFromHtmlVIsible(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("getFromHtml")) {
            testCaseExecutionData = this.getFromHTML(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("getFromJS")) {
            testCaseExecutionData = this.getFromJS(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("getFromTestData")) {
            testCaseExecutionData = this.getFromTestData(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("getAttributeFromHtml")) {
            testCaseExecutionData = this.getAttributeFromHtml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("getFromCookie")) {
            testCaseExecutionData = this.getFromCookie(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("getFromXml")) {
            testCaseExecutionData = this.getFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if (testCaseCountryProperty.getType().equals("getFromJson")) {
            testCaseExecutionData = this.getFromJson(testCaseExecutionData, forceRecalculation);
        } else if ("executeSoapFromLib".equals(testCaseCountryProperty.getType())) {
            testCaseExecutionData = this.executeSoapFromLib(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else if ("getDifferencesFromXml".equals(testCaseCountryProperty.getType())) {
            testCaseExecutionData = this.getDifferencesFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
        } else {
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_UNKNOWNPROPERTY);
            res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseCountryProperty.getType()));
        }

        testCaseExecutionData.setEnd(new Date().getTime());
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

            /*  First check if property has already been calculated 
             *  if action is calculateProperty, then set isKnownData to false. 
             */
            List<TestCaseExecutionData> dataList = tCExecution.getTestCaseExecutionDataList();
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
            if (tecd.getPropertyResultMessage().equals(new MessageEvent(MessageEventEnum.PROPERTY_PENDING))) {
                calculateProperty(tecd, testCaseStepActionExecution, eachTccp, forceCalculation);
                try {
                    testCaseExecutionDataService.insertOrUpdateTestCaseExecutionData(tecd);

                } catch (CerberusException cex) {
                    LOG.error(cex.getMessage(), cex);
                }
            }
            /**
             * Add TestCaseExecutionData in TestCaseExecutionData List of the
             * TestCaseExecution
             */
            tCExecution.getTestCaseExecutionDataList().add(tecd);

            /**
             * After calculation, replace properties by value calculated
             */
            stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%" + eachTccp.getProperty() + "%", tecd.getValue());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + eachTccp.getProperty() + " calculated with Value = " + tecd.getValue() + ", Value1 = " + tecd.getValue1() + ", Value2 = " + tecd.getValue2());
            }
        }
        return stringToDecode;
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
        boolean propertyDefined = false;
        for (TestCaseCountryProperties tccp : propertieOfTestcase) {
            if (tccp.getProperty().equals(property)) {
                propertyDefined = true;
                if (tccp.getCountry().equals(country)) {
                    testCaseCountryProperty = tccp;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Property found :" + tccp);
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
        if (testCaseCountryProperty!= null) {
            List<String> allProperties = new ArrayList();
            allProperties.addAll(StringUtil.getAllProperties(testCaseCountryProperty.getValue1()));
            allProperties.addAll(StringUtil.getAllProperties(testCaseCountryProperty.getValue2()));

            for (String internalProperty : allProperties) {
                result.addAll(getListOfPropertiesLinkedToProperty(test, testCase, country, internalProperty, usedTest, usedTestCase, crossedProperties, propertieOfTestcase));
            }
            result.add(testCaseCountryProperty);
        }
        return result;
    }

    private String decodeValue(String stringToDecode, TestCaseExecution tCExecution) {
        /**
         * Trying to replace by system environment variables .
         */
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_SYSTEM%", tCExecution.getApplication().getSystem());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_APPLI%", tCExecution.getApplication().getApplication());
        stringToDecode = StringUtil.replaceAllProperties(stringToDecode, "%SYS_APP_DOMAIN%", tCExecution.getCountryEnvironmentApplication().getDomain());
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
            testCaseExecutionData.setValue(script);

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
            String valueFromHTML = this.webdriverService.getValueFromHTML(tCExecution.getSession(), testCaseExecutionData.getValue1());
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
            String valueFromHTML = this.webdriverService.getAttributeFromHtml(tCExecution.getSession(), testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETATTRIBUTEFROMHTML);
            } else {
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ATTRIBUTEDONOTEXIST);
            }
            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%ATTRIBUTE%", testCaseExecutionData.getValue2()));
            res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));

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
                String attachement = "";
                if (!testCaseExecutionData.getValue2().isEmpty()){
                    attachement = testCaseExecutionData.getValue2();
                }else{
                    attachement = soapLib.getAttachmentUrl();
                }
                soapService.callSOAPAndStoreResponseInMemory(tCExecution.getExecutionUUID(), soapLib.getEnvelope(), soapLib.getServicePath(), soapLib.getMethod(), attachement);
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
            String valueFromHTML = this.webdriverService.getValueFromHTMLVisible(tCExecution.getSession(), testCaseExecutionData.getValue1());
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
            if (LOG.isDebugEnabled()){
                LOG.error(exception.toString());
            }
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);

            res.setDescription(res.getDescription().replaceAll("%URL%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

}
