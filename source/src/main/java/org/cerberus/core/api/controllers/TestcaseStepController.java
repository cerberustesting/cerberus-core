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
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
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
    private final ILogEventService logEventService;

    private static final Logger LOG = LogManager.getLogger(TestcaseStepController.class);

    @ApiOperation("Get all Testcase Steps")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV001.class, responseContainer = "List")
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(headers = {API_VERSION_1}, produces = "application/json")
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

    @ApiOperation("Get all Testcase Steps from a test folder")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV001.class, responseContainer = "List")
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
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

    @ApiOperation("Get all Testcase Steps of a testcase")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV001.class, responseContainer = "List")
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}/{testcaseId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
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

    @ApiOperation("Get a Testcase Step by its key (testFolderId and testcaseId and stepId)")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV001.class)
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
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
