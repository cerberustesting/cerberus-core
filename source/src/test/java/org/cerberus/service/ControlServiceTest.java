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

package org.cerberus.service;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.cerberus.entity.TestCaseStepActionControlExecution;
import org.cerberus.serviceEngine.impl.ControlService;
import org.cerberus.serviceEngine.impl.SeleniumService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.WebDriverException;
import org.springframework.test.context.ContextConfiguration;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/07/2013
 * @since 0.9.0
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class ControlServiceTest {

    @Mock
    private SeleniumService seleniumService;
    @InjectMocks
    private ControlService controlService;

    @Test
    public void testDoControlStringEqualWhenSuccess() {
        String property = "test";
        String value = "test";
        String msg = "'" + property + "' is equal to '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyStringEqual");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlStringEqualWhenFail() {
        String property = "test";
        String value = "test fail";
        String msg = "'" + value + "' is not equal to '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyStringEqual");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlStringDifferentWhenSuccess() {
        String property = "test";
        String value = "test success";
        String msg = "'" + value + "' is different from '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyStringDifferent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlStringDifferentWhenFail() {
        String property = "test";
        String value = "test";
        String msg = "'" + value + "' is not different from '" + property + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyStringDifferent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerGreaterWhenSuccess() {
        String property = "10";
        String value = "5";
        String msg = "'" + property + "' is greater than '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyIntegerGreater");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlIntegerGreaterWhenFail() {
        String property = "5";
        String value = "10";
        String msg = "'" + property + "' is not greater than '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyIntegerGreater");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerGreaterWhenPropertyNotNumeric() {
        String property = "ten";
        String value = "5";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyIntegerGreater");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerGreaterWhenValueNotNumeric() {
        String property = "10";
        String value = "five";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyIntegerGreater");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerMinorWhenSuccess() {
        String property = "5";
        String value = "10";
        String msg = "'" + property + "' is minor than '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyIntegerMinor");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlIntegerMinorWhenFail() {
        String property = "10";
        String value = "5";
        String msg = "'" + property + "' is not minor than '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyIntegerMinor");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerMinorWhenPropertyNotNumeric() {
        String property = "five";
        String value = "5";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyIntegerMinor");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerMinorWhenValueNotNumeric() {
        String property = "10";
        String value = "five";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyIntegerMinor");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

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
        tcsace.setFatal("Y");

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
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementPresentWhenWebDriverException() {
        String property = "id=test";
        String value = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server! Detailed error : .*";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        when(seleniumService.isElementPresent(anyString())).thenThrow(new WebDriverException());

        this.controlService.doControl(tcsace);

        Assert.assertTrue( tcsace.getControlResultMessage().getDescription().matches(msg));
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
        tcsace.setFatal("Y");

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
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementNotPresentWhenWebDriverException() {
        String property = "id=test";
        String value = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server! Detailed error : .*";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementNotPresent");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        when(seleniumService.isElementPresent(anyString())).thenThrow(new WebDriverException());

        this.controlService.doControl(tcsace);

        Assert.assertTrue( tcsace.getControlResultMessage().getDescription().matches(msg));
        Assert.assertEquals("CA", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }


    @Test
    public void testDoControlElementNotVisibleWhenSuccess() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '"+property+"' is present and not visible on the page.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementNotVisible");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");
        
        when(seleniumService.isElementPresent(anyString())).thenReturn(true);
        when(seleniumService.isElementNotVisible(anyString())).thenReturn(true);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlElementNotVisibleWhenFail() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is visible on the page.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementNotVisible");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        when(seleniumService.isElementPresent(anyString())).thenReturn(true);
        when(seleniumService.isElementNotVisible(anyString())).thenReturn(false);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementNotVisibleWhenPropertyNull() {
        String property = "null";
        String value = "id=test";
        String msg = "Object is 'null'. This is mandatory in order to perform the control verify element not visible";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementNotVisible");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementNotVisibleWhenWebDriverException() {
        String property = "id=test";
        String value = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server! Detailed error : .*";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementNotVisible");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        when(seleniumService.isElementPresent(anyString())).thenThrow(new WebDriverException());

        this.controlService.doControl(tcsace);

        Assert.assertTrue( tcsace.getControlResultMessage().getDescription().matches(msg));
        Assert.assertEquals("CA", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementInElementWhenValueIsNull() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '"+value+"' is not child of element '"+property+"'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementInElement");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }
    

    @Test
    public void testDoControlElementInElementWhenPropertyIsNull() {
        String property = "null";
        String value = "id=test";
        String msg = "Element '"+value+"' is not child of element '"+property+"'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementInElement");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementInElementWhenValueIsNotChildOfProperty() {
        String property = "id=parent";
        String value = "id=child";
        String msg = "Element '"+value+"' is not child of element '"+property+"'.";

        when(seleniumService.isElementInElement(property, value)).thenReturn(Boolean.FALSE);

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementInElement");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlElementInElementWhenValueIsChildOfProperty() {
        String property = "id=parent";
        String value = "id=child";
        String msg = "Element '"+value+"' in child of element '"+property+"'.";

        when(seleniumService.isElementInElement(property, value)).thenReturn(Boolean.TRUE);

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControlType("verifyElementInElement");
        tcsace.setControlProperty(property);
        tcsace.setControlValue(value);
        tcsace.setFatal("Y");

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }
}
