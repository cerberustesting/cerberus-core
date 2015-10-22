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

import org.cerberus.enums.MessageGeneralEnum;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.BuildRevisionInvariant;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.Test;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.IBuildRevisionInvariantService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.service.engine.IExecutionCheckService;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 15/01/2013
 * @since 0.9.0
 */
@Service
public class ExecutionCheckService implements IExecutionCheckService {

    /**
     * The associated {@link org.apache.log4j.Logger} to this class
     */
    private static final Logger LOG = Logger.getLogger(ExecutionCheckService.class);

    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ITestCaseCountryService testCaseCountryService;
    @Autowired
    private IBuildRevisionInvariantService buildRevisionInvariantService;
    private MessageGeneral message;

    @Override
    public MessageGeneral checkTestCaseExecution(TestCaseExecution tCExecution) {
        if (tCExecution.isManualURL()) {
            /**
             * Manual application connectivity parameter
             */
            if (this.checkTestCaseActive(tCExecution.gettCase())
                    && this.checkTestActive(tCExecution.getTestObj())
                    && this.checkTestCaseNotManual(tCExecution.gettCase())
                    && this.checkTypeEnvironment(tCExecution)
                    && this.checkCountry(tCExecution)
                    && this.checkMaintenanceTime(tCExecution)) {
                return new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS);
            }
        } else {
            /**
             * Automatic application connectivity parameter (from database)
             */
            if (this.checkEnvironmentActive(tCExecution.getCountryEnvParam())
                    && this.checkTestCaseNotManual(tCExecution.gettCase())
                    && this.checkRangeBuildRevision(tCExecution)
                    && this.checkTargetBuildRevision(tCExecution)
                    && this.checkActiveEnvironmentGroup(tCExecution)
                    && this.checkTestCaseActive(tCExecution.gettCase())
                    && this.checkTestActive(tCExecution.getTestObj())
                    && this.checkTypeEnvironment(tCExecution)
                    && this.checkCountry(tCExecution)
                    && this.checkMaintenanceTime(tCExecution)
                    && this.checkVerboseIsNotZeroForFirefoxOnly(tCExecution)) {
                return new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS);
            }
        }
        return message;
    }

    private boolean checkEnvironmentActive(CountryEnvParam cep) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if environment is active");
        }
        if (cep.isActive()) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_NOTACTIVE);
        return false;
    }

    private boolean checkTestCaseActive(TCase testCase) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if testcase is active");
        }
        if (testCase.getActive().equals("Y")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_NOTACTIVE);
        return false;
    }

    private boolean checkTestActive(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if test is active");
        }
        if (test.getActive().equals("Y")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TEST_NOTACTIVE);
        message.setDescription(message.getDescription().replaceAll("%TEST%", test.getTest()));
        return false;
    }

    private boolean checkTestCaseNotManual(TCase testCase) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if testcase is not MANUAL");
        }
        if (!(testCase.getGroup().equals("MANUAL"))) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_ISMANUAL);
        return false;
    }

    private boolean checkTypeEnvironment(TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if application environment type is compatible with environment type");
        }
        try {
            if (applicationService.convert(applicationService.readByKey(tCExecution.gettCase().getApplication())).getType().equalsIgnoreCase("COMPARISON")) {
                if (tCExecution.gettCase().getGroup().equalsIgnoreCase("COMPARATIVE")) {
                    message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TYPE_DIFFERENT);
                    return false;
                }
            }
        } catch (CerberusException ex) {
            LOG.fatal("Unable to find Application", ex);
        }
        return true;
    }

    private boolean checkRangeBuildRevision(TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if test can be executed in this build and revision");
        }
        TCase tc = tCExecution.gettCase();
        CountryEnvParam env = tCExecution.getCountryEnvParam();
        String tcFromSprint = ParameterParserUtil.parseStringParam(tc.getFromSprint(), "");
        String tcToSprint = ParameterParserUtil.parseStringParam(tc.getToSprint(), "");
        String tcFromRevision = ParameterParserUtil.parseStringParam(tc.getFromRevision(), "");
        String tcToRevision = ParameterParserUtil.parseStringParam(tc.getToRevision(), "");
        String sprint = ParameterParserUtil.parseStringParam(env.getBuild(), "");
        String revision = ParameterParserUtil.parseStringParam(env.getRevision(), "");

        if (!tcFromSprint.isEmpty() && sprint != null) {
            try {
                int dif = this.compareBuild(sprint, tcFromSprint, env.getSystem());
                if (dif == 0) {
                    if (!tcFromRevision.isEmpty() && revision != null) {
                        if (this.compareRevision(revision, tcFromRevision, env.getSystem()) < 0) {
                            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_DIFFERENT);
                            return false;
                        }
                    }
                } else if (dif < 0) {
                    message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_DIFFERENT);
                    return false;
                }
            } catch (NumberFormatException exception) {
                message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_WRONGFORMAT);
                return false;
            }
        }

        if (!tcToSprint.isEmpty() && sprint != null) {
            try {
                int dif = this.compareBuild(tcToSprint, sprint, env.getSystem());
                if (dif == 0) {
                    if (!tcToRevision.isEmpty() && revision != null) {
                        if (this.compareRevision(tcToRevision, revision, env.getSystem()) < 0) {
                            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_DIFFERENT);
                            return false;
                        }
                    }
                } else if (dif < 0) {
                    message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_DIFFERENT);
                    return false;
                }
            } catch (NumberFormatException exception) {
                message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_WRONGFORMAT);
                return false;
            }
        }

        return true;
    }

    private boolean checkTargetBuildRevision(TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking target build");
        }
        TCase tc = tCExecution.gettCase();
        CountryEnvParam env = tCExecution.getCountryEnvParam();
        String tcSprint = ParameterParserUtil.parseStringParam(tc.getTargetSprint(), "");
        String tcRevision = ParameterParserUtil.parseStringParam(tc.getTargetRevision(), "");
        String sprint = ParameterParserUtil.parseStringParam(env.getBuild(), "");
        String revision = ParameterParserUtil.parseStringParam(env.getRevision(), "");

        if (!tcSprint.isEmpty() && sprint != null) {
            try {
                int dif = this.compareBuild(sprint, tcSprint, env.getSystem());
                if (dif == 0) {
                    if (!tcRevision.isEmpty() && revision != null) {
                        if (this.compareRevision(revision, tcRevision, env.getSystem()) < 0) {
                            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TARGET_DIFFERENT);
                            return false;
                        }
                    }
                } else if (dif < 0) {
                    message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TARGET_DIFFERENT);
                    return false;
                }
            } catch (NumberFormatException exception) {
                message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TARGET_WRONGFORMAT);
                return false;
            }
        }
        return true;
    }

    private boolean checkActiveEnvironmentGroup(TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking environment " + tCExecution.getCountryEnvParam().getEnvironment());
        }
        TCase tc = tCExecution.gettCase();
        if (tCExecution.getEnvironmentDataObj().getGp1().equalsIgnoreCase("QA")) {
            return this.checkRunQA(tc, tCExecution.getEnvironmentData());
        } else if (tCExecution.getEnvironmentDataObj().getGp1().equalsIgnoreCase("UAT")) {
            return this.checkRunUAT(tc, tCExecution.getEnvironmentData());
        } else if (tCExecution.getEnvironmentDataObj().getGp1().equalsIgnoreCase("PROD")) {
            return this.checkRunPROD(tc, tCExecution.getEnvironmentData());
        } else if (tCExecution.getEnvironmentDataObj().getGp1().equalsIgnoreCase("DEV")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_NOTDEFINED);
        message.setDescription(message.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
        message.setDescription(message.getDescription().replaceAll("%ENVGP%", tCExecution.getEnvironmentDataObj().getGp1()));
        return false;
    }

    private boolean checkRunQA(TCase tc, String env) {
        if (tc.getRunQA().equals("Y")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RUNQA_NOTDEFINED);
        message.setDescription(message.getDescription().replaceAll("%ENV%", env));
        return false;
    }

    private boolean checkRunUAT(TCase tc, String env) {
        if (tc.getRunUAT().equals("Y")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RUNUAT_NOTDEFINED);
        message.setDescription(message.getDescription().replaceAll("%ENV%", env));
        return false;
    }

    private boolean checkRunPROD(TCase tc, String env) {
        if (tc.getRunPROD().equals("Y")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RUNPROD_NOTDEFINED);
        message.setDescription(message.getDescription().replaceAll("%ENV%", env));
        return false;
    }

    private boolean checkCountry(TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if country is setup for this testcase. " + tCExecution.getTest() + "-" + tCExecution.getTestCase() + "-" + tCExecution.getCountry());
        }
        try {
            testCaseCountryService.findTestCaseCountryByKey(tCExecution.getTest(), tCExecution.getTestCase(), tCExecution.getCountry());
        } catch (CerberusException e) {
            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOTDEFINED);
            return false;
        }
        return true;
    }

    private int compareBuild(String build1, String build2, String system) {
        try {
            BuildRevisionInvariant b1 = this.buildRevisionInvariantService.findBuildRevisionInvariantByKey(system, 1, build1);
            BuildRevisionInvariant b2 = this.buildRevisionInvariantService.findBuildRevisionInvariantByKey(system, 1, build2);

            return b1.getSeq().compareTo(b2.getSeq());
        } catch (CerberusException e) {
            throw new NumberFormatException();
        }
    }

    private int compareRevision(String rev1, String rev2, String system) {
        try {
            BuildRevisionInvariant r1 = this.buildRevisionInvariantService.findBuildRevisionInvariantByKey(system, 2, rev1);
            BuildRevisionInvariant r2 = this.buildRevisionInvariantService.findBuildRevisionInvariantByKey(system, 2, rev2);

            return r1.getSeq().compareTo(r2.getSeq());
        } catch (CerberusException e) {
            throw new NumberFormatException();
        }
    }

    private boolean checkMaintenanceTime(TestCaseExecution tCExecution) {
        if (tCExecution.getCountryEnvParam().isMaintenanceAct()) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String nowDate = sdf.format(new Date());

            try {
                long now = sdf.parse(nowDate).getTime();
                long startMaintenance = sdf.parse(tCExecution.getCountryEnvParam().getMaintenanceStr()).getTime();
                long endMaintenance = sdf.parse(tCExecution.getCountryEnvParam().getMaintenanceStr()).getTime();

                if (!(now > startMaintenance && now < endMaintenance)) {
                    return true;
                }

            } catch (ParseException exception) {
                LOG.error(exception.toString());
            }
            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_UNDER_MAINTENANCE);
            return false;
        }
        return true;
    }

    private boolean checkVerboseIsNotZeroForFirefoxOnly(TestCaseExecution tCExecution) {
        if (!tCExecution.getBrowser().equalsIgnoreCase("firefox")) {
            if (tCExecution.getVerbose() > 0) {
                message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_VERBOSE_USED_WITH_INCORRECT_BROWSER);
                return false;
            }
        }
        return true;
    }
}
