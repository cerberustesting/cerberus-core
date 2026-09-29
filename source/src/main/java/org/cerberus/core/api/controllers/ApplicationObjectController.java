/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.application.ApplicationObjectDTOV001;
import org.cerberus.core.api.dto.application.ApplicationObjectMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.IApplicationObjectService;
import org.cerberus.core.crud.service.ILogEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Tag(name = "ApplicationObject", description = "Endpoints related to Application Objects")
@RestController
@RequestMapping(path = "/public/applicationobjects")
public class ApplicationObjectController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final ApplicationObjectMapperV001 applicationObjectMapper;
    private final IApplicationObjectService applicationObjectService;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;


    //LIST THE OBJECTS OF AN APPLICATION
    @GetMapping(path = "/{application}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List the objects of an application",
            description = "List the objects of an application",
            responses = {
                @ApiResponse(responseCode = "200", description = "List the objects of an application", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ApplicationObjectDTOV001.class))) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<ApplicationObjectDTOV001>> findByApplication(
            @Parameter(description = "Application name") @PathVariable("application") String application,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applicationobjects", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /applicationobjects called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.applicationObjectService.readByApplicationAPI(application)
                        .stream()
                        .map(this.applicationObjectMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    //GET AN APPLICATION OBJECT
    @GetMapping(path = "/{application}/{object}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get an application object",
            description = "Get an application object",
            responses = {
                @ApiResponse(responseCode = "200", description = "Get an application object", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationObjectDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<ApplicationObjectDTOV001> findByKey(
            @Parameter(description = "Application name") @PathVariable("application") String application,
            @Parameter(description = "Object name") @PathVariable("object") String object,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applicationobjects", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /applicationobjects called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.applicationObjectMapper.toDTO(
                        this.applicationObjectService.readByKeyAPI(application, object)
                )
        );
    }

    //CREATE AN APPLICATION OBJECT
    @PostMapping(path = "/{application}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create an application object",
            description = "Create an application object",
            responses = {
                @ApiResponse(responseCode = "201", description = "Create an application object", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationObjectDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<ApplicationObjectDTOV001> create(
            @Parameter(description = "Application name") @PathVariable("application") String application,
            @Valid @JsonView(View.Public.POST.class) @RequestBody ApplicationObjectDTOV001 body,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applicationobjects", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /applicationobjects called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.applicationObjectMapper.toDTO(
                        this.applicationObjectService.createAPI(application, this.applicationObjectMapper.toEntity(body), login)
                )
        );
    }

    //REPLACE AN APPLICATION OBJECT
    @PutMapping(path = "/{application}/{object}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Replace an application object",
            description = "Replace an application object",
            responses = {
                @ApiResponse(responseCode = "200", description = "Replace an application object", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationObjectDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<ApplicationObjectDTOV001> updatePUT(
            @Parameter(description = "Application name") @PathVariable("application") String application,
            @Parameter(description = "Object name") @PathVariable("object") String object,
            @Valid @JsonView(View.Public.PUT.class) @RequestBody ApplicationObjectDTOV001 body,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applicationobjects", "CALL-PUT", LogEvent.STATUS_INFO, String.format("API /applicationobjects called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.applicationObjectMapper.toDTO(
                        this.applicationObjectService.updateAPI(application, object, this.applicationObjectMapper.toEntity(body), login, false)
                )
        );
    }

    //PARTIALLY UPDATE AN APPLICATION OBJECT
    @PatchMapping(path = "/{application}/{object}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Partially update an application object",
            description = "Partially update an application object",
            responses = {
                @ApiResponse(responseCode = "200", description = "Partially update an application object", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationObjectDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<ApplicationObjectDTOV001> updatePATCH(
            @Parameter(description = "Application name") @PathVariable("application") String application,
            @Parameter(description = "Object name") @PathVariable("object") String object,
            @Valid @JsonView(View.Public.PATCH.class) @RequestBody ApplicationObjectDTOV001 body,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applicationobjects", "CALL-PATCH", LogEvent.STATUS_INFO, String.format("API /applicationobjects called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.applicationObjectMapper.toDTO(
                        this.applicationObjectService.updateAPI(application, object, this.applicationObjectMapper.toEntity(body), login, true)
                )
        );
    }

    //DELETE AN APPLICATION OBJECT
    @DeleteMapping(path = "/{application}/{object}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Delete an application object",
            description = "Delete an application object",
            responses = {
                @ApiResponse(responseCode = "200", description = "Delete an application object", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationObjectDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<ApplicationObjectDTOV001> delete(
            @Parameter(description = "Application name") @PathVariable("application") String application,
            @Parameter(description = "Object name") @PathVariable("object") String object,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/applicationobjects", "CALL-DELETE", LogEvent.STATUS_INFO, String.format("API /applicationobjects called with URL: %s", request.getRequestURL()), request, login);

        ApplicationObjectDTOV001 deleted = this.applicationObjectMapper.toDTO(this.applicationObjectService.readByKeyAPI(application, object));
        this.applicationObjectService.deleteAPI(application, object);
        return ResponseWrapper.wrap(deleted);
    }
}
