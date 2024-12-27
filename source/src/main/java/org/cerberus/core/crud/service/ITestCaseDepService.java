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
package org.cerberus.core.crud.service;

import java.util.HashMap;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseDep;
import org.cerberus.core.exception.CerberusException;

import java.util.List;

public interface ITestCaseDepService {

    /**
     *
     * @param test
     * @param testcase
     * @param testDep
     * @param testcaseDep
     * @return
     * @throws CerberusException
     */
    TestCaseDep readByKey(String test, String testcase, String testDep, String testcaseDep) throws CerberusException;

    /**
     *
     * @param test
     * @param testcase
     * @return
     * @throws CerberusException
     */
    List<TestCaseDep> readByTestAndTestcase(String test, String testcase) throws CerberusException;

    /**
     *
     * @param testCaseList
     * @return
     * @throws CerberusException
     */
    List<TestCaseDep> readByTestAndTestcase(List<TestCase> testCaseList) throws CerberusException;

    HashMap<String, List<TestCaseDep>> convertTestCaseDependencyListToHash(List<TestCaseDep> testCaseDependencies);

    void create(TestCaseDep testCaseDep) throws CerberusException;

    void createList(List<TestCaseDep> testCaseDepList) throws CerberusException;

    void update(TestCaseDep testCaseDep) throws CerberusException;

    void updateList(List<TestCaseDep> testCaseDepList) throws CerberusException;

    void delete(TestCaseDep testCaseDep) throws CerberusException;

    void deleteList(List<TestCaseDep> testCaseDepList) throws CerberusException;

    void compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseDep> newList) throws CerberusException;
}
