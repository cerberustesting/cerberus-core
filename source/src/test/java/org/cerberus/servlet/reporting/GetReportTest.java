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
package org.cerberus.servlet.reporting;

import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.factory.impl.FactoryTestCase;
import org.cerberus.crud.service.impl.TestCaseExecutionService;
import org.cerberus.crud.service.impl.TestCaseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Class that test the servlet GetReport
 *
 * @author Tiago Bernardes
 * @version 1.0, 24/06/2014
 * @see org.cerberus.servlet.reporting.GetReport
 * @since 0.9.2
 */
@RunWith(PowerMockRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class GetReportTest {

    @Mock
    private TestCaseService testCaseService;
    @Mock
    private TestCaseExecutionService testCaseExecutionService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private FactoryTestCase factoryTCase;
    @Autowired
    private GetReport servlet;

//    @Before
//    public void setUp() throws ServletException {
////        servlet = new GetReport();
//    }
    @Test
    @Ignore
    public void testGetTestCaseFromRequest() throws ServletException, IOException {
        String test = "TEST";
        String creator = null;
        String implementer = null;
        String project = null;
        String application = null;
        String priority = null;
        String group = null;
        String status = null;
        String active = null;
        String targetBuild = null;
        String targetRev = null;
        String comment = null;

        TestCase tCase = factoryTCase.create(test, null, null, null, creator, implementer, null, project, null, null, application, null, null, null, -1, group,
                status, null, null, null, active, null, null, null, null, null, null, null, null, targetBuild, targetRev, comment, null, null, null, null, null);
        List<TestCase> list = new ArrayList<TestCase>();

        when(request.getParameter("Test[]")).thenReturn(test);
        when(request.getParameter("Project[]")).thenReturn(test);
        when(request.getParameter("Application[]")).thenReturn(test);
        when(request.getParameter("TcActive[]")).thenReturn(test);
        when(request.getParameter("Priority[]")).thenReturn(test);
        when(request.getParameter("Status[]")).thenReturn(test);
        when(request.getParameter("Group[]")).thenReturn(test);
        when(request.getParameter("TargetBuild[]")).thenReturn(test);
        when(request.getParameter("TargetRev[]")).thenReturn(test);
        when(request.getParameter("Creator[]")).thenReturn(test);
        when(request.getParameter("Implementer[]")).thenReturn(test);
        when(request.getParameter("Comment[]")).thenReturn(test);
        when(request.getParameter("Environment[]")).thenReturn(test);
        when(request.getParameter("Build[]")).thenReturn(test);
        when(request.getParameter("Revision[]")).thenReturn(test);
        when(request.getParameter("Ip[]")).thenReturn(test);
        when(request.getParameter("Port[]")).thenReturn(test);
        when(request.getParameter("Tag[]")).thenReturn(test);
        when(request.getParameter("BrowserFullVersion[]")).thenReturn(test);
        when(request.getParameter("Country[]")).thenReturn(test);
        when(request.getParameter("Browser[]")).thenReturn(test);

        when(testCaseService.findTestCaseByAllCriteria(Matchers.<TestCase>anyObject(), anyString(), anyString())).thenReturn(list);
//        when(testCaseService.findTestCaseByAllCriteria(tCase, "", "VC")).thenReturn(list);

        servlet.doGet(request, response);

        assertEquals("application/json", response.getContentType());
    }

}
