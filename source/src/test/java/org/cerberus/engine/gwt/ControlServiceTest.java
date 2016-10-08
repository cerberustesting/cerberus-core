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

package org.cerberus.engine.gwt;

import junit.framework.Assert;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.Identifier;
import org.cerberus.crud.entity.Session;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.appium.IAppiumService;
import org.cerberus.engine.gwt.impl.ControlService;
import org.cerberus.engine.execution.impl.IdentifierService;
import org.cerberus.engine.gwt.impl.PropertyService;
import org.cerberus.service.webdriver.impl.WebDriverService;
import org.cerberus.service.xmlunit.impl.XmlUnitService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
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
    private Session session;
    
    @Mock
    private WebDriverService webdriverService;
    
    @Mock
    private TestCaseExecution tCExecution;
    
    @Mock
    private Application application;
    
    @Mock
    private PropertyService propertyService;
    
    @Mock
    private IdentifierService identifierService;
    
    @Mock
    private IAppiumService appiumService;
    
    @InjectMocks
    private ControlService controlService;
    
    @Mock
    private XmlUnitService xmlUnitService;
    
    @Before
    public void before() {
    	 when(tCExecution.getApplicationObj()).thenReturn(application);
         when(application.getType()).thenReturn("GUI");
    }
    
    @Test
    public void testDoControlStringEqualWhenSuccess() {
        String property = "test";
        String value = "test";
        String msg = "'" + property + "' is equal to '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyStringEqual");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyStringEqual");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyStringDifferent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyStringDifferent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyIntegerGreater");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyIntegerGreater");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyIntegerGreater");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyIntegerGreater");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyIntegerMinor");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyIntegerMinor");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyIntegerMinor");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyIntegerMinor");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }
    
    @Test
    public void testDoControlIntegerEqualsWhenSuccess() {
        String property = "5";
        String value = "5";
        String msg = "'" + property + "' is equal to '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyIntegerEquals");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlIntegerEqualsWhenFail() {
        String property = "5";
        String value = "10";
        String msg = "'" + property + "' is not equal to '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyIntegerEquals");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerEqualsWhenPropertyNotNumeric() {
        String property = "five";
        String value = "5";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyIntegerEquals");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerEqualsWhenValueNotNumeric() {
        String property = "10";
        String value = "five";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyIntegerEquals");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }
    
    @Test
    public void testDoControlIntegerDifferentWhenSuccess() {
        String property = "5";
        String value = "10";
        String msg = "'" + property + "' is different from '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyIntegerDifferent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Test
    public void testDoControlIntegerDifferentWhenFail() {
        String property = "5";
        String value = "5";
        String msg = "'" + property + "' is not different from '" + value + "'.";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyIntegerDifferent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerDifferentWhenPropertyNotNumeric() {
        String property = "five";
        String value = "5";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyIntegerDifferent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Test
    public void testDoControlIntegerDifferentWhenValueNotNumeric() {
        String property = "10";
        String value = "five";
        String msg = "At least one of the Properties is not numeric, can not compare properties!";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyIntegerDifferent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Ignore
    @Test
    public void testDoControlElementPresentWhenSuccess() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is present on the page.";
        Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementPresent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        when(webdriverService.isElementPresent(session, identifier)).thenReturn(true);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Ignore
    @Test
    public void testDoControlElementPresentWhenFail() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is not present on the page.";
        Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementPresent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        when(webdriverService.isElementPresent(session, identifier)).thenReturn(false);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Ignore
    @Test
    public void testDoControlElementPresentWhenPropertyNull() {
        String property = "null";
        String value = "id=test";
        String msg = "Object is 'null'. This is mandatory in order to perform the control verify element present";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementPresent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Ignore
    @Test
    public void testDoControlElementPresentWhenWebDriverException() {
        String property = "id=test";
        String value = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server! Detailed error : .*";
Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");
        
        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementPresent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        tCExecution.setSession(session);
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);
        
        when(webdriverService.isElementPresent(tCExecution.getSession(), identifier)).thenThrow(new WebDriverException());

        this.controlService.doControl(tcsace);

        Assert.assertTrue( tcsace.getControlResultMessage().getDescription().matches(msg));
        Assert.assertEquals("CA", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Ignore
    @Test
    public void testDoControlElementNotPresentWhenSuccess() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is not present on the page.";
        Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementNotPresent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        when(webdriverService.isElementPresent(session, identifier)).thenReturn(false);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Ignore
    @Test
    public void testDoControlElementNotPresentWhenFail() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is present on the page.";
Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");
        
        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementNotPresent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        when(webdriverService.isElementPresent(session, identifier)).thenReturn(true);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Ignore
    @Test
    public void testDoControlElementNotPresentWhenPropertyNull() {
        String property = "null";
        String value = "id=test";
        String msg = "Object is 'null'. This is mandatory in order to perform the control verify element not present";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementNotPresent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Ignore
    @Test
    public void testDoControlElementNotPresentWhenWebDriverException() {
        String property = "id=test";
        String value = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server! Detailed error : .*";
Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");
        
        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementNotPresent");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        when(webdriverService.isElementPresent(session, identifier)).thenThrow(new WebDriverException());

        this.controlService.doControl(tcsace);

        Assert.assertTrue( tcsace.getControlResultMessage().getDescription().matches(msg));
        Assert.assertEquals("CA", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }


    @Ignore
    @Test
    public void testDoControlElementNotVisibleWhenSuccess() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '"+property+"' is present and not visible on the page.";
Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");
        
        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementNotVisible");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);
        
        when(webdriverService.isElementPresent(session, identifier)).thenReturn(true);
        when(webdriverService.isElementNotVisible(session, identifier)).thenReturn(true);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
    }

    @Ignore
    @Test
    public void testDoControlElementNotVisibleWhenFail() {
        String property = "id=test";
        String value = "null";
        String msg = "Element '" + property + "' is visible on the page.";
        Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementNotVisible");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        when(webdriverService.isElementPresent(session, identifier)).thenReturn(true);
        when(webdriverService.isElementNotVisible(session, identifier)).thenReturn(false);

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
        tcsace.setControl("verifyElementNotVisible");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Ignore
    @Test
    public void testDoControlElementNotVisibleWhenWebDriverException() {
        String property = "id=test";
        String value = "null";
        String msg = "The test case is canceled due to lost connection to Selenium Server! Detailed error : .*";
Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");
        
        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementNotVisible");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        when(webdriverService.isElementPresent(session, identifier)).thenThrow(new WebDriverException());

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
        tcsace.setControl("verifyElementInElement");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

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
        tcsace.setControl("verifyElementInElement");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Ignore
    @Test
    public void testDoControlElementInElementWhenValueIsNotChildOfProperty() {
        String property = "id=parent";
        String value = "id=child";
        String msg = "Element '"+value+"' is not child of element '"+property+"'.";
        Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");
        Identifier identifierValue = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test2");

        when(webdriverService.isElementInElement(session, identifier, identifierValue)).thenReturn(Boolean.FALSE);

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementInElement");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("KO", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }

    @Ignore
    @Test
    public void testDoControlElementInElementWhenValueIsChildOfProperty() {
        String property = "id=parent";
        String value = "id=child";
        String msg = "Element '"+value+"' in child of element '"+property+"'.";
        Identifier identifier = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test");
        Identifier identifierValue = new Identifier();
        identifier.setIdentifier("id");
        identifier.setLocator("test2");

        when(webdriverService.isElementInElement(session, identifier, identifierValue)).thenReturn(Boolean.TRUE);

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementInElement");
        tcsace.setValue1(property);
        tcsace.setValue2(value);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
        Assert.assertEquals("OK", tcsace.getReturnCode());
        Assert.assertEquals("Y", tcsace.getFatal());
    }
    
    @Test
    public void testVerifyElementEqualsWithNotCompatibleApplication() {
    	String xpath = "/foo/bar";
        String expectedElement = "<bar>baz</bar>";

        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
        tcsace.setControl("verifyElementEquals");
        tcsace.setValue1(xpath);
        tcsace.setValue2(expectedElement);
        tcsace.setFatal("Y");
        TestCaseStepExecution tcse = new TestCaseStepExecution();
        tcse.settCExecution(tCExecution);
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        tcsae.setTestCaseStepExecution(tcse);
        tcsace.setTestCaseStepActionExecution(tcsae);

        this.controlService.doControl(tcsace);

        Assert.assertEquals(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION.getCode(), tcsace.getControlResultMessage().getCode());
    }
    
//    @Test
//    public void testVerifyElementEqualsWithElementPresent() {
//        String xpath = "/foo/bar";
//        String expectedElement = "<bar>baz</bar>";
//        String xmlResponse = "<bar>bar</bar>";
//        String msg = "Element in path '" + xpath + "' is equal to '" + expectedElement + "'.";
//
////        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
////        tcsace.setControlType("verifyElementEquals");
////        tcsace.setControlProperty(xpath);
////        tcsace.setControlValue(expectedElement);
////        tcsace.setFatal("Y");
////        TestCaseStepExecution tcse = new TestCaseStepExecution();
////        tcse.settCExecution(tCExecution);
////        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
////        tcsae.setTestCaseStepExecution(tcse);
////        tcsace.setTestCaseStepActionExecution(tcsae);
//        //when(application.getType()).thenReturn("WS");
//        when(xmlUnitService.isElementEquals(xmlResponse, xpath, expectedElement)).thenReturn(Boolean.TRUE);
//
//        this.controlService.doControl(tcsace);
//
//        Assert.assertEquals(MessageEventEnum.CONTROL_SUCCESS_ELEMENTEQUALS.getCode(), tcsace.getControlResultMessage().getCode());
//        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }

//    @Test
//    public void testVerifyElementEqualsWithElementAbsent() {
//    	String xpath = "/foo/bar";
//        String expectedElement = "<bar>baz</bar>";
//        String msg = "Element in path '" + xpath + "' is not equal to '" + expectedElement + "'.";
//        
//        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//        tcsace.setControlType("verifyElementEquals");
//        tcsace.setControlProperty(xpath);
//        tcsace.setControlValue(expectedElement);
//        tcsace.setFatal("Y");
//        TestCaseStepExecution tcse = new TestCaseStepExecution();
//        tcse.settCExecution(tCExecution);
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setTestCaseStepExecution(tcse);
//        tcsace.setTestCaseStepActionExecution(tcsae);
//        
//        when(application.getType()).thenReturn("WS");
//        when(xmlUnitService.isElementEquals(tCExecution, xpath, expectedElement)).thenReturn(Boolean.FALSE);
//
//        this.controlService.doControl(tcsace);
//
//        Assert.assertEquals(MessageEventEnum.CONTROL_FAILED_ELEMENTEQUALS.getCode(), tcsace.getControlResultMessage().getCode());
//        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyElementDifferentWithNotCompatibleApplication() {
//    	String xpath = "/foo/bar";
//        String expectedElement = "<bar>baz</bar>";
//
//        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//        tcsace.setControlType("verifyElementDifferent");
//        tcsace.setControlProperty(xpath);
//        tcsace.setControlValue(expectedElement);
//        tcsace.setFatal("Y");
//        TestCaseStepExecution tcse = new TestCaseStepExecution();
//        tcse.settCExecution(tCExecution);
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setTestCaseStepExecution(tcse);
//        tcsace.setTestCaseStepActionExecution(tcsae);
//
//        this.controlService.doControl(tcsace);
//
//        Assert.assertEquals(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION.getCode(), tcsace.getControlResultMessage().getCode());
//    }
//    
//    @Test
//    public void testVerifyElementDifferentWithElementDifferent() {
//    	String xpath = "/foo/bar";
//        String expectedElement = "<bar>baz</bar>";
//        String msg = "Element in path '" + xpath + "' is different from '" + expectedElement + "'.";
//        
//        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//        tcsace.setControlType("verifyElementDifferent");
//        tcsace.setControlProperty(xpath);
//        tcsace.setControlValue(expectedElement);
//        tcsace.setFatal("Y");
//        TestCaseStepExecution tcse = new TestCaseStepExecution();
//        tcse.settCExecution(tCExecution);
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setTestCaseStepExecution(tcse);
//        tcsace.setTestCaseStepActionExecution(tcsae);
//        
//        when(application.getType()).thenReturn("WS");
//        when(xmlUnitService.isElementEquals(tCExecution, xpath, expectedElement)).thenReturn(Boolean.FALSE);
//
//        this.controlService.doControl(tcsace);
//
//        Assert.assertEquals(MessageEventEnum.CONTROL_SUCCESS_ELEMENTDIFFERENT.getCode(), tcsace.getControlResultMessage().getCode());
//        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyElementDifferentWithElementEquals() {
//    	String xpath = "/foo/bar";
//        String expectedElement = "<bar>baz</bar>";
//        String msg = "Element in path '" + xpath + "' is not different from '" + expectedElement + "'.";
//        
//        TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//        tcsace.setControlType("verifyElementDifferent");
//        tcsace.setControlProperty(xpath);
//        tcsace.setControlValue(expectedElement);
//        tcsace.setFatal("Y");
//        TestCaseStepExecution tcse = new TestCaseStepExecution();
//        tcse.settCExecution(tCExecution);
//        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//        tcsae.setTestCaseStepExecution(tcse);
//        tcsace.setTestCaseStepActionExecution(tcsae);
//        
//        when(application.getType()).thenReturn("WS");
//        when(xmlUnitService.isElementEquals(tCExecution, xpath, expectedElement)).thenReturn(Boolean.TRUE);
//
//        this.controlService.doControl(tcsace);
//
//        Assert.assertEquals(MessageEventEnum.CONTROL_FAILED_ELEMENTDIFFERENT.getCode(), tcsace.getControlResultMessage().getCode());
//        Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyTextInElementWhenElementExistsAndTextEquals() {
//		String xpath = "/foo/bar";
//		String actual = "foo";
//		String expected = "foo";
//		String msg = "Element '" + xpath + "' with value '" + actual + "' is equal to '" + expected + "'.";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("WS");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(true);
//		when(xmlUnitService.getFromXml("uuid", null, xpath)).thenReturn(actual);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_SUCCESS_TEXTINELEMENT.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyTextInElementWhenElementExistsAndTextNotEquals() {
//		String xpath = "/foo/bar";
//		String actual = "foo";
//		String expected = "bar";
//		String msg = "Element '" + xpath + "' with value '" + actual + "' is not equal to '" + expected + "'.";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("WS");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(true);
//		when(xmlUnitService.getFromXml("uuid", null, xpath)).thenReturn(actual);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyTextInElementWhenElementExistsAndTextIsNull() {
//		String xpath = "/foo/bar";
//		String actual = null;
//		String expected = "bar";
//		String msg = "Found Element '" + xpath + "' but can not find text or value.";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("WS");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(true);
//		when(xmlUnitService.getFromXml("uuid", null, xpath)).thenReturn(actual);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT_NULL.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyTextInElementWhenElementNotExists() {
//		String xpath = "/foo/bar";
//		String expected = "bar";
//		String msg = "Failed to verifyTextInElement because could not find element '" + xpath + "'";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("WS");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(false);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_FAILED_TEXTINELEMENT_NO_SUCH_ELEMENT.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    public void testVerifyTextInElementWithNotSupportedApplication() {
//		String xpath = "/foo/bar";
//		String actual = "foo";
//		String expected = "bar";
//		String msg = "Not executed because Control 'verifyTextInElement' is not supported for application type 'UNKNOWN'.";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("UNKNOWN");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(true);
//		when(xmlUnitService.getFromXml("uuid", null, xpath)).thenReturn(actual);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyTextNotInElementWhenElementExistsAndTextDifferent() {
//		String xpath = "/foo/bar";
//		String actual = "foo";
//		String expected = "bar";
//		String msg = "Element '" + xpath + "' with value '" + actual + "' is different than '" + expected + "'.";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextNotInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("WS");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(true);
//		when(xmlUnitService.getFromXml("uuid", null, xpath)).thenReturn(actual);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_SUCCESS_TEXTNOTINELEMENT.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyTextNotInElementWhenElementExistsAndTextNotDifferent() {
//		String xpath = "/foo/bar";
//		String actual = "foo";
//		String expected = "foo";
//		String msg = "Element '" + xpath + "' with value '" + actual + "' is not different than '" + expected + "'.";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextNotInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("WS");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(true);
//		when(xmlUnitService.getFromXml("uuid", null, xpath)).thenReturn(actual);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_FAILED_TEXTNOTINELEMENT.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyTextNotInElementWhenElementExistsAndTextIsNull() {
//		String xpath = "/foo/bar";
//		String actual = null;
//		String expected = "bar";
//		String msg = "Found Element '" + xpath + "' but can not find text or value.";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextNotInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("WS");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(true);
//		when(xmlUnitService.getFromXml("uuid", null, xpath)).thenReturn(actual);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_FAILED_TEXTNOTINELEMENT_NULL.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    @Test
//    public void testVerifyTextNotInElementWhenElementNotExists() {
//		String xpath = "/foo/bar";
//		String expected = "bar";
//		String msg = "Failed to verifyTextNotInElement because could not find element '" + xpath + "'";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextNotInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("WS");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(false);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_FAILED_TEXTNOTINELEMENT_NO_SUCH_ELEMENT.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
//    public void testVerifyTextNotInElementWithNotSupportedApplication() {
//		String xpath = "/foo/bar";
//		String actual = "foo";
//		String expected = "bar";
//		String msg = "Not executed because Control 'verifyTextNotInElement' is not supported for application type 'UNKNOWN'.";
//
//		TestCaseStepActionControlExecution tcsace = new TestCaseStepActionControlExecution();
//		tcsace.setControlType("verifyTextNotInElement");
//		tcsace.setControlProperty(xpath);
//		tcsace.setControlValue(expected);
//		tcsace.setFatal("Y");
//		TestCaseStepExecution tcse = new TestCaseStepExecution();
//		tcse.settCExecution(tCExecution);
//		TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
//		tcsae.setTestCaseStepExecution(tcse);
//		tcsace.setTestCaseStepActionExecution(tcsae);
//
//		when(application.getType()).thenReturn("UNKNOWN");
//		when(tCExecution.getExecutionUUID()).thenReturn("uuid");
//		when(xmlUnitService.isElementPresent(tCExecution, xpath)).thenReturn(true);
//		when(xmlUnitService.getFromXml("uuid", null, xpath)).thenReturn(actual);
//
//		this.controlService.doControl(tcsace);
//
//		Assert.assertEquals(MessageEventEnum.CONTROL_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION.getCode(), tcsace.getControlResultMessage().getCode());
//		Assert.assertEquals(msg, tcsace.getControlResultMessage().getDescription());
//    }
//    
}
