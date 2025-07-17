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
package org.cerberus.core.apiprivate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.cerberus.core.util.datatable.DataTableInformation;
import com.google.gson.Gson;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.factory.IFactoryLogEvent;
import org.cerberus.core.crud.factory.IFactoryTest;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author bcivel
 */
@RestController
@RequestMapping("/tests")
public class TestPrivateController {

    private static final Logger LOG = LogManager.getLogger(TestPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    TestCaseExecutionService testCaseExecutionService;
    @Autowired
    ITestService testService;
    @Autowired
    IFactoryTest factoryTest;
    @Autowired
    ILogEventService logEventService;
    @Autowired
    IFactoryLogEvent factoryLogEvent;
    @Autowired
    IParameterService parameterService;

    /**
     * Create Test
     *
     * @param test
     * @param active
     * @param parentTest
     * @param description
     * @param request
     * @return
     */
    @Operation(hidden=true)
    @PostMapping("/create")
    public String create(
            @Parameter(description = "Test name", required = true) @RequestParam(name = "test") String test,
            @Parameter(description = "Active") @RequestParam(name = "Active", required = false) String active,
            @Parameter(description = "ParentTest") @RequestParam(name = "ParentTest", required = false) String parentTest,
            @Parameter(description = "Description") @RequestParam(name = "Description", required = false) String description,
            @Parameter(hidden = true) HttpServletRequest request) {

        JSONObject jsonResponse = new JSONObject();

        try {

            // Calling Servlet Transversal Util.
            ServletUtil.servletStart(request);

            test = policy.sanitize(test);
            boolean isActive = (ParameterParserUtil.parseBooleanParam(policy.sanitize(active), false));
            parentTest = policy.sanitize(parentTest);
            description = policy.sanitize(description);

            Answer ans = new Answer();
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
            ans.setResultMessage(msg);

            Test testData = factoryTest.create(test, description, isActive, parentTest, request.getUserPrincipal().getName(), null, null, null);
            ans = testService.create(testData);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Object created. Adding Log entry.
                 */
                logEventService.createForPrivateCalls("/test", "CREATE", LogEvent.STATUS_INFO, "Create Test : ['" + test + "']", request);
            }

            /**
             * Formating and returning the json result.
             */
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return jsonResponse.toString();
    }

    /**
     * Delete Test
     *
     * @param test
     * @param request
     * @return
     */
    @Operation(hidden=true)
    @DeleteMapping("/delete")
    public String delete(
            @Parameter(description = "Test name", required = true) @RequestParam(name = "test") String test,
            @Parameter(hidden = true) HttpServletRequest request) {

        JSONObject jsonResponse = new JSONObject();

        try {
            // Calling Servlet Transversal Util.
            ServletUtil.servletStart(request);

            test = policy.sanitize(test);

            Answer ans = testService.deleteIfNotUsed(test);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                logEventService.createForPrivateCalls("/test", "DELETE", LogEvent.STATUS_INFO, "Delete Test : ['" + test + "']", request);
            }

            // Formating and returning the json result.
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());
        } catch (JSONException ex) {
            LOG.warn(ex);
        }

