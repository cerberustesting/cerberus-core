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
package org.cerberus.engine.execution.impl;

import org.cerberus.enums.MessageGeneralEnum;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.BuildRevisionInvariant;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.Test;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.IBuildRevisionInvariantService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.engine.execution.IExecutionCheckService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
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
            if (this.checkTestCaseActive(tCExecution.getTestCaseObj())
                    && this.checkTestActive(tCExecution.getTestObj())
                    && this.checkTestCaseNotManual(tCExecution)
                    && this.checkTypeEnvironment(tCExecution)
                    && this.checkCountry(tCExecution)
                    && this.checkMaintenanceTime(tCExecution)
                    && this.checkUserAgentConsistent(tCExecution)) {
                return new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS);
            }
        } else /**
         * Automatic application connectivity parameter (from database)
         */
        {
            if (this.checkEnvironmentActive(tCExecution.getCountryEnvParam())
                    && this.checkTestCaseNotManual(tCExecution)
                    && this.checkRangeBuildRevision(tCExecution)
                    && this.checkTargetBuildRevision(tCExecution)
                    && this.checkActiveEnvironmentGroup(tCExecution)
                    && this.checkTestCaseActive(tCExecution.getTestCaseObj())
                    && this.checkTestActive(tCExecution.getTestObj())
                    && this.checkTypeEnvironment(tCExecution)
                    && this.checkCountry(tCExecution)
                    && this.checkMaintenanceTime(tCExecution)
                    && this.checkVerboseIsNotZeroForFirefoxOnly(tCExecution)
                    && this.checkUserAgentConsistent(tCExecution)) {
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

    private boolean checkTestCaseActive(TestCase testCase) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if testcase is active");
        }
        if (testCase.getTcActive().equals("Y")) {
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
        message.setDescription(message.getDescription().replace("%TEST%", test.getTest()));
        return false;
    }

    private boolean checkTestCaseNotManual(TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if testcase is not MANUAL");
        }

        if ("N".equals(tCExecution.getManualExecution()) && tCExecution.getTestCaseObj().getGroup().equals("MANUAL")) {
            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_ISMANUAL);
            return false;
        }
        return true;
    }

    private boolean checkTypeEnvironment(TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if application environment type is compatible with environment type");
        }
        try {
            if (applicationService.convert(applicationService.readByKey(tCExecution.getTestCaseObj().getApplication())).getType().equalsIgnoreCase("COMPARISON")) {
                if (tCExecution.getTestCaseObj().getGroup().equalsIgnoreCase("COMPARATIVE")) {
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
        TestCase tc = tCExecution.getTestCaseObj();
        CountryEnvParam env = tCExecution.getCountryEnvParam();
        String tcFromSprint = ParameterParserUtil.parseStringParam(tc.getFromBuild(), "");
        String tcToSprint = ParameterParserUtil.parseStringParam(tc.getToBuild(), "");
        String tcFromRevision = ParameterParserUtil.parseStringParam(tc.getFromRev(), "");
        String tcToRevision = ParameterParserUtil.parseStringParam(tc.getToRev(), "");
        String sprint = ParameterParserUtil.parseStringParam(env.getBuild(), "");
        String revision = ParameterParserUtil.parseStringParam(env.getRevision(), "");
        int dif = -1;

        if (!tcFromSprint.isEmpty() && sprint != null) {
            try {
                if (sprint.isEmpty()) {
                    message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_NOTDEFINED);
                    return false;
                } else {
                    dif = this.compareBuild(sprint, tcFromSprint, env.getSystem());
                }
                if (dif == 0) {
                    if (!tcFromRevision.isEmpty() && revision != null) {
                        if (revision.isEmpty()) {
                            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_NOTDEFINED);
                            return false;
                        } else if (this.compareRevision(revision, tcFromRevision, env.getSystem()) < 0) {
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
            } catch (CerberusException ex) {
                message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_BADLYDEFINED);
                return false;
            }
        }

        if (!tcToSprint.isEmpty() && sprint != null) {
            try {
                if (sprint.isEmpty()) {
                    message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_NOTDEFINED);
                    return false;
                } else {
                    dif = this.compareBuild(tcToSprint, sprint, env.getSystem());
                }
                if (dif == 0) {
                    if (!tcToRevision.isEmpty() && revision != null) {
                        if (revision.isEmpty()) {
                            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_NOTDEFINED);
                            return false;
                        } else if (this.compareRevision(tcToRevision, revision, env.getSystem()) < 0) {
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
            } catch (CerberusException ex) {
                message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_BADLYDEFINED);
                return false;
            }
        }

        return true;
    }

    private boolean checkTargetBuildRevision(TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking target build");
        }
        TestCase tc = tCExecution.getTestCaseObj();
        CountryEnvParam env = tCExecution.getCountryEnvParam();
        String tcSprint = ParameterParserUtil.parseStringParam(tc.getTargetBuild(), "");
        String tcRevision = ParameterParserUtil.parseStringParam(tc.getTargetRev(), "");
        String sprint = ParameterParserUtil.parseStringParam(env.getBuild(), "");
        String revision = ParameterParserUtil.parseStringParam(env.getRevision(), "");
        int dif = -1;

        if (!tcSprint.isEmpty() && sprint != null) {
            try {
                if (sprint.isEmpty()) {
                    message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_NOTDEFINED);
                    return false;
                } else {
                    dif = this.compareBuild(sprint, tcSprint, env.getSystem());
                }
                if (dif == 0) {
                    if (!tcRevision.isEmpty() && revision != null) {
                        if (revision.isEmpty()) {
                            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_NOTDEFINED);
                            return false;
                        } else if (this.compareRevision(revision, tcRevision, env.getSystem()) < 0) {
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
            } catch (CerberusException ex) {
                message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_BADLYDEFINED);
                return false;
            }
        }
        return true;
    }

    private boolean checkActiveEnvironmentGroup(TestCaseExecution tCExecution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking environment " + tCExecution.getCountryEnvParam().getEnvironment());
        }
        TestCase tc = tCExecution.getTestCaseObj();
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
        message.setDescription(message.getDescription().replace("%ENV%", tCExecution.getEnvironmentData()));
        message.setDescription(message.getDescription().replace("%ENVGP%", tCExecution.getEnvironmentDataObj().getGp1()));
        return false;
    }

    private boolean checkRunQA(TestCase tc, String env) {
        if (tc.getActiveQA().equals("Y")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RUNQA_NOTDEFINED);
        message.setDescription(message.getDescription().replace("%ENV%", env));
        return false;
    }

    private boolean checkRunUAT(TestCase tc, String env) {
        if (tc.getActiveUAT().equals("Y")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RUNUAT_NOTDEFINED);
        message.setDescription(message.getDescription().replace("%ENV%", env));
        return false;
    }

    private boolean checkRunPROD(TestCase tc, String env) {
        if (tc.getActivePROD().equals("Y")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RUNPROD_NOTDEFINED);
        message.setDescription(message.getDescription().replace("%ENV%", env));
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

    private int compareBuild(String build1, String build2, String system) throws CerberusException{
        BuildRevisionInvariant b1;
        BuildRevisionInvariant b2;
        
        try {
            b1 = buildRevisionInvariantService.convert(buildRevisionInvariantService.readByKey(system, 1, build1));
            b2 = buildRevisionInvariantService.convert(buildRevisionInvariantService.readByKey(system, 1, build2));
        } catch (CerberusException e) {
            throw new NumberFormatException();
        }

        if (null == b1 || null == b2) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_BADLYDEFINED));
        }

        return b1.getSeq().compareTo(b2.getSeq());

    }

    private int compareRevision(String rev1, String rev2, String system) {
        try {
            BuildRevisionInvariant r1 = buildRevisionInvariantService.convert(buildRevisionInvariantService.readByKey(system, 2, rev1));
            BuildRevisionInvariant r2 = buildRevisionInvariantService.convert(buildRevisionInvariantService.readByKey(system, 2, rev2));

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

    private boolean checkUserAgentConsistent(TestCaseExecution tCExecution) {
        // We check here that User Agent has not been forced at TestCase Level and Robot Level on different value.
        if (!(StringUtil.isNullOrEmpty(tCExecution.getTestCaseObj().getUserAgent())) && !(StringUtil.isNullOrEmpty(tCExecution.getUserAgent()))) {
            if (!(tCExecution.getTestCaseObj().getUserAgent().equals(tCExecution.getUserAgent()))) {
                message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_USERAGENTDIFFERENT)
                        .resolveDescription("UATESTCASE", tCExecution.getTestCaseObj().getUserAgent())
                        .resolveDescription("UAROBOT", tCExecution.getUserAgent());
                return false;
            }
        }
        return true;
    }
}
