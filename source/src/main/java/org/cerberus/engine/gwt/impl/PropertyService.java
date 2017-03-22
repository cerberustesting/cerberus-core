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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionData;
import org.cerberus.crud.service.IAppServiceService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ISqlLibraryService;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.crud.service.ITestDataService;
import org.cerberus.engine.entity.Identifier;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.execution.IIdentifierService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.gwt.IPropertyService;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.datalib.IDataLibService;
import org.cerberus.service.groovy.IGroovyService;
import org.cerberus.service.json.IJsonService;
import org.cerberus.service.soap.ISoapService;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.service.webdriver.IWebDriverService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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
    private static final String MESSAGE_DEPRECATED = "[DEPRECATED]";

    @Autowired
    private IWebDriverService webdriverService;
    @Autowired
    private ISqlLibraryService sqlLibraryService;
    @Autowired
    private IAppServiceService appServiceService;
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
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
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
    private IDataLibService dataLibService;
    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private IVariableService variableService;

    public static final Pattern PROPERTY_VARIABLE_PATTERN = Pattern.compile("%[^%]+%");

    @Override
    public AnswerItem<String> decodeStringWithExistingProperties(String stringToDecode, TestCaseExecution tCExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {

        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        AnswerItem<String> answer = new AnswerItem();
        answer.setResultMessage(msg);
        answer.setItem(stringToDecode);

        String country = tCExecution.getCountry();
        long now = new Date().getTime();
        String stringToDecodeInit = stringToDecode;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to decode string : " + stringToDecode);
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
            answer.setItem(stringToDecode);
            answer.setResultMessage(msg);
            return answer;
        }

        /**
         * Get the list of properties needed to calculate the required property
         */
        List<TestCaseCountryProperties> tcProperties = tCExecution.getTestCaseCountryPropertyList();
        List<TestCaseCountryProperties> linkedProperties = new ArrayList();
        for (String internalProperty : internalPropertiesFromStringToDecode) { // Looping on potential properties in string to decode.
            List<TestCaseCountryProperties> newLinkedProperties = new ArrayList();
            newLinkedProperties = this.getListOfPropertiesLinkedToProperty(country, internalProperty, new ArrayList(), tcProperties);
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
            tecd = factoryTestCaseExecutionData.create(tCExecution.getId(), eachTccp.getProperty(), 1, eachTccp.getDescription(), null, eachTccp.getType(),
                    eachTccp.getValue1(), eachTccp.getValue2(), null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING),
                    eachTccp.getRetryNb(), eachTccp.getRetryPeriod(), eachTccp.getDatabase(), eachTccp.getValue1(), eachTccp.getValue2(), eachTccp.getLength(),
                    eachTccp.getRowLimit(), eachTccp.getNature());
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
            if (MessageEventEnum.PROPERTY_PENDING.equals(tecd.getPropertyResultMessage().getSource())) {
                calculateProperty(tecd, tCExecution, testCaseStepActionExecution, eachTccp, forceCalculation);
                msg = tecd.getPropertyResultMessage();
                //saves the result 
                try {
                    testCaseExecutionDataService.convert(testCaseExecutionDataService.save(tecd));
                    if (tecd.getDataLibRawData() != null) { // If the property is a TestDataLib, we same all rows retreived in order to support nature such as NOTINUSe or RANDOMNEW.
                        for (int i = 1; i < (tecd.getDataLibRawData().size()); i++) {
                            now = new Date().getTime();
                            TestCaseExecutionData tcedS = factoryTestCaseExecutionData.create(tecd.getId(), tecd.getProperty(), (i + 1),
                                    tecd.getDescription(), tecd.getDataLibRawData().get(i).get(""), tecd.getType(), "", "",
                                    tecd.getRC(), "", now, now, now, now, null, 0, 0, "", "", "", 0, 0, "");
                            testCaseExecutionDataService.convert(testCaseExecutionDataService.save(tcedS));
                        }
                    }
                } catch (CerberusException cex) {
                    LOG.error(cex.getMessage(), cex);
                }
            }

//            //if the property result message indicates that we need to stop the test action, then the action is notified               
//            //or if the property was not successfully calculated, either because it was not defined for the country or because it does not exist
//            //then we notify the execution
//            if (tecd.getPropertyResultMessage().getCodeString().equals("FA")
//                    || tecd.getPropertyResultMessage().getCodeString().equals("NA")) {
//                if (!(testCaseStepActionExecution == null)) {
//                    testCaseStepActionExecution.setStopExecution(tecd.isStopExecution());
//                    testCaseStepActionExecution.setActionResultMessage(tecd.getPropertyResultMessage());
//                    testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(tecd.getPropertyResultMessage().getMessage()));
//                }
//            }
            /**
             * Add TestCaseExecutionData in TestCaseExecutionData List of the
             * TestCaseExecution
             */
            LOG.debug("Adding into Execution data list. Property : " + eachTccp.getProperty() + " Value1 : " + eachTccp.getValue1() + " Value : " + tecd.getValue());
            tCExecution.getTestCaseExecutionDataList().add(tecd);
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

        answer.setResultMessage(msg);
        answer.setItem(stringToDecode);
        return answer;
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

    /**
     * Method that takes the potencial @param property, finds it (or not if it is not a existing property) inside the
     * existing property list @param propertiesOfTestcase and gets the list of all other properties required (contained inside value1 or value2).
     *
     * @param country country used to filter property from propertiesOfTestcase
     * @param property property to be calculated
     * @param crossedProperties List of previously found properties.
     * @param propertiesOfTestcase List of properties defined from the testcase.
     * @return list of TestCaseCountryProperties that are included inside the
     * definition of the @param property
     */
    private List<TestCaseCountryProperties> getListOfPropertiesLinkedToProperty(String country, String property, List<String> crossedProperties,
            List<TestCaseCountryProperties> propertiesOfTestcase) {
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
        AnswerItem ansSearch = findMatchingTestCaseCountryProperty(property, country, propertiesOfTestcase);
        testCaseCountryProperty = (TestCaseCountryProperties) ansSearch.getItem();

        if (testCaseCountryProperty == null) {
            //if the property does not exists, then a dummy property with the error message is defined and returned to the TC's execution
//            MessageEvent msg = ansSearch.getResultMessage();
//            TestCaseCountryProperties tccpToReturn = new TestCaseCountryProperties();
//            tccpToReturn.setProperty(property);
//            tccpToReturn.setType("");
//            tccpToReturn.setResult(msg);
//            result.add(tccpToReturn);
//
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("Property " + property + " not defined : " + msg.getDescription());
//            }
            return result;
//        } else {
        }

        /* 
         * Check if property value1 and value2 contains internal properties
         */
        List<String> allProperties = new ArrayList();

        // Value1 treatment
        List<String> propertiesValue1 = new ArrayList();
        //check the properties specified in the test
        for (String propSqlName : this.getPropertiesListFromString(testCaseCountryProperty.getValue1())) {
            for (TestCaseCountryProperties pr : propertiesOfTestcase) {
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
            for (TestCaseCountryProperties pr : propertiesOfTestcase) {
                if (pr.getProperty().equals(propSqlName)) {
                    propertiesValue2.add(propSqlName);
                    break;
                }
            }
        }
        allProperties.addAll(propertiesValue2);

        for (String internalProperty : allProperties) {
            result.addAll(getListOfPropertiesLinkedToProperty(country, internalProperty, crossedProperties, propertiesOfTestcase));
        }
        result.add(testCaseCountryProperty);

        return result;
    }

    private String decodeStringWithAlreadyCalculatedProperties(String stringToReplace, TestCaseExecution tCExecution) {
        String variableValue = "";
        String variableString1 = "";
        String variableString2 = "";

        for (TestCaseExecutionData tced : tCExecution.getTestCaseExecutionDataList()) {

            if ((tced.getType() != null) && (tced.getType().equals(TestCaseCountryProperties.TYPE_GETFROMDATALIB))) { // Type could be null in case property do not exist.
                /* Replacement in case of TestDataLib */

                // Key value of the DataLib.
                if (tced.getValue() != null) {
                    stringToReplace = stringToReplace.replace("%property." + tced.getProperty() + "%", tced.getValue());
                    stringToReplace = stringToReplace.replace("%" + tced.getProperty() + "%", tced.getValue());
                }

                // For each subdata of the getFromDataLib property, we try to replace with PROPERTY(SUBDATA).
                if (!(tced.getDataLibRawData() == null)) {
                    int ind = 0;
                    for (HashMap<String, String> dataRow : tced.getDataLibRawData()) { // We loop every row result.
                        for (String key : dataRow.keySet()) { // We loop every subdata
                            if (dataRow.get(key) != null) {
                                variableValue = dataRow.get(key);

                                variableString1 = tced.getProperty() + "(" + (ind + 1) + ")" + "(" + key + ")";
                                stringToReplace = stringToReplace.replace("%property." + variableString1 + "%", variableValue);
                                stringToReplace = stringToReplace.replace("%" + variableString1 + "%", variableValue);
                                variableString2 = tced.getProperty() + "." + (ind + 1) + "." + key;
                                stringToReplace = stringToReplace.replace("%property." + variableString2 + "%", variableValue);
                                stringToReplace = stringToReplace.replace("%" + variableString2 + "%", variableValue);

                                if (key.equals("")) { // If subdata is empty we can omit the () or .
                                    variableString1 = tced.getProperty() + "(" + (ind + 1) + ")";
                                    stringToReplace = stringToReplace.replace("%property." + variableString1 + "%", variableValue);
                                    stringToReplace = stringToReplace.replace("%" + variableString1 + "%", variableValue);
                                    variableString2 = tced.getProperty() + "." + (ind + 1);
                                    stringToReplace = stringToReplace.replace("%property." + variableString2 + "%", variableValue);
                                    stringToReplace = stringToReplace.replace("%" + variableString2 + "%", variableValue);
                                }

                                if (ind == 0) { // Dimention of the data is not mandatory for the 1st row.
                                    variableString1 = tced.getProperty() + "(" + key + ")";
                                    stringToReplace = stringToReplace.replace("%property." + variableString1 + "%", variableValue);
                                    stringToReplace = stringToReplace.replace("%" + variableString1 + "%", variableValue);
                                    variableString2 = tced.getProperty() + "." + key;
                                    stringToReplace = stringToReplace.replace("%property" + variableString2 + "%", variableValue);
                                    stringToReplace = stringToReplace.replace("%" + variableString2 + "%", variableValue);
                                }

                            }
                        }
                        ind++;
                    }
                }

            } else if (tced.getValue() != null) {
                /* Replacement in case of normal PROPERTY */
                stringToReplace = stringToReplace.replace("%property." + tced.getProperty() + "%", tced.getValue());
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
     * @param str the {@link String} to get all properties
     * @return a list of properties contained into the given {@link String}
     */
    private List<String> getPropertiesListFromString(String str) {
        List<String> properties = new ArrayList<String>();
        if (str == null) {
            return properties;
        }

//        Matcher propertyMatcher = PROPERTY_VARIABLE_PATTERN.matcher(str);
//        while (propertyMatcher.find()) {
//            String rawProperty = propertyMatcher.group();
//            // Removes the first and last '%' character to only get the property name
//            rawProperty = rawProperty.substring(1, rawProperty.length() - 1);
//            // Replace Property. if it exist and is in start
//            rawProperty = rawProperty.replaceFirst("^property\\.", "");
//            // Removes the variable part of the property eg : (subdata)
//            String[] ramProp1 = rawProperty.split("\\(");
//            // Removes the variable part of the property eg : .subdata
//            String[] ramProp2 = ramProp1[0].split("\\.");
//            properties.add(ramProp2[0]);
//        }
        String[] text1 = str.split("%");
        int i = 0;
        for (String rawProperty : text1) {
            LOG.debug("Potential property : " + rawProperty);
            if (((i > 0) || (str.startsWith("%"))) && ((i < (text1.length - 1)) || str.endsWith("%"))) { // First and last string from split is not to be considered.
                // Removes "property." string.
                rawProperty = rawProperty.replaceFirst("^property\\.", "");
                // Removes the variable part of the property eg : (subdata)
                String[] ramProp1 = rawProperty.split("\\(");
                // Removes the variable part of the property eg : .subdata
                String[] ramProp2 = ramProp1[0].split("\\.");
                if (!(StringUtil.isNullOrEmpty(ramProp2[0].trim())) // Avoid getting empty Property names.
                        && ramProp2[0].trim().length() <= TestCaseCountryProperties.MAX_PROPERTY_LENGTH // Properties cannot be bigger than n caracters.
                        && !ramProp2[0].trim().contains("\n")) { // Properties cannot contain \n.
                    properties.add(ramProp2[0].trim());
                    LOG.debug("getPropertiesListFromString TO " + ramProp2[0].trim());
                }
                // Avoid getting empty Property names.
            }
            i++;
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

    private void calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution,
            TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        testCaseExecutionData.setStart(new Date().getTime());
        MessageEvent res;
        String test = tCExecution.getTest();
        String testCase = tCExecution.getTestCase();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to calculate Property : '" + testCaseCountryProperty.getProperty() + "'");
        }

        /**
         * Decode Property replacing properties encapsulated with %
         */
        if (testCaseCountryProperty.getValue1().contains("%")) {
            String decodedValue = variableService.decodeStringWithSystemVariable(testCaseCountryProperty.getValue1(), tCExecution);
            decodedValue = this.decodeStringWithAlreadyCalculatedProperties(decodedValue, tCExecution);
            testCaseExecutionData.setValue1(decodedValue);
        }

        if (testCaseCountryProperty.getValue2() != null && testCaseCountryProperty.getValue2().contains("%")) {
            String decodedValue = variableService.decodeStringWithSystemVariable(testCaseCountryProperty.getValue2(), tCExecution);
            decodedValue = this.decodeStringWithAlreadyCalculatedProperties(decodedValue, tCExecution);
            testCaseExecutionData.setValue2(decodedValue);
        }

        int execution_count = 0;
        int retries = testCaseCountryProperty.getRetryNb();
        int periodms = testCaseCountryProperty.getRetryPeriod();
        LOG.debug("Init Retries : " + retries + " Period : " + periodms);

        /**
         * Controling that retrynb and retryperiod are correctly feeded. <br>
         * This is to avoid that <br>
         * 1/ retry is greater than cerberus_property_maxretry <br>
         * 2/ total duration of property calculation is longuer than
         * cerberus_property_maxretrytotalduration
         */
        boolean forced_retry = false;
        String forced_retry_message = "";
        if (!(retries == 0)) {
            int maxretry = parameterService.getParameterIntegerByKey("cerberus_property_maxretry", "", 50);
            if (retries > maxretry) {
                retries = maxretry;
                forced_retry = true;
            }
            int maxtotalduration = parameterService.getParameterIntegerByKey("cerberus_property_maxretrytotalduration", "", 1800000);
            if (periodms > maxtotalduration) {
                periodms = maxtotalduration;
                forced_retry = true;
            }
            if (retries * periodms > maxtotalduration) {
                retries = (int) maxtotalduration / periodms;
                forced_retry = true;
            }
            if (forced_retry) {
                forced_retry_message = "WARNING : Forced Retries : " + testCaseCountryProperty.getRetryNb() + "-->" + retries + " and Period : " + testCaseCountryProperty.getRetryPeriod() + "-->" + periodms + " (in order to respect the constrains cerberus_property_maxretry " + maxretry + " & cerberus_property_maxtotalduration " + maxtotalduration + ")";
                LOG.debug("Forced Retries : " + retries + " Period : " + periodms + " in order to respect the constrains cerberus_property_maxretry " + maxretry + " & cerberus_property_maxtotalduration " + maxtotalduration);
            }

        }

        /**
         * Looping on calculating the action until result is OK or reach the max
         * retry.
         */
        while (execution_count <= retries && !(testCaseExecutionData.getPropertyResultMessage().getCodeString().equals("OK"))) {
            LOG.debug("Attempt #" + execution_count + " " + testCaseCountryProperty.getProperty() + " " + testCaseCountryProperty.getValue1());

            if (execution_count >= 1) { // We only wait the period if not on the very first calculation.
                try {
                    Thread.sleep(periodms);
                    LOG.debug("Attempt #" + execution_count + " " + testCaseCountryProperty.getProperty() + " " + testCaseCountryProperty.getValue1() + " Waiting " + periodms + " ms");
                } catch (InterruptedException ex) {
                    LOG.error(ex.toString());
                }
            }

            /**
             * Calculate Property regarding the type
             */
            switch (testCaseCountryProperty.getType()) {
                case TestCaseCountryProperties.TYPE_TEXT:
                    testCaseExecutionData = this.property_calculateText(testCaseExecutionData, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETFROMDATALIB:
                    testCaseExecutionData = this.property_getFromDataLib(testCaseExecutionData, tCExecution, testCaseStepActionExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_EXECUTESQL:
                    testCaseExecutionData = this.property_executeSql(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETFROMHTML:
                    testCaseExecutionData = this.property_getFromHtml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETFROMHTMLVISIBLE:
                    testCaseExecutionData = this.property_getFromHtmlVisible(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETFROMJS:
                    testCaseExecutionData = this.property_getFromJS(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETATTRIBUTEFROMHTML:
                    testCaseExecutionData = this.property_getAttributeFromHtml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETFROMCOOKIE:
                    testCaseExecutionData = this.property_getFromCookie(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETFROMXML:
                    testCaseExecutionData = this.property_getFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETDIFFERENCESFROMXML:
                    testCaseExecutionData = this.property_getDifferencesFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETFROMJSON:
                    testCaseExecutionData = this.property_getFromJson(testCaseExecutionData, tCExecution, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_GETFROMGROOVY:
                    testCaseExecutionData = this.property_getFromGroovy(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    break;

                case TestCaseCountryProperties.TYPE_EXECUTESOAPFROMLIB: // DEPRECATED
                    testCaseExecutionData = this.property_executeSoapFromLib(testCaseExecutionData, tCExecution, testCaseStepActionExecution, testCaseCountryProperty, forceRecalculation);
                    res = testCaseExecutionData.getPropertyResultMessage();
                    res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
                    testCaseExecutionData.setPropertyResultMessage(res);
                    logEventService.createForPrivateCalls("ENGINE", TestCaseCountryProperties.TYPE_EXECUTESOAPFROMLIB, MESSAGE_DEPRECATED + " Deprecated Property triggered by TestCase : ['" + test + "|" + testCase + "']");
                    LOG.warn(MESSAGE_DEPRECATED + " Deprecated Property " + TestCaseCountryProperties.TYPE_EXECUTESOAPFROMLIB + " triggered by TestCase : ['" + test + "'|'" + testCase + "']");
                    break;

                case TestCaseCountryProperties.TYPE_EXECUTESQLFROMLIB: // DEPRECATED
                    testCaseExecutionData = this.property_executeSqlFromLib(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceRecalculation);
                    res = testCaseExecutionData.getPropertyResultMessage();
                    res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
                    testCaseExecutionData.setPropertyResultMessage(res);
                    logEventService.createForPrivateCalls("ENGINE", TestCaseCountryProperties.TYPE_EXECUTESQLFROMLIB, MESSAGE_DEPRECATED + " Deprecated Property triggered by TestCase : ['" + test + "|" + testCase + "']");
                    LOG.warn(MESSAGE_DEPRECATED + " Deprecated Property " + TestCaseCountryProperties.TYPE_EXECUTESQLFROMLIB + " triggered by TestCase : ['" + test + "'|'" + testCase + "']");
                    break;

                case TestCaseCountryProperties.TYPE_GETFROMTESTDATA: // DEPRECATED
                    testCaseExecutionData = this.property_getFromTestData(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                    res = testCaseExecutionData.getPropertyResultMessage();
                    res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
                    testCaseExecutionData.setPropertyResultMessage(res);
                    logEventService.createForPrivateCalls("ENGINE", TestCaseCountryProperties.TYPE_GETFROMTESTDATA, MESSAGE_DEPRECATED + " Deprecated Property triggered by TestCase : ['" + test + "|" + testCase + "']");
                    LOG.warn(MESSAGE_DEPRECATED + " Deprecated Property " + TestCaseCountryProperties.TYPE_GETFROMTESTDATA + " triggered by TestCase : ['" + test + "'|'" + testCase + "']");
                    break;

                default:
                    res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_UNKNOWNPROPERTY);
                    res.setDescription(res.getDescription().replace("%PROPERTY%", testCaseCountryProperty.getType()));
                    testCaseExecutionData.setPropertyResultMessage(res);
            }
            execution_count++;

        }

        if (execution_count >= 2) { // If there were at least 1 retry, we notify it in the result message.
            res = testCaseExecutionData.getPropertyResultMessage();
            res.setDescription("Retried " + (execution_count - 1) + " time(s) with " + periodms + "ms period - " + res.getDescription());
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        if (forced_retry) { // If the retry and period parameter was changed, we notify it in the result message.
            res = testCaseExecutionData.getPropertyResultMessage();
            res.setDescription(forced_retry_message + " - " + res.getDescription());
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
        testCaseExecutionData = this.property_executeSql(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceCalculation);
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_executeSql(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        return sQLService.calculateOnDatabase(testCaseExecutionData, testCaseCountryProperty, tCExecution);
    }

    private TestCaseExecutionData property_calculateText(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        if (TestCaseCountryProperties.NATURE_RANDOM.equals(testCaseCountryProperty.getNature())
                //TODO CTE Voir avec B. Civel "RANDOM_NEW"
                || (testCaseCountryProperty.getNature().equals(TestCaseCountryProperties.NATURE_RANDOMNEW))) {
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
            LOG.debug("Setting value : " + testCaseExecutionData.getValue1());
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
            LOG.debug(exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);

            res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromHtml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        if (tCExecution.getApplicationObj().getType().equals(Application.TYPE_APK)
                || tCExecution.getApplicationObj().getType().equals(Application.TYPE_IPA)
                || tCExecution.getApplicationObj().getType().equals(Application.TYPE_GUI)) {

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
                LOG.debug(exception.toString());
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ELEMENTDONOTEXIST);
                res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }

        } else {

            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_FEATURENOTSUPPORTED);
            res.setDescription(res.getDescription().replace("%APPTYPE%", tCExecution.getApplicationObj().getType()));
            res.setDescription(res.getDescription().replace("%PROPTYPE%", testCaseExecutionData.getType()));

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
            LOG.debug("Exception Running JS Script :" + message);
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
            LOG.debug("Exception Running Grrovy Script :" + e.getMessage());
            testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMGROOVY_EXCEPTION).resolveDescription("REASON", e.getMessage()));
        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromTestData(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        String propertyValue = "";

        try {
            propertyValue = testCaseExecutionData.getValue1();
            String valueFromTestData = testDataService.findTestDataByKey(propertyValue, tCExecution.getApplicationObj().getApplication(),
                    tCExecution.getEnvironmentData(), tCExecution.getCountry()).getValue();
            if (valueFromTestData != null) {
                testCaseExecutionData.setValue(valueFromTestData);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_TESTDATA);
                res.setDescription(res.getDescription().replace("%PROPERTY%", propertyValue));
                res.setDescription(res.getDescription().replace("%VALUE%", valueFromTestData));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (CerberusException exception) {
            LOG.debug("Exception Getting value from TestData for data :'" + propertyValue + "'\n" + exception.getMessageError().getDescription());
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
            LOG.debug(exception.toString());
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);

            res.setDescription(res.getDescription().replace("%ELEMENT%", testCaseExecutionData.getValue1()));
        }
        testCaseExecutionData.setPropertyResultMessage(res);
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_executeSoapFromLib(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        String result = null;
        try {
            AppService appService = this.appServiceService.findAppServiceByKey(testCaseExecutionData.getValue1());
            if (appService != null) {
                String attachement = "";//TODO implement this feature
                //TODO implement the executeSoapFromLib
                /*if (!testCaseExecutionData.getValue2().isEmpty()){
                 attachement = testCaseExecutionData.getValue2();
                 }else{
                 attachement = soapLib.getAttachmentUrl();
                 }*/
                String decodedEnveloppe = appService.getServiceRequest();
                String decodedServicePath = appService.getServicePath();
                String decodedMethod = appService.getOperation();

                if (appService.getServiceRequest().contains("%")) {
                    decodedEnveloppe = variableService.decodeStringCompletly(appService.getServiceRequest(), tCExecution, testCaseStepActionExecution, false);
                }
                if (appService.getServicePath().contains("%")) {
                    decodedServicePath = variableService.decodeStringCompletly(appService.getServicePath(), tCExecution, testCaseStepActionExecution, false);
                }
                if (appService.getOperation().contains("%")) {
                    decodedMethod = variableService.decodeStringCompletly(appService.getOperation(), tCExecution, testCaseStepActionExecution, false);
                }

                //Call Soap and set LastSoapCall of the testCaseExecution.
                AnswerItem soapCall = soapService.callSOAP(decodedEnveloppe, decodedServicePath, decodedMethod, attachement, null, null, 60000);
                AppService se1 = (AppService) soapCall.getItem();
//                tCExecution.setLastSOAPCalled(soapCall);

                if (soapCall.isCodeEquals(200)) {
//                    SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
                    String xmlResponse = se1.getResponseHTTPBody();
                    result = xmlUnitService.getFromXml(xmlResponse, appService.getParsingAnswer());
                }
                if (result != null) {
                    testCaseExecutionData.setValue(result);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SOAP);
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SOAPFROMLIB_NODATA);
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            }
        } catch (CerberusException exception) {
            LOG.error(exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);

            res.setDescription(res.getDescription().replace("%PROPERTY%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        } catch (CerberusEventException ex) {
            LOG.error(ex.toString());
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
            message.setDescription(message.getDescription().replace("%SOAPNAME%", testCaseExecutionData.getValue1()));
            message.setDescription(message.getDescription().replace("%DESCRIPTION%", ex.getMessageError().getDescription()));
            testCaseExecutionData.setPropertyResultMessage(message);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromXml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        // 1. Get XML value to parse

        String xmlToParse = null;
        // If value2 is defined, then take it as XML value to parse
        if (!(StringUtil.isNullOrEmpty(testCaseExecutionData.getValue2()))) {
            xmlToParse = testCaseExecutionData.getValue2();
        } // Else try to get the last known response from service call
        else if (tCExecution.getLastServiceCalled() != null) {
            xmlToParse = tCExecution.getLastServiceCalled().getResponseHTTPBody();
        } // If XML to parse is still null, then there is an error in XML value definition
        else if (xmlToParse == null) {
            testCaseExecutionData.setPropertyResultMessage(
                    new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMXML)
                    .resolveDescription("VALUE1", testCaseExecutionData.getValue1())
                    .resolveDescription("VALUE2", testCaseExecutionData.getValue2()));
            return testCaseExecutionData;
        }
        // Else we can try to parse it thanks to the dedicated service

        try {
            String valueFromXml = xmlUnitService.getFromXml(xmlToParse, testCaseExecutionData.getValue1());
            if (valueFromXml != null) {
                testCaseExecutionData.setValue(valueFromXml);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMXML);
                res.setDescription(res.getDescription().replace("%VALUE%", valueFromXml));
                res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMXML);
                res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception ex) {
            LOG.debug(ex.toString());
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
            LOG.debug(exception.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOOKIE_COOKIENOTFOUND);

            res.setDescription(res.getDescription().replace("%COOKIE%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;

    }

    private TestCaseExecutionData property_getDifferencesFromXml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        try {
            LOG.debug("Computing differences between " + testCaseExecutionData.getValue1() + " and " + testCaseExecutionData.getValue2());
            String differences = xmlUnitService.getDifferencesFromXml(testCaseExecutionData.getValue1(), testCaseExecutionData.getValue2());
            if (differences != null) {
                LOG.debug("Computing done.");
                testCaseExecutionData.setValue(differences);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETDIFFERENCESFROMXML);
                res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                LOG.debug("Computing failed.");
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETDIFFERENCESFROMXML);
                res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
                res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception ex) {
            LOG.debug(ex.toString());
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETDIFFERENCESFROMXML);

            res.setDescription(res.getDescription().replace("%VALUE1%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%VALUE2%", testCaseExecutionData.getValue2()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromJson(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, boolean forceRecalculation) {
        String jsonResponse = "";
        try {
            /**
             * If tCExecution LastServiceCalled exist, get the response;
             */
            if (null != tCExecution.getLastServiceCalled()) {
                jsonResponse = tCExecution.getLastServiceCalled().getResponseHTTPBody();
            }
            String newUrl = null;
            if (!(StringUtil.isNullOrEmpty(testCaseExecutionData.getValue2()))) {
                newUrl = testCaseExecutionData.getValue2();
            }

            String valueFromJson = this.jsonService.getFromJson(jsonResponse, newUrl, testCaseExecutionData.getValue1());
            if (valueFromJson != null) {
                if (!"".equals(valueFromJson)) {
                    testCaseExecutionData.setValue(valueFromJson);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMJSON);
                    res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
                    res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
                    res.setDescription(res.getDescription().replace("%VALUE%", valueFromJson));
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
                    res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
                    res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
                res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
                res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (Exception exception) {
            if (LOG.isDebugEnabled()) {
                LOG.error(exception.toString());
            }
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);

            res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromDataLib(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {

        MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB);

        TestDataLib testDataLib;
        List<HashMap<String, String>> result = null;

        // We get here the correct TestDataLib entry from the Value1 (name) that better match the context on system, environment and country.
        AnswerItem<TestDataLib> answer = testDataLibService.readByNameBySystemByEnvironmentByCountry(testCaseExecutionData.getValue1(),
                tCExecution.getApplicationObj().getSystem(), tCExecution.getEnvironmentData(),
                tCExecution.getCountry());

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answer.getItem() != null) {
            testDataLib = (TestDataLib) answer.getItem();

            AnswerList serviceAnswer;

            //check if there are properties defined in the data specification
            try {
                if (testDataLib.getType().equals(TestDataLib.TYPE_SERVICE)) {
                    //check if the servicepath contains properties that neeed to be calculated
                    String decodedServicePath = variableService.decodeStringCompletly(testDataLib.getServicePath(), tCExecution, testCaseStepActionExecution, false);
                    testDataLib.setServicePath(decodedServicePath);
                    //check if the method contains properties that neeed to be calculated
                    String decodedMethod = variableService.decodeStringCompletly(testDataLib.getMethod(), tCExecution, testCaseStepActionExecution, false);
                    testDataLib.setMethod(decodedMethod);
                    //check if the envelope contains properties that neeed to be calculated
                    String decodedEnvelope = variableService.decodeStringCompletly(testDataLib.getEnvelope(), tCExecution, testCaseStepActionExecution, false);
                    testDataLib.setEnvelope(decodedEnvelope);

                } else if (testDataLib.getType().equals(TestDataLib.TYPE_SQL)) {
                    //check if the script contains properties that neeed to be calculated
                    String decodedScript = variableService.decodeStringCompletly(testDataLib.getScript(), tCExecution, testCaseStepActionExecution, false);
                    testDataLib.setScript(decodedScript);

                }
            } catch (CerberusEventException cex) {
                LOG.error(cex.toString());
            }

            //we need to recalculate the result for the lib
            serviceAnswer = dataLibService.getFromDataLib(testDataLib, testCaseCountryProperty, tCExecution, testCaseExecutionData);

            res = serviceAnswer.getResultMessage();
            result = (List<HashMap<String, String>>) serviceAnswer.getDataList(); //test data library returned by the service

//            }
            if (result != null) {
                // Keeping raw data to testCaseExecutionData object.
                testCaseExecutionData.setDataLibRawData(result);

                // Value of testCaseExecutionData object takes the master subdata entry "".
                String value = (String) result.get(0).get("");
                testCaseExecutionData.setValue(value);

                //Record result in filessytem.
                recorderService.recordTestDataLibProperty(tCExecution.getId(), testCaseCountryProperty.getProperty(), 1, result);

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

}
