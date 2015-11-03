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
package org.cerberus.servlet.crud.countryenvironment;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ICountryEnvironmentDatabaseService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class FindCountryEnvironmentDatabase extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(FindCountryEnvironmentDatabase.class);
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
        final PrintWriter out = response.getWriter();
        final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        try {
            
            String system = policy.sanitize(request.getParameter("System"));
            String country = policy.sanitize(request.getParameter("Country"));
            String environment = policy.sanitize(request.getParameter("Environment"));
            
            
            JSONArray data = new JSONArray(); //data that will be shown in the table

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ICountryEnvironmentDatabaseService cedService = appContext.getBean(ICountryEnvironmentDatabaseService.class);

            List<CountryEnvironmentDatabase> cedList = cedService.findListByCriteria(system, country, environment);

            JSONObject jsonResponse = new JSONObject();

            for (CountryEnvironmentDatabase ced : cedList) {
                JSONArray row = new JSONArray();
                row.put("System="+ced.getSystem()+"&Country="+ced.getCountry()+"&Env="+ced.getEnvironment()+"&Database="+ced.getDatabase()+"&")
                        .put(ced.getCountry())
                        .put(ced.getEnvironment())
                        .put(ced.getDatabase())
                        .put(ced.getConnectionPoolName());

                data.put(row);
            }

            
            jsonResponse.put("aaData", data);
            
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            LOG.warn(ex.toString());
        } catch (CerberusException ex) {
            java.util.logging.Logger.getLogger(FindCountryEnvironmentDatabase.class.getName()).log(Level.SEVERE, null, ex);
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
