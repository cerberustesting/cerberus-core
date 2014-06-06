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
package org.cerberus.servlet.testBattery;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.TestBattery;
import org.cerberus.entity.TestBatteryContentWithDescription;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ITestBatteryService;
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
@WebServlet(name = "GetTestBattery", urlPatterns = {"/GetTestBattery"})
public class GetTestBattery extends HttpServlet {

    private ITestBatteryService testBatteryService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        testBatteryService = appContext.getBean(ITestBatteryService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String action = policy.sanitize(request.getParameter("action"));
        String testBattery = policy.sanitize(request.getParameter("testBattery"));

        try {
            JSONObject jsonResponse = new JSONObject();
            try {
                if (action != null && "findAllTestBattery".equals(action.trim())) {
                    jsonResponse.put("TestBatteries", findAllTestBatteryToJSON());
                } else if (action != null && "findAllTestBatteryContent".equals(action.trim())) {
                    jsonResponse.put("TestBatteryContents", findAllTestBatteryContentToJSON(testBatteryService.findTestBatteryByKey(Integer.parseInt(testBattery)).getTestbattery()));
                }
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

    private JSONArray findAllTestBatteryToJSON() throws JSONException, CerberusException {
        JSONArray jsonResponse = new JSONArray();
        for (TestBattery testBattery : testBatteryService.findAll()) {
            jsonResponse.put(convertTestBatteryToJSONObject(testBattery));
        }

        return jsonResponse;
    }

    private JSONArray findAllTestBatteryContentToJSON(String testBattery) throws JSONException, CerberusException {
        JSONArray jsonResponse = new JSONArray();
        for (TestBatteryContentWithDescription testBatteryContentWithDescription : testBatteryService.findTestBatteryContentsWithDescriptionByTestBatteryName(testBattery)) {
            jsonResponse.put(convertTestBatteryContentWithDescriptionToJSONObject(testBatteryContentWithDescription));
        }

        return jsonResponse;
    }

    private JSONArray convertTestBatteryToJSONObject(TestBattery testBattery) throws JSONException {
        JSONArray result = new JSONArray();
        result.put(testBattery.getTestbatteryID());
        result.put(testBattery.getTestbattery());
        result.put(testBattery.getDescription());
        return result;
    }

    private JSONArray convertTestBatteryContentWithDescriptionToJSONObject(TestBatteryContentWithDescription testBatteryContentWithDescription) throws JSONException {
        JSONArray result = new JSONArray();
        result.put(testBatteryContentWithDescription.getTestbatterycontentID());
        result.put(testBatteryContentWithDescription.getTestbattery());
        result.put(testBatteryContentWithDescription.getTest());

        result.put("<a target=\"TestCase\" href=\"TestCase.jsp?Test=" + testBatteryContentWithDescription.getTest() + "&TestCase=" + testBatteryContentWithDescription.getTestCase() + "\">" + testBatteryContentWithDescription.getTestCase() + "</a>");
        result.put(testBatteryContentWithDescription.getDescription());

        return result;
    }

}
