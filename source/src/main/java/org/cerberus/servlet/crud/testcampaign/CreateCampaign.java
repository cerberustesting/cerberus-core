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
package org.cerberus.servlet.crud.testcampaign;

import org.cerberus.crud.entity.CampaignContent;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.factory.IFactoryCampaign;
import org.cerberus.crud.factory.IFactoryCampaignContent;
import org.cerberus.crud.factory.IFactoryCampaignParameter;
import org.cerberus.crud.service.ICampaignContentService;
import org.cerberus.crud.service.ICampaignParameterService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author cte
 */
public class CreateCampaign extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    final void processRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Answer ans = new Answer();
        Answer finalAnswer = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("text/html;charset=UTF-8");
        String charset = request.getCharacterEncoding();

        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String name = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("Campaign"), null, charset);
        String desc = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("Description"), null, charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String battery = ParameterParserUtil.parseStringParam(request.getParameter("Batteries"), null);
        String parameter = ParameterParserUtil.parseStringParam(request.getParameter("Parameters"), null);

        ICampaignService campaignService = appContext.getBean(ICampaignService.class);
        IFactoryCampaign factoryCampaign = appContext.getBean(IFactoryCampaign.class);

        Campaign camp = factoryCampaign.create(0, name, desc);
        finalAnswer = campaignService.create(camp);
        if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            logEventService.createPrivateCalls("/CreateCampaign", "CREATE", "Create Campaign : " + camp.getCampaign(), request);

            if (battery != null) {
                JSONArray batteries = new JSONArray(battery);
                ICampaignContentService campaignContentService = appContext.getBean(ICampaignContentService.class);
                IFactoryCampaignContent factoryCampaignContent = appContext.getBean(IFactoryCampaignContent.class);
                finalAnswer = campaignContentService.deleteByCampaign(name);
                int i = 0;
                while (i < batteries.length() && finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    JSONArray bat = batteries.getJSONArray(i);
                    CampaignContent co = factoryCampaignContent.create(0, bat.getString(0), bat.getString(1));
                    finalAnswer = campaignContentService.create(co);
                    i++;
                    if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Adding Log entry.
                         */
                        logEventService.createPrivateCalls("/CreateCampaign", "CREATE", "Update Campaign Content : " + co.getCampaign() + ", " + co.getTestbattery(), request);
                    }
                }
            }

            if (parameter != null) {
                JSONArray parameters = new JSONArray(parameter);
                ICampaignParameterService campaignParameterService = appContext.getBean(ICampaignParameterService.class);
                IFactoryCampaignParameter factoryCampaignParameter = appContext.getBean(IFactoryCampaignParameter.class);
                finalAnswer = campaignParameterService.deleteByCampaign(name);
                int i = 0;
                while (i < parameters.length() && finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    JSONArray bat = parameters.getJSONArray(i);
                    CampaignParameter co = factoryCampaignParameter.create(0, bat.getString(2), bat.getString(1), bat.getString(3));
                    finalAnswer = campaignParameterService.create(co);
                    i++;
                    if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Adding Log entry.
                         */
                        logEventService.createPrivateCalls("/CreateCampaign", "CREATE", "Update Campaign Parameter : " + co.getCampaign() + ", " + co.getValue(), request);
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
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            this.processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(CreateCampaign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CreateCampaign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            this.processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(CreateCampaign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CreateCampaign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
