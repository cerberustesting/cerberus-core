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
package org.cerberus.engine.gwt.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.cerberus.engine.entity.Identifier;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.execution.IIdentifierService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.gwt.IPropertyService;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.appium.impl.AndroidAppiumService;
import org.cerberus.service.appium.impl.IOSAppiumService;
import org.cerberus.service.datalib.IDataLibService;
import org.cerberus.service.executor.IExecutorService;
import org.cerberus.service.groovy.IGroovyService;
import org.cerberus.service.har.IHarService;
import org.cerberus.service.json.IJsonService;
import org.cerberus.service.soap.ISoapService;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.service.webdriver.IWebDriverService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    private static final Logger LOG = LogManager.getLogger(PropertyService.class);
    private static final String MESSAGE_DEPRECATED = "[DEPRECATED]";
    private static final String VALUE_NULL = "<NULL>";

    @Autowired
    private IWebDriverService webdriverService;
    @Autowired
    private ISqlLibraryService sqlLibraryService;
    @Autowired
    private IAppServiceService appServiceService;
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
    @Autowired
    private AndroidAppiumService androidAppiumService;
    @Autowired
    private IOSAppiumService iosAppiumService;
    @Autowired
    private IExecutorService executorService;
    @Autowired
    private IHarService harService;

    @Override
    public AnswerItem<String> decodeStringWithExistingProperties(String stringToDecode, TestCaseExecution tCExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {

        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        AnswerItem<String> answer = new AnswerItem<>();
        answer.setResultMessage(msg);
        answer.setItem(stringToDecode);

        String country = tCExecution.getCountry();
        long now = new Date().getTime();
        String stringToDecodeInit = stringToDecode;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to decode string (Property) : " + stringToDecode);
        }

        /**
         * We start to decode properties from available executiondata List.
         */
        stringToDecode = decodeStringWithAlreadyCalculatedProperties(stringToDecode, tCExecution);

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
        List<TestCaseCountryProperties> linkedProperties = new ArrayList<>();
        for (String internalProperty : internalPropertiesFromStringToDecode) { // Looping on potential properties in string to decode.
            List<TestCaseCountryProperties> newLinkedProperties = new ArrayList<>();
            newLinkedProperties = this.getListOfPropertiesLinkedToProperty(country, internalProperty, new ArrayList<>(), tcProperties);
            linkedProperties.addAll(newLinkedProperties);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + internalProperty + " need calculation of these (" + newLinkedProperties.size() + ") property(ies) " + newLinkedProperties);
            }
        }

        /**
         * For all linked properties, calculate it if needed.
         */
        for (TestCaseCountryProperties eachTccp : linkedProperties) {
            TestCaseExecutionData tcExeData;
            /**
             * First create testCaseExecutionData object
             */
            now = new Date().getTime();
            tcExeData = factoryTestCaseExecutionData.create(tCExecution.getId(), eachTccp.getProperty(), 1, eachTccp.getDescription(), null, eachTccp.getType(), eachTccp.getRank(),
                    eachTccp.getValue1(), eachTccp.getValue2(), null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING),
                    eachTccp.getRetryNb(), eachTccp.getRetryPeriod(), eachTccp.getDatabase(), eachTccp.getValue1(), eachTccp.getValue2(), eachTccp.getLength(),
                    eachTccp.getLength(), eachTccp.getRowLimit(), eachTccp.getNature(), tCExecution.getApplicationObj().getSystem(), tCExecution.getEnvironment(), tCExecution.getCountry(), "", null, "N");
            tcExeData.setTestCaseCountryProperties(eachTccp);
            tcExeData.settCExecution(tCExecution);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Trying to calculate Property : '" + tcExeData.getProperty() + "' " + tcExeData);
            }

            /*  First check if property has already been calculated
             *  if action is calculateProperty, then set isKnownData to false.
             */
            tcExeData = getExecutionDataFromList(tCExecution.getTestCaseExecutionDataMap(), eachTccp, forceCalculation, tcExeData);

            /**
             * If testcasecountryproperty not defined, set ExecutionData with
             * the same resultMessage
             */
            if (eachTccp.getResult() != null) {
                tcExeData.setPropertyResultMessage(eachTccp.getResult());
            }
            /*
             * If not already calculated, or calculateProperty, then calculate it and insert or update it.
             */
            if (MessageEventEnum.PROPERTY_PENDING.equals(tcExeData.getPropertyResultMessage().getSource())) {
                calculateProperty(tcExeData, tCExecution, testCaseStepActionExecution, eachTccp, forceCalculation);
                msg = tcExeData.getPropertyResultMessage();
                //saves the result
                try {
                    testCaseExecutionDataService.save(tcExeData);
                    /**
                     * Add TestCaseExecutionData in TestCaseExecutionData List
                     * of the TestCaseExecution
                     */
                    LOG.debug("Adding into Execution data list. Property : '" + tcExeData.getProperty() + "' Index : '" + String.valueOf(tcExeData.getIndex()) + "' Value : '" + tcExeData.getValue() + "'");
                    tCExecution.getTestCaseExecutionDataMap().put(tcExeData.getProperty(), tcExeData);
                    if (tcExeData.getDataLibRawData() != null) { // If the property is a TestDataLib, we same all rows retreived in order to support nature such as NOTINUSe or RANDOMNEW.
                        for (int i = 1; i < (tcExeData.getDataLibRawData().size()); i++) {
                            now = new Date().getTime();
                            TestCaseExecutionData tcedS = factoryTestCaseExecutionData.create(tcExeData.getId(), tcExeData.getProperty(), (i + 1),
                                    tcExeData.getDescription(), tcExeData.getDataLibRawData().get(i).get(""), tcExeData.getType(), tcExeData.getRank(), "", "",
                                    tcExeData.getRC(), "", now, now, now, now, null, 0, 0, "", "", "", "", "", 0, "", tcExeData.getSystem(), tcExeData.getEnvironment(), tcExeData.getCountry(), tcExeData.getDataLib(), null, "N");
                            testCaseExecutionDataService.save(tcedS);
                        }
                    }
                } catch (CerberusException cex) {
                    LOG.error(cex.getMessage(), cex);
                }
            }

            /**
             * After calculation, replace properties by value calculated
             */
            stringToDecode = decodeStringWithAlreadyCalculatedProperties(stringToDecode, tCExecution);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + eachTccp.getProperty() + " calculated with Value = " + tcExeData.getValue() + ", Value1 = " + tcExeData.getValue1() + ", Value2 = " + tcExeData.getValue2());
            }
            /**
             * Log TestCaseExecutionData
             */
            if (tCExecution.getVerbose() > 0) {
                LOG.info(tcExeData.toJson(false, true));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished to decode String (property) : '" + stringToDecodeInit + "' to :'" + stringToDecode + "'");
        }

        answer.setResultMessage(msg);
        answer.setItem(stringToDecode);
        return answer;
    }

    /**
     * Auxiliary method that returns the execution data for a property.
     *
     * @param hashTemp1 list of execution data
     * @param eachTccp property to be calculated
     * @param forceCalculation indicates whether a property must be
     * re-calculated if it was already computed in previous steps
     * @param tecd execution data for the property
     * @return the updated execution data for the property
     */
    private TestCaseExecutionData getExecutionDataFromList(TreeMap<String, TestCaseExecutionData> hashTemp1, TestCaseCountryProperties eachTccp, boolean forceCalculation, TestCaseExecutionData tecd) {
        LOG.debug("Searching " + eachTccp.getProperty() + " Into list of " + hashTemp1.size());
        try {

            if (hashTemp1.containsKey(eachTccp.getProperty())) {
                if (forceCalculation) {
                    LOG.debug("Property has already been calculated but forcing new calculation by removing it : " + hashTemp1.get(eachTccp.getProperty()));
                    hashTemp1.remove(eachTccp.getProperty());
                    return tecd;
                } else {
                    LOG.debug("Property has already been calculated : " + hashTemp1.get(eachTccp.getProperty()));
                    return hashTemp1.get(eachTccp.getProperty());
                }
            } else {
                LOG.debug("Property was never calculated.");
                return tecd;
            }

        } catch (Exception ex) {
            LOG.error("Exception catched inside getExecutionDataFromList : " + ex, ex);
        }
        return tecd;

    }

    /**
     * Method that takes the potencial @param property, finds it (or not if it
     * is not a existing property) inside the existing property list @param
     * propertiesOfTestcase and gets the list of all other properties required
     * (contained inside value1 or value2).
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
        List<TestCaseCountryProperties> result = new ArrayList<>();
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
            return result;
        }

        /*
         * Check if property value1 and value2 contains internal properties
         */
        List<String> allProperties = new ArrayList<>();

        // Value1 treatment
        List<String> propertiesValue1 = new ArrayList<>();
        //check the properties specified in the test
        for (String propNameFromValue1 : this.getPropertiesListFromString(testCaseCountryProperty.getValue1())) {
            for (TestCaseCountryProperties pr : propertiesOfTestcase) {
                if (pr.getProperty().equals(propNameFromValue1)) {
                    propertiesValue1.add(propNameFromValue1);
                    break;
                }
            }
        }
        allProperties.addAll(propertiesValue1);

        // Value2 treatment :
        List<String> propertiesValue2 = new ArrayList<>();
        //check the properties specified in the test
        for (String propNameFromValue2 : this.getPropertiesListFromString(testCaseCountryProperty.getValue2())) {
            for (TestCaseCountryProperties pr : propertiesOfTestcase) {
                if (pr.getProperty().equals(propNameFromValue2)) {
                    propertiesValue2.add(propNameFromValue2);
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
        TestCaseExecutionData tced;
        for (String key1 : tCExecution.getTestCaseExecutionDataMap().keySet()) {
            tced = tCExecution.getTestCaseExecutionDataMap().get(key1);
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
                                    stringToReplace = stringToReplace.replace("%property." + variableString2 + "%", variableValue);
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
        LOG.debug("Starting to guess properties from string : " + str);
        if (str == null) {
            LOG.debug("Stoping to guess properties - Empty String ");
            return properties;
        }

        String[] text1 = str.split("%");
        int i = 0;
        for (String rawProperty : text1) {
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
                    LOG.debug("Adding string to result " + ramProp2[0].trim());
                } else {
                    LOG.debug("Discarding string (empty or too big or contains cariage return).");
                }
                // Avoid getting empty Property names.
            } else {
                LOG.debug("Discarding string (first or last split).");
            }
            i++;
        }

        LOG.debug("Stopping to guess properties - Finished.");
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
        if (propertieOfTestcase != null) {
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

    @Override
    public void calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseStepActionExecution testCaseStepActionExecution,
            TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        testCaseExecutionData.setStart(new Date().getTime());
        MessageEvent res;
        String test = tCExecution.getTest();
        String testCase = tCExecution.getTestCase();
        AnswerItem<String> answerDecode = new AnswerItem<>();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to calculate Property : '" + testCaseCountryProperty.getProperty() + "'");
        }

        // Checking recursive decode.
        if ((tCExecution.getRecursiveAlreadyCalculatedPropertiesList() != null) && (tCExecution.getRecursiveAlreadyCalculatedPropertiesList().contains(testCaseCountryProperty.getProperty()))) {
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_RECURSIVE);
            res.setDescription(res.getDescription().replace("%PROPERTY%", testCaseCountryProperty.getProperty())
                    .replace("%HISTO%", tCExecution.getRecursiveAlreadyCalculatedPropertiesList().toString()));
            testCaseExecutionData.setPropertyResultMessage(res);
            testCaseExecutionData.setEnd(new Date().getTime());
            LOG.debug("Finished to calculate Property (interupted) : '" + testCaseCountryProperty.getProperty() + "' : " + testCaseExecutionData.getPropertyResultMessage().getDescription());
            return;
        }
        if (tCExecution.getRecursiveAlreadyCalculatedPropertiesList() != null) {
            tCExecution.getRecursiveAlreadyCalculatedPropertiesList().add(testCaseCountryProperty.getProperty());
        }

        try {

            // Check if cache activated and cache entry exist.
            int cacheValue = testCaseCountryProperty.getCacheExpire();
            boolean useCache = false;
            TestCaseExecutionData data = null;

            if (cacheValue > 0) {
                try {
                    data = testCaseExecutionDataService.readLastCacheEntry(tCExecution.getApplicationObj().getSystem(), tCExecution.getEnvironment(), tCExecution.getCountry(), testCaseCountryProperty.getProperty(), cacheValue);
                    if (data != null) {
                        useCache = true;
                    }
                } catch (CerberusException e) {
                    // do nothing, useCache will be false
                }
            }

            if (!useCache) {

                /**
                 * Decode Property replacing properties encapsulated with %
                 */
                if (testCaseCountryProperty.getValue1().contains("%")) {

                    answerDecode = variableService.decodeStringCompletly(testCaseCountryProperty.getValue1(), tCExecution, null, false);
                    testCaseExecutionData.setValue1((String) answerDecode.getItem());
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the property result.
                        testCaseExecutionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Property Value1"));
                        testCaseExecutionData.setEnd(new Date().getTime());
                        testCaseExecutionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        LOG.debug("Finished to calculate Property (interupted) : '" + testCaseCountryProperty.getProperty() + "' : " + testCaseExecutionData.getPropertyResultMessage().getDescription());
                        return;
                    }

                }

                if (testCaseCountryProperty.getValue2() != null && testCaseCountryProperty.getValue2().contains("%")) {

                    answerDecode = variableService.decodeStringCompletly(testCaseCountryProperty.getValue2(), tCExecution, null, false);
                    testCaseExecutionData.setValue2((String) answerDecode.getItem());
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the property result.
                        testCaseExecutionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Property Value2"));
                        testCaseExecutionData.setEnd(new Date().getTime());
                        testCaseExecutionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        LOG.debug("Finished to calculate Property (interupted) : '" + testCaseCountryProperty.getProperty() + "' : " + testCaseExecutionData.getPropertyResultMessage().getDescription());
                        return;
                    }

                }

                // cache not activated or no entry exist.
                int execution_count = 0;
                int retries = testCaseCountryProperty.getRetryNb();
                int periodms = testCaseCountryProperty.getRetryPeriod();
                LOG.debug("Init Retries : " + retries + " Period : " + periodms);

                /**
                 * Controling that retrynb and retryperiod are correctly feeded.
                 * <br>
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
                 * Looping on calculating the action until result is OK or reach
                 * the max retry.
                 */
                while (execution_count <= retries && !(testCaseExecutionData.getPropertyResultMessage().getCodeString().equals("OK"))) {
                    LOG.debug("Attempt #" + execution_count + " " + testCaseCountryProperty.getProperty() + " " + testCaseCountryProperty.getValue1());

                    if (execution_count >= 1) { // We only wait the period if not on the very first calculation.
                        try {
                            Thread.sleep(periodms);
                            LOG.debug("Attempt #" + execution_count + " " + testCaseCountryProperty.getProperty() + " " + testCaseCountryProperty.getValue1() + " Waiting " + periodms + " ms");
                        } catch (InterruptedException ex) {
                            LOG.error(ex.toString(), ex);
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

                        case TestCaseCountryProperties.TYPE_GETFROMSQL:
                            testCaseExecutionData = this.property_getFromSql(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
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

                        case TestCaseCountryProperties.TYPE_GETRAWFROMXML:
                            testCaseExecutionData = this.property_getRawFromXml(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
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

                        case TestCaseCountryProperties.TYPE_GETFROMCOMMAND:
                            testCaseExecutionData = this.property_getFromCommand(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETELEMENTPOSITION:
                            testCaseExecutionData = this.property_getElementPosition(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMNETWORKTRAFFIC:
                            testCaseExecutionData = this.property_getFromNetworkTraffic(testCaseExecutionData, testCaseCountryProperty, tCExecution, forceRecalculation);
                            break;

                        // DEPRECATED Property types.
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

            } else {
                // cache activated and entry exist. We set the current value with cache entry data and notify the result from the messsage.
                TestCaseExecutionData testCaseExecutionDataFromCache = data;
                testCaseExecutionData.setFromCache("Y");
                testCaseExecutionData.setDataLib(testCaseExecutionDataFromCache.getDataLib());
                testCaseExecutionData.setValue(testCaseExecutionDataFromCache.getValue());
                testCaseExecutionData.setJsonResult(testCaseExecutionDataFromCache.getJsonResult());

                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_DISPLAY);
                res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_FROMCACHE).resolveDescription("ID", String.valueOf(testCaseExecutionDataFromCache.getId())).resolveDescription("DATE", df.format(testCaseExecutionDataFromCache.getStart()));
                testCaseExecutionData.setPropertyResultMessage(res);

                if (!StringUtil.isNullOrEmpty(testCaseExecutionDataFromCache.getJsonResult())) {

                    // Convert json to HashMap.
                    List<HashMap<String, String>> result = null;
                    result = new ArrayList<>();
                    try {
                        LOG.debug("Converting Json : " + testCaseExecutionDataFromCache.getJsonResult());

                        JSONArray json = new JSONArray(testCaseExecutionDataFromCache.getJsonResult());
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject explrObject = json.getJSONObject(i);
                            LOG.debug(explrObject.toString());
                            HashMap<String, String> resultHash = new HashMap<>();
                            Iterator<?> nameItr = explrObject.keys();
                            while (nameItr.hasNext()) {
                                String name = (String) nameItr.next();
                                if (name.equals("KEY")) {
                                    resultHash.put("", explrObject.getString(name));
                                } else {
                                    resultHash.put(name, explrObject.getString(name));
                                }
                            }
                            result.add(resultHash);
                        }
                    } catch (JSONException ex) {
                        java.util.logging.Logger.getLogger(PropertyService.class.getName()).log(Level.SEVERE, null, ex);
                        LOG.error(ex, ex);
                    }
                    testCaseExecutionData.setDataLibRawData(result);
                    //Record result in filessytem.
                    recorderService.recordTestDataLibProperty(tCExecution.getId(), testCaseCountryProperty.getProperty(), 1, result);

                }

            }

        } catch (CerberusEventException ex) {
            LOG.error(ex.toString(), ex);
            testCaseExecutionData.setEnd(new Date().getTime());
            testCaseExecutionData.setPropertyResultMessage(ex.getMessageError());
        }

        testCaseExecutionData.setEnd(new Date().getTime());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished to calculate Property : '" + testCaseCountryProperty.getProperty() + "'");
        }

    }

    private TestCaseExecutionData property_getFromCommand(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        // Check if script has been correctly defined
        String script = testCaseExecutionData.getValue1();
        if (script == null || script.isEmpty()) {
            testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOMMAND_NULL));
            return testCaseExecutionData;
        }

        // Try to evaluate Command script
        try {
            if (tCExecution.getAppTypeEngine().equals(Application.TYPE_APK)) {
                String message = androidAppiumService.executeCommandString(tCExecution.getSession(), script, testCaseExecutionData.getValue2());

                String value = "";
                if (!StringUtil.isNullOrEmpty(message)) {
                    value = message;
                }
                testCaseExecutionData.setValue(value);
                testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMCOMMAND).resolveDescription("VALUE", value));

            } else if (tCExecution.getAppTypeEngine().equals(Application.TYPE_IPA)) {
                String message = iosAppiumService.executeCommandString(tCExecution.getSession(), script, testCaseExecutionData.getValue2());

                String value = "";
                if (!StringUtil.isNullOrEmpty(message)) {
                    value = message;
                }
                testCaseExecutionData.setValue(value);
                testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMCOMMAND).resolveDescription("VALUE", value));

            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_FEATURENOTSUPPORTED);
                res.setDescription(res.getDescription().replace("%APPTYPE%", tCExecution.getAppTypeEngine()));
                res.setDescription(res.getDescription().replace("%PROPTYPE%", testCaseExecutionData.getType()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception e) {
            LOG.debug("Exception Running Command Script :" + e.getMessage());
            testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMCOMMAND_EXCEPTION).resolveDescription("REASON", e.getMessage()));
        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getElementPosition(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        // Check if script has been correctly defined
        String script = testCaseExecutionData.getValue1();
        if (script == null || script.isEmpty()) {
            testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETELEMENTPOSITION_NULL));
            return testCaseExecutionData;
        }

        try {
            Identifier identifier = new Identifier();
            if (script != null) {
                identifier = identifierService.convertStringToIdentifier(script);
            }

            if (tCExecution.getAppTypeEngine().equals(Application.TYPE_APK)) {
                String message = androidAppiumService.getElementPosition(tCExecution.getSession(), identifier);

                String value = "";
                if (!StringUtil.isNullOrEmpty(message)) {
                    value = message;
                }
                testCaseExecutionData.setValue(value);
                testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETELEMENTPOSITION).resolveDescription("VALUE", value));
            } else if (tCExecution.getAppTypeEngine().equals(Application.TYPE_IPA)) {
                String message = iosAppiumService.getElementPosition(tCExecution.getSession(), identifier);

                String value = "";
                if (!StringUtil.isNullOrEmpty(message)) {
                    value = message;
                }
                testCaseExecutionData.setValue(value);
                testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETELEMENTPOSITION).resolveDescription("VALUE", value));
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_FEATURENOTSUPPORTED);
                res.setDescription(res.getDescription().replace("%APPTYPE%", tCExecution.getAppTypeEngine()));
                res.setDescription(res.getDescription().replace("%PROPTYPE%", testCaseExecutionData.getType()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } catch (Exception e) {
            LOG.debug("Exception Running Command Script :" + e.getMessage());
            testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETELEMENTPOSITION_EXCEPTION).resolveDescription("REASON", e.getMessage()));
        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_executeSqlFromLib(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution, boolean forceCalculation) {
        try {
            String script = this.sqlLibraryService.findSqlLibraryByKey(testCaseExecutionData.getValue1()).getScript();
            testCaseExecutionData.setValue1(script); //TODO use the new library

        } catch (CerberusException ex) {
            LOG.warn(ex);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_SQLLIB_NOTEXIT);
            res.setDescription(res.getDescription().replace("%SQLLIB%", testCaseExecutionData.getValue1()));

            testCaseExecutionData.setPropertyResultMessage(res);

            testCaseExecutionData.setEnd(new Date().getTime());
            return testCaseExecutionData;
        }
        testCaseExecutionData = this.property_getFromSql(testCaseExecutionData, tCExecution, testCaseCountryProperty, forceCalculation);
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromNetworkTraffic(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution, boolean forceCalculation) {
        if ("Y".equalsIgnoreCase(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {

            try {
                //TODO : check if HAR is the same than the last one to avoid to download same har file several times
                // String remoteHarMD5 = "http://" + tCExecution.getRobotExecutorObj().getHost() + ":" + tCExecution.getRobotExecutorObj().getExecutorExtensionPort() + "/getHarMD5?uuid="+tCExecution.getRemoteProxyUUID();

                JSONObject harRes = executorService.getHar(testCaseExecutionData.getValue1(), false, tCExecution.getRobotExecutorObj().getExecutorExtensionHost(), tCExecution.getRobotExecutorObj().getExecutorExtensionPort(),
                        tCExecution.getRemoteProxyUUID(), tCExecution.getSystem());

                harRes = harService.enrichWithStats(harRes, tCExecution.getCountryEnvironmentParameters().getDomain(), tCExecution.getSystem());

                //Record result in filessytem.
                testCaseExecutionData.addFileList(recorderService.recordProperty(tCExecution.getId(), testCaseExecutionData.getProperty(), 1, harRes.toString(1)));

                String valueFromJson = this.jsonService.getFromJson(harRes.toString(), null, testCaseExecutionData.getValue2());

                if (valueFromJson != null) {
                    testCaseExecutionData.setValue(valueFromJson);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMNETWORKTRAFFIC);
                    res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
                    res.setDescription(res.getDescription().replace("%VALUE%", valueFromJson));
                    testCaseExecutionData.setPropertyResultMessage(res);

                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMNETWORKTRAFFIC_PATHNOTFOUND);
                    res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue2()));
                    testCaseExecutionData.setPropertyResultMessage(res);

                }

            } catch (Exception ex) {
                LOG.warn("Exception when getting property from Network Traffic.", ex);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMNETWORKTRAFFIC_PROXYNOTACTIVE);
                res.setDescription(res.getDescription().replace("%DETAIL%", ex.toString()));

                testCaseExecutionData.setPropertyResultMessage(res);

                testCaseExecutionData.setEnd(new Date().getTime());
                return testCaseExecutionData;
            }
        } else {
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMNETWORKTRAFFIC_PROXYNOTACTIVE);
            res.setDescription(res.getDescription().replace("%ROBOT%", tCExecution.getRobot()));
            res.setDescription(res.getDescription().replace("%EXECUTOR%", tCExecution.getRobotExecutor()));
            testCaseExecutionData.setPropertyResultMessage(res);

        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromSql(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        return sQLService.calculateOnDatabase(testCaseExecutionData, testCaseCountryProperty, tCExecution);
    }

    private TestCaseExecutionData property_calculateText(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        if (TestCaseCountryProperties.NATURE_RANDOM.equals(testCaseCountryProperty.getNature())
                //TODO CTE Voir avec B. Civel "RANDOM_NEW"
                || (testCaseCountryProperty.getNature().equals(TestCaseCountryProperties.NATURE_RANDOMNEW))) {
            if (testCaseCountryProperty.getLength().equals("0")) {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TEXTRANDOMLENGHT0);
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                String charset;
                if (testCaseExecutionData.getValue1() != null && !"".equals(testCaseExecutionData.getValue1().trim())) {
                    charset = testCaseExecutionData.getValue1();
                } else {
                    charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                }
                String value = StringUtil.getRandomString(ParameterParserUtil.parseIntegerParam(testCaseCountryProperty.getLength(), 0), charset);
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
        if (tCExecution.getAppTypeEngine().equals(Application.TYPE_APK)
                || tCExecution.getAppTypeEngine().equals(Application.TYPE_IPA)
                || tCExecution.getAppTypeEngine().equals(Application.TYPE_GUI)) {

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
            res.setDescription(res.getDescription().replace("%APPTYPE%", tCExecution.getAppTypeEngine()));
            res.setDescription(res.getDescription().replace("%PROPTYPE%", testCaseExecutionData.getType()));

        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromJS(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        String script = testCaseExecutionData.getValue1();
        String valueFromJS;
        String message = "";
        if (tCExecution.getManualExecution().equals("Y")) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_NOTPOSSIBLE);
            testCaseExecutionData.setPropertyResultMessage(mes);
        } else {
            try {
                valueFromJS = this.webdriverService.getValueFromJS(tCExecution.getSession(), script);
            } catch (Exception e) {
                message = e.getMessage().split("\n")[0];
                LOG.debug("Exception Running JS Script :" + message);
                valueFromJS = null;
            }
            if (valueFromJS != null) {
                testCaseExecutionData.setValue(valueFromJS);
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_JS);
                res.setDescription(res.getDescription().replace("%SCRIPT%", script));
                res.setDescription(res.getDescription().replace("%VALUE%", valueFromJS));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_JS_EXCEPTION);
                res.setDescription(res.getDescription().replace("%EXCEPTION%", message));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
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
        AnswerItem<String> answerDecode = new AnswerItem<>();

        try {
            AppService appService = this.appServiceService.findAppServiceByKey(testCaseExecutionData.getValue1());
            if (appService != null) {

                String decodedEnveloppe = appService.getServiceRequest();
                String decodedServicePath = appService.getServicePath();
                String decodedMethod = appService.getOperation();
                String decodedAttachement = appService.getAttachementURL();

                if (appService.getServiceRequest().contains("%")) {
                    answerDecode = variableService.decodeStringCompletly(appService.getServiceRequest(), tCExecution, testCaseStepActionExecution, false);
                    decodedEnveloppe = (String) answerDecode.getItem();
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseExecutionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "SOAP Service Request"));
                        testCaseExecutionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        LOG.debug("Property interupted due to decode 'SOAP Service Request' Error.");
                        return testCaseExecutionData;
                    }
                }
                if (appService.getServicePath().contains("%")) {
                    answerDecode = variableService.decodeStringCompletly(appService.getServicePath(), tCExecution, testCaseStepActionExecution, false);
                    decodedServicePath = (String) answerDecode.getItem();
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseExecutionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "SOAP Service Path"));
                        testCaseExecutionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        LOG.debug("Property interupted due to decode 'SOAP Service Path.");
                        return testCaseExecutionData;
                    }
                }
                if (appService.getOperation().contains("%")) {
                    answerDecode = variableService.decodeStringCompletly(appService.getOperation(), tCExecution, testCaseStepActionExecution, false);
                    decodedMethod = (String) answerDecode.getItem();
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseExecutionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "SOAP Operation"));
                        testCaseExecutionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        LOG.debug("Property interupted due to decode 'SOAP Operation.");
                        return testCaseExecutionData;
                    }
                }
                if (appService.getAttachementURL().contains("%")) {
                    answerDecode = variableService.decodeStringCompletly(appService.getAttachementURL(), tCExecution, testCaseStepActionExecution, false);
                    decodedAttachement = (String) answerDecode.getItem();
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action r
                        // If anything wrong with the decode --> we stop here with decode message in the acesult.
                        testCaseExecutionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "SOAP Attachement URL"));
                        testCaseExecutionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        LOG.debug("Property interupted due to decode 'SOAP Attachement URL.");
                        return testCaseExecutionData;
                    }
                }

                //Call Soap and set LastSoapCall of the testCaseExecution.
                AnswerItem soapCall = soapService.callSOAP(decodedEnveloppe, decodedServicePath, decodedMethod, decodedAttachement, null, null, 60000, tCExecution.getApplicationObj().getSystem());
                AppService se1 = (AppService) soapCall.getItem();
