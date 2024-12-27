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
import org.cerberus.core.crud.entity.BuildRevisionInvariant;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.IBuildRevisionInvariantService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
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
@WebServlet(name = "UpdateBuildRevisionInvariant", urlPatterns = {"/UpdateBuildRevisionInvariant"})
public class UpdateBuildRevisionInvariant extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateBuildRevisionInvariant.class);
    
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
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
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
        Integer seq = -1;
        boolean seq_error = false;
        try {
            if (request.getParameter("seq") != null && !request.getParameter("seq").isEmpty()) {
                seq = Integer.valueOf(policy.sanitize(request.getParameter("seq")));
            }
        } catch (Exception ex) {
            seq_error = true;
        }
        Integer level = -1;
        boolean level_error = false;
        try {
            if (request.getParameter("level") != null && !request.getParameter("level").isEmpty()) {
                level = Integer.valueOf(policy.sanitize(request.getParameter("level")));
            }
        } catch (Exception ex) {
            level_error = true;
        }
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String system = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("system"), "", charset);
        String versionName = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("versionname"), "", charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isEmptyOrNull(system)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "BuildRevisionInvariant")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "System name is missing!"));
            ans.setResultMessage(msg);
        } else if (level_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "BuildRevisionInvariant")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Could not manage to convert level to an integer value!"));
            ans.setResultMessage(msg);
        } else if (seq_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "BuildRevisionInvariant")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Could not manage to convert sequence to an integer value!"));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);

            AnswerItem resp = buildRevisionInvariantService.readByKey(system, level, seq);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem()!=null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "BuildRevisionInvariant")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "BuildRevisionInvariant does not exist."));
                ans.setResultMessage(msg);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                BuildRevisionInvariant buildRevisionInvariantData = (BuildRevisionInvariant) resp.getItem();
                buildRevisionInvariantData.setSystem(system);
                buildRevisionInvariantData.setLevel(level);
                buildRevisionInvariantData.setSeq(seq);
                buildRevisionInvariantData.setVersionName(versionName);
                ans = buildRevisionInvariantService.update(buildRevisionInvariantData.getSystem(), buildRevisionInvariantData.getLevel(), buildRevisionInvariantData.getSeq(), buildRevisionInvariantData);

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Update was successful. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateBuildRevisionInvariant", "UPDATE", LogEvent.STATUS_INFO, "Updated BuildRevisionInvariant : ['" + system + "'|'" + level + "'|'" + seq + "']", request);
                }
            }
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
