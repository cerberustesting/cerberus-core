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

import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.dto.testcaseexecution.TestCaseExecutionMonthlyStatsDTOV001;
import org.cerberus.core.api.dto.testcaseexecution.TestCaseExecutionStatsDTOV001;
import org.cerberus.core.crud.entity.stats.TestCaseExecutionStats;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.bug.IBugService;
import org.cerberus.core.util.servlet.ServletUtil;
import org.cerberus.core.websocket.ExecutionMonitor;
import org.cerberus.core.websocket.ExecutionMonitorWebSocket;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bcivel
 */
@RestController
@RequestMapping("/executions")
public class ExecutionPrivateController {

    private static final Logger LOG = LogManager.getLogger(ExecutionPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    private ITestCaseExecutionService executionService;
    @Autowired
    private IBugService bugService;
    @Autowired
    private ExecutionUUID executionUUIDObject;
    @Autowired
    private ExecutionMonitor executionMonitor;
    @Autowired
    private ExecutionMonitorWebSocket executionMonitorWebSocket;

    @Operation(hidden = true)
    @GetMapping("/getLastByCriteria")
    public String getLastByCriteria(
            @RequestParam(name = "test") String test,
            @RequestParam(name = "testCase") String testCase,
            @Parameter(name = "numberOfExecution", description = "Number of executions expected. If empty, all matching executions will be returned.")
            @RequestParam(name = "numberOfExecution", required = false) Integer numberOfExecution,
            @Parameter(name = "tag", description = "Tag of the execution expected.")
            @RequestParam(name = "tag", required = false) String tag,
            @Parameter(name = "campaign", description = "Campaign name of the execution expected.")
            @RequestParam(name = "campaign", required = false) String campaign) {

        try {
            test = policy.sanitize(test);
            testCase = policy.sanitize(testCase);
            tag = policy.sanitize(tag);
            JSONArray ja = executionService.getLastByCriteria(test, testCase, tag, numberOfExecution);
            return ja.toString();
        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
            return "error";
        }
    }

    @Operation(hidden = true)
    @GetMapping("/count")
    public String getnbByCriteria(
            @RequestParam(name = "system", value = "system", required = false) List<String> systems,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {
            LOG.debug(systems);

            return jsonResponse.put("iTotalRecords", executionService.getNbExecutions(systems)).toString();
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

    @Operation(hidden = true)
    @GetMapping("/running")
    public String getRunning(
            //            @RequestParam(name = "system", value = "system", required = false) List<String> systems,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        return executionUUIDObject.getRunningStatus().toString();

    }

    @Operation(hidden = true)
    @PostMapping("{executionId}/declareFalseNegative")
    public String updateDeclareFalseNegative(
            @PathVariable("executionId") long executionId,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);
        try {
            executionService.updateFalseNegative(executionId, true, request.getUserPrincipal().getName());
            executionMonitor.updateExecutionToMonitor(executionId, true);
            executionMonitorWebSocket.send(true);
        } catch (Exception ex) {
            LOG.error(ex, ex);
            return ex.toString();
        }
        return "";

    }

    @Operation(hidden = true)
    @PostMapping("{executionId}/undeclareFalseNegative")
    public String updateUndeclareFalseNegative(
            @PathVariable("executionId") long executionId,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);
        try {
            executionService.updateFalseNegative(executionId, false, request.getUserPrincipal().getName());
            executionMonitor.updateExecutionToMonitor(executionId, false);
            executionMonitorWebSocket.send(true);
        } catch (Exception ex) {
            LOG.error(ex, ex);
            return ex.toString();
        }
        return "";

    }

    @Operation(hidden = true)
    @PostMapping("{executionId}/createBug")
    public String createBug(
            @PathVariable("executionId") long executionId,
            HttpServletRequest request) {

        JSONObject newBugCreated = new JSONObject();
        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);
        try {
            newBugCreated = bugService.createBugFromID(executionId, "");
        } catch (Exception ex) {
            LOG.warn(ex, ex);
        }
        return newBugCreated.toString();

    }

