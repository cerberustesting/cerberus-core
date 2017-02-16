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
package org.cerberus.engine.execution.impl;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.engine.entity.Session;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.engine.execution.ISeleniumServerService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class SeleniumServerService implements ISeleniumServerService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IInvariantService invariantService;
    @Autowired
    private ISikuliService sikuliService;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SeleniumServerService.class);

    @Override
    public void startServer(TestCaseExecution tCExecution) throws CerberusException {
        //message used for log purposes 
        String logPrefix = "[" + tCExecution.getTest() + " - " + tCExecution.getTestCase() + "] ";

        try {

            LOG.info(logPrefix + "Start Selenium Server");

            /**
             * Set Session
             */
            LOG.debug(logPrefix + "Setting the session.");
            String system = tCExecution.getApplicationObj().getSystem();

            /**
             * Get the parameters that will be used to set the servers
             * (selenium/appium) If timeout has been defined at the execution
             * level, set the selenium & appium wait element with this value,
             * else, take the one from parameter
             */
            Integer cerberus_selenium_pageLoadTimeout, cerberus_selenium_implicitlyWait, cerberus_selenium_setScriptTimeout, cerberus_selenium_wait_element, cerberus_appium_wait_element;

            if (!tCExecution.getTimeout().isEmpty()) {
                cerberus_selenium_wait_element = Integer.valueOf(tCExecution.getTimeout());
                cerberus_appium_wait_element = Integer.valueOf(tCExecution.getTimeout());
            } else {
                cerberus_selenium_wait_element = this.getTimeoutSetInParameterTable(system, "cerberus_selenium_wait_element", 90000, logPrefix);
                cerberus_appium_wait_element = this.getTimeoutSetInParameterTable(system, "cerberus_appium_wait_element", 90000, logPrefix);;
            }
            cerberus_selenium_pageLoadTimeout = this.getTimeoutSetInParameterTable(system, "cerberus_selenium_pageLoadTimeout", 90000, logPrefix);
            cerberus_selenium_implicitlyWait = this.getTimeoutSetInParameterTable(system, "cerberus_selenium_implicitlyWait", 0, logPrefix);
            cerberus_selenium_setScriptTimeout = this.getTimeoutSetInParameterTable(system, "cerberus_selenium_setScriptTimeout", 90000, logPrefix);

            LOG.debug(logPrefix + "TimeOut defined on session : " + cerberus_selenium_wait_element);

            Session session = new Session();
            session.setCerberus_selenium_implicitlyWait(cerberus_selenium_implicitlyWait);
            session.setCerberus_selenium_pageLoadTimeout(cerberus_selenium_pageLoadTimeout);
            session.setCerberus_selenium_setScriptTimeout(cerberus_selenium_setScriptTimeout);
            session.setCerberus_selenium_wait_element(cerberus_selenium_wait_element);
            session.setCerberus_appium_wait_element(cerberus_appium_wait_element);
            session.setHost(tCExecution.getSeleniumIP());
            session.setPort(tCExecution.getPort());
            tCExecution.setSession(session);
            LOG.debug(logPrefix + "Session is set.");

            /**
             * SetUp Capabilities
             */
            LOG.debug(logPrefix + "Set Capabilities");
            DesiredCapabilities caps = this.setCapabilities(tCExecution);
            session.setDesiredCapabilities(caps);
            LOG.debug(logPrefix + "Set Capabilities - retreived");

            /**
             * SetUp Driver
             */
            LOG.debug(logPrefix + "Set Driver");
            WebDriver driver = null;
            AppiumDriver appiumDriver = null;
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (caps.getPlatform().is(Platform.ANDROID)) {
                    appiumDriver = new AndroidDriver(new URL("http://" + tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort() + "/wd/hub"), caps);
                    driver = (WebDriver) appiumDriver;
                } else if (caps.getPlatform().is(Platform.MAC)) {
                    appiumDriver = new IOSDriver(new URL("http://" + tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort() + "/wd/hub"), caps);
                    driver = (WebDriver) appiumDriver;
                } else {
                    driver = new RemoteWebDriver(new URL("http://" + tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort() + "/wd/hub"), caps);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                appiumDriver = new AndroidDriver(new URL("http://" + tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort() + "/wd/hub"), caps);
                driver = (WebDriver) appiumDriver;
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                appiumDriver = new IOSDriver(new URL("http://" + tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort() + "/wd/hub"), caps);
                driver = (WebDriver) appiumDriver;
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                sikuliService.doSikuliAction(session, "openApp", null, tCExecution.getCountryEnvironmentParameters().getIp());
            }

            /**
             * Defining the timeout at the driver level. Only in case of not
             * Appium Driver (see
             * https://github.com/vertigo17/Cerberus/issues/754)
             */
            if (driver != null && appiumDriver == null) {
                driver.manage().timeouts().pageLoadTimeout(cerberus_selenium_pageLoadTimeout, TimeUnit.MILLISECONDS);
                driver.manage().timeouts().implicitlyWait(cerberus_selenium_implicitlyWait, TimeUnit.MILLISECONDS);
                driver.manage().timeouts().setScriptTimeout(cerberus_selenium_setScriptTimeout, TimeUnit.MILLISECONDS);
            }
            tCExecution.getSession().setDriver(driver);
            tCExecution.getSession().setAppiumDriver(appiumDriver);

            /**
             * If Gui application, maximize window Get IP of Node in case of
             * remote Server
             */
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    && !caps.getPlatform().equals(Platform.ANDROID)) {
                driver.manage().window().maximize();
                getIPOfNode(tCExecution);

                /**
                 * If screenSize is defined, set the size of the screen.
                 */
                if (!tCExecution.getScreenSize().equals("")) {
                    Integer screenWidth = Integer.valueOf(tCExecution.getScreenSize().split("\\*")[0]);
                    Integer screenLength = Integer.valueOf(tCExecution.getScreenSize().split("\\*")[1]);
                    setScreenSize(driver, screenWidth, screenLength);
                }
                tCExecution.setScreenSize(getScreenSize(driver));
            }
            tCExecution.getSession().setStarted(true);

        } catch (CerberusException exception) {
            LOG.error(logPrefix + exception.toString());
            throw new CerberusException(exception.getMessageError());
        } catch (MalformedURLException exception) {
            LOG.error(logPrefix + exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_URL_MALFORMED);
            mes.setDescription(mes.getDescription().replace("%URL%", tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort()));
            throw new CerberusException(mes);
        } catch (UnreachableBrowserException exception) {
            LOG.error(logPrefix + exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_COULDNOTCONNECT);
            mes.setDescription(mes.getDescription().replace("%SSIP%", tCExecution.getSeleniumIP()));
            mes.setDescription(mes.getDescription().replace("%SSPORT%", tCExecution.getSeleniumPort()));
            throw new CerberusException(mes);
        } catch (Exception exception) {
            LOG.error(logPrefix + exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
            mes.setDescription(mes.getDescription().replace("%MES%", exception.toString()));
            throw new CerberusException(mes);
        }
    }

    /**
     * Set DesiredCapabilities
     * @param tCExecution
     * @return
     * @throws CerberusException
     */
    private DesiredCapabilities setCapabilities(TestCaseExecution tCExecution) throws CerberusException {
        /**
         * Instanciate DesiredCapabilities
         */
        DesiredCapabilities caps = new DesiredCapabilities();
        if (!StringUtil.isNullOrEmpty(tCExecution.getBrowser())) {
            caps = this.setCapabilityBrowser(caps, tCExecution.getBrowser(), tCExecution);
        }
        /**
         * Feed DesiredCapabilities with values get from Robot
         */
        if (!StringUtil.isNullOrEmpty(tCExecution.getPlatform())) {
            caps.setCapability("platform", tCExecution.getPlatform());
        }
        if (!StringUtil.isNullOrEmpty(tCExecution.getVersion())) {
            caps.setCapability("version", tCExecution.getVersion());
        }

        /**
         * Loop on RobotCapabilities to feed DesiredCapabilities
         */
        List<RobotCapability> additionalCapabilities = tCExecution.getCapabilities();
        if (additionalCapabilities != null) {
            for (RobotCapability additionalCapability : additionalCapabilities) {
                caps.setCapability(additionalCapability.getCapability(), additionalCapability.getValue());
            }
        }

        /**
         * if application is a mobile one, then set the "app" capability to the
         * application binary path
         */
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
            // Set the app capability with the application path
            if (tCExecution.isManualURL()) {
                caps.setCapability("app", tCExecution.getMyHost());
            } else {
                caps.setCapability("app", tCExecution.getCountryEnvironmentParameters().getIp());
            }
        }

        /**
         * Add custom capabilities
         */
        // Maximize windows for chrome browser
        if ("chrome".equals(caps.getBrowserName())) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--kiosk");
            caps.setCapability(ChromeOptions.CAPABILITY, options);
        }

        return caps;
    }

    /**
     * Instanciate DesiredCapabilities regarding the browser
     * @param capabilities
     * @param browser
     * @param tCExecution
     * @return
     * @throws CerberusException
     */
    private DesiredCapabilities setCapabilityBrowser(DesiredCapabilities capabilities, String browser, TestCaseExecution tCExecution) throws CerberusException {
        try {
            if (browser.equalsIgnoreCase("firefox")) {
                capabilities = DesiredCapabilities.firefox();
                FirefoxProfile profile = new FirefoxProfile();
                profile.setAcceptUntrustedCertificates(true);
                profile.setAssumeUntrustedCertificateIssuer(true);
                profile.setPreference("app.update.enabled", false);
                profile.setEnableNativeEvents(true);
                try {
                    Invariant invariant = this.invariantService.findInvariantByIdValue("COUNTRY", tCExecution.getCountry());
                    if (invariant.getGp2() == null) {
                        LOG.warn("Country selected (" + tCExecution.getCountry() + ") has no value of GP2 in Invariant table, default language set to English (en)");
                        profile.setPreference("intl.accept_languages", "en");
                    } else {
                        profile.setPreference("intl.accept_languages", invariant.getGp2());
                    }
                } catch (CerberusException ex) {
                    LOG.warn("Country selected (" + tCExecution.getCountry() + ") not in Invariant table, default language set to English (en)");
                    profile.setPreference("intl.accept_languages", "en");
                }

                capabilities.setCapability("acceptInsecureCerts", true);
                capabilities.setCapability("acceptSslCerts", true);
                //capabilities.setCapability("marionette", true);
                capabilities.setCapability(FirefoxDriver.PROFILE, profile);
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
                LOG.warn("Not supported Browser : " + browser);
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
                mes.setDescription(mes.getDescription().replace("%MES%", "Browser '" + browser + "' is not supported"));
                mes.setDescription("Not supported Browser : " + browser);
                throw new CerberusException(mes);
            }
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
            mes.setDescription(mes.getDescription().replace("%MES%", "Failed to set capability on the browser '" + browser + "' due to " + ex.getMessageError().getDescription()));
            throw new CerberusException(mes);
        }
        return capabilities;
    }

    @Override
    public boolean stopServer(Session session) {
        if (session.isStarted()) {
            try {
                // Wait 2 sec till HAR is exported
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
            }
            LOG.info("Stop execution session");
            session.quit();
            return true;
        }
        return false;
    }

    private static void getIPOfNode(TestCaseExecution tCExecution) {
        try {
            Session session = tCExecution.getSession();
            HttpCommandExecutor ce = (HttpCommandExecutor) ((RemoteWebDriver) session.getDriver()).getCommandExecutor();
            SessionId sessionId = ((RemoteWebDriver) session.getDriver()).getSessionId();
            String hostName = ce.getAddressOfRemoteServer().getHost();
            int port = ce.getAddressOfRemoteServer().getPort();
            HttpHost host = new HttpHost(hostName, port);
            HttpClient client = HttpClientBuilder.create().build();
            URL sessionURL = new URL("http://" + session.getHost() + ":" + session.getPort() + "/grid/api/testsession?session=" + sessionId);
            BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST", sessionURL.toExternalForm());
            HttpResponse response = client.execute(host, r);
            if (!response.getStatusLine().toString().contains("403")) {
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
            LOG.error(ex.toString());
        } catch (JSONException ex) {
            LOG.error(ex.toString());
        }
    }

    @Override
    public Capabilities getUsedCapabilities(Session session) {
        Capabilities caps = ((RemoteWebDriver) session.getDriver()).getCapabilities();
        return caps;
    }

    private void setScreenSize(WebDriver driver, Integer width, Integer length) {
        driver.manage().window().setPosition(new Point(0, 0));
        driver.manage().window().setSize(new Dimension(width, length));
    }

    private String getScreenSize(WebDriver driver) {
        return driver.manage().window().getSize().toString();
    }

    private Integer getTimeoutSetInParameterTable(String system, String parameter, Integer defaultWait, String logPrefix) {
        try {
            AnswerItem timeoutParameter = parameterService.readWithSystem1ByKey("", parameter, system);
            if (timeoutParameter != null && timeoutParameter.isCodeStringEquals(MessageEventEnum.DATA_OPERATION_OK.getCodeString())) {
                if (((Parameter) timeoutParameter.getItem()).getSystem1value().isEmpty()) {
                    return Integer.valueOf(((Parameter) timeoutParameter.getItem()).getValue());
                } else {
                    return Integer.valueOf(((Parameter) timeoutParameter.getItem()).getSystem1value());
                }
            } else {
                LOG.warn(logPrefix + "Parameter (" + parameter + ") not set in Parameter table, default value set to " + defaultWait + " milliseconds. ");
            }
        } catch (NumberFormatException ex) {
            LOG.warn(logPrefix + "Parameter (" + parameter + ") must be an integer, default value set to " + defaultWait + " milliseconds. " + ex.toString());
        }
        return defaultWait;
    }
}
