/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.exception.CerberusException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.util.servlet.ServletUtil;

/**
 *
 * @author memiks
 */
@WebServlet(value = "/GetPropertiesForTestCase", name = "GetPropertiesForTestCase")
public class GetPropertiesForTestCase extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(GetPropertiesForTestCase.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {

            String test = request.getParameter("test");
            String testcase = request.getParameter("testcase");
            String property = request.getParameter("property");

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestCaseCountryPropertiesService testCaseCountryPropertiesService = appContext.getBean(ITestCaseCountryPropertiesService.class);

            JSONArray propertyList = new JSONArray();
            List<TestCaseCountryProperties> properties = testCaseCountryPropertiesService.findDistinctPropertiesOfTestCase(test, testcase);

            for (TestCaseCountryProperties prop : properties) {
                if (property == null || "".equals(property.trim()) || property.equals(prop.getProperty())) {
                    JSONObject propertyFound = new JSONObject();

                    propertyFound.put("property", prop.getProperty());
                    propertyFound.put("description", prop.getDescription());
                    propertyFound.put("type", prop.getType());
                    propertyFound.put("rank", prop.getRank());
                    propertyFound.put("database", prop.getDatabase());
                    propertyFound.put("value1", prop.getValue1());
                    propertyFound.put("value2", prop.getValue2());
                    propertyFound.put("length", prop.getLength());
                    propertyFound.put("rowLimit", prop.getRowLimit());
                    propertyFound.put("nature", prop.getNature());
                    propertyFound.put("retryNb", prop.getRetryNb());
                    propertyFound.put("retryPeriod", prop.getRetryPeriod());
                    propertyFound.put("cacheExpire", prop.getCacheExpire());
                    propertyFound.put("rank", prop.getRank());

                    List<String> countriesSelected = testCaseCountryPropertiesService.findCountryByProperty(prop);
                    JSONArray countries = new JSONArray();
                    for (String country : countriesSelected) {
                        countries.put(country);
                    }
                    propertyFound.put("country", countries);
                    propertyList.put(propertyFound);
                }
            }

            response.setContentType("application/json");
            response.getWriter().print(propertyList.toString());

        } catch (JSONException ex) {
            LOG.warn(ex.toString());
        } catch (CerberusException ex) {
            LOG.warn(ex.toString());
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
