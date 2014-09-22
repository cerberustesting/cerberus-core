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
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.Property;
import org.cerberus.entity.SoapLibrary;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TestCaseExecutionData;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseExecutionData;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ISoapLibraryService;
import org.cerberus.service.ISqlLibraryService;
import org.cerberus.service.ITestCaseExecutionDataService;
import org.cerberus.service.ITestDataService;
import org.cerberus.service.impl.TestCaseCountryPropertiesService;
import org.cerberus.serviceEngine.IPropertyService;
import org.cerberus.serviceEngine.ISQLService;
import org.cerberus.serviceEngine.ISeleniumService;
import org.cerberus.serviceEngine.ISoapService;
import org.cerberus.serviceEngine.IXmlUnitService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.TestCaseExecutionDataUtil;
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
    private ISeleniumService seleniumService;
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
    private TestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;

    private TestCaseExecutionData calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty) {
        testCaseExecutionData.setStart(new Date().getTime());
        MessageEvent res;

        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();

        /**
         * Decode Property replacing properties encaplsulated with %
         */
        try {
            if (testCaseCountryProperty.getValue1().contains("%")) {
                String decodedValue = this.decodeValue(testCaseCountryProperty.getValue1(), testCaseStepActionExecution);
                testCaseExecutionData.setValue(decodedValue);
                testCaseExecutionData.setValue1(decodedValue);
                testCaseCountryProperty.setValue1(decodedValue);
            }
        } catch (CerberusEventException ex) {
            testCaseExecutionData.setEnd(new Date().getTime());
            testCaseExecutionData.setPropertyResultMessage(ex.getMessageError());
            return testCaseExecutionData;
        }

        try {
            if (testCaseCountryProperty.getValue2() != null && testCaseCountryProperty.getValue2().contains("%")) {
                String decodedValue = this.decodeValue(testCaseCountryProperty.getValue2(), testCaseStepActionExecution);
                testCaseExecutionData.setValue2(decodedValue);
                testCaseCountryProperty.setValue2(decodedValue);
            }
        } catch (CerberusEventException ex) {
            testCaseExecutionData.setEnd(new Date().getTime());
            testCaseExecutionData.setPropertyResultMessage(ex.getMessageError());
            return testCaseExecutionData;
        }

        /**
         * Calculate Property regarding the type
         */
        if (testCaseCountryProperty.getType().equals("executeSqlFromLib")) {
            testCaseExecutionData = this.executeSqlFromLib(testCaseExecutionData, testCaseCountryProperty, tCExecution);
        } else if (testCaseCountryProperty.getType().equals("executeSql")) {
            testCaseExecutionData = this.executeSql(testCaseExecutionData, testCaseCountryProperty, tCExecution);
        } else if (testCaseCountryProperty.getType().equals("text")) {
            testCaseExecutionData = this.calculateText(testCaseExecutionData, testCaseCountryProperty);
        } else if (testCaseCountryProperty.getType().equals("getFromHtmlVisible")) {
            testCaseExecutionData = this.getFromHtmlVIsible(testCaseExecutionData, tCExecution, testCaseCountryProperty);
        } else if (testCaseCountryProperty.getType().equals("getFromHtml")) {
            testCaseExecutionData = this.getFromHTML(testCaseExecutionData, tCExecution, testCaseCountryProperty);
        } else if (testCaseCountryProperty.getType().equals("getFromJS")) {
            testCaseExecutionData = this.getFromJS(testCaseExecutionData, tCExecution, testCaseCountryProperty);
        } else if (testCaseCountryProperty.getType().equals("getFromTestData")) {
            testCaseExecutionData = this.getFromTestData(testCaseExecutionData, tCExecution, testCaseCountryProperty);
        } else if (testCaseCountryProperty.getType().equals("getAttributeFromHtml")) {
            testCaseExecutionData = this.getAttributeFromHtml(testCaseExecutionData, tCExecution, testCaseCountryProperty);
        } else if (testCaseCountryProperty.getType().equals("getFromCookie")) {
            testCaseExecutionData = this.getFromCookie(testCaseExecutionData, tCExecution, testCaseCountryProperty);
        } else if (testCaseCountryProperty.getType().equals("getFromXml")) {
            testCaseExecutionData = this.getFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty);
        } else if ("executeSoapFromLib".equals(testCaseCountryProperty.getType())) {
            testCaseExecutionData = this.executeSoapFromLib(testCaseExecutionData, tCExecution, testCaseCountryProperty);
        } else if ("getDifferencesFromXml".equals(testCaseCountryProperty.getType())) {
            testCaseExecutionData = this.getDifferencesFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty);
        } else {
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_UNKNOWNPROPERTY);
            res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseCountryProperty.getType()));
        }

        testCaseExecutionData.setEnd(new Date().getTime());
        return testCaseExecutionData;
    }

    @Override
    public TestCaseExecutionData calculateProperty(String property, TestCaseStepActionExecution testCaseStepActionExecution) {
        //TO DELETE AFTER VALIDATION : return calculateProperty(property, new ArrayList<String>(), testCaseStepActionExecution);
        return getPropertiesAndPerformCalculation(property, testCaseStepActionExecution);
    }

    @Override
    public TestCaseExecutionData getPropertiesAndPerformCalculation(String property, TestCaseStepActionExecution testCaseStepActionExecution){
        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        String test = testCaseStepActionExecution.getTest();
        String testCase = testCaseStepActionExecution.getTestCase();
        String country = tCExecution.getCountry();
        long now = new Date().getTime();
        TestCaseExecutionData testCaseExecutionData = factoryTestCaseExecutionData.create(tCExecution.getId(), property, null, null, null, null, null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING));

        // Get the list of properties needed to calculate the required property
        List<TestCaseCountryProperties> propertiesToCalculate = null;
        try {
            propertiesToCalculate = getListOfPropertiesLinkedToProperty(test, testCase, country, property, new ArrayList<String>());
        } catch (CerberusEventException ex) {
            testCaseExecutionData.setPropertyResultMessage(ex.getMessageError());
            return testCaseExecutionData;
        }
        
        // Iterate on the properties needed and calculate it if necessary.
        for (TestCaseCountryProperties tccp : propertiesToCalculate) {
            now = new Date().getTime();
            TestCaseExecutionData tecd = factoryTestCaseExecutionData.create(tCExecution.getId(), tccp.getProperty(), null, tccp.getType(),
                    tccp.getValue1(), tccp.getValue2(), null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING));
            tecd.setTestCaseCountryProperties(tccp);
            // Check if property has already been calculated
            boolean isKnownData = false;
            for (TestCaseExecutionData knownData : tCExecution.getTestCaseExecutionDataList()) {
                if (knownData.getProperty().equalsIgnoreCase(tccp.getProperty())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Property " + property + " has already been calculated");
                    }
                    isKnownData = true;
                    tecd = knownData;
                    break;
                }
            }

            // If not already calculated, calculate it.
            if (!isKnownData) {
                // Really calculate our property
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Calculating property " + tecd.getProperty());
                }
                tecd = calculateProperty(tecd, testCaseStepActionExecution, tccp);
                LOG.warn("result of calculation " + tecd.toString());
                // Finally store result and return it
                try {
                    testCaseExecutionDataService.insertTestCaseExecutionData(tecd);
                    tCExecution.getTestCaseExecutionDataList().add(tecd);
                } catch (CerberusException cex) {
                    LOG.error(null, cex);
                }
            } 
            

            // If an error occurred then it is not necessary to continue calculation
            if (tecd.getPropertyResultMessage().getCode() % 100 != 0) {
                LOG.error("Error found");
                return tecd;

            }
            
            // Update testcaseexecution data if the property is the initial one.
            if (tccp.getProperty().equals(property)){
            testCaseExecutionData = tecd;
            }

        }
        return testCaseExecutionData;
    }

    @Override
    public List<TestCaseCountryProperties> getListOfPropertiesLinkedToProperty(String test, String testCase, String country, String property, List<String> crossedProperties) throws CerberusEventException {
        List<TestCaseCountryProperties> result = new ArrayList();
        // Check if property is not already known (recursive case)
        if (crossedProperties.contains(property)) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_CYCLICDEFINITION);
            msg.setDescription(msg.getDescription().replaceAll("%PROP%", property));
            throw new CerberusEventException(msg);
        }
        crossedProperties.add(property);
        
        TestCaseCountryProperties testCaseCountryProperty = null;
        try {
            testCaseCountryProperty = testCaseCountryPropertiesService.findTestCaseCountryPropertiesByKey(test, testCase, country, property);
        } catch (CerberusException cex) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION);
            msg.setDescription(msg.getDescription().replaceAll("%COUNTRY%", country));
            msg.setDescription(msg.getDescription().replaceAll("%PROP%", property));
            throw new CerberusEventException(msg);
        }

        // Check if property value1 contains internal properties
        for (String internalProperty : StringUtil.getAllProperties(testCaseCountryProperty.getValue1())) {
            // If the internal property is defined in the test case then we trigger a calculation
            boolean isADefinedProperty = false;
            List<TestCaseCountryProperties> definedProperties = testCaseCountryPropertiesService.findDistinctPropertiesOfTestCase(test, testCase);
            for (TestCaseCountryProperties definedProperty : definedProperties) {
                if (internalProperty.equals(definedProperty.getProperty())) {
                    isADefinedProperty = true;
                    break;
                }
            }

            // If it is not a defined property then we don't have to calculate it
            if (!isADefinedProperty) {
                continue;
            }
            result.addAll(getListOfPropertiesLinkedToProperty(test, testCase, country, internalProperty, crossedProperties));
            

        }
        result.add(testCaseCountryProperty);
        return result;
    }

