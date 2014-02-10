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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.log4j.Level;
import org.cerberus.entity.Invariant;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.Parameter;
import org.cerberus.entity.Selenium;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactorySelenium;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.IParameterService;
import org.cerberus.serviceEngine.IPropertyService;
import org.cerberus.serviceEngine.ISeleniumService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public class SeleniumService implements ISeleniumService {

    private static final int TIMEOUT_MILLIS = 30000;
    private static final int TIMEOUT_WEBELEMENT = 300;
    private Selenium selenium;
    private boolean started;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IFactorySelenium factorySelenium;
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private IInvariantService invariantService;

    @Override
    public MessageGeneral startSeleniumServer(long runId, String host, String port, String browser, String ip, String login, int verbose, String country) {

        if (!this.started) {
            /**
             * We activate Network Traffic for verbose 1 and 2.
             */
            boolean record = (verbose > 0);
            long defaultWait;
            try {
                Parameter param = parameterService.findParameterByKey("selenium_defaultWait", "");
                defaultWait = Long.parseLong(param.getValue());
            } catch (CerberusException ex) {
                MyLogger.log(Selenium.class.getName(), Level.WARN, "Parameter (selenium_defaultWait) not in Parameter table, default wait set to 90 seconds");
                defaultWait = 90;
            }

            this.selenium = factorySelenium.create(host, port, browser, login, ip, null, defaultWait);
            Logger.getLogger(SeleniumService.class.getName()).log(java.util.logging.Level.WARNING, null, browser);

            try {

                if (this.invariantService.isInvariantExist("BROWSER", browser)) {
                    startSeleniumBrowser(runId, record, country, browser);
                } else {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                    mes.setDescription(mes.getDescription().replaceAll("%MES%", "Browser " + browser + " is not supported."));
                    return mes;
                }

                this.selenium.getDriver().manage().window().maximize();
                this.started = true;
                return new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS);
            } catch (CerberusException ex) {
                Logger.getLogger(SeleniumService.class.getName()).log(java.util.logging.Level.WARNING, null, ex.getMessage());
                return ex.getMessageError();
            }
        }
        return new MessageGeneral(MessageGeneralEnum.EXECUTION_FA);
    }

    @Override
    public boolean isSeleniumServerReachable(String host, String port) {
        try {
            TelnetClient tc = new TelnetClient();
            tc.connect(host, Integer.valueOf(port));
            if (tc.isConnected()) {
                tc.disconnect();
                return true;
            }
        } catch (MalformedURLException exception) {
            MyLogger.log(SeleniumService.class.getName(), Level.WARN, exception.toString());
        } catch (ProtocolException exception) {
            MyLogger.log(SeleniumService.class.getName(), Level.WARN, exception.toString());
        } catch (IOException exception) {
            MyLogger.log(SeleniumService.class.getName(), Level.WARN, exception.toString());
        }
        return false;
    }

    @Override
    public FirefoxProfile setFirefoxProfile(long runId, boolean record, String country) throws CerberusException {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setEnableNativeEvents(true);
        profile.setAcceptUntrustedCertificates(true);
        profile.setPreference("network.http.connection-timeout", "300");

        try {
            Invariant invariant = this.invariantService.findInvariantByIdValue("COUNTRY", country);
            if (invariant.getGp2() == null) {
                MyLogger.log(Selenium.class.getName(), Level.WARN, "Country selected (" + country + ") has no value of GP2 in Invariant table, default language set to English(en)");
                profile.setPreference("intl.accept_languages", "en");
            } else {
                profile.setPreference("intl.accept_languages", invariant.getGp2());
            }
        } catch (CerberusException ex) {
            MyLogger.log(Selenium.class.getName(), Level.WARN, "Country selected (" + country + ") not in Invariant table, default language set to English(en)");
            profile.setPreference("intl.accept_languages", "en");
        }

        if (record) {
            String firebugPath = parameterService.findParameterByKey("cerberus_selenium_firefoxextension_firebug", "").getValue();
            String netexportPath = parameterService.findParameterByKey("cerberus_selenium_firefoxextension_netexport", "").getValue();
            if (StringUtil.isNullOrEmpty(firebugPath)) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription(mes.getDescription().replaceAll("%MES%", "Mandatory parameter for network traffic 'cerberus_selenium_firefoxextension_firebug' not defined."));
                throw new CerberusException(mes);
            }
            if (StringUtil.isNullOrEmpty(netexportPath)) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription(mes.getDescription().replaceAll("%MES%", "Mandatory parameter for network traffic 'cerberus_selenium_firefoxextension_netexport' not defined."));
                throw new CerberusException(mes);
            }

            File firebug = new File(firebugPath);
            if (!firebug.canRead()) {
                MyLogger.log(Selenium.class.getName(), Level.WARN, "Can't read : " + firebugPath);
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription(mes.getDescription().replaceAll("%MES%", "File not found : '" + firebugPath + "' Change the Cerberus parameter : cerberus_selenium_firefoxextension_firebug"));
                throw new CerberusException(mes);

            }
            try {
                MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Adding firebug extension : " + firebugPath);
                profile.addExtension(firebug);
            } catch (IOException exception) {
                MyLogger.log(Selenium.class.getName(), Level.WARN, exception.toString());
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription(mes.getDescription().replaceAll("%MES%", "File not found : " + firebugPath));
                throw new CerberusException(mes);
            }

            File netExport = new File(netexportPath);
            if (!netExport.canRead()) {
                MyLogger.log(Selenium.class.getName(), Level.WARN, "Can't read : " + netexportPath);
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription(mes.getDescription().replaceAll("%MES%", "File not found : " + netexportPath + "' Change the Cerberus parameter : cerberus_selenium_firefoxextension_netexport"));
                throw new CerberusException(mes);
            }
            try {
                MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Adding netexport extension : " + netexportPath);
                profile.addExtension(netExport);
            } catch (IOException exception) {
                MyLogger.log(Selenium.class.getName(), Level.WARN, exception.toString());
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription(mes.getDescription().replaceAll("%MES%", "File not found : " + netexportPath));
                throw new CerberusException(mes);
            }

            String cerberusUrl = parameterService.findParameterByKey("cerberus_url", "").getValue();
            if (StringUtil.isNullOrEmpty(cerberusUrl)) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription(mes.getDescription().replaceAll("%MES%", "Mandatory parameter for network traffic 'cerberus_url' not defined."));
                throw new CerberusException(mes);
            }


            // Set default Firefox preferences
            profile.setPreference("app.update.enabled", false);

            // Set default Firebug preferences
            profile.setPreference("extensions.firebug.currentVersion", "1.11.4");
            profile.setPreference("extensions.firebug.allPagesActivation", "on");
            profile.setPreference("extensions.firebug.defaultPanelName", "net");
            profile.setPreference("extensions.firebug.net.enableSites", true);

            // Set default NetExport preferences
            profile.setPreference("extensions.firebug.netexport.alwaysEnableAutoExport", true);
            // Export to Server.
            String url = cerberusUrl + "/SaveStatistic?logId=" + runId;
            profile.setPreference("extensions.firebug.netexport.autoExportToServer", true);
            profile.setPreference("extensions.firebug.netexport.beaconServerURL", url);
            MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Selenium netexport.beaconServerURL : " + url);
            // Export to File. This only works on the selenium server side so should not be used as we don't know where to put the file and neither if Linux or Windows based.
