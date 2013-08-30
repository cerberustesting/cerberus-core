package com.redcats.tst.entity;

import org.openqa.selenium.WebDriver;


/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public class Selenium {

    private String host;
    private String port;
    private String browser;
    private String login;
    private String ip;
    private WebDriver driver;


    public String getHost() {
        return this.host;
    }

    public void setHost(String tempHost) {
        this.host = tempHost;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String tempPort) {
        this.port = tempPort;
    }

    public String getBrowser() {
        return this.browser;
    }

    public void setBrowser(String tempBrowser) {
        this.browser = tempBrowser;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String tempLogin) {
        this.login = tempLogin;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String tempIp) {
        this.ip = tempIp;
    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public void setDriver(WebDriver webDriver) {
        this.driver = webDriver;
    }
}
