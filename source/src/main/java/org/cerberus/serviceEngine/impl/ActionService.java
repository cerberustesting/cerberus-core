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
package org.cerberus.serviceEngine.impl;

import java.util.Date;
import org.apache.log4j.Level;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.log.MyLogger;
import org.cerberus.serviceEngine.IActionService;
import org.cerberus.serviceEngine.IPropertyService;
import org.cerberus.serviceEngine.ISeleniumService;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class ActionService implements IActionService{
    
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private ISeleniumService seleniumService;
    
    
    @Override
    public TestCaseStepActionExecution doAction(TestCaseStepActionExecution testCaseStepActionExecution) {
        /**
         * Decode the 2 fields property and values before doing the control.
         */
        if (testCaseStepActionExecution.getObject().contains("%")) {
            String decodedValue = propertyService.decodeValue(testCaseStepActionExecution.getObject(), testCaseStepActionExecution.getTestCaseExecutionDataList(), testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution());
            testCaseStepActionExecution.setObject(decodedValue);
        }

        /**
         * Timestamp starts after the decode. TODO protect when property is
         * null.
         */
        testCaseStepActionExecution.setStart(new Date().getTime());

        String object = testCaseStepActionExecution.getObject();
        String property = testCaseStepActionExecution.getProperty();
        String propertyName = testCaseStepActionExecution.getPropertyName();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Doing Action : " + testCaseStepActionExecution.getAction() + " with object : " + object + " and property : " + property);

        MessageEvent res;

        String applicationType = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType();
        //TODO On JDK 7 implement switch with string
        if (testCaseStepActionExecution.getAction().equals("click")) {
            res = this.doActionClick(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("clickAndWait")) {
            res = this.doActionClickWait(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("doubleClick")) {
            res = this.doActionDoubleClick(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("enter")) {
            res = this.doActionKeyPress(applicationType, object, "RETURN");

        } else if (testCaseStepActionExecution.getAction().equals("keypress")) {
            res = this.doActionKeyPress(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseOver")) {
            res = this.doActionMouseOver(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseOverAndWait")) {
            res = this.doActionMouseOverAndWait(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("openUrlWithBase")) {
            res = this.doActionOpenURLWithBase(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("openUrlLogin")) {
            testCaseStepActionExecution.setObject(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getCountryEnvironmentApplication().getUrlLogin());
            res = this.doActionUrlLogin(applicationType);

        } else if (testCaseStepActionExecution.getAction().equals("select")) {
            res = this.doActionSelect(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("selectAndWait")) {
            res = this.doActionSelect(applicationType, object, property);
            this.doActionWait(applicationType, StringUtil.NULL, StringUtil.NULL);

        } else if (testCaseStepActionExecution.getAction().equals("focusToIframe")) {
            res = this.doActionFocusToIframe(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("focusDefaultIframe")) {
            res = this.doActionFocusDefaultIframe(applicationType);

        } else if (testCaseStepActionExecution.getAction().equals("type")) {
            res = this.doActionType(applicationType, object, property, propertyName);

        } else if (testCaseStepActionExecution.getAction().equals("wait")) {
            res = this.doActionWait(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseDown")) {
            res = this.doActionMouseDown(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("mouseUp")) {
            res = this.doActionMouseUp(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("switchToWindow")) {
            res = this.doActionSwitchToWindow(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("manageDialog")) {
            res = this.doActionManageDialog(applicationType, object, property);

        } else if (testCaseStepActionExecution.getAction().equals("calculateProperty")) {
            res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_PROPERTYCALCULATED);
            res.setDescription(res.getDescription().replaceAll("%PROP%", testCaseStepActionExecution.getPropertyName()));
        } else if (testCaseStepActionExecution.getAction().equals("takeScreenshot")) {
            res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TAKESCREENSHOT);
        } else {
            res = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKNOWNACTION);
            res.setDescription(res.getDescription().replaceAll("%ACTION%", testCaseStepActionExecution.getAction()));
        }

        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Result of the action : " + res.getCodeString() + " " + res.getDescription());
        testCaseStepActionExecution.setActionResultMessage(res);

        /**
         * Determine here the impact of the Action on the full test return code
         * from the ResultMessage of the Action.
         */
        testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(res.getMessage()));
        /**
         * Determine here if we stop the test from the ResultMessage of the
         * Action.
         */
        testCaseStepActionExecution.setStopExecution(res.isStopTest());

        testCaseStepActionExecution.setEnd(new Date().getTime());
        return testCaseStepActionExecution;
    }

    private MessageEvent doActionClick(String applicationType, String string1, String string2) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionClick(string1, string2);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "Click"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", string1));
        return message;
    }

    private MessageEvent doActionMouseDown(String applicationType, String string1, String string2) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionMouseDown(string1, string2);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "MouseDown"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", string1));
        return message;
    }

    private MessageEvent doActionMouseUp(String applicationType, String string1, String string2) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionMouseUp(string1, string2);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "MouseUp"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", string1));
        return message;
    }

    private MessageEvent doActionSwitchToWindow(String applicationType, String string1, String string2) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionSwitchToWindow(string1, string2);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "SwitchToWindow"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", string1));
        return message;
    }

    private MessageEvent doActionManageDialog(String applicationType, String string1, String string2) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionManageDialog(string1, string2);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "ManageDialog"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", string1));
        return message;
    }

    private MessageEvent doActionClickWait(String applicationType, String string1, String string2) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionClickWait(string1, string2);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "ClickAndWait"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", string1));
        return message;
    }

    private MessageEvent doActionDoubleClick(String applicationType, String string1, String string2) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionDoubleClick(string1, string2);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "ClickAndWait"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", string1));
        return message;
    }

    private MessageEvent doActionType(String applicationType, String html, String property, String propertyName) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionType(html, property, propertyName);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "ClickAndWait"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;
    }

    private MessageEvent doActionMouseOver(String applicationType, String html, String property) {
         MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionMouseOver(html, property);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "ClickAndWait"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;
    }

    private MessageEvent doActionMouseOverAndWait(String applicationType, String actionObject, String actionProperty) {
         MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionMouseOverAndWait(actionObject, actionProperty);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "ClickAndWait"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;
    }

    private MessageEvent doActionWait(String applicationType, String object, String property) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionWait(object, property);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "ClickAndWait"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;
    }

    private MessageEvent doActionKeyPress(String applicationType, String html, String property) {
        MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionKeyPress(html, property);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "KeyPress"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;
    }

    private MessageEvent doActionOpenURLWithBase(String applicationType, String value, String property) {
         MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionOpenURLWithBase(value, property);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "OpenURLWithBase"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;
    }

    private MessageEvent doActionSelect(String applicationType, String html, String property) {
         MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionSelect(html, property);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "Select"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;
    }

    private MessageEvent doActionUrlLogin(String applicationType) {
         MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionUrlLogin();
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "UrlLogin"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;
    }

    private MessageEvent doActionFocusToIframe(String applicationType, String object, String property) {
         MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionFocusToIframe(object, property);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "FocusToIframe"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;
    }

    private MessageEvent doActionFocusDefaultIframe(String applicationType) {
         MessageEvent message;
        if (applicationType.equalsIgnoreCase("GUI")){
        return seleniumService.doSeleniumActionFocusDefaultIframe();
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replaceAll("%ACTION%", "FocusDefaultIframe"));
        message.setDescription(message.getDescription().replaceAll("%APPLICATIONTYPE%", applicationType));
        return message;

    }

}
