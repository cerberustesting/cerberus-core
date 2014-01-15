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
package org.cerberus.servlet.publi;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.entity.TCExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.impl.ApplicationService;
import org.cerberus.service.impl.InvariantService;
import org.cerberus.service.impl.TestCaseExecutionService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.version.Version;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This servlet is for monitoring purpose. It reports the number of "Performance
 * monitor" execution on WORKING testcases that were performed in the last
 * minutes (nbminuteshistory). Nagios system calls it in order to verify the
 * number of KO in the last n minutes.
 * <p/>
 * It can be filtered using the following criterias : application - Filter on
 * the executions performed on the application - country : filter on the
 * execution performed on the country - controlstatus : filter all execution
 * that return the following status.
 *
 * @author vertigo
 */
@WebServlet(name = "GetNumberOfExecutions", urlPatterns = {"/GetNumberOfExecutions"})
public class GetNumberOfExecutions extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
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
        IApplicationService myApplicationService = appContext.getBean(ApplicationService.class);
        IInvariantService myInvariantService = appContext.getBean(InvariantService.class);

        // Parsing all parameters.
        String environment = ParameterParserUtil.parseStringParam(request.getParameter("environment"), "PROD");
        String test = ParameterParserUtil.parseStringParam(request.getParameter("test"), "");
        String application = ParameterParserUtil.parseStringParam(request.getParameter("application"), "");
        String country = ParameterParserUtil.parseStringParam(request.getParameter("country"), "");
        String controlStatus = ParameterParserUtil.parseStringParam(request.getParameter("controlstatus"), "");
        int NbMinutes = ParameterParserUtil.parseIntegerParam(request.getParameter("nbminuteshistory"), 0);

        String helpMessage = "\nThis servlet return the number of execution that match the following criterias :\n"
                + "nbminuteshistory [mandatory] : the number of minutes in the past from the moment the servlet is called. This parameter must be > 0. " + NbMinutes + "\n"
                + "test : Executions done on the test. " + test + "\n"
                + "environment : Environment where the execution happened. Default to PROD. " + environment + "\n"
                + "country : Executions done on the country. " + country + "\n"
                + "application : Executions done against that application. " + application + "\n"
                + "controlstatus : execution that return the following status. " + controlStatus + "\n";

        try {

            boolean error = false;

            // Checking the parameter validity. nbminuteshistory is a mandatory parameter.
            if (NbMinutes == 0) {
                out.println("Error - Parameter nbminuteshistory is mandatory. Please feed it in order to specify the elapsed time where the history should be considered.");
                error = true;
            }
            // Checking the parameter validity. If application has been entered, does it exist ?
            if (!application.equalsIgnoreCase("") && !myApplicationService.isApplicationExist(application)) {
                out.println("Error - Application does not exist  : " + application);
                error = true;
            }

            if (!country.equalsIgnoreCase("") && !myInvariantService.isInvariantExist("COUNTRY", country)) {
                out.println("Warning - Country does not exist  : " + country);
            }

            if (!environment.equalsIgnoreCase("") && !myInvariantService.isInvariantExist("ENVIRONMENT", environment)) {
                out.println("Warning - Environment does not exist  : " + environment);
            }

            if (!controlStatus.equalsIgnoreCase("") && !myInvariantService.isInvariantExist("TCESTATUS", controlStatus)) {
                out.println("Warning - Control Status does not exist  : " + controlStatus);
            }
            
            // Starting the request only if previous parameters exist.
            if (!error) {

                // Getting a timestamp to filter the executions based on the nb of minutes
                String dateLimitFrom = DateUtil.getMySQLTimestampTodayDeltaMinutes(-NbMinutes);

                ITestCaseExecutionService MyTestExecutionService = appContext.getBean(TestCaseExecutionService.class);
                List<TCExecution> myList;

                // Getting the lists of test cases the follow the criterias.
                try {
                    myList = MyTestExecutionService.findTCExecutionbyCriteria1(dateLimitFrom, test, "", application, country, environment, controlStatus, "WORKING");
                    out.println(myList.size());
                } catch (CerberusException e) {
                    out.println("0");
                }

            } else {
                // In case of errors, we displayu the help message.
                out.println(helpMessage);

            }


        } catch (Exception e) {
            Logger.getLogger(GetNumberOfExecutions.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
            out.print("Error while Getting number of executions : ");
            out.println(e.getMessage());
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
