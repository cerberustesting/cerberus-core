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
package org.cerberus.core.servlet.crud.test;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.factory.IFactoryTestCaseLabel;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
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
@WebServlet(name = "SaveTestCaseLabel", urlPatterns = {"/SaveTestCaseLabel"})
public class SaveTestCaseLabel extends HttpServlet {
    
    private static final Logger LOG = LogManager.getLogger(SaveTestCaseLabel.class);
    private ITestCaseLabelService testCaseLabelService;
    private IFactoryTestCaseLabel testCaseLabelFactory;

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        String usr = request.getUserPrincipal().getName();

        testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);
        testCaseLabelFactory = appContext.getBean(IFactoryTestCaseLabel.class);
        
        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        String test = policy.sanitize(request.getParameter("test"));
        String testcase = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testcase"), "", charset);
        
        
        String[] labelIdList = request.getParameterValues("labelid");
        /**
         * Checking all constrains before calling the services.
         */
        if (test.isEmpty() || testcase.isEmpty()) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseLabel")
                    .replace("%OPERATION%", "Save")
                    .replace("%REASON%", "Mandatory parameter is missing. [test|testcase]"));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            List<TestCaseLabel> toSave = new ArrayList<>();
            if (null!= labelIdList && labelIdList.length!=0) {
                Timestamp creationDate = new Timestamp(new Date().getTime());
                for (String lableId : labelIdList){
                toSave.add(testCaseLabelFactory.create(0, test, testcase, Integer.valueOf(lableId), usr, creationDate, usr, creationDate, null));
                }
            }
            
            List<TestCaseLabel> existingList = testCaseLabelService.readByTestTestCase(test, testcase, null).getDataList();
            testCaseLabelService.compareListAndUpdateInsertDeleteElements(test, testcase, toSave);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseLabel")
                    .replace("%OPERATION%", "Save")); 
            ans.setResultMessage(msg);
            
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
