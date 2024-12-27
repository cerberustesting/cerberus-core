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
package org.cerberus.core.servlet.crud.test;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.ITestCaseService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 08/02/2013
 * @since 2.0.0
 */
@WebServlet(name = "GetTestCaseForTest", urlPatterns =  "/GetTestCaseForTest")
public class GetTestCaseForTest extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(GetTestCaseForTest.class);
    
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String testName = policy.sanitize(httpServletRequest.getParameter("test"));
        String system = policy.sanitize(httpServletRequest.getParameter("system"));

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testService = appContext.getBean(ITestCaseService.class);

        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            List<TestCase> tcaseList;
            if (system == null){
                tcaseList = testService.findTestCaseByTest(testName);
            } else{
                tcaseList = testService.findTestCaseActiveAutomatedBySystem(testName, system);
            }
            
            for (TestCase list : tcaseList) {
                JSONObject testCase = new JSONObject();
                testCase.put("testCase", list.getTestcase());
                testCase.put("description", list.getTestcase().concat(" [").concat(list.getApplication()).concat("] : ").concat(list.getDescription()));
                testCase.put("application", list.getApplication());
                array.put(testCase);
            }
            jsonObject.put("testCaseList", array);

            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(jsonObject.toString());
        } catch (JSONException exception) {
            LOG.warn(exception.toString());
        }
    }
}
