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
package org.cerberus.serviceEngine.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import org.apache.log4j.Level;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.Session;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.log.MyLogger;
import org.cerberus.serviceEngine.IWebDriverService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class WebDriverService implements IWebDriverService{


    private static final int TIMEOUT_MILLIS = 30000;
    private static final int TIMEOUT_WEBELEMENT = 300;

    
    private By getIdentifier(String input) {
        String identifier;
        String locator;

        String[] strings = input.split("=", 2);
        if (strings.length == 1) {
            identifier = "id";
            locator = strings[0];
        } else {
            identifier = strings[0];
            locator = strings[1];
        }

        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Finding selenium Element : " + locator + " by : " + identifier);

        if (identifier.equalsIgnoreCase("id")) {
            return By.id(locator);

        } else if (identifier.equalsIgnoreCase("name")) {
            return By.name(locator);

        } else if (identifier.equalsIgnoreCase("class")) {
            return By.className(locator);

        } else if (identifier.equalsIgnoreCase("css")) {
            return By.cssSelector(locator);

        } else if (identifier.equalsIgnoreCase("xpath")) {
            return By.xpath(locator);

        } else if (identifier.equalsIgnoreCase("link")) {
            return By.linkText(locator);

        } else if (identifier.equalsIgnoreCase("data-cerberus")) {
            return By.xpath("//*[@data-cerberus='" + locator + "']");

        } else {
            throw new NoSuchElementException(identifier);
        }
    }

    private WebElement getSeleniumElement(Session session, String input, boolean visible, boolean clickable) {
        By locator = this.getIdentifier(input);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Waiting for Element : " + input);
        try {
            WebDriverWait wait = new WebDriverWait(session.getDriver(), session.getDefaultWait());
            if (visible) {
                if (clickable) {
                    wait.until(ExpectedConditions.elementToBeClickable(locator));
                } else {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                }
            } else {
                wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            }
        } catch (TimeoutException exception) {
            throw new NoSuchElementException(input);
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Finding Element : " + input);
        return session.getDriver().findElement(locator);
    }

    @Override
    public String getValueFromHTMLVisible(Session session, String locator) {
        WebElement webElement = this.getSeleniumElement(session, locator, true, false);
        String result;

        if (webElement.getTagName().equalsIgnoreCase("select")) {
            Select select = (Select) webElement;
            result = select.getFirstSelectedOption().getText();
        } else if (webElement.getTagName().equalsIgnoreCase("option") || webElement.getTagName().equalsIgnoreCase("input")) {
            result = webElement.getAttribute("value");
        } else {
            result = webElement.getText();
        }
        return result;
    }

    @Override
    public String getValueFromHTML(Session session, String locator) {
        WebElement webElement = this.getSeleniumElement(session, locator, false, false);
        String result = null;

        if (webElement.getTagName().equalsIgnoreCase("select")) {
            if (webElement.getAttribute("disabled") == null || webElement.getAttribute("disabled").isEmpty()) {
                Select select = (Select) webElement;
                result = select.getFirstSelectedOption().getText();
            } else {
                result = webElement.getText();
                //result = "Unable to retrieve, element disabled ?";
            }
        } else if (webElement.getTagName().equalsIgnoreCase("option") || webElement.getTagName().equalsIgnoreCase("input")) {
            result = webElement.getAttribute("value");
        } else {
            result = webElement.getText();
        }
        /**
         * If return is empty, we search for hidden tags
         */
        if (StringUtil.isNullOrEmpty(result)) {
            String script = "return arguments[0].innerHTML";
            try {
                result = (String) ((JavascriptExecutor) session.getDriver()).executeScript(script, webElement);
            } catch (Exception e) {
                MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, "getValueFromHTML locator : '" + locator + "', exception : " + e.getMessage());
            }
        }

        return result;
    }

    @Override
    public String getAlertText(Session session) {
        Alert alert = session.getDriver().switchTo().alert();
        if (alert != null) {
            return alert.getText();
        }

        return null;
    }

    @Override
    public String getValueFromJS(Session session, String script) {
        JavascriptExecutor js = (JavascriptExecutor) session.getDriver();
        Object response = js.executeScript(script);

        if (response == null) {
            return "";
        }

        if (response instanceof String) {
            return (String) response;
        }

        return String.valueOf(response);
    }

    @Override
    public String getAttributeFromHtml(Session session, String locator, String attribute) {
        String result = null;
        try {
            WebElement webElement = this.getSeleniumElement(session, locator, true, false);
            result = webElement.getAttribute(attribute);
        } catch (WebDriverException exception) {
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
        }
        return result;
    }

    @Override
    public boolean isElementPresent(Session session, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(session, locator, false, false);
            return webElement != null;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public boolean isElementVisible(Session session, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(session, locator, true, false);
            return webElement != null && webElement.isDisplayed();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public boolean isElementNotVisible(Session session, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(session, locator, false, false);
            return webElement != null && !webElement.isDisplayed();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public String getPageSource(Session session) {
        return session.getDriver().getPageSource();
    }

    @Override
    public String getTitle(Session session) {
        return session.getDriver().getTitle();
    }

    /**
     * Return the current URL from Selenium.
     *
     * @return current URL without HTTP://IP:PORT/CONTEXTROOT/
     * @throws CerberusEventException Cannot find application host (from
     * Database) inside current URL (from Selenium)
     */
    @Override
    public String getCurrentUrl(Session session, String url) throws CerberusEventException {
        /*
         * Example: URL (http://mypage/page/index.jsp), IP (mypage)
         * URL.split(IP, 2)
         * Pos | Description
         *  0  |    http://
         *  1  |    /page/index.jsp
         */
        String strings[] = session.getDriver().getCurrentUrl().split(url, 2);
        if (strings.length < 2) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_NOT_MATCH_APPLICATION);
            msg.setDescription(msg.getDescription().replaceAll("%HOST%", session.getDriver().getCurrentUrl()));
            msg.setDescription(msg.getDescription().replaceAll("%URL%", url));
            MyLogger.log(WebDriverService.class.getName(), Level.WARN, msg.toString());
            throw new CerberusEventException(msg);
        }
        return strings[1];
    }

    
    @Override
    public BufferedImage takeScreenShot(Session session) {
        BufferedImage newImage = null;
        try {
            WebDriver augmentedDriver = new Augmenter().augment(session.getDriver());
            File image = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
            BufferedImage bufferedImage = ImageIO.read(image);

            newImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
        } catch (IOException exception) {
            MyLogger.log(WebDriverService.class.getName(), Level.WARN, exception.toString());
        } catch (WebDriverException exception) {
            //TODO check why occur
            //possible that the page still loading
            MyLogger.log(WebDriverService.class.getName(), Level.WARN, exception.toString());
        }
        return newImage;
    }

    @Override
    public boolean isElementInElement(Session session, String element, String childElement) {
        By elementLocator = this.getIdentifier(element);
        By childElementLocator = this.getIdentifier(childElement);

        return (session.getDriver().findElement(elementLocator) != null
                && session.getDriver().findElement(elementLocator).findElement(childElementLocator) != null);
    }

    @Override
    public boolean isElementNotClickable(Session session, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(session, locator, true, true);
            return webElement == null;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public boolean isElementClickable(Session session, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(session, locator, true, true);
            return webElement != null;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public MessageEvent doSeleniumActionClick(Session session, String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
//                    Actions actions = new Actions(selenium.getDriver());
//                    actions.click(this.getSeleniumElement(selenium, string1, true, true));
//                    actions.build().perform();
                    this.getSeleniumElement(session, string1, true, true).click();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    this.getSeleniumElement(session, string1, true, true).click();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_CLICK);
    }

    @Override
    public MessageEvent doSeleniumActionMouseDown(Session session, String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
                    Actions actions = new Actions(session.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(session, string1, true, true));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEDOWN_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    Actions actions = new Actions(session.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(session, string1, true, true));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEDOWN_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_CLICK);
    }

    @Override
    public MessageEvent doSeleniumActionMouseUp(Session session, String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
                    Actions actions = new Actions(session.getDriver());
                    actions.release(this.getSeleniumElement(session, string1, true, true));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEUP);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEUP_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    Actions actions = new Actions(session.getDriver());
                    actions.release(this.getSeleniumElement(session, string1, true, true));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEUP);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEUP_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_CLICK);
    }

    @Override
    public MessageEvent doSeleniumActionSwitchToWindow(Session session, String string1, String string2) {
        MessageEvent message;
        String windowTitle;
        try {
            if (!StringUtil.isNullOrEmpty(string1)) {
                windowTitle = string1;
            } else if (!StringUtil.isNull(string2)) {
                windowTitle = string2;
            } else {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SWITCHTOWINDOW_NO_SUCH_ELEMENT);
                message.setDescription(message.getDescription().replaceAll("%WINDOW%", "No Title Specified"));
                return message;
            }

            if (!StringUtil.isNullOrEmpty(windowTitle)) {
                String[] strings = windowTitle.split("=");
                String identifier, value;

                if (strings.length == 1) {
                    identifier = "title";
                    value = strings[0];
                } else {
                    identifier = strings[0];
                    value = strings[1];
                }

                String currentHandle;
                try {
                    // Current serial handle of the window.
                    currentHandle = session.getDriver().getWindowHandle();
                } catch (NoSuchWindowException exception) {
                    // Add try catch to handle not exist anymore window (like when popup is closed).
                    currentHandle = null;
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, "Window is closed ? " + exception.toString());
                }

                try {
                    // Get serials handles list of all browser windows
                    Set<String> handles = session.getDriver().getWindowHandles();

                    // Loop into each of them
                    for (String windowHandle : handles) {
                        if (!windowHandle.equals(currentHandle)) {
                            session.getDriver().switchTo().window(windowHandle);
                            if (seleniumTestTitleOfWindow(session, session.getDriver().getTitle(), identifier, value)) {
                                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SWITCHTOWINDOW);
                                message.setDescription(message.getDescription().replaceAll("%WINDOW%", windowTitle));
                                return message;
                            }
                        }
                        MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, "windowHandle=" + windowHandle);
                    }
                } catch (NoSuchElementException exception) {
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SWITCHTOWINDOW_NO_SUCH_ELEMENT);
        message.setDescription(message.getDescription().replaceAll("%WINDOW%", windowTitle));
        return message;
    }

    @Override
    public MessageEvent doSeleniumActionManageDialog(Session session, String object, String property) {
        try {
            String value = object;
            if (value == null || value.trim().length() == 0) {
                value = property;
            }
            if ("ok".equalsIgnoreCase(value)) {
                // Accept javascript popup dialog.
                session.getDriver().switchTo().alert().accept();
                session.getDriver().switchTo().defaultContent();
                return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLOSE_ALERT);
            } else if ("cancel".equalsIgnoreCase(value)) {
                // Dismiss javascript popup dialog.
                session.getDriver().switchTo().alert().dismiss();
                session.getDriver().switchTo().defaultContent();
                return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLOSE_ALERT);
            }

        } catch (NoSuchWindowException exception) {
            // Add try catch to handle not exist anymore alert popup (like when popup is closed).
            MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, "Alert popup is closed ? " + exception.toString());
        } catch (WebDriverException exception) {
            MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, "Alert popup is closed ? " + exception.toString());
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLOSE_ALERT);
    }

    private boolean seleniumTestTitleOfWindow(Session session, String title, String identifier, String value) {
        if (value != null && title != null) {
            if ("title".equals(identifier) && value.equals(title)) {
                return true;
            }

            if ("regexTitle".equals(identifier)) {
                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher(session.getDriver().getTitle());

                return matcher.find();
            }
        }
        return false;
    }

    @Override
    public MessageEvent doSeleniumActionClickWait(Session session, String actionObject, String actionProperty) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                try {
                    this.getSeleniumElement(session, actionObject, true, true).click();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
                if (StringUtil.isNumeric(actionProperty)) {
                    int sleep = Integer.parseInt(actionProperty);
                    try {
                        Thread.sleep(sleep);
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICKANDWAIT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                        message.setDescription(message.getDescription().replaceAll("%TIME%", Integer.toString(sleep)));
                        return message;
                    } catch (InterruptedException e) {
                        MyLogger.log(WebDriverService.class.getName(), Level.INFO, e.toString());
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICKANDWAIT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                        message.setDescription(message.getDescription().replaceAll("%TIME%", Integer.toString(sleep)));
                        return message;
                    }
                }
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICKANDWAIT_NO_NUMERIC);
                message.setDescription(message.getDescription().replaceAll("%TIME%", actionProperty));
                return message;
            } else if (StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                try {
                    this.getSeleniumElement(session, actionObject, true, true).click();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICKANDNOWAIT);
                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                return message;
            } else if (!StringUtil.isNull(actionProperty) && StringUtil.isNull(actionObject)) {
                try {
                    this.getSeleniumElement(session, actionProperty, true, true).click();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionProperty));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICKANDNOWAIT);
                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionProperty));
                return message;
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICKANDWAIT_GENERIC);
    }

    @Override
    public MessageEvent doSeleniumActionDoubleClick(Session session, String html, String property) {
        MessageEvent message;
        try {
            Actions actions = new Actions(session.getDriver());
            if (!StringUtil.isNull(property)) {
                try {
                    actions.doubleClick(this.getSeleniumElement(session, property, true, true));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DOUBLECLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(html)) {
                try {
                    actions.doubleClick(this.getSeleniumElement(session, html, true, true));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DOUBLECLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK);
    }

    @Override
    public MessageEvent doSeleniumActionType(Session session, String html, String property, String propertyName) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(html)) {
                try {
                    WebElement webElement = this.getSeleniumElement(session, html, true, true);
                    webElement.clear();
                    if (!StringUtil.isNull(property)) {
                        webElement.sendKeys(property);
                    }
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TYPE);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    if (!StringUtil.isNull(property)) {
                        message.setDescription(message.getDescription().replaceAll("%DATA%", ParameterParserUtil.securePassword(property, propertyName)));
                    } else {
                        message.setDescription(message.getDescription().replaceAll("%DATA%", "No property"));
                    }
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
    }

    @Override
    public MessageEvent doSeleniumActionMouseOver(Session session, String html, String property) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(html)) {
                try {
                    Actions actions = new Actions(session.getDriver());
                    WebElement menuHoverLink = this.getSeleniumElement(session, html, true, true);
                    actions.moveToElement(menuHoverLink);
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVER);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(property)) {
                try {
                    Actions actions = new Actions(session.getDriver());
                    WebElement menuHoverLink = this.getSeleniumElement(session, property, true, true);
                    actions.moveToElement(menuHoverLink);
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVER);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER);
    }

    @Override
    public MessageEvent doSeleniumActionMouseOverAndWait(Session session, String actionObject, String actionProperty) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                if (StringUtil.isNumeric(actionProperty)) {
                    try {
                        Actions actions = new Actions(session.getDriver());
                        WebElement menuHoverLink = this.getSeleniumElement(session, actionObject, true, true);
                        actions.moveToElement(menuHoverLink);
                        actions.build().perform();
                        int sleep = Integer.parseInt(actionProperty);
                        try {
                            Thread.sleep(sleep);
                            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVERANDWAIT);
                            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                            message.setDescription(message.getDescription().replaceAll("%TIME%", Integer.toString(sleep)));
                            return message;
                        } catch (InterruptedException e) {
                            MyLogger.log(WebDriverService.class.getName(), Level.INFO, e.toString());
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVERANDWAIT);
                            message.setDescription(message.getDescription().replaceAll("%ELEMENT1%", actionObject));
                            message.setDescription(message.getDescription().replaceAll("%TIME%", Integer.toString(sleep)));
                            return message;
                        }
                    } catch (NoSuchElementException exception) {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                        MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                        return message;
                    }
                }
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVERANDWAIT_NO_NUMERIC);
                message.setDescription(message.getDescription().replaceAll("%TIME%", actionProperty));
                return message;
            } else if (StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                try {
                    Actions actions = new Actions(session.getDriver());
                    WebElement menuHoverLink = this.getSeleniumElement(session, actionObject, true, true);
                    actions.moveToElement(menuHoverLink);
                    actions.build().perform();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICKANDNOWAIT);
                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                return message;
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVERANDWAIT_GENERIC);
    }

    @Override
    public MessageEvent doSeleniumActionWait(Session session, String object, String property) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(property)) {
                if (StringUtil.isNumeric(property)) {
                    try {
                        Thread.sleep(Integer.parseInt(property));
                    } catch (InterruptedException exception) {
                        MyLogger.log(WebDriverService.class.getName(), Level.INFO, exception.toString());
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT);
                        message.setDescription(message.getDescription().replaceAll("%TIME%", property));
                        return message;
                    }
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME);
                    message.setDescription(message.getDescription().replaceAll("%TIME%", property));
                    return message;
                } else {
                    try {
                        WebDriverWait wait = new WebDriverWait(session.getDriver(), TIMEOUT_WEBELEMENT);
                        wait.until(ExpectedConditions.presenceOfElementLocated(this.getIdentifier(property)));
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                        return message;
                    } catch (NoSuchElementException exception) {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                        MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                        return message;
                    }
                }
            } else if (!StringUtil.isNull(object)) {
                if (StringUtil.isNumeric(object)) {
                    try {
                        Thread.sleep(Integer.parseInt(object));
                    } catch (InterruptedException exception) {
                        MyLogger.log(WebDriverService.class.getName(), Level.INFO, exception.toString());
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT);
                        message.setDescription(message.getDescription().replaceAll("%TIME%", object));
                        return message;
                    }
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME);
                    message.setDescription(message.getDescription().replaceAll("%TIME%", object));
                    return message;
                } else {
                    try {
                        WebDriverWait wait = new WebDriverWait(session.getDriver(), TIMEOUT_WEBELEMENT);
                        wait.until(ExpectedConditions.presenceOfElementLocated(this.getIdentifier(object)));
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", object));
                        return message;
                    } catch (NoSuchElementException exception) {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", object));
                        MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                        return message;
                    }
                }
            } else {
                try {
                    Thread.sleep(TIMEOUT_MILLIS);
                } catch (InterruptedException exception) {
                    MyLogger.log(WebDriverService.class.getName(), Level.INFO, exception.toString());
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT);
                    message.setDescription(message.getDescription().replaceAll("%TIME%", Integer.toString(TIMEOUT_MILLIS)));
                    return message;
                }
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME);
                message.setDescription(message.getDescription().replaceAll("%TIME%", Integer.toString(TIMEOUT_MILLIS)));
                return message;
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
    }

    @Override
    public MessageEvent doSeleniumActionKeyPress(Session session, String html, String property) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(html) && !StringUtil.isNull(property)) {
                try {
                    WebElement element = this.getSeleniumElement(session, html, true, true);
                    element.sendKeys(Keys.valueOf(property));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    message.setDescription(message.getDescription().replaceAll("%DATA%", property));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS);
    }

    @Override
    public MessageEvent doSeleniumActionOpenURL(Session session, String host, String value, String property, boolean withBase) {
        MessageEvent message;
        String url = "null";
        try {
            if (!StringUtil.isNull(value)) {
                url = value;
            } else if (!StringUtil.isNull(property)) {
                url = property;
            }
            if (!StringUtil.isNull(url)) {
                if (withBase) {
                    url = "http://" + host + url;
                }
                session.getDriver().get(url);

                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_OPENURL);
                message.setDescription(message.getDescription().replaceAll("%URL%", url));
                return message;
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_OPENURL);
        message.setDescription(message.getDescription().replaceAll("%URL%", url));
        return message;
    }

    @Override
    public MessageEvent doSeleniumActionSelect(Session session, String html, String property) {
        MessageEvent message;
        String identifier;
        String value = "";

        try {
            if (!StringUtil.isNull(html) && !StringUtil.isNull(property)) {

                String[] strings = property.split("=");
                if (strings.length == 1) {
                    identifier = "value";
                    value = strings[0];
                } else {
                    identifier = strings[0];
                    value = strings[1];
                }

                Select select;
                try {
                    select = new Select(this.getSeleniumElement(session, html, true, true));
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
                if (identifier.equalsIgnoreCase("value")) {
                    select.selectByValue(value);
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SELECT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    message.setDescription(message.getDescription().replaceAll("%DATA%", property));
                    return message;
                } else if (identifier.equalsIgnoreCase("label")) {
                    select.selectByVisibleText(value);
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SELECT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    message.setDescription(message.getDescription().replaceAll("%DATA%", property));
                    return message;
                } else if (identifier.equalsIgnoreCase("index") && StringUtil.isNumeric(value)) {
                    select.selectByIndex(Integer.parseInt(value));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SELECT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    message.setDescription(message.getDescription().replaceAll("%DATA%", property));
                    return message;
                } else if (identifier.equalsIgnoreCase("regexValue") || identifier.equalsIgnoreCase("regexIndex")
                        || identifier.equalsIgnoreCase("regexLabel")) {
                    java.util.List<WebElement> list = select.getOptions();

                    if (identifier.equalsIgnoreCase("regexValue")) {
                        for (WebElement option : list) {
                            String optionValue = option.getAttribute("value");
                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher(optionValue);

                            if (matcher.find()) {
                                select.selectByValue(optionValue);
                                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SELECT);
                                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                                message.setDescription(message.getDescription().replaceAll("%DATA%", property));
                                return message;
                            }
                        }
                    } else if (identifier.equalsIgnoreCase("regexLabel")) {
                        for (WebElement option : list) {
                            String optionLabel = option.getText();
                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher(optionLabel);

                            if (matcher.find()) {
                                select.selectByVisibleText(optionLabel);
                                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SELECT);
                                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                                message.setDescription(message.getDescription().replaceAll("%DATA%", property));
                                return message;
                            }
                        }
                    } else if (identifier.equalsIgnoreCase("regexIndex") && StringUtil.isNumeric(value)) {
                        for (WebElement option : list) {
                            Integer id = 0;
                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher(id.toString());

                            if (matcher.find()) {
                                select.selectByIndex(Integer.parseInt(value));
                                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SELECT);
                                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                                message.setDescription(message.getDescription().replaceAll("%DATA%", property));
                                return message;
                            }
                            id++;
                        }
                    }
                } else {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_NO_IDENTIFIER);
                    message.setDescription(message.getDescription().replaceAll("%IDENTIFIER%", html));
                    return message;
                }
            }
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_NO_SUCH_VALUE);
            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
            message.setDescription(message.getDescription().replaceAll("%DATA%", property));
            return message;
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        } catch (PatternSyntaxException e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_REGEX_INVALIDPATERN);
            message.setDescription(message.getDescription().replaceAll("%PATERN%", value));
            message.setDescription(message.getDescription().replaceAll("%ERROR%", e.getMessage()));
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT);
    }

    @Override
    public MessageEvent doSeleniumActionUrlLogin(Session session, String host, String uri) {
        MessageEvent message;
        String url = "http://" + host + uri;
        try {
            session.getDriver().get(url);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_URLLOGIN);
            message.setDescription(message.getDescription().replaceAll("%URL%", url));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_URLLOGIN);
            message.setDescription(message.getDescription().replaceAll("%URL%", url) + " " + e.getMessage());
            return message;
        }
    }

    @Override
    public MessageEvent doSeleniumActionFocusToIframe(Session session, String object, String property) {
        MessageEvent message;

        try {
            if (!StringUtil.isNullOrEmpty(property)) {
                try {
                    session.getDriver().switchTo().frame(this.getSeleniumElement(session, property, false, false));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSTOIFRAME);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", property));
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_FOCUS_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", property));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                }
            } else {
                try {
                    session.getDriver().switchTo().frame(this.getSeleniumElement(session, object, false, false));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSTOIFRAME);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", object));
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_FOCUS_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", object));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }

        return message;
    }

    @Override
    public MessageEvent doSeleniumActionFocusDefaultIframe(Session session) {
        MessageEvent message;

        try {
            session.getDriver().switchTo().defaultContent();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSDEFAULTIFRAME);
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }

        return message;
    }

    @Override
    public MessageEvent doSeleniumActionMouseDownMouseUp(Session session, String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
                    Actions actions = new Actions(session.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(session, string1, true, false));
                    actions.build().perform();
                    actions.release(this.getSeleniumElement(session, string1, true, false));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    Actions actions = new Actions(session.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(session, string1, true, false));
                    actions.build().perform();
                    actions.release(this.getSeleniumElement(session, string1, true, false));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_CLICK);
    }

    @Override
    public String getFromCookie(Session session, String cookieName, String cookieParameter) {
        Cookie cookie = session.getDriver().manage().getCookieNamed(cookieName);
        if (cookie != null) {
            if (cookieParameter.equals("name")) {
                return cookie.getName();
            }
            if (cookieParameter.equals("expiry")) {
                return cookie.getExpiry().toString();
            }
            if (cookieParameter.equals("value")) {
                return cookie.getValue();
            }
            if (cookieParameter.equals("domain")) {
                return cookie.getDomain();
            }
            if (cookieParameter.equals("path")) {
                return cookie.getPath();
            }
            if (cookieParameter.equals("isHttpOnly")) {
                return String.valueOf(cookie.isHttpOnly());
            }
            if (cookieParameter.equals("isSecure")) {
                return String.valueOf(cookie.isSecure());
            }
        } else {
            return "cookieNotFound";
        }
        return null;
    }

    @Override
    public List<String> getSeleniumLog(Session session) {
        List<String> result = new ArrayList();
        Logs logs = session.getDriver().manage().logs();

        for (String logType : logs.getAvailableLogTypes()) {
            LogEntries logEntries = logs.get(logType);
            result.add("********************" + logType + "********************\n");
            for (LogEntry logEntry : logEntries) {
                result.add(new Date(logEntry.getTimestamp()) + " : " + logEntry.getLevel() + " : " + logEntry.getMessage() + "\n");
            }
        }

        return result;
    }

}

