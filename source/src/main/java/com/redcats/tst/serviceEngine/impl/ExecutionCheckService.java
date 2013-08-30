package com.redcats.tst.serviceEngine.impl;

import com.redcats.tst.entity.CountryEnvParam;
import com.redcats.tst.entity.Invariant;
import com.redcats.tst.entity.MessageEventEnum;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.TCExecution;
import com.redcats.tst.entity.TCase;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.IApplicationService;
import com.redcats.tst.serviceEngine.IExecutionCheckService;
import com.redcats.tst.service.IInvariantService;
import com.redcats.tst.service.ITestCaseCountryService;
import com.redcats.tst.util.ParameterParserUtil;
import com.redcats.tst.util.StringUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 15/01/2013
 * @since 2.0.0
 */
@Service
public class ExecutionCheckService implements IExecutionCheckService {

    // private TestCaseExecution execution;
    // private Environment environment;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ITestCaseCountryService testCaseCountryService;
    @Autowired
    private IInvariantService invariantService;
    private MessageGeneral message;

    @Override
    public MessageGeneral checkTestCaseExecution(TCExecution tCExecution) {
        if (tCExecution.isManualURL()) {
            /**
             * Manual application connectivity parameter
             */
            if (this.checkTestCaseActive(tCExecution.gettCase())
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
                    && this.checkRangeBuildRevision(tCExecution)
                    && this.checkTargetBuildRevision(tCExecution)
                    && this.checkActiveEnvironmentGroup(tCExecution)
                    && this.checkTestCaseActive(tCExecution.gettCase())
                    && this.checkTypeEnvironment(tCExecution)
                    && this.checkCountry(tCExecution)
                    && this.checkMaintenanceTime(tCExecution)) {
                return new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS);
            }
        }
        return message;
    }

    private boolean checkEnvironmentActive(CountryEnvParam cep) {
        MyLogger.log(ExecutionCheckService.class.getName(), Level.DEBUG, "Checking if environment is active");
        if (cep.isActive()) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_NOTACTIVE);
        return false;
    }

    private boolean checkTestCaseActive(TCase testCase) {
        MyLogger.log(ExecutionCheckService.class.getName(), Level.DEBUG, "Checking if testcase is active");
        if (testCase.getActive().equals("Y")) {
            return true;
        }
        message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_NOTACTIVE);
        return false;
    }

    private boolean checkTypeEnvironment(TCExecution tCExecution) {
        MyLogger.log(ExecutionCheckService.class.getName(), Level.DEBUG, "Checking if application environment type is compatible with environment type");
        try {
            if (applicationService.findApplicationByKey(tCExecution.gettCase().getApplication()).getType().equalsIgnoreCase("COMPARISON")) {
                if (tCExecution.gettCase().getGroup().equalsIgnoreCase("COMPARATIVE")) {
                    message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TYPE_DIFFERENT);
                    return false;
                }
            }
        } catch (CerberusException ex) {
            Logger.getLogger(ExecutionCheckService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean checkRangeBuildRevision(TCExecution tCExecution) {
        MyLogger.log(ExecutionCheckService.class.getName(), Level.DEBUG, "Checking if test can be executed in this build and revision");
        TCase tc = tCExecution.gettCase();
        CountryEnvParam env = tCExecution.getCountryEnvParam();
        String tcFromSprint = ParameterParserUtil.parseStringParam(tc.getFromSprint(),"");
        String tcToSprint = ParameterParserUtil.parseStringParam(tc.getToSprint(),"");
        String tcFromRevision = ParameterParserUtil.parseStringParam(tc.getFromRevision(),"");
        String tcToRevision = ParameterParserUtil.parseStringParam(tc.getToRevision(),"");
        String sprint = ParameterParserUtil.parseStringParam(env.getBuild(),"");
        String revision = ParameterParserUtil.parseStringParam(env.getRevision(),"");

        if (!tcFromSprint.isEmpty() && sprint != null) {
            try {
                int dif = this.compareBuild(sprint, tcFromSprint);
                if (dif == 0) {
                    if (!tcFromRevision.isEmpty() && revision != null) {
                        if (this.compareRevision(revision, tcFromRevision) < 0) {
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
                int dif = this.compareBuild(tcToSprint, sprint);
                if (dif == 0) {
                    if (!tcToRevision.isEmpty() && revision != null) {
                        if (this.compareRevision(tcToRevision, revision) < 0) {
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

    private boolean checkTargetBuildRevision(TCExecution tCExecution) {
        MyLogger.log(ExecutionCheckService.class.getName(), Level.DEBUG, "Checking target build");
        TCase tc = tCExecution.gettCase();
        CountryEnvParam env = tCExecution.getCountryEnvParam();
        String tcSprint = ParameterParserUtil.parseStringParam(tc.getTargetSprint(),"");
        String tcRevision = ParameterParserUtil.parseStringParam(tc.getTargetRevision(),"");
        String sprint = ParameterParserUtil.parseStringParam(env.getBuild(),"");
        String revision = ParameterParserUtil.parseStringParam(env.getRevision(),"");

        if (!tcSprint.isEmpty() && sprint != null) {
            try {
                int dif = this.compareBuild(sprint, tcSprint);
                if (dif == 0) {
                    if (!tcRevision.isEmpty() && revision != null) {
                        if (this.compareRevision(revision, tcRevision) < 0) {
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

    private boolean checkActiveEnvironmentGroup(TCExecution tCExecution) {
        MyLogger.log(ExecutionCheckService.class.getName(), Level.DEBUG, "Checking environment " + tCExecution.getCountryEnvParam().getEnvironment());
        TCase tc = tCExecution.gettCase();
        CountryEnvParam env = tCExecution.getCountryEnvParam();
        // TODO : Test needs to be done from Environment group and not directly from environment string.
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

    private boolean checkCountry(TCExecution tCExecution) {
        MyLogger.log(ExecutionCheckService.class.getName(), Level.DEBUG, "Checking if country is setup for this testcase. " + tCExecution.getTest() + "-" + tCExecution.getTestCase() + "-" + tCExecution.getCountry());
        try {
            testCaseCountryService.findTestCaseCountryByKey(tCExecution.getTest(), tCExecution.getTestCase(), tCExecution.getCountry());
        } catch (CerberusException e) {
            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOTDEFINED);
            return false;
        }
        return true;
    }

    private int compareBuild(String build1, String build2) {
        if (build1.length() > 5 && build2.length() > 5) {
            int year1 = Integer.parseInt(build1.substring(0, 4));
            int num1 = Integer.parseInt(build1.substring(5));
            int year2 = Integer.parseInt(build2.substring(0, 4));
            int num2 = Integer.parseInt(build2.substring(5));

            if (year1 > year2) {
                return 1;
            } else if (year1 > year2) {
                return -1;
            } else {
                if (num1 > num2) {
                    return 1;
                } else if (num1 < num2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
        throw new NumberFormatException();
    }

    private int compareRevision(String rev1, String rev2) {
        if (rev1.length() > 2 && rev2.length() > 2) {
            char tcLetter = rev1.charAt(0);
            int tcRev = Integer.parseInt(rev1.substring(1));
            char letter = rev2.charAt(0);
            int rev = Integer.parseInt(rev2.substring(1));

            if (tcLetter > letter) {
                return 1;
            } else if (tcLetter < letter) {
                return -1;
            } else {
                if (tcRev > rev) {
                    return 1;
                } else if (tcRev < rev) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
        throw new NumberFormatException();
    }

    private boolean checkMaintenanceTime(TCExecution tCExecution) {
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
                MyLogger.log(ExecutionCheckService.class.getName(), Level.ERROR, exception.toString());
            }
            message = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_UNDER_MAINTENANCE);
            return false;
        }
        return true;
    }
}
