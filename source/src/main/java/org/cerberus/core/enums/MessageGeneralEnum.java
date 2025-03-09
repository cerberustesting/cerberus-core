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
package org.cerberus.core.enums;

/**
 * Message is a generic Message that is used to feedback the result of any
 * Cerberus execution. For every message, we have: - a number - a 2 digit code
 * that report the status of the event. - a clear message that will be reported
 * to the user. describing what was done or the error that occurred.
 */
public enum MessageGeneralEnum {

    VALIDATION_FAILED_ENVIRONMENT_NOTACTIVE(50, "FA", "The environment selected isn't active."),
    VALIDATION_FAILED_TESTCASE_NOTACTIVE(51, "FA", "The test case selected isn't active."),
    VALIDATION_FAILED_TYPE_DIFFERENT(52, "FA", "The type of environment is defined as Comparison but not the test case."),
    VALIDATION_FAILED_RANGE_DIFFERENT(53, "FA", "The Build/Revision of test case isn't in the range of build."),
    VALIDATION_FAILED_RANGE_WRONGFORMAT(54, "FA", "The Build/Revision of test case isn't formatted as expected."),
    VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_NOTDEFINED(54, "FA", "The Build/Revision of environment isn't defined. To check that the testcase can be executed in a specific Build/Revision range, Build/Revision of the environment must be set from BuildRevisionParameter page."),
    VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_BADLYDEFINED(54, "FA", "The Build/Revision of environment isn't properly defined. To check that the testcase can be executed in a specific Build/Revision range, Build/Revision of the environment must be set from BuildRevisionParameter page."),
    VALIDATION_FAILED_TARGET_DIFFERENT(55, "FA", "The target Build/Revision of test case isn't in the range of build."),
    VALIDATION_FAILED_TARGET_WRONGFORMAT(56, "FA", "The target Build/Revision of test case isn't formatted as expected."),
    VALIDATION_FAILED_ISACTIVEQA_NOTDEFINED(57, "FA", "The test case isn't defined to run in QA environment. You try to run it on '%ENV%' that belong to the QA environment group."),
    VALIDATION_FAILED_ISACTIVEUAT_NOTDEFINED(58, "FA", "The test case isn't defined to run in UAT environment. You try to run it on '%ENV%' that belong to the UAT environment group."),
    VALIDATION_FAILED_ISACTIVEPROD_NOTDEFINED(59, "FA", "The test case isn't defined to run in Production environment. You try to run it on '%ENV%' that belong to the PROD environment group."),
    VALIDATION_FAILED_COUNTRY_NOTDEFINED(60, "FA", "The test case isn't defined to run in selected country."),
    VALIDATION_FAILED_ENVIRONMENT_NOTDEFINED(61, "FA", "The environment selected '%ENV%' belong to a group of environment '%ENVGP%' that is neither DEV, QA, UAT and PROD."),
    VALIDATION_FAILED_ENVIRONMENT_UNDER_MAINTENANCE(62, "FA", "The environment %SYSTEM% - %COUNTRY% - %ENV% is under daily maintenance (%START% to %END%). You try to run a test against an environment that is currently under maintenance."),
    VALIDATION_FAILED_SELENIUM_COULDNOTCONNECT(63, "FA", "Could not contact Robot server on %SSIP% using port %SSPORT%. Possible causes are invalid address of the remote server or browser start-up failure. %ERROR%"),
    VALIDATION_FAILED_SELENIUM_NOCONNECTION(63, "FA", "The selenium webdriver is not set because we are on a manual execution"),
    VALIDATION_FAILED_SIKULI_COULDNOTCONNECT(63, "FA", "Could not contact Sikuli server on %SSIP% using port %SSPORT%. Possible causes are invalid address of the remote server or cerberus-extension-sikuli not started properly."),
    VALIDATION_FAILED_APPLICATION_NOT_FOUND(64, "FA", "Application '%APPLI%' does not exist."),
    VALIDATION_FAILED_COUNTRYENV_NOT_FOUND(65, "FA", "System '%SYSTEM%' Country '%COUNTRY%' environment '%ENV%' parameters does not exist."),
    VALIDATION_FAILED_COUNTRYENVAPP_NOT_FOUND(66, "FA", "Country '%COUNTRY%' environment '%ENV%' application '%APPLI%' parameters does not exist or is not active."),
    VALIDATION_FAILED_TESTCASE_NOT_FOUND(67, "FA", "The test case ('%TEST%'-'%TESTCASE%') does not exist."),
    VALIDATION_FAILED_COULDNOTCREATE_RUNID(68, "FA", "RunID could not be created."),
    VALIDATION_FAILED_OUTPUTFORMAT_INVALID(69, "FA", "outputformat parameter value '%PARAM%' is not valid."),
    VALIDATION_FAILED_VERBOSE_INVALID(70, "FA", "verbose parameter value '%PARAM%' is not valid."),
    VALIDATION_FAILED_COUNTRY_NOT_FOUND(71, "FA", "Country '%COUNTRY%' does not exist."),
    VALIDATION_FAILED_BROWSER_NOT_SUPPORTED(72, "FA", "Browser %BROWSER% not supported."),
    VALIDATION_FAILED_TESTCASE_ISMANUAL(73, "FA", "The test case group is MANUAL and cannot be executed in an automated way."),
    VALIDATION_FAILED_SCREENSHOT_INVALID(80, "FA", "screenshot parameter value '%PARAM%' is not valid."),
    VALIDATION_FAILED_MANUALURL_INVALID(81, "FA", "ManualURL parameter activated but myhost parameter empty. Either desactivate the manual URL mode or define at least the myhost parameter."),
    VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST(82, "FA", "Environment '%ENV%' does not exit."),
    VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST_MAN(82, "FA", "The environment you defined in myenvdata parameter '%ENV%' does not exit."),
    VALIDATION_FAILED_PRIORITY_DOESNOTEXIST(82, "FA", "The priority '%PRIO%' you defined in testcase  does not exit."),
    VALIDATION_FAILED_SELENIUM_EMPTYORBADIP(83, "FA", "Selenium IP parameter (ss_ip) : '%IP%' is empty or badly formated."),
    VALIDATION_FAILED_SELENIUM_EMPTYORBADPORT(84, "FA", "Selenium Port parameter (ss_p) : '%PORT%' is empty or badly formated."),
    VALIDATION_FAILED_URL_MALFORMED(86, "FA", "URL to access Robot server '%URL%' is not correct and cannot be handled by Cerberus. Please check your Robot server and port parameter."),
    VALIDATION_FAILED_TEST_NOT_FOUND(87, "FA", "Test '%TEST%' does not exist."),
    VALIDATION_FAILED_TEST_NOTACTIVE(88, "FA", "The test '%TEST%' isn't active."),
    VALIDATION_FAILED_USERAGENTDIFFERENT(90, "FA", "User Agent has been specified at robot and TestCase level with different values. TestCase : '%UATESTCASE%' Robot : '%UAROBOT%'"),
    VALIDATION_FAILED_CERBERUSEXECUTORNOTAVAILABLE(90, "FA", "Could not connect to Cerberus Robot Proxy host '%HOST%' on port %PORT%. Check the Cerberus Robot Proxy is up and running, or unselect the Proxy flag at robot executor level (robot : %ROBOT%, executor : %ROBOTEXE%)."),
    VALIDATION_FAILED_KAFKACONSUMERSEEK(90, "FA", "Could not start execution because failed when trying to retreive Kafka topics latest offsets on Service %SERVICE%. %DETAIL%"),
    VALIDATION_FAILED_INSTANCE_INACTIVE(91, "FA", "This Cerberus Instance has been temporary disabled. Please try again later."),
    VALIDATION_SUCCEEDED(89, "PE", "The validation succeeded"),
    EXECUTION_PE_TESTSTARTED(5, "PE", "Test started..."),
    EXECUTION_PE_CHECKINGPARAMETERS(5, "PE", "Checking parameters..."),
    EXECUTION_PE_LOADINGDATA(5, "PE", "Loading data..."),
    EXECUTION_PE_VALIDATIONSTARTING(5, "PE", "Validation is beeing done..."),
    EXECUTION_PE_PREPARINGROBOTSERVER(5, "PE", "Preparing Robot Capabilities..."),
    EXECUTION_PE_STARTINGROBOTSERVER(5, "PE", "Connection to Robot Server (%IP%)..."),
    EXECUTION_PE_CREATINGRUNID(5, "PE", "Creating RunID..."),
    EXECUTION_PE_LOADINGDETAILEDDATA(5, "PE", "Loading detailed data..."),
    EXECUTION_PE_TESTEXECUTING(5, "PE", "Test is executing..."),
    EXECUTION_PE_LOADINGKAFKACONSUMERS(5, "PE", "Test is getting current KAFKA Partitions latest offset..."),
    EXECUTION_OK(1, "OK", "The test case finished successfully"),
    EXECUTION_KO(2, "KO", "The test case finished, but failed on validations."),
    EXECUTION_FA(3, "FA", "The test case failed to be executed. More likely due to an error in the test or in Cerberus configuration."),
    EXECUTION_FA_SERVLETVALIDATONS(14, "FA", "The test case failed to be submitted."),
    EXECUTION_FA_ACTION(4, "FA", "The test case failed to be executed because of an action."),
    EXECUTION_FA_SELENIUM(8, "FA", "The test case failed to be executed. Could not start Robot Server. %MES%."),
    EXECUTION_FA_CONNECTIVITY(10, "FA", "The test case failed to be executed. Connectivity issues were found."),
    EXECUTION_FA_CERBERUS(9, "FA", "The test case failed to be executed due to error in Cerberus, please contact Administrator of Cerberus. %MES%."),
    EXECUTION_FA_CONDITION(11, "FA", "The test case failed to be executed. An error occured when evaluating the %AREA%condition '%COND%'. %MES%"),
    EXECUTION_FA_DECODE(11, "FA", "The test case failed to be executed. An error occured when decoding the %AREA%. %MES%"),
    EXECUTION_FA_CAPABILITYDECODE(11, "FA", "The test case failed to be executed. An error occured when decoding the %AREA%. %MES%"),
    EXECUTION_FA_ROBOTNOTEXIST(11, "FA", "Robot '%ROBOT%' does not exist"),
    EXECUTION_FA_ROBOTBESTEXECUTORNOTEXIST(11, "FA", "Could not get the best Executor of Robot '%ROBOT%'"),
    EXECUTION_FA_ROBOTEXECUTORNOTEXIST(11, "FA", "Executor '%EXECUTOR%' of Robot '%ROBOT%' does not exist"),
    EXECUTION_FA_ROBOTNOTACTIVE(11, "FA", "Robot '%ROBOT%' is not Active."),
    EXECUTION_FA_ROBOTEXECUTORNOTACTIVE(11, "FA", "Executor '%EXECUTOR%' of Robot '%ROBOT%' is not Active."),
    EXECUTION_CA(6, "CA", "The test case has been cancelled by the user."),
    EXECUTION_NA(7, "NA", "The test case could not be run because of missing testing data."),
    EXECUTION_NE_CONDITION(12, "NE", "The test case was not executed following the evaluation of the condition '%COND%'. %MES%"),
    EXECUTION_WE(13, "WE", ""),
    EXECUTION_NE(14, "NE", "The test case was not executed."),
    APPLICATION_NOT_FOUND(100, "FA", "Application does not exist."),
    NOT_IMPLEMEMTED(101, "FA", "Missing data."),
    NO_DATA_FOUND(102, "FA", "Missing data."),
    SQLLIB_NOT_FOUND(103, "FA", "SQL Library was not found."),
    CANNOT_UPDATE_TABLE(104, "", "Cannot update table."),
    SOAPLIB_NOT_FOUND(105, "FA", "SOAP Library was not found"),
    SOAPLIB_MALFORMED_URL(106, "FA", "SOAP Attachment File badly configured"),
    GUI_TEST_CREATION_NOT_HAVE_RIGHT(403, "", "Error : You dont have the user right to create a Test. Please contact your Cerberus Administrator."),
    GUI_TEST_DUPLICATION_NOT_EXISTING_TEST(403, "", "Error : You're trying to duplicate a test which does not exist anymore."),
    GUI_TEST_CREATION_ISSUE(403, "", "Error : A problem has been found inserting data in database. Please contact your Cerberus Administrator."),
    GUI_ERROR_INSERTING_DATA(403, "", "Error : A problem has been found inserting data in database. Please try later or contact your Cerberus Administrator with the following details : %DETAILS%."),
    GUI_TESTCASE_DUPLICATION_ALREADY_EXISTS(403, "", "Error : You're trying to duplicate a testcase which already exists."),
    GUI_TESTCASE_DELETE_USED_STEP(403, "", "Error : You're trying to delete a testcase which have some step used in other tests. Please remove the link before delete this testcase."),
    GUI_TESTCASE_NON_ADMIN_SAVE_WORKING_TESTCASE(403, "", "Error : You're trying to save a WORKING testcase without having the TestAdmin right to do so."),
    GUI_NO_ROBOT_EXECUTOR_AVAILABLE(403, "FA", "No robot executor available for robot %ROBOT%"),
    GUI_NO_ROBOT_AVAILABLE_FOR_TYPE(403, "FA", "No robot of type %TYPE% is available"),
    XRAY_MISSING_PARAMETERS(102, "FA", "Missing XRay parameter."),
    // Data operations
    DATA_OPERATION_SUCCESS(000, MessageCodeEnum.GENERIC_CODE_SUCCESS.getCodeString(), "The requested operation was concluded with success."),
    DATA_OPERATION_WARNING(000, MessageCodeEnum.GENERIC_CODE_WARNING.getCodeString(), "The requested operation was concluded but with warnings."),
    DATA_OPERATION_ERROR(900, MessageCodeEnum.GENERIC_CODE_ERROR.getCodeString(), "An error occurred while executing the requested operation !"),
    DATA_OPERATION_ERROR_WITH_DETAIL(900, MessageCodeEnum.GENERIC_CODE_ERROR.getCodeString(), "An error occurred while executing the requested operation ! %DETAIL%"),
    DATA_OPERATION_ERROR_WITH_REQUEST(900, MessageCodeEnum.GENERIC_CODE_ERROR.getCodeString(), "An error occurred while executing the requested operation ! request : %REQUEST%."),
    DATA_OPERATION_ERROR_DUPLICATE(900, MessageCodeEnum.GENERIC_CODE_ERROR.getCodeString(), "The %ITEM% that you are trying to %OPERATION% conflicts with an existing one! Please check for duplicates! %REASON%"),
    GENERIC_SUCCESS(000, MessageCodeEnum.GENERIC_CODE_SUCCESS.getCodeString(), "The requested operation was concluded with success."),
    GENERIC_WARNING(000, MessageCodeEnum.GENERIC_CODE_WARNING.getCodeString(), "The requested operation was concluded but with warnings."),
    GENERIC_ERROR(900, MessageCodeEnum.GENERIC_CODE_ERROR.getCodeString(), "An error occured while executing the requested operation !");

    private final int code;
    private final String codeString;
    private final String description;

    private MessageGeneralEnum(int tempCode, String tempCodeString, String tempDesc) {
        this.code = tempCode;
        this.codeString = tempCodeString;
        this.description = tempDesc;
    }

    public int getCode() {
        return this.code;
    }

    public String getCodeString() {
        return codeString;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return "MessageGeneralEnum{" + "code=" + code + ", codeString=" + codeString + ", description=" + description + '}';
    }

}
