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
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.application.*;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.services.ApplicationApiService;
import org.cerberus.core.api.services.ApplicationEnvironmentApiService;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mlombard
 */
@AllArgsConstructor
@Tag(name = "Application", description = "Endpoints related to Applications")
@RestController
@RequestMapping(path = "/public/applications")
public class ApplicationController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final ApplicationApiService applicationApiService;
    private final ApplicationEnvironmentApiService applicationEnvironmentApiService;
    private final ApplicationMapperV001 applicationMapper;
    private final CountryEnvironmentParametersMapperV001 applicationEnvironmentMapper;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;

    private static final Logger LOG = LogManager.getLogger(ApplicationController.class);


    //FIND APPLICATION BY APPLICATION NAME
    @GetMapping(path = "/{application}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get Application",
        description = "Get an application by its application name",
        responses = {
            @ApiResponse(responseCode = "200", description = "Found the application", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = ApplicationDTOV001.class))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<ApplicationDTOV001> findApplicationByIdName(
        @Parameter(description = "Application name") @PathVariable("application") String application,
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applications", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /applications called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.applicationMapper.toDTO(
                        this.applicationApiService.readByKeyWithDependency(application)
                )
        );
    }

    //FIND APPLICATION_COUNTRY_ENVIRONMENT BY NAMES
    @GetMapping(path = "/{application}/{country}/{environment}",headers = API_VERSION_1,produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get an application environment with its associated endpoint",
        description = "Fetches application environment parameters for a given application, country, and environment",
        responses = {
            @ApiResponse(responseCode = "200", description = "Found the application / country / environment", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = ResponseWrapper.class))}),
        }
    )
    @ResponseStatus(HttpStatus.OK)
    @JsonView(View.Public.GET.class)
    public ResponseWrapper<CountryEnvironmentParametersDTOV001> findApplicationEnvironmentByIds(
            @Parameter(description = "Application name") @PathVariable("application") String application,
            @Parameter(description = "Country") @PathVariable("country") String country,
            @Parameter(description = "Environment name") @PathVariable("environment") String environment,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal
    ) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applications", "CALL-GET", LogEvent.STATUS_INFO,
                String.format("API /applications called with URL: %s", request.getRequestURL()), request, login);

        Application applicationObj = this.applicationApiService.readByKey(application);
        if (applicationObj == null) {
            throw new EntityNotFoundException(Application.class, "application", application);
        }

        String system = applicationObj.getSystem();
        return ResponseWrapper.wrap(
                this.applicationEnvironmentMapper.toDTO(
                        this.applicationEnvironmentApiService.readByKey(system, application, country, environment)
                )
        );
    }

    //PUT APPLICATION_COUNTRY_ENVIRONMENT
    @PutMapping(path = "/{application}/{country}/{environment}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Update Application Country Environment",
        description = "Update an application country environment",
        responses = {
            @ApiResponse(responseCode = "200", description = "Put the application / country / environment", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = ResponseWrapper.class))}),
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<CountryEnvironmentParametersDTOV001> updatePUT(
        @Parameter(description = "Application name") @PathVariable("application") String applicationId,
        @Parameter(description = "Country") @PathVariable("country") String countryId,
        @Parameter(description = "Environment name") @PathVariable("environment") String environmentId,
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Valid @JsonView(View.Public.PUT.class) @RequestBody CountryEnvironmentParametersDTOV001 applicationEnvironmentToUpdate,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applications", "CALL-PUT", LogEvent.STATUS_INFO, String.format("API /applications called with URL: %s", request.getRequestURL()), request, login);

        // We first get the application in order to retreive the system.
        Application applicationObj = this.applicationApiService.readByKey(applicationId);
        if (applicationObj == null) {
            throw new EntityNotFoundException(Application.class, "application", applicationId);
        }
        String system = applicationObj.getSystem();

        return ResponseWrapper.wrap(
                this.applicationEnvironmentMapper.toDTO(
                        this.applicationEnvironmentApiService.updateApplicationEnvironmentPUT(
                                system,
                                applicationId,
                                countryId,
                                environmentId,
                                this.applicationEnvironmentMapper.toEntity(applicationEnvironmentToUpdate),
                                principal,
                                login
                        ))
        );
    }

    //PATCH APPLICATION_COUNTRY_ENVIRONMENT
    @PatchMapping(path = "/{application}/{country}/{environment}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Patch Application Country Environment",
        description = "Patch an application country environment",
        responses = {
            @ApiResponse(responseCode = "200", description = "Patch the application / country / environment", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = ResponseWrapper.class))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<CountryEnvironmentParametersDTOV001> updatePATCH(
        @Parameter(description = "Application name") @PathVariable("application") String applicationId,
        @Parameter(description = "Country") @PathVariable("country") String countryId,
        @Parameter(description = "Environment name") @PathVariable("environment") String environmentId,
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Valid @JsonView(View.Public.PATCH.class) @RequestBody CountryEnvironmentParametersDTOV001 applicationEnvironmentToUpdate,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applications", "CALL-PATCH", LogEvent.STATUS_INFO, String.format("API /applications called with URL: %s", request.getRequestURL()), request, login);

        // We first get the application in order to retreive the system.
        Application applicationObj = this.applicationApiService.readByKey(applicationId);
        if (applicationObj == null) {
            throw new EntityNotFoundException(Application.class, "application", applicationId);
        }
        String system = applicationObj.getSystem();

        return ResponseWrapper.wrap(
                this.applicationEnvironmentMapper.toDTO(
                        this.applicationEnvironmentApiService.updateApplicationEnvironmentPATCH(
                                system,
                                applicationId,
                                countryId,
                                environmentId,
                                applicationEnvironmentToUpdate,
                                principal,
                                login
                        ))
        );
    }
}
