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
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.factory.IFactoryTestDataLib;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet responsible for handling the creation of new test data lib entries
 *
 * @author FNogueira
 */
@WebServlet(name = "CreateTestDataLib", urlPatterns = {"/CreateTestDataLib"})
public class CreateTestDataLib extends HttpServlet {

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
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        String type = policy.sanitize(request.getParameter("type"));
        String system = policy.sanitize(request.getParameter("system"));
        String environment = policy.sanitize(request.getParameter("environment"));
        String country = policy.sanitize(request.getParameter("country"));
        String database = policy.sanitize(request.getParameter("database"));
        String databaseUrl = policy.sanitize(request.getParameter("databaseUrl"));
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String name = ParameterParserUtil.parseStringParam(request.getParameter("name"), null);
        String group = ParameterParserUtil.parseStringParam(request.getParameter("group"), "");
        String description = ParameterParserUtil.parseStringParam(request.getParameter("libdescription"), "");
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String script = ParameterParserUtil.parseStringParam(request.getParameter("script"), "");
        String servicePath = ParameterParserUtil.parseStringParam(request.getParameter("servicepath"), "");
        String method = ParameterParserUtil.parseStringParam(request.getParameter("method"), "");
        String envelope = ParameterParserUtil.parseStringParam(request.getParameter("envelope"), "");
        /**
         * Checking all constrains before calling the services.
         */

        if (StringUtil.isNullOrEmpty(name)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Test data library name is missing! "));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);
            IFactoryTestDataLib factoryLibService = appContext.getBean(IFactoryTestDataLib.class);

            TestDataLib lib = factoryLibService.create(0, name, system, environment, country, group,
                    type, database, script, databaseUrl, servicePath, method, envelope, description,
                    request.getRemoteUser(), null, "", null, null, null, null);
            List<TestDataLibData> subDataList = new ArrayList<TestDataLibData>();
            subDataList.addAll(extractTestDataLibDataSet(appContext, request, policy));
            //Creates the entries and the subdata list
            ans = libService.create(lib, subDataList);

            /**
             * Object created. Adding Log entry.
             */
            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createPrivateCalls("/CreateTestDataLib", "CREATE", "Create TestDataLib  : " + request.getParameter("name"), request);
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
            org.apache.log4j.Logger.getLogger(CreateTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
            response.getWriter().flush();
        }

    }

    private List<TestDataLibData> extractTestDataLibDataSet(ApplicationContext appContext, HttpServletRequest request, PolicyFactory policy) {
        //now we can insert the testdatalibdata that was specified in the insert page
        //we can have several tesdatalibdata
        IFactoryTestDataLibData factorySubdataService = appContext.getBean(IFactoryTestDataLibData.class);

        List<TestDataLibData> listSubdata = new ArrayList<TestDataLibData>();
        //as all fields (subadata, data, description) are mandatory  there will no problem
        //with accessing the following arrays
        String[] subdataEntries = request.getParameterValues("subdata");
        String[] subdataValues = request.getParameterValues("value");
        String[] subdataColumns = request.getParameterValues("column");
        String[] subdataParsingAnswer = request.getParameterValues("parsinganswer");
        String[] subdataDescriptions = request.getParameterValues("description");
        String charset = request.getCharacterEncoding();

        TestDataLibData subData;

        for (int i = 0; i < subdataEntries.length; i++) {
            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            // Parameter that needs to be secured --> We SECURE+DECODE them
            String subdata = ParameterParserUtil.parseStringParam(subdataEntries[i], null);
            String description = ParameterParserUtil.parseStringParam(subdataDescriptions[i], "");
            // Parameter that we cannot secure as we need the html --> We DECODE them
            String value = ParameterParserUtil.parseStringParam(subdataValues[i], "");
            String column = ParameterParserUtil.parseStringParam(subdataColumns[i], "");
            String parsinganswer = ParameterParserUtil.parseStringParam(subdataParsingAnswer[i], "");

            subData = factorySubdataService.create(null, null, //ids are not available yet
                    subdata, value, column, parsinganswer, description);
            listSubdata.add(subData);
        }

        return listSubdata;

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
