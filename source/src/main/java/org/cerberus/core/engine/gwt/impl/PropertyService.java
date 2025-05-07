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
package org.cerberus.core.engine.gwt.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.InvalidPathException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionData;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionData;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ISqlLibraryService;
import org.cerberus.core.crud.service.ITestCaseExecutionDataService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.engine.entity.ExecutionLog;
import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.execution.IIdentifierService;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.engine.gwt.IPropertyService;
import org.cerberus.core.engine.gwt.IVariableService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.appium.impl.AndroidAppiumService;
import org.cerberus.core.service.appium.impl.IOSAppiumService;
import org.cerberus.core.service.datalib.IDataLibService;
import org.cerberus.core.service.groovy.IGroovyService;
import org.cerberus.core.service.har.IHarService;
import org.cerberus.core.service.json.IJsonService;
import org.cerberus.core.service.robotproxy.IRobotProxyService;
import org.cerberus.core.service.soap.ISoapService;
import org.cerberus.core.service.sql.ISQLService;
import org.cerberus.core.service.webdriver.IWebDriverService;
import org.cerberus.core.service.xmlunit.IXmlUnitService;
import org.cerberus.core.util.DateUtil;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.jboss.aerogear.security.otp.Totp;
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
    public static final String VALUE_NULL = "<NULL>";
    /**
     * The property variable {@link Pattern}
     */
    public static final Pattern DATALIB_VARIABLE_PATTERN = Pattern.compile("%datalib\\.[^%]+%");

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
    private IRobotProxyService executorService;
    @Autowired
    private IHarService harService;

    @Override
    public AnswerItem<String> decodeStringWithExistingProperties(String stringToDecode, TestCaseExecution execution,
            TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {

        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        AnswerItem<String> answer = new AnswerItem<>();
        answer.setResultMessage(msg);
        answer.setItem(stringToDecode);

        String country = execution.getCountry();
        long now = new Date().getTime();
        String stringToDecodeInit = stringToDecode;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to decode string (Property) : " + stringToDecode);
        }

        /**
         * We start to decode properties from available executiondata List.
         */
        stringToDecode = decodeStringWithAlreadyCalculatedProperties(stringToDecode, execution);

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
        List<TestCaseCountryProperties> tcProperties = execution.getTestCaseCountryPropertyList();
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
            tcExeData = factoryTestCaseExecutionData.create(execution.getId(), eachTccp.getProperty(), 1, eachTccp.getDescription(), null, eachTccp.getType(), eachTccp.getRank(),
                    eachTccp.getValue1(), eachTccp.getValue2(), eachTccp.getValue3(), null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING),
                    eachTccp.getRetryNb(), eachTccp.getRetryPeriod(), eachTccp.getDatabase(), eachTccp.getValue1(), eachTccp.getValue2(), eachTccp.getValue3(), eachTccp.getLength(),
                    eachTccp.getLength(), eachTccp.getRowLimit(), eachTccp.getNature(), execution.getApplicationObj().getSystem(), execution.getEnvironment(), execution.getCountry(), "", null, "N");
            tcExeData.setTestCaseCountryProperties(eachTccp);
            tcExeData.settCExecution(execution);
            LOG.debug("Trying to calculate Property : '" + tcExeData.getProperty() + "' " + tcExeData);

            /*  First check if property has already been calculated
             *  if action is calculateProperty, then set isKnownData to false.
             */
            tcExeData = getExecutionDataFromList(execution.getTestCaseExecutionDataMap(), eachTccp, forceCalculation, tcExeData);

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
                calculateProperty(tcExeData, execution, testCaseStepActionExecution, eachTccp, forceCalculation);
                msg = tcExeData.getPropertyResultMessage();
                //saves the result
                try {
                    if (tcExeData.getId() > 0) { // Data is not saved to database in case the property is calculated from a service call simulation.
                        testCaseExecutionDataService.save(tcExeData, execution.getSecrets());
                    }
                    /**
                     * Add TestCaseExecutionData in TestCaseExecutionData List
                     * of the TestCaseExecution
                     */
                    LOG.debug("Adding into Execution data list. Property : '" + tcExeData.getProperty() + "' Index : '" + String.valueOf(tcExeData.getIndex()) + "' Value : '" + tcExeData.getValue() + "'");
                    execution.getTestCaseExecutionDataMap().put(tcExeData.getProperty(), tcExeData);
                    if (tcExeData.getDataLibRawData() != null) { // If the property is a TestDataLib, we same all rows retreived in order to support nature such as NOTINUSe or RANDOMNEW.
                        for (int i = 1; i < (tcExeData.getDataLibRawData().size()); i++) {
                            now = new Date().getTime();
                            TestCaseExecutionData tcedS = factoryTestCaseExecutionData.create(tcExeData.getId(), tcExeData.getProperty(), (i + 1),
                                    tcExeData.getDescription(), tcExeData.getDataLibRawData().get(i).get(""), tcExeData.getType(), tcExeData.getRank(), "", "", "",
                                    tcExeData.getRC(), "", now, now, now, now, null, 0, 0, "", "", "", "", "", "", 0, "", tcExeData.getSystem(), tcExeData.getEnvironment(), tcExeData.getCountry(), tcExeData.getDataLib(), null, "N");
                            testCaseExecutionDataService.save(tcedS, execution.getSecrets());
                        }
                    }
                } catch (CerberusException cex) {
                    LOG.error(cex.getMessage(), cex);
                }
            }

            /**
             * After calculation, replace properties by value calculated
             */
            stringToDecode = decodeStringWithAlreadyCalculatedProperties(stringToDecode, execution);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + eachTccp.getProperty() + " calculated with Value = " + tcExeData.getValue() + ", Value1 = " + tcExeData.getValue1() + ", Value2 = " + tcExeData.getValue2());
            }
            /**
             * Log TestCaseExecutionData
             */
            if ((execution.getVerbose() > 0) && parameterService.getParameterBooleanByKey("cerberus_executionlog_enable", execution.getSystem(), false)) {
                LOG.info(tcExeData.toJson(false, true, execution.getSecrets()));
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
                // %property.PROPERTY%
                if (tced.getValue() != null) {
                    stringToReplace = stringToReplace.replace("%property." + tced.getProperty() + "%", tced.getValue());
                    stringToReplace = stringToReplace.replace("%" + tced.getProperty() + "%", tced.getValue());
                }

                // Nb of rows of the property.
                // %property.PROPERTY.nbrows%
                if ((tced.getValue() != null) && (!(tced.getDataLibRawData() == null))) {
                    stringToReplace = stringToReplace.replace("%property." + tced.getProperty() + ".nbrows%", String.valueOf(tced.getDataLibRawData().size()));
                    stringToReplace = stringToReplace.replace("%" + tced.getProperty() + ".nbrows%", String.valueOf(tced.getDataLibRawData().size()));
                }

                // For each subdata of the getFromDataLib property, we try to replace with PROPERTY(SUBDATA).
                if (!(tced.getDataLibRawData() == null)) {
                    int ind = 0;
                    for (HashMap<String, String> dataRow : tced.getDataLibRawData()) { // We loop every row result.
                        for (String key : dataRow.keySet()) { // We loop every subdata
                            if (dataRow.get(key) != null) {
                                variableValue = dataRow.get(key);

                                // %property.PROPERTY(m)(SUBDATA)%
                                // %property.PROPERTY.m.SUBDATA%
                                variableString1 = tced.getProperty() + "(" + (ind + 1) + ")" + "(" + key + ")";
                                stringToReplace = stringToReplace.replace("%property." + variableString1 + "%", variableValue);
                                stringToReplace = stringToReplace.replace("%" + variableString1 + "%", variableValue);
                                variableString2 = tced.getProperty() + "." + (ind + 1) + "." + key;
                                stringToReplace = stringToReplace.replace("%property." + variableString2 + "%", variableValue);
                                stringToReplace = stringToReplace.replace("%" + variableString2 + "%", variableValue);

                                // %property.PROPERTY(m)%
                                // %property.PROPERTY.m%
                                if (key.isEmpty()) { // If subdata is empty we can omit the () or .
                                    variableString1 = tced.getProperty() + "(" + (ind + 1) + ")";
                                    stringToReplace = stringToReplace.replace("%property." + variableString1 + "%", variableValue);
                                    stringToReplace = stringToReplace.replace("%" + variableString1 + "%", variableValue);
                                    variableString2 = tced.getProperty() + "." + (ind + 1);
                                    stringToReplace = stringToReplace.replace("%property." + variableString2 + "%", variableValue);
                                    stringToReplace = stringToReplace.replace("%" + variableString2 + "%", variableValue);
                                }

                                // %property.PROPERTY(SUBDATA)%
                                // %property.PROPERTY.SUBDATA%
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
        List<String> properties = new ArrayList<>();
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
                if (!(StringUtil.isEmptyOrNull(ramProp2[0].trim())) // Avoid getting empty Property names.
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

        AnswerItem<TestCaseCountryProperties> item = new AnswerItem<>();
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
    public String decodeStringWithDatalib(String stringToDecode, TestCaseExecution execution, boolean forceCalculation) throws CerberusEventException {
        String stringToDecodeInit = stringToDecode;
        String system = "";
        String environment = "";
        String country = "";

        if (execution != null) {
            system = execution.getApplicationObj().getSystem();
            environment = execution.getEnvironmentData();
            country = execution.getCountry();
        }

        LOG.debug("Starting to decode string (Datalib) : " + stringToDecode);

        /**
         * Look at all the potential properties still contained in
         * StringToDecode (considering that properties are between %).
         */
        List<String> internalDatalibFromStringToDecode = this.getDatalibStringListFromString(stringToDecode);
        LOG.debug("Internal potencial Datalib still found inside String '" + stringToDecode + "' : " + internalDatalibFromStringToDecode);

        if (internalDatalibFromStringToDecode.isEmpty()) { // We escape if no property found on the string to decode
            LOG.debug("Finished to decode (no Datalib detected in string). Result : '" + stringToDecodeInit + "' to :'" + stringToDecode + "'");
            return stringToDecode;
        }

        Iterator i = internalDatalibFromStringToDecode.iterator();
        while (i.hasNext()) {
            String value = (String) i.next();
            String[] valueA = value.split("\\.");
            if (valueA.length >= 3) {
                LOG.debug("datalib name : " + valueA[1]);
                LOG.debug("datalib varaible requested : " + valueA[2]);

                String propName = "ENGINE-" + value;
                TestCaseExecutionData dataExecution = factoryTestCaseExecutionData.create(0, propName, 0, "Engine Data", valueA[1], TestCaseCountryProperties.TYPE_GETFROMDATALIB, 0, valueA[1], "", "", "",
                        null, 0, 0, 0, 0, null, 0, 0, "", "", "", "", "1", "1", 1, "STATIC", system, environment, country, valueA[1], "{}", "N");
                TestCaseCountryProperties property = TestCaseCountryProperties.builder().nature(TestCaseCountryProperties.NATURE_STATIC).cacheExpire(0).length("1").property(propName).build();
                dataExecution = property_getFromDataLib(dataExecution, execution, null, property, false);
                String val = null;
                if ("value".equals(valueA[2])) {
                    val = dataExecution.getValue();
                } else if ("base64".equals(valueA[2])) {
                    if ((dataExecution.getDataLibObj() != null) && StringUtil.isNotEmptyOrNull(dataExecution.getDataLibObj().getCsvUrl())) {

                        if (!StringUtil.isURL(dataExecution.getDataLibObj().getCsvUrl())) {
                            String filePath = StringUtil.addSuffixIfNotAlready(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_testdatalibfile_path, "", ""), File.separator) + dataExecution.getDataLibObj().getCsvUrl();
                            try {
                                File file = new File(filePath);
                                byte[] fileContent = Files.readAllBytes(file.toPath());
                                val = Base64.getEncoder().encodeToString(fileContent);
                            } catch (IOException e) {
                                val = "!! DECODE ERROR - could not read file :'" + filePath + "' !!";
                                LOG.warn(val, e);
                            }
                        } else {
                            URL urlToCall;
                            try {
                                urlToCall = new URL(dataExecution.getDataLibObj().getCsvUrl());
                                byte[] fileContent = urlToCall.openStream().readAllBytes();
                                val = Base64.getEncoder().encodeToString(fileContent);
                            } catch (Exception ex) {
                                val = "!! DECODE ERROR - could not read file :'" + dataExecution.getDataLibObj().getCsvUrl() + "' !!";
                                LOG.warn(val, ex);
                            }
                        }

                    }
                }
                if (val != null) {
                    stringToDecode = stringToDecode.replace("%" + value + "%", val);
                }
            }
        }

        LOG.debug("Finished to decode String (Datalib) : '" + stringToDecodeInit + "' to :'" + stringToDecode + "'");
        return stringToDecode;
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
     * @see #PROPERTY_VARIABLE_PATTERN
     */
    private List<String> getDatalibStringListFromString(String str) {
        List<String> datalibs = new ArrayList<>();
        if (str == null) {
            return datalibs;
        }

        Matcher datalibMatcher = DATALIB_VARIABLE_PATTERN.matcher(str);
        while (datalibMatcher.find()) {
            String rawDatalib = datalibMatcher.group();
            // Removes the first and last '%' character to only get the property name
            rawDatalib = rawDatalib.substring(1, rawDatalib.length() - 1);
            datalibs.add(rawDatalib);
        }
        return datalibs;
    }

    @Override
    public void calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseExecution execution, TestCaseStepActionExecution testCaseStepActionExecution,
            TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        testCaseExecutionData.setStart(new Date().getTime());
        MessageEvent res;
        String test = execution.getTest();
        String testCase = execution.getTestCase();
        AnswerItem<String> answerDecode = new AnswerItem<>();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to calculate Property : '" + testCaseCountryProperty.getProperty() + "'");
            execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Starting to calculate Property : '" + testCaseCountryProperty.getProperty() + "'");

        }

        // Checking recursive decode.
        if ((execution.getRecursiveAlreadyCalculatedPropertiesList() != null) && (execution.getRecursiveAlreadyCalculatedPropertiesList().contains(testCaseCountryProperty.getProperty()))) {
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_RECURSIVE);
            res.setDescription(res.getDescription().replace("%PROPERTY%", testCaseCountryProperty.getProperty())
                    .replace("%HISTO%", execution.getRecursiveAlreadyCalculatedPropertiesList().toString()));
            testCaseExecutionData.setPropertyResultMessage(res);
            testCaseExecutionData.setEnd(new Date().getTime());
            LOG.debug("Finished to calculate Property (interupted) : '" + testCaseCountryProperty.getProperty() + "' : " + testCaseExecutionData.getPropertyResultMessage().getDescription());
            return;
        }
        if (execution.getRecursiveAlreadyCalculatedPropertiesList() != null) {
            execution.getRecursiveAlreadyCalculatedPropertiesList().add(testCaseCountryProperty.getProperty());
        }

        try {

            // Check if cache activated and cache entry exist.
            int cacheValue = testCaseCountryProperty.getCacheExpire();
            boolean useCache = false;
            TestCaseExecutionData data = null;

            if (cacheValue > 0) {
                try {
                    data = testCaseExecutionDataService.readLastCacheEntry(execution.getApplicationObj().getSystem(), execution.getEnvironment(), execution.getCountry(), testCaseCountryProperty.getProperty(), cacheValue);
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

                    answerDecode = variableService.decodeStringCompletly(testCaseCountryProperty.getValue1(), execution, null, false);
                    testCaseExecutionData.setValue1(answerDecode.getItem());
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

                    answerDecode = variableService.decodeStringCompletly(testCaseCountryProperty.getValue2(), execution, null, false);
                    testCaseExecutionData.setValue2(answerDecode.getItem());
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
                        retries = maxtotalduration / periodms;
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
                            testCaseExecutionData = this.property_getFromDataLib(testCaseExecutionData, execution, testCaseStepActionExecution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMSQL:
                            testCaseExecutionData = this.property_getFromSql(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMHTML:
                            testCaseExecutionData = this.property_getFromHtml(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMHTMLVISIBLE:
                            testCaseExecutionData = this.property_getFromHtmlVisible(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMJS:
                            testCaseExecutionData = this.property_getFromJS(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETATTRIBUTEFROMHTML:
                            testCaseExecutionData = this.property_getAttributeFromHtml(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMCOOKIE:
                            testCaseExecutionData = this.property_getFromCookie(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMXML:
                            testCaseExecutionData = this.property_getFromXml(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETRAWFROMXML:
                            testCaseExecutionData = this.property_getRawFromXml(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETDIFFERENCESFROMXML:
                            testCaseExecutionData = this.property_getDifferencesFromXml(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMJSON:
                            testCaseExecutionData = this.property_getFromJson(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETRAWFROMJSON:
                            testCaseExecutionData = this.property_getRawFromJson(testCaseExecutionData, execution);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMGROOVY:
                            testCaseExecutionData = this.property_getFromGroovy(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMCOMMAND:
                            testCaseExecutionData = this.property_getFromCommand(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETELEMENTPOSITION:
                            testCaseExecutionData = this.property_getElementPosition(testCaseExecutionData, execution, testCaseCountryProperty, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMNETWORKTRAFFIC:
                            testCaseExecutionData = this.property_getFromNetworkTraffic(testCaseExecutionData, testCaseCountryProperty, execution, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETOTP:
                            testCaseExecutionData = this.property_getOTP(testCaseExecutionData, testCaseCountryProperty, execution, forceRecalculation);
                            break;

                        case TestCaseCountryProperties.TYPE_GETFROMEXECUTIONOBJECT:
                            testCaseExecutionData = this.property_getFromExecutionObject(testCaseExecutionData, testCaseCountryProperty, execution, forceRecalculation);
                            break;

                        // DEPRECATED Property types.
//                        case TestCaseCountryProperties.TYPE_EXECUTESOAPFROMLIB: // DEPRECATED
//                            testCaseExecutionData = this.property_executeSoapFromLib(testCaseExecutionData, execution, testCaseStepActionExecution, testCaseCountryProperty, forceRecalculation);
//                            res = testCaseExecutionData.getPropertyResultMessage();
//                            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
//                            testCaseExecutionData.setPropertyResultMessage(res);
//                            logEventService.createForPrivateCalls("ENGINE", TestCaseCountryProperties.TYPE_EXECUTESOAPFROMLIB, MESSAGE_DEPRECATED + " Deprecated Property triggered by TestCase : ['" + test + "|" + testCase + "']");
//                            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Property " + TestCaseCountryProperties.TYPE_EXECUTESOAPFROMLIB + " triggered by TestCase : ['" + test + "'|'" + testCase + "']");
//                            break;
//                        case TestCaseCountryProperties.TYPE_EXECUTESQLFROMLIB: // DEPRECATED
//                            testCaseExecutionData = this.property_executeSqlFromLib(testCaseExecutionData, testCaseCountryProperty, execution, forceRecalculation);
//                            res = testCaseExecutionData.getPropertyResultMessage();
//                            res.setDescription(MESSAGE_DEPRECATED + " " + res.getDescription());
//                            testCaseExecutionData.setPropertyResultMessage(res);
//                            logEventService.createForPrivateCalls("ENGINE", TestCaseCountryProperties.TYPE_EXECUTESQLFROMLIB, MESSAGE_DEPRECATED + " Deprecated Property triggered by TestCase : ['" + test + "|" + testCase + "']");
//                            LOG.warn(MESSAGE_DEPRECATED + " Deprecated Property " + TestCaseCountryProperties.TYPE_EXECUTESQLFROMLIB + " triggered by TestCase : ['" + test + "'|'" + testCase + "']");
//                            break;
                        default:
                            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_UNKNOWNPROPERTY);
                            res.setDescription(res.getDescription().replace("%PROPERTY%", testCaseCountryProperty.getType()));
                            testCaseExecutionData.setPropertyResultMessage(res);
                    }
                    execution_count++;

                    // Adding secrets if property looks like a password
                    addPropertyASecret(testCaseExecutionData, execution);

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

                if (!StringUtil.isEmptyOrNull(testCaseExecutionDataFromCache.getJsonResult())) {

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
                        LOG.error(ex, ex);
                    }
                    testCaseExecutionData.setDataLibRawData(result);

                    // Adding secrets if property looks like a password
                    addPropertyASecret(testCaseExecutionData, execution);

                    //Record result in filessytem.
                    recorderService.recordTestDataLibProperty(execution.getId(), testCaseCountryProperty.getProperty(), 1, result, execution.getSecrets());

                }

            }

        } catch (CerberusEventException ex) {
            LOG.error(ex.toString(), ex);
            testCaseExecutionData.setEnd(new Date().getTime());
            testCaseExecutionData.setPropertyResultMessage(ex.getMessageError());
        }

        testCaseExecutionData.setEnd(new Date().getTime());

        LOG.debug("Finished to calculate Property : '" + testCaseCountryProperty.getProperty() + "'");
        execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Finished to calculate Property : '" + testCaseCountryProperty.getProperty() + "' with value '" + testCaseExecutionData.getValue() + "'");

    }

    private void addPropertyASecret(TestCaseExecutionData executionData, TestCaseExecution execution) {
        if (executionData.getProperty().contains("PASSW")) {
            execution.addSecret(executionData.getValue());
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
                if (!StringUtil.isEmptyOrNull(message)) {
                    value = message;
                }
                testCaseExecutionData.setValue(value);
                testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMCOMMAND).resolveDescription("VALUE", value));

            } else if (tCExecution.getAppTypeEngine().equals(Application.TYPE_IPA)) {
                String message = iosAppiumService.executeCommandString(tCExecution.getSession(), script, testCaseExecutionData.getValue2());

                String value = "";
                if (!StringUtil.isEmptyOrNull(message)) {
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
                if (!StringUtil.isEmptyOrNull(message)) {
                    value = message;
                }
                testCaseExecutionData.setValue(value);
                testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETELEMENTPOSITION).resolveDescription("VALUE", value));
            } else if (tCExecution.getAppTypeEngine().equals(Application.TYPE_IPA)) {
                String message = iosAppiumService.getElementPosition(tCExecution.getSession(), identifier);

                String value = "";
                if (!StringUtil.isEmptyOrNull(message)) {
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

    private TestCaseExecutionData property_getFromNetworkTraffic(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution execution, boolean forceCalculation) {
        if (RobotExecutor.PROXY_TYPE_NETWORKTRAFFIC.equalsIgnoreCase(execution.getRobotExecutorObj().getExecutorProxyType())) {
            String jsonPath = testCaseExecutionData.getValue2();
            if (StringUtil.isEmptyOrNull(jsonPath)) {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMNETWORKTRAFFIC_MISSINGJSONPATH);
                testCaseExecutionData.setPropertyResultMessage(res);
                return testCaseExecutionData;

            }

            try {
                //TODO : check if HAR is the same than the last one to avoid to download same har file several times
                // String remoteHarMD5 = "http://" + tCExecution.getRobotExecutorObj().getHost() + ":" + tCExecution.getRobotExecutorObj().getExecutorProxyServicePort() + "/getHarMD5?uuid="+tCExecution.getRemoteProxyUUID();
                Integer indexFrom = 0;
                if (!execution.getNetworkTrafficIndexList().isEmpty()) {
                    // Take the value from the last entry.
                    indexFrom = execution.getNetworkTrafficIndexList().get(execution.getNetworkTrafficIndexList().size() - 1).getIndexRequestNb();
                }

                JSONObject harRes = executorService.getHar(testCaseExecutionData.getValue1(), false, execution.getRobotExecutorObj().getExecutorProxyServiceHost(), execution.getRobotExecutorObj().getExecutorProxyServicePort(),
                        execution.getRemoteProxyUUID(), execution.getSystem(), indexFrom);

                harRes = harService.enrichWithStats(harRes, execution.getCountryEnvApplicationParam().getDomain(), execution.getSystem(), execution.getNetworkTrafficIndexList());

                //Record result in filessytem.
                testCaseExecutionData.addFileList(recorderService.recordProperty(execution.getId(), testCaseExecutionData.getProperty(), 1, harRes.toString(1), execution.getSecrets()));

                String valueFromJson = this.jsonService.getFromJson(execution, harRes.toString(), null, jsonPath,
                        testCaseExecutionData.getNature().equals(TestCaseCountryProperties.NATURE_RANDOM), testCaseExecutionData.getRank(), testCaseExecutionData.getValue3());

                if (valueFromJson != null) {
                    testCaseExecutionData.setValue(valueFromJson);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMNETWORKTRAFFIC)
                            .resolveDescription("PARAM", jsonPath)
                            .resolveDescription("VALUE", valueFromJson)
                            .resolveDescription("INDEX", String.valueOf(execution.getNetworkTrafficIndexList().size()))
                            .resolveDescription("NBHITS", String.valueOf(indexFrom));
                    testCaseExecutionData.setPropertyResultMessage(res);

                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMNETWORKTRAFFIC_PATHNOTFOUND);
                    res.setDescription(res.getDescription().replace("%PARAM%", jsonPath));
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
            res.setDescription(res.getDescription().replace("%ROBOT%", execution.getRobot()));
            res.setDescription(res.getDescription().replace("%EXECUTOR%", execution.getRobotExecutor()));
            testCaseExecutionData.setPropertyResultMessage(res);

        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getOTP(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution, boolean forceCalculation) {

        if (StringUtil.isEmptyOrNull(testCaseExecutionData.getValue1())) {
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETOTP_MISSINGPARAMETER);
            testCaseExecutionData.setPropertyResultMessage(res);
            testCaseExecutionData.setEnd(new Date().getTime());
            return testCaseExecutionData;
        }

        try {

            String secretKey = testCaseExecutionData.getValue1();
            Totp totp = new Totp(secretKey);

            String val = totp.now();
            testCaseExecutionData.setValue(val);
            testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETOTP).resolveDescription("VALUE", val));

        } catch (Exception ex) {
            LOG.warn("Exception when getting property from OTP secret.", ex);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETOTP);
            res.setDescription(res.getDescription().replace("%DETAIL%", ex.toString()));

            testCaseExecutionData.setPropertyResultMessage(res);

            testCaseExecutionData.setEnd(new Date().getTime());
            return testCaseExecutionData;
        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromExecutionObject(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution, boolean forceCalculation) {

        if (StringUtil.isEmptyOrNull(testCaseExecutionData.getValue1())) {
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETOTP_MISSINGPARAMETER);
            testCaseExecutionData.setPropertyResultMessage(res);
            testCaseExecutionData.setEnd(new Date().getTime());
            return testCaseExecutionData;
        }

        try {
            String executionObject = tCExecution.toJson(true).toString();

            //Record result in filesystem.
            recorderService.recordProperty(tCExecution.getId(), testCaseExecutionData.getProperty(), 1, executionObject, tCExecution.getSecrets());

            String valueFromJson = this.jsonService
                    .getFromJson(tCExecution, executionObject, null, testCaseExecutionData.getValue1(),
                            testCaseExecutionData.getNature().equals(TestCaseCountryProperties.NATURE_RANDOM), testCaseExecutionData.getRank(), testCaseExecutionData.getValue3());

            if (valueFromJson == null) {
                throw new InvalidPathException();
            }

            testCaseExecutionData.setValue(valueFromJson);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMJSON);
            res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%VALUE%", valueFromJson));
            testCaseExecutionData.setPropertyResultMessage(res);
        } catch (JsonProcessingException | InvalidPathException exception) { //Path not found, invalid path syntax or empty path
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
            res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%ERROR%", ""));
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
                String valueFromHTML = null;

                switch (testCaseCountryProperty.getValue3()) {
                    case (TestCaseCountryProperties.VALUE3_VALUE):
                        valueFromHTML = this.webdriverService.getValueFromHTML(tCExecution.getSession(), identifier, TestCaseCountryProperties.NATURE_RANDOM.equals(testCaseExecutionData.getNature()), testCaseExecutionData.getRank());
                        break;
                    case (TestCaseCountryProperties.VALUE3_COUNT):
                        valueFromHTML = String.valueOf(this.webdriverService.getNumberOfElements(tCExecution.getSession(), identifier));
                        break;
                    case (TestCaseCountryProperties.VALUE3_RAW):
                        valueFromHTML = this.webdriverService.getWebElement(tCExecution.getSession(), identifier, TestCaseCountryProperties.NATURE_RANDOM.equals(testCaseExecutionData.getNature()), testCaseExecutionData.getRank()).getItem().getAttribute("innerHTML").toString();
                        break;
                    case (TestCaseCountryProperties.VALUE3_COORDINATE):
                        valueFromHTML = this.webdriverService.getElementPosition(tCExecution.getSession(), identifier, TestCaseCountryProperties.NATURE_RANDOM.equals(testCaseExecutionData.getNature()), testCaseExecutionData.getRank());
                        break;
                    case (TestCaseCountryProperties.VALUE3_RAWLIST):
                        valueFromHTML = this.webdriverService.getElements(tCExecution.getSession(), identifier);
                        break;
                    case (TestCaseCountryProperties.VALUE3_VALUELIST):
                        valueFromHTML = this.webdriverService.getElementsValues(tCExecution.getSession(), identifier);
                        break;
                    case (TestCaseCountryProperties.VALUE3_VALUESUM):
                        valueFromHTML = this.webdriverService.getElementsValuesSum(tCExecution, identifier);
                        break;
                    case (TestCaseCountryProperties.VALUE3_ATTRIBUTE):
                        valueFromHTML = this.webdriverService.getAttributeFromHtml(tCExecution.getSession(), identifier, testCaseExecutionData.getValue2(), TestCaseCountryProperties.NATURE_RANDOM.equals(testCaseExecutionData.getNature()), testCaseExecutionData.getRank());
                        break;
                    default:
                        valueFromHTML = this.webdriverService.getValueFromHTML(tCExecution.getSession(), identifier, false, 0);
                }

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

    private TestCaseExecutionData property_getFromJS(TestCaseExecutionData executionData, TestCaseExecution execution, TestCaseCountryProperties testCaseCountryProperty, boolean forceCalculation) {
        String script = executionData.getValue1();
        String valueFromJS;
        String message = "";
        if (execution.getManualExecution().equals("Y")) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_NOTPOSSIBLE);
            executionData.setPropertyResultMessage(mes);
        } else {

            if (execution.getAppTypeEngine().equals(Application.TYPE_GUI)
                    || execution.getApplicationObj().getType().equals(Application.TYPE_GUI)) {
                try {
                    valueFromJS = this.webdriverService.getValueFromJS(execution.getSession(), script);
                } catch (Exception e) {
                    message = e.getMessage().split("\n")[0];
                    LOG.debug("Exception Running JS Script :" + message);
                    valueFromJS = null;
                }
                if (valueFromJS != null) {
                    executionData.setValue(valueFromJS);
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_JS);
                    res.setDescription(res.getDescription().replace("%SCRIPT%", script));
                    res.resolveDescription("VALUE", valueFromJS);
                    executionData.setPropertyResultMessage(res);
                } else {
                    MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_JS_EXCEPTION);
                    res.setDescription(res.getDescription().replace("%EXCEPTION%", message));
                    executionData.setPropertyResultMessage(res);
                }

            } else {
                MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_FEATURENOTSUPPORTED);
                res.setDescription(res.getDescription().replace("%APPTYPE%", execution.getAppTypeEngine()));
                res.setDescription(res.getDescription().replace("%PROPTYPE%", executionData.getType()));
                executionData.setPropertyResultMessage(res);
            }
        }
        return executionData;
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
            String valueFromHTML = this.webdriverService.getAttributeFromHtml(tCExecution.getSession(), identifier, testCaseExecutionData.getValue2(), TestCaseCountryProperties.NATURE_RANDOM.equals(testCaseExecutionData.getNature()), testCaseExecutionData.getRank());
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
                    decodedEnveloppe = answerDecode.getItem();
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
                    decodedServicePath = answerDecode.getItem();
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
                    decodedMethod = answerDecode.getItem();
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
                    decodedAttachement = answerDecode.getItem();
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
        if (!(StringUtil.isEmptyOrNull(testCaseExecutionData.getValue2()))) {
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

            String valueFromXml = "";
            switch (testCaseExecutionData.getValue3()) {
                case TestCaseCountryProperties.VALUE3_VALUE:
                    valueFromXml = xmlUnitService.getFromXml(xmlToParse, testCaseExecutionData.getValue1());
                    break;
                case TestCaseCountryProperties.VALUE3_RAW:
                    valueFromXml = xmlUnitService.getRawFromXml(xmlToParse, testCaseExecutionData.getValue1());
                    break;
                default:
                    valueFromXml = xmlUnitService.getFromXml(xmlToParse, testCaseExecutionData.getValue1());
                    break;
            }

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
        if (!(StringUtil.isEmptyOrNull(testCaseExecutionData.getValue2()))) {
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

    private TestCaseExecutionData property_getFromJson(TestCaseExecutionData testCaseExecutionData, TestCaseExecution execution, TestCaseCountryProperties testCaseCountryProperty, boolean forceRecalculation) {
        String jsonResponse = "";

        if (null != execution.getLastServiceCalled()) {
            jsonResponse = execution.getLastServiceCalled().getResponseHTTPBody();
        }

        if (!(StringUtil.isEmptyOrNull(testCaseExecutionData.getValue2()))) {
            try {
                jsonResponse = this.jsonService.callUrlAndGetJsonResponse(testCaseExecutionData.getValue2());

            } catch (MalformedURLException e) {
                LOG.debug("URL is invalid so we consider that it is a json file.");
                jsonResponse = testCaseExecutionData.getValue2();
            }
        }

        try {
            //Record result in filessytem.
            recorderService.recordProperty(execution.getId(), testCaseExecutionData.getProperty(), 1, jsonResponse, execution.getSecrets());

            String valueFromJSON = this.jsonService
                    .getFromJson(execution, jsonResponse, null, testCaseExecutionData.getValue1(),
                            testCaseExecutionData.getNature().equals(TestCaseCountryProperties.NATURE_RANDOM), testCaseExecutionData.getRank(), testCaseExecutionData.getValue3());

            if (valueFromJSON == null) {
                throw new InvalidPathException();
            }

            testCaseExecutionData.setValue(valueFromJSON);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMJSON);
            res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%VALUE%", valueFromJSON));
            testCaseExecutionData.setPropertyResultMessage(res);
        } catch (JsonProcessingException | InvalidPathException exception) { //Path not found, invalid path syntax or empty path
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
            res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%ERROR%", ""));
            testCaseExecutionData.setPropertyResultMessage(res);
        }

        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getRawFromJson(TestCaseExecutionData testCaseExecutionData, TestCaseExecution execution) {
        String jsonResponse = "";

        //If tCExecution LastServiceCalled exist, get the response
        if (execution.getLastServiceCalled() != null) {
            jsonResponse = execution.getLastServiceCalled().getResponseHTTPBody();
        }

        if (!(StringUtil.isEmptyOrNull(testCaseExecutionData.getValue2()))) {
            try {
                jsonResponse = this.jsonService.callUrlAndGetJsonResponse(testCaseExecutionData.getValue2());

            } catch (MalformedURLException e) {
                LOG.debug("URL is invalid so we consider that it is a json file.");
                jsonResponse = testCaseExecutionData.getValue2();
            }
        }

        //Process
        try {
            //Record result in filesystem.
            recorderService.recordProperty(execution.getId(), testCaseExecutionData.getProperty(), 1, jsonResponse, execution.getSecrets());

            //Get the raw
            String valueFromJson = this.jsonService.getRawFromJson(jsonResponse, testCaseExecutionData.getValue1());

            testCaseExecutionData.setValue(valueFromJson);
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMJSON);
            res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%VALUE%", valueFromJson));
            testCaseExecutionData.setPropertyResultMessage(res);

        } catch (JsonProcessingException | InvalidPathException exception) { //Path not found, invalid path syntax or empty path
            if (LOG.isDebugEnabled()) {
                LOG.error("Exception when getting property from JSON.", exception);
            }
            MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMJSON_PARAMETERNOTFOUND);
            res.setDescription(res.getDescription().replace("%URL%", testCaseExecutionData.getValue2()));
            res.setDescription(res.getDescription().replace("%PARAM%", testCaseExecutionData.getValue1()));
            res.setDescription(res.getDescription().replace("%ERROR%", ""));
            testCaseExecutionData.setPropertyResultMessage(res);
        }
        return testCaseExecutionData;
    }

    private TestCaseExecutionData property_getFromDataLib(TestCaseExecutionData executionData, TestCaseExecution execution,
            TestCaseStepActionExecution actionExecution, TestCaseCountryProperties property, boolean forceRecalculation) {

        MessageEvent res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB);

        TestDataLib testDataLib;
        List<HashMap<String, String>> result = null;
        AnswerItem<String> answerDecode = new AnswerItem<>();

        // We get here the correct TestDataLib entry from the Value1 (name) that better match the context on system, environment and country.
        AnswerItem<TestDataLib> answer = testDataLibService.readByNameBySystemByEnvironmentByCountry(executionData.getValue1(),
                execution.getApplicationObj().getSystem(), execution.getEnvironmentData(),
                execution.getCountry());

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answer.getItem() != null) {
            testDataLib = answer.getItem();

            AnswerList<HashMap<String, String>> serviceAnswer;

            // Here, we try to decode the SQL field if datalib is SQL type.
            try {
                if (testDataLib.getType().equals(TestDataLib.TYPE_SQL)) {
                    //check if the script contains properties that neeed to be calculated
                    answerDecode = variableService.decodeStringCompletly(testDataLib.getScript(), execution, actionExecution, false);
                    String decodedScript = answerDecode.getItem();
                    testDataLib.setScript(decodedScript);
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        executionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "SQL Script"));
                        executionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        LOG.debug("Property interupted due to decode 'SQL Script'.");
                        return executionData;
                    }
                }
            } catch (CerberusEventException cex) {
                LOG.error(cex.toString(), cex);
            }

            String decodedLength = null;

            // Here, we try to decode testCaseCountryProperty field `length` to get the value of property if needed
            try {
                answerDecode = variableService.decodeStringCompletly(property.getLength(), execution, actionExecution, false);
                decodedLength = answerDecode.getItem();
                if (!(answerDecode.isCodeStringEquals("OK"))) {
                    executionData.setPropertyResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "length"));
                    executionData.setStopExecution(answerDecode.getResultMessage().isStopTest());
                    LOG.debug("Property interupted due to decode 'Length field'.");
                    return executionData;
                }
            } catch (CerberusEventException cex) {
                LOG.error(cex.toString(), cex);
            }

            // We cast from string to integer ((String)testcasecountryproperty field `length` -> (Integer)testcaseexecutiondata field `length`)
            // if we can't, testCaseExecutionData field `length` will be equal to 0
            // if we can, we set the value of testCaseExecutionData field `length` to the casted value
            if (decodedLength != null) {
                try {
                    if (StringUtil.isEmptyOrNull(decodedLength)) {
                        decodedLength = "0";
                    }
                    Integer.parseInt(decodedLength);
                    executionData.setLength(decodedLength);

                } catch (NumberFormatException e) {
                    LOG.error(e.toString(), e);
                    MessageEvent msg = new MessageEvent(MessageEventEnum.CASTING_OPERATION_FAILED);
                    msg.setDescription(msg.getDescription().replace("%ERROR%", e.toString()));
                    msg.setDescription(msg.getDescription().replace("%FIELD%", "length of property " + property.getProperty()));
                    executionData.setPropertyResultMessage(msg);
                    executionData.setStopExecution(msg.isStopTest());
                    return executionData;
                }
            }

            // We calculate here the result for the lib
            serviceAnswer = dataLibService.getFromDataLib(testDataLib, property, execution, executionData);
            executionData.setDataLib(testDataLib.getName());
            executionData.setDataLibObj(testDataLib);

            res = serviceAnswer.getResultMessage();
            result = serviceAnswer.getDataList(); //test data library returned by the service

            if (result != null) {
                // Keeping raw data to testCaseExecutionData object.
                executionData.setDataLibRawData(result);

                // Value of testCaseExecutionData object takes the master subdata entry "".
                String value = result.get(0).get("");
                if (value == null) {
                    final boolean ignoreNonMatchedSubdata = parameterService.getParameterBooleanByKey("cerberus_testdatalib_ignoreNonMatchedSubdata", StringUtils.EMPTY, false);
                    if (ignoreNonMatchedSubdata) {
                        final String defaultSubdataValue = ignoreNonMatchedSubdata ? parameterService.getParameterStringByKey("cerberus_testdatalib_subdataDefaultValue", StringUtils.EMPTY, StringUtils.EMPTY) : StringUtils.EMPTY;
                        LOG.debug("Unmatched columns parsing enabled: Null answer received from service call of datalib '{}' with default value", () -> testDataLib.getName());
                        executionData.setValue(defaultSubdataValue);
                    } else {
                        executionData.setValue(VALUE_NULL);
                    }
                } else {
                    executionData.setValue(value);
                    // Converting HashMap to json.
                    String jsonText = "";
                    JSONArray jsonResult = null;
                    try {
                        jsonResult = dataLibService.convertToJSONObject(result);
                        jsonText = jsonResult.toString();
                    } catch (JSONException ex) {
                        LOG.error(ex, ex);
                    }

                    executionData.setJsonResult(jsonText);
                }

                //Record result in filessytem.
                executionData.addFileList(recorderService.recordTestDataLibProperty(execution.getId(), property.getProperty(), 1, result, execution.getSecrets()));

            }

            res.resolveDescription("ENTRY", testDataLib.getName());
            res.resolveDescription("ENTRYID", String.valueOf(testDataLib.getTestDataLibID()));

        } else {//no TestDataLib found was returned
            //the library does not exist at all
            AnswerList nameExistsAnswer = testDataLibService.readNameListByName(executionData.getValue1(), 1, false);
            if (nameExistsAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && nameExistsAnswer.getTotalRows() > 0) {
                //if the library name exists but was not available or does not exist for the current specification but exists for other countries/environments/systems
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOT_FOUND_ERROR);
                res.setDescription(res.getDescription().replace("%ITEM%", executionData.getValue1()).
                        replace("%COUNTRY%", execution.getCountryEnvApplicationParam().getCountry()).
                        replace("%ENVIRONMENT%", execution.getCountryEnvApplicationParam().getEnvironment()).
                        replace("%SYSTEM%", execution.getCountryEnvApplicationParam().getSystem()));
            } else {
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOT_EXIST_ERROR);
                res.setDescription(res.getDescription().replace("%ITEM%", executionData.getValue1()));
            }

        }
        res.setDescription(res.getDescription().replace("%VALUE1%", executionData.getValue1()));
        executionData.setPropertyResultMessage(res);

        return executionData;
    }

}
