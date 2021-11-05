/**
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
package org.cerberus.api.controller.v001;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import java.security.Principal;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.api.dto.v001.TestcaseStepActionDTOV001;
import org.cerberus.api.mapper.v001.TestcaseStepActionMapperV001;
import org.cerberus.api.service.PublicApiAuthenticationService;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mlombard
 */
@AllArgsConstructor
@Api(tags = "Testcase Action")
@RestController
@RequestMapping(path = "/public/testcasestepactions")
public class TestcaseStepActionControllerV001 {
    
    private static final Logger LOG = LogManager.getLogger(TestcaseStepActionControllerV001.class);
    
    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final TestcaseStepActionMapperV001 actionMapper;
    private final ITestCaseStepActionService actionService;
    private final PublicApiAuthenticationService apiAuthenticationService;
    
    @ApiOperation("Find a testcaseStepAction by key (testFolderId, testcaseId, stepId, actionId)")
    @ApiResponse(code = 200, message = "operation successful", response = TestcaseStepActionDTOV001.class)
    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}/{actionId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public TestcaseStepActionDTOV001 findControlByKey(
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
    
    @PostMapping(headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public void create(
            @RequestBody TestcaseStepActionDTOV001 actionDTO,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        TestCaseStepAction action = this.actionMapper.toEntity(actionDTO);
        LOG.debug(action.toString());
        
    }
    
}
