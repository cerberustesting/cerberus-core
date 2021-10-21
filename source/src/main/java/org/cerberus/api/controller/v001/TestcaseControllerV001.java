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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.api.dto.v001.TestcaseDTOV001;
import org.cerberus.exception.CerberusException;
import org.cerberus.api.mapper.v001.TestcaseMapperV001;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mlombard
 */
@AllArgsConstructor
@RestController
@RequestMapping(path = "/public/testcases")
public class TestcaseControllerV001 {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "apikey";
    private final ITestCaseService testCaseService;
    private final TestcaseMapperV001 testcaseMapper;

    @ApiOperation("Get all testcases filtered by test")
    @ApiResponse(code = 200, message = "ok", response = TestcaseDTOV001.class, responseContainer = "List")
    @GetMapping(path = "/{testFolderId}", headers = {API_VERSION_1, API_KEY}, produces = "application/json")
    public List<TestcaseDTOV001> findTestcasesByTest(@PathVariable("testFolderId") String testFolderId) throws CerberusException {
        return this.testCaseService.findTestCaseByTest(testFolderId)
                .stream()
                .map(this.testcaseMapper::toDTO)
                .collect(Collectors.toList());
    }


    @ApiOperation("Get a testcase filtered by testFolderId and testCaseFolderId")
    @ApiResponse(code = 200, message = "ok", response = TestcaseDTOV001.class)
    @GetMapping(path = "/{testFolderId}/{testcaseId}", headers = {API_VERSION_1, API_KEY}, produces = "application/json")
    public TestcaseDTOV001 findTestcasesByTestAndTestcase(@PathVariable("testFolderId") String testFolderId, @PathVariable("testcaseId") String testcaseId) throws CerberusException {
        return this.testcaseMapper.toDTO(this.testCaseService.findTestCaseByKey(testFolderId, testcaseId));
    }

    @ApiOperation("Create a new Testcase")
    @ApiResponse(code = 200, message = "ok")
    @PostMapping(headers = {API_VERSION_1, API_KEY}, produces = "application/json")
    public void createTestcase(@RequestBody TestcaseDTOV001 newTestcase) {
        this.testCaseService.create(this.testcaseMapper.toEntity(newTestcase));
    }

}
