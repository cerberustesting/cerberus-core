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

import com.google.gson.Gson;

import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.dto.application.ApplicationMonthlyStatsDTOV001;
import org.cerberus.core.api.dto.application.ApplicationStatsDTOV001;
import org.cerberus.core.api.dto.testcase.TestcaseMonthlyStatsDTOV001;
import org.cerberus.core.api.dto.testcase.TestcaseStatsDTOV001;
import org.cerberus.core.crud.entity.stats.ApplicationStats;
import org.cerberus.core.crud.entity.stats.TestCaseStats;
import org.cerberus.core.crud.entity.TestGenericObject;
import org.cerberus.core.crud.service.impl.TestCaseService;
import org.cerberus.core.crud.service.impl.TestGenericObjectService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.datatable.DataTableInformation;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bcivel
 */
@RestController
@RequestMapping("/testcases")
public class TestCasePrivateController {

    private static final Logger LOG = LogManager.getLogger(TestCasePrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    TestCaseService testCaseService;
    @Autowired
    TestGenericObjectService testGenericObjectService;

    @Operation(hidden=true)
    @GetMapping("/count")
    public String getnbByCriteria(
            @RequestParam(name = "system", value = "system", required = false) List<String> systems,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {
            LOG.debug(systems);

            return jsonResponse.put("iTotalRecords", testCaseService.getnbtc(systems)).toString();
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

    @Operation(hidden=true)
    @PostMapping("/objects")
    public String readObjects(
            @RequestParam(name = "system", value = "system", required = false) List<String> systems,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {

            AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
            AnswerList<TestGenericObject> testObjectAnswerList = new AnswerList<>();

            DataTableInformation dti = new DataTableInformation(request, "test,description,active,automated,tdatecrea");
            String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");
            LOG.debug(columnName);

            if (!StringUtil.isEmptyOrNull(columnName)) {
                AnswerList testGenericObjectList = testGenericObjectService.readDistinctValuesByCriteria(systems, dti.getSearchParameter(), dti.getIndividualSearch(), columnName);
                jsonResponse.put("distinctValues", testGenericObjectList.getDataList());
                jsonResponse.put("message", testGenericObjectList.getMessageDescription());
                jsonResponse.put("messageType", testGenericObjectList.getMessageCodeString());

            } else {
//        AnswerList applicationList = campaignService.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
//        object.put("distinctValues", applicationList.getDataList());
                JSONArray jsonArray = new JSONArray();
                testObjectAnswerList = testGenericObjectService.readBySystemByCriteria(systems, dti.getStartPosition(), dti.getLength(), dti.getColumnName(), dti.getSort(), dti.getSearchParameter(), dti.getIndividualSearch());
                jsonResponse.put("message", testObjectAnswerList.getMessageDescription());
                jsonResponse.put("messageType", testObjectAnswerList.getMessageCodeString());

                if (testObjectAnswerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    for (TestGenericObject objectTest : testObjectAnswerList.getDataList()) {
                        Gson gson = new Gson();
                        JSONObject objJSON = new JSONObject(gson.toJson(objectTest));
                        objJSON.put("hasPermissions", testCaseService.hasPermissionsUpdateFromStatus(objectTest.getStatus(), request));
                        jsonArray.put(objJSON);
                    }
                } else {
                    LOG.error(testObjectAnswerList.getResultMessage());
                }

                jsonResponse.put("contentTable", jsonArray);
                jsonResponse.put("iTotalRecords", testObjectAnswerList.getTotalRows());
                jsonResponse.put("iTotalDisplayRecords", testObjectAnswerList.getTotalRows());

            }

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return jsonResponse.toString();
    }

    /**
     * Endpoint pour récupérer les statistiques d'usage AI sur une période
     *@return DTO avec totalInputTokens, totalOutputTokens, totalCost
     */
    @Operation(hidden=true)
    @GetMapping("/monthlyStats")
    public TestcaseMonthlyStatsDTOV001 getMonthlyStats(
            @RequestParam(name = "user", required = false) String user,
            @RequestParam(name = "system", required = false) List<String> systems) {

        LocalDate today = LocalDate.now();

        // Last 30 days
        LocalDate thisStartDate = today.minusDays(30);
        LocalDate thisEndDate   = today;

        // --- Get Global Stats : All dates, All systems --- and build DTO
        TestCaseStats statsGlobal = testCaseService
                .readTestCaseStats(null, null, null)
                .getItem();
        TestcaseStatsDTOV001 statsGlobalDto = TestcaseStatsDTOV001.builder()
                .totalCount(statsGlobal.getTotalCount())
                .createdLast30Days(statsGlobal.getTotalCreated())
                .workingCount(statsGlobal.getWorkingCount())
                .build();

        // --- Get last month Stats : Last 30 days, All systems --- and build DTO
        TestCaseStats statsGlobalPreviousMonth = testCaseService
                .readTestCaseStats(thisStartDate.toString(), thisEndDate.toString(), null)
                .getItem();
        TestcaseStatsDTOV001 statsGlobalPreviousMonthDto = TestcaseStatsDTOV001.builder()
                .totalCount(statsGlobalPreviousMonth.getTotalCount())
                .createdLast30Days(statsGlobalPreviousMonth.getTotalCreated())
                .workingCount(statsGlobalPreviousMonth.getWorkingCount())
                .build();

        // --- Get Selected System Stats : All dates, selected systems --- and build DTO
        TestCaseStats statsSystems = testCaseService
                .readTestCaseStats(null, null, systems)
                .getItem();
        TestcaseStatsDTOV001 statsSystemsDto = TestcaseStatsDTOV001.builder()
                .totalCount(statsSystems.getTotalCount())
                .createdLast30Days(statsSystems.getTotalCreated())
                .workingCount(statsSystems.getWorkingCount())
                .build();

        // --- Get Selected System Stats : Last 30 days, selected systems --- and build DTO
        TestCaseStats statsSystemsPreviousMonth = testCaseService
                .readTestCaseStats(thisStartDate.toString(), thisEndDate.toString(), systems)
                .getItem();
        TestcaseStatsDTOV001 statsSystemsPreviousMonthDto = TestcaseStatsDTOV001.builder()
                .totalCount(statsSystemsPreviousMonth.getTotalCount())
                .createdLast30Days(statsSystemsPreviousMonth.getTotalCreated())
                .workingCount(statsSystemsPreviousMonth.getWorkingCount())
                .build();

        // --- Build DTO final ---
        return TestcaseMonthlyStatsDTOV001.builder()
                .global(statsGlobalDto)
                .globalPreviousMonth(statsGlobalPreviousMonthDto)
                .system(statsSystemsDto)
                .systemPreviousMonth(statsSystemsPreviousMonthDto)
                .build();

    }

}
