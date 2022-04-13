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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.api.dto.v001.TestcaseStepDTOV001;
import org.cerberus.api.mappers.v001.TestcaseStepMapperV001;
import org.cerberus.api.services.PublicApiAuthenticationService;
import org.cerberus.api.services.TestcaseStepApiService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author MorganLmd
 */
@AllArgsConstructor
@Api(tags = "Testcase Step")
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

    @ApiOperation("Get all TestcaseSteps")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV001.class, responseContainer = "List")
    @GetMapping(headers = {API_VERSION_1}, produces = "application/json")
    public List<TestcaseStepDTOV001> findAll(
            @RequestParam(name = "islibrarystep", defaultValue = "false") boolean isLibraryStep,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);

        return this.testcaseStepApiService
                .findAllWithProperties(isLibraryStep)
                .stream()
                .map(this.stepMapper::toDTO)
                .collect(Collectors.toList());
    }

    @ApiOperation("Get all testcase steps from a test folder")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV001.class, responseContainer = "List")
    @GetMapping(path = "/{testFolderId}", headers = {API_VERSION_1}, produces = "application/json")
    public List<TestcaseStepDTOV001> findAllByTestFolderId(
            @PathVariable("testFolderId") String testFolderId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return this.testcaseStepApiService.findByTestFolderId(testFolderId)
                .stream()
                .map(this.stepMapper::toDTO)
                .collect(Collectors.toList());
    }

    @ApiOperation("Get all testcase steps of a testcase")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV001.class, responseContainer = "List")
    @GetMapping(path = "/{testFolderId}/{testcaseId}", headers = {API_VERSION_1}, produces = "application/json")
    public List<TestcaseStepDTOV001> findAllByTestFolderIdTestcaseId(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return this.testCaseStepService.readByTestTestCaseAPI(testFolderId, testcaseId)
                .stream()
                .map(this.stepMapper::toDTO)
                .collect(Collectors.toList());
    }

    @ApiOperation("Get a Testcase Step by key (testFolderId and testcaseId and stepId)")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV001.class)
    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}", headers = {API_VERSION_1}, produces = "application/json")
    public TestcaseStepDTOV001 findByKey(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("stepId") int stepId,
            @RequestParam(name = "islibrarystep", defaultValue = "false") boolean isLibraryStep,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return this.stepMapper.toDTO(
                this.testCaseStepService.readTestcaseStepWithDependenciesAPI(
                        testFolderId,
                        testcaseId,
                        stepId
                )
        );
    }
}
