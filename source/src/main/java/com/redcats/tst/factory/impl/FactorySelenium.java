/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Selenium;
import com.redcats.tst.factory.IFactorySelenium;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactorySelenium implements IFactorySelenium {

    @Override
    public Selenium create(String host, String port, String browser, String login, String ip, WebDriver driver, long wait) {
        Selenium newSelenium = new Selenium();
        newSelenium.setHost(host == null ? "localhost" : host);
        newSelenium.setPort(port);
        newSelenium.setBrowser(browser);
        newSelenium.setLogin(login.startsWith("/") ? login.substring(1) : login);
        newSelenium.setIp(ip);
        newSelenium.setDefaultWait(wait);

        return newSelenium;
    }
}
