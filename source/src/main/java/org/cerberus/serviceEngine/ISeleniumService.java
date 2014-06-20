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
package org.cerberus.serviceEngine;

import java.util.UUID;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.Selenium;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.openqa.selenium.Capabilities;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public interface ISeleniumService {

    TestCaseExecution startSeleniumServer(TestCaseExecution tCExecution, String host, String port, String browser,  String version, String platform, String ip, String login, int verbose, String country) throws CerberusException;

    boolean stopSeleniumServer(Selenium selenium);

    boolean startSeleniumBrowser(TestCaseExecution tCExecution, boolean record, String country, String browser, String version, String platform) throws CerberusException;

    String getValueFromHTMLVisible(Selenium selenium, String locator);

    String getValueFromHTML(Selenium selenium, String locator);

    String getAlertText(Selenium selenium);

    String getValueFromJS(Selenium selenium, String script);
    
    boolean isElementPresent(Selenium selenium, String locator);

    boolean isElementVisible(Selenium selenium, String locator);

    boolean isElementNotVisible(Selenium selenium, String locator);
    
    boolean isElementInElement(Selenium selenium, String element, String childElement);

    String getPageSource(Selenium selenium);

    String getTitle(Selenium selenium);

    /**
     * @return Method return a string with the right part of the URL in order to
     * be agnostic of the environment. ex :
     * http://redoute.com/mypathlevel1/mypathlevel2/file.aspx will return
     * /mypathlevel1/mypathlevel2/file.aspx
     * @throws org.cerberus.exception.CerberusEventException in case the URL does not contain the host of
     * the application beeing tested. That could happen if the application
     * redirect to a different host during the testcase execution.
     */
    String getCurrentUrl(Selenium selenium) throws CerberusEventException;

    Capabilities getUsedCapabilities(Selenium selenium);
     
    String getAttributeFromHtml(Selenium selenium, String locator, String attribute);

    void doScreenShot(Selenium selenium, String runId, String path);
    
    MessageEvent doSeleniumActionClick(Selenium selenium, String string1, String string2);
    
    MessageEvent doSeleniumActionMouseDown(Selenium selenium, String string1, String string2);

    MessageEvent doSeleniumActionMouseUp(Selenium selenium, String string1, String string2);

    MessageEvent doSeleniumActionSwitchToWindow(Selenium selenium, String string1, String string2);

    MessageEvent doSeleniumActionManageDialog(Selenium selenium, String object, String property);
    
    MessageEvent doSeleniumActionClickWait(Selenium selenium, String actionObject, String actionProperty);
    
    MessageEvent doSeleniumActionDoubleClick(Selenium selenium, String html, String property);

    MessageEvent doSeleniumActionType(Selenium selenium, String html, String property, String propertyName);

    MessageEvent doSeleniumActionMouseOver(Selenium selenium, String html, String property);
    
    MessageEvent doSeleniumActionMouseOverAndWait(Selenium selenium, String actionObject, String actionProperty);

    MessageEvent doSeleniumActionWait(Selenium selenium, String object, String property);

    MessageEvent doSeleniumActionKeyPress(Selenium selenium, String html, String property);
    
    MessageEvent doSeleniumActionOpenURL(Selenium selenium, String value, String property, boolean withBase);

    MessageEvent doSeleniumActionSelect(Selenium selenium, String html, String property);
    
    MessageEvent doSeleniumActionUrlLogin(Selenium selenium);

    MessageEvent doSeleniumActionFocusToIframe(Selenium selenium, String object, String property);

    MessageEvent doSeleniumActionFocusDefaultIframe(Selenium selenium);
    
    MessageEvent doSeleniumActionMouseDownMouseUp(Selenium selenium, String string1, String string2);
}