        return jsonResponse.toString();
    }

    /**
     * Read By Key
     *
     * @param request
     * @param test
     * @return
     */
    @Operation(hidden=true)
    @GetMapping("/readByKey")
    public String readByKey(@Parameter(description = "Test name", required = true) @RequestParam(name = "test") String test,
                            @Parameter(hidden = true) HttpServletRequest request) {

        JSONObject object = new JSONObject();
        boolean userHasPermissions = request.isUserInRole("TestAdmin");

        try {
            // Calling Servlet Transversal Util.
            ServletUtil.servletStart(request);

            test = policy.sanitize(test);

            AnswerItem<Test> answerTest = testService.readByKey(test);

            if (answerTest.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                //if the service returns an OK message then we can get the item and convert it to JSONformat
                Gson gson = new Gson();
                Test testObj = answerTest.getItem();
                object.put("contentTable", new JSONObject(gson.toJson(testObj)));
            }

            object.put("hasPermissions", userHasPermissions);

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();

    }

    /**
     * Read
     *
     * @param request
     * @return
     */
    @Operation(hidden=true)
    @GetMapping("/read")
    public String read(HttpServletRequest request) {

        boolean userHasPermissions = request.isUserInRole("TestAdmin");

        JSONObject object = new JSONObject();
        try {
            AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
            AnswerList<Test> testList = new AnswerList<>();

            DataTableInformation dti = new DataTableInformation(request, "test,description,active,automated,tdatecrea");

            testList = testService.readByCriteria(dti.getStartPosition(), dti.getLength(), dti.getColumnName(), dti.getSort(), dti.getSearchParameter(), dti.getIndividualSearch());

            JSONArray jsonArray = new JSONArray();
            if (testList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                for (Test test : testList.getDataList()) {
                    Gson gson = new Gson();
                    jsonArray.put(new JSONObject(gson.toJson(test)).put("hasPermissions", userHasPermissions));
                }
            }

            object.put("contentTable", jsonArray);
            object.put("hasPermissions", userHasPermissions);
            object.put("iTotalRecords", testList.getTotalRows());
            object.put("iTotalDisplayRecords", testList.getTotalRows());

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();
    }

    /**
     * Read By System
     *
     * @param request
     * @param system
     * @return
     */
    @Operation(hidden=true)
    @GetMapping("readBySystem")
    public String readBySystem(
            @Parameter(description = "System name", required = true) @RequestParam(name = "system") String system,
            @Parameter(hidden = true) HttpServletRequest request) {
        JSONObject object = new JSONObject();
        boolean userHasPermissions = request.isUserInRole("TestAdmin");

        try {

            // Calling Servlet Transversal Util.
            ServletUtil.servletStart(request);

            system = policy.sanitize(system);

            AnswerList<Test> testList = testService.readDistinctBySystem(system);

            JSONArray jsonArray = new JSONArray();
            if (testList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                for (Test test : testList.getDataList()) {
                    Gson gson = new Gson();
                    jsonArray.put(new JSONObject(gson.toJson(test)));
                }
            }
            object.put("contentTable", jsonArray);
            object.put("iTotalRecords", testList.getTotalRows());
            object.put("iTotalDisplayRecords", testList.getTotalRows());
            object.put("hasPermissions", userHasPermissions);

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();

    }

    /**
     * Read Distinct Value Of Column
     *
     * @param request
     * @return
     */
    @Operation(hidden=true)
    @GetMapping("readDistinctValueOfColumn")
    public String readDistinctValueOfColumn(HttpServletRequest request) {

        JSONObject object = new JSONObject();
        try {
            DataTableInformation dti = new DataTableInformation(request, "test,description,active,automated,tdatecrea");
            AnswerList testCaseList = testService.readDistinctValuesByCriteria(dti.getSearchParameter(), dti.getIndividualSearch(), dti.getColumnName());
            object.put("distinctValues", testCaseList.getDataList());

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();
    }

    
    /**
     * Update Test
     * @param request
     * @param originalTest
     * @param test
     * @param active
     * @param description
     * @return 
     */
    @Operation(hidden=true)
    @PatchMapping("/update")
    public String update(
            @Parameter(description = "Original Test") @RequestParam(name = "OriginalTest", required = true) String originalTest,
            @Parameter(description = "Test name", required = true) @RequestParam(name = "test") String test,
            @Parameter(description = "Active") @RequestParam(name = "Active", required = false) String active,
            @Parameter(description = "Description") @RequestParam(name = "Description", required = false) String description,
            @Parameter(hidden = true) HttpServletRequest request) {

        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();

        try {

            // Calling Servlet Transversal Util.
            ServletUtil.servletStart(request);

            Test testObj = new Test();
            testObj.setTest(test);
            testObj.setActive(ParameterParserUtil.parseBooleanParam(active, false));
            testObj.setDescription(description);

            ans = testService.updateIfExists(originalTest, testObj);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Update was successful. Adding Log entry.
                 */
                logEventService.createForPrivateCalls("/test", "UPDATE", LogEvent.STATUS_INFO, "Updated Test : ['" + originalTest + "']", request);
            }

            /**
             * Formating and returning the json result.
             */
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());
            
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return jsonResponse.toString();
    }

}
