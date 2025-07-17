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
import org.cerberus.core.api.dto.appservice.AppServiceDTOV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepDTOV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.api.services.TestcaseStepApiService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MorganLmd
 */
@AllArgsConstructor
@Tag(name = "Testcase Step", description = "Operations related to Testcase Step")
@RestController
@RequestMapping(path = "/public/testcasesteps")
public class TestcaseStepController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final ITestCaseStepService testCaseStepService;
    private final TestcaseStepApiService testcaseStepApiService;
    private final TestcaseStepMapperV001 stepMapper;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;

    private static final Logger LOG = LogManager.getLogger(TestcaseStepController.class);

    @GetMapping(headers = {API_VERSION_1}, produces = "application/json")
    @Operation(
            summary = "Get all Testcase Steps",
            description = "Get all Testcase Steps",
            responses = {
                @ApiResponse(responseCode = "200", description = "Found the steps", content = { @Content(mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = TestcaseStepDTOV001.class)))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<TestcaseStepDTOV001>> findAll(
            @RequestParam(name = "islibrarystep", defaultValue = "false") boolean isLibraryStep,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {
        
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcasesteps", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /testcasesteps called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.testcaseStepApiService
                        .findAllWithProperties(isLibraryStep)
                        .stream()
                        .map(this.stepMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(path = "/{testFolderId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get all Testcase Steps from a test folder",
            description = "Get all Testcase Steps from a test folder",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the steps", content = { @Content(mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = TestcaseStepDTOV001.class)))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<TestcaseStepDTOV001>> findAllByTestFolderId(
            @PathVariable("testFolderId") String testFolderId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {
        
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcasesteps", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /testcasesteps called with URL: %s", request.getRequestURL()), request, login);
        
        return ResponseWrapper.wrap(
                this.testcaseStepApiService.findByTestFolderId(testFolderId)
                        .stream()
                        .map(this.stepMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(path = "/{testFolderId}/{testcaseId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get all Testcase Steps of a testcase",
            description = "Get all Testcase Steps of a testcase",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the steps", content = { @Content(mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = TestcaseStepDTOV001.class)))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<TestcaseStepDTOV001>> findAllByTestFolderIdTestcaseId(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {
        
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcasesteps", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /testcasesteps called with URL: %s", request.getRequestURL()), request, login);
        
        return ResponseWrapper.wrap(
                this.testCaseStepService.readByTestTestCaseAPI(testFolderId, testcaseId)
                        .stream()
                        .map(this.stepMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get a Testcase Step by its key (testFolderId and testcaseId and stepId)",
            description = "Get a Testcase Step by its key (testFolderId and testcaseId and stepId)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the step", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = TestcaseStepDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<TestcaseStepDTOV001> findByKey(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("stepId") int stepId,
            @RequestParam(name = "islibrarystep", defaultValue = "false") boolean isLibraryStep,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {
        
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcasesteps", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /testcasesteps called with URL: %s", request.getRequestURL()), request, login);
        
        return ResponseWrapper.wrap(
                this.stepMapper.toDTO(
                        this.testCaseStepService.readTestcaseStepWithDependenciesAPI(
                                testFolderId,
                                testcaseId,
                                stepId
                        )
                )
        );
    }
}
