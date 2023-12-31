/*
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.appservice.AppServiceDTOV001;
import org.cerberus.core.api.dto.appservice.AppServiceMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.ILogEventService;
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
@Api(tags = "Service")
@RestController
@RequestMapping(path = "/public/services")
public class AppServiceController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final PublicApiAuthenticationService apiAuthenticationService;
    private static final Logger LOG = LogManager.getLogger(AppServiceController.class);
    private final AppServiceMapperV001 appServiceMapper;
    private final IAppServiceService appServiceService;
    private final ILogEventService logEventService;

    @ApiOperation("Get a service by its service name")
    @ApiResponse(code = 200, message = "ok", response = AppServiceDTOV001.class)
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{service}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<AppServiceDTOV001> findByKey(
            @PathVariable("service") String service,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {
        
        logEventService.createForPublicCalls("/public/services", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /services called with URL: %s", request.getRequestURL()), request);
        this.apiAuthenticationService.authenticate(principal, apiKey);
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

    @ApiOperation("Create a service")
    @ApiResponse(code = 200, message = "ok")
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<AppServiceDTOV001> create(
            @Valid @JsonView(View.Public.POST.class) @RequestBody AppServiceDTOV001 serviceDTO,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {
        
        logEventService.createForPublicCalls("/public/services", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /services called with URL: %s", request.getRequestURL()), request);
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return ResponseWrapper.wrap(
                this.appServiceMapper.toDTO(
                        this.appServiceService.createAPI(
                                this.appServiceMapper.toEntity(serviceDTO)
                        )
                )
        );
    }

    @ApiOperation("Update a service")
    @ApiResponse(code = 200, message = "ok")
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "/{service}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<AppServiceDTOV001> update(
            @PathVariable("service") String service,
            @Valid @JsonView(View.Public.PUT.class) @RequestBody AppServiceDTOV001 serviceDTO,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {
        
        logEventService.createForPublicCalls("/public/services", "CALL-PUT", LogEvent.STATUS_INFO, String.format("API /services called with URL: %s", request.getRequestURL()), request);
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return ResponseWrapper.wrap(
                this.appServiceMapper.toDTO(
                        this.appServiceService.updateAPI(
                                service,
                                this.appServiceMapper.toEntity(serviceDTO)
                        )
                )
        );
    }
}
