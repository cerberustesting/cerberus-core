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
import org.cerberus.core.api.dto.invariant.InvariantDTOV001;
import org.cerberus.core.api.dto.invariant.InvariantMapperV001;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionDTOV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.InvariantApiService;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.exception.CerberusException;
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
@Tag(name = "Invariant", description = "Endpoints related to Invariants")
@RestController
@RequestMapping(path = "/public/invariants")
public class InvariantController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final InvariantApiService invariantApiService;
    private final InvariantMapperV001 invariantMapper;
    private final ILogEventService logEventService;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private static final Logger LOG = LogManager.getLogger(InvariantController.class);

    @GetMapping(path = "/{idName}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get all invariants filtered by idName",
            description = "Get all invariants filtered by idName",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the invariants", content = { @Content(mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = InvariantDTOV001.class)))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<InvariantDTOV001>> findInvariantByIdName(
            @PathVariable("idName") String idName,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/invariants", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /invariants called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.invariantApiService.readyByIdName(idName)
                        .stream()
                        .map(this.invariantMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(path = "/{idName}/{value}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get invariant filtered by idName and value",
            description = "Get invariant filtered by idName and value",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the invariant", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = InvariantDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<InvariantDTOV001> findInvariantByIdNameAndValue(
            @PathVariable("idName") String idName,
            @PathVariable("value") String value,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/invariants", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /invariants called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.invariantMapper.toDTO(
                        this.invariantApiService.readByKey(idName, value)
                )
        );
    }

}
