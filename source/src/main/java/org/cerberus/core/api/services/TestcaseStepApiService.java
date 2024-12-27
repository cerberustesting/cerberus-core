/**
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
package org.cerberus.core.api.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepDTOV001;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.crud.dao.ITestCaseStepDAO;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.stereotype.Service;

/**
 * @author mlombard
 */
@AllArgsConstructor
@Service
public class TestcaseStepApiService {

    private static final Logger LOG = LogManager.getLogger(TestcaseStepApiService.class);
    private final ITestCaseStepDAO testCaseStepDAO;
    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private final IInvariantService invariantService;

    public List<TestCaseStep> findAll() {
        List<TestCaseStep> testcaseSteps = this.testCaseStepDAO.findAllTestcaseSteps();
        if (testcaseSteps == null || testcaseSteps.isEmpty()) {
            throw new EntityNotFoundException(TestcaseStepDTOV001.class);
        }
        return testcaseSteps;
    }

    public List<TestCaseStep> findAllWithSteps() {
        List<TestCaseStep> testcaseSteps = this.testCaseStepDAO.findAllLibrarySteps();
        if (testcaseSteps == null || testcaseSteps.isEmpty()) {
            throw new EntityNotFoundException(TestcaseStepDTOV001.class);
        }
        return testcaseSteps;
    }

    public List<TestCaseStep> findByTestFolderId(String testFolderId) {
        List<TestCaseStep> testcaseSteps = this.testCaseStepDAO.findTestcaseStepsByTestFolderId(testFolderId);
        if (testcaseSteps == null || testcaseSteps.isEmpty()) {
            throw new EntityNotFoundException(TestcaseStepDTOV001.class, "testFolderId", testFolderId);
        }
        return testcaseSteps;
    }

    public List<TestCaseStep> findAllWithProperties(boolean isLibraryStep) {
        List<TestCaseStep> steps = isLibraryStep ? this.findAllWithSteps() : this.findAll();

        try {
            Map<String, Invariant> countryInvariants = invariantService.readByIdNameToHash("COUNTRY");
            List<TestCase> testcases = getTestcasesFromSteps(steps);
            Map<Pair<String, String>, List<TestCaseCountryProperties>> testCaseCountryProperties = getCountriesByTestAndTestCase(countryInvariants, testcases);

            steps
                    .forEach(testCaseStep -> testCaseStep.setProperties(
                            testCaseCountryProperties.get(
                                    Pair.of(
                                            testCaseStep.getTest(),
                                            testCaseStep.getTestcase()
                                    )
                            )
                    ));
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }

        return steps;
    }

    private Map<Pair<String, String>, List<TestCaseCountryProperties>> getCountriesByTestAndTestCase(Map<String, Invariant> countryInvariants, List<TestCase> testcases) throws CerberusException {
        return this.testCaseCountryPropertiesService
                .findDistinctPropertiesOfTestCaseFromTestcaseList(
                        testcases,
                        (HashMap<String, Invariant>) countryInvariants
                ).stream()
                .collect(
                        Collectors.groupingBy(prop -> Pair.of(prop.getTest(), prop.getTestcase()))
                );
    }

    private List<TestCase> getTestcasesFromSteps(List<TestCaseStep> steps) {
        return steps
                .stream()
                .map(step -> TestCase.builder()
                        .test(step.getTest())
                        .testcase(step.getTestcase())
                        .build()
                )
                .distinct()
                .collect(Collectors.toList());
    }

}
