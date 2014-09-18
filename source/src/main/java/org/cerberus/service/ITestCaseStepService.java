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

import org.cerberus.entity.TestCaseStep;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepService {

    /**
     * GetListOfSteps
     * @param test
     * @param testcase
     * @return List of TestCaseStep
     */
    List<TestCaseStep> getListOfSteps(String test, String testcase);

    List<String> getLoginStepFromTestCase(String countryCode, String application);

    /**
     * InsertTestCaseStep
     * @param testCaseStep
     * @throws CerberusException 
     */
    void insertTestCaseStep(TestCaseStep testCaseStep) throws CerberusException;

    /**
     * InsertListTestCaseStep
     * @param testCaseStepList
     * @return true if no exception reached
     */
    boolean insertListTestCaseStep(List<TestCaseStep> testCaseStepList);
    
    /**
     * FindTestCaseStep
     * @param test
     * @param testcase
     * @param step
     * @return TestCaseStep object
     */
    TestCaseStep findTestCaseStep(String test, String testcase, Integer step);

    public void updateTestCaseStep(TestCaseStep tcsLeft) throws CerberusException ;

    public void deleteTestCaseStep(TestCaseStep tcs) throws CerberusException ;
    
    public void deleteListTestCaseStep(List<TestCaseStep> tcsToDelete) throws CerberusException ;
    
    List <TestCaseStep> getTestCaseStepUsingStepInParamter(String test, String testCase, int step) throws CerberusException;
    
    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStep> newList, List<TestCaseStep> oldList) throws CerberusException;

}
