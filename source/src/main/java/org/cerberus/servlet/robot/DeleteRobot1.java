/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.servlet.robot;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.Robot;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.IRobotService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
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
@WebServlet(name = "DeleteRobot1", urlPatterns = {"/DeleteRobot1"})
public class DeleteRobot1 extends HttpServlet {

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
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        /**
         * Parsing and securing all required parameters.
         */
        Integer robotid = 0;
        boolean robotid_error = true;
        try {
            if (request.getParameter("robotid") != null && !request.getParameter("robotid").equals("")) {
                robotid = Integer.valueOf(policy.sanitize(request.getParameter("robotid")));
                robotid_error = false;
            }
        } catch (Exception ex) {
            robotid_error = true;
        }

        /**
         * Checking all constrains before calling the services.
         */
        if (robotid_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Delete")
                    .replace("%REASON%", "Robot ID (robotid) is missing."));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IRobotService robotService = appContext.getBean(IRobotService.class);

            AnswerItem resp = robotService.readByKeyTech(robotid);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()))) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                        .replace("%OPERATION%", "Delete")
                        .replace("%REASON%", "Robot does not exist."));
                ans.setResultMessage(msg);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can delete it.
                 */
                Robot robotData = (Robot) resp.getItem();
                ans = robotService.delete(robotData);

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Delete was successful. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createPrivateCalls("/DeleteRobot", "DELETE", "Delete Robot : ['" + robotid + "'|'" + robotData.getRobot() + "']", request);
                }
            }
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
            Logger.getLogger(DeleteRobot1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(DeleteRobot1.class
                    .getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(DeleteRobot1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(DeleteRobot1.class
                    .getName()).log(Level.SEVERE, null, ex);
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
