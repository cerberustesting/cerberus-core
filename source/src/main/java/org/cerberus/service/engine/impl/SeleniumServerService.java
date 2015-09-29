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
package org.cerberus.service.engine.impl;

import io.appium.java_client.AppiumDriver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.Selenium;
import org.cerberus.crud.entity.Session;
import org.cerberus.crud.entity.SessionCapabilities;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.engine.ISeleniumServerService;
import org.cerberus.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
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

    @Override
    public void startServer(TestCaseExecution tCExecution) throws CerberusException {

        try {

            /**
             * SetUp Capabilities
             */
            MyLogger.log(SeleniumServerService.class.getName(), Level.DEBUG, "Set Capabilities");
            DesiredCapabilities caps = this.setCapabilities(tCExecution);

            /**
             * SetUp Driver
             */
            MyLogger.log(SeleniumServerService.class.getName(), Level.DEBUG, "Set Driver");
            WebDriver driver = null;
            AppiumDriver appiumDriver = null;
            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                driver = new RemoteWebDriver(new URL("http://" + tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort() + "/wd/hub"), caps);
            } else if (tCExecution.getApplication().getType().equalsIgnoreCase("APK")) {
                appiumDriver = new AppiumDriver(new URL("http://" + tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort() + "/wd/hub"), caps);
                driver = (WebDriver) appiumDriver;
            }

            tCExecution.getSession().setDriver(driver);
            tCExecution.getSession().setAppiumDriver(appiumDriver);

            /**
             * If Gui application, maximize window Get IP of Node in case of
             * remote Server
             */
            if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
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
            MyLogger.log(Selenium.class.getName(), Level.ERROR, exception.toString());
            throw new CerberusException(exception.getMessageError());
        } catch (MalformedURLException exception) {
            MyLogger.log(Selenium.class.getName(), Level.ERROR, exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_URL_MALFORMED);
            mes.setDescription(mes.getDescription().replace("%URL%", tCExecution.getSession().getHost() + ":" + tCExecution.getSession().getPort()));
            throw new CerberusException(mes);
        } catch (UnreachableBrowserException exception) {
            MyLogger.log(Selenium.class.getName(), Level.ERROR, exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_COULDNOTCONNECT);
            mes.setDescription(mes.getDescription().replace("%SSIP%", tCExecution.getSeleniumIP()));
            mes.setDescription(mes.getDescription().replace("%SSPORT%", tCExecution.getSeleniumPort()));
            throw new CerberusException(mes);
        } catch (Exception exception) {
            MyLogger.log(Selenium.class.getName(), Level.ERROR, exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_SELENIUM);
            mes.setDescription(mes.getDescription().replace("%MES%", exception.toString()));
            throw new CerberusException(mes);
        }
    }

    private DesiredCapabilities setFirefoxProfile(TestCaseExecution tCExecution) throws CerberusException {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setEnableNativeEvents(true);
        profile.setAcceptUntrustedCertificates(true);
        profile.setPreference("network.http.connection-timeout", "300");

        try {
            Invariant invariant = this.invariantService.findInvariantByIdValue("COUNTRY", tCExecution.getCountry());
            if (invariant.getGp2() == null) {
                MyLogger.log(Selenium.class.getName(), Level.WARN, "Country selected (" + tCExecution.getCountry() + ") has no value of GP2 in Invariant table, default language set to English(en)");
                profile.setPreference("intl.accept_languages", "en");
            } else {
                profile.setPreference("intl.accept_languages", invariant.getGp2());
            }
        } catch (CerberusException ex) {
            MyLogger.log(Selenium.class.getName(), Level.WARN, "Country selected (" + tCExecution.getCountry() + ") not in Invariant table, default language set to English(en)");
            profile.setPreference("intl.accept_languages", "en");
        }

        if (tCExecution.getVerbose() > 0) {
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
                MyLogger.log(SeleniumServerService.class.getName(), Level.DEBUG, "Adding firebug extension : " + firebugPath);
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
                MyLogger.log(SeleniumServerService.class.getName(), Level.DEBUG, "Adding netexport extension : " + netexportPath);
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
            String url = cerberusUrl + "/SaveStatistic?logId=" + tCExecution.getExecutionUUID();
            profile.setPreference("extensions.firebug.netexport.autoExportToServer", true);
            profile.setPreference("extensions.firebug.netexport.beaconServerURL", url);
            MyLogger.log(SeleniumServerService.class.getName(), Level.DEBUG, "Selenium netexport.beaconServerURL : " + url);
            profile.setPreference("extensions.firebug.netexport.sendToConfirmation", false);
            profile.setPreference("extensions.firebug.netexport.showPreview", false);

        }

        //if userAgent
        if (!("").equals(tCExecution.getUserAgent())) {
            profile.setPreference("general.useragent.override", tCExecution.getUserAgent());
        }

        DesiredCapabilities dc = DesiredCapabilities.firefox();
        dc.setCapability(FirefoxDriver.PROFILE, profile);

        return dc;
    }

    private DesiredCapabilities setCapabilities(TestCaseExecution tCExecution) throws CerberusException {
        DesiredCapabilities caps = new DesiredCapabilities();
        for (SessionCapabilities cap : tCExecution.getSession().getCapabilities()) {
            if (!cap.getValue().equals("")) {
                if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
                    if (cap.getCapability().equalsIgnoreCase("browser")) {
                        caps = this.setCapabilityBrowser(caps, cap.getValue(), tCExecution);
                    } else {
                        caps.setCapability(cap.getCapability(), cap.getValue());
                    }
                }
            }
            if (tCExecution.getApplication().getType().equalsIgnoreCase("APK")) {
                caps.setCapability(CapabilityType.BROWSER_NAME, "");
                caps.setCapability("deviceName", "Android");
                caps.setCapability("automationName", "Appium");
                caps.setCapability("platformName", "Android");
                caps.setCapability("autoWebview", true);

//                if (cap.getCapability().equalsIgnoreCase("browser")) {
//                    caps.setCapability(CapabilityType.BROWSER_NAME, "android");
//                }
//                if (cap.getCapability().equalsIgnoreCase("platform")) {
//                    caps.setCapability("platformName", cap.getValue());
//                }
//                if (cap.getCapability().equalsIgnoreCase("version")) {
//                    caps.setCapability("deviceName", cap.getValue());
//                }
            }

        }
        /**
         * If android app, set app capability with the link where is stored the
         * apk
         */
        if (tCExecution.getApplication().getType().equalsIgnoreCase("APK")) {
            File app = new File(tCExecution.getCountryEnvironmentApplication().getIp());
            caps.setCapability("app", app);
        }
        return caps;
    }

    @Override
    public boolean stopServer(Session session) {
        if (session.isStarted()) {
            try {
                // Wait 2 sec till HAR is exported
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SeleniumServerService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

            session.getDriver().quit();
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
            Logger.getLogger(SeleniumServerService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(SeleniumServerService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public DesiredCapabilities setCapabilityBrowser(DesiredCapabilities capabilities, String browser, TestCaseExecution tCExecution) throws CerberusException {
        try {
            if (browser.equalsIgnoreCase("firefox")) {
                capabilities = this.setFirefoxProfile(tCExecution);
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
}
