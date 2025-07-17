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
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.application.CountryEnvironmentParametersDTOV001;
import org.cerberus.core.api.dto.appservice.AppServiceCallDTO;
import org.cerberus.core.api.dto.appservice.AppServiceDTOV001;
import org.cerberus.core.api.dto.appservice.AppServiceMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.appservice.IServiceService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Tag(name = "Service", description = "Endpoints related to Application Services")
@RestController
@RequestMapping(path = "/public/services")
public class AppServiceController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final PublicApiAuthenticationService apiAuthenticationService;
    private static final Logger LOG = LogManager.getLogger(AppServiceController.class);
    private final AppServiceMapperV001 appServiceMapper;
    private final IAppServiceService appServiceService;
    private final IServiceService serviceService;
    private final ILogEventService logEventService;

    //FIND SERVICE BY SERVICE NAMES
    @GetMapping(path = "/{service}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get a service by its service name",
        description = "Get a service by its service name",
        responses = {
            @ApiResponse(responseCode = "200", description = "Found the service", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = AppServiceDTOV001.class))})
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<AppServiceDTOV001> findByKey(
            @Parameter(description = "Service name") @PathVariable("service") String service,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/services", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /services called with URL: %s", request.getRequestURL()), request, login);

        Optional<AppService> appServiceOptional = Optional.ofNullable(this.appServiceService.readByKeyWithDependency(service).getItem());
        if (appServiceOptional.isPresent()) {
            return ResponseWrapper.wrap(
                    this.appServiceMapper.toDTO(
                            appServiceOptional.get()
                    )
            );
        } else {
            throw new EntityNotFoundException(AppService.class, "service", service);
        }
    }

    //CREATE SERVICE
    @PostMapping(headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Create a service",
        description = "Create a service",
        responses = {
            @ApiResponse(responseCode = "200", description = "Created the service", content = @Content)
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<AppServiceDTOV001> create(
            @Valid @JsonView(View.Public.POST.class) @RequestBody AppServiceDTOV001 serviceDTO,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/services", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /services called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.appServiceMapper.toDTO(
                        this.appServiceService.createAPI(
                                this.appServiceMapper.toEntity(serviceDTO),
                                login
                        )
                )
        );
    }

    //UPDATE SERVICE BY SERVICE NAMES
    @PutMapping(path = "/{service}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update a service",
            description = "Update a service",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated the service", content = @Content)
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<AppServiceDTOV001> update(
            @Parameter(description = "Service name") @PathVariable("service") String service,
            @Valid @JsonView(View.Public.PUT.class) @RequestBody AppServiceDTOV001 serviceDTO,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/services", "CALL-PUT", LogEvent.STATUS_INFO, String.format("API /services called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.appServiceMapper.toDTO(
                        this.appServiceService.updateAPI(
                                service,
                                this.appServiceMapper.toEntity(serviceDTO),
                                login
                        )
                )
        );
    }

    //CALL SERVICE BY SERVICE NAMES
    @PostMapping(path = "/call/{service}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Call a service and return the result",
            description = "Call a service and return the result\"",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Called the service", content = @Content)
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<String> call(
            @Parameter(description = "Service name") @PathVariable("service") String service,
            @Valid @JsonView(View.Public.POST.class) @RequestBody AppServiceCallDTO serviceCallDTO,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/services/call", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /services/call called with URL: %s", request.getRequestURL()), request, login);

        JSONObject result = new JSONObject();

        AnswerItem<AppService> ans = serviceService.callAPI(service, serviceCallDTO.getCountry(), serviceCallDTO.getEnvironment(), serviceCallDTO.getApplication(), serviceCallDTO.getSystem(),
                serviceCallDTO.getTimeout(), serviceCallDTO.getKafkanb(), serviceCallDTO.getKafkaTime(), serviceCallDTO.getProps(), login);

        // If the service to call is a temporary create service, we clean it right after the call.
        if (service.contains(AppService.SERVICENAME_SIMULATIONCALL)) {
            Answer ans1 = appServiceService.delete(
                    AppService.builder().service(service).build()
            );
        }

        if (ans != null) {
            try {
                result.put("message", ans.getMessageDescription());
                result.put("messageCode", ans.getMessageCodeString());
                if (ans.getItem() != null) {
                    result.put("call", ans.getItem().toJSONOnExecution());
                }
            } catch (JSONException ex) {
                LOG.error(ex, ex);
            }
        }

        return ResponseWrapper.wrap(result.toString());

    }

}
