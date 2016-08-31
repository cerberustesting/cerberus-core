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
package org.cerberus.servlet.crud.transversaltables;

import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bcivel
 */
public class DeleteInvariant2 extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);

            String idName = request.getParameter("idName");
            String invVal = request.getParameter("value");
            Invariant invariantData = invariantService.findInvariantByIdValue(idName, invVal);
            invariantService.deleteInvariant(invariantData);

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            logEventService.createPrivateCalls("/DeleteInvariant", "DELETE", "Delete Invariant : ['" + idName + "'|'" + invVal + "']", request);
            /**
             * Formating and returning the json result.
             */
            JSONObject jsonResponse = new JSONObject();
            Answer ans = new Answer();
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Invariant").replace("%OPERATION%", "DELETE"));
            ans.setResultMessage(msg);

            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse);
            response.getWriter().flush();
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
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(DeleteInvariant2.class.getName()).log(Level.SEVERE, null, ex);
        }catch (JSONException ex) {
            Logger.getLogger(DeleteInvariant2.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(DeleteInvariant2.class.getName()).log(Level.SEVERE, null, ex);
        }catch (JSONException ex) {
            Logger.getLogger(DeleteInvariant2.class.getName()).log(Level.SEVERE, null, ex);
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
