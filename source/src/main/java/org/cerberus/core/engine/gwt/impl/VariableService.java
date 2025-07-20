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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.crud.entity.TestCaseStepExecution;
import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.engine.gwt.IVariableService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;

/**
 * Created by corentin on 20/10/16.
 */
@Service
public class VariableService implements IVariableService {

    private static final Logger LOG = LogManager.getLogger(VariableService.class);

    private static final String VALUE_WHEN_NULL = "<null>";
    // TIPS : Test online your java regex using : https://www.regexplanet.com/advanced/java/index.html
    public static final Pattern SYSTEM_VARIABLE_DATE_PATTERN = Pattern.compile("%system.([a-zA-Z0-9-+]*)-([a-z A-Z0-9.,:;'\"$]*)%");
    public static final Pattern SYSTEM_SUBVARIABLE_DATE_PATTERN = Pattern.compile("([a-zA-Z]*)([-+])([0-9]*)");

    @Autowired
    private PropertyService propertyService;
    @Autowired
    private ApplicationObjectVariableService applicationObjectVariableService;
    @Autowired
    private IRecorderService recorderService;

    @Override
    public AnswerItem<String> decodeStringCompletly(String initStringToDecode, TestCaseExecution testCaseExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {

        MessageEvent msg = new MessageEvent(MessageEventEnum.DECODE_SUCCESS);
        AnswerItem<String> answer = new AnswerItem<>();
        answer.setResultMessage(msg);
        answer.setItem(initStringToDecode);

        String stringToDecode = initStringToDecode;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start Decoding : " + stringToDecode);
        }

        /**
         * Nothing to decode if null or empty string.
         */
        if (StringUtil.isEmptyOrNull(stringToDecode)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stop Decoding : Nothing to decode on : " + stringToDecode);
            }
            return answer;
        }
        if (stringToDecode.startsWith(Identifier.IDENTIFIER_ERRATUM + "=")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stop Decoding : String starts by erratum= : " + stringToDecode);
            }
            return answer;
        }

        int count_decode = 1;
        while (stringToDecode.contains("%") && count_decode <= 2) {
            /**
             * We iterate the property decode because properties names could be
             * inside other properties.
             */
            /**
             * Decode System Variables.
             */
            if (stringToDecode.contains("%")) {
                LOG.debug("Starting to decode (system variable) string iteration#" + count_decode + ": " + stringToDecode);
                stringToDecode = this.decodeStringWithSystemVariable(stringToDecode, testCaseExecution);
                LOG.debug("Finished to decode (system variable) iteration#" + count_decode + ". Result : " + stringToDecode);
            } else {
                LOG.debug("Stop Decoding : No more things to decode on (exit when trying to decode System variable) : " + stringToDecode);
                answer.setItem(stringToDecode);
                return answer;
            }

            /**
             * Decode ApplicationObject.
             */
            if (stringToDecode.contains("%")) {
                LOG.debug("Starting to decode (Application Object) string iteration#" + count_decode + ": " + stringToDecode);
                stringToDecode = applicationObjectVariableService.decodeStringWithApplicationObject(stringToDecode, testCaseExecution, forceCalculation);
                LOG.debug("Finished to decode (Application Object) iteration#" + count_decode + ". Result : " + stringToDecode);
            } else {
                LOG.debug("Stop Decoding : No more things to decode on (exit when trying to decode ApplicationObject variable) : " + stringToDecode);
                answer.setItem(stringToDecode);
                return answer;
            }

            /**
             * Decode Datalib.
             */
            if (stringToDecode.contains("%")) {
                if (stringToDecode.contains("%datalib.")) {
                    LOG.debug("Starting to decode (Datalib) string iteration#" + count_decode + ": " + stringToDecode);
                    stringToDecode = propertyService.decodeStringWithDatalib(stringToDecode, testCaseExecution, forceCalculation);
                    LOG.debug("Finished to decode (Datalib) iteration#" + count_decode + ". Result : " + stringToDecode);
                }
            } else {
                LOG.debug("Stop Decoding : No more things to decode on (exit when trying to decode Datalib variable) : " + stringToDecode);
                answer.setItem(stringToDecode);
                return answer;
            }

            /**
             * Decode Properties.
             */
            LOG.debug("Starting to decode (Properties) string  iteration#" + count_decode + " : " + stringToDecode);
            answer = propertyService.decodeStringWithExistingProperties(stringToDecode, testCaseExecution, testCaseStepActionExecution, forceCalculation);
            stringToDecode = answer.getItem();
            LOG.debug("Finished to decode (Properties) iteration#" + count_decode + ". Result : " + stringToDecode);
            LOG.debug("   Result Message : " + answer.getResultMessage().getCodeString() + " - " + answer.getResultMessage().getDescription());

            /**
             *
             * if the property result message indicates that we need to stop the
             * test action, then the action is notified or if the property was
             * not successfully calculated, either because it was not defined
             * for the country or because it does not exist then we notify the
             * execution.
             */
            if (answer.getResultMessage().getCodeString().equals("FA")
                    || answer.getResultMessage().getCodeString().equals("NA")) {
                String prop_message = answer.getResultMessage().getDescription();
                answer.setResultMessage(new MessageEvent(MessageEventEnum.DECODE_FAILED_GENERIC)
                        .resolveDescription("ERROR", prop_message));
                answer.setItem(stringToDecode);
                return answer;
            }
            count_decode++;
        }

        // Checking if after the decode we still have some variable not decoded.
        LOG.debug("Checking If after decode we still have uncoded variable.");
        List<String> variableList = getVariableListFromString(stringToDecode);
        if (variableList.size() > 0) {
            String messageList = "";
            for (String var : variableList) {
                messageList += var + " ,";
            }
            messageList = StringUtil.removeLastChar(messageList);
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DECODE_FAILED_VARIABLENOTDECODED)
                    .resolveDescription("NB", String.valueOf(variableList.size()))
                    .resolveDescription("VAR", messageList));
            answer.setItem(stringToDecode);
            LOG.debug("Stop Decoding with error : " + answer.getResultMessage().getCodeString() + " - " + answer.getResultMessage().getDescription());
            return answer;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Stop Decoding : All iteration finished : " + stringToDecode);
        }
        answer.setItem(stringToDecode);
        return answer;
    }

    private List<String> getVariableListFromString(String str) {
        List<String> variable = new ArrayList<>();

        final String regex = "%(property|system|object|service|datalib)\\..*?%";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            LOG.debug("Full match: " + matcher.group());
            variable.add(matcher.group());
        }

        return variable;
    }

    @Override
    public String decodeStringWithSystemVariable(String stringToDecode, TestCaseExecution execution) {

        try {
            CountryEnvironmentParameters envappli;
            if (execution.getCurrentApplication() != null) {
                envappli = execution.getCountryEnvApplicationParams().getOrDefault(execution.getCurrentApplication(), execution.getCountryEnvApplicationParam());
            } else {
                envappli = execution.getCountryEnvApplicationParam();
            }
            /**
             * Trying to replace by system environment variables from Execution.
             */
            stringToDecode = stringToDecode.replace("%SYS_SYSTEM%", execution.getApplicationObj().getSystem());
            stringToDecode = stringToDecode.replace("%SYS_APPLI%", execution.getApplicationObj().getApplication());
            stringToDecode = stringToDecode.replace("%SYS_BROWSER%", execution.getBrowser());
            stringToDecode = stringToDecode.replace("%SYS_ROBOT%", execution.getRobot());
            stringToDecode = stringToDecode.replace("%SYS_ROBOTDECLI%", execution.getRobotDecli());
            stringToDecode = stringToDecode.replace("%SYS_SCREENSIZE%", execution.getScreenSize());
            stringToDecode = stringToDecode.replace("%SYS_APP_DOMAIN%", envappli.getDomain().split(",")[0].trim());
            stringToDecode = stringToDecode.replace("%SYS_APP_HOST%", envappli.getIp());
            stringToDecode = stringToDecode.replace("%SYS_APP_CONTEXTROOT%", envappli.getUrl());
            stringToDecode = stringToDecode.replace("%SYS_APP_VAR1%", envappli.getVar1());
            stringToDecode = stringToDecode.replace("%SYS_APP_VAR2%", envappli.getVar2());
            stringToDecode = stringToDecode.replace("%SYS_APP_VAR3%", envappli.getVar3());
            stringToDecode = stringToDecode.replace("%SYS_APP_VAR4%", envappli.getVar4());
            stringToDecode = stringToDecode.replace("%SYS_EXEURL%", execution.getUrl());
            stringToDecode = stringToDecode.replace("%SYS_ENV%", execution.getEnvironmentData());
            stringToDecode = stringToDecode.replace("%SYS_ENVGP%", execution.getEnvironmentDataObj().getGp1());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRY%", execution.getCountry());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP1%", execution.getCountryObj().getGp1());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP2%", execution.getCountryObj().getGp2());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP3%", execution.getCountryObj().getGp3());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP4%", execution.getCountryObj().getGp4());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP5%", execution.getCountryObj().getGp5());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP6%", execution.getCountryObj().getGp6());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP7%", execution.getCountryObj().getGp7());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP8%", execution.getCountryObj().getGp8());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP9%", execution.getCountryObj().getGp9());
            stringToDecode = stringToDecode.replace("%SYS_TEST%", execution.getTest());
            stringToDecode = stringToDecode.replace("%SYS_TESTCASE%", execution.getTestCase());
            stringToDecode = stringToDecode.replace("%SYS_TESTCASEDESCRIPTION%", execution.getDescription());
            stringToDecode = stringToDecode.replace("%SYS_SSIP%", execution.getSeleniumIP());
            stringToDecode = stringToDecode.replace("%SYS_SSPORT%", execution.getSeleniumPort());
            stringToDecode = stringToDecode.replace("%SYS_TAG%", execution.getTag());
            stringToDecode = stringToDecode.replace("%SYS_EXECUTIONID%", String.valueOf(execution.getId()));
            stringToDecode = stringToDecode.replace("%SYS_EXESTART%", String.valueOf(new Timestamp(execution.getStart())));
            stringToDecode = stringToDecode.replace("%SYS_EXESTORAGEURL%", recorderService.getStorageSubFolderURL(execution.getId()));
            long nowInMS = new Date().getTime();
            stringToDecode = stringToDecode.replace("%SYS_EXEELAPSEDMS%", String.valueOf(nowInMS - execution.getStart()));
            // New syntax
            stringToDecode = stringToDecode.replace("%system.SYSTEM%", execution.getApplicationObj().getSystem());
            stringToDecode = stringToDecode.replace("%system.APPLI%", execution.getApplicationObj().getApplication());
            stringToDecode = stringToDecode.replace("%system.BROWSER%", execution.getBrowser());
            stringToDecode = stringToDecode.replace("%system.ROBOT%", execution.getRobot());
            stringToDecode = stringToDecode.replace("%system.ROBOTDECLI%", execution.getRobotDecli());
            stringToDecode = stringToDecode.replace("%system.ROBOTSESSIONID%", execution.getRobotSessionID());
            stringToDecode = stringToDecode.replace("%system.ROBOTPROVIDERSESSIONID%", execution.getRobotProviderSessionID());
            if (execution.getRobotExecutorObj() != null) {
                stringToDecode = stringToDecode.replace("%system.ROBOTHOST%", execution.getRobotExecutorObj().getHost());
            }

            stringToDecode = stringToDecode.replace("%system.SCREENSIZE%", execution.getScreenSize());
            stringToDecode = stringToDecode.replace("%system.APP_DOMAIN%", envappli.getDomain().split(",")[0].trim());
            stringToDecode = stringToDecode.replace("%system.APP_HOST%", envappli.getIp());
            stringToDecode = stringToDecode.replace("%system.APP_CONTEXTROOT%", envappli.getUrl());
            stringToDecode = stringToDecode.replace("%system.APP_VAR1%", envappli.getVar1());
            stringToDecode = stringToDecode.replace("%system.APP_VAR2%", envappli.getVar2());
            stringToDecode = stringToDecode.replace("%system.APP_VAR3%", envappli.getVar3());
            stringToDecode = stringToDecode.replace("%system.APP_VAR4%", envappli.getVar4());
            stringToDecode = stringToDecode.replace("%system.APP_SECRET1%", envappli.getSecret1());
            stringToDecode = stringToDecode.replace("%system.APP_SECRET2%", envappli.getSecret2());
            stringToDecode = stringToDecode.replace("%system.EXEURL%", execution.getUrl());
            stringToDecode = stringToDecode.replace("%system.ENV%", execution.getEnvironmentData());
            stringToDecode = stringToDecode.replace("%system.ENVGP%", execution.getEnvironmentDataObj().getGp1());
            stringToDecode = stringToDecode.replace("%system.COUNTRY%", execution.getCountry());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP1%", execution.getCountryObj().getGp1());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP2%", execution.getCountryObj().getGp2());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP3%", execution.getCountryObj().getGp3());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP4%", execution.getCountryObj().getGp4());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP5%", execution.getCountryObj().getGp5());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP6%", execution.getCountryObj().getGp6());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP7%", execution.getCountryObj().getGp7());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP8%", execution.getCountryObj().getGp8());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP9%", execution.getCountryObj().getGp9());
            stringToDecode = stringToDecode.replace("%system.TEST%", execution.getTest());
            stringToDecode = stringToDecode.replace("%system.TESTCASE%", execution.getTestCase());
            stringToDecode = stringToDecode.replace("%system.TESTCASEDESCRIPTION%", execution.getDescription());
            stringToDecode = stringToDecode.replace("%system.SSIP%", execution.getSeleniumIP());
            stringToDecode = stringToDecode.replace("%system.SSPORT%", execution.getSeleniumPort());
            stringToDecode = stringToDecode.replace("%system.TAG%", execution.getTag());
            stringToDecode = stringToDecode.replace("%system.EXECUTIONID%", String.valueOf(execution.getId()));
            stringToDecode = stringToDecode.replace("%system.EXESTART%", String.valueOf(new Timestamp(execution.getStart())));
            stringToDecode = stringToDecode.replace("%system.EXESTORAGEURL%", recorderService.getStorageSubFolderURL(execution.getId()));
            nowInMS = new Date().getTime();
            stringToDecode = stringToDecode.replace("%system.EXEELAPSEDMS%", String.valueOf(nowInMS - execution.getStart()));
            if (execution.getRemoteProxyUUID() != null) {
                stringToDecode = stringToDecode.replace("%system.REMOTEPROXYUUID%", execution.getRemoteProxyUUID());
            }
            if (execution.getRobotExecutorObj() != null) {
                stringToDecode = stringToDecode.replace("%system.REMOTEPROXY_HAR_URL%", "http://" + execution.getRobotExecutorObj().getExecutorProxyServiceHost() + ":" + execution.getRobotExecutorObj().getExecutorProxyServicePort() + "/getHar?uuid=" + execution.getRemoteProxyUUID());
            }
            /**
             * Trying to replace by system environment variables from Step
             * Execution .
             */
            if (execution.getTestCaseStepExecutionList() != null) {

                if (execution.getTestCaseStepExecutionList().size() > 0) {
                    stringToDecode = stringToDecode.replace("%system.CURRENTSTEP_SORT%", String.valueOf(execution.getTestCaseStepExecutionList().get(execution.getTestCaseStepExecutionList().size() - 1).getSort()));
                    stringToDecode = stringToDecode.replace("%SYS_CURRENTSTEP_SORT%", String.valueOf(execution.getTestCaseStepExecutionList().get(execution.getTestCaseStepExecutionList().size() - 1).getSort()));

                    // %SYS_CURRENTSTEP_INDEX%
                    if (stringToDecode.contains("%SYS_CURRENTSTEP_")) {
                        TestCaseStepExecution currentStep = execution.getTestCaseStepExecutionList().get(execution.getTestCaseStepExecutionList().size() - 1);
                        stringToDecode = stringToDecode.replace("%SYS_CURRENTSTEP_INDEX%", String.valueOf(currentStep.getIndex()));
                        stringToDecode = stringToDecode.replace("%SYS_CURRENTSTEP_STARTISO%", new Timestamp(currentStep.getStart()).toString());
                        nowInMS = new Date().getTime();
                        stringToDecode = stringToDecode.replace("%SYS_CURRENTSTEP_ELAPSEDMS%", String.valueOf(nowInMS - currentStep.getFullStart()));

                    }
                    if (stringToDecode.contains("%system.CURRENTSTEP_")) {
                        TestCaseStepExecution currentStep = execution.getTestCaseStepExecutionList().get(execution.getTestCaseStepExecutionList().size() - 1);
                        stringToDecode = stringToDecode.replace("%system.CURRENTSTEP_INDEX%", String.valueOf(currentStep.getIndex()));
                        stringToDecode = stringToDecode.replace("%system.CURRENTSTEP_STARTISO%", new Timestamp(currentStep.getStart()).toString());
                        nowInMS = new Date().getTime();
                        stringToDecode = stringToDecode.replace("%system.CURRENTSTEP_ELAPSEDMS%", String.valueOf(nowInMS - currentStep.getFullStart()));
                    }
                }

                // %SYS_STEP.n.RETURNCODE%
                if (stringToDecode.contains("%SYS_STEP.")) {
                    String syntaxToReplace = "";
                    for (Object testCaseStepExecution : execution.getTestCaseStepExecutionList()) {
                        TestCaseStepExecution tcse = (TestCaseStepExecution) testCaseStepExecution;
                        syntaxToReplace = "%SYS_STEP." + tcse.getSort() + "." + tcse.getIndex() + ".RETURNCODE%";
                        stringToDecode = stringToDecode.replace(syntaxToReplace, tcse.getReturnCode());
                    }
                }
                if (stringToDecode.contains("%system.STEP.")) {
                    String syntaxToReplace = "";
                    for (Object testCaseStepExecution : execution.getTestCaseStepExecutionList()) {
                        TestCaseStepExecution tcse = (TestCaseStepExecution) testCaseStepExecution;
                        syntaxToReplace = "%system.STEP." + tcse.getSort() + "." + tcse.getIndex() + ".RETURNCODE%";
                        stringToDecode = stringToDecode.replace(syntaxToReplace, tcse.getReturnCode());
                    }
                }

            }

            /**
             * Last Service Called Variables.
             */
            if (!(execution.getLastServiceCalled() == null)) {
                stringToDecode = stringToDecode.replace("%system.LASTSERVICE_HTTPCODE%", String.valueOf(execution.getLastServiceCalled().getResponseHTTPCode()));
                stringToDecode = stringToDecode.replace("%system.LASTSERVICE_CALL%", execution.getLastServiceCalled().toJSONOnDefaultExecution().toString());
                if (execution.getLastServiceCalled().getEnd().getTime() > execution.getLastServiceCalled().getStart().getTime()) {
                    stringToDecode = stringToDecode.replace("%system.LASTSERVICE_RESPONSETIME%",
                            String.valueOf(execution.getLastServiceCalled().getEnd().getTime() - execution.getLastServiceCalled().getStart().getTime()));
                } else {
                    stringToDecode = stringToDecode.replace("%system.LASTSERVICE_RESPONSETIME%", VALUE_WHEN_NULL);
                }
                if (!(execution.getLastServiceCalled().getResponseHTTPBody() == null)) {
                    stringToDecode = stringToDecode.replace("%system.LASTSERVICE_RESPONSE%", execution.getLastServiceCalled().getResponseHTTPBody());
                } else {
                    stringToDecode = stringToDecode.replace("%system.LASTSERVICE_RESPONSE%", VALUE_WHEN_NULL);
                }
            } else {
                stringToDecode = stringToDecode.replace("%system.LASTSERVICE_HTTPCODE%", VALUE_WHEN_NULL);
                stringToDecode = stringToDecode.replace("%system.LASTSERVICE_CALL%", VALUE_WHEN_NULL);
                stringToDecode = stringToDecode.replace("%system.LASTSERVICE_RESPONSE%", VALUE_WHEN_NULL);
                stringToDecode = stringToDecode.replace("%system.LASTSERVICE_RESPONSETIME%", VALUE_WHEN_NULL);
            }
            if (!(execution.getLastServiceCalled() == null)) {
                stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_HTTPCODE%", String.valueOf(execution.getLastServiceCalled().getResponseHTTPCode()));
                stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_CALL%", execution.getLastServiceCalled().toJSONOnDefaultExecution().toString());
                if (!(execution.getLastServiceCalled().getResponseHTTPBody() == null)) {
                    stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_RESPONSE%", execution.getLastServiceCalled().getResponseHTTPBody());
                } else {
                    stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_RESPONSE%", VALUE_WHEN_NULL);
                }
            } else {
                stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_HTTPCODE%", VALUE_WHEN_NULL);
                stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_CALL%", VALUE_WHEN_NULL);
                stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_RESPONSE%", VALUE_WHEN_NULL);
            }

            /**
             * Trying to replace date variables .
             */
            stringToDecode = decodeStringWithDateVariable(stringToDecode, execution.getCountryObj().getGp2());

            return stringToDecode;

        } catch (Exception e) {
            LOG.error("Error when decoding system variable on exe : " + execution.getId(), e);
            LOG.error(e, e);
        }
        return stringToDecode;
    }

    public String decodeStringWithDateVariable(String stringToDecode, String locale) {

        Matcher variableMatcher = SYSTEM_VARIABLE_DATE_PATTERN.matcher(stringToDecode);
        Matcher subVariableMatcher = null;

        if (variableMatcher.find()) {
            SimpleDateFormat formater = null; // Define the MySQL Format.

            do {

                Date today = new Date(); // Getting now.
                Calendar cal = Calendar.getInstance();
                cal.setTime(today);

                switch (variableMatcher.group(1)) {
                    case "TODAY":
                        break;
                    case "TOMORROW":
                        cal.add(Calendar.HOUR, +24);
                        break;
                    case "YESTERDAY":
                        cal.add(Calendar.HOUR, -24);
                        break;
                    default:
                        try {

                        subVariableMatcher = SYSTEM_SUBVARIABLE_DATE_PATTERN.matcher(variableMatcher.group(1));
                        if (subVariableMatcher.matches()) {
                            LOG.debug("Time Unit " + subVariableMatcher.group(1) + " Offset " + subVariableMatcher.group(2) + subVariableMatcher.group(3));
                            switch (subVariableMatcher.group(1)) {
                                case "YEAR":
                                    cal.add(Calendar.YEAR, Integer.parseInt(subVariableMatcher.group(2) + subVariableMatcher.group(3)));
                                    break;
                                case "MONTH":
                                    cal.add(Calendar.MONTH, Integer.parseInt(subVariableMatcher.group(2) + subVariableMatcher.group(3)));
                                    break;
                                case "WEEK":
                                    cal.add(Calendar.WEEK_OF_YEAR, Integer.parseInt(subVariableMatcher.group(2) + subVariableMatcher.group(3)));
                                    break;
                                case "DAY":
                                    cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(subVariableMatcher.group(2) + subVariableMatcher.group(3)));
                                    break;
                                case "HOUR":
                                    cal.add(Calendar.HOUR, Integer.parseInt(subVariableMatcher.group(2) + subVariableMatcher.group(3)));
                                    break;
                                case "MINUTE":
                                    cal.add(Calendar.MINUTE, Integer.parseInt(subVariableMatcher.group(2) + subVariableMatcher.group(3)));
                                    break;
                                default:
                                    return stringToDecode.replace(variableMatcher.group(0), "[!!System Date decode error - Unknown Time Unit in '" + subVariableMatcher.group(1) + "' adding '" + subVariableMatcher.group(2) + subVariableMatcher.group(3) + "'!!]");
                            }
                        }

                    } catch (Exception e) {
                        LOG.warn("Warning when trying to decode a date system variable.", e);
                        return stringToDecode.replace(variableMatcher.group(0), "[!!System Date decode error - " + e.getMessage() + " in '" + subVariableMatcher.group(1) + "' adding '" + subVariableMatcher.group(2) + subVariableMatcher.group(3) + "'!!]");
                    }
                }

                String errorMess = "";
                try {
                    if (StringUtil.isNotEmptyOrNull(locale)) {
                        errorMess = " in '" + variableMatcher.group(2) + "' with locale " + locale.split("-")[0];
                        LOG.debug("Decode Date Format : " + variableMatcher.group(2) + " with locale " + locale.split("-")[0]);
                        formater = new SimpleDateFormat(variableMatcher.group(2), new Locale(locale.split("-")[0]));
                    } else {
                        errorMess = " in '" + variableMatcher.group(2) + "'";
                        LOG.debug("Decode Date Format : " + variableMatcher.group(2));
                        formater = new SimpleDateFormat(variableMatcher.group(2));
                    }
                    stringToDecode = stringToDecode.replace(variableMatcher.group(0), formater.format(cal.getTime()));
                } catch (Exception e) {
                    stringToDecode = stringToDecode.replace(variableMatcher.group(0), "[!!System Date decode error - " + e.getMessage() + errorMess + "!!]");
                    LOG.warn("Warning when trying to decode a date system variable.", e);
                }

            } while (variableMatcher.find());

        }

        return stringToDecode;
    }

}
