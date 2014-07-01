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
package org.cerberus.entity;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 19/Dez/2012
 * @since 0.9.0
 */
public enum MessageEventEnum {

    /**
     * Message is used to feedback the result of any Cerberus event. Events
     * could by Property, Action, Control or even Step. For every event, we
     * have: - a number - a 2 digit code that report the status of the event. -
     * a clear message that will be reported to the user. describing what was
     * done or the error that occured. - a boolean that define whether the
     * complete test execution should stop or not. - a boolean that define
     * whether a screenshot will be done in case of problem (only if screenshot
     * option is set to 1). - the corresponding Execution message that will be
     * updated at the execution level.
     * <p/>
     * Code standard is : All SUCCESS are x00 (same code for all). All FAILED
     * are from x50 to x99 (different code for each). Pending is x99.
     */
    PROPERTY_SUCCESS(100, "OK", "Property caculated successfully.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_SQL(100, "OK", "SQL executed against database '%DB%' and JDBCPOOL '%JDBCPOOLNAME%'. SQL : '%SQL%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_SQL_RANDOM(100, "OK", "Random result fetch from SQL executed against database '%DB%' and JDBCPOOL '%JDBCPOOLNAME%'. SQL : '%SQL%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_SQL_RANDOM_NEW(100, "OK", "Random new result fetch from SQL executed against database '%DB%' and JDBCPOOL '%JDBCPOOLNAME%'. SQL : '%SQL%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_SQL_NOTINUSE(100, "OK", "Result from property not currently used fetch from SQL executed against database '%DB%' and JDBCPOOL '%JDBCPOOLNAME%'. SQL : '%SQL%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_HTML(100, "OK", "HTML property calculated with '%VALUE%' from element '%ELEMENT%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_HTMLVISIBLE(100, "OK", "HTML Visible property calculated with '%VALUE%' from element '%ELEMENT%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_GETFROMXML(100, "OK", "Property from Xml '%VALUE1%' calculated and returned '%VALUE2%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_RANDOM(100, "OK", "Random property calculated with '%VALUE%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_TEXT(100, "OK", "Text property calculated with '%VALUE%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_RANDOM_NEW(100, "OK", "Random New property calculated with '%VALUE%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_GETATTRIBUTEFROMHTML(100, "OK", "Attribute '%ATTRIBUTE%' has returned '%VALUE%' for element '%ELEMENT%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_TESTDATA(100, "OK", "TestData %PROPERTY% correctly returned '%VALUE%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_SUCCESS_SOAP(100, "OK", "SOAP Request executed", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_FAILED(150, "KO", "PROPERTY_ERROR Generic error on getting the property.", true, false, MessageGeneralEnum.EXECUTION_NA),
    PROPERTY_FAILED_NO_PROPERTY_DEFINITION(151, "", "Warning, Property not defined for %PROP% and country %COUNTRY%.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    PROPERTY_FAILED_SQL(152, "KO", "An error occur when connecting to ?! Error detail: ?", true, false, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_SQL_NODATA(153, "KO", "The SQL performed against database %DB% and JDBCPOOL %JDBCPOOLNAME% returned no data to test. SQL : %SQL%", true, false, MessageGeneralEnum.EXECUTION_NA),
    PROPERTY_FAILED_SQL_ERROR(154, "KO", "The SQL '%SQL%' has an error : '%EX%'.", true, false, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_SQL_CANNOTACCESSJDBC(155, "KO", "An error occur when connecting to JDBC datasource '%JDBC%'. Please verify with your administrator that the JDBC is configured inside the application server. Error detail: %EX%", true, false, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_SQL_JDBCPOOLNOTCONFIGURED(156, "KO", "The JDBC connection pool name does not exist for the corresponding System : %SYSTEM% Country : %COUNTRY%, environment : %ENV% and database : %DB% . Please define it inside the database.", true, false, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_SQL_EMPTYJDBCPOOL(157, "KO", "The JDBC connection pool name is empty for the corresponding System : %SYSTEM% Country : %COUNTRY%, environment : %ENV% and database : %DB% . Please define it inside the database.", true, false, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_SQL_SQLLIB_NOTEXIT(158, "KO", "The SQL Lib %SQLLIB% does not exist. Please define it inside the database or pick another one.", true, false, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_SQL_GENERIC(159, "KO", "An unknown error occur when connecting to %JDBC%.", true, false, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_HTML_ELEMENTDONOTEXIST(180, "KO", "Failed to calculate property because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST(181, "KO", "Failed to calculate visible html property because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_UNKNOWNPROPERTY(182, "KO", "Property function '%PROPERTY%' does not exist or is not supported by the engine.", true, false, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_TEXTRANDOMLENGHT0(183, "KO", "RANDOM or RANDOMNEW text Property function cannot have a lenght at 0. Pick STATIC nature (if you want an empty string) or increase the lenght.", true, false, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST(184, "KO", "TestData %PROPERTY% do not exist!", true, true, MessageGeneralEnum.EXECUTION_NA),
    PROPERTY_FAILED_JS_EXCEPTION(185, "NA", "Failed to Execute script!\nException reached : %EXCEPTION%.", true, true, MessageGeneralEnum.EXECUTION_NA),
    PROPERTY_FAILED_GETFROMXML(181, "NA", "Failed to get Data from '%VALUE1%' because could not find '%VALUE2%'!", true, true, MessageGeneralEnum.EXECUTION_FA),
    PROPERTY_PENDING(199, "PE", "Calculating property...", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS(200, "OK", "", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_CLICK(200, "OK", "Element '%ELEMENT%' clicked.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_CLICKANDWAIT(200, "OK", "Element '%ELEMENT%' clicked and waited %TIME% ms.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_CLICKANDNOWAIT(200, "OK", "Element '%ELEMENT%' clicked and waited for page to load", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_TYPE(200, "OK", "Element '%ELEMENT%' feeded with '%DATA%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_DOUBLECLICK(200, "OK", "Element '%ELEMENT%' double clicked.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_URLLOGIN(200, "OK", "Opened '%URL%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_MOUSEOVER(200, "OK", "Mouse moved over '%ELEMENT%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_MOUSEOVERANDWAIT(200, "OK", "Mouse moved over '%ELEMENT%' and waited %TIME% ms.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_WAIT_TIME(200, "OK", "Waited %TIME% ms.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_WAIT_ELEMENT(200, "OK", "Waited for %ELEMENT%.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_KEYPRESS(200, "OK", "Element '%ELEMENT%' keypress with '%DATA%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_OPENURL(200, "OK", "Opened URL '%URL%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_SELECT(200, "OK", "Element '%ELEMENT%' selected with '%DATA%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_PROPERTYCALCULATED(200, "OK", "Property '%PROP%' has been calculated.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_FOCUSTOIFRAME(200, "OK", "Focus of Selenium was changed to Iframe '%IFRAME%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_FOCUSDEFAULTIFRAME(200, "OK", "Focus of Selenium was changed to default Iframe", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_TAKESCREENSHOT(200, "OK", "Screenshot taken.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_GETPAGESOURCE(200, "OK", "Page Source has been recorded.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_MOUSEDOWN(200, "OK", "Mouse Left Click pressed on Element '%ELEMENT%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_MOUSEUP(200, "OK", "Mouse Left Click Released on Element '%ELEMENT%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_SWITCHTOWINDOW(200, "OK", "Focus of Selenium was changed to Window '%WINDOW%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_CLOSE_ALERT(200, "OK", "Alert popup is closed !", true, true, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_CALLSOAP(200, "OK", "Call to SOAP '%SOAPNAME%' executed successfully and stored to memory!", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_SUCCESS_MOUSEDOWNMOUSEUP(200, "OK", "Mouse Left Click pressed and released on Element '%ELEMENT%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_FAILED(250, "FA", "Unknown Action Error.", true, true, MessageGeneralEnum.EXECUTION_FA),
    ACTION_FAILED_CLICK(251, "FA", "Failed to click on '%ELEMENT%'.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_SELENIUM_CONNECTIVITY(252, "CA", "The test case is canceled due to lost connection to Selenium Server!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_NO_SUCH_ELEMENT(253, "FA", "Identifier '?=' isn't recognized! Use: id=, name=, class=, css= or xpath=.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_NO_ELEMENT_TO_CLICK(254, "FA", "Object and Property are ‘null’. At least one is mandatory in order to perform the action click.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_CLICKANDWAIT_GENERIC(255, "FA", "Object is 'null'. This is mandatory in order to perform the action click and wait.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_CLICKANDWAIT(256, "FA", "Element '%ELEMENT%' clicked but failed to wait '%TIME%' ms.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_TYPE(257, "FA", "Object and/or Property are ‘null’. Both are mandatory in order to perform the action type.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_DOUBLECLICK(258, "FA", "Object and Property are ‘null’. At least one is mandatory in order to perform the action double click.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_URLLOGIN(259, "FA", "Failed to open '%URL%'.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_MOUSEOVER(260, "FA", "Object and property are ‘null’. At least one is mandatory in order to perform the action mouse over.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_MOUSEOVERANDWAIT_GENERIC(261, "FA", "Object is 'null'. This is mandatory in order to perform the action mouse over and wait.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_MOUSEOVERANDWAIT(262, "FA", "Mouse over '%ELEMENT%' but failed to wait '%TIME%' ms.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_WAIT(263, "FA", "Failed to wait '%TIME%' ms.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_KEYPRESS(264, "FA", "Object and/or Property are ‘null’. Both are mandatory in order to perform the action type.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_OPENURL(265, "FA", "Failed to open '%URL%'.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_SELECT(266, "FA", "Object and/or Property are ‘null’. Both are mandatory in order to perform the action type.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_CLICK_NO_SUCH_ELEMENT(267, "FA", "Failed to click because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_DOUBLECLICK_NO_SUCH_ELEMENT(268, "FA", "Failed to double click because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_TYPE_NO_SUCH_ELEMENT(269, "FA", "Failed to type because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT(270, "FA", "Failed to move mouse over because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_WAIT_NO_SUCH_ELEMENT(271, "FA", "Failed to wait because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_KEYPRESS_NO_SUCH_ELEMENT(272, "FA", "Failed to press key because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_SELECT_NO_SUCH_ELEMENT(273, "FA", "Failed to select because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_SELECT_NO_IDENTIFIER(274, "FA", "Identifier '%IDENTIFIER%=' isn't recognized! Use: value=, label= or index=.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_CLICKANDWAIT_NO_NUMERIC(275, "FA", "Failed to wait because '%TIME%' in not numeric!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_MOUSEOVERANDWAIT_NO_NUMERIC(276, "FA", "Failed to wait because '%TIME%' in not numeric!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_UNKNOWNACTION(277, "FA", "Action %ACTION% does not exist or is not supported yet.", true, false, MessageGeneralEnum.EXECUTION_FA),
    ACTION_FAILED_PROPERTYFAILED(278, "FA", "Action failed because there were an error on the property definition.", true, false, MessageGeneralEnum.EXECUTION_FA),
    ACTION_FAILED_SELECT_NO_SUCH_VALUE(279, "FA", "Found the element '%ELEMENT%', but failed to select because could not find '%DATA%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_FOCUS_NO_SUCH_ELEMENT(280, "FA", "Failed to focus to iframe because could not find element '%IFRAME%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_SELECT_REGEX_INVALIDPATERN(281, "FA", "Pattern '%PATERN%' is not valid. Detailed error : %ERROR%", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_MOUSEUP_NO_SUCH_ELEMENT(282, "FA", "Failed to release click because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_MOUSEDOWN_NO_SUCH_ELEMENT(283, "FA", "Failed to left click because could not find element '%ELEMENT%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_SWITCHTOWINDOW_NO_SUCH_ELEMENT(280, "FA", "Failed to switch to window because could not find element '%WINDOW%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_CLOSE_ALERT(280, "FA", "Failed to close to alert popup !", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_FAILED_CALLSOAP(286, "FA", "Failed to call the SOAP '%SOAPNAME%'!", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_NOTEXECUTED_NO_PROPERTY_DEFINITION(290, "NA", "Not executed because Property '%PROP%' is not defined for the country '%COUNTRY%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION(291, "FA", "Not executed because Action '%ACTION%' is not supported for application type '%APPLICATIONTYPE%'.", true, true, MessageGeneralEnum.EXECUTION_FA_ACTION),
    ACTION_PENDING(299, "PE", "Doing Action...", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS(300, "OK", "", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_EQUAL(300, "OK", "'%STRING1%' is equal to '%STRING2%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_CONTAINS(300, "OK", "'%STRING1%' contains '%STRING2%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_DIFFERENT(300, "OK", "'%STRING1%' is different from '%STRING2%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_GREATER(300, "OK", "'%STRING1%' is greater than '%STRING2%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_MINOR(300, "OK", "'%STRING1%' is minor than '%STRING2%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_PRESENT(300, "OK", "Element '%STRING1%' is present on the page.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_NOTPRESENT(300, "OK", "Element '%STRING1%' is not present on the page.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_VISIBLE(300, "OK", "Element '%STRING1%' is visible on the page.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_NOTVISIBLE(300, "OK", "Element '%STRING1%' is present and not visible on the page.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_TEXTINELEMENT(300, "OK", "Element '%STRING1%' with value '%STRING2%' is equal to '%STRING3%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_TEXTINPAGE(300, "OK", "Pattern '%STRING1%' found in page.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_TEXTNOTINPAGE(300, "OK", "Pattern '%STRING1%' not found in page.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_URL(300, "OK", "Current page '%STRING1%' is equal to '%STRING2%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_TITLE(300, "OK", "Current title '%STRING1%' is equal to '%STRING2%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_REGEXINELEMENT(300, "OK", "Element '%STRING1%' with value '%STRING2%' match '%STRING3%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_TEXTINALERT(300, "OK", "Alert text with value '%STRING1%' match '%STRING2%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_ELEMENTINELEMENT(300, "OK", "Element '%STRING1%' in child of element '%STRING2%'.", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_SUCCESS_SIMILARTREE(300, "OK", "Tree '%STRING1%' is equal to tree '%STRING2%'", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    CONTROL_FAILED(350, "KO", "Control Failed", false, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_UNKNOWNCONTROL(351, "KO", "Control function '%CONTROL%' does not exist or is not supported.", true, false, MessageGeneralEnum.EXECUTION_FA),
    CONTROL_FAILED_FATAL(352, "KO", "Fatal Control Failed", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_NO_SUCH_ELEMENT(353, "FA", "Element '%ELEMENT%' doesn't exist. Selenium Exception : %SELEX%.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_SELENIUM_CONNECTIVITY(354, "CA", "The test case is canceled due to lost connection to Selenium Server! Detailed error : %ERROR%", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_PROPERTY_NOTNUMERIC(355, "KO", "At least one of the Properties is not numeric, can not compare properties!", true, false, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_URL_NOT_MATCH_APPLICATION(356, "FA", "Cannot find application host '%HOST% inside current URL %URL%. Maybe this is due to a redirection done on the web site. That can be corrected by modifying the application URL.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_EQUAL(357, "KO", "'%STRING1%' is not equal to '%STRING2%'.", true, false, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_DIFFERENT(358, "KO", "'%STRING1%' is not different from '%STRING2%'.", true, false, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_GREATER(359, "KO", "'%STRING1%' is not greater than '%STRING2%'.", true, false, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_MINOR(360, "KO", "'%STRING1%' is not minor than '%STRING2%'.", true, false, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_PRESENT(361, "KO", "Element '%STRING1%' is not present on the page.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_PRESENT_NULL(362, "KO", "Object is 'null'. This is mandatory in order to perform the control verify element present", true, false, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_NOTPRESENT(363, "KO", "Element '%STRING1%' is present on the page.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_NOTPRESENT_NULL(364, "KO", "Object is 'null'. This is mandatory in order to perform the control verify element not present", true, false, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_VISIBLE(365, "KO", "Element '%STRING1%' not visible on the page.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_NOTVISIBLE(365, "KO", "Element '%STRING1%' is visible on the page.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_VISIBLE_NULL(366, "KO", "Object is 'null'. This is mandatory in order to perform the control verify element visible", true, false, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_NOTVISIBLE_NULL(366, "KO", "Object is 'null'. This is mandatory in order to perform the control verify element not visible", true, false, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TEXTINELEMENT(367, "KO", "Element '%STRING1%' with value '%STRING2%' is not equal to '%STRING3%'.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TEXTINELEMENT_NULL(368, "KO", "Found Element '%STRING1%' but can not find text or value.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TEXTINELEMENT_NO_SUCH_ELEMENT(369, "KO", "Failed to verifyText because could not find element '%ELEMENT%'", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TEXTINPAGE(370, "KO", "Pattern '%STRING1%' not found in page!", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TEXTINPAGE_INVALIDPATERN(371, "KO", "Pattern '%PATERN%' is not valid. Detailed error : %ERROR%", true, false, MessageGeneralEnum.EXECUTION_FA),
    CONTROL_FAILED_URL(372, "KO", "Current page '%STRING1%' is not equal to '%STRING2%'", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TITLE(373, "KO", "Current title '%STRING1%' is not equal to '%STRING2%'", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TEXTNOTINPAGE(374, "KO", "Pattern '%STRING1%' found in page!", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TEXTNOTINPAGE_INVALIDPATERN(375, "KO", "Pattern '%PATERN%' is not valid. Detailed error : %ERROR%", true, false, MessageGeneralEnum.EXECUTION_FA),
    CONTROL_FAILED_REGEXINELEMENT(376, "KO", "Element '%STRING1%' with value '%STRING2%' does not match '%STRING3%'.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_REGEXINELEMENT_NULL(377, "KO", "Object is 'null'. This is mandatory in order to perform the control verify regex in element", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_REGEXINELEMENT_NO_SUCH_ELEMENT(378, "KO", "Failed to verifyRegex because could not find element '%ELEMENT%'", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_REGEXINELEMENT_INVALIDPATERN(379, "KO", "Pattern '%PATERN%' is not valid. Detailed error : %ERROR%", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TEXTINALERT(380, "KO", "Alert text with value '%STRING1%' does not match '%STRING2%'.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_TEXTINALERT_NULL(381, "KO", "Alert popup does not have text.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_CONTAINS(382, "KO", "'%STRING1%' does not contain '%STRING2%'.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_ELEMENTINELEMENT(383, "KO", "Element '%STRING1%' is not child of element '%STRING2%'.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_FAILED_SIMILARTREE(385, "KO", "Tree '%STRING1%' is not the same than tree '%STRING2%'.", true, true, MessageGeneralEnum.EXECUTION_KO),
    CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION(384, "KO", "Not executed because Control '%CONTROL%' is not supported for application type '%APPLICATIONTYPE%'.", true, true, MessageGeneralEnum.EXECUTION_FA),
    CONTROL_PENDING(399, "PE", "Control beeing performed...", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    STEP_SUCCESS(400, "OK", "", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    STEP_FAILED(450, "KO", "", false, true, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    STEP_PENDING(499, "PE", "Step running...", false, false, MessageGeneralEnum.EXECUTION_PE_TESTSTARTED),
    NO_DATA_FOUND(500, "", "Could not find row.", true, true, MessageGeneralEnum.EXECUTION_FA),
    APPLICATION_NOT_FOUND(501, "FA", "Application of the testcase does not exist.", true, true, MessageGeneralEnum.EXECUTION_FA),
    TESTCASEEXECUTION_CANNOTFINDTESTCASEEXECUTIONBYCRITERIA(502, "CA", "An error occur when trying to find execution by criteria", true, true, MessageGeneralEnum.EXECUTION_FA),
    TESTCASEEXECUTION_CANNOTINSERTTESTCASEEXECUTION(503, "CA", "An error occur when trying to insert a testcaseexecution", true, true, MessageGeneralEnum.EXECUTION_FA),
    NOT_IMPLEMEMTED(900, "", "Not Implememted.", true, true, MessageGeneralEnum.EXECUTION_FA) //TODO add env property => configure com.cerberus.environment and JNDI
    ;
    private final int code;
    private final String codeString;
    private final String description;
    private final boolean stopTest;
    private final boolean doScreenshot;
    private final MessageGeneralEnum message;

    private MessageEventEnum(int tempCode, String tempCodeString, String tempDesc, boolean tempStopTest, boolean tempDoScreenshot, MessageGeneralEnum tempMessage) {
        this.code = tempCode;
        this.codeString = tempCodeString;
        this.description = tempDesc;
        this.stopTest = tempStopTest;
        this.doScreenshot = tempDoScreenshot;
        this.message = tempMessage;
    }

    public String getDescription() {
        return description;
    }

    public MessageGeneralEnum getMessage() {
        return message;
    }

    public boolean isStopTest() {
        return stopTest;
    }

    public boolean isDoScreenshot() {
        return doScreenshot;
    }

    public int getCode() {
        return this.code;
    }

    public String getCodeString() {
        return codeString;
    }
}
