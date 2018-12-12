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
package org.cerberus.crud.service;

import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseDep;
import org.cerberus.exception.CerberusException;

import java.util.List;

public interface ITestCaseDepService {

    TestCaseDep readByKey(String test, String testcase, String testDep, String testcaseDep) throws CerberusException;

    List<TestCaseDep> readByTestAndTestCase(String test, String testcase) throws CerberusException;
    List<TestCaseDep> readByTestAndTestCase(List<TestCase> testCaseList) throws CerberusException;

    void create(TestCaseDep testCaseDep) throws CerberusException;

    void createList(List<TestCaseDep> testCaseDepList) throws CerberusException;

    void update(TestCaseDep testCaseDep) throws CerberusException;

    void updateList(List<TestCaseDep> testCaseDepList) throws CerberusException;

    void delete(TestCaseDep testCaseDep) throws CerberusException;

    void deleteList(List<TestCaseDep> testCaseDepList) throws CerberusException;

    void compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseDep> newList) throws CerberusException;
}
