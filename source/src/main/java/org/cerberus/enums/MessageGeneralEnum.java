/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.enums;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 19/Dez/2012
 * @since 0.9.0
 */
public enum MessageGeneralEnum {

    /**
     * Message is a generic Message that is used to feedback the result of any Cerberus execution.
     * For every message, we have:
     * - a number
     * - a 2 digit code that report the status of the event.
     * - a clear message that will be reported to the user. describing what was done or the error that occurred.
     */

    VALIDATION_FAILED_ENVIRONMENT_NOTACTIVE(50, "", "The environment selected isn't active."),
    VALIDATION_FAILED_TESTCASE_NOTACTIVE(51, "", "The test case selected isn't active."),
    VALIDATION_FAILED_TYPE_DIFFERENT(52, "", "The type of environment is defined as Comparison but not the test case."),
    VALIDATION_FAILED_RANGE_DIFFERENT(53, "", "The Build/Revision of test case isn't in the range of build."),
    VALIDATION_FAILED_RANGE_WRONGFORMAT(54, "", "The Build/Revision of test case isn't formatted as expected."),
    VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_NOTDEFINED(54, "", "The Build/Revision of environment isn't defined. To check that the testcase can be executed in a specific Build/Revision range, Build/Revision of the environment must be set from BuildRevisionParameter page."),
    VALIDATION_FAILED_RANGE_ENVIRONMENT_BUILDREVISION_BADLYDEFINED(54, "", "The Build/Revision of environment isn't properly defined. To check that the testcase can be executed in a specific Build/Revision range, Build/Revision of the environment must be set from BuildRevisionParameter page."),
    VALIDATION_FAILED_TARGET_DIFFERENT(55, "", "The target Build/Revision of test case isn't in the range of build."),
    VALIDATION_FAILED_TARGET_WRONGFORMAT(56, "", "The target Build/Revision of test case isn't formatted as expected."),
    VALIDATION_FAILED_RUNQA_NOTDEFINED(57, "", "The test case isn't defined to run in QA environment. You try to run it on '%ENV%' that belong to the QA environment group."),
    VALIDATION_FAILED_RUNUAT_NOTDEFINED(58, "", "The test case isn't defined to run in UAT environment. You try to run it on '%ENV%' that belong to the UAT environment group."),
    VALIDATION_FAILED_RUNPROD_NOTDEFINED(59, "", "The test case isn't defined to run in Production environment. You try to run it on '%ENV%' that belong to the PROD environment group."),
    VALIDATION_FAILED_COUNTRY_NOTDEFINED(60, "", "The test case isn't defined to run in selected country."),
    VALIDATION_FAILED_ENVIRONMENT_NOTDEFINED(61, "", "The environment selected '%ENV%' belong to a group of environment '%ENVGP%' that is neither DEV, QA, UAT and PROD."),
    VALIDATION_FAILED_ENVIRONMENT_UNDER_MAINTENANCE(62, "", "The environment is under maintenance. You try to run a test aginst an environment that is currently under maintenance."),
    VALIDATION_FAILED_SELENIUM_COULDNOTCONNECT(63, "", "Could not contact Selenium server on %SSIP% using port %SSPORT%. Possible causes are invalid address of the remote server or browser start-up failure."),
    VALIDATION_FAILED_APPLICATION_NOT_FOUND(64, "", "Application '%APPLI%' does not exist."),
    VALIDATION_FAILED_COUNTRYENV_NOT_FOUND(65, "", "System '%SYSTEM%' Country '%COUNTRY%' environment '%ENV%' parameters does not exist."),
    VALIDATION_FAILED_COUNTRYENVAPP_NOT_FOUND(66, "", "Country '%COUNTRY%' environment '%ENV%' application '%APPLI%' parameters does not exist."),
    VALIDATION_FAILED_TESTCASE_NOT_FOUND(67, "", "The test case ('%TEST%'-'%TESTCASE%') does not exist."),
    VALIDATION_FAILED_COULDNOTCREATE_RUNID(68, "", "RunID could not be created."),
    VALIDATION_FAILED_OUTPUTFORMAT_INVALID(69, "", "outputformat parameter value '%PARAM%' is not valid."),
    VALIDATION_FAILED_VERBOSE_INVALID(70, "", "verbose parameter value '%PARAM%' is not valid."),
    VALIDATION_FAILED_COUNTRY_NOT_FOUND(71, "", "Country '%COUNTRY%' does not exist."),
    VALIDATION_FAILED_BROWSER_NOT_SUPPORTED(72, "", "Browser %BROWSER% not supported."),
    VALIDATION_FAILED_TESTCASE_ISMANUAL(73, "", "The test case group is MANUAL and cannot be executed in an automated way."),
    VALIDATION_FAILED_SCREENSHOT_INVALID(80, "", "screenshot parameter value '%PARAM%' is not valid."),
    VALIDATION_FAILED_MANUALURL_INVALID(81, "", "ManualURL parameter activated but myhost parameter empty. Either desactivate the manual URL mode or define at least the myhost parameter."),
    VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST(82, "", "Environment '%ENV%' does not exit."),
    VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST_MAN(82, "", "The environment you defined in myenvdata parameter '%ENV%' does not exit."),
    VALIDATION_FAILED_SELENIUM_EMPTYORBADIP(83, "", "Selenium IP parameter (ss_ip) : '%IP%' is empty or badly formated."),
    VALIDATION_FAILED_SELENIUM_EMPTYORBADPORT(84, "", "Selenium Port parameter (ss_p) : '%PORT%' is empty or badly formated."),
    VALIDATION_FAILED_VERBOSE_USED_WITH_INCORRECT_BROWSER(85, "", "Verbose should be used only with Firefox. For other browsers, it should be set to 0."),
    VALIDATION_FAILED_URL_MALFORMED(86, "", "URL to access Selenium server '%URL%' is not correct and cannot be handled by Cerberus. Please check your Selenium server and port parameter."),
    VALIDATION_FAILED_TEST_NOT_FOUND(87, "", "Test '%TEST%' does not exist."),
    VALIDATION_FAILED_TEST_NOTACTIVE(88, "", "The test '%TEST%' isn't active."),
    VALIDATION_FAILED_USERAGENTDIFFERENT(90, "", "User Agent has been specified at robot and TestCase level with different values. TestCase : '%UATESTCASE%' Robot : '%UAROBOT%'"),
    VALIDATION_SUCCEEDED(89, "", "The validation succeeded"),

