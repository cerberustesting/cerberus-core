package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseStepActionControlExecution;
import com.redcats.tst.serviceEngine.impl.ControlService;
import com.redcats.tst.serviceEngine.impl.SeleniumService;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.WebDriverException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/07/2013
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlServiceTest {

    @Mock
    private SeleniumService seleniumService;

    @InjectMocks
    private ControlService controlService;

    @Test
    public void testDoControlPropertyEqualWhenSuccess() {
        String property = "test";
        String value = "test";
        String msg = "'" + property + "' is equal to '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyEqual");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlPropertyEqualWhenFail() {
        String property = "test";
        String value = "test fail";
        String msg = "'" + value + "' is not equal to '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyEqual");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlPropertyDifferentWhenSuccess() {
        String property = "test";
        String value = "test success";
        String msg = "'" + value + "' is different from '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyDifferent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlPropertyDifferentWhenFail() {
        String property = "test";
        String value = "test";
        String msg = "'" + value + "' is not different from '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyDifferent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlPropertyGreaterWhenSuccess() {
        String property = "5";
        String value = "10";
        String msg = "'" + value + "' is greater than '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyGreater");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlPropertyGreaterWhenFail() {
        String property = "10";
        String value = "5";
        String msg = "'" + value + "' is not greater than '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyGreater");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlPropertyGreaterWhenPropertyNotNumeric() {
        String property = "5";
        String value = "ten";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyGreater");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlPropertyGreaterWhenValueNotNumeric() {
        String property = "five";
        String value = "10";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyGreater");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlPropertyMinorWhenSuccess() {
        String property = "10";
        String value = "5";
        String msg = "'" + value + "' is minor than '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyMinor");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlPropertyMinorWhenFail() {
        String property = "5";
        String value = "10";
        String msg = "'" + value + "' is not minor than '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyMinor");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlPropertyMinorWhenPropertyNotNumeric() {
        String property = "10";
        String value = "five";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyMinor");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlPropertyMinorWhenValueNotNumeric() {
        String property = "10";
        String value = "five";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyPropertyGreater");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementPresentWhenSuccess() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is present on the page.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        when(seleniumService.isElementPresent(anyString())).thenReturn(true);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlElementPresentWhenFail() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is not present on the page.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        when(seleniumService.isElementPresent(anyString())).thenReturn(false);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementPresentWhenPropertyNull() {
        String property = "null";
        String value = "id=test";
        String msg = "Object is 'null'. This is mandatory in order to perform the control verify element present";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementPresentWhenWebDriverException() {
        String property = "id=test";
        String value = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        when(seleniumService.isElementPresent(anyString())).thenThrow(new WebDriverException());

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("CA", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementNotPresentWhenSuccess() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is not present on the page.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementNotPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        when(seleniumService.isElementPresent(anyString())).thenReturn(false);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlElementNotPresentWhenFail() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is present on the page.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementNotPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        when(seleniumService.isElementPresent(anyString())).thenReturn(true);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementNotPresentWhenPropertyNull() {
        String property = "null";
        String value = "id=test";
        String msg = "Object is 'null'. This is mandatory in order to perform the control verify element not present";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementNotPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementNotPresentWhenWebDriverException() {
        String property = "id=test";
        String value = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementNotPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        when(seleniumService.isElementPresent(anyString())).thenThrow(new WebDriverException());

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("CA", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }
}
