/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.servlet.crud.testdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Handles the modification of subdata entries.
 *
 * @author FNogueira
 */
@WebServlet(name = "UpdateTestDataLibData", urlPatterns = {"/UpdateTestDataLibData"})
public class UpdateTestDataLibData extends HttpServlet {

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
            throws ServletException, IOException {
        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("application/json");

        String data = request.getParameter("data");

        try {
            boolean validationsOK = true;
            if (StringUtil.isNullOrEmpty(data)) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Sub-data entries")
                        .replace("%OPERATION%", "Update sub-data entries")
                        .replace("%REASON%", "Data to be modified is missing! "));
                ans.setResultMessage(msg);
            } else {

                JSONObject dataToEdit = new JSONObject(data);

                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

                //checks if the subdataentry can be deleted
                ITestDataLibDataService subDataService = appContext.getBean(ITestDataLibDataService.class);

                //removes the entries selected by the user
                ArrayList<TestDataLibData> entriesToRemove = new ArrayList<TestDataLibData>();
                if (dataToEdit.has("remove")) {
                    AnswerList removeListAnswer = parseTestDataLibDataList((JSONArray) dataToEdit.get("remove"), appContext);
                    //check if validations are ok
                    if (removeListAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_VALIDATIONS_OK.getCode())) {
                        entriesToRemove.addAll((List<TestDataLibData>) removeListAnswer.getDataList());
                    } else {
                        //has an error
                        validationsOK = false;
                    }
                }

                //updates the selected entries
                ArrayList<TestDataLibData> entriesToUpdate = new ArrayList<TestDataLibData>();
                if (dataToEdit.has("update")) {
                    AnswerList updateListAnswer = parseTestDataLibDataList((JSONArray) dataToEdit.get("update"), appContext);
                    if (updateListAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_VALIDATIONS_OK.getCode())) {
                        entriesToUpdate.addAll((List<TestDataLibData>) updateListAnswer.getDataList());
                    } else {
                        //has an error
                        validationsOK = false;
                    }
                }

                //inserts new subdataentries
                ArrayList<TestDataLibData> entriesToInsert = new ArrayList<TestDataLibData>();
                if (dataToEdit.has("insert")) {
                    AnswerList insertListAnswer = parseTestDataLibDataList((JSONArray) dataToEdit.get("insert"), appContext);
                    //check if validations are ok
                    if (insertListAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_VALIDATIONS_OK.getCode())) {
                        entriesToInsert.addAll((List<TestDataLibData>) insertListAnswer.getDataList());

                    } else {
                        //has an error
                        validationsOK = false;
                    }

                }
                if (validationsOK) {
                    //check if the lists contain data
                    if (entriesToInsert.isEmpty() && entriesToUpdate.isEmpty() && entriesToRemove.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Sub-data entries")
                                .replace("%OPERATION%", "Update sub-data entries")
                                .replace("%REASON%", "No data sent "));
                        ans.setResultMessage(msg);

                    } else {
                        //everything is ok
                        ans = subDataService.createUpdateDelete(entriesToInsert, entriesToUpdate, entriesToRemove);

                        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                            log(appContext, entriesToRemove, request, entriesToUpdate, entriesToInsert);
                        }
                    }
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Sub-data entries")
                            .replace("%OPERATION%", "Update sub-data entries")
                            .replace("%REASON%", "Data to be modified is not valid! "));
                    ans.setResultMessage(msg);
                }
            }
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse);
            response.getWriter().flush();

        } catch (JSONException ex) {
            org.apache.log4j.Logger.getLogger(ReadTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
            response.getWriter().flush();
        }

    }

    private void log(ApplicationContext appContext, ArrayList<TestDataLibData> entriesToRemove, HttpServletRequest request, ArrayList<TestDataLibData> entriesToUpdate, ArrayList<TestDataLibData> entriesToInsert) throws BeansException {
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        //does the log for each record modified
        /**
         * Objects Modified. Adding Log entry.
         */
        for (TestDataLibData entry : entriesToRemove) {
            logEventService.createPrivateCalls("/UpdateTestDataLibData", "DELETE", "Sub-data entry with id: "
                    + entry.getTestDataLibDataID() + " name: " + entry.getSubData() + "[library id: " + entry.getTestDataLibID() + "]", request);
        }
        for (TestDataLibData entry : entriesToUpdate) {
            logEventService.createPrivateCalls("/UpdateTestDataLibData", "UPDATE", "Sub-data entry with id: "
                    + entry.getTestDataLibDataID() + " name: " + entry.getSubData() + "[library id: " + entry.getTestDataLibID() + "]", request);
        }
        for (TestDataLibData entry : entriesToInsert) {
            logEventService.createPrivateCalls("/UpdateTestDataLibData", "INSERT", "New sub-data entry : "
                    + entry.getTestDataLibDataID() + " name: " + entry.getSubData() + "[library id: " + entry.getTestDataLibID() + "]", request);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    private AnswerList parseTestDataLibDataList(JSONArray entryList, ApplicationContext appContext) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_OK);
        ArrayList<TestDataLibData> entries = new ArrayList<TestDataLibData>();
        IFactoryTestDataLibData factoryLibService = appContext.getBean(IFactoryTestDataLibData.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        JSONObject obj;

        for (int i = 0; i < entryList.length(); i++) {
            try {
                obj = entryList.getJSONObject(i);
                TestDataLibData item = factoryLibService.create(
                        Integer.parseInt(obj.get("testdatalibdataid").toString()), //parse will do the sanitize, it will vail if the id is text
                        Integer.parseInt(obj.get("testdatalibid").toString()),//parse will do the sanitize, it will vail if the id is text
                        policy.sanitize(obj.get("subdata").toString()),
                        policy.sanitize(obj.get("value").toString()),
                        policy.sanitize(obj.get("column").toString()),
                        policy.sanitize(obj.get("parsinganswer").toString()),
                        policy.sanitize(obj.get("description").toString()));
                entries.add(item);
            } catch (NumberFormatException ex) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ex.getMessage()));
                entries.clear();
                break;
            } catch (JSONException ex) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ex.getMessage()));
                entries.clear();
                break;
            }
        }

        answer.setResultMessage(msg);
        answer.setDataList(entries);
        answer.setTotalRows(entries.size());

        return answer;
    }
}
