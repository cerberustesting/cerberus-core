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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
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
@Api(tags = "Queued Execution")
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

    @ApiOperation(value = "Add a testcases list to the execution queue", notes = "testcases, countries, environments and robots are required.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Test cases successfully added to the execution queue", response = QueuedExecutionResultDTOV001.class),})
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(headers = {API_VERSION_1}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

    @ApiOperation(value = "Add a campaign to the execution queue", notes = "You can override the default campaign parameters with the JSON body. \n "
            + "Write in the JSON only the parameters you want to override. \n"
            + "If you don't want to override parameters, send an empty json ({}).")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Campaign successfully added to the execution queue.", response = QueuedExecutionResultDTOV001.class),
        @ApiResponse(code = 404, message = "Campaign doesn't exist.")
    })
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{campaignId}", headers = {API_VERSION_1}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