    @Operation(hidden = true)
    @GetMapping("/monthlyStats")
    public TestCaseExecutionMonthlyStatsDTOV001 getMonthlyStats(
            @RequestParam(name = "system", required = false) List<String> systems) throws JsonProcessingException {

        LocalDate today = LocalDate.now();

        // Périodes
        LocalDate thisStartDate = today.minusDays(30);
        LocalDate thisEndDate   = today;

        // Previous 30 days
        LocalDate prevStartDate = today.minusDays(60);
        LocalDate prevEndDate   = today.minusDays(30);


        // --- Get Last Month Stats : Last Month dates, All systems --- and build DTO
        TestCaseExecutionStats statsGlobalLastMonth = executionService.readTestCaseExecutionStats(thisStartDate.toString(), thisEndDate.toString(), null)
                .getItem();
        TestCaseExecutionStatsDTOV001 statsGlobalLastMonthDto = TestCaseExecutionStatsDTOV001.builder()
                .totalExecutions(statsGlobalLastMonth.getTotalTestCaseExecutions())
                .fromDate(statsGlobalLastMonth.getFromDate())
                .toDate(statsGlobalLastMonth.getToDate())
                .build();

        // --- Get last month Stats : Previous 30 days, All systems --- and build DTO
        TestCaseExecutionStats statsGlobalPreviousMonth = executionService
                .readTestCaseExecutionStats(prevStartDate.toString(), prevEndDate.toString(), null)
                .getItem();
        TestCaseExecutionStatsDTOV001 statsGlobalPreviousMonthDto = TestCaseExecutionStatsDTOV001.builder()
                .totalExecutions(statsGlobalPreviousMonth.getTotalTestCaseExecutions())
                .fromDate(statsGlobalPreviousMonth.getFromDate())
                .toDate(statsGlobalPreviousMonth.getToDate())
                .build();

        // --- Get Selected System Stats : All dates, selected systems --- and build DTO
        TestCaseExecutionStats statsSystemsLastMonth = executionService
                .readTestCaseExecutionStats(thisStartDate.toString(), thisEndDate.toString(), systems)
                .getItem();
        TestCaseExecutionStatsDTOV001 statsSystemsLastMonthDto = TestCaseExecutionStatsDTOV001.builder()
                .totalExecutions(statsSystemsLastMonth.getTotalTestCaseExecutions())
                .fromDate(statsSystemsLastMonth.getFromDate())
                .toDate(statsSystemsLastMonth.getToDate())
                .build();

        // --- Get Selected System Stats : Last 30 days, selected systems --- and build DTO
        TestCaseExecutionStats statsSystemsPreviousMonth = executionService
                .readTestCaseExecutionStats(prevStartDate.toString(), prevEndDate.toString(), systems)
                .getItem();
        TestCaseExecutionStatsDTOV001 statsSystemsPreviousMonthDto = TestCaseExecutionStatsDTOV001.builder()
                .totalExecutions(statsSystemsPreviousMonth.getTotalTestCaseExecutions())
                .fromDate(statsSystemsPreviousMonth.getFromDate())
                .toDate(statsSystemsPreviousMonth.getToDate())
                .build();

        //ON GOING
        ObjectMapper mapper = new ObjectMapper();
        JSONObject ongoing = executionUUIDObject.getRunningStatus();
        JsonNode ongoingNode = mapper.readTree(ongoing.toString());

        // --- Build DTO final ---
        return TestCaseExecutionMonthlyStatsDTOV001.builder()
                .globalLastMonth(statsGlobalLastMonthDto)
                .globalPreviousMonth(statsGlobalPreviousMonthDto)
                .systemLastMonth(statsSystemsLastMonthDto)
                .systemPreviousMonth(statsSystemsPreviousMonthDto)
                .ongoingExecution(ongoingNode)
                .build();
    }

}
