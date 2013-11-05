/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.Selenium;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author bcivel
 */
public interface IFactorySelenium {

    /**
     * 
     * @param host : IP of the Test Machine
     * @param port : Port ued for connection to the test Machine
     * @param browser : Browser Name used for the test
     * @param login
     * @param ip
     * @param driver
     * @return 
     */
    Selenium create(String host, String port, String browser, String login, String ip, WebDriver driver, long wait);
}
