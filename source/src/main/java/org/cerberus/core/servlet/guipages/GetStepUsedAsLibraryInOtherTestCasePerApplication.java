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
package org.cerberus.core.servlet.guipages;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "GetStepUsedAsLibraryInOtherTestCasePerApplication", urlPatterns = {"/GetStepUsedAsLibraryInOtherTestCasePerApplication"})
public class GetStepUsedAsLibraryInOtherTestCasePerApplication extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(GetStepUsedAsLibraryInOtherTestCasePerApplication.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String echo = policy.sanitize(request.getParameter("sEcho"));
        String system = policy.sanitize(request.getParameter("System"));

        JSONObject jsonResponse = new JSONObject();

        try {

            JSONArray data = new JSONArray();

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestCaseStepService stepService = appContext.getBean(ITestCaseStepService.class);
            for (TestCaseStep tcs : stepService.getStepLibraryBySystem(system)) {
                JSONArray row = new JSONArray();
                StringBuilder testLink = new StringBuilder();
                testLink.append("<a href=\"TestCaseList.jsp?test=");
                testLink.append(tcs.getTest());
                testLink.append("\">");
                testLink.append(tcs.getTest());
                testLink.append("</a>");
                row.put(testLink.toString());
                StringBuilder testcaseLink = new StringBuilder();
                testcaseLink.append("<a href=\"TestCaseScript.jsp?test=");
                testcaseLink.append(tcs.getTest());
                testcaseLink.append("&testcase=");
                testcaseLink.append(tcs.getTestcase());
                testcaseLink.append("\">");
                testcaseLink.append(tcs.getTestcase());
                testcaseLink.append("</a>");
                row.put(testcaseLink.toString());
                row.put(tcs.getStepId());
                row.put(tcs.getDescription());
                data.put(row);
            }

            //data that will be shown in the table
            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", data.length());
            jsonResponse.put("iTotalDisplayRecords", data.length());

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            LOG.warn(ex.toString());
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }

    }
}
