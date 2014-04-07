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
package org.cerberus.servlet.testCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.entity.TestCase;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.service.ITestCaseStepActionService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Tiago Bernardes <tbernardes@redoute.pt>
 * @version 1.0
 * @since 2012-08-17
 */
@WebServlet(name = "UpdateTestCase", urlPatterns = {"/UpdateTestCase"})
public class UpdateTestCase extends HttpServlet {

    /**
     * Process the post request from UpdateTestCase form in TestCase
     * page.
     * <p/>
     * Use {@link #updateTestCase(TestCase tc, int type)} to update the TestCase
     * information.
     *
     * @param request  information from the request page
     * @param response information from the response page
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TestCase tc = getInfo(request);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService tcs = appContext.getBean(ITestCaseService.class);
        
        if (tcs.updateTestCaseInformation(tc) &&  tcs.updateTestCaseInformationCountries(tc)) {

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateTestCase", "UPDATE", "Update testcase : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "']", "", ""));
            } catch (CerberusException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
            }

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
     * Create new TestCase object from the information of request form
     *
     * @param request information from the request page
     * @return TestCase object
     * @see org.cerberus.entity.TestCase
     */
    private TestCase getInfo(HttpServletRequest request) {
        TestCase tc = new TestCase();
        tc.setTest(request.getParameter("Test"));
        tc.setTestCase(request.getParameter("TestCase"));
        tc.setImplementer(request.getParameter("editImplementer"));
        tc.setLastModifier(request.getUserPrincipal().getName());
        tc.setProject(request.getParameter("editProject"));
        tc.setTicket(request.getParameter("editTicket"));
        tc.setApplication(request.getParameter("editApplication"));
        tc.setRunQA(request.getParameter("editRunQA"));
        tc.setRunUAT(request.getParameter("editRunUAT"));
        tc.setRunPROD(request.getParameter("editRunPROD"));
        tc.setPriority(Integer.parseInt(request.getParameter("editPriority")));
        tc.setGroup(request.getParameter("editGroup"));
        tc.setStatus(request.getParameter("editStatus"));
        List<String> countries = new ArrayList<String>();
        if (request.getParameterValues("testcase_country_general") != null) {
            Collections.addAll(countries, request.getParameterValues("testcase_country_general"));
        }
        tc.setCountryList(countries);
        tc.setShortDescription(request.getParameter("editDescription"));
        tc.setDescription(request.getParameter("valueDetail"));
        tc.setHowTo(request.getParameter("howtoDetail"));
        tc.setActive(request.getParameter("editTcActive"));
        tc.setFromSprint(request.getParameter("editFromBuild"));
        tc.setFromRevision(request.getParameter("editFromRev"));
        tc.setToSprint(request.getParameter("editToBuild"));
        tc.setToRevision(request.getParameter("editToRev"));
        tc.setBugID(request.getParameter("editBugID"));
        tc.setTargetSprint(request.getParameter("editTargetBuild"));
        tc.setTargetRevision(request.getParameter("editTargetRev"));
        tc.setComment(request.getParameter("editComment"));
        return tc;
    }
}
