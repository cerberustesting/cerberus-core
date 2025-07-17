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
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionDTOV001;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionMapperV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepDTOV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mlombard
 */
@AllArgsConstructor
@Tag(name = "Testcase Action", description = "Operations related to Testcase Action")
@RestController
@RequestMapping(path = "/public/testcasestepactions")
public class TestcaseStepActionController {

    private static final Logger LOG = LogManager.getLogger(TestcaseStepActionController.class);

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final TestcaseStepActionMapperV001 actionMapper;
    private final ITestCaseStepActionService actionService;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;

    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}/{actionId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Find a testcase Action by its key (testFolderId, testcaseId, stepId, actionId)",
            description = "Find a testcase Action by its key (testFolderId, testcaseId, stepId, actionId)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the action", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = TestcaseStepActionDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<TestcaseStepActionDTOV001> findActionByKey(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("stepId") int stepId,
            @PathVariable("actionId") int actionId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcasestepactions", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /testcasestepactions called with URL: %s", request.getRequestURL()), request);

        return ResponseWrapper.wrap(
                this.actionMapper.toDTO(
                        this.actionService.findTestCaseStepActionbyKey(
                                testFolderId, testcaseId, stepId, actionId)
                )
        );
    }

    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Find actions by testcase step (testFolderId, testcaseId, stepId)",
            description = "Find actions by testcase step (testFolderId, testcaseId, stepId)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the actions", content = { @Content(mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = TestcaseStepActionDTOV001.class)))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<TestcaseStepActionDTOV001>> findActionsByStep(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("stepId") int stepId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcasestepactions", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /testcasestepactions called with URL: %s", request.getRequestURL()), request);

        return ResponseWrapper.wrap(
                this.actionService.readByVarious1WithDependency(testFolderId, testcaseId, stepId)
                        .getDataList()
                        .stream()
                        .map(this.actionMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }
}
