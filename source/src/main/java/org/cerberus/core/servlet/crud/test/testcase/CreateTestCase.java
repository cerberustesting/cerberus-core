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
package org.cerberus.core.servlet.crud.test.testcase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.TestCaseService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.core.crud.entity.LogEvent;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "CreateTestCase", urlPatterns = {"/CreateTestCase"})
@Controller
public class CreateTestCase extends AbstractCreateUpdateTestCase {

    private static final Logger LOG = LogManager.getLogger(CreateTestCase.class);

    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private TestCaseService testCaseService;

    @Override
    protected String getTypeOperation() {
        return "Create";
    }

    @Override
    protected TestCase getTestCaseBeforeTraitment(String keyTest, String keyTestCase) {
        return new TestCase();
    }

    @Override
    protected void updateTestCase(String originalTest, String originalTestCase, TestCase tc) throws CerberusException {
        testCaseService.convert(testCaseService.create(tc));
    }

    @Override
    protected void fireLogEvent(String keyTest, String keyTestCase, TestCase tc, HttpServletRequest request, HttpServletResponse response) {
        logEventService.createForPrivateCalls("/CreateTestCase", "CREATE", LogEvent.STATUS_INFO, "Create TestCase : ['" + keyTest + "'|'" + keyTestCase + "'] " + "version : " + tc.getVersion(), request);
    }

}
