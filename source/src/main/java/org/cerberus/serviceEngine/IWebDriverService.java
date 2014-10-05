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

import java.awt.image.BufferedImage;
import java.util.List;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.Session;
import org.cerberus.exception.CerberusEventException;
import org.openqa.selenium.Capabilities;

/**
 *
 * @author bcivel
 */
public interface IWebDriverService {

    String getValueFromHTMLVisible(Session session, String locator);

    String getValueFromHTML(Session session, String locator);

    String getAlertText(Session session);

    String getValueFromJS(Session session, String script);
    
    boolean isElementPresent(Session session, String locator);

    boolean isElementVisible(Session session, String locator);

    boolean isElementNotVisible(Session session, String locator);
    
    boolean isElementInElement(Session session, String element, String childElement);
    
    boolean isElementNotClickable(Session session, String locator);
    
    boolean isElementClickable(Session session, String locator);

    String getPageSource(Session session);

    String getTitle(Session session);

    /**
     * @return Method return a string with the right part of the URL in order to
     * be agnostic of the environment. ex :
     * http://redoute.com/mypathlevel1/mypathlevel2/file.aspx will return
     * /mypathlevel1/mypathlevel2/file.aspx
     * @throws org.cerberus.exception.CerberusEventException in case the URL does not contain the host of
     * the application beeing tested. That could happen if the application
     * redirect to a different host during the testcase execution.
     */
    String getCurrentUrl(Session session, String url) throws CerberusEventException;

    String getAttributeFromHtml(Session session, String locator, String attribute);
    
    String getFromCookie(Session session, String cookieName, String cookieParameter);

    BufferedImage takeScreenShot(Session session);
    
    List<String> getSeleniumLog(Session session);
    
    MessageEvent doSeleniumActionClick(Session session, String string1, String string2);
    
    MessageEvent doSeleniumActionMouseDown(Session session, String string1, String string2);

    MessageEvent doSeleniumActionMouseUp(Session session, String string1, String string2);

    MessageEvent doSeleniumActionSwitchToWindow(Session session, String string1, String string2);

    MessageEvent doSeleniumActionManageDialog(Session session, String object, String property);
    
    MessageEvent doSeleniumActionClickWait(Session session, String actionObject, String actionProperty);
    
    MessageEvent doSeleniumActionDoubleClick(Session session, String html, String property);

    MessageEvent doSeleniumActionType(Session session, String html, String property, String propertyName);

    MessageEvent doSeleniumActionMouseOver(Session session, String html, String property);
    
    MessageEvent doSeleniumActionMouseOverAndWait(Session session, String actionObject, String actionProperty);

    MessageEvent doSeleniumActionWait(Session session, String object, String property);

    MessageEvent doSeleniumActionKeyPress(Session session, String html, String property);
    
    MessageEvent doSeleniumActionOpenURL(Session session, String host, String value, String property, boolean withBase);

    MessageEvent doSeleniumActionSelect(Session session, String html, String property);
    
    MessageEvent doSeleniumActionUrlLogin(Session session, String host, String uri);

    MessageEvent doSeleniumActionFocusToIframe(Session session, String object, String property);

    MessageEvent doSeleniumActionFocusDefaultIframe(Session session);
    
    MessageEvent doSeleniumActionMouseDownMouseUp(Session session, String string1, String string2);
}
    
