/*
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
package org.cerberus.core.api.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import java.security.Principal;
import java.sql.Timestamp;
import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.appservice.AppServiceDTOV001;
import org.cerberus.core.api.dto.user.UserMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.engine.scheduler.SchedulerInit;
import org.cerberus.core.exception.CerberusException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Tag(name = "Manage", description = "Endpoints related to Cerberus instance management")
@RestController
@RequestMapping(path = "/public/manage")
public class ManageControler {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final PublicApiAuthenticationService apiAuthenticationService;
    private static final Logger LOG = LogManager.getLogger(ManageControler.class);
    private final UserMapperV001 userMapper;
    private final ILogEventService logEventService;
    private final IExecutionThreadPoolService executionThreadPoolService;
    private final IParameterService parameterService;
    private final SchedulerInit cerberusScheduler;
    private final ExecutionUUID euuid;

    @PostMapping(path = "/start", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Start Cerberus Instance (remove splash page, reload scheduler and force a queue processing)",
            description = "Start Cerberus Instance (remove splash page, reload scheduler and force a queue processing)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the service", content = { @Content(mediaType = "application/json")})
            }
    )
    @JsonView(View.Public.POST.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper manageStart(
            @RequestParam("scope") String scope,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/manage", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /manage called with URL: %s", request.getRequestURL()), request, login);

        executionThreadPoolService.setSplashPageActive(false);

        /**
         * We activate the instance to process queue and start new executions.
         */
        executionThreadPoolService.setInstanceActive(true);
        /**
         * We reactivate and force reload all scheduler entries.
         */
        cerberusScheduler.setInstanceSchedulerVersion("INIT");
        cerberusScheduler.init();
        /**
         * We run the execution pool.
         */
        try {
            executionThreadPoolService.executeNextInQueueAsynchroneously(false);
        } catch (CerberusException ex) {
            LOG.error("Exception triggering the ThreadPool job.", ex);
        }

        return new ResponseWrapper<>(HttpStatus.ACCEPTED, "Instance Started");

    }

    /*
    @ApiOperation("Clean Cerberus Instance from old PE executions")
    @ApiResponse(code = 200, message = "ok")
    @JsonView(View.Public.POST.class)
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/cleanExecutions", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper cleanPEExecutions(
            @RequestParam("horizonMs") int horizonMs,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/manage", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /manage called with URL: %s", request.getRequestURL()), request, login);

        JSONArray executionArray = new JSONArray();
        for (Object ex : euuid.getExecutionUUIDList().values()) {
            TestCaseExecution execution = (TestCaseExecution) ex;
            JSONObject object = new JSONObject();
            object.put("id", execution.getId());
            object.put("test", execution.getTest());
            object.put("testcase", execution.getTestCase());
            object.put("system", execution.getApplicationObj().getSystem());
            object.put("application", execution.getApplication());
            object.put("environment", execution.getEnvironmentData());
            object.put("country", execution.getCountry());
            object.put("robotIP", execution.getSeleniumIP());
            object.put("tag", execution.getTag());
            object.put("start", new Timestamp(execution.getStart()));
            executionArray.put(object);
        }
        LOG.debug(executionArray);

        return new ResponseWrapper<>(HttpStatus.ACCEPTED, String.valueOf(executionArray.length()) + " PE Execution(s) cleaned");

    }
     */
}
