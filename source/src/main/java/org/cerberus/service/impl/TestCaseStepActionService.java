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
package org.cerberus.service.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cerberus.dao.ITestCaseStepActionDAO;
import org.cerberus.entity.TestCaseStepAction;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ITestCaseStepActionService;
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
    public List<TestCaseStepAction> getListOfAction(String test, String testcase, int step) {
        return testCaseStepActionDAO.findActionByTestTestCaseStep(test, testcase, step);
    }

    @Override
    public void insertTestCaseStepAction(TestCaseStepAction testCaseStepAction) throws CerberusException  {
        testCaseStepActionDAO.insertTestCaseStepAction(testCaseStepAction);
        }

    @Override
    public boolean insertListTestCaseStepAction(List<TestCaseStepAction> testCaseStepActionList) {
        for (TestCaseStepAction tcsa : testCaseStepActionList){
            try {
                insertTestCaseStepAction(tcsa);
            } catch (CerberusException ex) {
                Logger.getLogger(TestCaseStepActionService.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }
}
