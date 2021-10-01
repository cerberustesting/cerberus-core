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
package org.cerberus.controller.publicv1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import java.util.List;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.dto.publicv1.TestcaseDTOV1;
import org.cerberus.dto.publicv1.TestcaseStepDTOV1;
import org.cerberus.mapper.TestcaseStepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mlombard
 */
@RestController
@RequestMapping(path = "/public/testcasesteps")
public class TestcaseStepController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "API_KEY";
    private final ITestCaseStepService testCaseStepService;

    @Autowired
    public TestcaseStepController(ITestCaseStepService testCaseStepService) {
        this.testCaseStepService = testCaseStepService;
    }

    @ApiOperation("Get all TestcaseSteps")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV1.class, responseContainer = "List")
    @GetMapping(headers = {API_VERSION_1, API_KEY}, produces = "application/json")
    public List<TestcaseStepDTOV1> findAllTestcaseSteps(@RequestParam("libraryStep") Boolean isLibraryStep) {
        return null;
    }

    @ApiOperation("Get all testcase steps from a test folder")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV1.class, responseContainer = "List")
    @GetMapping(path = "/{testFolderId}", headers = {API_VERSION_1, API_KEY}, produces = "application/json")
    public List<TestcaseStepDTOV1> findTestcaseStepsByTestFolderId(@PathVariable("testFolderId") String testFolderId) {
        return null;
    }

    @ApiOperation("Get a TestcaseStep filtered by testFolderId and testcaseId")
    @ApiResponse(code = 200, message = "ok", response = TestcaseStepDTOV1.class)
    @GetMapping(path = "/{testFolderId}/{testcaseId}/{stepId}", headers = {API_VERSION_1, API_KEY}, produces = "application/json")
    public TestcaseStepDTOV1 findTestcaseStepByTestFolderIdAndTestcaseIdAndStepId(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("stepId") String stepId,
            @RequestParam("libraryStep") Boolean isLibraryStep) {

        return TestcaseStepMapper.convertToDto(
                this.testCaseStepService.readTestcaseStepWithDependencies(
                        testFolderId,
                        testcaseId,
                        Integer.parseInt(stepId)
                )
        );
    }

}
