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
package org.cerberus.core.servlet.reporting;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.core.crud.service.ITestCaseStepActionExecutionService;
import org.json.JSONArray;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "TestCaseActionExecutionDetail", urlPatterns = {"/TestCaseActionExecutionDetail"})
public class TestCaseActionExecutionDetail extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseStepActionExecutionService testCaseExecutionDetailService = appContext.getBean(ITestCaseStepActionExecutionService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String test = policy.sanitize(httpServletRequest.getParameter("test"));
        String testcase = policy.sanitize(httpServletRequest.getParameter("testcase"));
        String country = policy.sanitize(httpServletRequest.getParameter("country"));


        JSONArray data = testCaseExecutionDetailService.lastActionExecutionDuration(test, testcase, country);

        try {


            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(data.toString());
        } catch (Exception e) {
            httpServletResponse.setContentType("text/html");
            httpServletResponse.getWriter().print(e.getMessage());
        }
    }
}