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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.dto.ITestCaseManualExecutionDTO;
import org.cerberus.dto.TestCaseManualExecution;
import org.cerberus.entity.TCase;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IManualTestCaseService;
import org.cerberus.service.ITestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 25/11/2013
 * @since 0.9.1
 */
@Service
public class ManualTestCaseService implements IManualTestCaseService {

    @Autowired
    private ITestCaseManualExecutionDTO testCaseManualExecutionDTO;
    @Autowired
    private ITestCaseService testCaseService;

    @Override
    public List<TestCaseManualExecution> findTestCaseManualExecution(TCase testCase, String text, String system, String country, String env, String campaign) {
        List<TestCaseManualExecution> result = new ArrayList();
        List<TestCaseManualExecution> tcmeList = testCaseManualExecutionDTO.findTestCaseManualExecution(testCase, text, system, country, env, campaign);
        try {
            for (TestCaseManualExecution tcme : tcmeList) {
                TCase tcComplete = testCaseService.findTestCaseByKeyWithDependency(tcme.getTest(), tcme.getTestCase());
                tcme.settCase(tcComplete);
                result.add(tcme);
            }
        } catch (CerberusException ex) {
            Logger.getLogger(ManualTestCaseService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
