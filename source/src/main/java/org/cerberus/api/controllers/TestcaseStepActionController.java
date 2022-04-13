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
import org.cerberus.api.dto.v001.TestcaseStepActionDTOV001;
import org.cerberus.api.mappers.v001.TestcaseStepActionMapperV001;
import org.cerberus.api.services.PublicApiAuthenticationService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mlombard
 */
@AllArgsConstructor
@Api(tags = "Testcase Action")
@RestController
@RequestMapping(path = "/public/testcasestepactions")
public class TestcaseStepActionController {

    private static final Logger LOG = LogManager.getLogger(TestcaseStepActionController.class);

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final TestcaseStepActionMapperV001 actionMapper;
    private final ITestCaseStepActionService actionService;
    private final PublicApiAuthenticationService apiAuthenticationService;

    @ApiOperation("Find a testcaseStepAction by key (testFolderId, testcaseId, stepId, actionId)")
    @ApiResponse(code = 200, message = "operation successful", response = TestcaseStepActionDTOV001.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}/{actionId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public TestcaseStepActionDTOV001 findActionByKey(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("stepId") int stepId,
            @PathVariable("actionId") int actionId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return this.actionMapper.toDTO(
                this.actionService.findTestCaseStepActionbyKey(
                        testFolderId, testcaseId, stepId, actionId)
        );
    }

    @ApiOperation("Find actions by testcase step (testFolderId, testcaseId, stepId)")
    @ApiResponse(code = 200, message = "operation successful", response = TestcaseStepActionDTOV001.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TestcaseStepActionDTOV001> findActionsByStep(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("stepId") int stepId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return this.actionService.readByVarious1WithDependency(testFolderId, testcaseId, stepId)
                .getDataList()
                .stream()
                .map(this.actionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
