package org.cerberus.service;

import org.cerberus.entity.Selenium;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.serviceEngine.impl.SeleniumService;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebElement;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 17/01/2013
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class SeleniumServiceTest {

    @Mock
    private WebDriver driver;

    @Mock
    private Selenium selenium;

    @Mock
    private RemoteWebElement element;

    @InjectMocks
    private SeleniumService seleniumService;


    /**
     * Action Click
     */
    @Test
    public void testDoActionClickWhenObjectNullAndPropertyNull() {
        String object = "null";
        String property = "null";
        String msg = "Object and Property are ‘null’. At least one is mandatory in order to perform the action click.";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("click");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickObjectWhenSuccess() {
        String object = "id=test";
        String property = "null";
        String msg = "Element '" + object + "' clicked.";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("click");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenReturn(element);

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickObjectWhenElementNotFound() {
        String object = "id=test";
        String property = "null";
        String msg = "Failed to click because could not find element '" + object + "'!";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("click");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenThrow(new NoSuchElementException(""));

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickPropertyWhenSuccess() {
        String object = "null";
        String property = "id=test";
        String msg = "Element '" + property + "' clicked.";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("click");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenReturn(element);

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickPropertyWhenElementNotFound() {
        String object = "null";
        String property = "id=test";
        String msg = "Failed to click because could not find element '" + property + "'!";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("click");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenThrow(new NoSuchElementException(""));

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickWhenWebDriverException() {
        String object = "id=test";
        String property = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server!";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("click");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenThrow(new WebDriverException());

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    /**
     * Action ClickAndWait
     */
    @Test
    public void testDoActionClickWaitWhenSuccess() {
        String object = "id=test";
        String property = "100";
        String msg = "Element '" + object + "' clicked and waited " + property + " ms.";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("clickAndWait");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenReturn(element);

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    //TODO testDoActionClickWaitWhenInterruptedException
//    @Test
    public void testDoActionClickWaitWhenInterruptedException() throws InterruptedException {
        String object = "id=test";
        String property = "100";
        String msg = "Element '" + object + "' clicked but failed to wait '" + property + "' ms.";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("clickAndWait");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenReturn(element);
//        doThrow(new InterruptedException()).when(Thread);
//        when(Thread.sleep(anyLong())).thenThrow(new InterruptedException());

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickWaitWhenPropertyNotNumeric() {
        String object = "id=test";
        String property = "dez";
        String msg = "Failed to wait because '" + property + "' in not numeric!";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("clickAndWait");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenReturn(element);

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickWaitWhenElementNotFound() {
        String object = "id=test";
        String property = "5000";
        String msg = "Failed to click because could not find element '" + object + "'!";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("clickAndWait");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenThrow(new NoSuchElementException(""));

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickWaitWhenObjectNullAndPropertyNull() {
        String object = "null";
        String property = "null";
        String msg = "Object is 'null'. This is mandatory in order to perform the action click and wait.";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("clickAndWait");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickWaitWhenObjectNotNullAndPropertyNULL() {
        String object = "id=test";
        String property = "null";
        String msg = "Element '" + object + "' clicked and waited for page to load";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("clickAndWait");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenReturn(element);

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickWaitWhenElementNotFoundAndPropertyNULL() {
        String object = "id=test";
        String property = "null";
        String msg = "Failed to click because could not find element '" + object + "'!";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("clickAndWait");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenReturn(driver);
        when(driver.findElement(By.id(anyString()))).thenThrow(new NoSuchElementException(""));

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

    @Test
    public void testDoActionClickWaitWhenWebDriverException() {
        String object = "id=test";
        String property = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server!";

        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setAction("clickAndWait");
        tcsae.setObject(object);
        tcsae.setProperty(property);

        when(selenium.getDriver()).thenThrow(new WebDriverException());

        this.seleniumService.doAction(tcsae);

        Assert.assertEquals(msg, tcsae.getActionResultMessage().getDescription());
    }

}
