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
package org.cerberus.core.servlet.crud.countryenvironment;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cerberus.core.crud.service.ICountryEnvironmentService;
import org.cerberus.core.crud.service.impl.CountryEnvironmentService;
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
 * @version 1.0, 21/02/2013
 * @since 2.0.0
 */
@WebServlet(name= "GetEnvironmentAvailable", value = "/GetEnvironmentAvailable")
public class GetEnvironmentAvailable extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(GetEnvironmentAvailable.class);
    
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String testName = policy.sanitize(httpServletRequest.getParameter("test"));
        String testCaseName = policy.sanitize(httpServletRequest.getParameter("testCase"));
        String country = policy.sanitize(httpServletRequest.getParameter("country"));

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ICountryEnvironmentService countryEnvironmentService = appContext.getBean(CountryEnvironmentService.class);

        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            for (String[] strings : countryEnvironmentService.getEnvironmentAvailable(testName, testCaseName, country)) {
                JSONObject env = new JSONObject();
                env.put("environment", strings[0]);
                env.put("description", strings[0].concat(" With Build: ").concat(strings[1]).concat(" And Revision: ").concat(strings[2]));
                array.put(env);
            }

            jsonObject.put("envList", array);

            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(jsonObject.toString());
        } catch (JSONException exception) {
            LOG.warn(exception.toString());
        }
    }
}
