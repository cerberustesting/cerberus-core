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
import org.cerberus.engine.entity.MessageEvent;
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
import org.json.JSONArray;
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
        try {

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
            String databaseCsv = policy.sanitize(request.getParameter("databaseCsv"));
            // Parameter that needs to be secured --> We SECURE+DECODE them
            String name = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("name"), null, charset);
            String group = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("group"), "", charset);
            String description = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("libdescription"), "", charset);
            // Parameter that we cannot secure as we need the html --> We DECODE them
            String script = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("script"), "", charset);
            String servicePath = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("servicepath"), "", charset);
            String method = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("method"), "", charset);
            String envelope = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("envelope"), "", charset);
            String csvUrl = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("csvUrl"), "", charset);
            String separator = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("separator"), "", charset);
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
                        type, database, script, databaseUrl, servicePath, method, envelope, databaseCsv, csvUrl, separator, description,
                        request.getRemoteUser(), null, "", null, null, null, null, null);

                // Getting list of application from JSON Call
                JSONArray objSubDataArray = new JSONArray(request.getParameter("subDataList"));
                List<TestDataLibData> tdldList = new ArrayList();
                tdldList = getSubDataFromParameter(request, appContext, -1, objSubDataArray);

                lib.setSubDataLib(tdldList);
                //Creates the entries and the subdata list
                ans = libService.create(lib);

                /**
                 * Object created. Adding Log entry.
                 */
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createPrivateCalls("/CreateTestDataLib", "CREATE", "Create TestDataLib  : " + request.getParameter("name"), request);
                }
            }

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

    private List<TestDataLibData> getSubDataFromParameter(HttpServletRequest request, ApplicationContext appContext, int testDataLibId, JSONArray json) throws JSONException {
        List<TestDataLibData> tdldList = new ArrayList();
        IFactoryTestDataLibData tdldFactory = appContext.getBean(IFactoryTestDataLibData.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            Integer testDataLibDataId = objectJson.getInt("testDataLibDataID");
            // Parameter that needs to be secured --> We SECURE+DECODE them
            // NONE
            // Parameter that we cannot secure as we need the html --> We DECODE them
            String subdata = ParameterParserUtil.parseStringParamAndDecode(objectJson.getString("subData"), "", charset);
            String value = ParameterParserUtil.parseStringParamAndDecode(objectJson.getString("value"), "", charset);
            String column = ParameterParserUtil.parseStringParamAndDecode(objectJson.getString("column"), "", charset);
            String parsingAnswer = ParameterParserUtil.parseStringParamAndDecode(objectJson.getString("parsingAnswer"), "", charset);
            String columnPosition = ParameterParserUtil.parseStringParamAndDecode(objectJson.getString("columnPosition"), "", charset);
            String description = ParameterParserUtil.parseStringParamAndDecode(objectJson.getString("description"), "", charset);

            if (!delete) {
                TestDataLibData tdld = tdldFactory.create(testDataLibDataId, testDataLibId, subdata, value, column, parsingAnswer, columnPosition, description);
                tdldList.add(tdld);
            }
        }
        return tdldList;
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
