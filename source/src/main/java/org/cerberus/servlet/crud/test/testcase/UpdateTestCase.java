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
package org.cerberus.servlet.crud.test.testcase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "UpdateTestCase", urlPatterns = {"/UpdateTestCase"})
@Controller
public class UpdateTestCase extends AbstractCreateUpdateTestCase {

    private static final Logger LOG = LogManager.getLogger(UpdateTestCase.class);

    @Autowired
    private ITestCaseService testCaseService;

    @Autowired
    private ILogEventService logEventService;


    @Override
    protected String getTypeOperation() {
        return "Update";
    }

    @Override
    protected TestCase getTestCaseBeforeTraitment (String keyTest, String keyTestCase) {
        AnswerItem resp = testCaseService.readByKey(keyTest, keyTestCase);

        return (TestCase) resp.getItem();
    }

    @Override
    protected void fireLogEvent (String keyTest, String keyTestCase, TestCase tc, HttpServletRequest request, HttpServletResponse response) {
        logEventService.createForPrivateCalls("/UpdateTestCase", "UPDATE", "Update TestCase Header : ['" + keyTest + "'|'" + keyTestCase + "'] " + "version : "+tc.getVersion(), request);
    }

    @Override
    protected void updateTestCase(String originalTest, String originalTestCase, TestCase tc) throws CerberusException {
        testCaseService.convert(testCaseService.update(originalTest, originalTestCase, tc));
    }

}
