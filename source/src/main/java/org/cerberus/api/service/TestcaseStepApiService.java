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
package org.cerberus.api.service;

import java.util.List;
import org.cerberus.api.dto.v001.InvariantDTOV001;
import org.cerberus.api.dto.v001.TestcaseStepDTOV001;
import org.cerberus.api.errorhandler.exception.EntityNotFoundException;
import org.cerberus.crud.dao.ITestCaseStepDAO;
import org.cerberus.crud.entity.TestCaseStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author mlombard
 */
@Service
public class TestcaseStepApiService {

    private final ITestCaseStepDAO testCaseStepDAO;

    @Autowired
    public TestcaseStepApiService(ITestCaseStepDAO testCaseStepDAO) {
        this.testCaseStepDAO = testCaseStepDAO;
    }

    public List<TestCaseStep> findAllTestcaseSteps() {
        List<TestCaseStep> testcaseSteps = this.testCaseStepDAO.findAllTestcaseSteps();
        if (testcaseSteps == null || testcaseSteps.isEmpty()) {
            throw new EntityNotFoundException(TestcaseStepDTOV001.class);
        }
        return testcaseSteps;
    }

    public List<TestCaseStep> findAllLibrarySteps() {
        List<TestCaseStep> testcaseSteps = this.testCaseStepDAO.findAllLibrarySteps();
        if (testcaseSteps == null || testcaseSteps.isEmpty()) {
            throw new EntityNotFoundException(TestcaseStepDTOV001.class);
        }
        return testcaseSteps;
    }

    public List<TestCaseStep> findTestcaseStepsByTestFolderId(String testFolderId) {
        List<TestCaseStep> testcaseSteps = this.testCaseStepDAO.findTestcaseStepsByTestFolderId(testFolderId);
        if (testcaseSteps == null || testcaseSteps.isEmpty()) {
            throw new EntityNotFoundException(TestcaseStepDTOV001.class, "testFolderId", testFolderId);
        }
        return testcaseSteps;
    }

}
