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
import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionAckDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionElementsRequestDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionElementsResultDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionHighlightRequestDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStartDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStartResultDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStatusDTOV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.DebugExecutionService;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Start, step through, stop and poll a debug-mode test case execution : the
 * execution pauses before each action and only runs it once a "next" command
 * is received, with element-wait timeouts effectively disabled for the whole
 * session. See {@link DebugExecutionService} for the underlying mechanism.
 *
 * @author bcivel
 */
@AllArgsConstructor
@Tag(name = "Debug Execution", description = "Endpoints related to debug-mode (step-by-step) Test Case Execution")
@Validated
@RestController
@RequestMapping(path = "/public/debugexecutions/")
public class DebugExecutionController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final PublicApiAuthenticationService apiAuthenticationService;
    private final DebugExecutionService debugExecutionService;
    private final ILogEventService logEventService;

    @PostMapping(headers = {API_VERSION_1}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Start a debug-mode execution of a test case.",
            description = "Starts the test case asynchronously and pauses immediately before the first action. Element-wait timeouts are disabled for the whole session. Call /next repeatedly to advance one action at a time, or /stop to end it.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Debug execution successfully started", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DebugExecutionStartResultDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<DebugExecutionStartResultDTOV001> startDebugExecution(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @JsonView(View.Public.POST.class) @RequestBody DebugExecutionStartDTOV001 debugExecutionStart,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/debugexecutions", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /debugexecutions called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(this.debugExecutionService.startDebugExecution(debugExecutionStart, login));
    }

    @PostMapping(path = "/{executionUUID}/next", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Advance a debug-mode execution by exactly one action.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Command accepted", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DebugExecutionAckDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<DebugExecutionAckDTOV001> next(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @PathVariable("executionUUID") String executionUUID,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/debugexecutions", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /debugexecutions/%s/next called with URL: %s", executionUUID, request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(this.debugExecutionService.sendNext(executionUUID));
    }

    @PostMapping(path = "/{executionUUID}/retry", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Re-run the action/control that just failed, instead of advancing.",
            description = "Only meaningful when the current status reports pendingFailed=true. The engine reloads the action/control definition fresh from the database before re-running it, so an edit made elsewhere (e.g. via the test case editor) is picked up.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Command accepted", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DebugExecutionAckDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<DebugExecutionAckDTOV001> retry(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @PathVariable("executionUUID") String executionUUID,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/debugexecutions", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /debugexecutions/%s/retry called with URL: %s", executionUUID, request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(this.debugExecutionService.sendRetry(executionUUID));
    }

    @PostMapping(path = "/{executionUUID}/stop", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Stop a debug-mode execution.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Command accepted", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DebugExecutionAckDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<DebugExecutionAckDTOV001> stop(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @PathVariable("executionUUID") String executionUUID,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/debugexecutions", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /debugexecutions/%s/stop called with URL: %s", executionUUID, request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(this.debugExecutionService.stopDebugSession(executionUUID));
    }

    @GetMapping(path = "/{executionUUID}/status", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get the current status of a debug-mode execution.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current status", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DebugExecutionStatusDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<DebugExecutionStatusDTOV001> status(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @PathVariable("executionUUID") String executionUUID,
            Principal principal) {

        this.apiAuthenticationService.authenticateLogin(principal, apiKey);

        return ResponseWrapper.wrap(this.debugExecutionService.getStatus(executionUUID));
    }

    @PostMapping(path = "/elements", headers = {API_VERSION_1}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Analyze a page source and list its elements.",
            description = "Parses the given HTML page source and returns the notable/interactive elements it detects, each with a short description and an XPath locator (deterministic, no AI call).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Elements detected", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DebugExecutionElementsResultDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<DebugExecutionElementsResultDTOV001> extractElements(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @JsonView(View.Public.POST.class) @RequestBody DebugExecutionElementsRequestDTOV001 elementsRequest,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/debugexecutions", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /debugexecutions/elements called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(this.debugExecutionService.extractPageElements(elementsRequest));
    }

    @PostMapping(path = "/{executionUUID}/highlight", headers = {API_VERSION_1}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Highlight an element in the live browser session of a debug execution.",
            description = "Ad hoc, one-off command against the live robot session — locates the element by XPath and briefly outlines it, bypassing the normal step/action loop entirely.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Command accepted", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DebugExecutionAckDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<DebugExecutionAckDTOV001> highlight(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @PathVariable("executionUUID") String executionUUID,
            @JsonView(View.Public.POST.class) @RequestBody DebugExecutionHighlightRequestDTOV001 highlightRequest,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/debugexecutions", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /debugexecutions/%s/highlight called with URL: %s", executionUUID, request.getRequestURL()), request, login);

        this.debugExecutionService.highlightElement(executionUUID, highlightRequest.getXpath());
        return ResponseWrapper.wrap(DebugExecutionAckDTOV001.builder().executionUUID(executionUUID).accepted(true).build());
    }
}