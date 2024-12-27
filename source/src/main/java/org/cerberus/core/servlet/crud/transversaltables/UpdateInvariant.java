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
package org.cerberus.core.servlet.crud.transversaltables;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "UpdateInvariant", urlPatterns = {"/UpdateInvariant"})
public class UpdateInvariant extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateInvariant.class);
    
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

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        String id = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("idName"), "", charset);
        String value = request.getParameter("value");
        String oriId = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("originalIdName"), "", charset);
        String oriValue = request.getParameter("originalValue");
        String description = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("description"), "", charset);
        String veryShortDescField = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("veryShortDesc"), "", charset);
        String gp1 = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("gp1"), "", charset);
        String gp2 = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("gp2"), "", charset);
        String gp3 = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("gp3"), "", charset);
        String gp4 = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("gp4"), "", charset);
        String gp5 = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("gp5"), "", charset);
        String gp6 = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("gp6"), "", charset);
        String gp7 = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("gp7"), "", charset);
        String gp8 = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("gp8"), "", charset);
        String gp9 = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("gp9"), "", charset);

        Integer sort = 10;
        boolean sort_error = false;
        try {
            if (request.getParameter("sort") != null && !request.getParameter("sort").isEmpty()) {
                sort = Integer.valueOf(policy.sanitize(request.getParameter("sort")));
            }
        } catch (Exception ex) {
            sort_error = true;
        }

        boolean userHasPermissions = request.isUserInRole("Administrator");

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isEmptyOrNull(id)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Invariant")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Invariant name is missing!"));
            finalAnswer.setResultMessage(msg);
        } else if (sort_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Invariant")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Could not manage to convert sort to an integer value!"));
            finalAnswer.setResultMessage(msg);
        } else if ((id.equals(Invariant.IDNAME_COUNTRY) || id.equals(Invariant.IDNAME_ENVIRONMENT) || id.equals(Invariant.IDNAME_SYSTEM))
                && StringUtil.isEmptyOrNull(value)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Invariant")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Value is empty !! COUNTRY, ENVIRONMENT or SYSTEM invariant can't have empty value. "));
            finalAnswer.setResultMessage(msg);
        } else if ((id.equals(Invariant.IDNAME_COUNTRY) || id.equals(Invariant.IDNAME_ENVIRONMENT) || id.equals(Invariant.IDNAME_SYSTEM))
                && value.length() > 45) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Invariant")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Value is too large !! COUNTRY, ENVIRONMENT or SYSTEM invariant can't have more than 45 characters. "));
            finalAnswer.setResultMessage(msg);
        } else if ((id.equals(Invariant.IDNAME_COUNTRY) || id.equals(Invariant.IDNAME_ENVIRONMENT) || id.equals(Invariant.IDNAME_SYSTEM))
                && (!value.matches("[a-zA-Z0-9-_]+"))) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Invariant")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Value contains special characters !! COUNTRY, ENVIRONMENT or SYSTEM invariant only allow letter, digits, - or _. "));
            finalAnswer.setResultMessage(msg);
        } else if (!userHasPermissions) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Invariant")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "You don't have the right to do that"));
            finalAnswer.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);

            AnswerItem resp = invariantService.readByKey(oriId, oriValue);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, resp);

            } else {
                Invariant invariantData = (Invariant) resp.getItem();
                if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                    /**
                     * Object could not be found. We stop here and report the
                     * error.
                     */
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, resp);

                } else {
                    if (invariantService.hasPermissionsUpdate(invariantData, request)) {
                        invariantData.setIdName(id);
                        invariantData.setValue(value);
                        invariantData.setSort(sort);
                        invariantData.setDescription(description);
                        invariantData.setVeryShortDesc(veryShortDescField);
                        invariantData.setGp1(gp1);
                        invariantData.setGp2(gp2);
                        invariantData.setGp3(gp3);
                        invariantData.setGp4(gp4);
                        invariantData.setGp5(gp5);
                        invariantData.setGp6(gp6);
                        invariantData.setGp7(gp7);
                        invariantData.setGp8(gp8);
                        invariantData.setGp9(gp9);

                        ans = invariantService.update(oriId, oriValue, invariantData);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);

                        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                            /**
                             * Object updated. Adding Log entry.
                             */
                            ILogEventService logEventService = appContext.getBean(LogEventService.class);
                            logEventService.createForPrivateCalls("/UpdateInvariant", "UPDATE", LogEvent.STATUS_INFO, "Update Invariant : ['" + id + "']", request);
                        }
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Invariant")
                                .replace("%OPERATION%", "Update")
                                .replace("%REASON%", "The Invariant is not Public!"));
                        ans.setResultMessage(msg);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    }
                }
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());

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
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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
