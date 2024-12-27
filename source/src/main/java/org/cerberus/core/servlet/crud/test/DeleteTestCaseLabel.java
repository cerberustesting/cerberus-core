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
package org.cerberus.core.servlet.crud.test;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.factory.IFactoryTestCaseLabel;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.servlet.ServletUtil;
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
@WebServlet(name = "DeleteTestCaseLabel", urlPatterns = {"/DeleteTestCaseLabel"})
public class DeleteTestCaseLabel extends HttpServlet {

    private final String OBJECT_NAME = "Test Case Label";
    private static final Logger LOG = LogManager.getLogger("DeleteTestCaseLabel");

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
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        // Parameter that we cannot secure as we need the html --> We DECODE them
        Integer myIdInt = 0;

        String[] myLabelIdList = request.getParameterValues("labelid");
        String[] myTestList = request.getParameterValues("test");
        String[] myTestCaseList = request.getParameterValues("testcase");

        if ((myTestList.length == 0) || (myTestCaseList.length == 0) || (myLabelIdList.length == 0)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Missing Parameter (either test, testcase or labelid)."));
            ans.setResultMessage(msg);
        } else if (myTestList.length != myTestCaseList.length) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Number of Test does not match number of testcase."));
            ans.setResultMessage(msg);
        }

        StringBuilder output_message = new StringBuilder();
        int massErrorCounter = 0;
        for (int i = 0; i < myLabelIdList.length; i++) {

            String myLabelId = myLabelIdList[i];
            myIdInt = 0;
            boolean label_error = true;
            try {
                if (myLabelId != null && !myLabelId.isEmpty()) {
                    myIdInt = Integer.valueOf(policy.sanitize(myLabelId));
                    label_error = false;
                }
            } catch (Exception ex) {
                label_error = true;
            }

            /**
             * Checking all constrains before calling the services.
             */
            if (label_error) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Could not manage to convert labelid to an integer value or labelid is missing."));
                ans.setResultMessage(msg);
                massErrorCounter++;
                output_message.append("<br>id : ").append(myLabelId).append(" - ").append(msg.getDescription());
            } else {
                /**
                 * All data seems cleans so we can call the services.
                 */
                ILabelService labelService = appContext.getBean(ILabelService.class);
                IFactoryTestCaseLabel factoryTestCaseLabel = appContext.getBean(IFactoryTestCaseLabel.class);
                ITestCaseLabelService testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);

                AnswerItem resp = labelService.readByKey(myIdInt);
                if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                    /**
                     * Object could not be found. We stop here and report the
                     * error.
                     */
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                            .replace("%OPERATION%", "Delete")
                            .replace("%REASON%", "Label does not exist."));
                    ans.setResultMessage(msg);
                    massErrorCounter++;
                    output_message.append("<br>labelid : ").append(myLabelId).append(" - ").append(msg.getDescription());

                } else {
                    for (int j = 0; j < myTestList.length; j++) {
                        /**
                         * The service was able to perform the query and confirm
                         * the object exist, then we can create it.
                         */
                        resp = testCaseLabelService.readByKey(myTestList[j], myTestCaseList[j], myIdInt);

                        if ((resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                            TestCaseLabel tcLabel = (TestCaseLabel) resp.getItem();
                            ans = testCaseLabelService.delete(tcLabel);

                            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                                /**
                                 * Update was successful. Adding Log entry.
                                 */
                                logEventService.createForPrivateCalls("/DeleteTestCaseLabel", "DELETE", LogEvent.STATUS_INFO, "Deleted TestCaseLabel : ['" + myIdInt + "'|'" + myTestList[j] + "'|'" + myTestCaseList[j] + "']", request);
                            } else {
                                massErrorCounter++;
                                output_message.append("<br>Label : ").append(myLabelId).append(" Test : '").append(myTestList[j]).append("' TestCase : '").append(myTestCaseList[j]).append("' - ").append(ans.getResultMessage().getDescription());
                            }
                        }

                    }
                }
            }
        }

        if (myTestList.length > 1) {
            if (massErrorCounter == myTestList.length) { // All updates are in ERROR.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Mass Update")
                        .replace("%REASON%", massErrorCounter + " label links(s) out of " + (myTestList.length * myLabelIdList.length) + " failed to be deleted due to an issue.<br>") + output_message.toString());
                ans.setResultMessage(msg);
            } else if (massErrorCounter > 0) { // At least 1 update in error
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Mass Update")
                        .replace("%REASON%", massErrorCounter + " label links(s) out of " + (myTestList.length * myLabelIdList.length) + " failed to be deleted due to an issue.<br>") + output_message.toString());
                ans.setResultMessage(msg);
            } else { // No error detected.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Mass Update") + "\n\nAll " + (myTestList.length * myLabelIdList.length) + " label links(s) deleted successfuly.");
                ans.setResultMessage(msg);
            }
            logEventService.createForPrivateCalls("/DeleteTestCaseLabel", "MASSUPDATE", LogEvent.STATUS_INFO, msg.getDescription(), request);
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
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
        try {
            processRequest(request, response);

        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
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
        try {
            processRequest(request, response);

        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
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
