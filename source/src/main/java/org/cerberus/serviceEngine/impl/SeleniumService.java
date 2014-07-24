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
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.log4j.Level;
import org.cerberus.entity.Invariant;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.Parameter;
import org.cerberus.entity.Selenium;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactorySelenium;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.IParameterService;
import org.cerberus.serviceEngine.ISeleniumService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
@Service
public class SeleniumService implements ISeleniumService {

    private static final int TIMEOUT_MILLIS = 30000;
    private static final int TIMEOUT_WEBELEMENT = 300;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IFactorySelenium factorySelenium;
    @Autowired
    private IInvariantService invariantService;

    @Override
    public TestCaseExecution startSeleniumServer(TestCaseExecution tCExecution, String host, String port, String browser, String version, String platform, String ip, String login, int verbose, String country) throws CerberusException {

//        if (tCExecution.getSelenium()==null || !tCExecution.getSelenium().isStarted()) {
        /**
         * We activate Network Traffic for verbose 1 and 2.
         */
        boolean record = (verbose > 0);
        long defaultWait;
        try {
            Parameter param = parameterService.findParameterByKey("selenium_defaultWait", "");
            String to = tCExecution.getTimeout().equals("") ? param.getValue() : tCExecution.getTimeout();
            defaultWait = Long.parseLong(to);
        } catch (CerberusException ex) {
            MyLogger.log(Selenium.class.getName(), Level.WARN, "Parameter (selenium_defaultWait) not in Parameter table, default wait set to 90 seconds");
            defaultWait = 90;
        }

        Selenium selenium = factorySelenium.create(host, port, browser, version, platform, login, ip, null, defaultWait);
        tCExecution.setSelenium(selenium);
        try {

            if (this.invariantService.isInvariantExist("BROWSER", browser)) {
                startSeleniumBrowser(tCExecution, record, country, browser, version, platform);
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription(mes.getDescription().replaceAll("%MES%", "Browser " + browser + " is not supported."));
                throw new CerberusException(mes);
            }

            selenium.getDriver().manage().window().maximize();
            selenium.setStarted(true);
            tCExecution.setSelenium(selenium);
            return tCExecution;
        } catch (CerberusException ex) {
            throw new CerberusException(ex.getMessageError());
        }
//        } else {
//            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
//        }
    }

    private DesiredCapabilities setFirefoxProfile(String executionUUID, boolean record, String country) throws CerberusException {
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
            String url = cerberusUrl + "/SaveStatistic?logId=" + executionUUID;
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

        DesiredCapabilities dc = DesiredCapabilities.firefox();
        dc.setCapability(FirefoxDriver.PROFILE, profile);

        return dc;
    }

