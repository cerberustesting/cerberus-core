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
import org.cerberus.core.api.dto.testcase.LabelDTOV001;
import org.cerberus.core.api.dto.testcase.LabelMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILabelService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Tag(name = "Label", description = "Endpoints related to Labels")
@RestController
@RequestMapping(path = "/public/labels")
public class LabelController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final LabelMapperV001 labelMapper;
    private final ILabelService labelService;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;


    //LIST LABELS
    @GetMapping(headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List labels",
            description = "List labels",
            responses = {
                @ApiResponse(responseCode = "200", description = "List labels", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LabelDTOV001.class))) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<LabelDTOV001>> findAll(
            @Parameter(description = "Filter on system (global labels are included)") @RequestParam(name = "system", required = false) String system,
            @Parameter(description = "Filter on type (STICKER, BATTERY, REQUIREMENT)") @RequestParam(name = "type", required = false) String type,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/labels", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /labels called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.labelService.readByVariousAPI(
                        system == null || system.isEmpty() ? List.of() : List.of(system),
                        type == null || type.isEmpty() ? List.of() : List.of(type))
                        .stream()
                        .map(this.labelMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    //GET A LABEL BY ITS ID
    @GetMapping(path = "/{id}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get a label by its id",
            description = "Get a label by its id",
            responses = {
                @ApiResponse(responseCode = "200", description = "Get a label by its id", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = LabelDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<LabelDTOV001> findByKey(
            @Parameter(description = "Label id") @PathVariable("id") Integer id,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/labels", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /labels called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(this.labelMapper.toDTO(this.labelService.readByKeyAPI(id)));
    }

    //CREATE A LABEL
    @PostMapping(headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a label",
            description = "Create a label",
            responses = {
                @ApiResponse(responseCode = "201", description = "Create a label", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = LabelDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<LabelDTOV001> create(
            @Valid @JsonView(View.Public.POST.class) @RequestBody LabelDTOV001 body,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/labels", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /labels called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.labelMapper.toDTO(
                        this.labelService.createAPI(this.labelMapper.toEntity(body), login)
                )
        );
    }

    //REPLACE A LABEL
    @PutMapping(path = "/{id}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Replace a label",
            description = "Replace a label",
            responses = {
                @ApiResponse(responseCode = "200", description = "Replace a label", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = LabelDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<LabelDTOV001> updatePUT(
            @Parameter(description = "Label id") @PathVariable("id") Integer id,
            @Valid @JsonView(View.Public.PUT.class) @RequestBody LabelDTOV001 body,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/labels", "CALL-PUT", LogEvent.STATUS_INFO, String.format("API /labels called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.labelMapper.toDTO(
                        this.labelService.updateAPI(id, this.labelMapper.toEntity(body), login, false)
                )
        );
    }

    //PARTIALLY UPDATE A LABEL
    @PatchMapping(path = "/{id}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Partially update a label",
            description = "Partially update a label",
            responses = {
                @ApiResponse(responseCode = "200", description = "Partially update a label", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = LabelDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<LabelDTOV001> updatePATCH(
            @Parameter(description = "Label id") @PathVariable("id") Integer id,
            @Valid @JsonView(View.Public.PATCH.class) @RequestBody LabelDTOV001 body,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/labels", "CALL-PATCH", LogEvent.STATUS_INFO, String.format("API /labels called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.labelMapper.toDTO(
                        this.labelService.updateAPI(id, this.labelMapper.toEntity(body), login, true)
                )
        );
    }

    //DELETE A LABEL
    @DeleteMapping(path = "/{id}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Delete a label",
            description = "Delete a label",
            responses = {
                @ApiResponse(responseCode = "200", description = "Delete a label", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = LabelDTOV001.class)) })
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<LabelDTOV001> delete(
            @Parameter(description = "Label id") @PathVariable("id") Integer id,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/labels", "CALL-DELETE", LogEvent.STATUS_INFO, String.format("API /labels called with URL: %s", request.getRequestURL()), request, login);

        LabelDTOV001 deleted = this.labelMapper.toDTO(this.labelService.readByKeyAPI(id));
        this.labelService.deleteAPI(id, login);
        return ResponseWrapper.wrap(deleted);
    }
}
