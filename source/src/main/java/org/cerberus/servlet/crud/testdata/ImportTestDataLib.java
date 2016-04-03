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
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.service.IImportFileService;
import org.cerberus.crud.service.IImportFileService.XMLHandlerEnumType;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet responsible for handling the import of test data lib entries from
 * files.
 *
 * @author FNogueira
 */
@WebServlet(name = "ImportTestDataLib", urlPatterns = {"/ImportTestDataLib"})
@MultipartConfig
public class ImportTestDataLib extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        Answer answer = new Answer(msg);

        JSONObject jsonResponse = new JSONObject();

        response.setContentType("application/json");

        try {

            //file sent from the upload
            final Part filePart = request.getPart("fileInput");

            if (request.getPart("fileInput") != null) {

                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                IImportFileService importService = appContext.getBean(IImportFileService.class);

                //schema that will be used to validate the file uploaded by the user
                InputStream schemaLocation = getServletContext().getResourceAsStream("/WEB-INF/classes/xsd/testdatalib.xsd");

                AnswerItem dataFromService = importService.importAndValidateXMLFromInputStream(filePart.getInputStream(), schemaLocation, XMLHandlerEnumType.TESTDATALIB_HANDLER);
                msg = dataFromService.getResultMessage();
                answer.setResultMessage(msg);

                if (dataFromService.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    //if the import succeeds then we can insert the data
                    HashMap<TestDataLib, List<TestDataLibData>> map = (HashMap<TestDataLib, List<TestDataLibData>>) dataFromService.getItem();

                    //creates the testdatalib data that was imported from file
                    ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);

                    answer = libService.create(map);

                    if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_IMPORT_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library"));
                        //Adding log entry
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createPrivateCalls("/ImportTestDataLib", "IMPORT", "Test Data Library Imported.", request);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_IMPORT_ERROR);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib").replace("%REASON%", answer.getMessageDescription()));
                    }
                }
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "A Problem was found with the file that is being imported."));
                answer.setResultMessage(msg);
            }

            jsonResponse.put("messageType", msg.getMessage().getCodeString());
            jsonResponse.put("message", msg.getDescription());

            response.getWriter().print(jsonResponse);
            response.getWriter().flush();

        } catch (JSONException ex) {
            org.apache.log4j.Logger.getLogger(ImportTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
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

}
