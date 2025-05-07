/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.engine.gwt.impl;

import com.google.common.primitives.Ints;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.*;
import org.cerberus.core.crud.factory.IFactoryAppService;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionData;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionDataService;
import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.execution.IIdentifierService;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.engine.execution.IRobotServerService;
import org.cerberus.core.engine.execution.impl.RobotServerService;
import org.cerberus.core.engine.gwt.IActionService;
import org.cerberus.core.engine.gwt.IPropertyService;
import org.cerberus.core.engine.gwt.IVariableService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.appium.IAppiumService;
import org.cerberus.core.service.appium.SwipeAction;
import org.cerberus.core.service.appservice.IServiceService;
import org.cerberus.core.service.cerberuscommand.ICerberusCommand;
import org.cerberus.core.service.consolelog.IConsolelogService;
import org.cerberus.core.service.har.IHarService;
import org.cerberus.core.service.har.entity.NetworkTrafficIndex;
import org.cerberus.core.service.rest.IRestService;
import org.cerberus.core.service.robotextension.IFilemanagementService;
import org.cerberus.core.service.robotextension.ISikuliService;
import org.cerberus.core.service.robotextension.impl.SikuliService;
import org.cerberus.core.service.robotproxy.IRobotProxyService;
import org.cerberus.core.service.soap.ISoapService;
import org.cerberus.core.service.sql.ISQLService;
import org.cerberus.core.service.webdriver.IWebDriverService;
import org.cerberus.core.service.xmlunit.IXmlUnitService;
import org.cerberus.core.util.PDFUtil;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class ActionService implements IActionService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IWebDriverService webdriverService;
    @Autowired
    private ISoapService soapService;
    @Autowired
    private IRestService restService;
    @Autowired
    private IHarService harService;
    @Autowired
    private IAppServiceService appServiceService;
    @Autowired
    private IRecorderService recorderService;
    @Autowired
    private IXmlUnitService xmlUnitService;
    @Autowired
    private ISikuliService sikuliService;
    @Autowired
    private IFilemanagementService filemanagementService;
    @Autowired
    private IIdentifierService identifierService;
    @Autowired
    @Qualifier("AndroidAppiumService")
    private IAppiumService androidAppiumService;
    @Autowired
    @Qualifier("IOSAppiumService")
    private IAppiumService iosAppiumService;
    @Autowired
    @Qualifier("CerberusCommand")
    private ICerberusCommand cerberusCommand;
    @Autowired
    private ISQLService sqlService;
    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private IVariableService variableService;
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private IServiceService serviceService;
    @Autowired
    private IConsolelogService consolelogService;
    @Autowired
    private IRobotProxyService executorService;
    @Autowired
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;
    @Autowired
    private IFactoryAppService factoryAppService;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private IRobotServerService robotServerService;

    private static final Logger LOG = LogManager.getLogger(ActionService.class);
    private static final String MESSAGE_DEPRECATED = "[DEPRECATED]";

    @Override
    public TestCaseStepActionExecution doAction(TestCaseStepActionExecution actionExecution) {
        MessageEvent res;
        TestCaseExecution execution = actionExecution.getTestCaseStepExecution().gettCExecution();
        AnswerItem<String> answerDecode = new AnswerItem<>();

        // Empty Execution values depending of the action.
        actionExecution = cleanValues(actionExecution);

        /**
         * Decode the step action description, value1, value2 and value3
         */
        try {

            execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

            answerDecode = variableService.decodeStringCompletly(actionExecution.getDescription(),
                    execution, actionExecution, false);
            actionExecution.setDescription(answerDecode.getItem());

            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the action result.
                actionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Description"));
                actionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                actionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                actionExecution.setEnd(new Date().getTime());
                LOG.debug("Action interupted due to decode 'Description' Error.");
                return actionExecution;
            }
        } catch (CerberusEventException cex) {
            actionExecution.setActionResultMessage(cex.getMessageError());
            actionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            actionExecution.setEnd(new Date().getTime());
            return actionExecution;
        }

        /**
         * Decode the object field before doing the action.
         */
        try {

            execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

            answerDecode = variableService.decodeStringCompletly(actionExecution.getValue1(), execution, actionExecution, false);
            actionExecution.setValue1(answerDecode.getItem());

            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the action result.
                actionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Value1"));
                actionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                actionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                actionExecution.setEnd(new Date().getTime());
                LOG.debug("Action interupted due to decode 'Action Value1' Error.");
                return actionExecution;
            }
        } catch (CerberusEventException cex) {
            actionExecution.setActionResultMessage(cex.getMessageError());
            actionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            actionExecution.setEnd(new Date().getTime());
            return actionExecution;
        }

        try {

            execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

            answerDecode = variableService.decodeStringCompletly(actionExecution.getValue2(), execution, actionExecution, false);
            actionExecution.setValue2(answerDecode.getItem());

            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the action result.
                actionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Value2"));
                actionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                actionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                actionExecution.setEnd(new Date().getTime());
                LOG.debug("Action interupted due to decode 'Action Value2' Error.");
                return actionExecution;
            }
        } catch (CerberusEventException cex) {
            actionExecution.setActionResultMessage(cex.getMessageError());
            actionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            actionExecution.setEnd(new Date().getTime());
            return actionExecution;
        }

        try {

            execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

            answerDecode = variableService.decodeStringCompletly(actionExecution.getValue3(), execution, actionExecution, false);
            actionExecution.setValue3(answerDecode.getItem());

            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the action result.
                actionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Value3"));
                actionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                actionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                actionExecution.setEnd(new Date().getTime());
                LOG.debug("Action interupted due to decode 'Action Value3' Error.");
                return actionExecution;
            }
        } catch (CerberusEventException cex) {
            actionExecution.setActionResultMessage(cex.getMessageError());
            actionExecution.setExecutionResultMessage(new MessageGeneral(cex.getMessageError().getMessage()));
            actionExecution.setEnd(new Date().getTime());
            return actionExecution;
        }

        /**
         * Timestamp starts after the decode.
         */
        actionExecution.setStart(new Date().getTime());

        String value1 = actionExecution.getValue1();
        String value2 = actionExecution.getValue2();
        String value3 = actionExecution.getValue3();
        String propertyName = actionExecution.getPropertyName();
        LOG.debug("Doing Action : " + actionExecution.getAction() + " with value1 : " + value1 + " and value2 : " + value2 + " and value3 : " + value3);

        // When starting a new action, we reset the property list that was already calculated.
        execution.setRecursiveAlreadyCalculatedPropertiesList(new ArrayList<>());

        // Define Timeout
        HashMap<String, String> optionsMap = robotServerService.getMapFromOptions(actionExecution.getOptions());
        if (optionsMap.containsKey(RobotServerService.OPTIONS_TIMEOUT_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX).isEmpty()) {
            Optional<Integer> timeoutOptionValue = Optional.ofNullable(Ints.tryParse(optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX)));
            if (timeoutOptionValue.isPresent()) {
                robotServerService.setOptionsTimeout(execution.getSession(), timeoutOptionValue.get());
            } else {
                //TODO return a message alerting about the failed cast
                LOG.debug("failed to parse option value : {}", optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX));
            }
        }
        if (optionsMap.containsKey(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX).isEmpty()) {
            Optional<Integer> highlightOptionValue = Optional.ofNullable(Ints.tryParse(optionsMap.get(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX)));
            if (highlightOptionValue.isPresent()) {
                robotServerService.setOptionsHighlightElement(execution.getSession(), highlightOptionValue.get());
            } else {
                //TODO return a message alerting about the failed cast
                LOG.debug("failed to parse option value : {}", optionsMap.get(RobotServerService.OPTIONS_TIMEOUT_SYNTAX));
            }
        }
        if (optionsMap.containsKey(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX).isEmpty()) {
            String minSimilarity = optionsMap.get(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX);
            robotServerService.setOptionsMinSimilarity(execution.getSession(), minSimilarity);
        }
        if (optionsMap.containsKey(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX) && !optionsMap.get(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX).isEmpty()) {
            String typeDelay = optionsMap.get(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX);
            robotServerService.setOptionsTypeDelay(execution.getSession(), typeDelay);
        }

        // Record picture= files at action level.
        Identifier identifier = identifierService.convertStringToIdentifier(value1);
        if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE) && !StringUtil.isEmptyOrNull(identifier.getLocator())) {
            LOG.debug("Saving Image 1 on Action : " + identifier.getLocator());
            actionExecution.addFileList(recorderService.recordPicture(actionExecution, -1, identifier.getLocator(), "value1"));
        }
        identifier = identifierService.convertStringToIdentifier(value2);
        if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE) && !StringUtil.isEmptyOrNull(identifier.getLocator())) {
            LOG.debug("Saving Image 2 on Action : " + identifier.getLocator());
            actionExecution.addFileList(recorderService.recordPicture(actionExecution, -1, identifier.getLocator(), "value2"));
        }

        /**
         * Wait in ms before the action.
         */
        if (actionExecution.getWaitBefore() > 0) {
            try {
                Thread.sleep(Long.parseLong(String.valueOf(actionExecution.getWaitBefore())));
            } catch (InterruptedException ex) {
                LOG.error("Exception when waiting before action. {}-{}-{}", execution.getId(), actionExecution.getStepId(), actionExecution.getId(), ex);
            }
        }

        /**
         * Route the actions to the correct method.
         */
        try {
            switch (actionExecution.getAction()) {
                case TestCaseStepAction.ACTION_CLICK:
                    res = this.doActionClick(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_LONGPRESS:
                    res = this.doActionLongPress(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MOUSELEFTBUTTONPRESS:
                    res = this.doActionMouseLeftButtonPress(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MOUSELEFTBUTTONRELEASE:
                    res = this.doActionMouseLeftButtonRelease(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MOUSEMOVE:
                    res = this.doActionMouseMove(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_DOUBLECLICK:
                    res = this.doActionDoubleClick(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_RIGHTCLICK:
                    res = this.doActionRightClick(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MOUSEOVER:
                    res = this.doActionMouseOver(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_FOCUSTOIFRAME:
                    res = this.doActionFocusToIframe(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_FOCUSDEFAULTIFRAME:
                    res = this.doActionFocusDefaultIframe(execution);
                    break;
                case TestCaseStepAction.ACTION_SWITCHTOWINDOW:
                    res = this.doActionSwitchToWindow(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_SWITCHTOCONTEXT:
                    res = this.doActionSwitchToContext(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_MANAGEDIALOG:
                    res = this.doActionManageDialog(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_MANAGEDIALOGKEYPRESS:
                    res = this.doActionManageDialogKeyPress(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_OPENURLWITHBASE:
                    res = this.doActionOpenURL(execution, value1, value2, true);
                    break;
                case TestCaseStepAction.ACTION_OPENURLLOGIN:
                    actionExecution.setValue1(actionExecution.getTestCaseStepExecution().gettCExecution().getCountryEnvApplicationParam().getUrlLogin());
                    res = this.doActionUrlLogin(execution);
                    break;
                case TestCaseStepAction.ACTION_OPENURL:
                    res = this.doActionOpenURL(execution, value1, value2, false);
                    break;
                case TestCaseStepAction.ACTION_REFRESHCURRENTPAGE:
                    res = this.doActionRefreshCurrentPage(execution);
                    break;
                case TestCaseStepAction.ACTION_RETURNPREVIOUSPAGE:
                    res = this.doActionReturnPreviousPage(execution);
                    break;
                case TestCaseStepAction.ACTION_FORWARDNEXTPAGE:
                    res = this.doActionForwardNextPage(execution);
                    break;
                case TestCaseStepAction.ACTION_EXECUTEJS:
                    res = this.doActionExecuteJS(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_EXECUTECOMMAND:
                    res = this.doActionExecuteCommand(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_EXECUTECERBERUSCOMMAND:
                    res = this.doActionExecuteCerberusCommand(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_OPENAPP:
                    res = this.doActionOpenApp(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_CLOSEAPP:
                    res = this.doActionCloseApp(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_DRAGANDDROP:
                    res = this.doActionDragAndDrop(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_SELECT:
                    res = this.doActionSelect(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_KEYPRESS:
                    res = this.doActionKeyPress(execution, value1, value2, value3);
                    break;
                case TestCaseStepAction.ACTION_TYPE:
                    res = this.doActionType(execution, value1, value2, propertyName);
                    break;
                case TestCaseStepAction.ACTION_CLEARFIELD:
                    res = this.doActionClearField(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_HIDEKEYBOARD:
                    res = this.doActionHideKeyboard(execution);
                    break;
                case TestCaseStepAction.ACTION_SWIPE:
                    res = this.doActionSwipe(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_SCROLLTO:
                    res = this.doActionScrollTo(execution, value1, value2, value3);
                    break;
                case TestCaseStepAction.ACTION_INSTALLAPP:
                    res = this.doActionInstallApp(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_REMOVEAPP:
                    res = this.doActionRemoveApp(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_WAIT:
                    res = this.doActionWait(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_WAITVANISH:
                    res = this.doActionWaitVanish(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_WAITNETWORKTRAFFICIDLE:
                    res = this.doActionWaitNetworkTrafficIdle(execution);
                    break;
                case TestCaseStepAction.ACTION_CALLSERVICE:
                    res = this.doActionCallService(actionExecution, value1, value2, value3);
                    break;
                case TestCaseStepAction.ACTION_EXECUTESQLUPDATE:
                    res = this.doActionExecuteSQLUpdate(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_EXECUTESQLSTOREPROCEDURE:
                    res = this.doActionExecuteSQLStoredProcedure(execution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_CALCULATEPROPERTY:
                    res = this.doActionCalculateProperty(execution, actionExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_SETNETWORKTRAFFICCONTENT:
                    res = this.doActionSetNetworkTrafficContent(execution, actionExecution, value1, value2);
                    break;
                case TestCaseStepAction.ACTION_INDEXNETWORKTRAFFIC:
                    res = this.doActionIndexNetworkTraffic(execution, actionExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_SETCONSOLECONTENT:
                    res = this.doActionSetConsoleContent(execution, actionExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_SETSERVICECALLCONTENT:
                    res = this.doActionSetServiceCallContent(execution, actionExecution);
                    break;
                case TestCaseStepAction.ACTION_SETCONTENT:
                    res = this.doActionSetContent(execution, actionExecution, value1);
                    break;
                case TestCaseStepAction.ACTION_CLEANROBOTFILE:
                    res = this.doActionCleanRobotFile(execution, value1);
                    break;
                case TestCaseStepAction.ACTION_UPLOADROBOTFILE:
                    res = this.doActionUploadRobotFile(execution, value1, value2, value3);
                    break;
                case TestCaseStepAction.ACTION_GETROBOTFILE:
                    res = this.doActionGetRobotFile(execution, actionExecution, value1, value2, value3);
                    break;
                case TestCaseStepAction.ACTION_LOCKDEVICE:
                    res = this.doActionLockDevice(execution);
                    break;
                case TestCaseStepAction.ACTION_UNLOCKDEVICE:
                    res = this.doActionUnlockDevice(execution);
                    break;
                case TestCaseStepAction.ACTION_ROTATEDEVICE:
                    res = this.doActionRotateDevice(execution);
                    break;
                case TestCaseStepAction.ACTION_DONOTHING:
                    res = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
                    break;
                default:
                    res = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKNOWNACTION);
                    res.setDescription(res.getDescription().replace("%ACTION%", actionExecution.getAction()));

            }
        } catch (final Exception unexpected) {
            LOG.error("Unexpected exception: " + unexpected.getMessage(), unexpected);
            res = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC).resolveDescription("DETAIL", unexpected.getMessage());
        }

        LOG.debug("Result of the action : " + res.getCodeString() + " " + res.getDescription());

        // Reset Timeout to default
        robotServerService.setOptionsToDefault(execution.getSession());

        /**
         * In case 1/ the action is flagged as not fatal with a specific return
         * code = N and 2/ the return of the action is stopping the test -->
         * whatever the return of the action is, we force the return to move
         * forward the test with no screenshot, pagesource.
         */
        if (actionExecution.isFatal().equals("N") && res.isStopTest()) {
            res.setDescription(res.getDescription() + " -- Execution forced to continue.");
            res.setStopTest(false);
            res.setMessage(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING);
        }

        /**
         * Put Wait in ms before the action to message.
         */
        if (actionExecution.getWaitBefore() > 0) {
            res.setDescription(res.getDescription() + " -- Waited " + String.valueOf(actionExecution.getWaitBefore()) + " ms Before.");
        }

        /**
         * Wait in ms after the action.
         */
        if (actionExecution.getWaitAfter() > 0) {
            try {
                Thread.sleep(Long.parseLong(String.valueOf(actionExecution.getWaitAfter())));
                res.setDescription(res.getDescription() + " -- Waited " + String.valueOf(actionExecution.getWaitAfter()) + " ms After.");
            } catch (InterruptedException ex) {
                LOG.error("Exception when waiting after action. {}-{}-{}", execution.getId(), actionExecution.getStepId(), actionExecution.getId(), ex);
            }
        }

        actionExecution.setActionResultMessage(res);

        /**
         * Determine here the impact of the Action on the full test return code
         * from the ResultMessage of the Action.
         */
        actionExecution.setExecutionResultMessage(new MessageGeneral(res.getMessage()));

        /**
         * Determine here if we stop the test from the ResultMessage of the
         * Action.
         */
        actionExecution.setStopExecution(res.isStopTest());

        /**
         * Timestamp stops here.
         */
        actionExecution.setEnd(new Date().getTime());

        return actionExecution;
    }

    private TestCaseStepActionExecution cleanValues(TestCaseStepActionExecution actionExecution) {
        switch (actionExecution.getAction()) {

            // No parameters
            case TestCaseStepAction.ACTION_OPENURLLOGIN:
            case TestCaseStepAction.ACTION_FOCUSDEFAULTIFRAME:
            case TestCaseStepAction.ACTION_REFRESHCURRENTPAGE:
            case TestCaseStepAction.ACTION_HIDEKEYBOARD:
            case TestCaseStepAction.ACTION_WAITNETWORKTRAFFICIDLE:
            case TestCaseStepAction.ACTION_SETCONSOLECONTENT:
            case TestCaseStepAction.ACTION_SETSERVICECALLCONTENT:
            case TestCaseStepAction.ACTION_DONOTHING:
                actionExecution.setValue1("");
                actionExecution.setValue1Init("");
                actionExecution.setValue2("");
                actionExecution.setValue2Init("");
                actionExecution.setValue3("");
                actionExecution.setValue3Init("");
                break;
            // Only Value1
            case TestCaseStepAction.ACTION_MOUSEMOVE:
            case TestCaseStepAction.ACTION_OPENURLWITHBASE:
            case TestCaseStepAction.ACTION_FOCUSTOIFRAME:
            case TestCaseStepAction.ACTION_OPENURL:
            case TestCaseStepAction.ACTION_SWITCHTOWINDOW:
            case TestCaseStepAction.ACTION_SWITCHTOCONTEXT:
            case TestCaseStepAction.ACTION_MANAGEDIALOG:
            case TestCaseStepAction.ACTION_MANAGEDIALOGKEYPRESS:
            case TestCaseStepAction.ACTION_EXECUTEJS:
            case TestCaseStepAction.ACTION_EXECUTECERBERUSCOMMAND:
            case TestCaseStepAction.ACTION_CLEARFIELD:
            case TestCaseStepAction.ACTION_CLOSEAPP:
            case TestCaseStepAction.ACTION_INDEXNETWORKTRAFFIC:
            case TestCaseStepAction.ACTION_INSTALLAPP:
            case TestCaseStepAction.ACTION_REMOVEAPP:
            case TestCaseStepAction.ACTION_WAIT:
            case TestCaseStepAction.ACTION_WAITVANISH:
            case TestCaseStepAction.ACTION_SETCONTENT:
            case TestCaseStepAction.ACTION_CLEANROBOTFILE:
                actionExecution.setValue2("");
                actionExecution.setValue2Init("");
                actionExecution.setValue3("");
                actionExecution.setValue3Init("");
                break;
            // Only Value 1 and Value 2
            case TestCaseStepAction.ACTION_CLICK:
            case TestCaseStepAction.ACTION_MOUSELEFTBUTTONPRESS:
            case TestCaseStepAction.ACTION_MOUSELEFTBUTTONRELEASE:
            case TestCaseStepAction.ACTION_DOUBLECLICK:
            case TestCaseStepAction.ACTION_RIGHTCLICK:
            case TestCaseStepAction.ACTION_LONGPRESS:
            case TestCaseStepAction.ACTION_EXECUTECOMMAND:
            case TestCaseStepAction.ACTION_OPENAPP:
            case TestCaseStepAction.ACTION_DRAGANDDROP:
            case TestCaseStepAction.ACTION_SELECT:
            case TestCaseStepAction.ACTION_TYPE:
            case TestCaseStepAction.ACTION_SETNETWORKTRAFFICCONTENT:
            case TestCaseStepAction.ACTION_CALCULATEPROPERTY:
            case TestCaseStepAction.ACTION_EXECUTESQLSTOREPROCEDURE:
            case TestCaseStepAction.ACTION_EXECUTESQLUPDATE:
            case TestCaseStepAction.ACTION_SWIPE:
                actionExecution.setValue3("");
                actionExecution.setValue3Init("");
                break;
            // Value 1 and Value 2 and Value 3
            case TestCaseStepAction.ACTION_KEYPRESS:
            case TestCaseStepAction.ACTION_CALLSERVICE:
            case TestCaseStepAction.ACTION_GETROBOTFILE:
            case TestCaseStepAction.ACTION_SCROLLTO:
            case TestCaseStepAction.ACTION_UPLOADROBOTFILE:
            case TestCaseStepAction.ACTION_MOUSEOVER:
                break;
            default:

        }
        return actionExecution;
    }

    private MessageEvent doActionCleanRobotFile(TestCaseExecution execution, String filename) {
        MessageEvent message;

        try {

            if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {

                return filemanagementService.doFilemanagementActionCleanRobotFile(execution.getSession(), filename);

            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CLEANROBOTFILE));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", execution.getApplicationObj().getType()));
            return message;

        } catch (Exception e) {

            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.error("Exception Running " + TestCaseStepAction.ACTION_CLEANROBOTFILE + "  :" + messageString, e);
            return message;

        }
    }

    private MessageEvent doActionGetRobotFile(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, String filename, String nbFiles, String option) {
        MessageEvent message;

        try {
            if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {

                AnswerItem<JSONObject> ans = new AnswerItem<>();

                Integer nbFilesInt = ParameterParserUtil.parseIntegerParam(nbFiles, 1);
                ans = filemanagementService.doFilemanagementActionGetRobotFile(execution.getSession(), filename, nbFilesInt, option);

                JSONObject contentJSON = ans.getItem();
                if (contentJSON == null) {
                    return ans.getResultMessage();
                }
                LOG.debug(contentJSON.toString(1));

                AppService appSrv = factoryAppService.create("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", false, "", "", false, "", false, "", false, "", "", "", null, "", null, "");
                JSONObject contentJSONnew = new JSONObject();

                // We copy the header values for the service answered.
                if (contentJSON.has("totalFilesAvailable")) {
                    contentJSONnew.put("totalFilesAvailable", contentJSON.getInt("totalFilesAvailable"));
                }
                if (contentJSON.has("totalFilesDownloaded")) {
                    contentJSONnew.put("totalFilesDownloaded", contentJSON.getInt("totalFilesDownloaded"));
                }

                // We copy the file contents decoding content when it is in a compatible format.
                JSONArray newFiles = new JSONArray();
                JSONArray files = new JSONArray();
                if (contentJSON.has("files")) {

                    files = contentJSON.getJSONArray("files");
                    for (int i = 0; i < files.length(); i++) {
                        JSONObject file = new JSONObject();
                        file.put("filename", files.getJSONObject(i).getString("filename"));
                        file.put("path", files.getJSONObject(i).getString("path"));
                        file.put("size", files.getJSONObject(i).getInt("size"));
                        file.put("lastModified", files.getJSONObject(i).getString("lastModified"));
                        // Getting the Base64 content in order to convert it back and guess its content.
                        String fileContentBase64 = files.getJSONObject(i).getString("contentBase64");
                        byte[] filecontent = Base64.decodeBase64(fileContentBase64);
                        String sFileContent = new String(filecontent, StandardCharsets.UTF_8);
                        String contentType = appServiceService.guessContentType(sFileContent);
                        LOG.debug(files.getJSONObject(i).getString("filename"));
                        if (null == contentType) {
                            if (PDFUtil.isPdf(filecontent)) {
                                TestCaseExecutionFile localFile = recorderService.recordRobotFile(execution, actionExecution, 0, null, filecontent, "robot-" + i + "-", files.getJSONObject(i).getString("filename"), AppService.RESPONSEHTTPBODYCONTENTTYPE_PDF);
                                actionExecution.addFileList(localFile);
                                JSONObject pdfInfo = new JSONObject();
                                pdfInfo.put("pdfPageNb", PDFUtil.getNumberOfPages(filecontent));
                                pdfInfo.put("pdfCertInfo", PDFUtil.getSignatures(filecontent));
                                pdfInfo.put("pdfText", PDFUtil.getTextFromPdf(filecontent));
                                file.put("pdfInfo", pdfInfo);
                                file.put("contentType", AppService.RESPONSEHTTPBODYCONTENTTYPE_PDF);
                            } else {
                                actionExecution.addFileList(recorderService.recordRobotFile(execution, actionExecution, 0, null, filecontent, "robot-" + i + "-", files.getJSONObject(i).getString("filename"), "BIN"));
                                file.put("contentType", "BIN");
                            }
                            file.put("content", sFileContent.substring(0, (100 > sFileContent.length()) ? sFileContent.length() : 100));
                        } else {
                            switch (contentType) {
                                case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON:
                                    file.put("contentType", contentType);
                                    if (sFileContent.startsWith("[")) {
                                        JSONArray contentFileJSON = new JSONArray(sFileContent);
                                        file.put("content", contentFileJSON);
                                    } else {
                                        JSONObject contentFileJSON = new JSONObject(sFileContent);
                                        file.put("content", contentFileJSON);
                                    }
                                    break;
                                case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                                    file.put("contentType", contentType);
                                    file.put("content", sFileContent);
                                    break;
                                default:
                                    file.put("contentType", AppService.RESPONSEHTTPBODYCONTENTTYPE_UNKNOWN);
                                    file.put("content", sFileContent.substring(0, (100 > sFileContent.length()) ? sFileContent.length() : 100));
                                    break;
                            }
                            actionExecution.addFileList(recorderService.recordRobotFile(execution, actionExecution, 0, null, filecontent, "robot-" + i + "-", files.getJSONObject(i).getString("filename"), contentType));
                        }
                        newFiles.put(file);
                    }
                }
                contentJSONnew.put("files", newFiles);

                String content = contentJSONnew.toString();
                appSrv.setResponseHTTPBody(content);
                appSrv.setResponseHTTPBodyContentType(appServiceService.guessContentType(appSrv, AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON));
                appSrv.setRecordTraceFile(false);

                execution.setLastServiceCalled(appSrv);

                /**
                 * Record the Request and Response in file system.
                 */
                actionExecution.addFileList(recorderService.recordContent(execution, actionExecution, 0, null, content, appSrv.getResponseHTTPBodyContentType()));

                // Forcing the apptype to SRV in order to allow all controls to plug to the json context of the har.
                execution.setAppTypeEngine(Application.TYPE_SRV);

                return ans.getResultMessage();

            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_GETROBOTFILE));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", execution.getApplicationObj().getType()));
            return message;

        } catch (Exception e) {

            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.error("Exception Running " + TestCaseStepAction.ACTION_GETROBOTFILE + " :" + messageString, e);
            return message;

        }
    }

    private MessageEvent doActionUploadRobotFile(TestCaseExecution execution, String filename, String contentBase64, String option) {
        MessageEvent message;

        try {

            if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {

                return filemanagementService.doFilemanagementActionUploadRobotFile(execution.getSession(), filename, contentBase64, option);

            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_UPLOADROBOTFILE));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", execution.getApplicationObj().getType()));
            return message;

        } catch (Exception e) {

            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.error("Exception Running " + TestCaseStepAction.ACTION_UPLOADROBOTFILE + " :" + messageString, e);
            return message;

        }
    }

    private MessageEvent doActionInstallApp(TestCaseExecution execution, String appPath) {
        MessageEvent message;

        try {

            if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.installApp(execution.getSession(), appPath);
            } else if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.installApp(execution.getSession(), appPath);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_INSTALLAPP));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", execution.getApplicationObj().getType()));
            return message;

        } catch (Exception e) {

            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception Running install app  :" + messageString, e);
            return message;

        }
    }

    private MessageEvent doActionRemoveApp(TestCaseExecution execution, String appPackage) {
        MessageEvent message;

        try {
            if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.removeApp(execution.getSession(), appPackage);
            } else if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.removeApp(execution.getSession(), appPackage);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_REMOVEAPP));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", execution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception Running remove app  :" + messageString, e);
            return message;
        }
    }

    private MessageEvent doActionScrollTo(TestCaseExecution tCExecution, String element, String maxScrollDown, String offsets) {
        MessageEvent message;

        try {
            Offset offset = new Offset(offsets);

            Identifier identifier = identifierService.convertStringToIdentifierStrict(element);
            LOG.debug("Identifier :'" + identifier.getIdentifier() + "' Locator '" + identifier.getLocator() + "'");

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.scrollTo(tCExecution, identifier, maxScrollDown, offset.getHOffset(), offset.getVOffset());

            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.scrollTo(tCExecution, identifier, maxScrollDown, offset.getHOffset(), offset.getVOffset());

            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.scrollTo(tCExecution.getSession(), identifier, identifier.isSameIdentifier("") ? element : null, offsets);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_SCROLLTO));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;

        } catch (Exception e) {

            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            String messageString = "";
            if (e.getMessage() != null) {
                messageString = e.getMessage().split("\n")[0];
            }
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception Running scroll to :" + messageString, e);
            return message;
        }
    }

    private MessageEvent doActionExecuteCommand(TestCaseExecution tCExecution, String command, String args) {
        MessageEvent message;

        try {

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK) || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return androidAppiumService.executeCommand(tCExecution.getSession(), command, args);
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_EXECUTECOMMAND));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;

        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECOMMAND);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%EXCEPTION%", messageString));
            LOG.debug("Exception Running Shell :" + messageString, e);
            return message;
        }
    }

    private MessageEvent doActionExecuteCerberusCommand(TestCaseExecution tCExecution, String command) {

        MessageEvent message;

        try {
            return cerberusCommand.executeCerberusCommand(command);
        } catch (CerberusEventException e) {
            message = e.getMessageError();
            LOG.debug("Exception Running Shell :" + message.getMessage().getDescription());
            return message;
        }
    }

    private MessageEvent doActionClick(TestCaseExecution tCExecution, String value1, String offsetString) {
        String element;
        try {
            Offset offset = new Offset(offsetString);
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = value1;
            /**
             * Get Identifier (identifier, locator) and check it's valid
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            LOG.debug("Click : " + identifier.toString());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset(), false, false);
                } else {
                    if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliActionClick(tCExecution.getSession(), identifier.getLocator(), "");
                    } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliActionClick(tCExecution.getSession(), "", identifier.getLocator());
                    } else {
                        identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                        return webdriverService.doSeleniumActionClick(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset(), true, true);
                    }
                }

            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return androidAppiumService.click(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset());

            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return iosAppiumService.click(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset());

            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                if (StringUtil.isEmptyOrNull(identifier.getLocator())) {
                    return sikuliService.doSikuliActionClick(tCExecution.getSession(), "", "");
                }
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else {
                    return sikuliService.doSikuliActionClick(tCExecution.getSession(), "", identifier.getLocator());
                }

            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "Click")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Click :" + ex, ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionExecuteJS(TestCaseExecution tCExecution, String value1) {

        MessageEvent message;
        String script = value1;
        String valueFromJS;
        try {

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {

                valueFromJS = this.webdriverService.getValueFromJS(tCExecution.getSession(), script);
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_EXECUTEJS);
                message.setDescription(message.getDescription().replace("%SCRIPT%", script));
                message.setDescription(message.getDescription().replace("%VALUE%", valueFromJS));
                return message;

            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_EXECUTEJS));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTEJS);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%EXCEPTION%", messageString));
            LOG.debug("Exception Running JS Script :" + messageString);
            return message;
        }
    }

    private MessageEvent doActionMouseLeftButtonPress(TestCaseExecution tCExecution, String value1, String offsetString) {
        MessageEvent message;

        try {
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT) || StringUtil.isEmptyOrNull(value1)) {
                // If value1 is empty, the Sikuli engine must be used in order to click without element to click.
                return sikuliService.doSikuliActionLeftButtonPress(tCExecution.getSession());

            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                Offset offset = new Offset(offsetString);
                /**
                 * Get Identifier (identifier, locator)
                 */
                Identifier identifier = identifierService.convertStringToIdentifier(value1);
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());

                return webdriverService.doSeleniumActionMouseDown(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset(), true, true);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_MOUSELEFTBUTTONPRESS));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseDown :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionRightClick(TestCaseExecution tCExecution, String value1, String offsetString) {
        MessageEvent message;
        String element;
        try {
            Offset offset = new Offset(offsetString);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(value1);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionRightClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliActionRightClick(tCExecution.getSession(), "", identifier.getLocator());
                } else {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionRightClick(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset());
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                if (StringUtil.isEmptyOrNull(identifier.getLocator())) {
                    return sikuliService.doSikuliActionRightClick(tCExecution.getSession(), "", "");
                }
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionRightClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else {
                    return sikuliService.doSikuliActionRightClick(tCExecution.getSession(), "", identifier.getLocator());
                }
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_RIGHTCLICK));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action RightClick :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionMouseLeftButtonRelease(TestCaseExecution tCExecution, String value1, String offsetString) {
        MessageEvent message;
        try {
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT) || StringUtil.isEmptyOrNull(value1)) {
                // If value1 is empty, the Sikuli engine must be used in order to click without element to click.
                return sikuliService.doSikuliActionLeftButtonRelease(tCExecution.getSession());
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                Offset offset = new Offset(offsetString);
                /**
                 * Get Identifier (identifier, locator)
                 */
                Identifier identifier = identifierService.convertStringToIdentifier(value1);
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());

                return webdriverService.doSeleniumActionMouseUp(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset(), true, true);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_MOUSELEFTBUTTONRELEASE));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseUp :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionMouseMove(TestCaseExecution tCExecution, String value1, String value2) {
        MessageEvent message;
        String element;

        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            return sikuliService.doSikuliActionMouseMove(tCExecution.getSession(), value1);
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_MOUSEMOVE));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;
    }

    private MessageEvent doActionSwitchToWindow(TestCaseExecution execution, String object, String property) {
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "switchToWindow", execution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            //identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionSwitchToWindow(execution.getSession(), identifier);

            } else if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.switchToContext(execution.getSession(), identifier);

            } else if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.switchToContext(execution.getSession(), identifier);

            } else if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                return sikuliService.doSikuliActionSwitchApp(execution.getSession(), identifier.getLocator());

            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "SwitchToWindow")
                        .resolveDescription("APPLICATIONTYPE", execution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action SwitchToWindow :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionSwitchToContext(TestCaseExecution tCExecution, String context) {
        String applicationType = tCExecution.getApplicationObj().getType();

        if (applicationType.equalsIgnoreCase(Application.TYPE_APK)) {
            return androidAppiumService.switchToContext(tCExecution.getSession(), context);

        } else if (applicationType.equalsIgnoreCase(Application.TYPE_IPA)) {
            return iosAppiumService.switchToContext(tCExecution.getSession(), context);

        } else {
            return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                    .resolveDescription("ACTION", "SwitchToContext")
                    .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
        }
    }

    private MessageEvent doActionManageDialog(TestCaseExecution tCExecution, String value1, String value2) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(value1, value2, "manageDialog", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionManageDialog(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_MANAGEDIALOG));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action ManageDialog :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionManageDialogKeyPress(TestCaseExecution tCExecution, String value1) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(value1, "", "manageDialogKeyPress", tCExecution);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionManageDialogKeyPress(tCExecution.getSession(), element);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_MANAGEDIALOGKEYPRESS));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action ManageDialogKeypress :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionDoubleClick(TestCaseExecution tCExecution, String value1, String offsetString) {
        MessageEvent message;
        String element;
        try {
            Offset offset = new Offset(offsetString);
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = value1;
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionDoubleClick(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset(), false, false);
                } else {
                    if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), identifier.getLocator(), "");
                    } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), "", identifier.getLocator());
                    } else {
                        identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                        return webdriverService.doSeleniumActionDoubleClick(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset(), true, true);
                    }
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return webdriverService.doSeleniumActionDoubleClick(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset(), true, false);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                if (StringUtil.isEmptyOrNull(identifier.getLocator())) {
                    return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), "", "");
                }
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), identifier.getLocator(), "");
                } else {
                    return sikuliService.doSikuliActionDoubleClick(tCExecution.getSession(), "", identifier.getLocator());
                }
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_DOUBLECLICK));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action DoubleClick :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionType(TestCaseExecution tCExecution, String value1, String value2, String propertyName) {
        try {
            /**
             * Check object and property are not null for GUI/APK/IPA Check
             * property is not null for FAT Application
             */
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                if (value1 == null || value2 == null) {
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                if (value2 == null) {
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
                }
            }
            /**
             * Get Identifier (identifier, locator) if object not null
             */
            Identifier identifier = new Identifier();
            if (value1 != null) {
                identifier = identifierService.convertStringToIdentifier(value1);
            }

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionType(tCExecution.getSession(), identifier, value2, propertyName, false, false);
                } else {
                    if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliActionType(tCExecution.getSession(), identifier.getLocator(), value2);
                    } else {
                        identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                        return webdriverService.doSeleniumActionType(tCExecution.getSession(), identifier, value2, propertyName, true, true);
                    }
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.type(tCExecution.getSession(), identifier, value2, propertyName);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.type(tCExecution.getSession(), identifier, value2, propertyName);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                String locator = "";
                if (!StringUtil.isEmptyOrNull(value1)) {
                    identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                    locator = identifier.getLocator();
                }
                return sikuliService.doSikuliActionType(tCExecution.getSession(), locator, value2);
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "Type")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Type : " + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionMouseOver(TestCaseExecution tCExecution, String element, String offsetString) {
        MessageEvent message;
        try {

            Offset offset = new Offset(offsetString);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionMouseOver(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset(), false, false);
                } else {
                    if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                        return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), identifier.getLocator(), "", offsetString);
                    } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                        return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), "", identifier.getLocator(), offsetString);
                    } else {
                        identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                        return webdriverService.doSeleniumActionMouseOver(tCExecution.getSession(), identifier, offset.getHOffset(), offset.getVOffset(), true, true);
                    }
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), identifier.getLocator(), "", offsetString);
                } else {
                    return sikuliService.doSikuliActionMouseOver(tCExecution.getSession(), "", identifier.getLocator(), offsetString);
                }
            }

            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_MOUSEOVER));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action MouseOver :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionWait(TestCaseExecution tCExecution, String value1, String value2) {
        MessageEvent message;
        String element;
        long timeToWaitInMs = 0;
        Identifier identifier = null;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, null if both are empty
             */
            element = getElementToUse(value1, value2, "wait", tCExecution);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) { // If application are Selenium or appium based, we have a session and can use it to wait.

                /**
                 * if element is integer, set time to that value else Get
                 * Identifier (identifier, locator)
                 */
                if (StringUtil.isEmptyOrNull(element)) {
                    timeToWaitInMs = tCExecution.getCerberus_action_wait_default();
                } else if (StringUtil.isInteger(element)) {
                    timeToWaitInMs = Long.valueOf(element);
                } else {
                    identifier = identifierService.convertStringToIdentifier(element);
                }

                if (identifier != null && identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionWait(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier != null && identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliActionWait(tCExecution.getSession(), "", identifier.getLocator());
                } else if (identifier != null) {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                        return androidAppiumService.wait(tCExecution.getSession(), identifier);
                    } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                        return iosAppiumService.wait(tCExecution.getSession(), identifier);
                    } else {
                        return webdriverService.doSeleniumActionWait(tCExecution.getSession(), identifier);
                    }
                } else {
                    return this.waitTime(timeToWaitInMs);
                }
            } else { // For any other application we wait for the integer value.
                if (StringUtil.isEmptyOrNull(element)) {
                    // Get default wait from parameter
                    timeToWaitInMs = tCExecution.getCerberus_action_wait_default();
                } else if (StringUtil.isInteger(element)) {
                    timeToWaitInMs = Long.valueOf(element);
                }
                return this.waitTime(timeToWaitInMs);
            }

        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Wait :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionKeyPress(TestCaseExecution tCExecution, String element, String valueToKey, String modifier) {
        try {
            String appType = tCExecution.getApplicationObj().getType();
            /**
             * Check value1 and value2 are not null For IPA and APK, only value2
             * (key to press) is mandatory For GUI and FAT, both parameters are
             * mandatory
             */
            if (StringUtil.isEmptyOrNull(valueToKey)) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_MISSINGKEY).resolveDescription("APPLICATIONTYPE", appType);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            if (StringUtil.isEmptyOrNull(element) && appType.equalsIgnoreCase(Application.TYPE_GUI)) {
                element = "xpath=//body";
            }
            Identifier objectIdentifier = identifierService.convertStringToIdentifier(element);

            if (appType.equalsIgnoreCase(Application.TYPE_GUI)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.MAC.toString())
                        || tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.IOS.toString())) {
                    return iosAppiumService.keyPress(tCExecution.getSession(), valueToKey);

                } else if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    return webdriverService.doSeleniumActionKeyPress(tCExecution.getSession(), objectIdentifier, valueToKey, false, false);
                }
                if (objectIdentifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionKeyPress(tCExecution.getSession(), objectIdentifier.getLocator(), valueToKey, modifier);

                } else {
                    identifierService.checkWebElementIdentifier(objectIdentifier.getIdentifier());
                    return webdriverService.doSeleniumActionKeyPress(tCExecution.getSession(), objectIdentifier, valueToKey, true, true);
                }

            } else if (appType.equalsIgnoreCase(Application.TYPE_APK)) {
                return androidAppiumService.keyPress(tCExecution.getSession(), valueToKey);

            } else if (appType.equalsIgnoreCase(Application.TYPE_IPA)) {
                return iosAppiumService.keyPress(tCExecution.getSession(), valueToKey);

            } else if (appType.equalsIgnoreCase(Application.TYPE_FAT)) {
                return sikuliService.doSikuliActionKeyPress(tCExecution.getSession(), objectIdentifier.getLocator(), valueToKey, modifier);

            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "KeyPress")
                        .resolveDescription("APPLICATIONTYPE", appType);
            }
        } catch (CerberusEventException ex) {
            LOG.debug("Error doing Action KeyPress :" + ex);
            return ex.getMessageError();

        } catch (Exception ex) {
            LOG.debug("Error doing Action KeyPress :" + ex);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", ex.toString());
        }
    }

    private MessageEvent doActionOpenURL(TestCaseExecution execution, String value1, String value2, boolean withBase) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            if (withBase && StringUtil.isEmptyOrNull(value1)) {
                value1 = "/";
            }
            element = getElementToUse(value1, value2, "openUrl[WithBase]", execution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = new Identifier();
            identifier.setIdentifier("url");
            identifier.setLocator(element);

            if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionOpenURL(execution.getSession(), execution.getUrl(), identifier, withBase);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_OPENURL));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", execution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action OpenUrl :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionOpenApp(TestCaseExecution tCExecution, String value1, String value2) {
        MessageEvent message;

        /**
         * Check value1 is not null or empty
         */
        if (value1 == null || "".equals(value1)) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_OPENAPP);
        }

        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            return sikuliService.doSikuliActionOpenApp(tCExecution.getSession(), value1);

        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
            return androidAppiumService.openApp(tCExecution.getSession(), value1, value2);

        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
            return iosAppiumService.openApp(tCExecution.getSession(), value1, value2);

        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_OPENAPP));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;
    }

    private MessageEvent doActionCloseApp(TestCaseExecution tCExecution, String value1) {
        MessageEvent message;

        /**
         * Check value1 is not null or empty
         */
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            if (value1 == null || "".equals(value1)) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLOSEAPP);
            }
            return sikuliService.doSikuliActionCloseApp(tCExecution.getSession(), value1);

        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
            return androidAppiumService.closeApp(tCExecution.getSession());

        } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
            return iosAppiumService.closeApp(tCExecution.getSession());
        }

        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CLOSEAPP));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;
    }

    private MessageEvent doActionWaitVanish(TestCaseExecution tCExecution, String value1) {
        try {
            /**
             * Check value1 is not null or empty
             */
            if (value1 == null || "".equals(value1)) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLOSEAPP);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(value1);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionWaitVanish(tCExecution.getSession(), identifier.getLocator(), "");
                } else if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_TEXT)) {
                    return sikuliService.doSikuliActionWaitVanish(tCExecution.getSession(), "", identifier.getLocator());
                } else {
                    identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                    return webdriverService.doSeleniumActionWaitVanish(tCExecution.getSession(), identifier);
                }
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return webdriverService.doSeleniumActionWaitVanish(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {
                identifierService.checkSikuliIdentifier(identifier.getIdentifier());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionWaitVanish(tCExecution.getSession(), identifier.getLocator(), "");
                } else {
                    return sikuliService.doSikuliActionWaitVanish(tCExecution.getSession(), "", identifier.getLocator());
                }
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "WaitVanish")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action KeyPress :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionWaitNetworkTrafficIdle(TestCaseExecution tCExecution) {
        try {

            return executorService.waitForIdleNetwork(tCExecution.getRobotExecutorObj().getExecutorProxyServiceHost(), tCExecution.getRobotExecutorObj().getExecutorProxyServicePort(),
                    tCExecution.getRemoteProxyUUID(), tCExecution.getSystem());

        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action WaitNetworkTrafficIdle :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionSelect(TestCaseExecution tCExecution, String value1, String value2) {
        MessageEvent message;
        try {
            /**
             * Check object and property are not null
             */
            if (StringUtil.isEmptyOrNull(value1) || StringUtil.isEmptyOrNull(value2)) {
                return new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT);
            }
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifierValue1 = identifierService.convertStringToIdentifier(value1);
            Identifier identifierValue2 = identifierService.convertStringToSelectIdentifier(value2);

            identifierService.checkWebElementIdentifier(identifierValue1.getIdentifier());
            identifierService.checkSelectOptionsIdentifier(identifierValue2.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                if (tCExecution.getRobotObj().getPlatform().equalsIgnoreCase(Platform.ANDROID.toString())) {
                    return webdriverService.doSeleniumActionSelect(tCExecution.getSession(), identifierValue1, identifierValue2, false, false);
                }
                return webdriverService.doSeleniumActionSelect(tCExecution.getSession(), identifierValue1, identifierValue2, true, true);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_SELECT));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Select :" + ex);
            return ex.getMessageError();
        }

    }

    private MessageEvent doActionUrlLogin(TestCaseExecution tCExecution) {
        MessageEvent message;
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            return webdriverService.doSeleniumActionUrlLogin(tCExecution.getSession(), tCExecution.getUrl(), tCExecution.getCountryEnvApplicationParam().getUrlLogin());
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_OPENURLLOGIN));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;
    }

    private MessageEvent doActionFocusToIframe(TestCaseExecution tCExecution, String object, String property) {
        MessageEvent message;
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(object, property, "focusToIframe", tCExecution);
            /**
             * Get Identifier (identifier, locator)
             */
            Identifier identifier = identifierService.convertStringToIdentifier(element);
            identifierService.checkWebElementIdentifier(identifier.getIdentifier());

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return webdriverService.doSeleniumActionFocusToIframe(tCExecution.getSession(), identifier);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_FOCUSTOIFRAME));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action FocusToIframe :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionFocusDefaultIframe(TestCaseExecution tCExecution) {
        MessageEvent message;
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
            return webdriverService.doSeleniumActionFocusDefaultIframe(tCExecution.getSession());
        }
        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
        message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_FOCUSDEFAULTIFRAME));
        message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
        return message;

    }

    public MessageEvent doActionDragAndDrop(TestCaseExecution tCExecution, String value1, String value2) throws IOException {
        MessageEvent message;
        try {
            /**
             * Check source and target are not null
             */
            if (StringUtil.isEmptyOrNull(value1)) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DRAGANDDROP);
                message.setDescription(message.getDescription().replace("%ELEMENT%", value1));
                return message;
            } else if (StringUtil.isEmptyOrNull(value2)) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_DRAGANDDROP);
                message.setDescription(message.getDescription().replace("%ELEMENT%", value2));
                return message;
            }
            Identifier identifierDrag = identifierService.convertStringToIdentifier(value1);
            Identifier identifierDrop = identifierService.convertStringToIdentifier(value2);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {

                identifierService.checkWebElementIdentifier(identifierDrag.getIdentifier());
                identifierService.checkWebElementIdentifier(identifierDrop.getIdentifier());

                if (identifierDrag.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)
                        && identifierDrop.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE)) {
                    return sikuliService.doSikuliActionDragAndDrop(tCExecution.getSession(), identifierDrag, identifierDrop);
                } else {
                    if (Identifier.IDENTIFIER_OFFSET.equals(identifierDrop.getIdentifier())) {
                        return webdriverService.doSeleniumActionDragAndDropByOffset(tCExecution.getSession(), identifierDrag, identifierDrop, true, true);
                    } else {
                        return webdriverService.doSeleniumActionDragAndDrop(tCExecution.getSession(), identifierDrag, identifierDrop, true, true);
                    }
                }
            }
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {

                identifierService.checkSikuliIdentifier(identifierDrag.getIdentifier());
                identifierService.checkSikuliIdentifier(identifierDrop.getIdentifier());

                return sikuliService.doSikuliActionDragAndDrop(tCExecution.getSession(), identifierDrag, identifierDrop);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_DRAGANDDROP));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action DragAndDrop :" + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionCallService(TestCaseStepActionExecution action, String value1, String value2, String value3) {

        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
        TestCaseExecution execution = action.getTestCaseStepExecution().gettCExecution();
        AnswerItem lastServiceCalledAnswer;

        lastServiceCalledAnswer = serviceService.callService(value1, value2, value3, null, null, null, null, execution, robotServerService.getFromOptions(action.getOptions(), RobotServerService.OPTIONS_TIMEOUT_SYNTAX));
        message = lastServiceCalledAnswer.getResultMessage();

        if (lastServiceCalledAnswer.getItem() != null) {
            AppService lastServiceCalled = (AppService) lastServiceCalledAnswer.getItem();
            execution.setLastServiceCalled(lastServiceCalled);
            execution.setOriginalLastServiceCalled(lastServiceCalled.getResponseHTTPBody());
            execution.setOriginalLastServiceCalledContent(lastServiceCalled.getResponseHTTPBodyContentType());

            /**
             * Record the Request and Response in file system.
             */
            action.addFileList(recorderService.recordServiceCall(execution, action, 0, null, lastServiceCalled));
        }

        return message;

    }

    private MessageEvent doActionCalculateProperty(TestCaseExecution execution, TestCaseStepActionExecution testCaseStepActionExecution, String value1, String value2) {
        MessageEvent message;
        AnswerItem<String> answerDecode = new AnswerItem<>();
        if (StringUtil.isEmptyOrNull(value1)) {

            // Value1 is a mandatory parameter.
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALCULATEPROPERTY_MISSINGPROPERTY);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY));

        } else {
            try {

//                TestCaseExecution execution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
                // Getting the Country property definition.
                TestCaseCountryProperties tccp = null;
                boolean propertyExistOnAnyCountry = false;
                for (TestCaseCountryProperties object : execution.getTestCaseCountryPropertyList()) {
                    if ((object.getProperty().equalsIgnoreCase(value1)) && (object.getCountry().equalsIgnoreCase(execution.getCountry()))) {
                        tccp = object;
                    }
                    if ((object.getProperty().equalsIgnoreCase(value1))) {
                        propertyExistOnAnyCountry = true;
                    }
                }
                if (tccp == null) { // Could not find a country property inside the existing execution.
                    if (propertyExistOnAnyCountry) {
                        message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NO_PROPERTY_DEFINITION);
                        message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY)
                                .replace("%PROP%", value1)
                                .replace("%COUNTRY%", execution.getCountry()));
                        return message;
                    } else {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALCULATEPROPERTY_PROPERTYNOTFOUND);
                        message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY)
                                .replace("%PROP%", value1)
                                .replace("%COUNTRY%", execution.getCountry()));
                        return message;
                    }

                } else {
                    if (!(StringUtil.isEmptyOrNull(value2))) {
                        // If value2 is fed with something, we control here that value is a valid property name and gets its defintion.
                        tccp = null;
                        propertyExistOnAnyCountry = false;
                        for (TestCaseCountryProperties object : execution.getTestCaseCountryPropertyList()) {
                            if ((object.getProperty().equalsIgnoreCase(value2)) && (object.getCountry().equalsIgnoreCase(execution.getCountry()))) {
                                tccp = object;
                            }
                            if ((object.getProperty().equalsIgnoreCase(value2))) {
                                propertyExistOnAnyCountry = true;
                            }
                        }
                        if (tccp == null) { // Could not find a country property inside the existing execution.
                            if (propertyExistOnAnyCountry) {
                                message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NO_PROPERTY_DEFINITION);
                                message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY)
                                        .replace("%PROP%", value2)
                                        .replace("%COUNTRY%", execution.getCountry()));
                                return message;

                            } else {
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALCULATEPROPERTY_PROPERTYNOTFOUND);
                                message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_CALCULATEPROPERTY)
                                        .replace("%PROP%", value2)
                                        .replace("%COUNTRY%", execution.getCountry()));
                                return message;

                            }
                        }
                    }

                    // We calculate the property here.
                    long now = new Date().getTime();
                    TestCaseExecutionData tcExeData;

                    tcExeData = factoryTestCaseExecutionData.create(execution.getId(), tccp.getProperty(), 1, tccp.getDescription(), null, tccp.getType(),
                            tccp.getRank(), tccp.getValue1(), tccp.getValue2(), tccp.getValue3(), null, null, now, now, now, now, new MessageEvent(MessageEventEnum.PROPERTY_PENDING),
                            tccp.getRetryNb(), tccp.getRetryPeriod(), tccp.getDatabase(), tccp.getValue1(), tccp.getValue2(), tccp.getValue3(), tccp.getLength(), tccp.getLength(),
                            tccp.getRowLimit(), tccp.getNature(), "", "", "", "", "", "N");
                    tcExeData.setTestCaseCountryProperties(tccp);
                    propertyService.calculateProperty(tcExeData, execution, testCaseStepActionExecution, tccp, true);

                    // Property message goes to Action message.
                    message = tcExeData.getPropertyResultMessage();
                    if (message.getCodeString().equals("OK")) {
                        // If Property calculated successfully we summarize the message to a shorter version.
                        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALCULATEPROPERTY);
                        message.resolveDescription("VALUE", tcExeData.getValue());
                        message.resolveDescription("PROP", value1);
                        if (tcExeData.getDataLibRawData() != null) {
                            message.setDescription(message.getDescription() + " %NBROWS% row(s) with %NBSUBDATA% Subdata(s) calculated."
                                    .replace("%NBROWS%", String.valueOf(tcExeData.getDataLibRawData().size()))
                                    .replace("%NBSUBDATA%", String.valueOf(tcExeData.getDataLibRawData().get(0).size())));
                        }
                    }

                    if (!(StringUtil.isEmptyOrNull(value2))) {
                        // If value2 is fed we force the result to value1.
                        tcExeData.setProperty(value1);
                    }
                    //saves the result
                    try {
                        testCaseExecutionDataService.save(tcExeData, execution.getSecrets());
                        LOG.debug("Adding into Execution data list. Property : '" + tcExeData.getProperty() + "' Index : '" + tcExeData.getIndex() + "' Value : '" + tcExeData.getValue() + "'");
                        execution.getTestCaseExecutionDataMap().put(tcExeData.getProperty(), tcExeData);
                        if (tcExeData.getDataLibRawData() != null) { // If the property is a TestDataLib, we same all rows retreived in order to support nature such as NOTINUSe or RANDOMNEW.
                            for (int i = 1; i < (tcExeData.getDataLibRawData().size()); i++) {
                                now = new Date().getTime();
                                TestCaseExecutionData tcedS = factoryTestCaseExecutionData.create(tcExeData.getId(), tcExeData.getProperty(), (i + 1),
                                        tcExeData.getDescription(), tcExeData.getDataLibRawData().get(i).get(""), tcExeData.getType(), tcExeData.getRank(), "", "", "",
                                        tcExeData.getRC(), "", now, now, now, now, null, 0, 0, "", "", "", "", "", "", 0, "", "", "", "", "", "", "N");
                                testCaseExecutionDataService.save(tcedS, execution.getSecrets());
                            }
                        }
                    } catch (CerberusException cex) {
                        LOG.error(cex.getMessage(), cex);
                    }

                }

            } catch (Exception ex) {
                LOG.error(ex.toString(), ex);
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC).resolveDescription("DETAIL", ex.toString());
            }
        }
        return message;
    }

    private MessageEvent doActionSetNetworkTrafficContent(TestCaseExecution exe, TestCaseStepActionExecution actionexe, String urlToFilter, String withResponseContent) throws IOException {
        MessageEvent message;
        try {
            // Check that robot has executor activated
            if (!RobotExecutor.PROXY_TYPE_NETWORKTRAFFIC.equalsIgnoreCase(exe.getRobotExecutorObj().getExecutorProxyType()) || StringUtil.isEmptyOrNull(exe.getRobotExecutorObj().getExecutorBrowserProxyHost())) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETNETWORKTRAFFICCONTENT_ROBOTEXECUTORPROXYNOTACTIVATED);
                message.setDescription(message.getDescription().replace("%ROBOT%", exe.getRobotExecutorObj().getRobot()).replace("%EXECUTOR%", exe.getRobotExecutorObj().getExecutor()));
                return message;
            }

            LOG.debug("Getting Network Traffic content.");

            Integer indexFrom = 0;
            if (!exe.getNetworkTrafficIndexList().isEmpty()) {
                // Take the value from the last entry.
                indexFrom = exe.getNetworkTrafficIndexList().get(exe.getNetworkTrafficIndexList().size() - 1).getIndexRequestNb();
            }

            // We now get the har data.
            boolean doWithResponse = ParameterParserUtil.parseBooleanParam(withResponseContent, false);
            JSONObject har = executorService.getHar(urlToFilter, doWithResponse, exe.getRobotExecutorObj().getExecutorProxyServiceHost(), exe.getRobotExecutorObj().getExecutorProxyServicePort(), exe.getRemoteProxyUUID(), exe.getSystem(), indexFrom);

            har = harService.enrichWithStats(har, exe.getCountryEnvApplicationParam().getDomain(), exe.getSystem(), exe.getNetworkTrafficIndexList());

            AppService appSrv = factoryAppService.create("", AppService.TYPE_REST, AppService.METHOD_HTTPGET, "", "", "", "", "", "", "", "", "", "", "", "", true, "", "", false, "", false, "", false, "", "", "", null, "", null, null);
            appSrv.setResponseHTTPBody(har.toString());
            appSrv.setResponseHTTPBodyContentType(AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON);
            appSrv.setRecordTraceFile(false);

            exe.setLastServiceCalled(appSrv);

            /**
             * Record the Request and Response in file system.
             */
            actionexe.addFileList(recorderService.recordNetworkTrafficContent(exe, actionexe, 0, null, appSrv, true));

            // Forcing the apptype to SRV in order to allow all controls to plug to the json context of the har.
            exe.setAppTypeEngine(Application.TYPE_SRV);

            if (!exe.getNetworkTrafficIndexList().isEmpty()) {
                // Message will include the index and request nb when the content start.
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SETNETWORKTRAFFICCONTENT).resolveDescription("INDEX", String.valueOf(exe.getNetworkTrafficIndexList().size())).resolveDescription("NBHITS", String.valueOf(indexFrom));
            } else {
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SETNETWORKTRAFFICCONTENT_FROMINDEX0);
            }
            return message;
        } catch (Exception ex) {
            LOG.error("Error doing Action setNetworkTrafficContent :" + ex, ex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETNETWORKTRAFFICCONTENT);
            message.setDescription(message.getDescription().replace("%DETAIL%", ex.toString()));
            return message;
        }
    }

    private MessageEvent doActionIndexNetworkTraffic(TestCaseExecution exe, TestCaseStepActionExecution actionexe, String value1) throws IOException {
        MessageEvent message;
        try {
            // Check that robot has executor activated
            if (!RobotExecutor.PROXY_TYPE_NETWORKTRAFFIC.equalsIgnoreCase(exe.getRobotExecutorObj().getExecutorProxyType()) || StringUtil.isEmptyOrNull(exe.getRobotExecutorObj().getExecutorBrowserProxyHost())) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_INDEXNETWORKTRAFFIC_ROBOTEXECUTORPROXYNOTACTIVATED);
                message.setDescription(message.getDescription().replace("%ROBOT%", exe.getRobotExecutorObj().getRobot()).replace("%EXECUTOR%", exe.getRobotExecutorObj().getExecutor()));
                return message;
            }

            LOG.debug("Getting Network Traffic index");
            /**
             * Building the url to get the Latest index from cerberus-executor
             */
            Integer nbHits = executorService.getHitsNb(exe.getRobotExecutorObj().getExecutorProxyServiceHost(), exe.getRobotExecutorObj().getExecutorProxyServicePort(), exe.getRemoteProxyUUID());

            NetworkTrafficIndex nti = new NetworkTrafficIndex();
            if (StringUtil.isEmptyOrNull(value1)) {
                value1 = "INDEX" + (exe.getNetworkTrafficIndexList().size() + 1);
            }
            nti.setName(value1);
            nti.setIndexRequestNb(nbHits);
            exe.addNetworkTrafficIndexList(nti);

            LOG.debug("New Index : " + exe.getNetworkTrafficIndexList());

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_INDEXNETWORKTRAFFIC).resolveDescription("NB", nbHits.toString()).resolveDescription("INDEX", String.valueOf(exe.getNetworkTrafficIndexList().size()));
            return message;
        } catch (Exception ex) {
            LOG.error("Error doing Action indexNetworkTraffic :" + ex, ex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_INDEXNETWORKTRAFFIC);
            message.setDescription(message.getDescription().replace("%DETAIL%", ex.toString()));
            return message;
        }
    }

    private MessageEvent doActionSetConsoleContent(TestCaseExecution exe, TestCaseStepActionExecution actionexe, String textToFilter) throws IOException {
        MessageEvent message;
        try {
            /**
             * Building the url to get the Har file from cerberus-executor
             */
            LOG.debug("Getting Console Logs content.");

            JSONObject consoleRecap = new JSONObject();
            JSONArray consoleLogs = webdriverService.getJSONConsoleLog(exe.getSession());
            consoleRecap.put("logs", consoleLogs);
            JSONObject consoleStat = new JSONObject();
            consoleStat = consolelogService.enrichWithStats(consoleLogs);
            consoleRecap.put("stat", consoleStat);

            AppService appSrv = factoryAppService.create("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", false, "", "", false, "", false, "", false, "", null, "", null, "", null, "");
            appSrv.setResponseHTTPBody(consoleRecap.toString());
            appSrv.setResponseHTTPBodyContentType(AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON);
            appSrv.setRecordTraceFile(false);

            exe.setLastServiceCalled(appSrv);

            /**
             * Record the Request and Response in file system.
             */
            actionexe.addFileList(recorderService.recordConsoleContent(exe, actionexe, 0, null, consoleRecap, true));

            // Forcing the apptype to SRV in order to allow all controls to plug to the json context of the har.
            exe.setAppTypeEngine(Application.TYPE_SRV);

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SETCONSOLECONTENT);
            return message;
        } catch (Exception ex) {
            LOG.error("Error doing Action setNetworkTrafficContent :" + ex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETCONSOLECONTENT);
            message.setDescription(message.getDescription().replace("%DETAIL%", ex.toString()));
            return message;
        }
    }

    private MessageEvent doActionSetContent(TestCaseExecution exe, TestCaseStepActionExecution actionexe, String textContent) throws IOException {
        MessageEvent message;
        try {
            /**
             * Building the url to get the Har file from cerberus-executor
             */
            LOG.debug("Setting static content.");

            AppService appSrv = factoryAppService.create("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", false, "", "", false, "", false, "", false, "", "", "", null, "", null, "");
            appSrv.setResponseHTTPBody(textContent);
            appSrv.setResponseHTTPBodyContentType(appServiceService.guessContentType(appSrv, AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON));
            appSrv.setRecordTraceFile(false);

            exe.setLastServiceCalled(appSrv);

            /**
             * Record the Request and Response in file system.
             */
            actionexe.addFileList(recorderService.recordContent(exe, actionexe, 0, null, textContent, appSrv.getResponseHTTPBodyContentType()));

            // Forcing the apptype to SRV in order to allow all controls to plug to the json context of the har.
            exe.setAppTypeEngine(Application.TYPE_SRV);

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SETCONTENT);
            message.resolveDescription("TYPE", appSrv.getResponseHTTPBodyContentType());
            return message;
        } catch (Exception ex) {
            LOG.error("Error doing Action setNetworkTrafficContent :" + ex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETCONTENT);
            message.setDescription(message.getDescription().replace("%DETAIL%", ex.toString()));
            return message;
        }
    }

    public MessageEvent doActionSetServiceCallContent(TestCaseExecution exe, TestCaseStepActionExecution actionexe) throws IOException {
        MessageEvent message;
        try {

            // Check that robot has executor activated
            if (exe.getLastServiceCalled() == null) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETSERVICECALLCONTENT_NOLASTCALLDONE);
                return message;
            }

            // Force last service call content to JSON Service Call Structure & disable file save.
            exe.getLastServiceCalled().setResponseHTTPBody(exe.getLastServiceCalled().toJSONOnExecution().toString());
            exe.getLastServiceCalled().setResponseHTTPBodyContentType(AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON);
            exe.getLastServiceCalled().setRecordTraceFile(false);

            /**
             * Record the Request and Response in file system.
             */
            actionexe.addFileList(recorderService.recordServiceCallContent(exe, actionexe, exe.getLastServiceCalled()));

            // Forcing the apptype to SRV in order to allow all controls to plug to the json context of the har.
            exe.setAppTypeEngine(Application.TYPE_SRV);

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SETSERVICECALLCONTENT);
            return message;
        } catch (Exception ex) {
            LOG.error("Error doing Action setServiceCallContent :" + ex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SETSERVICECALLCONTENT);
            message.setDescription(message.getDescription().replace("%DETAIL%", ex.toString()));
            return message;
        }
    }

    private String getElementToUse(String value1, String value2, String action, TestCaseExecution execution) throws CerberusEventException {
        if (!StringUtil.isEmptyOrNull(value1)) {
            return value1;
        } else if (!StringUtil.isEmptyOrNull(value2)) {
            logEventService.createForPrivateCalls("ENGINE", action, LogEvent.STATUS_WARN, MESSAGE_DEPRECATED + " Beware, in future release, it won't be allowed to use action without using field value1. Triggered by TestCase : ['" + execution.getTest() + "'|'" + execution.getTestCase() + "'] Property : " + value2);
            LOG.warn(MESSAGE_DEPRECATED + " Action : " + action + ". Beware, in future release, it won't be allowed to use action without using field value1. Triggered by TestCase : ['" + execution.getTest() + "'|'" + execution.getTestCase() + "'] Property : " + value2);
            return value2;
        }
        if (!(action.equals("wait"))) { // Wait is the only action can be excuted with no parameters. For all other actions we raize an exception as this should never happen.
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_ELEMENT_TO_PERFORM_ACTION);
            message.setDescription(message.getDescription().replace("%ACTION%", action));
            throw new CerberusEventException(message);
        }
        return null;
    }

    private MessageEvent waitTime(Long timeToWaitMs) {
        MessageEvent message;
        /**
         * if timeToWait is null, throw CerberusException
         */
        if (timeToWaitMs == 0) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_INVALID_FORMAT);
            return message;
        }
        try {
            LOG.debug("TIME TO WAIT = " + timeToWaitMs);
            Thread.sleep(timeToWaitMs);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME);
            message.setDescription(message.getDescription().replace("%TIME%", String.valueOf(timeToWaitMs)));
            return message;
        } catch (InterruptedException exception) {
            LOG.warn(exception.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_TIME_WITHWARNINGS);
            message.setDescription(message.getDescription()
                    .replace("%TIME%", String.valueOf(timeToWaitMs))
                    .replace("%MESSAGE%", exception.toString()));
            return message;
        }
    }

    private MessageEvent doActionExecuteSQLUpdate(TestCaseExecution tCExecution, String object, String property) {
        return sqlService.executeUpdate(tCExecution.getApplicationObj().getSystem(),
                tCExecution.getCountry(), tCExecution.getEnvironment(), object, property);
    }

    private MessageEvent doActionExecuteSQLStoredProcedure(TestCaseExecution tCExecution, String object, String property) {
        return sqlService.executeCallableStatement(tCExecution.getApplicationObj().getSystem(),
                tCExecution.getCountry(), tCExecution.getEnvironment(), object, property);
    }

    private MessageEvent doActionHideKeyboard(TestCaseExecution tCExecution) {
        // Check argument
        if (tCExecution == null) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
        }

        // Hide keyboard according to application type
        String applicationType = tCExecution.getApplicationObj().getType();
        if (Application.TYPE_APK.equals(applicationType)) {
            return androidAppiumService.hideKeyboard(tCExecution.getSession());
        }
        if (Application.TYPE_IPA.equals(applicationType)) {
            return iosAppiumService.hideKeyboard(tCExecution.getSession());
        }

        // Else we are faced with a non supported application
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                .resolveDescription("ACTION", "Hide keyboard")
                .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
    }

    private MessageEvent doActionLockDevice(TestCaseExecution tCExecution) {
        // Check argument
        if (tCExecution == null) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
        }

        // Hide keyboard according to application type
        String applicationType = tCExecution.getApplicationObj().getType();
        if (Application.TYPE_APK.equals(applicationType)) {
            return androidAppiumService.lockDevice(tCExecution.getSession());
        }
        if (Application.TYPE_IPA.equals(applicationType)) {
            return iosAppiumService.lockDevice(tCExecution.getSession());
        }

        // Else we are faced with a non supported application
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                .resolveDescription("ACTION", "lockDevice")
                .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
    }

    private MessageEvent doActionUnlockDevice(TestCaseExecution tCExecution) {
        // Check argument
        if (tCExecution == null) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
        }

        // Hide keyboard according to application type
        String applicationType = tCExecution.getApplicationObj().getType();
        if (Application.TYPE_APK.equals(applicationType)) {
            return androidAppiumService.unlockDevice(tCExecution.getSession());
        }
        if (Application.TYPE_IPA.equals(applicationType)) {
            return iosAppiumService.unlockDevice(tCExecution.getSession());
        }

        // Else we are faced with a non supported application
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                .resolveDescription("ACTION", "unlockDevice")
                .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
    }

    private MessageEvent doActionRotateDevice(TestCaseExecution tCExecution) {
        // Check argument
        if (tCExecution == null) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
        }

        // Hide keyboard according to application type
        String applicationType = tCExecution.getApplicationObj().getType();
        if (Application.TYPE_APK.equals(applicationType)) {
            return androidAppiumService.rotateDevice(tCExecution.getSession());
        }
        if (Application.TYPE_IPA.equals(applicationType)) {
            return iosAppiumService.rotateDevice(tCExecution.getSession());
        }

        // Else we are faced with a non supported application
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                .resolveDescription("ACTION", "rotateDevice")
                .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
    }

    private MessageEvent doActionSwipe(TestCaseExecution tCExecution, String object, String property) {
        // Check arguments
        if (tCExecution == null || object == null) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE);
        }

        // Create the associated swipe action to the given arguments
        SwipeAction action = null;
        try {
            action = SwipeAction.fromStrings(object, property);
        } catch (Exception e) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_SWIPE)
                    .resolveDescription("DIRECTION", action == null ? "Unknown" : action.getActionType().name())
                    .resolveDescription("REASON", e.getMessage());
        }

        // Swipe screen according to the application type
        String applicationType = tCExecution.getApplicationObj().getType();
        if (Application.TYPE_APK.equals(applicationType)) {
            return androidAppiumService.swipe(tCExecution.getSession(), action);
        }
        if (Application.TYPE_IPA.equals(applicationType)) {
            return iosAppiumService.swipe(tCExecution.getSession(), action);
        }

        // Else we are faced with a non supported application
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                .resolveDescription("ACTION", "Swipe screen")
                .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
    }

    private MessageEvent doActionLongPress(TestCaseExecution tCExecution, String value1, String value2) {
        String element;
        try {
            /**
             * Get element to use String object if not empty, String property if
             * object empty, throws Exception if both empty)
             */
            element = getElementToUse(value1, value2, TestCaseStepAction.ACTION_LONGPRESS, tCExecution);
            /**
             * Get Identifier (identifier, locator) and check it's valid
             */
            Integer longPressTime = 8000;
            try {
                longPressTime = Integer.parseInt(value2);
            } catch (NumberFormatException e) {
                // do nothing
            }
            Identifier identifier = identifierService.convertStringToIdentifier(element);

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return androidAppiumService.longPress(tCExecution.getSession(), identifier, longPressTime);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return iosAppiumService.longPress(tCExecution.getSession(), identifier, longPressTime);
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "Long Click")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Click :" + ex, ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionClearField(TestCaseExecution tCExecution, String value1) {
        String element;
        try {
            /**
             * Check object and property are not null for GUI/APK/IPA Check
             * property is not null for FAT Application
             */
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                if (value1 == null) {
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLEARFIELD);
                }
            }
            /**
             * Get Identifier (identifier, locator) if object not null
             */
            Identifier identifier = new Identifier();
            if (value1 != null) {
                identifier = identifierService.convertStringToIdentifier(value1);
            }

            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return androidAppiumService.clearField(tCExecution.getSession(), identifier);
            } else if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) {
                identifierService.checkWebElementIdentifier(identifier.getIdentifier());
                return iosAppiumService.clearField(tCExecution.getSession(), identifier);
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION)
                        .resolveDescription("ACTION", "ClearField")
                        .resolveDescription("APPLICATIONTYPE", tCExecution.getApplicationObj().getType());
            }
        } catch (CerberusEventException ex) {
            LOG.fatal("Error doing Action Type : " + ex);
            return ex.getMessageError();
        }
    }

    private MessageEvent doActionRefreshCurrentPage(TestCaseExecution tCExecution) {
        MessageEvent message;

        try {
            LOG.debug("REFRESH CURRENT PAGE");
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return this.webdriverService.doSeleniumActionRefreshCurrentPage(tCExecution.getSession());
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_REFRESHCURRENTPAGE));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_REFRESHCURRENTPAGE);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception doing action refreshCurrentPage  :" + messageString, e);
            return message;
        }
    }

    private MessageEvent doActionReturnPreviousPage(TestCaseExecution tCExecution) {
        MessageEvent message;

        try {
            LOG.debug("RETURN PREVIOUS PAGE");
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return this.webdriverService.doSeleniumActionReturnPreviousPage(tCExecution.getSession());
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_RETURNPREVIOUSPAGE));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_RETURNPREVIOUSPAGE);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception doing action refreshCurrentPage  :" + messageString, e);
            return message;
        }
    }

    private MessageEvent doActionForwardNextPage(TestCaseExecution tCExecution) {
        MessageEvent message;

        try {
            LOG.debug("FORWARD NEXT PAGE");
            if (tCExecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                return this.webdriverService.doSeleniumActionForwardNextPage(tCExecution.getSession());
            }
            message = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
            message.setDescription(message.getDescription().replace("%ACTION%", TestCaseStepAction.ACTION_FORWARDNEXTPAGE));
            message.setDescription(message.getDescription().replace("%APPLICATIONTYPE%", tCExecution.getApplicationObj().getType()));
            return message;
        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_FORWARDNEXTPAGE);
            String messageString = e.getMessage().split("\n")[0];
            message.setDescription(message.getDescription().replace("%DETAIL%", messageString));
            LOG.debug("Exception doing action refreshCurrentPage  :" + messageString, e);
            return message;
        }
    }

    @Getter
    @Setter
    private class Offset {

        Integer hOffset = 0;
        Integer vOffset = 0;

        public Offset(String offsetString) {
            try {
                if (!StringUtil.isEmptyOrNull(offsetString)) {
                    String[] soffsets = offsetString.split(",");
                    hOffset = Integer.valueOf(soffsets[0]);
                    vOffset = Integer.valueOf(soffsets[1]);
                }
            } catch (Exception ex) {
                LOG.warn("Error decoding offset. It must be in two integers splited by comma. Continue with 0,0. Details :" + ex);
            }
        }
    }

}
