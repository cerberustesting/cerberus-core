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
import org.cerberus.core.api.dto.debugexecution.RemoteViewUrlsRequestDTOV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.ExecutionRemoteControlService;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Lets an external process attached to a running execution push back live view / remote-control
 * URLs for that execution. Mirrors, for a push model, what {@link DebugExecutionController} does
 * for a pull (front-end driven) model.
 *
 * @author bcivel
 */
@AllArgsConstructor
@Tag(name = "Execution Remote Control", description = "Endpoints letting an external service report live view / remote-control URLs for a running Test Case Execution")
@Validated
@RestController
@RequestMapping(path = "/public/executions/")
public class ExecutionRemoteControlController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ExecutionRemoteControlService executionRemoteControlService;
    private final ILogEventService logEventService;

    @PostMapping(path = "/{executionUUID}/remoteviewurls", headers = {API_VERSION_1}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Report the live view / remote-control URLs of a running execution.",
            description = "Called by an external service to attach live view (read-only) and/or remote-control (interactive) URLs to the running execution identified by executionUUID. Stored in-memory only, exactly like the URLs Cerberus resolves itself from Selenium 4 node stereotypes (crb:remoteUrl / crb:remoteControlUrl) for web executions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "URLs accepted", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DebugExecutionAckDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<DebugExecutionAckDTOV001> setRemoteViewUrls(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @PathVariable("executionUUID") String executionUUID,
            @JsonView(View.Public.POST.class) @RequestBody RemoteViewUrlsRequestDTOV001 remoteViewUrlsRequest,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/executions", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /executions/%s/remoteviewurls called with URL: %s", executionUUID, request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(this.executionRemoteControlService.setRemoteViewUrls(executionUUID, remoteViewUrlsRequest.getRemoteLiveUrl(), remoteViewUrlsRequest.getRemoteControlUrl()));
    }
}