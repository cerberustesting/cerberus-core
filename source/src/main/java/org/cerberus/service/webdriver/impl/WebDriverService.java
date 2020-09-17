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
import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.service.impl.ParameterService;
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
import org.openqa.selenium.Platform;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class WebDriverService implements IWebDriverService {

    private static final int TIMEOUT_WEBELEMENT = 300;
    private static final int TIMEOUT_FOCUS = 1000;

    private static final Logger LOG = LogManager.getLogger("WebDriverService");

    @Autowired
    ParameterService parameterService;

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

    @Override
    public MessageEvent scrollTo(Session session, Identifier identifier, String text) {
        MessageEvent message = null;
        WebElement webElement = null;

        try {
            if (StringUtil.isNullOrEmpty(text)) {
                AnswerItem answer = this.getSeleniumElement(session, identifier, false, false);
                if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                    webElement = (WebElement) answer.getItem();
                }
            } else {
                webElement = session.getDriver().findElement(By.xpath("//*[contains(text()," + text + ")]"));
            }

            if (webElement != null) {

                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SCROLLTO).resolveDescription("VALUE", identifier.toString());
                if (StringUtil.isNullOrEmpty(text)) {
                    scrollElement(session, webElement);
                } else {
                    scrollText(session, webElement);
                }
            }

            return message;

        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SCROLL_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (Exception e) {
            LOG.error("An error occured during scroll to (element:" + identifier, e);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            message.setDescription(message.getDescription().replace("%DETAIL%", e.getMessage()));
            return message;

        }

    }

    private void scrollText(Session session,  WebElement element) {
        // Create instance of Javascript executor
        JavascriptExecutor je = (JavascriptExecutor) session.getDriver();

        // now execute query which actually will scroll until that element is not appeared on page.
        je.executeScript("arguments[0].scrollIntoView(true);window.scrollBy(" + session.getCerberus_selenium_autoscroll_horizontal_offset() + "," + session.getCerberus_selenium_autoscroll_vertical_offset() + ");", element);
    }

    private void scrollElement(Session session, WebElement element) {
        /**
         * WebElement element =
         * driver.findElement(By.id(identifier.getLocator())); Actions actions =
         * new Actions(driver); actions.moveToElement(element);
         * actions.perform();
         */
        ((JavascriptExecutor) session.getDriver()).executeScript("arguments[0].scrollIntoView();window.scrollBy(" + session.getCerberus_selenium_autoscroll_horizontal_offset() + "," + session.getCerberus_selenium_autoscroll_vertical_offset() + ");", element);

    }

    private AnswerItem<WebElement> getSeleniumElement(Session session, Identifier identifier, boolean visible, boolean clickable) {
        AnswerItem<WebElement> answer = new AnswerItem<>();
        MessageEvent msg;
        By locator = this.getBy(identifier);
        LOG.debug("Waiting for Element : " + identifier.getIdentifier() + "=" + identifier.getLocator());
        try {
            WebDriverWait wait = new WebDriverWait(session.getDriver(), TimeUnit.MILLISECONDS.toSeconds(session.getCerberus_selenium_wait_element()));
            WebElement element;
            if (visible) {
                if (session.isCerberus_selenium_autoscroll()) {
                    element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                    scrollElement(session, element);
                }
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
                if (webElement.getTagName() != null && webElement.getTagName().equalsIgnoreCase("select")) {
                    if (webElement.getAttribute("disabled") == null || webElement.getAttribute("disabled").isEmpty()) {
                        Select select = new Select(webElement);
                        result = select.getFirstSelectedOption().getText();
                    } else {
                        result = webElement.getText();
                        //result = "Unable to retrieve, element disabled ?";
                    }
                } else if (webElement.getTagName() != null && (webElement.getTagName().equalsIgnoreCase("option") || webElement.getTagName().equalsIgnoreCase("input"))) {
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
        String currentURL = session.getDriver().getCurrentUrl();
        String cleanedCurrentURL = StringUtil.removeProtocolFromHostURL(currentURL);
        String cleanedURL = StringUtil.removeProtocolFromHostURL(applicationUrl);
        // We remove from current url the host part of the application.
        String strings[] = cleanedCurrentURL.split(cleanedURL, 2);
        if (strings.length < 2) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_NOT_MATCH_APPLICATION);
            msg.setDescription(msg.getDescription().replace("%HOST%", applicationUrl));
            msg.setDescription(msg.getDescription().replace("%CURRENTURL%", currentURL));
            LOG.debug(msg.getDescription());
            throw new CerberusEventException(msg);
        }
        String result = StringUtil.addPrefixIfNotAlready(strings[1], "/");
        return result;
    }

    @Override
    public File takeScreenShotFile(Session session, String cropValues) {
        boolean event = true;
        long timeout = System.currentTimeMillis() + (session.getCerberus_selenium_wait_element());
        //Try to capture picture. Try again until timeout is WebDriverException is raised.
        while (event) {
            try {
                WebDriver augmentedDriver = new Augmenter().augment(session.getDriver());
                File image = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
                if (!StringUtil.isNullOrEmpty(cropValues)) {
                    BufferedImage fullImg = ImageIO.read(image);
                    // x - the X coordinate of the upper-left corner of the specified rectangular region y - the Y coordinate of the upper-left corner of the specified rectangular region w - the width of the specified rectangular region h - the height of the specified rectangular region 
                    //Left, Top, largeur-Top, largeur-Left-Right, hauteur-Top-Bottom
                    int l = getValue(cropValues, 0);
                    int r = getValue(cropValues, 1);
                    int t = getValue(cropValues, 2);
                    int b = getValue(cropValues, 3);
                    if ((fullImg.getWidth() > (l + r)) && (fullImg.getHeight() > (t + b))) {
                        BufferedImage eleScreenshot = fullImg.getSubimage(l, t, (fullImg.getWidth() - l - r), (fullImg.getHeight() - t - b));
                        ImageIO.write(eleScreenshot, "png", image);
                    }
                }

                if (image != null) {
                    //logs for debug purposes
                    LOG.info("WebDriverService: screenshot taken with succes: " + image.getName() + " (size : " + image.length() + ")");
                } else {
                    LOG.warn("WebDriverService: screen-shot returned null: ");
                }
                return image;
            } catch (WebDriverException exception) {
                if (System.currentTimeMillis() >= timeout) {
                    LOG.warn(exception.toString());
                }
                event = false;
            } catch (IOException ex) {
                LOG.error("Exception when reading snapshot generated.", ex);
            }
        }

        return null;
    }

    private int getValue(String cropValues, int index) {
        String[] cropS = cropValues.split(",");
        if (cropS.length < 4) {
            return 0;
        }
        int returnv = 0;
        try {
            returnv = Integer.valueOf(cropS[index]);
        } catch (NumberFormatException e) {
            LOG.debug("Failed to convert Integer.");
            return 0;
        }
        return returnv;
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

        try {
            return (session.getDriver().findElement(elementLocator) != null
                    && session.getDriver().findElement(elementLocator).findElement(childElementLocator) != null);
        } catch (NoSuchElementException e) {
            return false;
        }
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
                    /**
                     * Actions implementation doesn't work properly for test on
                     * browser on real mobile device use the implementation
                     * click from Selenium instead
                     */
                    if (Platform.ANDROID.equals(session.getDesiredCapabilities().getPlatform())
                            || Platform.IOS.equals(session.getDesiredCapabilities().getPlatform())) {
                        webElement.click();
                    } else {
                        /**
                         * webElement.click(); did not provide good result for
                         * generating Selenium error : Element is not clickable
                         * at point
                         * https://github.com/cerberustesting/cerberus-source/issues/2030
                         */
                        //                    webElement.click();
                        Actions actions = new Actions(session.getDriver());
                        actions.click(webElement);
                        actions.build().perform();
                    }
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
                    message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                    return message;
                }
            }

            return answer.getResultMessage();
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }

    }

    @Override
    public MessageEvent doSeleniumActionMouseDown(Session session, Identifier identifier, boolean waitForVisibility, boolean waitForClickability) {
        MessageEvent message;
        try {

            AnswerItem answer = this.getSeleniumElement(session, identifier, waitForVisibility, waitForClickability);
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
    public MessageEvent doSeleniumActionMouseUp(Session session, Identifier identifier, boolean waitForVisibility, boolean waitForClickability) {
        MessageEvent message;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, waitForVisibility, waitForClickability);
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

    @Override
    public MessageEvent doSeleniumActionManageDialogKeyPress(Session session, String valueToPress) {
        MessageEvent message;
        String newKey = valueToPress;
        try {
            // Mapp Keys.
            // Not mapped keys are : NULL, CANCEL, HELP, , CLEAR, PAUSE, END, HOME, INSERT, META, COMMAND, ZENKAKU_HANKAKU

            newKey = newKey.replace("[TAB]", Keys.TAB)
                    .replace("[BACK_SPACE]", Keys.BACK_SPACE)
                    .replace("[RETURN]", Keys.RETURN).replace("[ENTER]", Keys.ENTER)
                    .replace("[SHIFT]", Keys.SHIFT).replace("[LEFT_SHIFT]", Keys.LEFT_SHIFT)
                    .replace("[CONTROL]", Keys.CONTROL).replace("[LEFT_CONTROL]", Keys.LEFT_CONTROL)
                    .replace("[ALT]", Keys.ALT).replace("[LEFT_ALT]", Keys.LEFT_ALT)
                    .replace("[ESCAPE]", Keys.ESCAPE)
                    .replace("[SPACE]", Keys.SPACE)
                    .replace("[PAGE_UP]", Keys.PAGE_UP).replace("[PAGE_DOWN]", Keys.PAGE_DOWN)
                    .replace("[LEFT]", Keys.LEFT).replace("[ARROW_LEFT]", Keys.ARROW_LEFT)
                    .replace("[UP]", Keys.UP).replace("[ARROW_UP]", Keys.ARROW_UP)
                    .replace("[RIGHT]", Keys.RIGHT).replace("[ARROW_RIGHT]", Keys.ARROW_RIGHT)
                    .replace("[DOWN]", Keys.DOWN).replace("[ARROW_DOWN]", Keys.ARROW_DOWN)
                    .replace("[DELETE]", Keys.DELETE)
                    .replace("[SEMICOLON]", Keys.SEMICOLON)
                    .replace("[EQUALS]", Keys.EQUALS)
                    .replace("[NUMPAD0]", Keys.NUMPAD0).replace("[NUMPAD1]", Keys.NUMPAD1).replace("[NUMPAD2]", Keys.NUMPAD2).replace("[NUMPAD3]", Keys.NUMPAD3)
                    .replace("[NUMPAD4]", Keys.NUMPAD4).replace("[NUMPAD5]", Keys.NUMPAD5).replace("[NUMPAD6]", Keys.NUMPAD6).replace("[NUMPAD7]", Keys.NUMPAD7)
                    .replace("[NUMPAD8]", Keys.NUMPAD8).replace("[NUMPAD9]", Keys.NUMPAD9)
                    .replace("[MULTIPLY]", Keys.MULTIPLY)
                    .replace("[ADD]", Keys.ADD)
                    .replace("[SEPARATOR]", Keys.SEPARATOR)
                    .replace("[SUBTRACT]", Keys.SUBTRACT)
                    .replace("[DECIMAL]", Keys.DECIMAL)
                    .replace("[DIVIDE]", Keys.DIVIDE)
                    .replace("[F1]", Keys.F1).replace("[F2]", Keys.F2).replace("[F3]", Keys.F3).replace("[F4]", Keys.F4).replace("[F5]", Keys.F5)
                    .replace("[F6]", Keys.F6).replace("[F7]", Keys.F7).replace("[F8]", Keys.F8).replace("[F9]", Keys.F9).replace("[F10]", Keys.F10).replace("[F11]", Keys.F11).replace("[F12]", Keys.F12);

            // Press Keyx on an alert popup dialog.
            session.getDriver().switchTo().alert().sendKeys(newKey);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_ALERT);
            message.setDescription(message.getDescription().replace("%KEY%", valueToPress));
            return message;
        } catch (NoSuchWindowException exception) {
            // Add try catch to handle not exist anymore alert popup (like when popup is closed).
            LOG.debug("Alert popup still exist ? " + exception.toString());
        } catch (TimeoutException exception) {
            LOG.warn(exception.toString());
        } catch (WebDriverException exception) {
            LOG.debug("Alert popup still exist ? " + exception.toString());
            return parseWebDriverException(exception);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_ALERT);
        message.setDescription(message.getDescription().replace("%KEY%", valueToPress));
        return message;
    }

    private boolean checkIfExpectedWindow(Session session, String identifier, String value) {

        boolean result = false;
        WebDriverWait wait = new WebDriverWait(session.getDriver(), TimeUnit.MILLISECONDS.toSeconds(session.getCerberus_selenium_wait_element()));
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
    public MessageEvent doSeleniumActionType(Session session, Identifier identifier, String valueToType, String propertyName, boolean waitForVisibility, boolean waitForClickability) {
        MessageEvent message;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, waitForVisibility, waitForClickability);
            if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement webElement = (WebElement) answer.getItem();
                if (webElement != null) {
                    webElement.clear();
                    if (!StringUtil.isNull(valueToType)) {
                        webElement.sendKeys(valueToType);
                    }
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TYPE);
                    message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                    if (!StringUtil.isNull(valueToType)) {
                        message.setDescription(message.getDescription().replace("%DATA%", ParameterParserUtil.securePassword(valueToType, propertyName)));
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
    public MessageEvent doSeleniumActionMouseOver(Session session, Identifier identifier, boolean waitForVisibility, boolean waitForClickability) {
        MessageEvent message;
        try {
            AnswerItem answer = this.getSeleniumElement(session, identifier, waitForVisibility, waitForClickability);
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
            WebDriverWait wait = new WebDriverWait(session.getDriver(), TimeUnit.MILLISECONDS.toSeconds(session.getCerberus_selenium_wait_element()));
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
            WebDriverWait wait = new WebDriverWait(session.getDriver(), TimeUnit.MILLISECONDS.toSeconds(session.getCerberus_selenium_wait_element()));
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

    @Override
    public MessageEvent doSeleniumActionRefreshCurrentPage(Session session) {

        MessageEvent message;

        try {
            session.getDriver().navigate().refresh();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_REFRESHCURRENTPAGE);
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_REFRESHCURRENTPAGE);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_pageLoadTimeout())));
            LOG.warn(exception.toString());
        } catch (WebDriverException exception) {
            LOG.warn(exception.toString());
            return parseWebDriverException(exception);
        }
        return message;
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
        try {
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

        } catch (Exception e) {
            LOG.error(e, e);
        }

        return false;
    }

    @Override
    public MessageEvent doSeleniumActionKeyPress(Session session, Identifier identifier, String keyToPress, boolean waitForVisibility, boolean waitForClickability) {

        MessageEvent message;
        try {
            if (!StringUtil.isNullOrEmpty(identifier.getLocator())) {
                AnswerItem answer = this.getSeleniumElement(session, identifier, waitForVisibility, waitForClickability);
                if (answer.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                    WebElement webElement = (WebElement) answer.getItem();
                    if (webElement != null) {
                        webElement.sendKeys(Keys.valueOf(keyToPress));
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS);
                        message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
                        message.setDescription(message.getDescription().replace("%DATA%", keyToPress));
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
                        Duration mydur = Duration.ofMillis(TIMEOUT_FOCUS);
                        wait.withTimeout(mydur);
                    }

                    //gets the robot
                    Robot r = new Robot();
                    //converts the Key description sent through Cerberus into the AWT key code
                    int keyCode = KeyCodeEnum.getAWTKeyCode(keyToPress);

                    if (keyCode != KeyCodeEnum.NOT_VALID.getKeyCode()) {
                        //if the code is valid then presses the key and releases the key
                        r.keyPress(keyCode);
                        r.keyRelease(keyCode);
                        //wait until the action is performed
                        Duration mydur = Duration.ofMillis(TIMEOUT_WEBELEMENT);
                        wait.withTimeout(mydur);

                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT);
                    } else {
                        //the key enterer is not valid
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_NOT_AVAILABLE);
                        LOG.debug("Key " + keyToPress + "is not available in the current context");
                    }

                    message.setDescription(message.getDescription().replace("%KEY%", keyToPress));

                } catch (AWTException ex) {
                    LOG.warn(ex);
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_ENV_ERROR);

                } catch (Exception exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
                    message.setDescription(message.getDescription().replace("%DETAIL%", exception.toString()));
                    LOG.debug(exception.toString());

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

        } catch (Exception exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            message.setDescription(message.getDescription().replace("%DETAIL%", exception.toString()));
            LOG.debug(exception.toString());

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
    public MessageEvent doSeleniumActionSelect(Session session, Identifier object, Identifier property, boolean waitForVisibility, boolean waitForClickability) {
        MessageEvent message;
        try {
            Select select;
            try {
                AnswerItem answer = this.getSeleniumElement(session, object, waitForVisibility, waitForClickability);
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

    // we need to implement the right selenium dragAndDrop method when it works
    @Override
    public MessageEvent doSeleniumActionDragAndDrop(Session session, Identifier drag, Identifier drop, boolean waitForVisibility, boolean waitForClickability) throws IOException {
        MessageEvent message;
        try {
            AnswerItem answerDrag = this.getSeleniumElement(session, drag, waitForVisibility, waitForClickability);
            AnswerItem answerDrop = this.getSeleniumElement(session, drop, waitForVisibility, waitForClickability);
            if (answerDrag.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())
                    && answerDrop.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT.getCode())) {
                WebElement source = (WebElement) answerDrag.getItem();
                WebElement target = (WebElement) answerDrop.getItem();
                if (source != null && target != null) {
                    ((JavascriptExecutor) session.getDriver()).executeScript("function createEvent(typeOfEvent) {\n" + "var event =document.createEvent(\"CustomEvent\");\n"
                            + "event.initCustomEvent(typeOfEvent,true, true, null);\n" + "event.dataTransfer = {\n" + "data: {},\n"
                            + "setData: function (key, value) {\n" + "this.data[key] = value;\n" + "},\n"
                            + "getData: function (key) {\n" + "return this.data[key];\n" + "}\n" + "};\n" + "return event;\n"
                            + "}\n" + "\n" + "function dispatchEvent(element, event,transferData) {\n"
                            + "if (transferData !== undefined) {\n" + "event.dataTransfer = transferData;\n" + "}\n"
                            + "if (element.dispatchEvent) {\n" + "element.dispatchEvent(event);\n"
                            + "} else if (element.fireEvent) {\n" + "element.fireEvent(\"on\" + event.type, event);\n" + "}\n"
                            + "}\n" + "\n" + "function simulateHTML5DragAndDrop(element, destination) {\n"
                            + "var dragStartEvent =createEvent('dragstart');\n" + "dispatchEvent(element, dragStartEvent);\n"
                            + "var dropEvent = createEvent('drop');\n"
                            + "dispatchEvent(destination, dropEvent,dragStartEvent.dataTransfer);\n"
                            + "var dragEndEvent = createEvent('dragend');\n"
                            + "dispatchEvent(element, dragEndEvent,dropEvent.dataTransfer);\n" + "}\n" + "\n"
                            + "var source = arguments[0];\n" + "var destination = arguments[1];\n"
                            + "simulateHTML5DragAndDrop(source,destination);", source, target);
                }
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DRAGANDDROP);
                message.setDescription(message.getDescription().replace("%SOURCE%", drag.getIdentifier() + "=" + drag.getLocator()));
                message.setDescription(message.getDescription().replace("%TARGET%", drop.getIdentifier() + "=" + drop.getLocator()));
                return message;
            } else {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DRAGANDDROP_NO_SUCH_ELEMENT);
                message.setDescription(message.getDescription().replace("%ELEMENT%", drag.getIdentifier() + "=" + drag.getLocator()));
                return message;
            }
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DRAGANDDROP_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", drag.getIdentifier() + "=" + drag.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (TimeoutException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TIMEOUT);
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(session.getCerberus_selenium_wait_element())));
            LOG.warn(exception.toString());
            return message;
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
    public Integer getNumberOfElements(Session session, Identifier identifier) {
        By locator = this.getBy(identifier);
        return session.getDriver().findElements(locator).size();
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
