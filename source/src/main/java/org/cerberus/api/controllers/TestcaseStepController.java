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
package org.cerberus.api.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.api.dto.v001.TestcaseStepDTOV001;
import org.cerberus.api.dto.views.View;
import org.cerberus.api.mappers.v001.TestcaseStepMapperV001;
import org.cerberus.api.services.PublicApiAuthenticationService;
import org.cerberus.api.services.TestcaseStepApiService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author MorganLmd
 */
@AllArgsConstructor
@Tag(name = "Testcase Step")
@RestController
@RequestMapping(path = "/public/testcasesteps")
public class TestcaseStepController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final ITestCaseStepService testCaseStepService;
    private final TestcaseStepApiService testcaseStepApiService;
    private final TestcaseStepMapperV001 stepMapper;
    private final PublicApiAuthenticationService apiAuthenticationService;

    private static final Logger LOG = LogManager.getLogger(TestcaseStepController.class);

    @Operation(summary = "Get all TestcaseSteps")
    @ApiResponse(responseCode = "200", description = "ok",
            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                    schema = @Schema(implementation = TestcaseStepDTOV001.class)
            ))})
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<List<TestcaseStepDTOV001>> findAll(
            @RequestParam(name = "islibrarystep", defaultValue = "false") boolean isLibraryStep,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);

        return ResponseWrapper.wrap(
                this.testcaseStepApiService
                        .findAllWithProperties(isLibraryStep)
                        .stream()
                        .map(this.stepMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Get all testcase steps from a test folder")
    @ApiResponse(responseCode = "200", description = "ok",
            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                    schema = @Schema(implementation = TestcaseStepDTOV001.class)
            ))})
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<List<TestcaseStepDTOV001>> findAllByTestFolderId(
            @PathVariable("testFolderId") String testFolderId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return ResponseWrapper.wrap(
                this.testcaseStepApiService.findByTestFolderId(testFolderId)
                        .stream()
                        .map(this.stepMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Get all testcase steps of a testcase")
    @ApiResponse(responseCode = "200", description = "ok",
            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                    schema = @Schema(implementation = TestcaseStepDTOV001.class)
            ))})
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}/{testcaseId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<List<TestcaseStepDTOV001>> findAllByTestFolderIdTestcaseId(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return ResponseWrapper.wrap(
                this.testCaseStepService.readByTestTestCaseAPI(testFolderId, testcaseId)
                        .stream()
                        .map(this.stepMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Get a Testcase Step by key (testFolderId and testcaseId and stepId)")
    @ApiResponse(responseCode = "200", description = "ok",
            content = @Content(schema = @Schema(implementation = TestcaseStepDTOV001.class)))
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<TestcaseStepDTOV001> findByKey(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("stepId") int stepId,
            @RequestParam(name = "islibrarystep", defaultValue = "false") boolean isLibraryStep,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
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
