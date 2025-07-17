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
import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Hidden;
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
import org.cerberus.core.api.dto.testcasecontrol.TestcaseStepActionControlDTOV001;
import org.cerberus.core.api.dto.testcasecontrol.TestcaseStepActionControlMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
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
@Tag(name = "Testcase Action Control", description = "Operations related to Testcase Action Control")
@RestController
@RequestMapping(path = "/public/testcasestepactioncontrols")
public class TestcaseStepActionControlController {

    private static final Logger LOG = LogManager.getLogger(TestcaseStepActionControlController.class);

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final TestcaseStepActionControlMapperV001 controlMapper;
    private final ITestCaseStepActionControlService controlService;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;

    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}/{actionId}/{controlId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Find a Testcase Control by its key (testFolderId, testcaseId, stepId, actionId, controlId)",
            description = "Find a Testcase Control by its key (testFolderId, testcaseId, stepId, actionId, controlId)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the testcase control", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = TestcaseStepActionControlDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<TestcaseStepActionControlDTOV001> findControlByKey(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("stepId") int stepId,
            @PathVariable("actionId") int actionId,
            @PathVariable("controlId") int controlId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {
        
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcasestepactioncontrols", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /testcasestepactioncontrols called with URL: %s", request.getRequestURL()), request, login);
        
        return ResponseWrapper.wrap(
                this.controlMapper.toDTO(
                        this.controlService.findTestCaseStepActionControlByKey(
                                testFolderId, testcaseId, stepId, actionId, controlId)
                )
        );
    }
}
