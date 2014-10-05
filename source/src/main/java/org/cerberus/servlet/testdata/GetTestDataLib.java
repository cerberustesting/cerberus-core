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
package org.cerberus.servlet.testdata;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.TestDataLib;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ITestDataLibService;
import org.cerberus.servlet.invariant.GetInvariantList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 */
@WebServlet(name = "GetTestDataLib", urlPatterns = {"/GetTestDataLib"})
public class GetTestDataLib extends HttpServlet {

    private ITestDataLibService testDataLibService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.process(request, response);
    }

    protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        testDataLibService = appContext.getBean(ITestDataLibService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);


        try {
            JSONObject jsonResponse = new JSONObject();
            try {
                jsonResponse.put("TestDataLib", findAllTestDataLibToJSON());
            } catch (CerberusException ex) {
                response.setContentType("text/html");
                response.getWriter().print(ex.getMessageError().getDescription());

            }
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            MyLogger.log(GetInvariantList.class.getName(), Level.FATAL, "" + e);
            response.setContentType("text/html");
            response.getWriter().print(e.getMessage());
        }
    }

    private JSONArray findAllTestDataLibToJSON() throws JSONException, CerberusException {
        JSONArray jsonResponse = new JSONArray();
        for (TestDataLib testDataLib : testDataLibService.findTestDataLibListByCriteria(0, 100, "TestDataLibID", "ASC", "", "")) {
            jsonResponse.put(convertTestDataLibToJSONObject(testDataLib));
        }

        return jsonResponse;
    }

    private JSONArray convertTestDataLibToJSONObject(TestDataLib testDataLib) throws JSONException {
        JSONArray result = new JSONArray();
        result.put(testDataLib.getTestDataLibID());
        result.put(testDataLib.getName());
        result.put(testDataLib.getSystem());
        result.put(testDataLib.getEnvironment());
        result.put(testDataLib.getCountry());
        result.put(testDataLib.getGroup());
        result.put(testDataLib.getType());
        result.put(testDataLib.getDatabase());
        result.put(testDataLib.getScript());
        result.put(testDataLib.getServicePath());
        result.put(testDataLib.getMethod());
        result.put(testDataLib.getEnvelope());
        result.put(testDataLib.getDescription());
        return result;
    }


}
