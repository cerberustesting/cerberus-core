/* Cerberus  Copyright (C) 2013  vertigo17
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
import java.util.Date;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.util.DateUtil;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by corentin on 20/10/16.
 */
@Service
public class VariableService implements IVariableService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VariableService.class);

    @Autowired
    PropertyService propertyService;
    @Autowired
    ApplicationObjectVariableService applicationObjectVariableService;
    @Autowired
    private IRecorderService recorderService;

    @Override
    public String decodeStringCompletly(String stringToDecode, TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {
        String result = stringToDecode;

        /**
         * Nothing to decode if null or empty string.
         */
        if (StringUtil.isNullOrEmpty(result)) {
            return result;
        }

        /**
         * Decode System Variables.
         */
        if (result.contains("%")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting to decode (system variable) string : " + result);
            }
            result = this.decodeStringWithSystemVariable(result, testCaseExecution);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (system variable). Result : " + result);
            }
        } else {
            return result;
        }

        /**
         * Decode ApplicationObject.
         */
        if (result.contains("%")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting to decode (Application Object) string : " + result);
            }
            result = applicationObjectVariableService.decodeStringWithApplicationObject(result, testCaseExecution, forceCalculation);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (Application Object). Result : " + result);
            }
        } else {
            return result;
        }

        /**
         * Decode Properties.
         */
        if (result.contains("%")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting to decode (Properties) string : " + result);
            }
            result = propertyService.decodeStringWithExistingProperties(result, testCaseExecution, testCaseStepActionExecution, forceCalculation);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (Properties). Result : " + result);
            }
        } else {
            return result;
        }

        return result;
    }

    @Override
    public String decodeStringWithSystemVariable(String stringToDecode, TestCaseExecution tCExecution) {
        /**
         * Trying to replace by system environment variables from Execution.
         */
        stringToDecode = stringToDecode.replace("%SYS_SYSTEM%", tCExecution.getApplicationObj().getSystem());
        stringToDecode = stringToDecode.replace("%SYS_APPLI%", tCExecution.getApplicationObj().getApplication());
        stringToDecode = stringToDecode.replace("%SYS_BROWSER%", tCExecution.getBrowser());
        stringToDecode = stringToDecode.replace("%SYS_APP_DOMAIN%", tCExecution.getCountryEnvironmentParameters().getDomain());
        stringToDecode = stringToDecode.replace("%SYS_APP_HOST%", tCExecution.getCountryEnvironmentParameters().getIp());
        stringToDecode = stringToDecode.replace("%SYS_APP_VAR1%", tCExecution.getCountryEnvironmentParameters().getVar1());
        stringToDecode = stringToDecode.replace("%SYS_APP_VAR2%", tCExecution.getCountryEnvironmentParameters().getVar2());
        stringToDecode = stringToDecode.replace("%SYS_APP_VAR3%", tCExecution.getCountryEnvironmentParameters().getVar3());
        stringToDecode = stringToDecode.replace("%SYS_APP_VAR4%", tCExecution.getCountryEnvironmentParameters().getVar4());
        stringToDecode = stringToDecode.replace("%SYS_ENV%", tCExecution.getEnvironmentData());
        stringToDecode = stringToDecode.replace("%SYS_ENVGP%", tCExecution.getEnvironmentDataObj().getGp1());
        stringToDecode = stringToDecode.replace("%SYS_COUNTRY%", tCExecution.getCountry());
        stringToDecode = stringToDecode.replace("%SYS_COUNTRYGP1%", tCExecution.getCountryObj().getGp1());
        stringToDecode = stringToDecode.replace("%SYS_TEST%", tCExecution.getTest());
        stringToDecode = stringToDecode.replace("%SYS_TESTCASE%", tCExecution.getTestCase());
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
        stringToDecode = stringToDecode.replace("%system.APP_DOMAIN%", tCExecution.getCountryEnvironmentParameters().getDomain());
        stringToDecode = stringToDecode.replace("%system.APP_HOST%", tCExecution.getCountryEnvironmentParameters().getIp());
        stringToDecode = stringToDecode.replace("%system.APP_VAR1%", tCExecution.getCountryEnvironmentParameters().getVar1());
        stringToDecode = stringToDecode.replace("%system.APP_VAR2%", tCExecution.getCountryEnvironmentParameters().getVar2());
        stringToDecode = stringToDecode.replace("%system.APP_VAR3%", tCExecution.getCountryEnvironmentParameters().getVar3());
        stringToDecode = stringToDecode.replace("%system.APP_VAR4%", tCExecution.getCountryEnvironmentParameters().getVar4());
        stringToDecode = stringToDecode.replace("%system.ENV%", tCExecution.getEnvironmentData());
        stringToDecode = stringToDecode.replace("%system.ENVGP%", tCExecution.getEnvironmentDataObj().getGp1());
        stringToDecode = stringToDecode.replace("%system.COUNTRY%", tCExecution.getCountry());
        stringToDecode = stringToDecode.replace("%system.COUNTRYGP1%", tCExecution.getCountryObj().getGp1());
        stringToDecode = stringToDecode.replace("%system.TEST%", tCExecution.getTest());
        stringToDecode = stringToDecode.replace("%system.TESTCASE%", tCExecution.getTestCase());
        stringToDecode = stringToDecode.replace("%system.SSIP%", tCExecution.getSeleniumIP());
        stringToDecode = stringToDecode.replace("%system.SSPORT%", tCExecution.getSeleniumPort());
        stringToDecode = stringToDecode.replace("%system.TAG%", tCExecution.getTag());
        stringToDecode = stringToDecode.replace("%system.EXECUTIONID%", String.valueOf(tCExecution.getId()));
        stringToDecode = stringToDecode.replace("%system.EXESTART%", String.valueOf(new Timestamp(tCExecution.getStart())));
        stringToDecode = stringToDecode.replace("%system.EXESTORAGEURL%", recorderService.getStorageSubFolderURL(tCExecution.getId()));
        nowInMS = new Date().getTime();
        stringToDecode = stringToDecode.replace("%system.EXEELAPSEDMS%", String.valueOf(nowInMS - tCExecution.getStart()));

        /**
         * Trying to replace by system environment variables from Step Execution
         * .
         */
        if (tCExecution.getTestCaseStepExecutionList() != null) {

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

        return stringToDecode;
    }

}