//            String cerberusHarPath = "logHar" + myFile.separator;
//            String cerberusHarPath = "logHar" ;
//            File dir = new File(cerberusHarPath + runId);
//            dir.mkdirs();
//            profile.setPreference("extensions.firebug.netexport.autoExportToFile", true);
//            profile.setPreference("extensions.firebug.netexport.defaultLogDir", cerberusHarPath );
//            MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Selenium netexport.defaultLogDir : " + cerberusHarPath);
            profile.setPreference("extensions.firebug.netexport.sendToConfirmation", false);
            profile.setPreference("extensions.firebug.netexport.showPreview", false);

        }

        return profile;
    }

    @Override
    public boolean startSeleniumBrowser(long runId, boolean record, String country, String browser) throws CerberusException {

        MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Starting " + browser);

        DesiredCapabilities capabilities = null;

        if (browser.equalsIgnoreCase("firefox")) {
            capabilities = DesiredCapabilities.firefox();
            FirefoxProfile profile = setFirefoxProfile(runId, record, country);
            capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        } else if (browser.equalsIgnoreCase("iexplorer")) {
            capabilities = DesiredCapabilities.internetExplorer();
        } else if (browser.equalsIgnoreCase("chrome")) {
            capabilities = DesiredCapabilities.chrome();
        }


        try {
            MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Set Driver");
            WebDriver driver = new RemoteWebDriver(new URL("http://" + selenium.getHost() + ":" + selenium.getPort() + "/wd/hub"), capabilities);
            selenium.setDriver(driver);
        } catch (MalformedURLException exception) {
            MyLogger.log(Selenium.class.getName(), Level.ERROR, exception.toString());
            return false;
        } catch (UnreachableBrowserException exception) {
            MyLogger.log(Selenium.class.getName(), Level.WARN, exception.toString());
            return false;
        } catch (Exception exception) {
            MyLogger.log(Selenium.class.getName(), Level.WARN, exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
            mes.setDescription(mes.getDescription().replaceAll("%MES%", "Selenium error : " + exception.getMessage().split("\n")[0]));
            throw new CerberusException(mes);
        }

        return true;
    }

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

    private WebElement getSeleniumElement(String input, boolean visible) {
        By locator = this.getIdentifier(input);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Waiting for Element : " + input);
        try {
            WebDriverWait wait = new WebDriverWait(this.selenium.getDriver(), this.selenium.getDefaultWait());
            if (visible) {
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } else {
                wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            }
        } catch (TimeoutException exception) {
            throw new NoSuchElementException(input);
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Finding selenium Element : " + input);
        return this.selenium.getDriver().findElement(locator);
    }

    @Override
    public boolean stopSeleniumServer() {
        if (this.started) {

            try {
                // Wait 2 sec till HAR is exported
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SeleniumService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

            this.selenium.getDriver().quit();
            this.started = false;
            return true;
        }

        return false;
    }

    @Override
    public String getValueFromHTMLVisible(String locator) {
        WebElement webElement = this.getSeleniumElement(locator, true);
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
    public String getValueFromHTML(String locator) {
        WebElement webElement = this.getSeleniumElement(locator, false);
        String result;

        if (webElement.getTagName().equalsIgnoreCase("select")) {
            Select select = (Select) webElement;
            result = select.getFirstSelectedOption().getText();
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
            result = (String) ((JavascriptExecutor) this.selenium.getDriver()).executeScript(script, webElement);
        }

        return result;
    }

    @Override
    public String getValueFromJS(String script) {
        JavascriptExecutor js = (JavascriptExecutor) selenium.getDriver();
        Object response = js.executeScript(script);

        if (response == null) {
            return "";
        }

        return String.valueOf(response);
    }

    @Override
    public boolean isElementPresent(String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(locator, false);
            return webElement != null;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public boolean isElementVisible(String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(locator, true);
            return webElement != null && webElement.isDisplayed();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public boolean isElementNotVisible(String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(locator, false);
            return webElement != null && !webElement.isDisplayed();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public String getPageSource() {
        return this.selenium.getDriver().getPageSource();
    }

    @Override
    public String getTitle() {
        return this.selenium.getDriver().getTitle();
    }

    /**
     * Return the current URL from Selenium.
     *
     * @return current URL without HTTP://IP:PORT/CONTEXTROOT/
     * @throws CerberusEventException Cannot find application host (from
     * Database) inside current URL (from Selenium)
     */
    @Override
    public String getCurrentUrl() throws CerberusEventException {
        /*
         * Example: URL (http://mypage/page/index.jsp), IP (mypage)
         * URL.split(IP, 2)
         * Pos | Description
         *  0  |    http://
         *  1  |    /page/index.jsp
         */
        String strings[] = this.selenium.getDriver().getCurrentUrl().split(this.selenium.getIp(), 2);
        if (strings.length < 2) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_NOT_MATCH_APPLICATION);
            msg.setDescription(msg.getDescription().replaceAll("%HOST%", this.selenium.getDriver().getCurrentUrl()));
            msg.setDescription(msg.getDescription().replaceAll("%URL%", this.selenium.getIp()));
            MyLogger.log(SeleniumService.class.getName(), Level.WARN, msg.toString());
            throw new CerberusEventException(msg);
        }
        return strings[1];
    }

    @Override
    public String getFullBrowserVersion() {

        Capabilities caps = ((RemoteWebDriver) this.selenium.getDriver()).getCapabilities();
        String VerPlatform = caps.getBrowserName() + " " + caps.getVersion() + " " + caps.getPlatform().toString();
        MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Browser & Platform version : " + VerPlatform);

        return VerPlatform;
    }

    @Override
    public void doScreenShot(String runId, String name) {
        try {
            WebDriver augmentedDriver = new Augmenter().augment(this.selenium.getDriver());
            File image = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
            BufferedImage bufferedImage = ImageIO.read(image);

            String imgPath;
            try {
                imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
                File dir = new File(imgPath + runId);
                dir.mkdirs();

                BufferedImage newImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                newImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
                ImageIO.write(newImage, "jpg", new File(imgPath + runId + File.separator + name));
            } catch (CerberusException ex) {
                Logger.getLogger(SeleniumService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

        } catch (IOException exception) {
            MyLogger.log(SeleniumService.class.getName(), Level.WARN, exception.toString());
        } catch (WebDriverException exception) {
            //TODO check why occur
            //possible that the page still loading
            MyLogger.log(SeleniumService.class.getName(), Level.WARN, exception.toString());
        }
    }

    @Override
    public TestCaseStepActionExecution doAction(TestCaseStepActionExecution testCaseStepActionExecution) {
        /**
         * Decode the 2 fields property and values before doing the control.
         */
        if (testCaseStepActionExecution.getObject().contains("%")) {
            String decodedValue = propertyService.decodeValue(testCaseStepActionExecution.getObject(), testCaseStepActionExecution.getTestCaseExecutionDataList(), testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution());
            testCaseStepActionExecution.setObject(decodedValue);
        }

        /**
         * Timestamp starts after the decode. TODO protect when property is
         * null.
         */
        testCaseStepActionExecution.setStart(new Date().getTime());

        String object = testCaseStepActionExecution.getObject();
        String property = testCaseStepActionExecution.getProperty();
        String propertyName = testCaseStepActionExecution.getPropertyName();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Doing Action : " + testCaseStepActionExecution.getAction() + " with object : " + object + " and property : " + property);

        MessageEvent res;

        //TODO On JDK 7 implement switch with string
        if (testCaseStepActionExecution.getAction().equals("click")) {
            res = this.doActionClick(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("clickAndWait")) {
            res = this.doActionClickWait(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("doubleClick")) {
            res = this.doActionDoubleClick(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("enter")) {
            res = this.doActionKeyPress(object, "RETURN");

        } else if (testCaseStepActionExecution.getAction().equals("keypress")) {
            res = this.doActionKeyPress(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseOver")) {
            res = this.doActionMouseOver(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseOverAndWait")) {
            res = this.doActionMouseOverAndWait(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("openUrlWithBase")) {
            res = this.doActionOpenURLWithBase(object);

        } else if (testCaseStepActionExecution.getAction().equals("openUrlLogin")) {
            testCaseStepActionExecution.setObject(this.selenium.getLogin());
            res = this.doActionUrlLogin();

        } else if (testCaseStepActionExecution.getAction().equals("select")) {
            res = this.doActionSelect(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("selectAndWait")) {
            res = this.doActionSelect(object, property);
            this.doActionWait(StringUtil.NULL, StringUtil.NULL);

        } else if (testCaseStepActionExecution.getAction().equals("focusToIframe")) {
            res = this.doActionFocusToIframe(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("focusDefaultIframe")) {
            res = this.doActionFocusDefaultIframe();

        } else if (testCaseStepActionExecution.getAction().equals("type")) {
            res = this.doActionType(object, property, propertyName);

        } else if (testCaseStepActionExecution.getAction().equals("wait")) {
            res = this.doActionWait(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseDown")) {
            res = this.doActionMouseDown(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseUp")) {
            res = this.doActionMouseUp(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("switchToWindow")) {
            res = this.doActionSwitchToWindow(object, property);

        } else if (testCaseStepActionExecution.getAction().equals("calculateProperty")) {
            res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_PROPERTYCALCULATED);
            res.setDescription(res.getDescription().replaceAll("%PROP%", testCaseStepActionExecution.getPropertyName()));
        } else if (testCaseStepActionExecution.getAction().equals("takeScreenshot")) {
            res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TAKESCREENSHOT);
        } else {
            res = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKNOWNACTION);
            res.setDescription(res.getDescription().replaceAll("%ACTION%", testCaseStepActionExecution.getAction()));
        }

        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Result of the action : " + res.getCodeString() + " " + res.getDescription());
        testCaseStepActionExecution.setActionResultMessage(res);

        /**
         * Determine here the impact of the Action on the full test return code
         * from the ResultMessage of the Action.
         */
        testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(res.getMessage()));
        /**
         * Determine here if we stop the test from the ResultMessage of the
         * Action.
         */
        testCaseStepActionExecution.setStopExecution(res.isStopTest());

        testCaseStepActionExecution.setEnd(new Date().getTime());
        return testCaseStepActionExecution;
    }

    private MessageEvent doActionClick(String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
                    this.getSeleniumElement(string1, true).click();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    this.getSeleniumElement(string2, true).click();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_CLICK);
    }

    private MessageEvent doActionMouseDown(String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
                    Actions actions = new Actions(this.selenium.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(string1, true));
                    actions.perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEDOWN_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    Actions actions = new Actions(this.selenium.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(string2, true));
                    actions.perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEDOWN_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_CLICK);
    }

    private MessageEvent doActionMouseUp(String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
                    Actions actions = new Actions(this.selenium.getDriver());
                    actions.release(this.getSeleniumElement(string1, true));
                    actions.perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEUP);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEUP_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    Actions actions = new Actions(this.selenium.getDriver());
                    actions.release(this.getSeleniumElement(string2, true));
                    actions.perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEUP);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEUP_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_CLICK);
    }

    private MessageEvent doActionSwitchToWindow(String string1, String string2) {
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
                String identifier,value;
                if (strings.length == 1) {
                    identifier = "value";
                    value = strings[0];
                } else {
                    identifier = strings[0];
                    value = strings[1];
                }
                
                String currentHandle;
                try {
                    // Current serial handle of the window.
                    currentHandle = this.selenium.getDriver().getWindowHandle();
                } catch (NoSuchWindowException exception) {
                    // Add try catch to handle not exist anymore window (like when popup is closed).
                    currentHandle = null;
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Window is closed ? " + exception.toString());
                }

                try {
                    // Get serials handles list of all browser windows
                    Set<String> handles = this.selenium.getDriver().getWindowHandles();

                    // Loop into each of them
                    for (String windowHandle : handles) {
                        if (!windowHandle.equals(currentHandle)) {
                            this.selenium.getDriver().switchTo().window(windowHandle);
                            if ("value".equals(identifier) && value.equals(this.selenium.getDriver().getTitle())
                                    || "contains".equals(identifier) && value.indexOf(this.selenium.getDriver().getTitle())>=0) {
                                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SWITCHTOWINDOW);
                                message.setDescription(message.getDescription().replaceAll("%WINDOW%", windowTitle));
                                return message;
                            }
                        }
                        MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "windowHandle=" + windowHandle);
                    }
                } catch (NoSuchElementException exception) {
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SWITCHTOWINDOW_NO_SUCH_ELEMENT);
        message.setDescription(message.getDescription().replaceAll("%WINDOW%", windowTitle));
        return message;
    }

    private MessageEvent doActionClickWait(String actionObject, String actionProperty) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                try {
                    this.getSeleniumElement(actionObject, true).click();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
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
                        MyLogger.log(SeleniumService.class.getName(), Level.INFO, e.toString());
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
                    this.getSeleniumElement(actionObject, true).click();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICKANDNOWAIT);
                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                return message;
            } else if (!StringUtil.isNull(actionProperty) && StringUtil.isNull(actionObject)) {
                try {
                    this.getSeleniumElement(actionProperty, true).click();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionProperty));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICKANDNOWAIT);
                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionProperty));
                return message;
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICKANDWAIT_GENERIC);
    }

    private MessageEvent doActionDoubleClick(String html, String property) {
        MessageEvent message;
        try {
            Actions actions = new Actions(this.selenium.getDriver());
            if (!StringUtil.isNull(property)) {
                try {
                    actions.doubleClick(this.getSeleniumElement(property, true));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DOUBLECLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(html)) {
                try {
                    actions.doubleClick(this.getSeleniumElement(html, true));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DOUBLECLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK);
    }

    private MessageEvent doActionType(String html, String property, String propertyName) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(html)) {
                try {
                    WebElement webElement = this.getSeleniumElement(html, true);
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
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
    }

    private MessageEvent doActionMouseOver(String html, String property) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(html)) {
                try {
                    Actions actions = new Actions(this.selenium.getDriver());
                    WebElement menuHoverLink = this.getSeleniumElement(html, true);
                    actions.moveToElement(menuHoverLink);
                    actions.perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVER);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(property)) {
                try {
                    Actions actions = new Actions(this.selenium.getDriver());
                    WebElement menuHoverLink = this.getSeleniumElement(property, true);
                    actions.moveToElement(menuHoverLink);
                    actions.perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVER);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER);
    }

    private MessageEvent doActionMouseOverAndWait(String actionObject, String actionProperty) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                if (StringUtil.isNumeric(actionProperty)) {
                    try {
                        Actions actions = new Actions(this.selenium.getDriver());
                        WebElement menuHoverLink = this.getSeleniumElement(actionObject, true);
                        actions.moveToElement(menuHoverLink);
                        actions.perform();
                        int sleep = Integer.parseInt(actionProperty);
                        try {
                            Thread.sleep(sleep);
                            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVERANDWAIT);
                            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                            message.setDescription(message.getDescription().replaceAll("%TIME%", Integer.toString(sleep)));
                            return message;
                        } catch (InterruptedException e) {
                            MyLogger.log(SeleniumService.class.getName(), Level.INFO, e.toString());
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVERANDWAIT);
                            message.setDescription(message.getDescription().replaceAll("%ELEMENT1%", actionObject));
                            message.setDescription(message.getDescription().replaceAll("%TIME%", Integer.toString(sleep)));
                            return message;
                        }
                    } catch (NoSuchElementException exception) {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                        MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                        return message;
                    }
                }
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVERANDWAIT_NO_NUMERIC);
                message.setDescription(message.getDescription().replaceAll("%TIME%", actionProperty));
                return message;
            } else if (StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                try {
                    Actions actions = new Actions(this.selenium.getDriver());
                    WebElement menuHoverLink = this.getSeleniumElement(actionObject, true);
                    actions.moveToElement(menuHoverLink);
                    actions.perform();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICKANDNOWAIT);
                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                return message;
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVERANDWAIT_GENERIC);
    }

    private MessageEvent doActionWait(String object, String property) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(property)) {
                if (StringUtil.isNumeric(property)) {
                    try {
                        Thread.sleep(Integer.parseInt(property));
                    } catch (InterruptedException exception) {
                        MyLogger.log(SeleniumService.class.getName(), Level.INFO, exception.toString());
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT);
                        message.setDescription(message.getDescription().replaceAll("%TIME%", property));
                        return message;
                    }
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME);
                    message.setDescription(message.getDescription().replaceAll("%TIME%", property));
                    return message;
                } else {
                    try {
                        WebDriverWait wait = new WebDriverWait(this.selenium.getDriver(), TIMEOUT_WEBELEMENT);
                        wait.until(ExpectedConditions.presenceOfElementLocated(this.getIdentifier(property)));
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                        return message;
                    } catch (NoSuchElementException exception) {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                        MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                        return message;
                    }
                }
            } else if (!StringUtil.isNull(object)) {
                if (StringUtil.isNumeric(object)) {
                    try {
                        Thread.sleep(Integer.parseInt(object));
                    } catch (InterruptedException exception) {
                        MyLogger.log(SeleniumService.class.getName(), Level.INFO, exception.toString());
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT);
                        message.setDescription(message.getDescription().replaceAll("%TIME%", object));
                        return message;
                    }
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME);
                    message.setDescription(message.getDescription().replaceAll("%TIME%", object));
                    return message;
                } else {
                    try {
                        WebDriverWait wait = new WebDriverWait(this.selenium.getDriver(), TIMEOUT_WEBELEMENT);
                        wait.until(ExpectedConditions.presenceOfElementLocated(this.getIdentifier(object)));
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", object));
                        return message;
                    } catch (NoSuchElementException exception) {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", object));
                        MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                        return message;
                    }
                }
            } else {
                try {
                    Thread.sleep(TIMEOUT_MILLIS);
                } catch (InterruptedException exception) {
                    MyLogger.log(SeleniumService.class.getName(), Level.INFO, exception.toString());
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
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
    }

    private MessageEvent doActionKeyPress(String html, String property) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(html) && !StringUtil.isNull(property)) {
                try {
                    WebElement element = this.getSeleniumElement(html, true);
                    element.sendKeys(Keys.valueOf(property));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    message.setDescription(message.getDescription().replaceAll("%DATA%", property));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS);
    }

    private MessageEvent doActionOpenURLWithBase(String url) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(url)) {
                this.selenium.getDriver().get("http://" + this.selenium.getIp() + url);
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_OPENURL);
                message.setDescription(message.getDescription().replaceAll("%URL%", url));
                return message;
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_OPENURL);
        message.setDescription(message.getDescription().replaceAll("%URL%", url));
        return message;
    }

    private MessageEvent doActionSelect(String html, String property) {
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
                    select = new Select(this.getSeleniumElement(html, true));
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
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
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        } catch (PatternSyntaxException e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_REGEX_INVALIDPATERN);
            message.setDescription(message.getDescription().replaceAll("%PATERN%", value));
            message.setDescription(message.getDescription().replaceAll("%ERROR%", e.getMessage()));
            return message;
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT);
    }

    private MessageEvent doActionUrlLogin() {
        MessageEvent message;
        String url = "http://" + this.selenium.getIp() + this.selenium.getLogin();
        try {
            this.selenium.getDriver().get(url);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_URLLOGIN);
            message.setDescription(message.getDescription().replaceAll("%URL%", url));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_URLLOGIN);
            message.setDescription(message.getDescription().replaceAll("%URL%", url) + " " + e.getMessage());
            return message;
        }
    }

    private MessageEvent doActionFocusToIframe(String object, String property) {
        MessageEvent message;

        try {
            if (!StringUtil.isNullOrEmpty(property)) {
                try {
                    this.selenium.getDriver().switchTo().frame(this.getSeleniumElement(property, false));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSTOIFRAME);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", property));
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_FOCUS_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", property));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                }
            } else {
                try {
                    this.selenium.getDriver().switchTo().frame(this.getSeleniumElement(object, false));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSTOIFRAME);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", object));
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_FOCUS_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", object));
                    MyLogger.log(SeleniumService.class.getName(), Level.ERROR, exception.toString());
                    return message;
                }
            }
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }

        return message;
    }

    private MessageEvent doActionFocusDefaultIframe() {
        MessageEvent message;

        try {
            this.selenium.getDriver().switchTo().defaultContent();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSDEFAULTIFRAME);
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }

        return message;
    }
}
