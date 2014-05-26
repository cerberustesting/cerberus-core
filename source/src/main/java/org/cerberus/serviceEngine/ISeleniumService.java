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

import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageGeneral;
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

    MessageGeneral startSeleniumServer(long runId, String host, String port, String browser,  String version, String platform, String ip, String login, int verbose, String country);

    boolean isSeleniumServerReachable(String host, String port);

    boolean stopSeleniumServer();

    boolean startSeleniumBrowser(long runId, boolean record, String country, String browser, String version, String platform) throws CerberusException;

    String getValueFromHTMLVisible(String locator);

    String getValueFromHTML(String locator);

    String getAlertText();

    String getValueFromJS(String script);
    
    boolean isElementPresent(String locator);

    boolean isElementVisible(String locator);

    boolean isElementNotVisible(String locator);
    
    boolean isElementInElement(String element, String childElement);

    String getPageSource();

    String getTitle();

    /**
     * @return Method return a string with the right part of the URL in order to
     * be agnostic of the environment. ex :
     * http://redoute.com/mypathlevel1/mypathlevel2/file.aspx will return
     * /mypathlevel1/mypathlevel2/file.aspx
     * @throws org.cerberus.exception.CerberusEventException in case the URL does not contain the host of
     * the application beeing tested. That could happen if the application
     * redirect to a different host during the testcase execution.
     */
    String getCurrentUrl() throws CerberusEventException;

    Capabilities getUsedCapabilities();
     
    String getAttributeFromHtml(String locator, String attribute);

    void doScreenShot(String runId, String path);
    
    MessageEvent doSeleniumActionClick(String string1, String string2);
    
    MessageEvent doSeleniumActionMouseDown(String string1, String string2);

    MessageEvent doSeleniumActionMouseUp(String string1, String string2);

    MessageEvent doSeleniumActionSwitchToWindow(String string1, String string2);

    MessageEvent doSeleniumActionManageDialog(String object, String property);
    
    MessageEvent doSeleniumActionClickWait(String actionObject, String actionProperty);
    
    MessageEvent doSeleniumActionDoubleClick(String html, String property);

    MessageEvent doSeleniumActionType(String html, String property, String propertyName);

    MessageEvent doSeleniumActionMouseOver(String html, String property);
    
    MessageEvent doSeleniumActionMouseOverAndWait(String actionObject, String actionProperty);

    MessageEvent doSeleniumActionWait(String object, String property);

    MessageEvent doSeleniumActionKeyPress(String html, String property);
    
    MessageEvent doSeleniumActionOpenURL(String value, String property, boolean withBase);

    MessageEvent doSeleniumActionSelect(String html, String property);
    
    MessageEvent doSeleniumActionUrlLogin();

    MessageEvent doSeleniumActionFocusToIframe(String object, String property);

    MessageEvent doSeleniumActionFocusDefaultIframe();
}
