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
package org.cerberus.core.service.notifications.webcall.impl;

import java.util.List;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.notifications.webcall.IWebcallGenerationService;

/**
 *
 * @author vertigo17
 */
@Service
public class WebcallGenerationService implements IWebcallGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(WebcallGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IInvariantService invariantService;

    @Override
    public JSONObject generateNotifyStartTagExecution(Tag tag, JSONObject ceberusEventMessage) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }

        JSONObject body = new JSONObject();
        body.put("text", "Execution Tag '" + tag.getTag() + "' Started.");
        body.put("tag", tag.toJsonV001(cerberusUrl, null, null, null));
        ceberusEventMessage.put("content", body);

        LOG.debug(ceberusEventMessage.toString(1));
        return ceberusEventMessage;

    }

    @Override
    public JSONObject generateNotifyEndTagExecution(Tag tag, JSONObject ceberusEventMessage, List<Invariant> prioritiesList, List<Invariant> countriesList, List<Invariant> environmentsList) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        prioritiesList = invariantService.readByIdName("PRIORITY");
        countriesList = invariantService.readByIdName("COUNTRY");
        environmentsList = invariantService.readByIdName("ENVIRONMENT");

        JSONObject body = new JSONObject();
        body.put("text", "Execution Tag '" + tag.getTag() + "' Ended.");
        body.put("tag", tag.toJsonV001(cerberusUrl, prioritiesList, countriesList, environmentsList));
        ceberusEventMessage.put("content", body);

        LOG.debug(ceberusEventMessage.toString(1));
        return ceberusEventMessage;

    }

    @Override
    public JSONObject generateNotifyStartExecution(TestCaseExecution exe, JSONObject ceberusEventMessage) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }

        JSONObject body = new JSONObject();
        body.put("text", "Execution " + exe.getId() + " Started.");
        body.put("execution", exe.toJsonV001(cerberusUrl, null, null, null));
        ceberusEventMessage.put("content", body);

        LOG.debug(ceberusEventMessage.toString(1));
        return ceberusEventMessage;

    }

    @Override
    public JSONObject generateNotifyEndExecution(TestCaseExecution exe, JSONObject ceberusEventMessage) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }

        JSONObject body = new JSONObject();
        body.put("text", "Execution " + exe.getId() + " Ended.");
        body.put("execution", exe.toJsonV001(cerberusUrl, null, null, null));
        ceberusEventMessage.put("content", body);

        LOG.debug(ceberusEventMessage.toString(1));
        return ceberusEventMessage;

    }

    @Override
    public JSONObject generateNotifyTestCaseChange(TestCase testCase, String originalTest, String originalTestcase, String eventReference, JSONObject ceberusEventMessage) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }

        JSONObject body = new JSONObject();
        switch (eventReference) {
            case EventHook.EVENTREFERENCE_TESTCASE_CREATE:
                body.put("text", "Testcase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' Created.");
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_DELETE:
                body.put("text", "Testcase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' Deleted.");
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_UPDATE:
                body.put("text", "Testcase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' Updated.");
                break;
        }

        body.put("testcase", testCase.toJsonV001(cerberusUrl, null));
        body.put("originalTestFolder", originalTest);
        body.put("originalTestcase", originalTestcase);
        ceberusEventMessage.put("content", body);

        LOG.debug(ceberusEventMessage.toString(1));
        return ceberusEventMessage;

    }

}
