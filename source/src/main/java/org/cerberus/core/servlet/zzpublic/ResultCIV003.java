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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.service.ciresult.ICIService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * @author bcivel
 */
@WebServlet(name = "ResultCIV003", urlPatterns = {"/ResultCIV003"})
public class ResultCIV003 extends HttpServlet {

    private static Logger LOG = LogManager.getLogger(ResultCIV003.class);
    private IAPIKeyService apiKeyService;
    private ITestCaseExecutionService testCaseExecutionService;

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        apiKeyService = appContext.getBean(IAPIKeyService.class);

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(ILogEventService.class);
        logEventService.createForPublicCalls("/ResultCIV003", "CALL", LogEvent.STATUS_INFO, "ResultCIV003 called : " + request.getRequestURL(), request);

        if (apiKeyService.authenticate(request, response)) {

            try {
                JSONObject jsonResponse = new JSONObject();

                String tag = policy.sanitize(request.getParameter("tag"));
                String campaign = policy.sanitize(request.getParameter("campaign"));
                String outputFormat = policy.sanitize(request.getParameter("outputformat"));

                String helpMessage = "This servlet is used to provide various execution counters as well as a global OK or KO status based on the number and status of the execution done on a specific tag. \n"
                        + "The number of executions are ponderated by parameters by priority from cerberus_ci_okcoefprio1 to cerberus_ci_okcoefprio4. \n"
                        + "Formula used is the following : \n"
                        + "Nb Exe Prio 1 testcases * cerberus_ci_okcoefprio1 + Nb Exe Prio 2 testcases * cerberus_ci_okcoefprio2 + "
                        + "Nb Exe Prio 3 testcases * cerberus_ci_okcoefprio3 + Nb Exe Prio 4 testcases * cerberus_ci_okcoefprio4.\n"
                        + "If no executions are found, the result is KO.\n"
                        + "With at least 1 execution, if result is < 1 then global servlet result is OK. If not, it is KO.\n"
                        + "All execution needs to have a status equal to KO, FA, NA, PE or NE.\n"
                        + "If at least 1 PE or 1 NE if found, global status will be PE\n"
                        + "Output format is json by default, or SVG if outputFormat=svg is defined\n"
                        + "Parameter list :\n"
                        + "- tag : Execution Tag to filter the test cases execution. [" + tag + "]\n"
                        + "- campaign : If you feed the campaign, the lastest Tag from that campaign will be selected. [" + campaign + "]\n"
                        + "- outputformat : ['text','json', 'svg']. Output format of the result. [" + outputFormat + "]\n";

                jsonResponse.put("helpMessage", helpMessage);

                boolean error = false;
                String error_message = "";

                // Checking the parameter validity. Tag is a mandatory parameter
                if (StringUtil.isEmptyOrNull(tag)) {

                    if (!StringUtil.isEmptyOrNull(campaign)) {
                        ITagService tagService = appContext.getBean(ITagService.class);

                        List<Tag> myList;
                        AnswerList<Tag> myAnswerList = tagService.readByVariousByCriteria(campaign, 0, 1, "id", "desc", null, null);
                        if (myAnswerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                            for (Tag tagCur : myAnswerList.getDataList()) {
                                tag = tagCur.getTag();
                            }
                        }
                    }

                }
                if (StringUtil.isEmptyOrNull(tag)) {
                    error_message += "Error - Either specify a tag or specify a campaign to get the latest tag from that campaign.";
                    error = true;

                }

                if (!error) {

                    ICIService ciService = appContext.getBean(ICIService.class);
                    testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
                    List<TestCaseExecution> executions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(tag);
                    jsonResponse = ciService.getCIResult(tag, null, executions);

                    // Log the result with calculation detail.
                    logEventService.createForPublicCalls("/ResultCIV003", "CALLRESULT", LogEvent.STATUS_INFO, "ResultCIV003 calculated for tag " + tag + " result [" + jsonResponse.getString("result") + "]", request);

                } else {

                    jsonResponse.put("messageType", "KO");
                    jsonResponse.put("message", error_message);
                }

                generateResponse(response, outputFormat, jsonResponse, error);

            } catch (JSONException e) {
                LOG.warn(e);
                //returns a default error message with the json format that is able to be parsed by the client-side
                response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
            } catch (CerberusException | ParseException exception) {
                LOG.error(exception, exception);
            }
        }

    }

    private void generateResponse(HttpServletResponse response, String outputFormat, JSONObject jsonResponse, boolean error) throws IOException, JSONException {

        StringBuilder returnMessage = new StringBuilder();

        switch (outputFormat) {
            case "svg":
                response.setContentType("image/svg+xml");
                response.setHeader("Cache-Control", "no-cache");
                response.setDateHeader("Expires", 0);
                try {
                    String tagToDisplay = "";
                    String resultToDisplay = "";
                    if (error) {
                        tagToDisplay = StringUtils.substring(jsonResponse.getString("message"), 0, 32);
                        resultToDisplay = "ERR";
                    } else {
                        tagToDisplay = StringUtils.substring(jsonResponse.getString("tag"), 0, 32);
                        resultToDisplay = jsonResponse.getString("result");
                    }
                    String responseSvg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"350\" height=\"20\">"
                            + "<linearGradient id=\"b\" x2=\"0\" y2=\"100%\">"
                            + "<stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"></stop>"
                            + "<stop offset=\"1\" stop-opacity=\".1\"></stop>"
                            + "</linearGradient>"
                            //RECTANGLE
                            + "<rect rx=\"3\" fill=\"#555\" width=\"250\" height=\"20\"></rect>"
                            + "<rect rx=\"3\" x=\"210\" fill=\"" + getColor(resultToDisplay) + "\" width=\"40\" height=\"20\"></rect>"
                            //TEXT
                            + "<g fill=\"#fff\" text-anchor=\"start\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"9\">"
                            + "<text x=\"10\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">" + tagToDisplay + "</text>"
                            + "<text x=\"10\" y=\"14\">" + tagToDisplay + "</text>"
                            + "<text x=\"225\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">" + resultToDisplay + "</text>"
                            + "<text x=\"225\" y=\"14\">" + resultToDisplay + "</text>"
                            + "</g>"
                            + "</svg>";

                    response.getWriter().print(responseSvg);
                } catch (JSONException ex) {
                    LOG.warn(ex);
                }
                break;
            case "text":
                response.setContentType("text");
                response.setCharacterEncoding("utf8");
                try {
                    if (error) {
                        returnMessage.append(jsonResponse.get("message"));
                        returnMessage.append("\n\n");
                        returnMessage.append(jsonResponse.get("helpMessage"));
                    } else {
                        returnMessage.append(jsonResponse.get("result"));
                    }
                    response.getWriter().print(returnMessage.toString());
                } catch (JSONException ex) {
                    LOG.warn(ex);
                }
                break;
            default:
                response.setContentType("application/json");
                response.setCharacterEncoding("utf8");
                response.getWriter().print(jsonResponse.toString(1));
        }

    }

    private String getColor(String controlStatus) {
        String color = null;

        if ("OK".equals(controlStatus)) {
            color = "#5CB85C";
        } else if ("KO".equals(controlStatus)) {
            color = "#D9534F";
        } else {
            color = "#3498DB";
        }
        return color;
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
