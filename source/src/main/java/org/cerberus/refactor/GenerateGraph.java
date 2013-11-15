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
package org.cerberus.refactor;


import org.cerberus.entity.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bcivel
 */
@WebServlet(name = "GenerateGraph", urlPatterns = {"/GenerateGraph"})
public class GenerateGraph extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("image/png");

        ServletOutputStream os = response.getOutputStream();
        String img = request.getParameter("source");

        String test = "Performance Monitor";
        if (request.getParameter("test") != null) {
            test = request.getParameter("test");
        }
        String testcase = "5025A";
        if (request.getParameter("testcase") != null) {
            testcase = request.getParameter("testcase");
        }
        String country = "all";
        if (request.getParameter("country") != null) {
            country = request.getParameter("country");
        }
        String parameter = "";
        if (request.getParameter("parameter") != null) {
            parameter = request.getParameter("parameter");
        }

        ImageIO.write(getChart(request, test, testcase, country, parameter), "png", os);
        os.close();

    }


    private RenderedImage getChart(HttpServletRequest request, String test, String testcase, String country, String parameter) {
        TestCase tc = new TestCase();
        List<String> countries = new ArrayList<String>();
        countries.add(country);
        tc.setCountryList(countries);
        tc.setTestCase(testcase);
        tc.setTest(test);


        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITCEwwwDetService tCEwwwDetService = appContext.getBean(ITCEwwwDetService.class);

        BufferedImage bi = tCEwwwDetService.getHistoricOfParameter(tc, parameter);

        return bi;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
