/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.service.webdriver.impl;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;
import static com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.engine.entity.Identifier;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.Session;
import org.cerberus.enums.KeyCodeEnum;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.service.webdriver.IWebDriverService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class WebDriverService implements IWebDriverService {

    private static final int TIMEOUT_MILLIS = 30000;
    private static final int TIMEOUT_WEBELEMENT = 300;
    private static final int TIMEOUT_FOCUS = 1000;

    private static final Logger LOG = LogManager.getLogger("WebDriverService");

    private By getBy(Identifier identifier) {

        LOG.debug("Finding selenium Element : " + identifier.getLocator() + " by : " + identifier.getIdentifier());

        if (identifier.getIdentifier().equalsIgnoreCase("id")) {
            return By.id(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("name")) {
            return By.name(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("class")) {
            return By.className(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("css")) {
            return By.cssSelector(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("xpath")) {
            return By.xpath(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("link")) {
            return By.linkText(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("data-cerberus")) {
            return By.xpath("//*[@data-cerberus='" + identifier.getLocator() + "']");

        } else {
            throw new NoSuchElementException(identifier.getIdentifier());
        }
    }

    private AnswerItem<WebElement> getSeleniumElement(Session session, Identifier identifier, boolean visible, boolean clickable) {
        AnswerItem<WebElement> answer = new AnswerItem<WebElement>();
        MessageEvent msg;
        By locator = this.getBy(identifier);
        LOG.debug("Waiting for Element : " + identifier.getIdentifier() + "=" + identifier.getLocator());
        try {
            WebDriverWait wait = new WebDriverWait(session.getDriver(), TimeUnit.MILLISECONDS.toSeconds(session.getCerberus_selenium_wait_element()));
            WebElement element;
            if (visible) {
                if (clickable) {
                    element = wait.until(ExpectedConditions.elementToBeClickable(locator));
                } else {
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                }
            } else {
                element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            }
            answer.setItem(element);
            msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
            msg.setDescription(msg.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
        } catch (TimeoutException exception) {
            LOG.warn("Exception waiting for element :" + exception);
            //throw new NoSuchElementException(identifier.getIdentifier() + "=" + identifier.getLocator());
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
            msg.setDescription(msg.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public String getValueFromHTMLVisible(Session session, Identifier identifier) {
        String result = null;
        AnswerItem answer = this.getSeleniumElement(session, identifier, true, false);
        if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
            WebElement webElement = (WebElement) answer.getItem();
            if (webElement != null) {
                if (webElement.getTagName().equalsIgnoreCase("select")) {
                    Select select = (Select) webElement;
                    result = select.getFirstSelectedOption().getText();
                } else if (webElement.getTagName().equalsIgnoreCase("option") || webElement.getTagName().equalsIgnoreCase("input")) {
                    result = webElement.getAttribute("value");
                } else {
                    result = webElement.getText();
                }
            }
        }

        return result;
    }

    @Override
    public String getValueFromHTML(Session session, Identifier identifier) {
        AnswerItem answer = this.getSeleniumElement(session, identifier, false, false);
        String result = null;
        if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
            WebElement webElement = (WebElement) answer.getItem();
            if (webElement != null) {
                if (webElement.getTagName().equalsIgnoreCase("select")) {
                    if (webElement.getAttribute("disabled") == null || webElement.getAttribute("disabled").isEmpty()) {
                        Select select = new Select(webElement);
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
                        LOG.debug("getValueFromHTML locator : '" + identifier.getIdentifier() + "=" + identifier.getLocator() + "', exception : " + e.getMessage());
                    }
                }
            }
        } else if (answer.isCodeEquals(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT.getCode())) {
            throw new NoSuchElementException(identifier.getIdentifier() + "=" + identifier.getLocator());
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
    public String getAttributeFromHtml(Session session, Identifier identifier, String attribute) {
        String result = null;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, true, false);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();
                if (webElement != null) {
                    result = webElement.getAttribute(attribute);
                }
            }
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
        }
        return result;
    }

    @Override
    public boolean isElementPresent(Session session, Identifier identifier) {
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, false, false);

            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();

                return webElement != null;
            }
        } catch (NoSuchElementException exception) {
            LOG.warn(exception.toString());
        }
        return false;
    }

    @Override
    public boolean isElementNotPresent(Session session, Identifier identifier) {
        By locator = this.getBy(identifier);
        LOG.debug("Waiting for Element to be not present : " + identifier.getIdentifier() + "=" + identifier.getLocator());
        try {
            WebDriverWait wait = new WebDriverWait(session.getDriver(), TimeUnit.MILLISECONDS.toSeconds(session.getCerberus_selenium_wait_element()));
            return wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfElementLocated(locator)));
        } catch (TimeoutException exception) {
            LOG.warn("Exception waiting for element to be not present :" + exception);
            return false;
        }
    }

    @Override
    public boolean isElementVisible(Session session, Identifier identifier) {
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, true, false);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();
                return webElement != null && webElement.isDisplayed();
            }

        } catch (NoSuchElementException exception) {
            LOG.warn(exception.toString());
        }
        return false;
    }

    @Override
    public boolean isElementNotVisible(Session session, Identifier identifier) {
        By locator = this.getBy(identifier);
        LOG.debug("Waiting for Element to be not visible : " + identifier.getIdentifier() + "=" + identifier.getLocator());
        try {
            WebDriverWait wait = new WebDriverWait(session.getDriver(), TimeUnit.MILLISECONDS.toSeconds(session.getCerberus_selenium_wait_element()));
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException exception) {
            LOG.warn("Exception waiting for element to be not visible :" + exception);
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
     * @param session
     * @param applicationUrl
     * @return current URL without HTTP://IP:PORT/CONTEXTROOT/
     * @throws CerberusEventException Cannot find application host (from
     * Database) inside current URL (from Selenium)
     */
    @Override
    public String getCurrentUrl(Session session, String applicationUrl) throws CerberusEventException {
        /*
         * Example: URL (http://cerberus.domain.fr/Cerberus/mypage/page/index.jsp)<br>
         * will return /mypage/page/index.jsp
         * No matter what, the output current relative URl will start by /
         */
        // We start to remove the protocol part of the urls.
        String cleanedCurrentURL = StringUtil.removeProtocolFromHostURL(session.getDriver().getCurrentUrl());
        String cleanedURL = StringUtil.removeProtocolFromHostURL(applicationUrl);
        // We remove from current url the host part of the application.
        String strings[] = cleanedCurrentURL.split(cleanedURL, 2);
        if (strings.length < 2) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_NOT_MATCH_APPLICATION);
            msg.setDescription(msg.getDescription().replace("%HOST%", applicationUrl));
            msg.setDescription(msg.getDescription().replace("%CURRENTURL%", session.getDriver().getCurrentUrl()));
            LOG.warn(msg.toString());
            throw new CerberusEventException(msg);
        }
        String result = StringUtil.addPrefixIfNotAlready(strings[1], "/");
        return result;
    }

    public File takeScreenShotFile(Session session) {
        boolean event = true;
        long timeout = System.currentTimeMillis() + (session.getCerberus_selenium_wait_element());
        //Try to capture picture. Try again until timeout is WebDriverException is raised.
        while (event) {
            try {
                WebDriver augmentedDriver = new Augmenter().augment(session.getDriver());
                File image = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);

                if (image != null) {
                    //logs for debug purposes
                    LOG.info("WebDriverService: screen-shot taken with succes: " + image.getName() + "(size" + image.length() + ")");
                } else {
                    LOG.warn("WebDriverService: screen-shot returned null: ");
                }
                return image;
            } catch (WebDriverException exception) {
                if (System.currentTimeMillis() >= timeout) {
                    LOG.warn(exception.toString());
                }
                event = false;
            }
        }

        return null;
    }

    @Override
    public BufferedImage takeScreenShot(Session session) {
        BufferedImage newImage = null;
        boolean event = true;
        long timeout = System.currentTimeMillis() + (session.getCerberus_selenium_wait_element());
        //Try to capture picture. Try again until timeout is WebDriverException is raised.
        while (event) {
            try {
                WebDriver augmentedDriver = new Augmenter().augment(session.getDriver());
                File image = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
                BufferedImage bufferedImage = ImageIO.read(image);

                newImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                newImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
                return newImage;
            } catch (IOException exception) {
                LOG.warn(exception.toString());
                event = false;
            } catch (WebDriverException exception) {
                if (System.currentTimeMillis() >= timeout) {
                    LOG.warn(exception.toString());
                    event = false;
                }
            }
        }
        return newImage;
    }

    @Override
    public boolean isElementInElement(Session session, Identifier identifier, Identifier childIdentifier) {
        By elementLocator = this.getBy(identifier);
        By childElementLocator = this.getBy(childIdentifier);

        return (session.getDriver().findElement(elementLocator) != null
                && session.getDriver().findElement(elementLocator).findElement(childElementLocator) != null);
    }

    @Override
    public boolean isElementNotClickable(Session session, Identifier identifier) {
        By locator = this.getBy(identifier);
        LOG.debug("Waiting for Element to be not clickable : " + identifier.getIdentifier() + "=" + identifier.getLocator());
        try {
            WebDriverWait wait = new WebDriverWait(session.getDriver(), TimeUnit.MILLISECONDS.toSeconds(session.getCerberus_selenium_wait_element()));
            return wait.until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(locator)));
        } catch (TimeoutException exception) {
            LOG.warn("Exception waiting for element to be not clickable :" + exception);
            return false;
        }
    }

    @Override
    public boolean isElementClickable(Session session, Identifier identifier) {
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, true, true);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();

                return webElement != null;
            }
        } catch (NoSuchElementException exception) {
            LOG.warn(exception.toString());
        }
        return false;
    }

    @Override
    public MessageEvent doSeleniumActionClick(Session session, final Identifier identifier, boolean waitForVisibility, boolean waitForClickability) {
        MessageEvent message;
        try {

            AnswerItem answer = this.getSeleniumElement(session, identifier, waitForVisibility, waitForClickability);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                final WebElement webElement = (WebElement) answer.getItem();

                if (webElement != null) {
                    // We noticed that sometimes, webelement.click never finished whatever the timeout set.
                    // Below is an implementation to secure timeout on thread before calling selenium.
                    // This is a test that can be extended or clean depending on the result.
                    ExecutorService executor = Executors.newCachedThreadPool();
                    Callable<MessageEvent> task = new Callable<MessageEvent>() {
                        public MessageEvent call() {
                            MessageEvent message;
                            Actions actions = new Actions(session.getDriver());
                            actions.click(webElement);
                            actions.build().perform();
                            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
                            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                            return message;
                        }
                    };
                    Future<MessageEvent> future = executor.submit(task);
                    try {
                        MessageEvent result = future.get(session.getCerberus_selenium_action_click_timeout(), TimeUnit.MILLISECONDS);
                        return result;
                    } catch (java.util.concurrent.TimeoutException ex) {
                        // handle the timeout
                        LOG.warn("Exception clicking on element :" + ex, ex);
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
                        message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
                        return message;
                    } catch (InterruptedException e) {
                        // handle the interrupts
                        LOG.warn("Exception clicking on element :" + e, e);
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK);
                        message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()).replace("%MESS%", e.toString()));
                        return message;
                    } catch (ExecutionException e) {
                        // handle other exceptions
                        LOG.warn("Exception clicking on element :" + e, e);
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()).replace("%MESS%", e.toString()));
                        return message;
                    } finally {
                        future.cancel(true);
                    }
                }
            }

            return answer.getResultMessage();
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
//        } catch (TimeoutException exception) {
//            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
//            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
//            MyLogger.log(WebDriverService.class.getName(), Level.WARN, exception.toString());
//            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }

    }

    @Override
    public MessageEvent doSeleniumActionMouseDown(Session session, Identifier identifier) {
        MessageEvent message;
        try {

            AnswerItem answer = this.getSeleniumElement(session, identifier, true, true);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();
                if (webElement != null) {
                    Actions actions = new Actions(session.getDriver());
                    actions.clickAndHold(webElement);
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                    return message;
                }
            }

            return answer.getResultMessage();
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEDOWN_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }
    }

    @Override
    public MessageEvent doSeleniumActionMouseUp(Session session, Identifier identifier) {
        MessageEvent message;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, true, true);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();
                if (webElement != null) {
                    Actions actions = new Actions(session.getDriver());
                    actions.release(webElement);
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEUP);
                    message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                    return message;
                }
            }

            return answer.getResultMessage();
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEUP_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }
    }

    @Override
    public MessageEvent doSeleniumActionSwitchToWindow(Session session, Identifier identifier) {
        MessageEvent message;
        String windowTitle = identifier.getLocator();

        String currentHandle;
        // Current serial handle of the window.
        // Add try catch to handle not exist anymore window (like when popup is closed).
        try {
            currentHandle = session.getDriver().getWindowHandle();
        } catch (NoSuchWindowException exception) {
            currentHandle = null;
            LOG.debug("Window is closed ? " + exception.toString());
        }

        try {
            // Get serials handles list of all browser windows
            Set<String> handles = session.getDriver().getWindowHandles();

            // Loop into each of them
            for (String windowHandle : handles) {
                if (!windowHandle.equals(currentHandle)) {
                    session.getDriver().switchTo().window(windowHandle);
                    if (checkIfExpectedWindow(session, identifier.getIdentifier(), identifier.getLocator())) {
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SWITCHTOWINDOW);
                        message.setDescription(message.getDescription().replace("%WINDOW%", windowTitle));
                        return message;
                    }
                }
                LOG.debug("windowHandle=" + windowHandle);
            }
        } catch (NoSuchElementException exception) {
            LOG.debug(exception.toString());
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SWITCHTOWINDOW_NO_SUCH_ELEMENT);
        message.setDescription(message.getDescription().replace("%WINDOW%", windowTitle));
        return message;
    }

    @Override
    public MessageEvent doSeleniumActionManageDialog(Session session, Identifier identifier) {
        MessageEvent message;
        try {
            if ("ok".equalsIgnoreCase(identifier.getLocator())) {
                // Accept javascript popup dialog.
                session.getDriver().switchTo().alert().accept();
                session.getDriver().switchTo().defaultContent();
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLOSE_ALERT);
                return message;
            } else if ("cancel".equalsIgnoreCase(identifier.getLocator())) {
                // Dismiss javascript popup dialog.
                session.getDriver().switchTo().alert().dismiss();
                session.getDriver().switchTo().defaultContent();
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLOSE_ALERT);
                return message;
            }
        } catch (NoSuchWindowException exception) {
            // Add try catch to handle not exist anymore alert popup (like when popup is closed).
            LOG.debug("Alert popup is closed ? " + exception.toString());
        } catch (TimeoutException exception) {
            LOG.warn(exception.toString());
        } catch (WebDriverException exception) {
            LOG.debug("Alert popup is closed ? " + exception.toString());
            return parseWebDriverException(exception);
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLOSE_ALERT);
    }

    private boolean checkIfExpectedWindow(Session session, String identifier, String value) {

        boolean result = false;
        WebDriverWait wait = new WebDriverWait(session.getDriver(), TIMEOUT_WEBELEMENT);
        String title;

        switch (identifier) {
            case Identifier.IDENTIFIER_URL: {

                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe("about:blank")));
                return session.getDriver().getCurrentUrl().equals(value);
            }
            case Identifier.IDENTIFIER_REGEXTITLE:
                wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("")));
                title = session.getDriver().getTitle();
                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher(title);
                result = matcher.find();
            default:
                wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("")));
                title = session.getDriver().getTitle();
                if (title.equals(value)) {
                    result = true;
                }
        }
        return result;
    }

    @Override
    public MessageEvent doSeleniumActionDoubleClick(Session session, Identifier identifier, boolean waitForVisibility, boolean waitForClickability) {
        MessageEvent message;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, waitForVisibility, waitForClickability);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();
                if (webElement != null) {
                    Actions actions = new Actions(session.getDriver());
                    actions.doubleClick(webElement);
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DOUBLECLICK);
                    message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                    return message;
                }
            }
            message = answer.getResultMessage();
            return message;
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }
    }

    @Override
    public MessageEvent doSeleniumActionType(Session session, Identifier identifier, String property, String propertyName) {
        MessageEvent message;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, true, true);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();
                if (webElement != null) {
                    webElement.clear();
                    if (!StringUtil.isNull(property)) {
                        webElement.sendKeys(property);
                    }
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TYPE);
                    message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                    if (!StringUtil.isNull(property)) {
                        message.setDescription(message.getDescription().replace("%DATA%", ParameterParserUtil.securePassword(property, propertyName)));
                    } else {
                        message.setDescription(message.getDescription().replace("%DATA%", "No property"));
                    }
                    return message;
                }
            }
            return answer.getResultMessage();
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }
    }

    @Override
    public MessageEvent doSeleniumActionMouseOver(Session session, Identifier identifier) {
        MessageEvent message;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, true, true);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement menuHoverLink = (WebElement) answer.getItem();
                if (menuHoverLink != null) {
                    Actions actions = new Actions(session.getDriver());
                    actions.moveToElement(menuHoverLink);
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVER);
                    message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                    return message;
                }
            }

            return answer.getResultMessage();
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }
    }

    @Override
    public MessageEvent doSeleniumActionWait(Session session, Identifier identifier) {
        MessageEvent message;
        try {
            WebDriverWait wait = new WebDriverWait(session.getDriver(), TIMEOUT_WEBELEMENT);
            wait.until(ExpectedConditions.presenceOfElementLocated(this.getBy(identifier)));
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        }
    }

    @Override
    public MessageEvent doSeleniumActionWaitVanish(Session session, Identifier identifier) {
        MessageEvent message;
        try {
            WebDriverWait wait = new WebDriverWait(session.getDriver(), TIMEOUT_WEBELEMENT);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(this.getBy(identifier)));
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAITVANISH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        }
    }

    /**
     * Disable the headless status of the running application preventing Robot
     * to work through a web container
     */
    public void disableHeadlessApplicationControl() {
        System.setProperty("java.awt.headless", "false");
        try {
            Field headlessField = GraphicsEnvironment.class.getDeclaredField("headless");
            headlessField.setAccessible(true);
            headlessField.set(null, Boolean.FALSE);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            LOG.warn(ex.toString());
        }
    }

    /**
     * Interface to Windows instrumentation in order to have control over all
     * the others applications running in the OS
     */
    public interface User32 extends W32APIOptions {

        User32 instance = (User32) Native.loadLibrary("user32", User32.class, DEFAULT_OPTIONS);

        boolean ShowWindow(HWND hWnd, int nCmdShow);

        boolean SetForegroundWindow(HWND hWnd);

        HWND FindWindow(String winClass, String title);
        int SW_SHOW = 1;
    }

    /**
     * Tries to focus the browser window thanks to the title given by the
     * webdriver in order to put it in foreground for Robot to work (Only works
     * on Windows so far, another way is to find for Xorg)
     *
     * @param session Webdriver session instance
     * @return True if the window is found, False otherwise
     */
    public boolean focusBrowserWindow(Session session) {
        WebDriver driver = session.getDriver();
        String title = driver.getTitle();
        User32 user32 = User32.instance;

        // Arbitrary
        String[] browsers = new String[]{
            "",
            "Google Chrome",
            "Mozilla Firefox",
            "Opera",
            "Safari",
            "Internet Explorer",
            "Microsoft Edge",};

        for (String browser : browsers) {
            HWND window;
            if (browser.isEmpty()) {
                window = user32.FindWindow(null, title);
            } else {
                window = user32.FindWindow(null, title + " - " + browser);
            }
            if (user32.ShowWindow(window, User32.SW_SHOW)) {
                return user32.SetForegroundWindow(window);
            }
        }
        return false;
    }

    @Override
    public MessageEvent doSeleniumActionKeyPress(Session session, Identifier identifier, String property) {

        MessageEvent message;
        try {
            if (!StringUtil.isNullOrEmpty(identifier.getLocator())) {
                AnswerItem answer = this.getSeleniumElement(session, identifier, true, true);
                if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                    WebElement webElement = (WebElement) answer.getItem();
                    if (webElement != null) {
                        webElement.sendKeys(Keys.valueOf(property));
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS);
                        message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                        message.setDescription(message.getDescription().replace("%DATA%", property));
                        return message;
                    }

                }
                return answer.getResultMessage();

            } else {
                try {
                    //disable headless application warning for Robot
                    this.disableHeadlessApplicationControl();

                    //create wait action
                    WebDriverWait wait = new WebDriverWait(session.getDriver(), 01);

                    //focus the browser window for Robot to work
                    if (this.focusBrowserWindow(session)) {
                        //wait until the browser is focused
                        wait.withTimeout(TIMEOUT_FOCUS, TimeUnit.MILLISECONDS);
                    }

                    //gets the robot 
                    Robot r = new Robot();
                    //converts the Key description sent through Cerberus into the AWT key code
                    int keyCode = KeyCodeEnum.getAWTKeyCode(property);

                    if (keyCode != KeyCodeEnum.NOT_VALID.getKeyCode()) {
                        //if the code is valid then presses the key and releases the key
                        r.keyPress(keyCode);
                        r.keyRelease(keyCode);
                        //wait until the action is performed
                        wait.withTimeout(TIMEOUT_WEBELEMENT, TimeUnit.MILLISECONDS);

                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT);
                    } else {
                        //the key enterer is not valid
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_NOT_AVAILABLE);
                        LOG.debug("Key " + property + "is not available in the current environment");
                    }

                    message.setDescription(message.getDescription().replace("%KEY%", property));

                } catch (AWTException ex) {
                    LOG.warn(ex);
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_ENV_ERROR);
                }
            }

        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());

        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());

        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);

        }
        return message;
    }

    @Override
    public MessageEvent doSeleniumActionOpenURL(Session session, String host, Identifier identifier, boolean withBase) {
        MessageEvent message;
        String url = "";
        try {
            if (!StringUtil.isNull(identifier.getLocator())) {
                if (withBase) {
                    host = StringUtil.cleanHostURL(host);
                    url = StringUtil.getURLFromString(host, "", identifier.getLocator(), "");
                } else {
                    url = StringUtil.cleanHostURL(identifier.getLocator());
                }
                session.getDriver().get(url);
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_OPENURL);
                message.setDescription(message.getDescription().replace("%URL%", url));

            } else {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_OPENURL);
                message.setDescription(message.getDescription().replace("%URL%", url));
            }
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_OPENURL_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_pageLoadTimeout())));
            message.setDescription(message.getDescription().replace("%URL%", url));
            LOG.warn(exception.toString());
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }

        return message;
    }

    @Override
    public MessageEvent doSeleniumActionSelect(Session session, Identifier object, Identifier property) {
        MessageEvent message;
        try {
            Select select;
            try {
                AnswerItem answer = this.getSeleniumElement(session, object, true, true);
                if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                    WebElement webElement = (WebElement) answer.getItem();
                    if (webElement != null) {
                        select = new Select(webElement);
                        this.selectRequestedOption(select, property, object.getIdentifier() + "=" + object.getLocator());
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SELECT);
                        message.setDescription(message.getDescription().replace("%ELEMENT%", object.getIdentifier() + "=" + object.getLocator()));
                        message.setDescription(message.getDescription().replace("%DATA%", property.getIdentifier() + "=" + property.getLocator()));
                        return message;
                    }
                }

                return answer.getResultMessage();
            } catch (NoSuchElementException exception) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_NO_SUCH_ELEMENT);
                message.setDescription(message.getDescription().replace("%ELEMENT%", object.getIdentifier() + "=" + object.getLocator()));
                LOG.debug(exception.toString());
                return message;
            } catch (TimeoutException exception) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
                message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
                LOG.warn(exception.toString());
                return message;
            }

        } catch (CerberusEventException ex) {
            LOG.warn(ex);
            return ex.getMessageError();
        }
    }

    private void selectRequestedOption(Select select, Identifier property, String element) throws CerberusEventException {
        MessageEvent message;
        try {
            if (property.getIdentifier().equalsIgnoreCase("value")) {
                select.selectByValue(property.getLocator());
            } else if (property.getIdentifier().equalsIgnoreCase("label")) {
                select.selectByVisibleText(property.getLocator());
            } else if (property.getIdentifier().equalsIgnoreCase("index") && StringUtil.isInteger(property.getLocator())) {
                select.selectByIndex(Integer.parseInt(property.getLocator()));
            } else if (property.getIdentifier().equalsIgnoreCase("regexValue")
                    || property.getIdentifier().equalsIgnoreCase("regexIndex")
                    || property.getIdentifier().equalsIgnoreCase("regexLabel")) {
                java.util.List<WebElement> list = select.getOptions();
                if (property.getIdentifier().equalsIgnoreCase("regexValue")) {
                    for (WebElement option : list) {
                        String optionValue = option.getAttribute("value");
                        Pattern pattern = Pattern.compile(property.getLocator());
                        Matcher matcher = pattern.matcher(optionValue);
                        if (matcher.find()) {
                            select.selectByValue(optionValue);
                        }
                    }
                } else if (property.getIdentifier().equalsIgnoreCase("regexLabel")) {
                    for (WebElement option : list) {
                        String optionLabel = option.getText();
                        Pattern pattern = Pattern.compile(property.getLocator());
                        Matcher matcher = pattern.matcher(optionLabel);

                        if (matcher.find()) {
                            select.selectByVisibleText(optionLabel);
                        }
                    }
                } else if (property.getIdentifier().equalsIgnoreCase("regexIndex") && StringUtil.isInteger(property.getLocator())) {
                    for (WebElement option : list) {
                        Integer id = 0;
                        Pattern pattern = Pattern.compile(property.getLocator());
                        Matcher matcher = pattern.matcher(id.toString());

                        if (matcher.find()) {
                            select.selectByIndex(Integer.parseInt(property.getLocator()));
                        }
                        id++;
                    }
                }
            } else {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_NO_IDENTIFIER);
                message.setDescription(message.getDescription().replace("%IDENTIFIER%", property.getIdentifier()));
                throw new CerberusEventException(message);
            }
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_NO_SUCH_VALUE);
            message.setDescription(message.getDescription().replace("%ELEMENT%", element));
            message.setDescription(message.getDescription().replace("%DATA%", property.getIdentifier() + "=" + property.getLocator()));
            throw new CerberusEventException(message);
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            message.setDescription(message.getDescription().replace("%ERROR%", exception.getMessage().split("\n")[0]));
            LOG.warn(exception.toString());
            throw new CerberusEventException(message);
        } catch (PatternSyntaxException e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_REGEX_INVALIDPATERN);
            message.setDescription(message.getDescription().replace("%PATERN%", property.getLocator()));
            message.setDescription(message.getDescription().replace("%ERROR%", e.getMessage()));
            throw new CerberusEventException(message);
        }
    }

    @Override
    public MessageEvent doSeleniumActionUrlLogin(Session session, String host, String uri) {
        MessageEvent message;

        host = StringUtil.cleanHostURL(host);
        String url = StringUtil.getURLFromString(host, "", uri, "");

        try {
            session.getDriver().get(url);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_URLLOGIN);
            message.setDescription(message.getDescription().replace("%URL%", url));

        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_URLLOGIN_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_pageLoadTimeout())));
            message.setDescription(message.getDescription().replace("%URL%", url));
            LOG.warn(exception.toString());
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_URLLOGIN);
            message.setDescription(message.getDescription().replace("%URL%", url) + " " + e.getMessage());
        }
        return message;
    }

    @Override
    public MessageEvent doSeleniumActionFocusToIframe(Session session, Identifier identifier) {
        MessageEvent message;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, false, false);

            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();
                if (webElement != null) {
                    session.getDriver().switchTo().frame(webElement);
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSTOIFRAME);
                    message.setDescription(message.getDescription().replace("%IFRAME%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                    return message;
                }

            }
            return answer.getResultMessage();

        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_FOCUS_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%IFRAME%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
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
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }

        return message;
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
        List<String> result = new ArrayList<>();
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

    @Override
    public MessageEvent doSeleniumActionRightClick(Session session, Identifier identifier) {
        MessageEvent message;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, true, true);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();
                if (webElement != null) {
                    Actions actions = new Actions(session.getDriver());
                    actions.contextClick(webElement);
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_RIGHTCLICK);
                    message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                    return message;
                }
            }

            return answer.getResultMessage();
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_RIGHTCLICK_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }
    }

    /**
     * @author vertigo17
     * @param exception the exception need to be parsed by Cerberus
     * @return A new Event Message with selenium related description
     */
    private MessageEvent parseWebDriverException(WebDriverException exception) {
        MessageEvent mes;
        LOG.fatal(exception.toString());
        mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
        mes.setDescription(mes.getDescription().replace("%ERROR%", exception.getMessage().split("\n")[0]));
        return mes;
    }

}
