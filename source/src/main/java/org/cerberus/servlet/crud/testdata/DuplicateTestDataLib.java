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
package org.cerberus.servlet.crud.testdata;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.factory.IFactoryTestDataLib;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * Servlet responsible for handling the creation of new test data lib entries
 *
 * @author FNogueira
 */
@WebServlet(name = "DuplicateTestDataLib", urlPatterns = {"/DuplicateTestDataLib"})
public class DuplicateTestDataLib extends HttpServlet {

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
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding();

        response.setContentType("application/json");

        /**
         * Parsing and securing all required parameters.
         */
        Integer testdatalibid = 0;
        boolean testdatalibid_error = true;
        try {
            if (request.getParameter("testdatalibid") != null && !request.getParameter("testdatalibid").isEmpty()) {
                testdatalibid = Integer.valueOf(request.getParameter("testdatalibid"));
                testdatalibid_error = false;
            }
        } catch (NumberFormatException ex) {
            testdatalibid_error = true;
            org.apache.log4j.Logger.getLogger(DuplicateTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
        }
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        String type = policy.sanitize(request.getParameter("type"));
        String system = policy.sanitize(request.getParameter("system"));
        String environment = policy.sanitize(request.getParameter("environment"));
        String country = policy.sanitize(request.getParameter("country"));
        String database = policy.sanitize(request.getParameter("database"));
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String name = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("name"), null, charset);
        String group = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("group"), "", charset);
        String description = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("libdescription"), "", charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String script = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("script"), "", charset);
        String servicePath = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("servicepath"), "", charset);
        String method = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("method"), "", charset);
        String envelope = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("envelope"), "", charset);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(name)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Test data library name is missing! "));
            ans.setResultMessage(msg);
        } else if (testdatalibid_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data library")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Could not manage to convert testdatalibid to an integer value or testdatalibid is missing."));
            ans.setResultMessage(msg);
        } else {
            //data is valid, then we can call the servicesl
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);
            IFactoryTestDataLib factoryLibService = appContext.getBean(IFactoryTestDataLib.class);

            TestDataLib lib = factoryLibService.create(testdatalibid, name, system, environment, country, group, type, database, script,
                    servicePath, method, envelope, description, request.getRemoteUser(), null, "", null, null, null, null);

            AnswerItem existsAnswer = libService.readByKey(lib.getName(), lib.getSystem(), lib.getEnvironment(), lib.getCountry());

            if (!existsAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND.getCode())) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library")
                        .replace("%OPERATION%", "Duplicate")
                        .replace("%REASON%", "The test data library that you are trying to create already exists!"));
                ans.setResultMessage(msg);

            } else {
                //data was not found, it means that the we can duplicate the entry
                ans = libService.duplicate(lib);

                //Object created. Adding Log entry.
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createPrivateCalls("/DuplicateTestDataLib", "DUPLICATE", "Duplicate with id:  : " + testdatalibid, request);
                }
            }
        }

        try {
            /**
             * Formating and returning the json result.
             */
            //sets the message returned by the operations
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();
        } catch (JSONException ex) {
            org.apache.log4j.Logger.getLogger(DuplicateTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
            response.getWriter().flush();
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
