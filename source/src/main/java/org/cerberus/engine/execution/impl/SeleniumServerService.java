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
package org.cerberus.engine.execution.impl;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.engine.entity.Session;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.engine.execution.ISeleniumServerService;
import org.cerberus.service.proxy.IProxyService;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.remote.http.HttpClient.Factory;
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
    @Autowired
    IProxyService proxyService;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SeleniumServerService.class);
    /**
     * Proxy default config. (Should never be used as default config is inserted
     * into database)
     */
    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

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
            Integer cerberus_selenium_pageLoadTimeout, cerberus_selenium_implicitlyWait, cerberus_selenium_setScriptTimeout, cerberus_selenium_wait_element, cerberus_appium_wait_element, cerberus_selenium_action_click_timeout;

            if (!tCExecution.getTimeout().isEmpty()) {
                cerberus_selenium_wait_element = Integer.valueOf(tCExecution.getTimeout());
                cerberus_appium_wait_element = Integer.valueOf(tCExecution.getTimeout());
            } else {
                cerberus_selenium_wait_element = parameterService.getParameterIntegerByKey("cerberus_selenium_wait_element", system, 90000);
                cerberus_appium_wait_element = parameterService.getParameterIntegerByKey("cerberus_appium_wait_element", system, 90000);
            }

            cerberus_selenium_pageLoadTimeout = parameterService.getParameterIntegerByKey("cerberus_selenium_pageLoadTimeout", system, 90000);
            cerberus_selenium_implicitlyWait = parameterService.getParameterIntegerByKey("cerberus_selenium_implicitlyWait", system, 0);
            cerberus_selenium_setScriptTimeout = parameterService.getParameterIntegerByKey("cerberus_selenium_setScriptTimeout", system, 90000);
            cerberus_selenium_action_click_timeout = parameterService.getParameterIntegerByKey("cerberus_selenium_action_click_timeout", system, 90000);

            LOG.debug(logPrefix + "TimeOut defined on session : " + cerberus_selenium_wait_element);

            Session session = new Session();
            session.setCerberus_selenium_implicitlyWait(cerberus_selenium_implicitlyWait);
            session.setCerberus_selenium_pageLoadTimeout(cerberus_selenium_pageLoadTimeout);
            session.setCerberus_selenium_setScriptTimeout(cerberus_selenium_setScriptTimeout);
            session.setCerberus_selenium_wait_element(cerberus_selenium_wait_element);
            session.setCerberus_appium_wait_element(cerberus_appium_wait_element);
            session.setCerberus_selenium_action_click_timeout(cerberus_selenium_action_click_timeout);
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
             * SetUp Proxy
             */
            String hubUrl = StringUtil.cleanHostURL(tCExecution.getSession().getHost()) + ":" + tCExecution.getSession().getPort() + "/wd/hub";
            URL url = new URL(hubUrl);
            HttpCommandExecutor executor = null;

            boolean isProxy = proxyService.useProxy(hubUrl, system);
            if (isProxy) {
                String proxyHost = parameterService.getParameterStringByKey("cerberus_proxy_host", system, DEFAULT_PROXY_HOST);
                int proxyPort = parameterService.getParameterIntegerByKey("cerberus_proxy_port", system, DEFAULT_PROXY_PORT);

                HttpClientBuilder builder = HttpClientBuilder.create();
                HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                builder.setProxy(proxy);

                if (parameterService.getParameterBooleanByKey("cerberus_proxyauthentification_active", system, DEFAULT_PROXYAUTHENT_ACTIVATE)) {

                    String proxyUser = parameterService.getParameterStringByKey("cerberus_proxyauthentification_user", system, DEFAULT_PROXYAUTHENT_USER);
                    String proxyPassword = parameterService.getParameterStringByKey("cerberus_proxyauthentification_password", system, DEFAULT_PROXYAUTHENT_PASSWORD);

                    CredentialsProvider credsProvider = new BasicCredentialsProvider();

                    credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPassword));

                    if (url.getUserInfo() != null && !url.getUserInfo().isEmpty()) {
                        credsProvider.setCredentials(new AuthScope(url.getHost(), (url.getPort() > 0 ? url.getPort() : url.getDefaultPort())), new UsernamePasswordCredentials(url.getUserInfo()));
                    }

                    builder.setDefaultCredentialsProvider(credsProvider);
                }

                Factory factory = new MyHttpClientFactory(builder);
                executor = new HttpCommandExecutor(new HashMap<String, CommandInfo>(), url, factory);

            }

            /**
             * SetUp Driver
             */
            LOG.debug(logPrefix + "Set Driver");
            WebDriver driver = null;
            AppiumDriver appiumDriver = null;
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (caps.getPlatform().is(Platform.ANDROID)) {
                    if (executor == null) {
                        appiumDriver = new AndroidDriver(url, caps);
                    } else {
                        appiumDriver = new AndroidDriver(executor, caps);
                    }
                    driver = (WebDriver) appiumDriver;
                } else if (caps.getPlatform().is(Platform.MAC)) {
                    if (executor == null) {
                        appiumDriver = new IOSDriver(url, caps);
                    } else {
                        appiumDriver = new IOSDriver(executor, caps);
                    }
                    driver = (WebDriver) appiumDriver;
                } else // Any Other
                {
                    if (executor == null) {
                        driver = new RemoteWebDriver(url, caps);
                    } else {
                        driver = new RemoteWebDriver(executor, caps);
                    }
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                if (executor == null) {
                    appiumDriver = new AndroidDriver(url, caps);
                } else {
                    appiumDriver = new AndroidDriver(executor, caps);
                }
                driver = (WebDriver) appiumDriver;
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                if (executor == null) {
                    appiumDriver = new IOSDriver(url, caps);
                } else {
                    appiumDriver = new IOSDriver(executor, caps);
                }
                driver = (WebDriver) appiumDriver;
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                /**
                 * Check sikuli extension is reachable
                 */
                if (!sikuliService.isSikuliServerReachable(session)) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SIKULI_COULDNOTCONNECT);
                    mes.setDescription(mes.getDescription().replace("%SSIP%", tCExecution.getSeleniumIP()));
                    mes.setDescription(mes.getDescription().replace("%SSPORT%", tCExecution.getSeleniumPort()));
                    throw new CerberusException(mes);
                }
                /**
                 * If CountryEnvParameter IP is set, open the App
                 */
                if (!tCExecution.getCountryEnvironmentParameters().getIp().isEmpty()) {
                    sikuliService.doSikuliAction(session, "openApp", null, tCExecution.getCountryEnvironmentParameters().getIp());
                }
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
             * remote Server. Maximize does not work for chrome browser
             */
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    && !caps.getPlatform().equals(Platform.ANDROID)) {
                if (!caps.getBrowserName().equals(BrowserType.CHROME)) {
                    driver.manage().window().maximize();
                }
                getIPOfNode(tCExecution);

                /**
                 * If screenSize is defined, set the size of the screen.
                 */
                String targetScreensize = getScreenSizeToUse(tCExecution.getTestCaseObj().getScreenSize(), tCExecution.getScreenSize());
                LOG.debug("Selenium resolution : " + targetScreensize);
                if ((!StringUtil.isNullOrEmpty(targetScreensize)) && targetScreensize.contains("*")) {
                    Integer screenWidth = Integer.valueOf(targetScreensize.split("\\*")[0]);
                    Integer screenLength = Integer.valueOf(targetScreensize.split("\\*")[1]);
                    setScreenSize(driver, screenWidth, screenLength);
                    LOG.debug("Selenium resolution Activated : " + screenWidth + "*" + screenLength);
                }
                tCExecution.setScreenSize(getScreenSize(driver));

                String userAgent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");
                tCExecution.setUserAgent(userAgent);

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
            mes.setDescription(mes.getDescription().replace("%ERROR%", exception.toString()));
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
     *
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
         * Loop on RobotCapabilities to feed DesiredCapabilities Capability must
         * be String, Integer or Boolean
         */
        List<RobotCapability> additionalCapabilities = tCExecution.getCapabilities();
        if (additionalCapabilities != null) {
            for (RobotCapability additionalCapability : additionalCapabilities) {
                if (StringUtil.isBoolean(additionalCapability.getValue())) {
                    caps.setCapability(additionalCapability.getCapability(), StringUtil.parseBoolean(additionalCapability.getValue()));
                } else if (StringUtil.isInteger(additionalCapability.getValue())) {
                    caps.setCapability(additionalCapability.getCapability(), Integer.valueOf(additionalCapability.getValue()));
                } else {
                    caps.setCapability(additionalCapability.getCapability(), additionalCapability.getValue());
                }
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

        return caps;
    }

    /**
     * Instanciate DesiredCapabilities regarding the browser
     *
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
                profile.setPreference("app.update.enabled", false);
                try {
                    Invariant invariant = invariantService.convert(invariantService.readByKey("COUNTRY", tCExecution.getCountry()));
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

                // Set UserAgent if testCaseUserAgent or robotUserAgent is defined
                String usedUserAgent = getUserAgentToUse(tCExecution.getTestCaseObj().getUserAgent(), tCExecution.getUserAgent());
                if (!StringUtil.isNullOrEmpty(usedUserAgent)) {
                    profile.setPreference("general.useragent.override", usedUserAgent);
                }
                capabilities.setCapability(FirefoxDriver.PROFILE, profile);

            } else if (browser.equalsIgnoreCase("IE")) {
                capabilities = DesiredCapabilities.internetExplorer();

            } else if (browser.equalsIgnoreCase("chrome")) {
                capabilities = DesiredCapabilities.chrome();
                /**
                 * Add custom capabilities
                 */
                ChromeOptions options = new ChromeOptions();
                // Maximize windows for chrome browser
                options.addArguments("--start-fullscreen");
                // Set UserAgent if necessary
                String usedUserAgent = getUserAgentToUse(tCExecution.getTestCaseObj().getUserAgent(), tCExecution.getUserAgent());
                if (!StringUtil.isNullOrEmpty(usedUserAgent)) {
                    options.addArguments("--user-agent=" + usedUserAgent);
                }
                capabilities.setCapability(ChromeOptions.CAPABILITY, options);

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

    /**
     * This method determine which user agent to use.
     *
     * @param userAgentTestCase
     * @param userAgentRobot
     * @return String containing the userAgent to use
     */
    private String getUserAgentToUse(String userAgentTestCase, String userAgentRobot) {
        if (StringUtil.isNullOrEmpty(userAgentRobot) && StringUtil.isNullOrEmpty(userAgentTestCase)) {
            return "";
        } else {
            return StringUtil.isNullOrEmpty(userAgentTestCase) ? userAgentRobot : userAgentTestCase;
        }
    }

    /**
     * This method determine which screenSize to use.
     *
     * @param screenSizeTestCase
     * @param screenSizeRobot
     * @return String containing the userAgent to use
     */
    private String getScreenSizeToUse(String screenSizeTestCase, String screenSizeRobot) {
        if (StringUtil.isNullOrEmpty(screenSizeRobot) && StringUtil.isNullOrEmpty(screenSizeTestCase)) {
            return "";
        } else {
            return StringUtil.isNullOrEmpty(screenSizeTestCase) ? screenSizeRobot : screenSizeTestCase;
        }
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
            if (!response.getStatusLine().toString().contains("403")
                    && !response.getEntity().getContentType().getValue().contains("text/html")) {
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
        return driver.manage().window().getSize().width + "*" + driver.manage().window().getSize().height;
    }

}
