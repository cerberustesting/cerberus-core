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
package org.cerberus.core.crud.service.impl;

import org.cerberus.core.crud.dao.ITestCaseDepDAO;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseDep;
import org.cerberus.core.crud.service.ITestCaseDepService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestCaseDepService implements ITestCaseDepService {

    @Autowired
    private ITestCaseDepDAO testCaseDepDao;

    @Override
    public TestCaseDep readByKey(String test, String testcase, String dependencyTest, String testcaseDependency) throws CerberusException {
        return testCaseDepDao.readByKey(test, testcase, dependencyTest, testcaseDependency);
    }

    @Override
    public List<TestCaseDep> readByTestAndTestcase(String test, String testcase) throws CerberusException {
        return testCaseDepDao.readByTestAndTestCase(test, testcase);
    }

    @Override
    public List<TestCaseDep> readByTestAndTestcase(List<TestCase> testcases) throws CerberusException {
        return testCaseDepDao.readByTestAndTestCase(testcases);
    }

    /**
     *
     * @param testCaseDependencies
     * @return HashMap<String, List<TestCaseDep>> with testCase as Key
     */
    @Override
    public HashMap<String, List<TestCaseDep>> convertTestCaseDependencyListToHash(List<TestCaseDep> testCaseDependencies) {
        HashMap<String, List<TestCaseDep>> testCaseDependenciesHash = new HashMap<>();
        for (TestCaseDep testCaseDependency : testCaseDependencies) {
            String key = testCaseDependency.getTest() + "##" + testCaseDependency.getTestcase();
            if (testCaseDependenciesHash.containsKey(key)) {
                testCaseDependenciesHash.get(key).add(testCaseDependency);
            } else {
                testCaseDependenciesHash.put(key, new ArrayList<>());
                testCaseDependenciesHash.get(key).add(testCaseDependency);
            }
        }
        return testCaseDependenciesHash;
    }

    @Override
    public void create(TestCaseDep testcaseDependency) throws CerberusException {
        testCaseDepDao.create(testcaseDependency);
    }

    @Override
    public void update(TestCaseDep testcaseDependency) throws CerberusException {
        testCaseDepDao.update(testcaseDependency);
    }

    @Override
    public void delete(TestCaseDep testcaseDependency) throws CerberusException {
        testCaseDepDao.delete(testcaseDependency);
    }

    @Override
    public void createList(List<TestCaseDep> testcaseDependencies) throws CerberusException {
        for (TestCaseDep testcaseDependency : testcaseDependencies) {
            this.create(testcaseDependency);
        }
    }

    @Override
    public void updateList(List<TestCaseDep> testcaseDependencies) throws CerberusException {
        for (TestCaseDep TestcaseDependency : testcaseDependencies) {
            this.update(TestcaseDependency);
        }
    }

    @Override
    public void deleteList(List<TestCaseDep> testcaseDependencies) throws CerberusException {
        for (TestCaseDep testcaseDependency : testcaseDependencies) {
            this.delete(testcaseDependency);
        }

    }

    @Override
    public void compareListAndUpdateInsertDeleteElements(String test, String testcase, List<TestCaseDep> newTestcaseDependencies) throws CerberusException {
        
        List<TestCaseDep> oldTestcaseDependencies = testCaseDepDao.readByTestAndTestCase(test, testcase);

        // toUpdate = all in newTestcaseDependencies and in oldTestcaseDependencies
        List<TestCaseDep> toUpdate = this.getObjectWithSameKey(newTestcaseDependencies, oldTestcaseDependencies);
        this.updateList(toUpdate);

        // toInsert = all in newTestcaseDependencies not in oldTestcaseDependencies
        List<TestCaseDep> toInsert = new ArrayList<>(newTestcaseDependencies);
        toInsert.removeIf(testcaseDependencyA -> oldTestcaseDependencies.stream().anyMatch(testcaseDependencyB -> testcaseDependencyB.hasSameKey(testcaseDependencyA))); // remove if it is the same key
        this.createList(toInsert);

        // toDelete = all in oldTestcaseDependencies and in newTestcaseDependencies
        List<TestCaseDep> toDelete = new ArrayList<>(oldTestcaseDependencies);
        toDelete.removeIf(testcaseDependencyA -> newTestcaseDependencies.stream().anyMatch(testcaseDependencyB -> testcaseDependencyB.hasSameKey(testcaseDependencyA))); // remove if it is the same key
        this.deleteList(toDelete);

    }

    private List<TestCaseDep> getObjectWithSameKey(List<TestCaseDep> testcaseDependenciesA, List<TestCaseDep> testcaseDependenciesB) {
        return testcaseDependenciesA.stream()
                .filter((testcaseDependencyA) -> testcaseDependenciesB.stream().anyMatch((testcaseDependencyB) -> testcaseDependencyB.hasSameKey(testcaseDependencyA)))
                .collect(Collectors.toList());
    }
}