//    TO DELETE AFTER VALIDATION : 
//    private TestCaseExecutionData calculateProperty(String property, List<String> crossedProperties, TestCaseStepActionExecution testCaseStepActionExecution) {
//        // Data initialisation
//        String test = testCaseStepActionExecution.getTest();
//        String testCase = testCaseStepActionExecution.getTestCase();
//        String country = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getCountry();
//        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
//        long now = new Date().getTime();
//        TestCaseExecutionData testCaseExecutionData = factoryTestCaseExecutionData.create(tCExecution.getId(), property, null, null, null, null, null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING));
//
//        // Check if property is not already known (recursive case)
//        if (crossedProperties.contains(property)) {
//            MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_CYCLICDEFINITION);
//            msg.setDescription(msg.getDescription().replaceAll("%PROP%", property));
//            testCaseExecutionData.setPropertyResultMessage(msg);
//            try {
//                testCaseExecutionDataService.insertTestCaseExecutionData(testCaseExecutionData);
//                tCExecution.getTestCaseExecutionDataList().add(testCaseExecutionData);
//            } catch (CerberusException cex) {
//                LOG.error(null, cex);
//            }
//            return testCaseExecutionData;
//        }
//        crossedProperties.add(property);
//
//        
//        // Check if property has already been calculated
//        for (TestCaseExecutionData knownData : tCExecution.getTestCaseExecutionDataList()) {
//            if (knownData.getProperty().equalsIgnoreCase(property)) {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("Property " + property + " has already been calculated");
//                }
//                return knownData;
//            }
//        }
//
//        // Start the calculation
//        // First, get property from database
//        TestCaseExecutionDataUtil.resetTimers(testCaseExecutionData, new Date().getTime());
//        TestCaseCountryProperties testCaseCountryProperty = null;
//        try {
//            testCaseCountryProperty = testCaseCountryPropertiesService.findTestCaseCountryPropertiesByKey(test, testCase, country, property);
//        } catch (CerberusException cex) {
//            MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION);
//            msg.setDescription(msg.getDescription().replaceAll("%COUNTRY%", country));
//            msg.setDescription(msg.getDescription().replaceAll("%PROP%", property));
//            testCaseExecutionData.setPropertyResultMessage(msg);
//            try {
//                testCaseExecutionDataService.insertTestCaseExecutionData(testCaseExecutionData);
//                tCExecution.getTestCaseExecutionDataList().add(testCaseExecutionData);
//            } catch (CerberusException cex2) {
//                LOG.error(null, cex2);
//            }
//            return testCaseExecutionData;
//        }
//
//        // Check if property value1 contains internal properties
//        for (String internalProperty : StringUtil.getAllProperties(testCaseCountryProperty.getValue1())) {
//            // If the internal property is defined in the test case then we trigger a calculation
//            boolean isADefinedProperty = false;
//            List<TestCaseCountryProperties> definedProperties = testCaseCountryPropertiesService.findDistinctPropertiesOfTestCase(test, testCase);
//            for (TestCaseCountryProperties definedProperty : definedProperties) {
//                if (internalProperty.equals(definedProperty.getProperty())) {
//                    isADefinedProperty = true;
//                    break;
//                }
//            }
//
//            // If it is not a defined property then we don't have to calculate it
//            if (!isADefinedProperty) {
//                continue;
//            }
//
//            // Calculate each of the internal properties recursively
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("Calculating internal property " + internalProperty);
//            }
//            TestCaseExecutionData internalTestCaseExecutionData = calculateProperty(internalProperty, crossedProperties, testCaseStepActionExecution);
//            // If an error occurred then it is not necessary to continue calculation
//            if (internalTestCaseExecutionData.getPropertyResultMessage().getCode() % 100 != 0) {
//                return internalTestCaseExecutionData;
//            }
//        }
//
//        // Feed our TestCaseExecutionData with data from database
//        testCaseExecutionData.setType(testCaseCountryProperty.getType());
//        testCaseExecutionData.setValue1(testCaseCountryProperty.getValue1());
//        testCaseExecutionData.setValue2(testCaseCountryProperty.getValue2());
//        testCaseExecutionData.setTestCaseCountryProperties(testCaseCountryProperty);
//
//        // Really calculate our property
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("Calculating property " + property);
//        }
//        testCaseExecutionData = calculateProperty(testCaseExecutionData, testCaseStepActionExecution, testCaseCountryProperty);
//
//        // Finally store result and return it
//        try {
//            testCaseExecutionDataService.insertTestCaseExecutionData(testCaseExecutionData);
//            tCExecution.getTestCaseExecutionDataList().add(testCaseExecutionData);
//        } catch (CerberusException cex) {
//            LOG.error(null, cex);
//        }
//        return testCaseExecutionData;
//    }

    @Override
    public String decodeValue(String myString, TestCaseStepActionExecution testCaseStepActionExecution) throws CerberusEventException {
        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        List<TestCaseExecutionData> properties = tCExecution.getTestCaseExecutionDataList();

        /**
         * Trying to replace by system environment variables .
         */
        myString = StringUtil.replaceAllProperties(myString, "%SYS_SYSTEM%", tCExecution.getApplication().getSystem());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_APPLI%", tCExecution.getApplication().getApplication());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_ENV%", tCExecution.getEnvironmentData());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_ENVGP%", tCExecution.getEnvironmentDataObj().getGp1());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_COUNTRY%", tCExecution.getCountry());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_COUNTRYGP1%", tCExecution.getCountryObj().getGp1());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_SSIP%", tCExecution.getSeleniumIP());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_SSPORT%", tCExecution.getSeleniumPort());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TAG%", tCExecution.getTag());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_EXECUTIONID%", String.valueOf(tCExecution.getId()));

        /**
         * Trying to replace date variables .
         */
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-yyyy%", DateUtil.getTodayFormat("yyyy"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-MM%", DateUtil.getTodayFormat("MM"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-dd%", DateUtil.getTodayFormat("dd"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-doy%", DateUtil.getTodayFormat("D"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-HH%", DateUtil.getTodayFormat("HH"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-mm%", DateUtil.getTodayFormat("mm"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-ss%", DateUtil.getTodayFormat("ss"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-yyyy%", DateUtil.getYesterdayFormat("yyyy"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-MM%", DateUtil.getYesterdayFormat("MM"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-dd%", DateUtil.getYesterdayFormat("dd"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-doy%", DateUtil.getYesterdayFormat("D"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-HH%", DateUtil.getYesterdayFormat("HH"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-mm%", DateUtil.getYesterdayFormat("mm"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-ss%", DateUtil.getYesterdayFormat("ss"));

        /**
         * Trying to replace by property value already defined if not null.
         */
        if (properties != null) {
            for (TestCaseExecutionData prop : properties) {
                myString = StringUtil.replaceAllProperties(myString, "%" + prop.getProperty() + "%", ParameterParserUtil.securePassword(prop.getValue(), prop.getProperty()));
            }
        }

        // Look at the internal properties. If there is no internal properties then return
        List<String> internalProperties = StringUtil.getAllProperties(myString);
        if (internalProperties.isEmpty()) {
            return myString;
        }

        // Else then try to calculate all those are defined into the current Test Case
        List<TestCaseCountryProperties> tcProperties = testCaseCountryPropertiesService.findDistinctPropertiesOfTestCase(testCaseStepActionExecution.getTest(), testCaseStepActionExecution.getTestCase());
        for (String internalProperty : internalProperties) {
            for (TestCaseCountryProperties tctProperty : tcProperties) {
                if (internalProperty.equals(tctProperty.getProperty())) {
                    TestCaseExecutionData internalData = getPropertiesAndPerformCalculation(internalProperty, testCaseStepActionExecution);
                    // If an error occurred, then throws an exception
                    if (internalData.getPropertyResultMessage().getCode() % 100 != 0) {
                        throw new CerberusEventException(internalData.getPropertyResultMessage());
                    }
                    myString = StringUtil.replaceAllProperties(myString, "%" + internalProperty + "%", internalData.getValue());
                }
            }
        }

        return myString;
    }

    private TestCaseExecutionData executeSqlFromLib(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution) {
        try {
            String script = this.sqlLibraryService.findSqlLibraryByKey(testCaseCountryProperty.getValue1()).getScript();
            testCaseExecutionData.setValue(script);
        } catch (CerberusException ex) {
            Logger.getLogger(PropertyService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_SQLLIB_NOTEXIT);
            res.setDescription(res.getDescription().replaceAll("%SQLLIB%", testCaseCountryProperty.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
            testCaseExecutionData.setEnd(new Date().getTime());
            return testCaseExecutionData;
        }
        testCaseExecutionData = this.executeSql(testCaseExecutionData, testCaseCountryProperty, tCExecution);
        return testCaseExecutionData;
    }

    private TestCaseExecutionData executeSql(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution) {
        return sQLService.calculateOnDatabase(testCaseExecutionData, testCaseCountryProperty, tCExecution);
    }

    private TestCaseExecutionData calculateText(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty) {
        if (Property.NATURE_RANDOM.equals(testCaseCountryProperty.getNature())
                //TODO CTE Voir avec B. Civel "RANDOM_NEW"
                || (testCaseCountryProperty.getNature().equals(Property.NATURE_RANDOMNEW))) {
            if (testCaseCountryProperty.getLength() == 0) {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TEXTRANDOMLENGHT0);
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                String charset;
                if (testCaseCountryProperty.getValue1() != null && !"".equals(testCaseCountryProperty.getValue1().trim())) {
                    charset = testCaseCountryProperty.getValue1();
                } else {
                    charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                }
                String value = StringUtil.getRandomString(testCaseCountryProperty.getLength(), charset);
                testCaseExecutionData.setValue(value);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_RANDOM);
                res.setDescription(res.getDescription().replaceAll("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
                testCaseExecutionData.setPropertyResultMessage(res);
//                    if (testCaseCountryProperty.getNature().equals("RANDOM_NEW")) {
//                        //TODO check if value exist on DB ( used in another test case of the revision )
//                    }
            }
        } else {
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, "Setting value : " + testCaseCountryProperty.getValue1());
            String value = testCaseCountryProperty.getValue1();
            testCaseExecutionData.setValue(value);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_TEXT);
            res.setDescription(res.getDescription().replaceAll("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromHTML(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
        try {
            String valueFromHTML = this.seleniumService.getValueFromHTML(tCExecution.getSelenium(), testCaseCountryProperty.getValue1());
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTML);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (NoSuchElementException exception) {
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ELEMENTDONOTEXIST);
            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromJS(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {

        String script = testCaseCountryProperty.getValue1();
        String valueFromJS;
        String message = "";
        try {
            valueFromJS = this.seleniumService.getValueFromJS(tCExecution.getSelenium(), script);
        } catch (Exception e) {
            message = e.getMessage().split("\n")[0];
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, "Exception Running JS Script :" + message);
            valueFromJS = null;
        }
        if (valueFromJS != null) {
            testCaseExecutionData.setValue(valueFromJS);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTML);
            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%VALUE%", script));
            testCaseExecutionData.setPropertyResultMessage(res);
        } else {
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_JS_EXCEPTION);
            res.setDescription(res.getDescription().replaceAll("%EXCEPTION%", message));
            testCaseExecutionData.setPropertyResultMessage(res);
        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromTestData(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
        String propertyValue = "";

        try {
            propertyValue = testCaseCountryProperty.getValue1();
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
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, "Exception Getting value from TestData for data :'" + propertyValue + "'\n" + exception.getMessageError().getDescription());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);
            res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseCountryProperty.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getAttributeFromHtml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
        MessageEvent res;
        try {
            String valueFromHTML = this.seleniumService.getAttributeFromHtml(tCExecution.getSelenium(), testCaseCountryProperty.getValue1(), testCaseCountryProperty.getValue2());
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETATTRIBUTEFROMHTML);
            } else {
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ATTRIBUTEDONOTEXIST);
            }
            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%ATTRIBUTE%", testCaseCountryProperty.getValue2()));
            res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
        } catch (NoSuchElementException exception) {
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, exception.toString());
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);
            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
        }
        testCaseExecutionData.setPropertyResultMessage(res);
        return testCaseExecutionData;
    }

    private TestCaseExecutionData executeSoapFromLib(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
        try {
            SoapLibrary soapLib = this.soapLibraryService.findSoapLibraryByKey(testCaseCountryProperty.getValue1());
            if (soapLib != null) {

                soapService.callSOAPAndStoreResponseInMemory(tCExecution.getExecutionUUID(), soapLib.getEnvelope(), soapLib.getServicePath(), soapLib.getMethod());
                String result = xmlUnitService.getFromXml(tCExecution.getExecutionUUID(), null, soapLib.getParsingAnswer());
                if (result != null) {
                    testCaseExecutionData.setValue(result);
                    testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SOAP));
                }
            }
        } catch (CerberusException exception) {
            MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);
            res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseCountryProperty.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromHtmlVIsible(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
        try {
            String valueFromHTML = this.seleniumService.getValueFromHTMLVisible(tCExecution.getSelenium(), testCaseCountryProperty.getValue1());
            if (valueFromHTML != null) {
                testCaseExecutionData.setValue(valueFromHTML);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTMLVISIBLE);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (NoSuchElementException exception) {
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);
            res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromXml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
        try {
            String valueFromXml = xmlUnitService.getFromXml(tCExecution.getExecutionUUID(), testCaseCountryProperty.getValue1(), testCaseCountryProperty.getValue2());
            if (valueFromXml != null) {
                testCaseExecutionData.setValue(valueFromXml);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMXML);
                res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseCountryProperty.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseCountryProperty.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception ex) {
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, ex.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMXML);
            res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseCountryProperty.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseCountryProperty.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData getFromCookie(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
        try {
            String valueFromCookie = this.seleniumService.getFromCookie(tCExecution.getSelenium(), testCaseCountryProperty.getValue1(), testCaseCountryProperty.getValue2());
            if (valueFromCookie != null) {
                if (!valueFromCookie.equals("cookieNotFound")) {
                    testCaseExecutionData.setValue(valueFromCookie);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMCOOKIE);
                    res.setDescription(res.getDescription().replaceAll("%COOKIE%", testCaseCountryProperty.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseCountryProperty.getValue2()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromCookie));
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_COOKIENOTFOUND);
                    res.setDescription(res.getDescription().replaceAll("%COOKIE%", testCaseCountryProperty.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseCountryProperty.getValue2()));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_PARAMETERNOTFOUND);
                res.setDescription(res.getDescription().replaceAll("%COOKIE%", testCaseCountryProperty.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseCountryProperty.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception exception) {
            MyLogger.log(PropertyService.class.getName(), Level.DEBUG, exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_COOKIENOTFOUND);
            res.setDescription(res.getDescription().replaceAll("%COOKIE%", testCaseCountryProperty.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%PARAM%", testCaseCountryProperty.getValue2()));
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
    private TestCaseExecutionData getDifferencesFromXml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
        try {
            MyLogger.log(PropertyService.class.getName(), Level.INFO, "Computing differences between " + testCaseCountryProperty.getValue1() + " and " + testCaseCountryProperty.getValue2());
            String differences = xmlUnitService.getDifferencesFromXml(testCaseCountryProperty.getValue1(), testCaseCountryProperty.getValue2());
            if (differences != null) {
                MyLogger.log(PropertyService.class.getName(), Level.INFO, "Computing done.");
                testCaseExecutionData.setValue(differences);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETDIFFERENCESFROMXML);
                res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseCountryProperty.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseCountryProperty.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                MyLogger.log(PropertyService.class.getName(), Level.INFO, "Computing failed.");
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETDIFFERENCESFROMXML);
                res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseCountryProperty.getValue1()));
                res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseCountryProperty.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception ex) {
            MyLogger.log(PropertyService.class.getName(), Level.INFO, ex.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETDIFFERENCESFROMXML);
            res.setDescription(res.getDescription().replaceAll("%VALUE1%", testCaseCountryProperty.getValue1()));
            res.setDescription(res.getDescription().replaceAll("%VALUE2%", testCaseCountryProperty.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

}
