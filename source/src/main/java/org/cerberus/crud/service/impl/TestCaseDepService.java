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
package org.cerberus.crud.service.impl;

import org.cerberus.crud.dao.ITestCaseDepDAO;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseDep;
import org.cerberus.crud.service.ITestCaseDepService;
import org.cerberus.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestCaseDepService implements ITestCaseDepService {

    @Autowired
    private ITestCaseDepDAO testCaseDepDao;

    @Override
    public TestCaseDep readByKey(String test, String testcase, String testDep, String testcaseDep) throws CerberusException {
        return testCaseDepDao.readByKey(test,testcase,testDep,testcaseDep);
    }

    @Override
    public List<TestCaseDep> readByTestAndTestCase(String test, String testcase) throws CerberusException {
        return testCaseDepDao.readByTestAndTestCase(test, testcase);
    }

    @Override
    public List<TestCaseDep> readByTestAndTestCase(List<TestCase> testCaseList) throws CerberusException {
        return testCaseDepDao.readByTestAndTestCase(testCaseList);
    }


    @Override
    public void create(TestCaseDep testCaseDep) throws CerberusException {
        testCaseDepDao.create(testCaseDep);
    }

    @Override
    public void update(TestCaseDep testCaseDep) throws CerberusException {
        testCaseDepDao.update(testCaseDep);
    }
    @Override
    public void delete(TestCaseDep testCaseDep) throws CerberusException {
        testCaseDepDao.delete(testCaseDep);
    }

    @Override
    public void createList(List<TestCaseDep> testCaseDepList) throws CerberusException {
        for(TestCaseDep tc : testCaseDepList) this.create(tc);
    }

    @Override
    public void updateList(List<TestCaseDep> testCaseDepList) throws CerberusException{
        for(TestCaseDep tc : testCaseDepList) this.update(tc);
    }

    @Override
    public void deleteList(List<TestCaseDep> testCaseDepList) throws CerberusException {
        for(TestCaseDep tc : testCaseDepList) this.delete(tc);

    }



    @Override
    public void compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseDep> newList) throws CerberusException {

            List<TestCaseDep> oldList = testCaseDepDao.readByTestAndTestCase(test, testCase);

            // toUpdate = all in newList and in oldList
            List<TestCaseDep> toUpdate = this.getObjectWithSameKey(newList, oldList);
            this.updateList(toUpdate);

            // toInsert = all in newList not in oldList
            List<TestCaseDep> toInsert = new ArrayList<>(newList);
            toInsert.removeIf( tcd1 -> oldList.stream().anyMatch( tcd2 -> tcd2.hasSameKey(tcd1) )); // remove if it is the same key
            this.createList(toInsert);

            // toDelete = all in oldList and in newList
            List<TestCaseDep> toDelete = new ArrayList<>(oldList);
            toDelete.removeIf( tcd1 -> newList.stream().anyMatch( tcd2 -> tcd2.hasSameKey(tcd1) )); // remove if it is the same key
            this.deleteList(toDelete);

    }

    private List<TestCaseDep> getObjectWithSameKey(List<TestCaseDep> lst1, List<TestCaseDep> lst2) {
        return lst1.stream()
                .filter( ( tcd1 ) -> lst2.stream().anyMatch( ( tcd2 ) -> tcd2.hasSameKey(tcd1)))
                .collect(Collectors.toList());
    }
}
