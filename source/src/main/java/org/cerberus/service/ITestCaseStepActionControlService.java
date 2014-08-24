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
package org.cerberus.service;

import java.util.List;

import org.cerberus.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepActionControlService {

    TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepNumber, int sequence, int control);

    List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence);

    void insertTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException;

    boolean insertListTestCaseStepActionControl(List<TestCaseStepActionControl> testCaseStepActionControlList);

    List<TestCaseStepActionControl> findControlByTestTestCaseStep(String test, String testcase, int step);

    public boolean updateTestCaseStepActionControl(TestCaseStepActionControl control);

    public List<TestCaseStepActionControl> findControlByTestTestCase(String initialTest, String initialTestCase);

    public void deleteListTestCaseStepActionControl(List<TestCaseStepActionControl> tcsacToDelete);
}
