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
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.application.ApplicationDTOV001;
import org.cerberus.core.api.dto.application.ApplicationMapperV001;
import org.cerberus.core.api.dto.application.CountryEnvironmentParametersDTOV001;
import org.cerberus.core.api.dto.application.CountryEnvironmentParametersMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.services.ApplicationApiService;
import org.cerberus.core.api.services.ApplicationEnvironmentApiService;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.exception.CerberusException;
import org.json.JSONObject;
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
@Api(tags = "Application")
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

    @ApiOperation("Get an application by its application name")
    @ApiResponse(code = 200, message = "operation successful", response = ApplicationDTOV001.class, responseContainer = "List")
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{application}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<ApplicationDTOV001> findApplicationByIdName(
            @PathVariable("application") String application,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applications", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /applications called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.applicationMapper.toDTO(
                        this.applicationApiService.readByKeyWithDependency(application)
                )
        );
    }

    @ApiOperation("Get an application environment with its associated endpoint")
    @ApiResponse(code = 200, message = "operation successful", response = CountryEnvironmentParametersDTOV001.class, responseContainer = "List")
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{application}/{country}/{environment}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<CountryEnvironmentParametersDTOV001> findApplicationEnvironmentByIds(
            @PathVariable("application") String application,
            @PathVariable("country") String country,
            @PathVariable("environment") String environment,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applications", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /applications called with URL: %s", request.getRequestURL()), request, login);

        // We first get the application in order to retreive the system.
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

    @ApiOperation("Update an application environment")
    @ApiResponse(code = 200, message = "ok")
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "/{application}/{country}/{environment}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<CountryEnvironmentParametersDTOV001> updatePUT(
            @PathVariable("application") String applicationId,
            @PathVariable("country") String countryId,
            @PathVariable("environment") String environmentId,
            @Valid @JsonView(View.Public.PUT.class) @RequestBody CountryEnvironmentParametersDTOV001 applicationEnvironmentToUpdate,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

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

    @ApiOperation("Update an application environment")
    @ApiResponse(code = 200, message = "ok")
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{application}/{country}/{environment}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<CountryEnvironmentParametersDTOV001> updatePATCH(
            @PathVariable("application") String applicationId,
            @PathVariable("country") String countryId,
            @PathVariable("environment") String environmentId,
            @Valid @JsonView(View.Public.PATCH.class) @RequestBody CountryEnvironmentParametersDTOV001 applicationEnvironmentToUpdate,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

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