//                tCExecution.setLastSOAPCalled(soapCall);

                if (soapCall.isCodeEquals(200)) {
//                    SOAPExecution lastSoapCalled = (SOAPExecution) tCExecution.getLastSOAPCalled().getItem();
                    String xmlResponse = se1.getResponseHTTPBody();
                    result = xmlUnitService.getFromXml(xmlResponse, appService.getAttachementURL());
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
            LOG.error(exception.toString(), exception);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);
            res.setDescription(res.getDescription().replace("%PROPERTY%", testCaseExecutionData.getValue1()));
            testCaseExecutionData.setPropertyResultMessage(res);

        } catch (CerberusEventException ex) {
            LOG.error(ex.toString(), ex);
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

    private TestCaseExecutionData property_getRawFromXml(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {

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
            String valueFromXml = xmlUnitService.getRawFromXml(xmlToParse, testCaseExecutionData.getValue1());
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

            if (!(StringUtil.isNullOrEmpty(testCaseExecutionData.getValue2()))) {
                try {
                    URL myurl;
                    myurl = new URL(testCaseExecutionData.getValue2());

                    String str = "";
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new InputStreamReader(myurl.openStream()));
                        while (null != (str = br.readLine())) {
                            sb.append(str);
                        }
                    } catch (IOException ex) {
                        LOG.warn("Error Getting Json File " + ex);
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e) {
                                LOG.warn(e.toString());
                            }
                        }
                    }
                    jsonResponse = sb.toString();

                } catch (MalformedURLException e) {
                    LOG.debug("URL is invalid so we consider that it is a json file.");
                    jsonResponse = testCaseExecutionData.getValue2();
                }
            }

            //Record result in filessytem.
            recorderService.recordProperty(tCExecution.getId(), testCaseExecutionData.getProperty(), 1, jsonResponse);

            String valueFromJson = this.jsonService.getFromJson(jsonResponse, null, testCaseExecutionData.getValue1());

            if (valueFromJson != null) {
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
                res.setDescription(res.getDescription().replace("%ERROR%", ""));
                testCaseExecutionData.setPropertyResultMessage(res);

            }
        } catch (Exception exception) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Exception when getting property from JSON.", exception);
            }
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
            res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%ERROR%", exception.toString()));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromDataLib(TestCaseExecutionData testCaseExecutionData, TestCaseExecution tCExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {

        MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB);

        TestDataLib testDataLib;
        List<HashMap<String, String>> result = null;
        AnswerItem<String> answerDecode = new AnswerItem<>();

        // We get here the correct TestDataLib entry from the Value1 (name) that better match the context on system, environment and country.
        AnswerItem<TestDataLib> answer = testDataLibService.readByNameBySystemByEnvironmentByCountry(testCaseExecutionData.getValue1(),
                tCExecution.getApplicationObj().getSystem(), tCExecution.getEnvironmentData(),
                tCExecution.getCountry());

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answer.getItem() != null) {
            testDataLib = (TestDataLib) answer.getItem();

            AnswerList<HashMap<String, String>> serviceAnswer;

            // Here, we try to decode the SQL field if datalib is SQL type.
            try {
                if (testDataLib.getType().equals(TestDataLib.TYPE_SQL)) {
                    //check if the script contains properties that neeed to be calculated
                    answerDecode = variableService.decodeStringCompletly(testDataLib.getScript(), tCExecution, testCaseStepActionExecution, false);
                    String decodedScript = (String) answerDecode.getItem();
                    testDataLib.setScript(decodedScript);
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseExecutionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "SQL Script"));
                        testCaseExecutionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        LOG.debug("Property interupted due to decode 'SQL Script'.");
                        return testCaseExecutionData;
                    }
                }
            } catch (CerberusEventException cex) {
                LOG.error(cex.toString(), cex);
            }

            String decodedLength = null;

            // Here, we try to decode testCaseCountryProperty field `length` to get the value of property if needed
            try {
                answerDecode = variableService.decodeStringCompletly(testCaseCountryProperty.getLength(), tCExecution, testCaseStepActionExecution, false);
                decodedLength = (String) answerDecode.getItem();
                if (!(answerDecode.isCodeStringEquals("OK"))) {
                    testCaseExecutionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "length"));
                    testCaseExecutionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                    LOG.debug("Property interupted due to decode 'Length field'.");
                    return testCaseExecutionData;
                }
            } catch (CerberusEventException cex) {
                LOG.error(cex.toString(), cex);
            }

            // We cast from string to integer ((String)testcasecountryproperty field `length` -> (Integer)testcaseexecutiondata field `length`)
            // if we can't, testCaseExecutionData field `length` will be equal to 0
            // if we can, we set the value of testCaseExecutionData field `length` to the casted value
            if (decodedLength != null) {
                try {
                    Integer.parseInt(decodedLength);
                    testCaseExecutionData.setLength(decodedLength);

                } catch (NumberFormatException e) {
                    LOG.error(e.toString(), e);
                    MessageEvent msg = new MessageEvent(MessageEventEnum.CASTING_OPERATION_FAILED);
                    msg.setDescription(msg.getDescription().replace("%ERROR%", e.toString()));
                    msg.setDescription(msg.getDescription().replace("%FIELD%", "field length"));
                    testCaseExecutionData.setPropertyResultMessage(msg);
                    testCaseExecutionData.setStopExecution(msg.isStopTest());
                    return testCaseExecutionData;
                }
            }

            // We calculate here the result for the lib
            serviceAnswer = dataLibService.getFromDataLib(testDataLib, testCaseCountryProperty, tCExecution, testCaseExecutionData);
            testCaseExecutionData.setDataLib(testDataLib.getName());

            res = serviceAnswer.getResultMessage();
            result = (List<HashMap<String, String>>) serviceAnswer.getDataList(); //test data library returned by the service

            if (result != null) {
                // Keeping raw data to testCaseExecutionData object.
                testCaseExecutionData.setDataLibRawData(result);

                // Value of testCaseExecutionData object takes the master subdata entry "".
                String value = (String) result.get(0).get("");
                if (value == null) {
                    testCaseExecutionData.setValue(VALUE_NULL);
                } else {
                    testCaseExecutionData.setValue(value);
                    // Converting HashMap to json.
                    String jsonText = "";
                    JSONArray jsonResult = null;
                    try {
                        jsonResult = dataLibService.convertToJSONObject(result);
                        jsonText = jsonResult.toString();
                    } catch (JSONException ex) {
                        java.util.logging.Logger.getLogger(PropertyService.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    testCaseExecutionData.setJsonResult(jsonText);
                }

                //Record result in filessytem.
                recorderService.recordTestDataLibProperty(tCExecution.getId(), testCaseCountryProperty.getProperty(), 1, result);

            }

            res.setDescription(res.getDescription().replace("%ENTRY%", testDataLib.getName()).replace("%ENTRYID%", String.valueOf(testDataLib.getTestDataLibID())));

        } else {//no TestDataLib found was returned
            //the library does not exist at all
            AnswerList nameExistsAnswer = testDataLibService.readNameListByName(testCaseExecutionData.getValue1(), 1, false);
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
