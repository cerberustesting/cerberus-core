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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.appservice.AppServiceDTOV001;
import org.cerberus.core.api.dto.queueexecution.QueuedExecutionDTOV001;
import org.cerberus.core.api.dto.queueexecution.QueuedExecutionMapperV001;
import org.cerberus.core.api.dto.queueexecution.QueuedExecutionResultDTOV001;
import org.cerberus.core.api.dto.queueexecution.QueuedExecutionResultMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.api.services.QueuedExecutionService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author lucashimpens
 */
@AllArgsConstructor
@Tag(name = "Queued Execution", description = "Endpoints related to Queued Execution")
@Validated
@RestController
@RequestMapping(path = "/public/queuedexecutions/")
public class QueuedExecutionController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final PublicApiAuthenticationService apiAuthenticationService;
    private final QueuedExecutionService queuedExecutionService;
    private final QueuedExecutionMapperV001 queuedExecutionMapper;
    private final ILogEventService logEventService;
    private final QueuedExecutionResultMapperV001 queuedExecutionResultMapper;

    @PostMapping(headers = {API_VERSION_1}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Add a testcases list to the execution queue.",
            description = "Add a testcases list to the execution queue. Testcases, countries, environments and robots are required.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test cases successfully added to the execution queue", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = QueuedExecutionResultDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<QueuedExecutionResultDTOV001> addTestcasesToExecutionQueue(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @JsonView(View.Public.POST.class) @RequestBody(required = false) QueuedExecutionDTOV001 queuedExecution,
            HttpServletRequest request,
            Principal principal) {
        
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/queuedexecutions", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /queuedexecutions called with URL: %s", request.getRequestURL()), request, login);
        
        return ResponseWrapper.wrap(
                this.queuedExecutionResultMapper.toDto(
                        this.queuedExecutionService.addTestcasesToExecutionQueue(
                                this.queuedExecutionMapper.toEntity(queuedExecution),
                                principal
                        )
                )
        );
    }

    @PostMapping(path = "/{campaignId}", headers = {API_VERSION_1}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Add a campaign to the execution queue.",
            description = "Add a campaign to the execution queue. You can override the default campaign parameters with the JSON body. Write in the JSON only the parameters you want to override. If you don't want to override parameters, send an empty json ({}).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test cases successfully added to the execution queue", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = QueuedExecutionResultDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<QueuedExecutionResultDTOV001> addCampaignToExecutionQueue(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            @JsonView(View.Public.POST.class) @RequestBody(required = false) QueuedExecutionDTOV001 queuedExecution,
            @PathVariable("campaignId") String campaignId,
            HttpServletRequest request,
            Principal principal) {
        
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/queuedexecutions", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /queuedexecutions called with URL: %s", request.getRequestURL()), request, login);
        
        return ResponseWrapper.wrap(
                this.queuedExecutionResultMapper.toDto(
                        this.queuedExecutionService.addCampaignToExecutionQueue(
                                campaignId,
                                this.queuedExecutionMapper.toEntity(queuedExecution),
                                principal
                        )
                )
        );
    }
}
