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
import org.cerberus.api.dto.v001.TestcaseDTOV001;
import org.cerberus.api.dto.views.View;
import org.cerberus.api.mappers.v001.TestcaseMapperV001;
import org.cerberus.api.services.PublicApiAuthenticationService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.exception.CerberusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author MorganLmd
 */
@AllArgsConstructor
@Tag(name = "Testcase")
@Validated
@RestController
@RequestMapping(path = "/public/testcases")
public class TestcaseController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final ITestCaseService testCaseService;
    private final TestcaseMapperV001 testcaseMapper;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private static final Logger LOG = LogManager.getLogger(TestcaseController.class);

    @Operation(summary = "Get all testcases filtered by test")
    @ApiResponse(responseCode = "200", description = "ok",
            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                    schema = @Schema(implementation = TestcaseDTOV001.class)
            ))})
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<List<TestcaseDTOV001>> findTestcasesByTest(
            @PathVariable("testFolderId") String testFolderId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return ResponseWrapper.wrap(
                this.testCaseService.findTestCaseByTest(testFolderId)
                        .stream()
                        .map(this.testcaseMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Get a testcase filtered by testFolderId and testCaseFolderId")
    @ApiResponse(responseCode = "200", description = "ok",
            content = @Content(schema = @Schema(implementation = TestcaseDTOV001.class)))
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}/{testcaseId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<TestcaseDTOV001> findTestcaseByTestAndTestcase(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) throws CerberusException {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return ResponseWrapper.wrap(
                this.testcaseMapper
                        .toDTO(
                                this.testCaseService.findTestCaseByKeyWithDependencies(testFolderId, testcaseId, true).getItem()
                        )
        );
    }

    @Operation(summary = "Create a new Testcase")
    @ApiResponse(responseCode = "200", description = "ok",
            content = @Content(schema = @Schema(implementation = TestcaseDTOV001.class)))
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<TestcaseDTOV001> createTestcase(
            @Valid @JsonView(View.Public.POST.class) @RequestBody TestcaseDTOV001 newTestcase,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) throws CerberusException {
        this.apiAuthenticationService.authenticate(principal, apiKey);

        return ResponseWrapper.wrap(
                this.testcaseMapper.toDTO(
                        this.testCaseService.createTestcaseWithDependenciesAPI(
                                this.testcaseMapper.toEntity(newTestcase)
                        )
                )
        );
    }

    @Operation(summary = "Update a Testcase")
    @ApiResponse(responseCode = "200", description = "ok",
            content = @Content(schema = @Schema(implementation = TestcaseDTOV001.class)))
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "/{testFolderId}/{testcaseId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<TestcaseDTOV001> update(
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("testFolderId") String testFolderId,
            @Valid @JsonView(View.Public.PUT.class) @RequestBody TestcaseDTOV001 testcaseToUpdate,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) throws CerberusException {

        this.apiAuthenticationService.authenticate(principal, apiKey);

        return ResponseWrapper.wrap(
                this.testcaseMapper.toDTO(
                        this.testCaseService.updateTestcaseAPI(
                                testFolderId,
                                testcaseId,
                                this.testcaseMapper.toEntity(testcaseToUpdate)
                        ))
        );
    }
}
