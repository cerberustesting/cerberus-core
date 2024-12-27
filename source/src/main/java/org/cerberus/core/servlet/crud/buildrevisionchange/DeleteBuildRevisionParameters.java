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
package org.cerberus.core.servlet.crud.buildrevisionchange;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.BuildRevisionParameters;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.service.IBuildRevisionParametersService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.LogEventService;
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
@WebServlet(name = "DeleteBuildRevisionParameters", urlPatterns = {"/DeleteBuildRevisionParameters"})
public class DeleteBuildRevisionParameters extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(DeleteBuildRevisionParameters.class);
    private final String OBJECT_NAME = "BuildRevisionParameters";

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
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        Integer brpid = 0;

        String[] myId = request.getParameterValues("id");
        StringBuilder output_message = new StringBuilder();
        MessageEvent final_msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        int massErrorCounter = 0;
        for (String myId1 : myId) {

            /**
             * Parsing and securing all required parameters.
             */
            brpid = 0;
            boolean brpid_error = true;
            try {
                if (myId1 != null && !myId1.isEmpty()) {
                    brpid = Integer.valueOf(policy.sanitize(myId1));
                    brpid_error = false;
                }
            } catch (Exception ex) {
                brpid_error = true;
            }

            /**
             * Checking all constrains before calling the services.
             */
            if (brpid_error) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Delete")
                        .replace("%REASON%", "BuildRevisionParameters ID (id) : Could not manage to convert id to an integer value or id is missing."));
                ans.setResultMessage(msg);
                massErrorCounter++;
                output_message.append("<br>id : ").append(myId1).append(" - ").append(msg.getDescription());
            } else {
                /**
                 * All data seems cleans so we can call the services.
                 */
                IBuildRevisionParametersService brpService = appContext.getBean(IBuildRevisionParametersService.class);

                AnswerItem resp = brpService.readByKeyTech(brpid);
                if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                    /**
                     * Object could not be found. We stop here and report the
                     * error.
                     */
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                            .replace("%OPERATION%", "Delete")
                            .replace("%REASON%", "BuildRevisionParameter does not exist."));
                    ans.setResultMessage(msg);
                    massErrorCounter++;
                    output_message.append("<br>id : ").append(myId1).append(" - ").append(msg.getDescription());

                } else {
                    /**
                     * The service was able to perform the query and confirm the
                     * object exist, then we can delete it.
                     */
                    BuildRevisionParameters brpData = (BuildRevisionParameters) resp.getItem();
                    ans = brpService.delete(brpData);

                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Delete was successful. Adding Log entry.
                         */
                        logEventService.createForPrivateCalls("/DeleteBuildRevisionParameters", "DELETE", LogEvent.STATUS_INFO, "Delete BuildRevisionParameters : ['" + brpid + "'|'" + brpData.getRelease() + "']", request);
                    } else {
                        massErrorCounter++;
                        output_message.append("<br>id : ").append(myId1).append(" - ").append(ans.getResultMessage().getDescription());
                    }
                }
            }

        }

        if (myId.length > 1) {
            if (massErrorCounter == myId.length) { // All updates are in ERROR.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Mass Update")
                        .replace("%REASON%", massErrorCounter + " objects(s) out of " + myId.length + " failed to update due to an issue.<br>") + output_message.toString());
                ans.setResultMessage(msg);
            } else if (massErrorCounter > 0) { // At least 1 update in error
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Mass Update")
                        .replace("%REASON%", massErrorCounter + " objects(s) out of " + myId.length + " failed to update due to an issue.<br>") + output_message.toString());
                ans.setResultMessage(msg);
            } else { // No error detected.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Mass Update") + "\n\nAll " + myId.length + " object(s) updated successfuly.");
                ans.setResultMessage(msg);
            }
            logEventService.createForPrivateCalls("/DeleteBuildRevisionParameters", "MASSUPDATE", LogEvent.STATUS_INFO, msg.getDescription(), request);
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse.toString());
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
