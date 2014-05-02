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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestBattery;
import org.cerberus.service.ITestBatteryService;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 */
@WebServlet(name = "AddTestBattery", urlPatterns = {"/AddTestBattery"})
public class AddTestBattery extends HttpServlet {

    private ITestBatteryService testBatteryService;
    private IFactoryTestBattery factoryTestBattery;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            testBatteryService = appContext.getBean(ITestBatteryService.class);
            factoryTestBattery = appContext.getBean(IFactoryTestBattery.class);
            PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

            String testbattery = policy.sanitize(request.getParameter("TestBattery"));
            String description = policy.sanitize(request.getParameter("Description"));

            response.setContentType("text/html");
            testBatteryService.createTestBattery(factoryTestBattery.create(null, testbattery, description));

            String newTestBatteryId = String.valueOf(testBatteryService.findTestBatteryByTestBatteryName(testbattery).getTestbatteryID());
            response.getWriter().append(newTestBatteryId).close();
        } catch (CerberusException ex) {
            Logger.getLogger(AddTestBattery.class.getName()).log(Level.SEVERE, null, ex);
            response.getWriter().append("-1").close();
        }

    }
}
