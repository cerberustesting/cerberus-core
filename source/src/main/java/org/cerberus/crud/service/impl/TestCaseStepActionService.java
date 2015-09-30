/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cerberus.crud.dao.ITestCaseStepActionDAO;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepActionService implements ITestCaseStepActionService {

    @Autowired
    private ITestCaseStepActionDAO testCaseStepActionDAO;

    @Override
    public TestCaseStepAction findTestCaseStepActionbyKey(String test, String testCase, int step, int sequence) {
        return testCaseStepActionDAO.readByKey(test, testCase, step, sequence);
    }

    @Override
    public List<TestCaseStepAction> getListOfAction(String test, String testcase, int step) {
        return testCaseStepActionDAO.findActionByTestTestCaseStep(test, testcase, step);
    }

    @Override
    public void insertTestCaseStepAction(TestCaseStepAction testCaseStepAction) throws CerberusException {
        testCaseStepActionDAO.create(testCaseStepAction);
    }

    @Override
    public boolean insertListTestCaseStepAction(List<TestCaseStepAction> testCaseStepActionList) {
        for (TestCaseStepAction tcsa : testCaseStepActionList) {
            try {
                insertTestCaseStepAction(tcsa);
            } catch (CerberusException ex) {
                Logger.getLogger(TestCaseStepActionService.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean changeTestCaseStepActionSequence(String test, String testCase, int step, int oldSequence, int newSequence) {
        return testCaseStepActionDAO.changeTestCaseStepActionSequence(test, testCase, step, oldSequence, newSequence);
    }

    @Override
    public boolean updateTestCaseStepAction(TestCaseStepAction tcsa) {
        try {
            testCaseStepActionDAO.update(tcsa);
        } catch (CerberusException ex) {
            Logger.getLogger(TestCaseStepActionService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public List<TestCaseStepAction> findTestCaseStepActionbyTestTestCase(String test, String testCase) throws CerberusException {
        return testCaseStepActionDAO.findTestCaseStepActionbyTestTestCase(test, testCase);

    }

    @Override
    public void deleteListTestCaseStepAction(List<TestCaseStepAction> tcsaToDelete) throws CerberusException {
        for (TestCaseStepAction tcsa : tcsaToDelete) {
            deleteTestCaseStepAction(tcsa);
        }
    }

    @Override
    public void deleteTestCaseStepAction(TestCaseStepAction tcsa) throws CerberusException {
        testCaseStepActionDAO.delete(tcsa);
    }

    @Override
    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStepAction> newList, List<TestCaseStepAction> oldList, boolean duplicate) throws CerberusException {
        /**
         * Iterate on (TestCaseStepAction From Page - TestCaseStepAction From
         * Database) If TestCaseStepAction in Database has same key : Update and
         * remove from the list. If TestCaseStepAction in database does ot exist
         * : Insert it.
         */
        List<TestCaseStepAction> tcsaToUpdateOrInsert = new ArrayList(newList);
        tcsaToUpdateOrInsert.removeAll(oldList);
        List<TestCaseStepAction> tcsaToUpdateOrInsertToIterate = new ArrayList(tcsaToUpdateOrInsert);

        for (TestCaseStepAction tcsaDifference : tcsaToUpdateOrInsertToIterate) {
            for (TestCaseStepAction tcsaInDatabase : oldList) {
                if (tcsaDifference.hasSameKey(tcsaInDatabase)) {
                    //System.out.print("Upd" + tcsaDifference.toString());
                    this.updateTestCaseStepAction(tcsaDifference);
                    tcsaToUpdateOrInsert.remove(tcsaDifference);
                }
            }
        }
        this.insertListTestCaseStepAction(tcsaToUpdateOrInsert);

        /**
         * Iterate on (TestCaseStepAction From Database - TestCaseStepAction
         * From Page). If TestCaseStepAction in Page has same key : remove from
         * the list. Then delete the list of TestCaseStepAction
         */
        if (!duplicate) {
            List<TestCaseStepAction> tcsaToDelete = new ArrayList(oldList);
            tcsaToDelete.removeAll(newList);
            List<TestCaseStepAction> tcsaToDeleteToIterate = new ArrayList(tcsaToDelete);

            for (TestCaseStepAction tcsaDifference : tcsaToDeleteToIterate) {
                //System.out.print("ToDlt" + tcsaDifference.toString());
                for (TestCaseStepAction tcsaInPage : newList) {
                    if (tcsaDifference.hasSameKey(tcsaInPage)) {
                        tcsaToDelete.remove(tcsaDifference);
                    }
                }
            }
            this.deleteListTestCaseStepAction(tcsaToDelete);
        }
    }
}
