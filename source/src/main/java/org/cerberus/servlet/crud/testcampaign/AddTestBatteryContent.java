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
package org.cerberus.servlet.crud.testcampaign;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.TestBatteryContent;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestBatteryContent;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ITestBatteryService;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 */
@WebServlet(name = "AddTestBatteryContent", urlPatterns = {"/AddTestBatteryContent"})
public class AddTestBatteryContent extends HttpServlet {

    private ITestBatteryService testBatteryService;
    private IFactoryTestBatteryContent factoryTestBatteryContent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        testBatteryService = appContext.getBean(ITestBatteryService.class);
        factoryTestBatteryContent = appContext.getBean(IFactoryTestBatteryContent.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String jsonResponse = "-1";
        String testbattery = policy.sanitize(request.getParameter("TestBattery"));
        String testBatteryName;
        
        
        String[] testcasesselected = request.getParameterValues("testcaseselected");
        
        if(testcasesselected == null) {
            response.setStatus(404);
            jsonResponse = "Please select at least one testcase !";
        } else {
            try {
                testBatteryName = testBatteryService.findTestBatteryByKey(Integer.parseInt(testbattery)).getTestbattery();
            } catch (CerberusException ex) {
                MyLogger.log(AddTestBatteryContent.class.getName(), Level.DEBUG, ex.getMessage());
                testBatteryName = null;
            }

            if (testBatteryName != null) {
                String test;
                String testcase;


                // response.setContentType("text/html");
                for (String testcaseselect : testcasesselected) {
                    test = policy.sanitize(URLDecoder.decode(testcaseselect.split("Test=")[1].split("&TestCase=")[0], "UTF-8"));
                    testcase = policy.sanitize(URLDecoder.decode(testcaseselect.split("&TestCase=")[1], "UTF-8"));
                    try {

                        testBatteryService.createTestBatteryContent(factoryTestBatteryContent.create(null, test, testcase, testBatteryName));
                        List<TestBatteryContent> batteryContent = testBatteryService.findTestBatteryContentsByCriteria(null, testBatteryName, test, testcase);

                        if (batteryContent != null && batteryContent.size() == 1) {
                            String newTestBatteryContentId = String.valueOf(
                                    testBatteryService.findTestBatteryContentsByCriteria(null, testBatteryName, test, testcase)
                                    .get(0).getTestbatterycontentID()
                            );
                            jsonResponse = newTestBatteryContentId;
                        }
                    } catch (CerberusException ex) {
                        MyLogger.log(AddTestBatteryContent.class.getName(), Level.DEBUG, ex.getMessage());
                        jsonResponse = "-1";
                    }
                }
            }
        }
        response.getWriter().append(jsonResponse).close();
    }
}
