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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.util.DateUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by corentin on 20/10/16.
 */
@Service
public class VariableService implements IVariableService {

    private static final Logger LOG = LogManager.getLogger(VariableService.class);

    private static final String VALUE_WHEN_NULL = "<null>";

    @Autowired
    private PropertyService propertyService;
    @Autowired
    private ApplicationObjectVariableService applicationObjectVariableService;
    @Autowired
    private IRecorderService recorderService;

    @Override
    public AnswerItem<String> decodeStringCompletly(String stringToDecode, TestCaseExecution testCaseExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {

        MessageEvent msg = new MessageEvent(MessageEventEnum.DECODE_SUCCESS);
        AnswerItem<String> answer = new AnswerItem<>();
        answer.setResultMessage(msg);
        answer.setItem(stringToDecode);

        String result = stringToDecode;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start Decoding : " + result);
        }

        /**
         * Nothing to decode if null or empty string.
         */
        if (StringUtil.isNullOrEmpty(result)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stop Decoding : Nothing to decode on : " + result);
            }
            return answer;
        }

        int count_decode = 1;
        while (result.contains("%") && count_decode <= 2) {
            /**
             * We iterate the property decode because properties names could be
             * inside other properties.
             */
            /**
             * Decode System Variables.
             */
            if (result.contains("%")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Starting to decode (system variable) string iteration#" + count_decode + ": " + result);
                }
                result = this.decodeStringWithSystemVariable(result, testCaseExecution);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Finished to decode (system variable) iteration#" + count_decode + ". Result : " + result);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Stop Decoding : No more things to decode on (exit when trying to decode System variable) : " + result);
                }
                answer.setItem(result);
                return answer;
            }

            /**
             * Decode ApplicationObject.
             */
            if (result.contains("%")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Starting to decode (Application Object) string iteration#" + count_decode + ": " + result);
                }
                result = applicationObjectVariableService.decodeStringWithApplicationObject(result, testCaseExecution, forceCalculation);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Finished to decode (Application Object) iteration#" + count_decode + ". Result : " + result);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Stop Decoding : No more things to decode on (exit when trying to decode ApplicationObject variable) : " + result);
                }
                answer.setItem(result);
                return answer;
            }

            /**
             * Decode Properties.
             */
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting to decode (Properties) string  iteration#" + count_decode + " : " + result);
            }
            answer = propertyService.decodeStringWithExistingProperties(result, testCaseExecution, testCaseStepActionExecution, forceCalculation);
            result = answer.getItem();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (Properties) iteration#" + count_decode + ". Result : " + result);
                LOG.debug("   Result Message : " + answer.getResultMessage().getCodeString() + " - " + answer.getResultMessage().getDescription());
            }

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
                answer.setItem(result);
                return answer;
            }
            count_decode++;
        }

        // Checking if after the decode we still have some variable not decoded.
        LOG.debug("Checking If after decode we still have uncoded variable.");
        List<String> variableList = getVariableListFromString(result);
        if (variableList.size() > 0) {
            String messageList = "";
            for (String var : variableList) {
                messageList += var + " ,";
            }
            messageList = StringUtil.removeLastChar(messageList, 2);
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DECODE_FAILED_VARIABLENOTDECODED)
                    .resolveDescription("NB", String.valueOf(variableList.size()))
                    .resolveDescription("VAR", messageList));
            answer.setItem(result);
            LOG.debug("Stop Decoding with error : " + answer.getResultMessage().getCodeString() + " - " + answer.getResultMessage().getDescription());
            return answer;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Stop Decoding : All iteration finished : " + result);
        }
        answer.setItem(result);
        return answer;
    }

    private List<String> getVariableListFromString(String str) {
        List<String> variable = new ArrayList<String>();

        final String regex = "%(property|system|object|service)\\..*?%";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            LOG.debug("Full match: " + matcher.group());
            variable.add(matcher.group());
        }

        return variable;
    }

    @Override
    public String decodeStringWithSystemVariable(String stringToDecode, TestCaseExecution tCExecution) {

        try {
            /**
             * Trying to replace by system environment variables from Execution.
             */
            stringToDecode = stringToDecode.replace("%SYS_SYSTEM%", tCExecution.getApplicationObj().getSystem());
            stringToDecode = stringToDecode.replace("%SYS_APPLI%", tCExecution.getApplicationObj().getApplication());
            stringToDecode = stringToDecode.replace("%SYS_BROWSER%", tCExecution.getBrowser());
            stringToDecode = stringToDecode.replace("%SYS_ROBOT%", tCExecution.getRobot());
            stringToDecode = stringToDecode.replace("%SYS_ROBOTDECLI%", tCExecution.getRobotDecli());
            stringToDecode = stringToDecode.replace("%SYS_SCREENSIZE%", tCExecution.getScreenSize());
            stringToDecode = stringToDecode.replace("%SYS_APP_DOMAIN%", tCExecution.getCountryEnvironmentParameters().getDomain().split(",")[0].trim());
            stringToDecode = stringToDecode.replace("%SYS_APP_HOST%", tCExecution.getCountryEnvironmentParameters().getIp());
            stringToDecode = stringToDecode.replace("%SYS_APP_CONTEXTROOT%", tCExecution.getCountryEnvironmentParameters().getUrl());
            stringToDecode = stringToDecode.replace("%SYS_APP_VAR1%", tCExecution.getCountryEnvironmentParameters().getVar1());
            stringToDecode = stringToDecode.replace("%SYS_APP_VAR2%", tCExecution.getCountryEnvironmentParameters().getVar2());
            stringToDecode = stringToDecode.replace("%SYS_APP_VAR3%", tCExecution.getCountryEnvironmentParameters().getVar3());
            stringToDecode = stringToDecode.replace("%SYS_APP_VAR4%", tCExecution.getCountryEnvironmentParameters().getVar4());
            stringToDecode = stringToDecode.replace("%SYS_EXEURL%", tCExecution.getUrl());
            stringToDecode = stringToDecode.replace("%SYS_ENV%", tCExecution.getEnvironmentData());
            stringToDecode = stringToDecode.replace("%SYS_ENVGP%", tCExecution.getEnvironmentDataObj().getGp1());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRY%", tCExecution.getCountry());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP1%", tCExecution.getCountryObj().getGp1());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP2%", tCExecution.getCountryObj().getGp2());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP3%", tCExecution.getCountryObj().getGp3());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP4%", tCExecution.getCountryObj().getGp4());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP5%", tCExecution.getCountryObj().getGp5());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP6%", tCExecution.getCountryObj().getGp6());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP7%", tCExecution.getCountryObj().getGp7());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP8%", tCExecution.getCountryObj().getGp8());
            stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP9%", tCExecution.getCountryObj().getGp9());
            stringToDecode = stringToDecode.replace("%SYS_TEST%", tCExecution.getTest());
            stringToDecode = stringToDecode.replace("%SYS_TESTCASE%", tCExecution.getTestCase());
            stringToDecode = stringToDecode.replace("%SYS_TESTCASEDESCRIPTION%", tCExecution.getDescription());
            stringToDecode = stringToDecode.replace("%SYS_SSIP%", tCExecution.getSeleniumIP());
            stringToDecode = stringToDecode.replace("%SYS_SSPORT%", tCExecution.getSeleniumPort());
            stringToDecode = stringToDecode.replace("%SYS_TAG%", tCExecution.getTag());
            stringToDecode = stringToDecode.replace("%SYS_EXECUTIONID%", String.valueOf(tCExecution.getId()));
            stringToDecode = stringToDecode.replace("%SYS_EXESTART%", String.valueOf(new Timestamp(tCExecution.getStart())));
            stringToDecode = stringToDecode.replace("%SYS_EXESTORAGEURL%", recorderService.getStorageSubFolderURL(tCExecution.getId()));
            long nowInMS = new Date().getTime();
            stringToDecode = stringToDecode.replace("%SYS_EXEELAPSEDMS%", String.valueOf(nowInMS - tCExecution.getStart()));
            // New syntax
            stringToDecode = stringToDecode.replace("%system.SYSTEM%", tCExecution.getApplicationObj().getSystem());
            stringToDecode = stringToDecode.replace("%system.APPLI%", tCExecution.getApplicationObj().getApplication());
            stringToDecode = stringToDecode.replace("%system.BROWSER%", tCExecution.getBrowser());
            stringToDecode = stringToDecode.replace("%system.ROBOT%", tCExecution.getRobot());
            stringToDecode = stringToDecode.replace("%system.ROBOTDECLI%", tCExecution.getRobotDecli());
            if (tCExecution.getRobotExecutorObj() != null) {
                stringToDecode = stringToDecode.replace("%system.ROBOTHOST%", tCExecution.getRobotExecutorObj().getHost());
            }

            stringToDecode = stringToDecode.replace("%system.SCREENSIZE%", tCExecution.getScreenSize());
            stringToDecode = stringToDecode.replace("%system.APP_DOMAIN%", tCExecution.getCountryEnvironmentParameters().getDomain().split(",")[0].trim());
            stringToDecode = stringToDecode.replace("%system.APP_HOST%", tCExecution.getCountryEnvironmentParameters().getIp());
            stringToDecode = stringToDecode.replace("%system.APP_CONTEXTROOT%", tCExecution.getCountryEnvironmentParameters().getUrl());
            stringToDecode = stringToDecode.replace("%system.APP_VAR1%", tCExecution.getCountryEnvironmentParameters().getVar1());
            stringToDecode = stringToDecode.replace("%system.APP_VAR2%", tCExecution.getCountryEnvironmentParameters().getVar2());
            stringToDecode = stringToDecode.replace("%system.APP_VAR3%", tCExecution.getCountryEnvironmentParameters().getVar3());
            stringToDecode = stringToDecode.replace("%system.APP_VAR4%", tCExecution.getCountryEnvironmentParameters().getVar4());
            stringToDecode = stringToDecode.replace("%system.EXEURL%", tCExecution.getUrl());
            stringToDecode = stringToDecode.replace("%system.ENV%", tCExecution.getEnvironmentData());
            stringToDecode = stringToDecode.replace("%system.ENVGP%", tCExecution.getEnvironmentDataObj().getGp1());
            stringToDecode = stringToDecode.replace("%system.COUNTRY%", tCExecution.getCountry());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP1%", tCExecution.getCountryObj().getGp1());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP2%", tCExecution.getCountryObj().getGp2());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP3%", tCExecution.getCountryObj().getGp3());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP4%", tCExecution.getCountryObj().getGp4());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP5%", tCExecution.getCountryObj().getGp5());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP6%", tCExecution.getCountryObj().getGp6());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP7%", tCExecution.getCountryObj().getGp7());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP8%", tCExecution.getCountryObj().getGp8());
            stringToDecode = stringToDecode.replace("%system.COUNTRYGP9%", tCExecution.getCountryObj().getGp9());
            stringToDecode = stringToDecode.replace("%system.TEST%", tCExecution.getTest());
            stringToDecode = stringToDecode.replace("%system.TESTCASE%", tCExecution.getTestCase());
            stringToDecode = stringToDecode.replace("%system.TESTCASEDESCRIPTION%", tCExecution.getDescription());
            stringToDecode = stringToDecode.replace("%system.SSIP%", tCExecution.getSeleniumIP());
            stringToDecode = stringToDecode.replace("%system.SSPORT%", tCExecution.getSeleniumPort());
            stringToDecode = stringToDecode.replace("%system.TAG%", tCExecution.getTag());
            stringToDecode = stringToDecode.replace("%system.EXECUTIONID%", String.valueOf(tCExecution.getId()));
            stringToDecode = stringToDecode.replace("%system.EXESTART%", String.valueOf(new Timestamp(tCExecution.getStart())));
            stringToDecode = stringToDecode.replace("%system.EXESTORAGEURL%", recorderService.getStorageSubFolderURL(tCExecution.getId()));
            nowInMS = new Date().getTime();
            stringToDecode = stringToDecode.replace("%system.EXEELAPSEDMS%", String.valueOf(nowInMS - tCExecution.getStart()));
            if (tCExecution.getRemoteProxyUUID() != null) {
                stringToDecode = stringToDecode.replace("%system.REMOTEPROXYUUID%", tCExecution.getRemoteProxyUUID());
            }
            if (tCExecution.getRobotExecutorObj() != null) {
                stringToDecode = stringToDecode.replace("%system.REMOTEPROXY_HAR_URL%", "http://" + tCExecution.getRobotExecutorObj().getExecutorExtensionHost() + ":" + tCExecution.getRobotExecutorObj().getExecutorExtensionPort() + "/getHar?uuid=" + tCExecution.getRemoteProxyUUID());
            }
            /**
             * Trying to replace by system environment variables from Step
             * Execution .
             */
            if (tCExecution.getTestCaseStepExecutionList() != null) {

                if (tCExecution.getTestCaseStepExecutionList().size() > 0) {
                    stringToDecode = stringToDecode.replace("%system.CURRENTSTEP_SORT%", String.valueOf(tCExecution.getTestCaseStepExecutionList().get(tCExecution.getTestCaseStepExecutionList().size() - 1).getSort()));
                    stringToDecode = stringToDecode.replace("%SYS_CURRENTSTEP_SORT%", String.valueOf(tCExecution.getTestCaseStepExecutionList().get(tCExecution.getTestCaseStepExecutionList().size() - 1).getSort()));

                    // %SYS_CURRENTSTEP_INDEX%
                    if (stringToDecode.contains("%SYS_CURRENTSTEP_")) {
                        TestCaseStepExecution currentStep = (TestCaseStepExecution) tCExecution.getTestCaseStepExecutionList().get(tCExecution.getTestCaseStepExecutionList().size() - 1);
                        stringToDecode = stringToDecode.replace("%SYS_CURRENTSTEP_INDEX%", String.valueOf(currentStep.getIndex()));
                        stringToDecode = stringToDecode.replace("%SYS_CURRENTSTEP_STARTISO%", new Timestamp(currentStep.getStart()).toString());
                        nowInMS = new Date().getTime();
                        stringToDecode = stringToDecode.replace("%SYS_CURRENTSTEP_ELAPSEDMS%", String.valueOf(nowInMS - currentStep.getFullStart()));

                    }
                    if (stringToDecode.contains("%system.CURRENTSTEP_")) {
                        TestCaseStepExecution currentStep = (TestCaseStepExecution) tCExecution.getTestCaseStepExecutionList().get(tCExecution.getTestCaseStepExecutionList().size() - 1);
                        stringToDecode = stringToDecode.replace("%system.CURRENTSTEP_INDEX%", String.valueOf(currentStep.getIndex()));
                        stringToDecode = stringToDecode.replace("%system.CURRENTSTEP_STARTISO%", new Timestamp(currentStep.getStart()).toString());
                        nowInMS = new Date().getTime();
                        stringToDecode = stringToDecode.replace("%system.CURRENTSTEP_ELAPSEDMS%", String.valueOf(nowInMS - currentStep.getFullStart()));
                    }
                }

                // %SYS_STEP.n.RETURNCODE%
                if (stringToDecode.contains("%SYS_STEP.")) {
                    String syntaxToReplace = "";
                    for (Object testCaseStepExecution : tCExecution.getTestCaseStepExecutionList()) {
                        TestCaseStepExecution tcse = (TestCaseStepExecution) testCaseStepExecution;
                        syntaxToReplace = "%SYS_STEP." + tcse.getSort() + "." + tcse.getIndex() + ".RETURNCODE%";
                        stringToDecode = stringToDecode.replace(syntaxToReplace, tcse.getReturnCode());
                    }
                }
                if (stringToDecode.contains("%system.STEP.")) {
                    String syntaxToReplace = "";
                    for (Object testCaseStepExecution : tCExecution.getTestCaseStepExecutionList()) {
                        TestCaseStepExecution tcse = (TestCaseStepExecution) testCaseStepExecution;
                        syntaxToReplace = "%system.STEP." + tcse.getSort() + "." + tcse.getIndex() + ".RETURNCODE%";
                        stringToDecode = stringToDecode.replace(syntaxToReplace, tcse.getReturnCode());
                    }
                }

            }

            /**
             * Last Service Called Variables.
             */
            if (!(tCExecution.getLastServiceCalled() == null)) {
                stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_HTTPCODE%", String.valueOf(tCExecution.getLastServiceCalled().getResponseHTTPCode()));
                stringToDecode = stringToDecode.replace("%system.LASTSERVICE_CALL%", tCExecution.getLastServiceCalled().toJSONOnDefaultExecution().toString());
            } else {
                stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_HTTPCODE%", VALUE_WHEN_NULL);
            }
            if (!(tCExecution.getLastServiceCalled() == null)) {
                stringToDecode = stringToDecode.replace("%system.LASTSERVICE_HTTPCODE%", String.valueOf(tCExecution.getLastServiceCalled().getResponseHTTPCode()));
                stringToDecode = stringToDecode.replace("%SYS_LASTSERVICE_CALL%", tCExecution.getLastServiceCalled().toJSONOnDefaultExecution().toString());
            } else {
                stringToDecode = stringToDecode.replace("%system.LASTSERVICE_HTTPCODE%", VALUE_WHEN_NULL);
            }

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
            stringToDecode = stringToDecode.replace("%SYS_TOMORROW-yyyy%", DateUtil.getTomorrowFormat("yyyy"));
            stringToDecode = stringToDecode.replace("%SYS_TOMORROW-MM%", DateUtil.getTomorrowFormat("MM"));
            stringToDecode = stringToDecode.replace("%SYS_TOMORROW-dd%", DateUtil.getTomorrowFormat("dd"));
            stringToDecode = stringToDecode.replace("%SYS_TOMORROW-doy%", DateUtil.getTomorrowFormat("D"));
            //New syntax
            stringToDecode = stringToDecode.replace("%system.TODAY-yyyy%", DateUtil.getTodayFormat("yyyy"));
            stringToDecode = stringToDecode.replace("%system.TODAY-MM%", DateUtil.getTodayFormat("MM"));
            stringToDecode = stringToDecode.replace("%system.TODAY-dd%", DateUtil.getTodayFormat("dd"));
            stringToDecode = stringToDecode.replace("%system.TODAY-doy%", DateUtil.getTodayFormat("D"));
            stringToDecode = stringToDecode.replace("%system.TODAY-HH%", DateUtil.getTodayFormat("HH"));
            stringToDecode = stringToDecode.replace("%system.TODAY-mm%", DateUtil.getTodayFormat("mm"));
            stringToDecode = stringToDecode.replace("%system.TODAY-ss%", DateUtil.getTodayFormat("ss"));
            stringToDecode = stringToDecode.replace("%system.YESTERDAY-yyyy%", DateUtil.getYesterdayFormat("yyyy"));
            stringToDecode = stringToDecode.replace("%system.YESTERDAY-MM%", DateUtil.getYesterdayFormat("MM"));
            stringToDecode = stringToDecode.replace("%system.YESTERDAY-dd%", DateUtil.getYesterdayFormat("dd"));
            stringToDecode = stringToDecode.replace("%system.YESTERDAY-doy%", DateUtil.getYesterdayFormat("D"));
            stringToDecode = stringToDecode.replace("%system.YESTERDAY-HH%", DateUtil.getYesterdayFormat("HH"));
            stringToDecode = stringToDecode.replace("%system.YESTERDAY-mm%", DateUtil.getYesterdayFormat("mm"));
            stringToDecode = stringToDecode.replace("%system.YESTERDAY-ss%", DateUtil.getYesterdayFormat("ss"));
            stringToDecode = stringToDecode.replace("%system.TOMORROW-yyyy%", DateUtil.getTomorrowFormat("yyyy"));
            stringToDecode = stringToDecode.replace("%system.TOMORROW-MM%", DateUtil.getTomorrowFormat("MM"));
            stringToDecode = stringToDecode.replace("%system.TOMORROW-dd%", DateUtil.getTomorrowFormat("dd"));
            stringToDecode = stringToDecode.replace("%system.TOMORROW-doy%", DateUtil.getTomorrowFormat("D"));

            return stringToDecode;

        } catch (Exception e) {
            LOG.error("Error when decoding system variable.", e, e.getStackTrace());
        }
        return stringToDecode;
    }

}
