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

import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.factory.IFactoryCampaignContent;
import org.cerberus.crud.factory.IFactoryCampaignParameter;
import org.cerberus.crud.factory.IFactoryTestBatteryContent;
import org.cerberus.crud.service.*;
import org.cerberus.crud.service.impl.LogEventService;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author bcivel
 */
public class UpdateTestBattery2 extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String charset = request.getCharacterEncoding();

        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        int tID = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("testbatteryID"), 0, charset);
        String testBattery = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testBattery"), null, charset);
        String desc = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("description"), "", charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String batteryContent = ParameterParserUtil.parseStringParam(request.getParameter("batteryContent"), null);

        if (StringUtil.isNullOrEmpty(testBattery)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Campaign")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Campaign name is missing!"));
            finalAnswer.setResultMessage(msg);
        } else {
            ITestBatteryService testBatteryService = appContext.getBean(ITestBatteryService.class);

            AnswerItem resp = testBatteryService.readByKey(testBattery);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) resp);
            } else {
                TestBattery test = (TestBattery) resp.getItem();
                if (!desc.equals(test.getDescription())) {
                    test.setDescription(desc);
                    finalAnswer = testBatteryService.update(test);
                    if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createForPrivateCalls("/UpdateTestBattery2", "UPDATE", "Update Test Battery : " + testBattery, request);
                    }
                }

                if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && batteryContent != null) {
                    JSONArray batteriesContent = new JSONArray(batteryContent);
                    ITestBatteryContentService testBatteryContentService = appContext.getBean(ITestBatteryContentService.class);
                    IFactoryTestBatteryContent factoryTestBatteryContent = appContext.getBean(IFactoryTestBatteryContent.class);
                    ArrayList<TestBatteryContent> arr = new ArrayList<>();
                    for (int i = 0; i < batteriesContent.length(); i++) {
                        JSONObject bat = batteriesContent.getJSONObject(i);
                        TestBatteryContent co = factoryTestBatteryContent.create(0, bat.getString("test"), bat.getString("testCase"), testBattery);
                        arr.add(co);
                    }

                    finalAnswer = testBatteryContentService.compareListAndUpdateInsertDeleteElements(test.getTestbattery(), arr);
                    if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createForPrivateCalls("/UpdateTestBattery2", "UPDATE", "Update Test battery : " + test.getTestbattery(), request);
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
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(UpdateTestBattery2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestBattery2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            String t = request.getParameter("value");
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(UpdateTestBattery2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestBattery2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