    @Override
    public boolean startSeleniumBrowser(TestCaseExecution tCExecution, boolean record, String country, String browser, String version, String platform) throws CerberusException {

        MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Starting " + browser);

        DesiredCapabilities capabilities = null;
        Selenium selenium = null;

        //TODO : take platform and version from servlet
        try {
            selenium = tCExecution.getSelenium();
            capabilities = setCapabilityBrowser(capabilities, browser, tCExecution.getExecutionUUID(), record, country);
            capabilities = setCapabilityPlatform(capabilities, platform);
            //capabilities = setCapabilityVersion(capabilities, version);

            MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Set Driver");
            WebDriver driver = new RemoteWebDriver(new URL("http://" + selenium.getHost() + ":" + selenium.getPort() + "/wd/hub"), capabilities);
            selenium.setDriver(driver);
            tCExecution = getIPOfNode(tCExecution);
            tCExecution.setSelenium(selenium);
        } catch (CerberusException exception) {
            MyLogger.log(Selenium.class.getName(), Level.ERROR, exception.toString());
            throw new CerberusException(exception.getMessageError());
        } catch (MalformedURLException exception) {
            MyLogger.log(Selenium.class.getName(), Level.ERROR, exception.toString());
            return false;
        } catch (UnreachableBrowserException exception) {
            MyLogger.log(Selenium.class.getName(), Level.ERROR, exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_COULDNOTCONNECT);
            mes.setDescription(mes.getDescription().replace("%SSIP%", selenium.getHost()));
            mes.setDescription(mes.getDescription().replace("%SSPORT%", selenium.getPort()));
            throw new CerberusException(mes);
        } catch (Exception exception) {
            MyLogger.log(Selenium.class.getName(), Level.ERROR, exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
            mes.setDescription(mes.getDescription().replace("%MES%", exception.toString()));
            throw new CerberusException(mes);
        }

        return true;
    }

    public DesiredCapabilities setCapabilityBrowser(DesiredCapabilities capabilities, String browser, String executionUUID, boolean record, String country) throws CerberusException {
        try {
            if (browser.equalsIgnoreCase("firefox")) {
                capabilities = this.setFirefoxProfile(executionUUID, record, country);
            } else if (browser.equalsIgnoreCase("IE")) {
                capabilities = DesiredCapabilities.internetExplorer();
            } else if (browser.equalsIgnoreCase("chrome")) {
                capabilities = DesiredCapabilities.chrome();
            } else if (browser.contains("android")) {
                capabilities = DesiredCapabilities.android();
            } else if (browser.contains("ipad")) {
                capabilities = DesiredCapabilities.ipad();
            } else if (browser.contains("iphone")) {
                capabilities = DesiredCapabilities.iphone();
            } else if (browser.contains("opera")) {
                capabilities = DesiredCapabilities.opera();
            } else if (browser.contains("safari")) {
                capabilities = DesiredCapabilities.safari();
            } else {
                MyLogger.log(Selenium.class.getName(), Level.WARN, "Not supported Browser : " + browser);
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription("Not supported Browser : " + browser);
                throw new CerberusException(mes);
            }
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
            throw new CerberusException(mes);
        }

        return capabilities;
    }

    public DesiredCapabilities setCapabilityVersion(DesiredCapabilities capabilities, String version) throws CerberusException {
        if (!version.equalsIgnoreCase("")) {
            capabilities.setCapability(CapabilityType.VERSION, version);
        }

        return capabilities;
    }

    public DesiredCapabilities setCapabilityPlatform(DesiredCapabilities capabilities, String platform) throws CerberusException {
        if (platform.equalsIgnoreCase("WINDOWS")) {
            capabilities.setPlatform(Platform.WINDOWS);
        } else if (platform.equalsIgnoreCase("LINUX")) {
            capabilities.setPlatform(Platform.LINUX);
        } else if (platform.equalsIgnoreCase("ANDROID")) {
            capabilities.setPlatform(Platform.ANDROID);
        } else if (platform.equalsIgnoreCase("MAC")) {
            capabilities.setPlatform(Platform.MAC);
        } else if (platform.equalsIgnoreCase("UNIX")) {
            capabilities.setPlatform(Platform.UNIX);
        } else if (platform.equalsIgnoreCase("VISTA")) {
            capabilities.setPlatform(Platform.VISTA);
        } else if (platform.equalsIgnoreCase("WIN8")) {
            capabilities.setPlatform(Platform.WIN8);
        } else if (platform.equalsIgnoreCase("XP")) {
            capabilities.setPlatform(Platform.XP);
        } else {
            capabilities.setPlatform(Platform.ANY);
        }

        return capabilities;
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

    private WebElement getSeleniumElement(Selenium selenium, String input, boolean visible, boolean clickable) {
        By locator = this.getIdentifier(input);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Waiting for Element : " + input);
        try {
            WebDriverWait wait = new WebDriverWait(selenium.getDriver(), selenium.getDefaultWait());
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
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Finding selenium Element : " + input);
        return selenium.getDriver().findElement(locator);
    }

    @Override
    public boolean stopSeleniumServer(Selenium selenium) {
        if (selenium.isStarted()) {
            try {
                // Wait 2 sec till HAR is exported
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SeleniumService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

            selenium.getDriver().quit();
            selenium.setStarted(false);
            return true;
        }

        return false;
    }

    @Override
    public String getValueFromHTMLVisible(Selenium selenium, String locator) {
        WebElement webElement = this.getSeleniumElement(selenium, locator, true, false);
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
    public String getValueFromHTML(Selenium selenium, String locator) {
        WebElement webElement = this.getSeleniumElement(selenium, locator, false, false);
        String result;

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
            result = (String) ((JavascriptExecutor) selenium.getDriver()).executeScript(script, webElement);
        }

        return result;
    }

    @Override
    public String getAlertText(Selenium selenium) {
        Alert alert = selenium.getDriver().switchTo().alert();
        if (alert != null) {
            return alert.getText();
        }

        return null;
    }

    @Override
    public String getValueFromJS(Selenium selenium, String script) {
        JavascriptExecutor js = (JavascriptExecutor) selenium.getDriver();
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
    public String getAttributeFromHtml(Selenium selenium, String locator, String attribute) {
        String result = null;
        try {
            WebElement webElement = this.getSeleniumElement(selenium, locator, true, false);
            result = webElement.getAttribute(attribute);
        } catch (WebDriverException exception) {
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
        }
        return result;
    }

    @Override
    public boolean isElementPresent(Selenium selenium, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(selenium, locator, false, false);
            return webElement != null;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public boolean isElementVisible(Selenium selenium, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(selenium, locator, true, false);
            return webElement != null && webElement.isDisplayed();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public boolean isElementNotVisible(Selenium selenium, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(selenium, locator, false, false);
            return webElement != null && !webElement.isDisplayed();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public String getPageSource(Selenium selenium) {
        return selenium.getDriver().getPageSource();
    }

    @Override
    public String getTitle(Selenium selenium) {
        return selenium.getDriver().getTitle();
    }

    /**
     * Return the current URL from Selenium.
     *
     * @return current URL without HTTP://IP:PORT/CONTEXTROOT/
     * @throws CerberusEventException Cannot find application host (from
     * Database) inside current URL (from Selenium)
     */
    @Override
    public String getCurrentUrl(Selenium selenium) throws CerberusEventException {
        /*
         * Example: URL (http://mypage/page/index.jsp), IP (mypage)
         * URL.split(IP, 2)
         * Pos | Description
         *  0  |    http://
         *  1  |    /page/index.jsp
         */
        String strings[] = selenium.getDriver().getCurrentUrl().split(selenium.getIp(), 2);
        if (strings.length < 2) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.CONTROL_FAILED_URL_NOT_MATCH_APPLICATION);
            msg.setDescription(msg.getDescription().replaceAll("%HOST%", selenium.getDriver().getCurrentUrl()));
            msg.setDescription(msg.getDescription().replaceAll("%URL%", selenium.getIp()));
            MyLogger.log(SeleniumService.class.getName(), Level.WARN, msg.toString());
            throw new CerberusEventException(msg);
        }
        return strings[1];
    }

    @Override
    public Capabilities getUsedCapabilities(Selenium selenium) {

        Capabilities caps = ((RemoteWebDriver) selenium.getDriver()).getCapabilities();
        return caps;
    }

    private static TestCaseExecution getIPOfNode(TestCaseExecution tCExecution) {
        try {
            Selenium selenium = tCExecution.getSelenium();
            HttpCommandExecutor ce = (HttpCommandExecutor) ((RemoteWebDriver) selenium.getDriver()).getCommandExecutor();
            SessionId sessionId = ((RemoteWebDriver) selenium.getDriver()).getSessionId();
            String hostName = ce.getAddressOfRemoteServer().getHost();
            int port = ce.getAddressOfRemoteServer().getPort();
            HttpHost host = new HttpHost(hostName, port);
            HttpClient client = HttpClientBuilder.create().build();
            URL sessionURL = new URL("http://" + selenium.getHost() + ":" + selenium.getPort() + "/grid/api/testsession?session=" + sessionId);
            BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST", sessionURL.toExternalForm());
            HttpResponse response = client.execute(host, r);
            if (!response.getStatusLine().toString().contains("403")){
            InputStream contents = response.getEntity().getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(contents, writer, "UTF8");
            JSONObject object = new JSONObject(writer.toString());
            URL myURL = new URL(object.getString("proxyId"));
            if ((myURL.getHost() != null) && (myURL.getPort() != -1)) {
                tCExecution.setIp(myURL.getHost());
                tCExecution.setPort(String.valueOf(myURL.getPort()));
            }
            }

        } catch (IOException ex) {
            Logger.getLogger(SeleniumService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(SeleniumService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return tCExecution;
    }

    @Override
    public void doScreenShot(Selenium selenium, String runId, String name) {
        try {
            WebDriver augmentedDriver = new Augmenter().augment(selenium.getDriver());
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
    public boolean isElementInElement(Selenium selenium, String element, String childElement) {
        By elementLocator = this.getIdentifier(element);
        By childElementLocator = this.getIdentifier(childElement);

        return (selenium.getDriver().findElement(elementLocator) != null
                && selenium.getDriver().findElement(elementLocator).findElement(childElementLocator) != null);
    }

    @Override
    public boolean isElementNotClickable(Selenium selenium, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(selenium, locator, true, true);
            return webElement == null;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public boolean isElementClickable(Selenium selenium, String locator) {
        try {
            WebElement webElement = this.getSeleniumElement(selenium, locator, true, true);
            return webElement != null;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    @Override
    public MessageEvent doSeleniumActionClick(Selenium selenium, String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
//                    Actions actions = new Actions(selenium.getDriver());
//                    actions.click(this.getSeleniumElement(selenium, string1, true, true));
//                    actions.build().perform();
                    this.getSeleniumElement(selenium, string1, true, true).click();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    this.getSeleniumElement(selenium, string1, true, true).click();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionMouseDown(Selenium selenium, String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
                    Actions actions = new Actions(selenium.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(selenium, string1, true, true));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEDOWN_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    Actions actions = new Actions(selenium.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(selenium, string1, true, true));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEDOWN_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionMouseUp(Selenium selenium, String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
                    Actions actions = new Actions(selenium.getDriver());
                    actions.release(this.getSeleniumElement(selenium, string1, true, true));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEUP);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEUP_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    Actions actions = new Actions(selenium.getDriver());
                    actions.release(this.getSeleniumElement(selenium, string1, true, true));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEUP);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEUP_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionSwitchToWindow(Selenium selenium, String string1, String string2) {
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
                    currentHandle = selenium.getDriver().getWindowHandle();
                } catch (NoSuchWindowException exception) {
                    // Add try catch to handle not exist anymore window (like when popup is closed).
                    currentHandle = null;
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Window is closed ? " + exception.toString());
                }

                try {
                    // Get serials handles list of all browser windows
                    Set<String> handles = selenium.getDriver().getWindowHandles();

                    // Loop into each of them
                    for (String windowHandle : handles) {
                        if (!windowHandle.equals(currentHandle)) {
                            selenium.getDriver().switchTo().window(windowHandle);
                            if (seleniumTestTitleOfWindow(selenium, selenium.getDriver().getTitle(), identifier, value)) {
                                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SWITCHTOWINDOW);
                                message.setDescription(message.getDescription().replaceAll("%WINDOW%", windowTitle));
                                return message;
                            }
                        }
                        MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "windowHandle=" + windowHandle);
                    }
                } catch (NoSuchElementException exception) {
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionManageDialog(Selenium selenium, String object, String property) {
        try {
            String value = object;
            if (value == null || value.trim().length() == 0) {
                value = property;
            }
            if ("ok".equalsIgnoreCase(value)) {
                // Accept javascript popup dialog.
                selenium.getDriver().switchTo().alert().accept();
                selenium.getDriver().switchTo().defaultContent();
                return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLOSE_ALERT);
            } else if ("cancel".equalsIgnoreCase(value)) {
                // Dismiss javascript popup dialog.
                selenium.getDriver().switchTo().alert().dismiss();
                selenium.getDriver().switchTo().defaultContent();
                return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLOSE_ALERT);
            }

        } catch (NoSuchWindowException exception) {
            // Add try catch to handle not exist anymore alert popup (like when popup is closed).
            MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Alert popup is closed ? " + exception.toString());
        } catch (WebDriverException exception) {
            MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, "Alert popup is closed ? " + exception.toString());
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLOSE_ALERT);
    }

    private boolean seleniumTestTitleOfWindow(Selenium selenium, String title, String identifier, String value) {
        if (value != null && title != null) {
            if ("title".equals(identifier) && value.equals(title)) {
                return true;
            }

            if ("regexTitle".equals(identifier)) {
                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher(selenium.getDriver().getTitle());

                return matcher.find();
            }
        }
        return false;
    }

    @Override
    public MessageEvent doSeleniumActionClickWait(Selenium selenium, String actionObject, String actionProperty) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                try {
                    this.getSeleniumElement(selenium, actionObject, true, true).click();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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
                    this.getSeleniumElement(selenium, actionObject, true, true).click();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICKANDNOWAIT);
                message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                return message;
            } else if (!StringUtil.isNull(actionProperty) && StringUtil.isNull(actionObject)) {
                try {
                    this.getSeleniumElement(selenium, actionProperty, true, true).click();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionProperty));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionDoubleClick(Selenium selenium, String html, String property) {
        MessageEvent message;
        try {
            Actions actions = new Actions(selenium.getDriver());
            if (!StringUtil.isNull(property)) {
                try {
                    actions.doubleClick(this.getSeleniumElement(selenium, property, true, true));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DOUBLECLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(html)) {
                try {
                    actions.doubleClick(this.getSeleniumElement(selenium, html, true, true));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DOUBLECLICK);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionType(Selenium selenium, String html, String property, String propertyName) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(html)) {
                try {
                    WebElement webElement = this.getSeleniumElement(selenium, html, true, true);
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
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionMouseOver(Selenium selenium, String html, String property) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(html)) {
                try {
                    Actions actions = new Actions(selenium.getDriver());
                    WebElement menuHoverLink = this.getSeleniumElement(selenium, html, true, true);
                    actions.moveToElement(menuHoverLink);
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVER);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(property)) {
                try {
                    Actions actions = new Actions(selenium.getDriver());
                    WebElement menuHoverLink = this.getSeleniumElement(selenium, property, true, true);
                    actions.moveToElement(menuHoverLink);
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVER);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionMouseOverAndWait(Selenium selenium, String actionObject, String actionProperty) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                if (StringUtil.isNumeric(actionProperty)) {
                    try {
                        Actions actions = new Actions(selenium.getDriver());
                        WebElement menuHoverLink = this.getSeleniumElement(selenium, actionObject, true, true);
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
                            MyLogger.log(SeleniumService.class.getName(), Level.INFO, e.toString());
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVERANDWAIT);
                            message.setDescription(message.getDescription().replaceAll("%ELEMENT1%", actionObject));
                            message.setDescription(message.getDescription().replaceAll("%TIME%", Integer.toString(sleep)));
                            return message;
                        }
                    } catch (NoSuchElementException exception) {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                        MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
                        return message;
                    }
                }
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVERANDWAIT_NO_NUMERIC);
                message.setDescription(message.getDescription().replaceAll("%TIME%", actionProperty));
                return message;
            } else if (StringUtil.isNull(actionProperty) && !StringUtil.isNull(actionObject)) {
                try {
                    Actions actions = new Actions(selenium.getDriver());
                    WebElement menuHoverLink = this.getSeleniumElement(selenium, actionObject, true, true);
                    actions.moveToElement(menuHoverLink);
                    actions.build().perform();
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", actionObject));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionWait(Selenium selenium, String object, String property) {
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
                        WebDriverWait wait = new WebDriverWait(selenium.getDriver(), TIMEOUT_WEBELEMENT);
                        wait.until(ExpectedConditions.presenceOfElementLocated(this.getIdentifier(property)));
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                        return message;
                    } catch (NoSuchElementException exception) {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", property));
                        MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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
                        WebDriverWait wait = new WebDriverWait(selenium.getDriver(), TIMEOUT_WEBELEMENT);
                        wait.until(ExpectedConditions.presenceOfElementLocated(this.getIdentifier(object)));
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", object));
                        return message;
                    } catch (NoSuchElementException exception) {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
                        message.setDescription(message.getDescription().replaceAll("%ELEMENT%", object));
                        MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionKeyPress(Selenium selenium, String html, String property) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(html) && !StringUtil.isNull(property)) {
                try {
                    WebElement element = this.getSeleniumElement(selenium, html, true, true);
                    element.sendKeys(Keys.valueOf(property));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    message.setDescription(message.getDescription().replaceAll("%DATA%", property));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionOpenURL(Selenium selenium, String value, String property, boolean withBase) {
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
                    selenium.getDriver().get("http://" + selenium.getIp() + url);
                } else {
                    selenium.getDriver().get(url);
                }
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

    @Override
    public MessageEvent doSeleniumActionSelect(Selenium selenium, String html, String property) {
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
                    select = new Select(this.getSeleniumElement(selenium, html, true, true));
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", html));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionUrlLogin(Selenium selenium) {
        MessageEvent message;
        String url = "http://" + selenium.getIp() + selenium.getLogin();
        try {
            selenium.getDriver().get(url);
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
    public MessageEvent doSeleniumActionFocusToIframe(Selenium selenium, String object, String property) {
        MessageEvent message;

        try {
            if (!StringUtil.isNullOrEmpty(property)) {
                try {
                    selenium.getDriver().switchTo().frame(this.getSeleniumElement(selenium, property, false, false));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSTOIFRAME);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", property));
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_FOCUS_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", property));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
                }
            } else {
                try {
                    selenium.getDriver().switchTo().frame(this.getSeleniumElement(selenium, object, false, false));
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSTOIFRAME);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", object));
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_FOCUS_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%IFRAME%", object));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public MessageEvent doSeleniumActionFocusDefaultIframe(Selenium selenium) {
        MessageEvent message;

        try {
            selenium.getDriver().switchTo().defaultContent();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOCUSDEFAULTIFRAME);
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(SeleniumService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }

        return message;
    }

    @Override
    public MessageEvent doSeleniumActionMouseDownMouseUp(Selenium selenium, String string1, String string2) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(string1)) {
                try {
                    Actions actions = new Actions(selenium.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(selenium, string1, true, false));
                    actions.build().perform();
                    actions.release(this.getSeleniumElement(selenium, string1, true, false));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
                    return message;
                }
            } else if (!StringUtil.isNull(string2)) {
                try {
                    Actions actions = new Actions(selenium.getDriver());
                    actions.clickAndHold(this.getSeleniumElement(selenium, string1, true, false));
                    actions.build().perform();
                    actions.release(this.getSeleniumElement(selenium, string1, true, false));
                    actions.build().perform();
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEDOWN);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string1));
                    return message;
                } catch (NoSuchElementException exception) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
                    message.setDescription(message.getDescription().replaceAll("%ELEMENT%", string2));
                    MyLogger.log(SeleniumService.class.getName(), Level.DEBUG, exception.toString());
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

    @Override
    public String getFromCookie(Selenium selenium, String cookieName, String cookieParameter) {
        Cookie cookie = selenium.getDriver().manage().getCookieNamed(cookieName);
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
    public List<String> getSeleniumLog(Selenium selenium) {
        List<String> result = new ArrayList();
        Logs logs = selenium.getDriver().manage().logs();
        LogEntries logEntries = logs.get(LogType.DRIVER);

        result.add("********************DRIVER********************\n");
        for (LogEntry logEntry : logEntries) {
            result.add(new Date(logEntry.getTimestamp()) + " : " + logEntry.getLevel() + " : " + logEntry.getMessage() + "\n");
        }

        result.add("********************BROWSER********************\n");
        logEntries = logs.get(LogType.BROWSER);
        for (LogEntry logEntry : logEntries) {
            result.add(new Date(logEntry.getTimestamp()) + " : " + logEntry.getLevel() + " : " + logEntry.getMessage() + "\n");
        }

        result.add("********************CLIENT********************\n");
        logEntries = logs.get(LogType.CLIENT);
        for (LogEntry logEntry : logEntries) {
            result.add(new Date(logEntry.getTimestamp()) + " : " + logEntry.getLevel() + " : " + logEntry.getMessage() + "\n");
        }

        result.add("********************SERVER********************\n");
        logEntries = logs.get(LogType.SERVER);
        for (LogEntry logEntry : logEntries) {
            result.add(new Date(logEntry.getTimestamp()) + " : " + logEntry.getLevel() + " : " + logEntry.getMessage() + "\n");
        }

        return result;
    }

}
