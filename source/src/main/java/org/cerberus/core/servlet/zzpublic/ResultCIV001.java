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
package org.cerberus.core.servlet.zzpublic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author vertigo17
 */
@WebServlet(name = "ResultCIV001", urlPatterns = {"/ResultCIV001"})
public class ResultCIV001 extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ResultCIV001.class);
    private IAPIKeyService apiKeyService;

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        apiKeyService = appContext.getBean(IAPIKeyService.class);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(ILogEventService.class);
        logEventService.createForPublicCalls("/ResultCIV001", "CALL", LogEvent.STATUS_INFO, "ResultCIV001 called : " + request.getRequestURL(), request);

        if (apiKeyService.authenticate(request, response)) {

            try {
                JSONObject jsonResponse = new JSONObject();

                String tag = policy.sanitize(request.getParameter("tag"));

                String helpMessage = "This servlet is used to provide a json object with various execution counters as well as a global OK or KO status based on the number and status of the execution done on a specific tag. "
                        + "The number of executions are ponderated by parameters by priority from cerberus_ci_okcoefprio1 to cerberus_ci_okcoefprio4. "
                        + "Formula used is the following : "
                        + "Nb Exe Prio 1 testcases * cerberus_ci_okcoefprio1 + Nb Exe Prio 2 testcases * cerberus_ci_okcoefprio2 + "
                        + "Nb Exe Prio 3 testcases * cerberus_ci_okcoefprio3 + Nb Exe Prio 4 testcases * cerberus_ci_okcoefprio4."
                        + "If no executions are found, the result is KO."
                        + "With at least 1 execution, if result is < 1 then global servlet result is OK. If not, it is KO."
                        + "All execution needs to have a status equal to KO, FA, NA or PE."
                        + "Parameter list :"
                        + "- tag [mandatory] : Execution Tag to filter the test cases execution. [" + tag + "]";

                boolean error = false;
                String error_message = "";

                // Checking the parameter validity. Tag is a mandatory parameter
                if (StringUtils.isBlank(tag)) {
                    error_message = "Error - Parameter tag is mandatory.";
                    error = true;
                }

                if (!error) {

                    ITestCaseExecutionService MyTestExecutionService = appContext.getBean(TestCaseExecutionService.class);
                    List<TestCaseExecution> myList;

                    int nbok = 0;
                    int nbko = 0;
                    int nbfa = 0;
                    int nbpe = 0;
                    int nbna = 0;
                    int nbca = 0;
                    int nbtotal = 0;

                    int nbkop1 = 0;
                    int nbkop2 = 0;
                    int nbkop3 = 0;
                    int nbkop4 = 0;

                    String exeStart = "";
                    long longStart = 0;
                    String exeEnd = "";
                    long longEnd = 0;

                    try {
                        myList = MyTestExecutionService.convert(MyTestExecutionService.readByTag(tag));

                        for (TestCaseExecution curExe : myList) {

                            if (longStart == 0) {
                                longStart = curExe.getStart();
                            }
                            if (curExe.getStart() < longStart) {
                                longStart = curExe.getStart();
                            }

                            if (longEnd == 0) {
                                longEnd = curExe.getEnd();
                            }
                            if (curExe.getEnd() > longEnd) {
                                longEnd = curExe.getEnd();
                            }

                            nbtotal++;

                            switch (curExe.getControlStatus()) {
                                case TestCaseExecution.CONTROLSTATUS_KO:
                                    nbko++;
                                    break;
                                case TestCaseExecution.CONTROLSTATUS_OK:
                                    nbok++;
                                    break;
                                case TestCaseExecution.CONTROLSTATUS_FA:
                                    nbfa++;
                                    break;
                                case TestCaseExecution.CONTROLSTATUS_NA:
                                    nbna++;
                                    break;
                                case TestCaseExecution.CONTROLSTATUS_CA:
                                    nbca++;
                                    break;
                                case TestCaseExecution.CONTROLSTATUS_PE:
                                    nbpe++;
                                    break;
                            }

                            if (!(curExe.getControlStatus().equals("OK"))) {
                                switch (curExe.getTestCaseObj().getPriority()) {
                                    case 1:
                                        nbkop1++;
                                        break;
                                    case 2:
                                        nbkop2++;
                                        break;
                                    case 3:
                                        nbkop3++;
                                        break;
                                    case 4:
                                        nbkop4++;
                                        break;
                                }
                            }

                        }

                    } catch (CerberusException ex) {
                        LOG.warn(ex);
                    }

                    IParameterService parameterService = appContext.getBean(IParameterService.class);

                    float pond1 = parameterService.getParameterFloatByKey("cerberus_ci_okcoefprio1", "", 0);
                    float pond2 = parameterService.getParameterFloatByKey("cerberus_ci_okcoefprio2", "", 0);
                    float pond3 = parameterService.getParameterFloatByKey("cerberus_ci_okcoefprio3", "", 0);
                    float pond4 = parameterService.getParameterFloatByKey("cerberus_ci_okcoefprio4", "", 0);
                    String result;
                    float resultCal = (nbkop1 * pond1) + (nbkop2 * pond2) + (nbkop3 * pond3) + (nbkop4 * pond4);
                    if ((resultCal < 1) && (nbtotal > 0)) {
                        result = "OK";
                    } else {
                        result = "KO";
                    }

                    jsonResponse.put("messageType", "OK");
                    jsonResponse.put("message", "CI result calculated with success.");
                    jsonResponse.put("CI_OK_prio1", pond1);
                    jsonResponse.put("CI_OK_prio2", pond2);
                    jsonResponse.put("CI_OK_prio3", pond3);
                    jsonResponse.put("CI_OK_prio4", pond4);
                    jsonResponse.put("CI_finalResult", resultCal);
                    jsonResponse.put("NonOK_prio1_nbOfExecution", nbkop1);
                    jsonResponse.put("NonOK_prio2_nbOfExecution", nbkop2);
                    jsonResponse.put("NonOK_prio3_nbOfExecution", nbkop3);
                    jsonResponse.put("NonOK_prio4_nbOfExecution", nbkop4);
                    jsonResponse.put("status_OK_nbOfExecution", nbok);
                    jsonResponse.put("status_KO_nbOfExecution", nbko);
                    jsonResponse.put("status_FA_nbOfExecution", nbfa);
                    jsonResponse.put("status_PE_nbOfExecution", nbpe);
                    jsonResponse.put("status_NA_nbOfExecution", nbna);
                    jsonResponse.put("status_CA_nbOfExecution", nbca);
                    jsonResponse.put("TOTAL_nbOfExecution", nbtotal);
                    jsonResponse.put("result", result);
                    jsonResponse.put("ExecutionStart", String.valueOf(new Timestamp(longStart)));
                    jsonResponse.put("ExecutionEnd", String.valueOf(new Timestamp(longEnd)));

                    response.getWriter().print(jsonResponse.toString());

                    // Log the result with calculation detail.
                    logEventService.createForPublicCalls("/ResultCIV001", "CALLRESULT", LogEvent.STATUS_INFO, "ResultCIV001 calculated with result [" + result + "] : " + nbkop1 + "*" + pond1 + " + " + nbkop2 + "*" + pond2 + " + " + nbkop3 + "*" + pond3 + " + " + nbkop4 + "*" + pond4 + " = " + resultCal, request);

                } else {

                    jsonResponse.put("messageType", "KO");
                    jsonResponse.put("message", error_message);
                    jsonResponse.put("helpMessage", helpMessage);

                    response.getWriter().print(jsonResponse.toString(1));

                }

            } catch (JSONException e) {
                LOG.warn(e);
                //returns a default error message with the json format that is able to be parsed by the client-side
                response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
            }
        }

    }

    // <editor-fold defaultstate="collapsed"
    // desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
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