    EXECUTION_PE_TESTSTARTED(5, "PE", "Test started..."),
    EXECUTION_PE_CHECKINGPARAMETERS(5, "PE", "Checking parameters..."),
    EXECUTION_PE_LOADINGDATA(5, "PE", "Loading data..."),
    EXECUTION_PE_VALIDATIONSTARTING(5, "PE", "Validation is beeing done..."),
    EXECUTION_PE_CREATINGRUNID(5, "PE", "Creating RunID..."),
    EXECUTION_PE_SELENIUMSTARTING(5, "PE", "Selenium Server is starting..."),
    EXECUTION_PE_LOADINGDETAILEDDATA(5, "PE", "Loading detailed data..."),
    EXECUTION_PE_TESTEXECUTING(5, "PE", "Test is executing..."),
    EXECUTION_OK(1, "OK", "The test case finished successfully"),
    EXECUTION_KO(2, "KO", "The test case finished, but failed on validations."),
    EXECUTION_FA(3, "FA", "The test case failed to be executed. More lickely due to an error in the test or in cerberus configuration."),
    EXECUTION_FA_ACTION(4, "FA", "The test case failed to be executed because of an action."),
    EXECUTION_FA_SELENIUM(8, "FA", "The test case failed to be executed. Could not start Selenium. %MES%."),    
    EXECUTION_FA_CONNECTIVITY(10, "FA", "The test case failed to be executed. Connectivity issues were found."),
    EXECUTION_FA_CERBERUS(9, "FA", "The test case failed to be executed due to error in Cerberus, please contact Administrator of Cerberus. %MES%."),
    EXECUTION_CA(6, "CA", "The test case has been cancelled by the user."),
    EXECUTION_NA(7, "NA", "The test case could not be run because of missing testing data."),
        
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

    // Data operations
    DATA_OPERATION_SUCCESS(000, MessageCodeEnum.GENERIC_CODE_SUCCESS.getCodeString(), "The requested operation was concluded with success."),
    DATA_OPERATION_WARNING(000, MessageCodeEnum.GENERIC_CODE_WARNING.getCodeString(), "The requested operation was concluded but with warnings."),
    DATA_OPERATION_ERROR(900, MessageCodeEnum.GENERIC_CODE_ERROR.getCodeString(), "An error occurred while executing the requested operation !"),
    GENERIC_SUCCESS(000, MessageCodeEnum.GENERIC_CODE_SUCCESS.getCodeString(), "The requested operation was concluded with success."),
    GENERIC_WARNING(000, MessageCodeEnum.GENERIC_CODE_WARNING.getCodeString(), "The requested operation was concluded but with warnings."),
    GENERIC_ERROR(900, MessageCodeEnum.GENERIC_CODE_ERROR.getCodeString(), "An error occurred while executing the requested operation !");
    
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
