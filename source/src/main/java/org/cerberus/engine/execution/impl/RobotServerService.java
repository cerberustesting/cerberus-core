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
import io.appium.java_client.LocksDevice;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.factory.IFactoryRobotCapability;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionHttpStatService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.entity.Session;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.execution.IRobotServerService;
import org.cerberus.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.har.IHarService;
import org.cerberus.service.proxy.IProxyService;
import org.cerberus.service.rest.IRestService;
import org.cerberus.service.robotproviders.ILambdaTestService;
import org.cerberus.service.robotextension.ISikuliService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.remote.http.HttpClient.Factory;
import org.openqa.selenium.remote.internal.OkHttpClient;
import org.openqa.selenium.safari.SafariOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.cerberus.service.robotproxy.IRobotProxyService;

/**
 * @author bcivel
 */
@Service
public class RobotServerService implements IRobotServerService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IInvariantService invariantService;
    @Autowired
    private ISikuliService sikuliService;
    @Autowired
    IProxyService proxyService;
    @Autowired
    ITestCaseExecutionHttpStatService testCaseExecutionHttpStatService;
    @Autowired
    private IExecutionThreadPoolService executionThreadPoolService;
    @Autowired
    private IRecorderService recorderService;
    @Autowired
    private IFactoryRobotCapability factoryRobotCapability;
    @Autowired
    private IRestService restService;
    @Autowired
    private IHarService harService;
    @Autowired
    private ILambdaTestService lambdaTestService;
    @Autowired
    private IRobotProxyService executorService;

    private static Map<String, Boolean> apkAlreadyPrepare = new HashMap<>();
    private static int totocpt = 0;

    private static final Logger LOG = LogManager.getLogger(RobotServerService.class);
    // Proxy default config. (Should never be used as default config is inserted into database)
    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    public static final String OPTIONS_TIMEOUT_SYNTAX = "timeout";
    public static final String OPTIONS_HIGHLIGHTELEMENT_SYNTAX = "highlightElement";
    public static final String OPTIONS_MINSIMILARITY_SYNTAX = "minSimilarity";
    public static final String OPTIONS_TYPEDELAY_SYNTAX = "typeDelay";

    @Override
    public void startServer(TestCaseExecution tCExecution) throws CerberusException {

        try {

            LOG.info("Start Robot Server (Selenium, Appium or Sikuli)");

            // Set Session
            LOG.debug("Setting the session.");
            String system = tCExecution.getApplicationObj().getSystem();

            /*
             * Get the parameters that will be used to set the servers
             * (selenium/appium) If timeout has been defined at the execution
             * level, set the selenium & appium wait element with this value,
             * else, take the one from parameter
             */
            Integer cerberus_selenium_pageLoadTimeout, cerberus_selenium_implicitlyWait, cerberus_selenium_setScriptTimeout,
                    cerberus_selenium_wait_element, cerberus_sikuli_wait_element, cerberus_appium_wait_element, cerberus_selenium_action_click_timeout,
                    cerberus_appium_action_longpress_wait, cerberus_selenium_autoscroll_vertical_offset, cerberus_selenium_autoscroll_horizontal_offset,
                    cerberus_selenium_highlightElement, cerberus_sikuli_highlightElement;
            boolean cerberus_selenium_autoscroll;
            String cerberus_sikuli_minSimilarity;
            String cerberus_sikuli_typeDelay;

            if (!tCExecution.getTimeout().isEmpty()) {
                cerberus_selenium_wait_element = Integer.valueOf(tCExecution.getTimeout());
                cerberus_sikuli_wait_element = Integer.valueOf(tCExecution.getTimeout());
                cerberus_appium_wait_element = Integer.valueOf(tCExecution.getTimeout());
            } else {
                cerberus_selenium_wait_element = parameterService.getParameterIntegerByKey("cerberus_selenium_wait_element", system, 30000);
                cerberus_sikuli_wait_element = parameterService.getParameterIntegerByKey("cerberus_sikuli_wait_element", system, 30000);
                cerberus_appium_wait_element = parameterService.getParameterIntegerByKey("cerberus_appium_wait_element", system, 30000);
            }

            cerberus_selenium_pageLoadTimeout = parameterService.getParameterIntegerByKey("cerberus_selenium_pageLoadTimeout", system, 90000);
            cerberus_selenium_implicitlyWait = parameterService.getParameterIntegerByKey("cerberus_selenium_implicitlyWait", system, 0);
            cerberus_selenium_setScriptTimeout = parameterService.getParameterIntegerByKey("cerberus_selenium_setScriptTimeout", system, 90000);
            cerberus_selenium_action_click_timeout = parameterService.getParameterIntegerByKey("cerberus_selenium_action_click_timeout", system, 90000);
            cerberus_selenium_autoscroll = parameterService.getParameterBooleanByKey("cerberus_selenium_autoscroll", system, false);
            cerberus_selenium_autoscroll_vertical_offset = parameterService.getParameterIntegerByKey("cerberus_selenium_autoscroll_vertical_offset", system, 0);
            cerberus_selenium_autoscroll_horizontal_offset = parameterService.getParameterIntegerByKey("cerberus_selenium_autoscroll_horizontal_offset", system, 0);
            cerberus_appium_action_longpress_wait = parameterService.getParameterIntegerByKey("cerberus_appium_action_longpress_wait", system, 8000);
            cerberus_selenium_highlightElement = parameterService.getParameterIntegerByKey("cerberus_selenium_highlightElement", "", 0);
            cerberus_sikuli_highlightElement = parameterService.getParameterIntegerByKey("cerberus_sikuli_highlightElement", "", 0);
            cerberus_sikuli_minSimilarity = parameterService.getParameterStringByKey("cerberus_sikuli_minSimilarity", "", "");
            cerberus_sikuli_typeDelay = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_sikuli_typeDelay, "", "0.1");

            LOG.debug("TimeOut defined on session : {}", cerberus_selenium_wait_element);

            Session session = new Session();
            session.setCerberus_selenium_implicitlyWait(cerberus_selenium_implicitlyWait);
            session.setCerberus_selenium_pageLoadTimeout(cerberus_selenium_pageLoadTimeout);
            session.setCerberus_selenium_setScriptTimeout(cerberus_selenium_setScriptTimeout);

            // _wait_element parameters
            session.setCerberus_selenium_wait_element(cerberus_selenium_wait_element);
            session.setCerberus_selenium_wait_element_default(cerberus_selenium_wait_element);
            session.setCerberus_sikuli_wait_element(cerberus_sikuli_wait_element);
            session.setCerberus_sikuli_wait_element_default(cerberus_sikuli_wait_element);
            session.setCerberus_appium_wait_element(cerberus_appium_wait_element);
            session.setCerberus_appium_wait_element_default(cerberus_appium_wait_element);

            // minSimilarity parameters
            session.setCerberus_sikuli_minSimilarity(cerberus_sikuli_minSimilarity);
            session.setCerberus_sikuli_minSimilarity_default(cerberus_sikuli_minSimilarity);

            // highlightElement parameters
            session.setCerberus_selenium_highlightElement(cerberus_selenium_highlightElement);
            session.setCerberus_selenium_highlightElement_default(cerberus_selenium_highlightElement);
            session.setCerberus_sikuli_highlightElement(cerberus_sikuli_highlightElement);
            session.setCerberus_sikuli_highlightElement_default(cerberus_sikuli_highlightElement);

            // typeDelay parameters
            session.setCerberus_sikuli_typeDelay(cerberus_sikuli_typeDelay);
            session.setCerberus_sikuli_typeDelay_default(cerberus_sikuli_typeDelay);
            
            // auto scroll parameters
            session.setCerberus_selenium_autoscroll(cerberus_selenium_autoscroll);
            session.setCerberus_selenium_autoscroll_vertical_offset(cerberus_selenium_autoscroll_vertical_offset);
            session.setCerberus_selenium_autoscroll_horizontal_offset(cerberus_selenium_autoscroll_horizontal_offset);

            session.setCerberus_selenium_action_click_timeout(cerberus_selenium_action_click_timeout);
            session.setCerberus_appium_action_longpress_wait(cerberus_appium_action_longpress_wait);
            session.setHost(tCExecution.getSeleniumIP());
            session.setPort(tCExecution.getRobotPort());
            session.setHostUser(tCExecution.getSeleniumIPUser());
            session.setHostPassword(tCExecution.getSeleniumIPPassword());
            session.setNodeHost(tCExecution.getSeleniumIP());
            session.setNodePort(tCExecution.getSeleniumPort());
            if (tCExecution.getRobotExecutorObj() != null) {
                LOG.debug("Session node proxy set : {}", tCExecution.getRobotExecutorObj().getNodeProxyPort());
                session.setNodeProxyPort(tCExecution.getRobotExecutorObj().getNodeProxyPort());
            } else {
                session.setNodeProxyPort(0);
            }
            session.setConsoleLogs(new JSONArray());

            tCExecution.setSession(session);
            tCExecution.setRobotProvider(guessRobotProvider(session.getHost()));
            LOG.debug("Session is set.");

            /*
             * Starting Cerberus Executor Proxy if it has been activated at
             * robot level.
             */
            if (tCExecution.getRobotExecutorObj() != null && "Y".equals(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {
                LOG.debug("Start Remote Proxy");
                executorService.startRemoteProxy(tCExecution);
                LOG.debug("Started Remote Proxy on port: {}", tCExecution.getRemoteProxyPort());
            }

            // SetUp Capabilities
            LOG.debug("Set Capabilities");
            MutableCapabilities caps = this.setCapabilities(tCExecution);
            session.setDesiredCapabilities(caps);
            LOG.debug("Set Capabilities - retrieved");

            // We record Caps list at the execution level.
            try {
                // Init additionalFinalCapabilities and set it from real caps.
                List<RobotCapability> additionalFinalCapabilities = new ArrayList<>();
                for (Map.Entry<String, Object> cap : caps.asMap().entrySet()) {
                    additionalFinalCapabilities.add(factoryRobotCapability.create(0, "", cap.getKey(), cap.getValue().toString()));
                }

                // Init inputCapabilities and set it from Robot values.
                List<RobotCapability> inputCapabilities = new ArrayList<>();
                if (tCExecution.getRobotObj() != null) {
                    inputCapabilities = tCExecution.getRobotObj().getCapabilities();
                }

                tCExecution.addFileList(recorderService.recordCapabilities(tCExecution, inputCapabilities, additionalFinalCapabilities));
            } catch (Exception ex) {
                LOG.error("Exception Saving Robot Caps {} Exception: {}", tCExecution.getId(), ex.toString(), ex);
            }

            // SetUp Proxy
            String hubUrl = StringUtil.cleanHostURL(RobotServerService.getBaseUrl(StringUtil.formatURLCredential(
                            tCExecution.getSession().getHostUser(),
                            tCExecution.getSession().getHostPassword(), session.getHost()),
                    session.getPort())) + "/wd/hub";
            LOG.debug("Hub URL :{}", hubUrl);
            URL url = new URL(hubUrl);
            HttpCommandExecutor executor = null;

            boolean isProxy = proxyService.useProxy(hubUrl, system);
            Factory factory = new OkHttpClient.Factory();

            // Timeout Management
            int robotTimeout = parameterService.getParameterIntegerByKey("cerberus_robot_timeout", system, 60000);
            Duration rbtTimeOut = Duration.ofMillis(robotTimeout);
            factory.builder().connectionTimeout(rbtTimeOut);

            if (isProxy) {

                // Proxy Management
                String proxyHost = parameterService.getParameterStringByKey("cerberus_proxy_host", system, DEFAULT_PROXY_HOST);
                int proxyPort = parameterService.getParameterIntegerByKey("cerberus_proxy_port", system, DEFAULT_PROXY_PORT);

                java.net.Proxy myproxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

                if (parameterService.getParameterBooleanByKey("cerberus_proxyauthentification_active", system, DEFAULT_PROXYAUTHENT_ACTIVATE)) {

                    String proxyUser = parameterService.getParameterStringByKey("cerberus_proxyauthentification_user", system, DEFAULT_PROXYAUTHENT_USER);
                    String proxyPassword = parameterService.getParameterStringByKey("cerberus_proxyauthentification_password", system, DEFAULT_PROXYAUTHENT_PASSWORD);

                    // TODO delete if comment bellow has no impact on the non reg campaign
/*
                    Authenticator proxyAuthenticator = new Authenticator() {
                        public Request authenticate(Route route, Response response) throws IOException {
                            String credential = Credentials.basic(proxyUser, proxyPassword);
                            return response.request().newBuilder()
                                    .header("Proxy-Authorization", credential)
                                    .build();
                        }
                    };
*/
                }
                factory.builder().proxy(myproxy);
            } else {
                factory.builder().proxy(java.net.Proxy.NO_PROXY);
            }

            executor = new HttpCommandExecutor(new HashMap<>(), url, factory);

            // SetUp Driver
            LOG.debug("Set Driver");
            WebDriver driver = null;
            AppiumDriver appiumDriver = null;
            switch (tCExecution.getApplicationObj().getType().toUpperCase()) {
                case Application.TYPE_GUI:
                    if (caps.getPlatform() != null && caps.getPlatform().is(Platform.ANDROID)) {
                        // Appium does not support connection from HTTPCommandExecutor. When connecting from Executor, it stops to work after a couple of instructions.
                        appiumDriver = new AndroidDriver(url, caps);
                    } else if (caps.getPlatform() != null && (caps.getPlatform().is(Platform.IOS) || caps.getPlatform().is(Platform.MAC))) {
                        appiumDriver = new IOSDriver(url, caps);
                    }
                    driver = appiumDriver == null ? new RemoteWebDriver(executor, caps) : appiumDriver;

                    tCExecution.setRobotProviderSessionID(getSession(driver, tCExecution.getRobotProvider()));
                    tCExecution.setRobotSessionID(getSession(driver));
                    break;

                case Application.TYPE_APK:
                    // add a lock on app path this part of code, because we can't install 2 apk with the same name simultaneously
                    String appUrl = null;
                    if (caps.getCapability("app") != null) {
                        appUrl = caps.getCapability("app").toString();
                    }

                    if (appUrl != null) { // FIX : appium can't install 2 apk simultaneously, so implement a litle latency between execution
                        synchronized (this) {
                            // with appium 1.7.2, we can't install 2 fresh apk simultaneously. Appium have to prepare the apk (transformation) on the first execution before (see this topic https://discuss.appium.io/t/execute-2-android-test-simultaneously-problem-during-install-apk/22030)
                            // provoque a latency if first test is already running and apk don't finish to be prepared
                            if (apkAlreadyPrepare.containsKey(appUrl) && Boolean.TRUE.equals(!apkAlreadyPrepare.get(appUrl))) {
                                Thread.sleep(10000);
                            } else {
                                apkAlreadyPrepare.put(appUrl, false);
                            }
                        }
                    }
                    appiumDriver = new AndroidDriver(url, caps);
                    if (apkAlreadyPrepare.containsKey(appUrl)) {
                        apkAlreadyPrepare.put(appUrl, true);
                    }

                    driver = appiumDriver;
                    tCExecution.setRobotProviderSessionID(getSession(driver, tCExecution.getRobotProvider()));
                    tCExecution.setRobotSessionID(getSession(driver));
                    break;

                case Application.TYPE_IPA:
                    appiumDriver = new IOSDriver(url, caps);
                    driver = appiumDriver;
                    tCExecution.setRobotProviderSessionID(getSession(driver, tCExecution.getRobotProvider()));
                    tCExecution.setRobotSessionID(getSession(driver));
                    break;

                case Application.TYPE_FAT:
                    //  Check sikuli extension is reachable
                    if (!sikuliService.isSikuliServerReachableOnRobot(session)) {
                        MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SIKULI_COULDNOTCONNECT);
                        mes.setDescription(mes.getDescription().replace("%SSIP%", tCExecution.getSession().getHost()));
                        mes.setDescription(mes.getDescription().replace("%SSPORT%", tCExecution.getSession().getPort()));
                        throw new CerberusException(mes);
                    }
                    // If CountryEnvParameter IP is set, open the App
                    if (!tCExecution.getCountryEnvironmentParameters().getIp().isEmpty()) {
                        sikuliService.doSikuliActionOpenApp(session, tCExecution.getCountryEnvironmentParameters().getIp());
                    }
                    break;
            }

            // We record Server Side Caps.
            if (driver != null) {
                try {

                    // Init additionalFinalCapabilities and set it from real caps.
                    List<RobotCapability> serverCapabilities = new ArrayList<>();
                    for (Map.Entry<String, Object> cap : ((HasCapabilities) driver).getCapabilities().asMap().entrySet()) {
                        serverCapabilities.add(factoryRobotCapability.create(0, "", cap.getKey(), cap.getValue().toString()));
                    }
                    tCExecution.addFileList(recorderService.recordServerCapabilities(tCExecution, serverCapabilities));
                } catch (Exception ex) {
                    LOG.error("Exception Saving Server Robot Caps " + tCExecution.getId(), ex);
                }
            }

            /*
             * Defining the timeout at the driver level. Only in case of no
             * Appium Driver (see
             * https://github.com/vertigo17/Cerberus/issues/754)
             */
            if (driver != null && appiumDriver == null) {
                driver.manage().timeouts().pageLoadTimeout(cerberus_selenium_pageLoadTimeout, TimeUnit.MILLISECONDS);
                driver.manage().timeouts().implicitlyWait(cerberus_selenium_implicitlyWait, TimeUnit.MILLISECONDS);
                driver.manage().timeouts().setScriptTimeout(cerberus_selenium_setScriptTimeout, TimeUnit.MILLISECONDS);
            }
            if (appiumDriver != null) {
                appiumDriver.manage().timeouts().implicitlyWait(cerberus_appium_wait_element, TimeUnit.MILLISECONDS);
            }
            tCExecution.getSession().setDriver(driver);
            tCExecution.getSession().setAppiumDriver(appiumDriver);

            /*
             * If Gui application, maximize window Get IP of Node in case of
             * remote Server. Maximize does not work for chrome browser We also
             * get the Real UserAgent from the browser.
             */
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    && !caps.getPlatform().equals(Platform.ANDROID) && !caps.getPlatform().equals(Platform.IOS)
                    && !caps.getPlatform().equals(Platform.MAC)) {
                // Maximize is not supported on Opera.
                if (!caps.getBrowserName().equals(BrowserType.CHROME) && !tCExecution.getBrowser().equalsIgnoreCase("opera")) {
                    driver.manage().window().maximize();
                }
                getIPOfNode(tCExecution);

                // If screenSize is defined, set the size of the screen.
                String targetScreensize = getScreenSizeToUse(tCExecution.getTestCaseObj().getScreenSize(), tCExecution.getScreenSize());
                LOG.debug("Selenium resolution : {}", targetScreensize);

                if (!tCExecution.getBrowser().equalsIgnoreCase(BrowserType.CHROME)) {
                    // For chrome the resolution has already been defined at capabilities level.
                    if ((!StringUtil.isNullOrEmpty(targetScreensize)) && targetScreensize.contains("*")) {
                        Integer screenWidth = Integer.valueOf(targetScreensize.split("\\*")[0]);
                        Integer screenLength = Integer.valueOf(targetScreensize.split("\\*")[1]);
                        setScreenSize(driver, screenWidth, screenLength);
                        LOG.debug("Selenium resolution Activated : {}*{}", screenWidth, screenLength);
                    }
                }
                // Getting windows size Not supported on Opera.
                if (!tCExecution.getBrowser().equalsIgnoreCase("opera")) {
                    tCExecution.setScreenSize(getScreenSize(driver));
                }
                tCExecution.setRobotDecli(tCExecution.getRobotDecli().replace("%SCREENSIZE%", tCExecution.getScreenSize()));

                String userAgent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");
                tCExecution.setUserAgent(userAgent);

            }

            // unlock device if deviceLockUnlock is active
            if (tCExecution.getRobotExecutorObj() != null && appiumDriver instanceof LocksDevice
                    && "Y".equals(tCExecution.getRobotExecutorObj().getDeviceLockUnlock())) {
                ((LocksDevice) appiumDriver).unlockDevice();
            }

            // Check if Sikuli is available on node.
            if (driver != null) {
                tCExecution.getSession().setSikuliAvailable(sikuliService.isSikuliServerReachableOnNode(tCExecution.getSession()));
            }

            tCExecution.getSession().setStarted(true);

        } catch (CerberusException exception) {
            LOG.error(exception.toString(), exception);
            throw new CerberusException(exception.getMessageError(), exception);
        } catch (MalformedURLException exception) {
            LOG.error(exception.toString(), exception);
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_URL_MALFORMED);
            mes.setDescription(mes.getDescription().replace("%URL%", tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort()));
            throw new CerberusException(mes, exception);
        } catch (UnreachableBrowserException exception) {
            LOG.warn("Could not connect to : {}:{}", tCExecution.getSeleniumIP(), tCExecution.getSeleniumPort());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_COULDNOTCONNECT);
            mes.setDescription(mes.getDescription().replace("%SSIP%", tCExecution.getSeleniumIP()));
            mes.setDescription(mes.getDescription().replace("%SSPORT%", tCExecution.getSeleniumPort()));
            mes.setDescription(mes.getDescription().replace("%ERROR%", exception.toString()));
            throw new CerberusException(mes, exception);
        } catch (Exception exception) {
            LOG.error(exception.toString(), exception);
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
            mes.setDescription(mes.getDescription().replace("%MES%", exception.toString()));
            executorService.stopRemoteProxy(tCExecution);
            throw new CerberusException(mes, exception);
        } finally {
            executionThreadPoolService.executeNextInQueueAsynchroneously(false);
        }
    }

    private String getSession(WebDriver driver, String robotProvider) {
        String session = "";
        switch (robotProvider) {
            case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
            case TestCaseExecution.ROBOTPROVIDER_LAMBDATEST: // For LambdaTest we get the exeid not here but by service call at the end of the execution.
            case TestCaseExecution.ROBOTPROVIDER_NONE:
                session = ((RemoteWebDriver) driver).getSessionId().toString();
                break;
            case TestCaseExecution.ROBOTPROVIDER_KOBITON:
                session = ((HasCapabilities) driver).getCapabilities().getCapability("kobitonSessionId").toString();
                break;
            default:
        }
        return session;
    }

    private String getSession(WebDriver driver) {
        String session = "";
        session = ((RemoteWebDriver) driver).getSessionId().toString();
        return session;
    }

    private String guessRobotProvider(String host) {
        if (host.contains("browserstack")) {
            return TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK;
        }
        if (host.contains("kobiton")) {
            return TestCaseExecution.ROBOTPROVIDER_KOBITON;
        }
        if (host.contains("lambdatest")) {
            return TestCaseExecution.ROBOTPROVIDER_LAMBDATEST;
        }
        return TestCaseExecution.ROBOTPROVIDER_NONE;
    }

    /**
     * Set DesiredCapabilities
     *
     * @param tCExecution
     * @return
     * @throws CerberusException
     */
    private MutableCapabilities setCapabilities(TestCaseExecution tCExecution) throws CerberusException {
        // Instanciate DesiredCapabilities
        MutableCapabilities caps = new MutableCapabilities();
        // In case browser is not defined at that level, we force it to firefox.
        if (StringUtil.isNullOrEmpty(tCExecution.getBrowser())) {
            tCExecution.setBrowser("");
        }

        // Set Browser Capabilities
        caps = this.setCapabilityBrowser(caps, tCExecution.getBrowser(), tCExecution);

        // Loop on RobotCapabilities to feed DesiredCapabilities Capability must be String, Integer or Boolean
        List<RobotCapability> additionalCapabilities = new ArrayList<>();
        if (tCExecution.getRobotObj() != null) {
            additionalCapabilities = tCExecution.getRobotObj().getCapabilitiesDecoded();
        }
        if (additionalCapabilities != null) {
            for (RobotCapability additionalCapability : additionalCapabilities) {
                LOG.debug("RobotCaps on Robot : {} caps: {} Value: {}", additionalCapability.getRobot(), additionalCapability.getCapability(), additionalCapability.getValue());
                if ((caps.getCapability(additionalCapability.getCapability()) == null)
                        || ((caps.getCapability(additionalCapability.getCapability()) != null) && (caps.getCapability(additionalCapability.getCapability()).toString().isEmpty()))) { // caps does not already exist so we can set it.
                    if (StringUtil.isBoolean(additionalCapability.getValue())) {
                        caps.setCapability(additionalCapability.getCapability(), StringUtil.parseBoolean(additionalCapability.getValue()));
                    } else if (StringUtil.isInteger(additionalCapability.getValue())) {
                        caps.setCapability(additionalCapability.getCapability(), Integer.valueOf(additionalCapability.getValue()));
                    } else {
                        caps.setCapability(additionalCapability.getCapability(), additionalCapability.getValue());
                    }
                }
            }
        } else {
            additionalCapabilities = new ArrayList<>();
        }

        // Feed DesiredCapabilities with values get from Robot
        if (!StringUtil.isNullOrEmpty(tCExecution.getPlatform())
                && ((caps.getCapability("platform") == null)
                || ((caps.getCapability("platform") != null)
                && (caps.getCapability("platform").toString().equals("ANY")
                || caps.getCapability("platform").toString().isEmpty())))) {

            caps.setCapability("platformName", tCExecution.getPlatform());
        }
        if (!StringUtil.isNullOrEmpty(tCExecution.getVersion())
                && ((caps.getCapability("version") == null)
                || ((caps.getCapability("version") != null)
                && (caps.getCapability("version").toString().isEmpty())))) {

            caps.setCapability("version", tCExecution.getVersion());
        }

        if (tCExecution.getRobotExecutorObj() != null) {
            // Setting deviceUdid and device name from executor.
            if (!StringUtil.isNullOrEmpty(tCExecution.getRobotExecutorObj().getDeviceUuid())
                    && ((caps.getCapability("udid") == null)
                    || ((caps.getCapability("udid") != null)
                    && (caps.getCapability("udid").toString().isEmpty())))) {

                caps.setCapability("udid", tCExecution.getRobotExecutorObj().getDeviceUuid());
            }

            if (!StringUtil.isNullOrEmpty(tCExecution.getRobotExecutorObj().getDeviceName())
                    && ((caps.getCapability("deviceName") == null)
                    || ((caps.getCapability("deviceName") != null)
                    && (caps.getCapability("deviceName").toString().isEmpty())))) {

                caps.setCapability("deviceName", tCExecution.getRobotExecutorObj().getDeviceName());
            }

            if (!StringUtil.isNullOrEmpty(tCExecution.getRobotExecutorObj().getDeviceName())) {
                if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                        && ((caps.getCapability("systemPort") == null)
                        || ((caps.getCapability("systemPort") != null)
                        && (caps.getCapability("systemPort").toString().isEmpty())))) {

                    caps.setCapability("systemPort", tCExecution.getRobotExecutorObj().getDevicePort());
                } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)
                        && ((caps.getCapability("wdaLocalPort") == null)
                        || ((caps.getCapability("wdaLocalPort") != null)
                        && (caps.getCapability("wdaLocalPort").toString().isEmpty())))) {

                    caps.setCapability("wdaLocalPort", tCExecution.getRobotExecutorObj().getDevicePort());
                }
            }
        }

        // if application is a mobile one, then set the "app" capability to theapplication binary path
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
            // Set the app capability with the application path
            if (!StringUtil.isNullOrEmpty(tCExecution.getMyHost())
                    && (isNotAlreadyDefined(caps, "app"))) {
                caps.setCapability("app", tCExecution.getMyHost());
            } else if (isNotAlreadyDefined(caps, "app")) {
                caps.setCapability("app", tCExecution.getCountryEnvironmentParameters().getIp());
            }

            if (!StringUtil.isNullOrEmpty(tCExecution.getCountryEnvironmentParameters().getMobileActivity())
                    && (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    && isNotAlreadyDefined(caps, "appWaitActivity"))) {

                caps.setCapability("appWaitActivity", tCExecution.getCountryEnvironmentParameters().getMobileActivity());
            }

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    && (isNotAlreadyDefined(caps, "automationName"))) {
                caps.setCapability("automationName", "UIAutomator2"); // use UIAutomator2 by default
            }
        }

        // Setting specific capabilities of external cloud providers.
        switch (tCExecution.getRobotProvider()) {
            case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
                if (!StringUtil.isNullOrEmpty(tCExecution.getTag()) && isNotAlreadyDefined(caps, "build")) {
                    caps.setCapability("build", tCExecution.getTag());
                }
                if (isNotAlreadyDefined(caps, "project")) {
                    caps.setCapability("project", tCExecution.getApplication());
                }
                if (isNotAlreadyDefined(caps, "name")) {
                    String externalExeName = parameterService.getParameterStringByKey("cerberus_browserstack_defaultexename", tCExecution.getSystem(), "Exe : %EXEID%");
                    externalExeName = externalExeName.replace("%EXEID%", String.valueOf(tCExecution.getId()));
                    caps.setCapability("name", externalExeName);
                }
                if (tCExecution.getVerbose() >= 2) {
                    if (isNotAlreadyDefined(caps, "browserstack.debug")) {
                        caps.setCapability("browserstack.debug", true);
                    }
                    if (isNotAlreadyDefined(caps, "browserstack.console")) {
                        caps.setCapability("browserstack.console", "warnings");
                    }
                    if (isNotAlreadyDefined(caps, "browserstack.networkLogs")) {
                        caps.setCapability("browserstack.networkLogs", true);
                    }
                }

                //Create or override these capabilities if proxy required.
                if (StringUtil.parseBoolean(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {
                    caps.setCapability("browserstack.local", true);
                    caps.setCapability("browserstack.user", tCExecution.getRobotExecutorObj().getHostUser());
                    caps.setCapability("browserstack.key", tCExecution.getRobotExecutorObj().getHostPassword());
                    caps.setCapability("browserstack.localIdentifier", tCExecution.getExecutionUUID());
                }

                break;
            case TestCaseExecution.ROBOTPROVIDER_LAMBDATEST:
                if (!StringUtil.isNullOrEmpty(tCExecution.getTag()) && isNotAlreadyDefined(caps, "build")) {
                    caps.setCapability("build", tCExecution.getTag());
                }
                if (isNotAlreadyDefined(caps, "name")) {
                    String externalExeName = parameterService.getParameterStringByKey("cerberus_lambdatest_defaultexename", tCExecution.getSystem(), "Exe : %EXEID% - %TESTDESCRIPTION%");
                    externalExeName = externalExeName.replace("%EXEID%", String.valueOf(tCExecution.getId()));
                    externalExeName = externalExeName.replace("%TESTFOLDER%", String.valueOf(tCExecution.getTest()));
                    externalExeName = externalExeName.replace("%TESTID%", String.valueOf(tCExecution.getTestCase()));
                    externalExeName = externalExeName.replace("%TESTDESCRIPTION%", String.valueOf(tCExecution.getDescription()));
                    caps.setCapability("name", externalExeName);
                }
                if (tCExecution.getVerbose() >= 2) {
                    if (isNotAlreadyDefined(caps, "video")) {
                        caps.setCapability("video", true);
                    }
                    if (isNotAlreadyDefined(caps, "visual")) {
                        caps.setCapability("visual", true);
                    }
                    if (isNotAlreadyDefined(caps, "network")) {
                        caps.setCapability("network", true);
                    }
                    if (isNotAlreadyDefined(caps, "console")) {
                        caps.setCapability("console", true);
                    }
                }
                break;
            case TestCaseExecution.ROBOTPROVIDER_KOBITON:
                if (isNotAlreadyDefined(caps, "sessionName")) {
                    String externalExeName = parameterService.getParameterStringByKey("cerberus_kobiton_defaultsessionname", tCExecution.getSystem(), "%EXEID% : %TEST% - %TESTCASE%");
                    externalExeName = externalExeName.replace("%EXEID%", String.valueOf(tCExecution.getId()));
                    externalExeName = externalExeName.replace("%APPLI%", String.valueOf(tCExecution.getApplication()));
                    externalExeName = externalExeName.replace("%TAG%", String.valueOf(tCExecution.getTag()));
                    externalExeName = externalExeName.replace("%TEST%", String.valueOf(tCExecution.getTest()));
                    externalExeName = externalExeName.replace("%TESTCASE%", String.valueOf(tCExecution.getTestCase()));
                    externalExeName = externalExeName.replace("%TESTCASEDESC%", String.valueOf(tCExecution.getTestCaseObj().getDescription()));
                    caps.setCapability("sessionName", externalExeName);
                }
                if (isNotAlreadyDefined(caps, "sessionDescription")) {
                    String externalExeName = parameterService.getParameterStringByKey("cerberus_kobiton_defaultsessiondescription", tCExecution.getSystem(), "%TESTCASEDESC%");
                    externalExeName = externalExeName.replace("%EXEID%", String.valueOf(tCExecution.getId()));
                    externalExeName = externalExeName.replace("%APPLI%", String.valueOf(tCExecution.getApplication()));
                    externalExeName = externalExeName.replace("%TAG%", String.valueOf(tCExecution.getTag()));
                    externalExeName = externalExeName.replace("%TEST%", String.valueOf(tCExecution.getTest()));
                    externalExeName = externalExeName.replace("%TESTCASE%", String.valueOf(tCExecution.getTestCase()));
                    externalExeName = externalExeName.replace("%TESTCASEDESC%", String.valueOf(tCExecution.getTestCaseObj().getDescription()));
                    caps.setCapability("sessionDescription", externalExeName);
                }

                if (isNotAlreadyDefined(caps, "deviceGroup")) {
                    caps.setCapability("deviceGroup", "KOBITON"); // use UIAutomator2 by default
                }
                break;
            case TestCaseExecution.ROBOTPROVIDER_NONE:
                break;
            default:
        }

        return caps;
    }

    private boolean isNotAlreadyDefined(MutableCapabilities caps, String capability) {
        return ((caps.getCapability(capability) == null)
                || ((caps.getCapability(capability) != null)
                && (caps.getCapability(capability).toString().isEmpty())));
    }

    /**
     * Instantiate DesiredCapabilities regarding the browser
     *
     * @param capabilities
     * @param browser
     * @param tCExecution
     * @return
     * @throws CerberusException
     */
    private MutableCapabilities setCapabilityBrowser(MutableCapabilities capabilities, String browser, TestCaseExecution tCExecution) throws CerberusException {
        try {
            // Get User Agent to use.
            String usedUserAgent;
            usedUserAgent = getUserAgentToUse(tCExecution.getTestCaseObj().getUserAgent(), tCExecution.getUserAgent());

            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, Level.ALL);

            switch (browser) {

                case "firefox":
                    FirefoxOptions optionsFF = new FirefoxOptions();
                    FirefoxProfile profile = new FirefoxProfile();
                    profile.setPreference("app.update.enabled", false);

                    // Language
                    try {
                        Invariant invariant = invariantService.convert(invariantService.readByKey("COUNTRY", tCExecution.getCountry()));
                        if (invariant.getGp2() == null) {
                            LOG.warn("Country selected ({}) has no value of GP2 in Invariant table, default language set to English (en)", tCExecution.getCountry());
                            profile.setPreference("intl.accept_languages", "en");
                        } else {
                            profile.setPreference("intl.accept_languages", invariant.getGp2());
                        }
                    } catch (CerberusException ex) {
                        LOG.warn("Country selected ({}) not in Invariant table, default language set to English (en)", tCExecution.getCountry());
                        profile.setPreference("intl.accept_languages", "en");
                    }

                    // Force a specific profile for that session (allows reusing cookies and browser preferences).
                    if (tCExecution.getRobotObj() != null && !StringUtil.isNullOrEmpty(tCExecution.getRobotObj().getProfileFolder())) {
                        optionsFF.addArguments("--profile");
                        optionsFF.addArguments(tCExecution.getRobotObj().getProfileFolder());
                    }

                    // Set UserAgent if testCaseUserAgent or robotUserAgent is defined
                    if (!StringUtil.isNullOrEmpty(usedUserAgent)) {
                        profile.setPreference("general.useragent.override", usedUserAgent);
                    }

                    // Verbose level and Headless
                    if (tCExecution.getVerbose() <= 0) {
                        optionsFF.setHeadless(true);
                    }
                    // Add the WebDriver proxy capability.
                    if (tCExecution.getRobotExecutorObj() != null && "Y".equals(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {
                        Proxy proxy = new Proxy();
                        proxy.setHttpProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setSslProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setProxyType(Proxy.ProxyType.MANUAL);
                        LOG.debug("Setting Firefox proxy to : {}", proxy);
                        optionsFF.setProxy(proxy);
                    }
                    optionsFF.setProfile(profile);

                    // Accept Insecure Certificates.
                    optionsFF.setAcceptInsecureCerts(tCExecution.getRobotObj() == null || tCExecution.getRobotObj().isAcceptInsecureCerts());

                    // Collect Logs on Selenium side.
                    optionsFF.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

                    return optionsFF;

                case "chrome":
                    ChromeOptions optionsCH = new ChromeOptions();
                    // Maximize windows for chrome browser
                    String targetScreensize = getScreenSizeToUse(tCExecution.getTestCaseObj().getScreenSize(), tCExecution.getScreenSize());
                    if ((!StringUtil.isNullOrEmpty(targetScreensize)) && targetScreensize.contains("*")) {
                        Integer screenWidth = Integer.valueOf(targetScreensize.split("\\*")[0]);
                        Integer screenLength = Integer.valueOf(targetScreensize.split("\\*")[1]);
                        String sizeOpts = "--window-size=" + screenWidth + "," + screenLength;
                        optionsCH.addArguments(sizeOpts);
                        LOG.debug("Selenium resolution (for Chrome) Activated : " + screenWidth + "*" + screenLength);

                    } else {
                        optionsCH.addArguments("start-maximized");
                    }

                    // Language
                    try {
                        Invariant invariant = invariantService.convert(invariantService.readByKey("COUNTRY", tCExecution.getCountry()));
                        if (invariant.getGp2() == null) {
                            LOG.warn("Country selected ({}) has no value of GP2 in Invariant table, default language set to English (en)", tCExecution.getCountry());
                            optionsCH.addArguments("--lang=en");
                        } else {
                            optionsCH.addArguments("--lang=" + invariant.getGp2());
                        }
                    } catch (CerberusException ex) {
                        LOG.warn("Country selected ({}) not in Invariant table, default language set to English (en)", tCExecution.getCountry());
                        optionsCH.addArguments("--lang=en");
                    }

                    // Force a specific profile for that session (allows reusing cookies and browser preferences).
                    if (tCExecution.getRobotObj() != null && !StringUtil.isNullOrEmpty(tCExecution.getRobotObj().getProfileFolder())) {
                        optionsCH.addArguments("user-data-dir=" + tCExecution.getRobotObj().getProfileFolder());
                    }

                    // Set UserAgent if necessary
                    if (!StringUtil.isNullOrEmpty(usedUserAgent)) {
                        optionsCH.addArguments("--user-agent=" + usedUserAgent);
                    }

                    // Verbose level and Headless
                    if (tCExecution.getVerbose() <= 0) {
                        optionsCH.addArguments("--headless");
                    }

                    // Add the WebDriver proxy capability.
                    if (tCExecution.getRobotExecutorObj() != null && "Y".equals(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {
                        Proxy proxy = new Proxy();
                        proxy.setHttpProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setSslProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setNoProxy("");
                        proxy.setProxyType(Proxy.ProxyType.MANUAL);
                        LOG.debug("Setting Chrome proxy to : {}", proxy);
                        optionsCH.setCapability(DEFAULT_PROXY_HOST, proxy);
                    }

                    // Accept Insecure Certificates.
                    if (tCExecution.getRobotObj() != null && !tCExecution.getRobotObj().isAcceptInsecureCerts()) {
                        optionsCH.setAcceptInsecureCerts(false);
                    } else {
                        optionsCH.setAcceptInsecureCerts(true);
                    }

                    // Extra Browser Parameters.
                    if (tCExecution.getRobotObj() != null && !StringUtil.isNullOrEmpty(tCExecution.getRobotObj().getExtraParam())) {
                        optionsCH.addArguments(tCExecution.getRobotObj().getExtraParam());
                    }

                    // Collect Logs on Selenium side.
                    optionsCH.setCapability("goog:loggingPrefs", logPrefs);

                    return optionsCH;

                case "safari":
                    SafariOptions optionsSA = new SafariOptions();
                    if (tCExecution.getRobotExecutorObj() != null && "Y".equals(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {
                        Proxy proxy = new Proxy();
                        proxy.setHttpProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setSslProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        optionsSA.setProxy(proxy);
                    }
                    return optionsSA;

                case "IE":
                    InternetExplorerOptions optionsIE = new InternetExplorerOptions();
                    // Add the WebDriver proxy capability.
                    if (tCExecution.getRobotExecutorObj() != null && "Y".equals(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {
                        Proxy proxy = new Proxy();
                        proxy.setHttpProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setSslProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setProxyType(Proxy.ProxyType.MANUAL);
                        LOG.debug("Setting IE proxy to : " + proxy.toString());
                        optionsIE.setCapability(DEFAULT_PROXY_HOST, proxy);
                    }
                    return optionsIE;

                case "edge":
                    EdgeOptions optionsED = new EdgeOptions();
                    if (tCExecution.getRobotExecutorObj() != null && "Y".equals(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {
                        Proxy proxy = new Proxy();
                        proxy.setHttpProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setSslProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        optionsED.setProxy(proxy);
                    }
                    return optionsED;

                case "opera":
                    OperaOptions optionsOP = new OperaOptions();
                    if (tCExecution.getRobotExecutorObj() != null && "Y".equals(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {
                        Proxy proxy = new Proxy();
                        proxy.setHttpProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setSslProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        optionsOP.setProxy(proxy);
                    }
                    optionsOP.setCapability("browser", "opera");
                    // Forcing a profile in order to force UserAgent. This has been commented because it fail when using BrowserStack that does not allow to create the correcponding profile folder.
//                    if (!StringUtil.isNullOrEmpty(usedUserAgent)) {
//                        optionsOP.setCapability("opera.profile", "{profileName: \"foo\",userAgent: \"" + usedUserAgent + "\"}");
//                    }
                    return optionsOP;

                case "android":
                    if (tCExecution.getRobotExecutorObj() != null && "Y".equals(tCExecution.getRobotExecutorObj().getExecutorProxyActive())) {
                        Proxy proxy = new Proxy();
                        proxy.setHttpProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                        proxy.setSslProxy(tCExecution.getRobotExecutorObj().getExecutorProxyHost() + ":" + tCExecution.getRemoteProxyPort());
                    }
                    capabilities = DesiredCapabilities.android();
                    break;

                case "ipad":
                    capabilities = DesiredCapabilities.ipad();
                    break;

                case "iphone":
                    capabilities = DesiredCapabilities.iphone();
                    break;

                case "":
                    // We allow to start a Selenium session without any browser defined. This is used to support bundleId capability.
                    break;

                // Unfortunatly Yandex is not yet supported on BrowserStack. Once it will be it should look like that:
//                case "yandex":
//                    capabilities = new DesiredCapabilities();
//                    capabilities.setCapability("browser", "Yandex");
//                    capabilities.setCapability("browser_version", "14.12");
//                    break;
                default:
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
     * @return String containing the screensize to use
     */
    private String getScreenSizeToUse(String screenSizeTestCase, String screenSizeRobot) {
        if (StringUtil.isNullOrEmpty(screenSizeRobot) && StringUtil.isNullOrEmpty(screenSizeTestCase)) {
            return "";
        } else {
            return StringUtil.isNullOrEmpty(screenSizeTestCase) ? screenSizeRobot : screenSizeTestCase;
        }
    }

    @Override
    public boolean stopServer(TestCaseExecution tce) {
        Session session = tce.getSession();
        if (session != null && session.isStarted()) {
            try {
                // Wait 2 sec till HAR is exported
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                LOG.error(ex.toString(), ex);
            }

            //  We remove manually the package if it is defined.
            if (session.getAppiumDriver() != null && tce.getCountryEnvironmentParameters() != null
                    && !StringUtil.isNullOrEmpty(tce.getCountryEnvironmentParameters().getMobilePackage())) {
                session.getAppiumDriver().removeApp(tce.getCountryEnvironmentParameters().getMobilePackage());
            }

            // We lock device if deviceLockUnlock is active.
            if (tce.getRobotExecutorObj() != null && session.getAppiumDriver() != null && session.getAppiumDriver() instanceof LocksDevice
                    && "Y".equals(tce.getRobotExecutorObj().getDeviceLockUnlock())) {
                ((LocksDevice) session.getAppiumDriver()).lockDevice();
            }

            //  We record Selenium log at the end of the execution.
            switch (tce.getRobotProvider()) {
                case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
                case TestCaseExecution.ROBOTPROVIDER_NONE:
                    try {
                        tce.addFileList(recorderService.recordSeleniumLog(tce));
                    } catch (Exception ex) {
                        LOG.error("Exception Getting Selenium Logs {}", tce.getId(), ex);
                    }
                    break;
                default:
            }

            // We record Console log at the end of the execution.
            switch (tce.getRobotProvider()) {
                case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
                case TestCaseExecution.ROBOTPROVIDER_NONE:
                    try {
                        tce.addFileList(recorderService.recordConsoleLog(tce));
                    } catch (Exception ex) {
                        LOG.error("Exception Getting Console Logs " + tce.getId(), ex);
                    }
                    break;
                default:
            }

            // We record Har log at the end of the execution.
            switch (tce.getRobotProvider()) {
                case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
//                    try {
//                        String url = "http://api.bs.com/getHar?uuid=" + tce.getRobotSessionID();
//                        tce.addFileList(recorderService.recordBrowserstackHarLog(tce, url));
//                    } catch (Exception ex) {
//                        LOG.error("Exception Getting Browserstack HAR File " + tce.getId(), ex);
//                    }
                    break;
                case TestCaseExecution.ROBOTPROVIDER_NONE:
                    break;
                default:
            }
            try {
                // Get Har File when Cerberus Executor is activated.
                // If proxy started and parameter verbose >= 1 activated
                if ("Y".equals(tce.getRobotExecutorObj().getExecutorProxyActive())
                        && tce.getVerbose() >= 1 && (parameterService.getParameterBooleanByKey("cerberus_networkstatsave_active", tce.getSystem(), false))) {

                    // Before collecting the stats, we wait the network idles for few minutes
                    executorService.waitForIdleNetwork(tce.getRobotExecutorObj().getExecutorExtensionHost(), tce.getRobotExecutorObj().getExecutorExtensionPort(), tce.getRemoteProxyUUID(), tce.getSystem());

                    // We now get the har data.
                    JSONObject har = executorService.getHar(null, false, tce.getRobotExecutorObj().getExecutorExtensionHost(), tce.getRobotExecutorObj().getExecutorExtensionPort(), tce.getRemoteProxyUUID(), tce.getSystem(), 0);

                    // and enrich it with stat entry.
                    har = harService.enrichWithStats(har, tce.getCountryEnvironmentParameters().getDomain(), tce.getSystem(), tce.getNetworkTrafficIndexList());

                    // We convert the har to database record HttpStat and save it to database.
                    try {

                        AnswerItem<TestCaseExecutionHttpStat> answHttpStat = testCaseExecutionHttpStatService.convertFromHarWithStat(tce, har);
                        tce.setHttpStat(answHttpStat.getItem());

                        testCaseExecutionHttpStatService.create(answHttpStat.getItem());

                    } catch (Exception ex) {
                        LOG.warn("Exception collecting and saving stats for execution {}  Exception : {}", tce.getId(), ex.toString());
                    }
                }
            } catch (Exception ex) {
                LOG.error("Exception Getting Har File from Cerberus Executor {}", tce.getId(), ex);
            }

            // We Stop the Robot Session (Selenium or Appium).
            LOG.info("Stop execution robot session");
            if (tce.getRobotProvider().equals(TestCaseExecution.ROBOTPROVIDER_KOBITON)) {
                // For Kobiton, we should first close Appium session.
                if (session.getAppiumDriver() != null) {
                    session.getAppiumDriver().close();
                }
                if (session.getDriver() != null) {
                    session.getDriver().quit();
                }
            } else {
                session.quit();
            }

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

            HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

            URL sessionURL = new URL(RobotServerService.getBaseUrl(session.getHost(), session.getPort()) + "/grid/api/testsession?session=" + sessionId);

            BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("GET", sessionURL.toExternalForm());
            LOG.debug("Calling Hub to get the node information. {}", sessionURL);
            HttpResponse response = client.execute(host, r);
            if (!response.getStatusLine().toString().contains("403")
                    && !response.getEntity().getContentType().getValue().contains("text/html")) {
                InputStream contents = response.getEntity().getContent();
                StringWriter writer = new StringWriter();
                IOUtils.copy(contents, writer, "UTF8");
                JSONObject object = new JSONObject(writer.toString());
                if (object.has("proxyId")) {
                    URL myURL = new URL(object.getString("proxyId"));
                    if ((myURL.getHost() != null) && (myURL.getPort() != -1)) {
                        LOG.debug("Get remote node information : {} - {}", myURL.getHost(), myURL.getPort());
                        tCExecution.setRobotHost(myURL.getHost());
                        tCExecution.setRobotPort(String.valueOf(myURL.getPort()));
                        // Node information at session level is now overwrite with real values.
                        tCExecution.getSession().setNodeHost(myURL.getHost());
                        tCExecution.getSession().setNodePort(String.valueOf(myURL.getPort()));
                    }
                } else {
                    LOG.debug("'proxyId' json data not available from remote Selenium Server request : {}", writer);
                }
            }

        } catch (IOException | JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
    }

    @Override
    public Capabilities getUsedCapabilities(Session session) {
        return ((HasCapabilities) session.getDriver()).getCapabilities();
    }

    private void setScreenSize(WebDriver driver, Integer width, Integer length) {
        driver.manage().window().setPosition(new Point(0, 0));
        driver.manage().window().setSize(new Dimension(width, length));
    }

    private String getScreenSize(WebDriver driver) {
        return driver.manage().window().getSize().width + "*" + driver.manage().window().getSize().height;
    }

    private static String getBaseUrl(String host, String port) {
        String baseurl = "";

        if (!StringUtil.isNullOrEmpty(host) && (host.contains("https://") || host.contains("http://"))) {
            baseurl = host;
        } else {
            baseurl = "http://" + host;
        }

        if (!StringUtil.isNullOrEmpty(port) && Integer.valueOf(port) > 0) {
            baseurl += ":" + port;
        }

        return baseurl;
    }

    @Override
    public HashMap<String, String> getMapFromOptions(JSONArray options) {
        HashMap<String, String> result = new HashMap<>();
        if (options.length() > 0) {
            LOG.debug("Converting {} To Map.", options);
            for (int i = 0; i < options.length(); i++) {
                try {
                    JSONObject option = options.getJSONObject(i);
                    if (option.getBoolean("act")) {
                        result.put(option.getString("option"), option.getString("value"));
                    }
                } catch (JSONException ex) {
                    LOG.error(ex, ex);
                    return result;
                }
            }
        }
        return result;
    }

    @Override
    public void setOptionsTimeout(Session session, Integer timeout) {
        if (session != null) {
            LOG.debug("Setting Robot Options timeout to : {}", timeout);
            session.setCerberus_selenium_wait_element(timeout);
            session.setCerberus_appium_wait_element(timeout);
            session.setCerberus_sikuli_wait_element(timeout);
        }
    }

    @Override
    public void setOptionsHighlightElement(Session session, Integer highlightElement) {
        if (session != null) {
            LOG.debug("Setting Robot Option highlightElement to : {}", highlightElement);
            session.setCerberus_selenium_highlightElement(highlightElement);
            session.setCerberus_sikuli_highlightElement(highlightElement);
        }
    }

    @Override
    public void setOptionsMinSimilarity(Session session, String minSimilarity) {
        if (session != null) {
            LOG.debug("Setting Robot Option minSimilarity to : {}", minSimilarity);
            session.setCerberus_sikuli_minSimilarity(minSimilarity);
        }
    }

    @Override
    public void setOptionsTypeDelay(Session session, String typeDelay) {
        if (session != null) {
            LOG.debug("Setting Robot Option typeDelay to : {}", typeDelay);
            session.setCerberus_sikuli_typeDelay(typeDelay);
        }
    }

    @Override
    public void setOptionsToDefault(Session session) {
        if (session != null) {
            LOG.debug("Setting Robot Timeout back to default values : Selenium {} Appium {} Sikuli {}",
                    session.getCerberus_selenium_wait_element_default(),
                    session.getCerberus_appium_wait_element_default(),
                    session.getCerberus_sikuli_wait_element_default());
            session.setCerberus_selenium_wait_element(session.getCerberus_selenium_wait_element_default());
            session.setCerberus_appium_wait_element(session.getCerberus_appium_wait_element_default());
            session.setCerberus_sikuli_wait_element(session.getCerberus_sikuli_wait_element_default());
            LOG.debug("Setting Robot highlightElement back to default values : Selenium {} Sikuli {}",
                    session.getCerberus_selenium_highlightElement_default(),
                    session.getCerberus_sikuli_highlightElement_default());
            session.setCerberus_selenium_highlightElement(session.getCerberus_selenium_highlightElement_default());
            session.setCerberus_sikuli_highlightElement(session.getCerberus_sikuli_highlightElement_default());
            LOG.debug("Setting Robot minSimilarity back to default values : {}", session.getCerberus_sikuli_minSimilarity_default());
            session.setCerberus_sikuli_minSimilarity(session.getCerberus_sikuli_minSimilarity_default());
            LOG.debug("Setting Robot typeDelay back to default values : {}", session.getCerberus_sikuli_typeDelay_default());
            session.setCerberus_sikuli_typeDelay(session.getCerberus_sikuli_typeDelay_default());
        }
    }

}
