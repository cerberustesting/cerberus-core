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
package org.cerberus.servlet.crud.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.TestCase;

import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.impl.LogEventService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * @author Tiago Bernardes <tbernardes@redoute.pt>
 * @version 1.0
 * @since 2012-08-17
 */
@WebServlet(name = "UpdateTestCase", urlPatterns = {"/UpdateTestCase"})
public class UpdateTestCase extends HttpServlet {

    private IFactoryTestCaseCountry factoryTestCaseCountry;

    /**
     * Process the post request from UpdateTestCase form in TestCase page.
     * <p/>
     * Use {@link #updateTestCase(TCase tc, int type)} to update the TestCase
     * information.
     *
     * @param request information from the request page
     * @param response information from the response page
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService tcs = appContext.getBean(ITestCaseService.class);

        TestCase tc = getInfo(request, appContext);

        if (tcs.updateTestCaseInformation(tc) && tcs.updateTestCaseInformationCountries(tc)) {

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            logEventService.createForPrivateCalls("/UpdateTestCase", "UPDATE", "Update testcase : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "']", request);

            response.sendRedirect(response.encodeRedirectURL("TestCase.jsp?Tinf=Y&Load=Load&Test=" + tc.getTest() + "&TestCase=" + tc.getTestCase()));
        } else {
            response.getWriter().print("Unable to record testcase");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        ITestCaseStepActionService testCaseStepActionService = appContext.getBean(ITestCaseStepActionService.class);

        testCaseStepActionService.changeTestCaseStepActionSequence(request.getParameter("test"), request.getParameter("testcase"),
                Integer.valueOf(request.getParameter("step")), Integer.valueOf(request.getParameter("fromPosition")),
                Integer.valueOf(request.getParameter("toPosition")));
        /*
         id	2
         step8	
         test	Customer Identification
         testcase	5023A
         toPosition	161
         */
    }

    /**
     * Create new TCase object from the information of request form
     *
     * @param request information from the request page
     * @return TCase object
     * @see org.cerberus.crud.entity.TestCase
     */
    private TestCase getInfo(HttpServletRequest request, ApplicationContext appContext) {
        factoryTestCaseCountry = appContext.getBean(IFactoryTestCaseCountry.class);

        TestCase tc = new TestCase();
        String test = request.getParameter("Test");
        String testCase = request.getParameter("TestCase");
        tc.setTest(test);
        tc.setTestCase(testCase);
        tc.setImplementer(request.getParameter("editImplementer"));
        tc.setUsrModif(request.getUserPrincipal().getName());
        tc.setProject(request.getParameter("editProject"));
        tc.setTicket(request.getParameter("editTicket"));
        tc.setApplication(request.getParameter("editApplication"));
        tc.setActiveQA(request.getParameter("editRunQA"));
        tc.setActiveUAT(request.getParameter("editRunUAT"));
        tc.setActivePROD(request.getParameter("editRunPROD"));
        tc.setPriority(Integer.parseInt(request.getParameter("editPriority")));
        tc.setGroup(request.getParameter("editGroup"));
        tc.setStatus(request.getParameter("editStatus"));
        List<TestCaseCountry> countries = new ArrayList<TestCaseCountry>();
        if (request.getParameterValues("testcase_country_general") != null) {
            for (String toto : request.getParameterValues("testcase_country_general")) {
                countries.add(factoryTestCaseCountry.create(test, testCase, toto));
            }
        }
        tc.setTestCaseCountry(countries);
        tc.setDescription(HtmlUtils.htmlEscape(request.getParameter("editDescription")));
        tc.setBehaviorOrValueExpected(request.getParameter("valueDetail"));
        tc.setHowTo(request.getParameter("howtoDetail"));
        tc.setTcActive(request.getParameter("editTcActive"));
        tc.setFromBuild(request.getParameter("editFromBuild"));
        tc.setFromRev(request.getParameter("editFromRev"));
        tc.setToBuild(request.getParameter("editToBuild"));
        tc.setToRev(request.getParameter("editToRev"));
        tc.setBugID(request.getParameter("editBugID"));
        tc.setTargetBuild(request.getParameter("editTargetBuild"));
        tc.setTargetRev(request.getParameter("editTargetRev"));
        tc.setComment(HtmlUtils.htmlEscape(request.getParameter("editComment")));
        tc.setFunction(request.getParameter("function"));
        return tc;
    }
}
