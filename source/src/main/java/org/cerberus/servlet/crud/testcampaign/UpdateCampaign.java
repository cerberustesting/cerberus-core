/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.servlet.crud.testcampaign;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.CampaignContent;
import org.cerberus.crud.entity.CampaignLabel;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.crud.factory.IFactoryCampaignContent;
import org.cerberus.crud.factory.IFactoryCampaignLabel;
import org.cerberus.crud.factory.IFactoryCampaignParameter;
import org.cerberus.crud.service.ICampaignContentService;
import org.cerberus.crud.service.ICampaignLabelService;
import org.cerberus.crud.service.ICampaignParameterService;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "UpdateCampaign", urlPatterns = {"/UpdateCampaign"})
public class UpdateCampaign extends HttpServlet {

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
        Answer ans = new Answer();
        Answer finalAnswer = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        PrintWriter out = response.getWriter();
        String charset = request.getCharacterEncoding();

        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        int cID = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("CampaignID"), 0, charset);
        String c = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("Campaign"), null, charset);
        String notifystart = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("NotifyStart"), null, charset);
        String notifyend = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("NotifyEnd"), null, charset);
        String desc = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("Description"), null, charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String distriblist = ParameterParserUtil.parseStringParam(request.getParameter("DistribList"), "");

        if (StringUtil.isNullOrEmpty(c)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Campaign")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Campaign name is missing!"));
            finalAnswer.setResultMessage(msg);
        } else {
            // Parameter that we cannot secure as we need the html --> We DECODE them
            String battery = ParameterParserUtil.parseStringParam(request.getParameter("Batteries"), null);
            String parameter = ParameterParserUtil.parseStringParam(request.getParameter("Parameters"), null);
            String label = ParameterParserUtil.parseStringParam(request.getParameter("Labels"), null);

            ICampaignService campaignService = appContext.getBean(ICampaignService.class);

            AnswerItem resp = campaignService.readByKey(c);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) resp);
            } else {
                Campaign camp = (Campaign) resp.getItem();
                camp.setDistribList(distriblist);
                camp.setNotifyStartTagExecution(notifystart);
                camp.setNotifyEndTagExecution(notifyend);
                camp.setDescription(desc);
                finalAnswer = campaignService.update(camp);
                if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateCampaign", "UPDATE", "Update Campaign : " + c, request);
                }

                if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && battery != null) {
                    JSONArray batteries = new JSONArray(battery);
                    ICampaignContentService campaignContentService = appContext.getBean(ICampaignContentService.class);
                    IFactoryCampaignContent factoryCampaignContent = appContext.getBean(IFactoryCampaignContent.class);
                    ArrayList<CampaignContent> arr = new ArrayList<>();
                    for (int i = 0; i < batteries.length(); i++) {
                        JSONArray bat = batteries.getJSONArray(i);
                        CampaignContent co = factoryCampaignContent.create(0, bat.getString(2), bat.getString(0));
                        arr.add(co);
                    }

                    finalAnswer = campaignContentService.compareListAndUpdateInsertDeleteElements(c, arr);
                    if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createForPrivateCalls("/UpdateCampaign", "UPDATE", "Update Campaign Content : " + camp.getCampaign(), request);
                    }
                }

                if (parameter != null) {
                    JSONArray parameters = new JSONArray(parameter);
                    ICampaignParameterService campaignParameterService = appContext.getBean(ICampaignParameterService.class);
                    IFactoryCampaignParameter factoryCampaignParameter = appContext.getBean(IFactoryCampaignParameter.class);
                    ArrayList<CampaignParameter> arr = new ArrayList<>();
                    for (int i = 0; i < parameters.length(); i++) {
                        JSONArray bat = parameters.getJSONArray(i);
                        CampaignParameter co = factoryCampaignParameter.create(0, bat.getString(0), bat.getString(2), bat.getString(3));
                        arr.add(co);
                    }

                    finalAnswer = campaignParameterService.compareListAndUpdateInsertDeleteElements(c, arr);
                    if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createForPrivateCalls("/UpdateCampaign", "UPDATE", "Update Campaign Parameter : " + camp.getCampaign(), request);
                    }
                }

                if (label != null) {
                    JSONArray labels = new JSONArray(label);
                    ICampaignLabelService campaignLabelService = appContext.getBean(ICampaignLabelService.class);
                    IFactoryCampaignLabel factoryCampaignLabel = appContext.getBean(IFactoryCampaignLabel.class);
                    ArrayList<CampaignLabel> arr = new ArrayList<>();
                    for (int i = 0; i < labels.length(); i++) {
                        JSONArray bat = labels.getJSONArray(i);
                        CampaignLabel co = factoryCampaignLabel.create(0, bat.getString(0), Integer.valueOf(bat.getString(2)), request.getRemoteUser(), null, request.getRemoteUser(), null);
                        arr.add(co);
                    }

                    finalAnswer = campaignLabelService.compareListAndUpdateInsertDeleteElements(c, arr);
                    if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createForPrivateCalls("/UpdateCampaign", "UPDATE", "Update Campaign Label : " + camp.getCampaign(), request);
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
            Logger.getLogger(UpdateCampaign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateCampaign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            String t = request.getParameter("value");
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(UpdateCampaign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateCampaign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
