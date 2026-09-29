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
import org.cerberus.core.api.dto.datalib.TestDataLibDTOV001;
import org.cerberus.core.api.dto.datalib.TestDataLibMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestDataLibService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Tag(name = "DataLib", description = "Endpoints related to Data Libraries and their sub-data")
@RestController
@RequestMapping(path = "/public/datalibs")
public class TestDataLibController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final TestDataLibMapperV001 testDataLibMapper;
    private final ITestDataLibService testDataLibService;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;


    //LIST DATA LIBRARIES (WITHOUT THEIR SUB-DATA)
    @GetMapping(headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List data libraries (without their sub-data)",
            description = "List data libraries (without their sub-data)",
            responses = {
                @ApiResponse(responseCode = "200", description = "List data libraries (without their sub-data)", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TestDataLibDTOV001.class))) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<TestDataLibDTOV001>> findAll(
            @Parameter(description = "Filter on name") @RequestParam(name = "name", required = false) String name,
            @Parameter(description = "Filter on system") @RequestParam(name = "system", required = false) String system,
            @Parameter(description = "Filter on environment") @RequestParam(name = "environment", required = false) String environment,
            @Parameter(description = "Filter on country") @RequestParam(name = "country", required = false) String country,
            @Parameter(description = "Filter on type") @RequestParam(name = "type", required = false) String type,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/datalibs", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /datalibs called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.testDataLibService.readByVariousAPI(name, system, environment, country, type)
                        .stream()
                        .map(this.testDataLibMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    //GET A DATA LIBRARY WITH ITS SUB-DATA
    @GetMapping(path = "/{id}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get a data library with its sub-data",
            description = "Get a data library with its sub-data",
            responses = {
                @ApiResponse(responseCode = "200", description = "Get a data library with its sub-data", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = TestDataLibDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<TestDataLibDTOV001> findByKey(
            @Parameter(description = "Data library id") @PathVariable("id") Integer id,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/datalibs", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /datalibs called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(this.testDataLibMapper.toDTO(this.testDataLibService.readByKeyAPI(id)));
    }

    //CREATE A DATA LIBRARY WITH ITS SUB-DATA
    @PostMapping(headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a data library with its sub-data",
            description = "Create a data library with its sub-data",
            responses = {
                @ApiResponse(responseCode = "201", description = "Create a data library with its sub-data", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = TestDataLibDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<TestDataLibDTOV001> create(
            @Valid @JsonView(View.Public.POST.class) @RequestBody TestDataLibDTOV001 body,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/datalibs", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /datalibs called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.testDataLibMapper.toDTO(
                        this.testDataLibService.createAPI(this.testDataLibMapper.toEntity(body), login)
                )
        );
    }

    //REPLACE A DATA LIBRARY (A PROVIDED SUBDATA LIST REPLACES THE EXISTING ONE)
    @PutMapping(path = "/{id}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Replace a data library (a provided subData list replaces the existing one)",
            description = "Replace a data library (a provided subData list replaces the existing one)",
            responses = {
                @ApiResponse(responseCode = "200", description = "Replace a data library (a provided subData list replaces the existing one)", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = TestDataLibDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<TestDataLibDTOV001> updatePUT(
            @Parameter(description = "Data library id") @PathVariable("id") Integer id,
            @Valid @JsonView(View.Public.PUT.class) @RequestBody TestDataLibDTOV001 body,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/datalibs", "CALL-PUT", LogEvent.STATUS_INFO, String.format("API /datalibs called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.testDataLibMapper.toDTO(
                        this.testDataLibService.updateAPI(id, this.testDataLibMapper.toEntity(body), body.getIgnoreFirstLine(), login, false)
                )
        );
    }

    //PARTIALLY UPDATE A DATA LIBRARY (PROVIDED SUB-DATA ARE MERGED BY NAME)
    @PatchMapping(path = "/{id}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Partially update a data library (provided sub-data are merged by name)",
            description = "Partially update a data library (provided sub-data are merged by name)",
            responses = {
                @ApiResponse(responseCode = "200", description = "Partially update a data library (provided sub-data are merged by name)", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = TestDataLibDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<TestDataLibDTOV001> updatePATCH(
            @Parameter(description = "Data library id") @PathVariable("id") Integer id,
            @Valid @JsonView(View.Public.PATCH.class) @RequestBody TestDataLibDTOV001 body,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/datalibs", "CALL-PATCH", LogEvent.STATUS_INFO, String.format("API /datalibs called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.testDataLibMapper.toDTO(
                        this.testDataLibService.updateAPI(id, this.testDataLibMapper.toEntity(body), body.getIgnoreFirstLine(), login, true)
                )
        );
    }

    //DELETE A DATA LIBRARY AND ITS SUB-DATA
    @DeleteMapping(path = "/{id}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Delete a data library and its sub-data",
            description = "Delete a data library and its sub-data",
            responses = {
                @ApiResponse(responseCode = "200", description = "Delete a data library and its sub-data", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = TestDataLibDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<TestDataLibDTOV001> delete(
            @Parameter(description = "Data library id") @PathVariable("id") Integer id,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/datalibs", "CALL-DELETE", LogEvent.STATUS_INFO, String.format("API /datalibs called with URL: %s", request.getRequestURL()), request, login);

        TestDataLibDTOV001 deleted = this.testDataLibMapper.toDTO(this.testDataLibService.readByKeyAPI(id));
        this.testDataLibService.deleteAPI(id);
        return ResponseWrapper.wrap(deleted);
    }
}
