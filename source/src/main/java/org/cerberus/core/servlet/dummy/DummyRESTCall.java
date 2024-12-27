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
package org.cerberus.core.servlet.dummy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cerberus.core.version.Infos;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This servlet is for testing purpose. It reports a REST call result.
 *
 * @author vertigo
 */
@WebServlet(name = "DummyRESTCall", urlPatterns = {"/DummyRESTCall", "/DummyRESTCall/*"})
public class DummyRESTCall extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(DummyRESTCall.class);

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
        PrintWriter out = response.getWriter();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        try {
            if (request.getParameter("sleep") == null) {
                Thread.sleep(1000);
            } else {
                Thread.sleep(Integer.valueOf(request.getParameter("sleep")));
            }
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("messageType", "OK");
            jsonResponse.put("message", "Dummy call performed with success.");
            jsonResponse.put("ContextPath", request.getContextPath());
            jsonResponse.put("RemoteUser", request.getRemoteUser());
            jsonResponse.put("RequestURI", request.getRequestURI());
            jsonResponse.put("RequestURL", request.getRequestURL().toString());
            jsonResponse.put("RemoteUser", request.getRemoteUser());
            jsonResponse.put("AuthType", request.getAuthType());
            jsonResponse.put("Method", request.getMethod());
            jsonResponse.put("RemoteAddr", request.getRemoteAddr());
            jsonResponse.put("RemoteHost", request.getRemoteHost());
            jsonResponse.put("RemotePort", request.getRemotePort());
            jsonResponse.put("LocalAddr", request.getLocalAddr());
            jsonResponse.put("LocalName", request.getLocalName());
            jsonResponse.put("LocalPort", request.getLocalPort());
            jsonResponse.put("QueryString", request.getQueryString());
            jsonResponse.put("EmptyString", "");
            jsonResponse.put("Boolean", true);

            String remoteIP = request.getRemoteAddr();
            if (request.getHeader("x-forwarded-for") != null) {
                remoteIP = request.getHeader("x-forwarded-for");
            }
            jsonResponse.put("RemoteIP", remoteIP);

            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < 10; i++) {
                JSONObject tempJsonResponse = new JSONObject();
                tempJsonResponse.put("integer", i);
                tempJsonResponse.put("val1", "AAA" + i);
                tempJsonResponse.put("val2", "BBB");
                tempJsonResponse.put("val3", true);
                tempJsonResponse.put("val4", false);
                jsonArray.put(tempJsonResponse);
            }
            jsonResponse.put("myArray", jsonArray);

            // Extract headers.
            JSONObject jsonHeaders = new JSONObject();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headers = request.getHeaders(headerName);
                while (headers.hasMoreElements()) {
                    String headerValue = headers.nextElement();
                    jsonHeaders.put(headerName, headerValue);
                }
            }
            jsonResponse.put("Header", jsonHeaders);

            // Extract Parameters.
            JSONObject jsonParameters = new JSONObject();
            Enumeration<String> parametersNames = request.getParameterNames();
            while (parametersNames.hasMoreElements()) {
                String parameterName = parametersNames.nextElement();
                String parameterValue = request.getParameter(parameterName);
                jsonParameters.put(parameterName, parameterValue);
            }
            jsonResponse.put("Parameters", jsonParameters);

            response.getWriter().print(jsonResponse.toString());

        } catch (Exception e) {
            LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
            out.print("Error while Getting number of executions : ");
            out.println(e.getMessage());
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
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>DELETE</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
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
