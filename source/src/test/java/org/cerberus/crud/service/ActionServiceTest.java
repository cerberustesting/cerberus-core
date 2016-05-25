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

package org.cerberus.crud.service;

import junit.framework.Assert;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Selenium;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.engine.gwt.impl.ActionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.ContextConfiguration;


/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 17/01/2013
 * @since 0.9.0
 */
@RunWith(PowerMockRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class ActionServiceTest {

    @Mock
    private WebDriver driver;
    @Mock
    private Selenium selenium;
    @Mock
    private RemoteWebElement element;
    @Mock
    private By by;
    @Mock
    private ExpectedCondition<WebElement> expectedCondition;
    @Mock
    private WebDriverWait webDriverWait;
    @Mock
    private FluentWait fluentWait;
    @InjectMocks
    private ActionService actionService;

    /**
     * Action Click
     */
//    @Test
//    public void testDoActionClickWhenObjectNullAndPropertyNull() {
//        String object = "";
//        String property = "";
//        String msg = "Object and Property are ‘null’. At least one is mandatory in order to perform the action click.";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("click");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//        TestCaseExecution tce = new TestCaseExecution();
//        TestCaseStepExecution tcse = new TestCaseStepExecution();
//        tcse.settCExecution(tce);
//        tcsae.setTestCaseStepExecution(tcse);
//        
//        tcsae = this.actionService.doAction(tcsae);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }

//    @Test
//    public void testDoActionClickObjectWhenSuccess() throws Exception {
//        String object = "id=test";
//        String property = "null";
//        String msg = "Element '" + object + "' clicked.";
//
////        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
////        tcsae.setAction("click");
////        tcsae.setObject(object);
////        tcsae.setProperty(property);
//        
//        PowerMockito.mockStatic(ExpectedConditions.class);
//        PowerMockito.mockStatic(By.class);
//
//        when(selenium.getDriver()).thenReturn(driver);
//        when(By.id(anyString())).thenReturn(by);
//        PowerMockito.whenNew(WebDriverWait.class).withAnyArguments().thenReturn(webDriverWait);
//        when(ExpectedConditions.visibilityOfElementLocated(by)).thenReturn(expectedCondition);
//        when(fluentWait.until(expectedCondition)).thenReturn(element);
//        when(driver.findElement(by)).thenReturn(element);
//
//        MessageEvent message = this.seleniumService.doSeleniumActionClick(object, property);
//
//        Assert.assertEquals(msg, message.getDescription());
//    }
//
//    @Test
//    public void testDoActionClickObjectWhenElementNotFound() {
//        String object = "id=test";
//        String property = "null";
//        String msg = "Failed to click because could not find element '" + object + "'!";
//
////        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
////        tcsae.setAction("click");
////        tcsae.setObject(object);
////        tcsae.setProperty(property);
//        
//        when(selenium.getDriver()).thenReturn(driver);
//        when(driver.findElement(By.id(anyString()))).thenThrow(new NoSuchElementException(""));
//
//        MessageEvent message = this.seleniumService.doSeleniumActionClick(object, property);
//
//        Assert.assertEquals(msg, message.getDescription());
//    }
//
//    @Test
//    public void testDoActionClickPropertyWhenSuccess() throws Exception {
//        String object = "null";
//        String property = "id=test";
//        String msg = "Element '" + property + "' clicked.";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("click");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        PowerMockito.mockStatic(ExpectedConditions.class);
//        PowerMockito.mockStatic(By.class);
//
//        when(selenium.getDriver()).thenReturn(driver);
//        when(By.id(anyString())).thenReturn(by);
//        PowerMockito.whenNew(WebDriverWait.class).withAnyArguments().thenReturn(webDriverWait);
//        when(ExpectedConditions.visibilityOfElementLocated(by)).thenReturn(expectedCondition);
//        when(fluentWait.until(ExpectedConditions.visibilityOfElementLocated(by))).thenReturn(element);
//        when(driver.findElement(by)).thenReturn(element);
//
//        this.seleniumService.doSeleniumActionClick(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionClickPropertyWhenElementNotFound() {
//        String object = "null";
//        String property = "id=test";
//        String msg = "Failed to click because could not find element '" + property + "'!";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("click");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        when(selenium.getDriver()).thenReturn(driver);
//        when(driver.findElement(By.id(anyString()))).thenThrow(new NoSuchElementException(""));
//
//        this.seleniumService.doSeleniumActionClick(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionClickWhenWebDriverException() {
//        String object = "id=test";
//        String property = "null";
//        String msg = "The test case is canceled due to lost connection to Selenium Server!";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("click");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        when(selenium.getDriver()).thenThrow(new WebDriverException());
//
//        this.seleniumService.doSeleniumActionClick(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    /**
//     * Action ClickAndWait
//     */
//    @Test
//    public void testDoActionClickWaitWhenSuccess() throws Exception {
//        String object = "id=test";
//        String property = "100";
//        String msg = "Element '" + object + "' clicked and waited " + property + " ms.";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("clickAndWait");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        PowerMockito.mockStatic(ExpectedConditions.class);
//        PowerMockito.mockStatic(By.class);
//
//        when(selenium.getDriver()).thenReturn(driver);
//        when(By.id(anyString())).thenReturn(by);
//        PowerMockito.whenNew(WebDriverWait.class).withAnyArguments().thenReturn(webDriverWait);
//        when(ExpectedConditions.visibilityOfElementLocated(by)).thenReturn(expectedCondition);
//        when(webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by))).thenReturn(element);
//        when(driver.findElement(by)).thenReturn(element);
//
//        this.seleniumService.doSeleniumActionClickWait(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    //TODO testDoActionClickWaitWhenInterruptedException
////    @Test
//    public void testDoActionClickWaitWhenInterruptedException() throws InterruptedException {
//        String object = "id=test";
//        String property = "100";
//        String msg = "Element '" + object + "' clicked but failed to wait '" + property + "' ms.";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("clickAndWait");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        when(selenium.getDriver()).thenReturn(driver);
//        when(driver.findElement(By.id(anyString()))).thenReturn(element);
////        doThrow(new InterruptedException()).when(Thread);
////        when(Thread.sleep(anyLong())).thenThrow(new InterruptedException());
//
//        this.seleniumService.doSeleniumActionClickWait(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionClickWaitWhenPropertyNotNumeric() throws Exception {
//        String object = "id=test";
//        String property = "dez";
//        String msg = "Failed to wait because '" + property + "' in not numeric!";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("clickAndWait");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        PowerMockito.mockStatic(ExpectedConditions.class);
//        PowerMockito.mockStatic(By.class);
//
//        when(selenium.getDriver()).thenReturn(driver);
//        when(By.id(anyString())).thenReturn(by);
//        PowerMockito.whenNew(WebDriverWait.class).withAnyArguments().thenReturn(webDriverWait);
//        when(ExpectedConditions.visibilityOfElementLocated(by)).thenReturn(expectedCondition);
//        when(webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by))).thenReturn(element);
//        when(driver.findElement(by)).thenReturn(element);
//
//        this.seleniumService.doSeleniumActionClickWait(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionClickWaitWhenElementNotFound() {
//        String object = "id=test";
//        String property = "5000";
//        String msg = "Failed to click because could not find element '" + object + "'!";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("clickAndWait");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        when(selenium.getDriver()).thenReturn(driver);
//        when(driver.findElement(By.id(anyString()))).thenThrow(new NoSuchElementException(""));
//
//        this.seleniumService.doSeleniumActionClickWait(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionClickWaitWhenObjectNullAndPropertyNull() {
//        String object = "null";
//        String property = "null";
//        String msg = "Object is 'null'. This is mandatory in order to perform the action click and wait.";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("clickAndWait");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        this.seleniumService.doSeleniumActionClick(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionClickWaitWhenObjectNotNullAndPropertyNULL() throws Exception {
//        String object = "id=test";
//        String property = "null";
//        String msg = "Element '" + object + "' clicked and waited for page to load";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("clickAndWait");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        PowerMockito.mockStatic(ExpectedConditions.class);
//        PowerMockito.mockStatic(By.class);
//
//        when(selenium.getDriver()).thenReturn(driver);
//        when(By.id(anyString())).thenReturn(by);
//        PowerMockito.whenNew(WebDriverWait.class).withAnyArguments().thenReturn(webDriverWait);
//        when(ExpectedConditions.visibilityOfElementLocated(by)).thenReturn(expectedCondition);
//        when(webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by))).thenReturn(element);
//        when(driver.findElement(by)).thenReturn(element);
//
//        this.seleniumService.doSeleniumActionClickWait(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionClickWaitWhenElementNotFoundAndPropertyNULL() {
//        String object = "id=test";
//        String property = "null";
//        String msg = "Failed to click because could not find element '" + object + "'!";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("clickAndWait");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        when(selenium.getDriver()).thenReturn(driver);
//        when(driver.findElement(By.id(anyString()))).thenThrow(new NoSuchElementException(""));
//
//        this.seleniumService.doSeleniumActionClickWait(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionClickWaitWhenWebDriverException() {
//        String object = "id=test";
//        String property = "null";
//        String msg = "The test case is canceled due to lost connection to Selenium Server!";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("clickAndWait");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        when(selenium.getDriver()).thenThrow(new WebDriverException());
//
//        this.seleniumService.doSeleniumActionClickWait(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//
//    @Test
//    public void testDoActionOpenURLWithBaseObjectNotNull() {
//        String object = "/test";
//        String property = "null";
//        String msg = "Opened URL '" + object + "'.";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("openUrlWithBase");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        when(selenium.getDriver()).thenReturn(driver);
//
//        this.seleniumService.doSeleniumActionOpenURLWithBase(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionOpenURLWithBaseObjectNullAndPropertyNotNull() {
//        String object = "null";
//        String property = "/test";
//        String msg = "Opened URL '" + property + "'.";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("openUrlWithBase");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        when(selenium.getDriver()).thenReturn(driver);
//
//        this.seleniumService.doSeleniumActionOpenURLWithBase(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//
//    @Test
//    public void testDoActionOpenURLWithBaseObjectNullAndPropertyNull() {
//        String object = "null";
//        String property = "null";
//        String msg = "Failed to open '" + object + "'.";
//
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setAction("openUrlWithBase");
//        tcsae.setObject(object);
//        tcsae.setProperty(property);
//
//        when(selenium.getDriver()).thenReturn(driver);
//
//        this.seleniumService.doSeleniumActionOpenURLWithBase(object, property);
//
//        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
//    }
//        
}
