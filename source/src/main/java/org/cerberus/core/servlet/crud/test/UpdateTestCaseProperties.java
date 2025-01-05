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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.crud.entity.LogEvent;

/**
 * @author bcivel
 */
@WebServlet(name = "UpdateTestCaseProperties", urlPatterns = {"/UpdateTestCaseProperties"})
public class UpdateTestCaseProperties extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateTestCaseProperties.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException  if a servlet-specific error occurs
     * @throws IOException       if an I/O error occurs
     * @throws CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        String initialTest = request.getParameter("informationInitialTest");
        String initialTestCase = request.getParameter("informationInitialTestCase");
        String test = request.getParameter("informationTest");
        String testCase = request.getParameter("informationTestCase");
        boolean duplicate = false;

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isEmptyOrNull(test) || StringUtil.isEmptyOrNull(testCase)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "mendatory fields are missing."));
            ans.setResultMessage(msg);
        } else {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
            ITestCaseCountryPropertiesService tccpService = appContext.getBean(ITestCaseCountryPropertiesService.class);

            AnswerItem resp = testCaseService.readByKey(test, testCase);
            TestCase tc = (TestCase) resp.getItem();
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "TestCase does not exist."));
                ans.setResultMessage(msg);

            } else /**
             * The service was able to perform the query and confirm the object
             * exist, then we can update it.
             */ {
                if (!testCaseService.hasPermissionsUpdate(tc, request)) { // We cannot update the testcase if the user is not at least in Test role.
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Not enought privilege to update the testcase. You mut belong to Test Privilege or even TestAdmin in case the test is in " + TestCase.TESTCASE_STATUS_WORKING + " status."));
                    ans.setResultMessage(msg);

                } else {

                    // Test Case exist and we can update it so Global update start here //
                    /**
                     * TestcaseCountryProperties Update.
                     */
                    List<TestCaseCountryProperties> tccpFromPage = getTestCaseCountryPropertiesFromParameter(request, appContext, test, testCase);
                    ans = tccpService.compareListAndUpdateInsertDeleteElements(initialTest, initialTestCase, tccpFromPage);

                    /**
                     * Adding Log entry.
                     */
                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Update was successful. Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createForPrivateCalls("/UpdateTestCaseProperties", "UPDATE", LogEvent.STATUS_INFO, "Update testcaseProperties : ['" + tc.getTest() + "'|'" + tc.getTestcase() + "']", request);
                    }

                }
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
    }

    private List<TestCaseCountryProperties> getTestCaseCountryPropertiesFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) throws JSONException {
        List<TestCaseCountryProperties> testCaseCountryProp = new ArrayList<>();
        JSONArray properties = new JSONArray(request.getParameter("propArr"));

        for (int i = 0; i < properties.length(); i++) {
            JSONObject propJson = properties.getJSONObject(i);

            boolean delete = propJson.getBoolean("toDelete");
            String property = propJson.getString("property");
            String description = propJson.getString("description");
            String type = propJson.getString("type");
            String value = propJson.getString("value1");
            String value2 = propJson.getString("value2");
            String length = propJson.getString("length");
            int rowLimit = propJson.getInt("rowLimit");
            int retryNb = propJson.optInt("retryNb");
            int rank = propJson.optInt("rank");
            int retryPeriod = propJson.optInt("retryPeriod");
            String nature = propJson.getString("nature");
            String database = propJson.getString("database");
            JSONArray countries = propJson.getJSONArray("country");
            if (!delete && !property.isEmpty()) {
                for (int j = 0; j < countries.length(); j++) {
                    String country = countries.getString(j);
                    testCaseCountryProp.add(TestCaseCountryProperties.builder()
                            .test(test)
                            .testcase(testCase)
                            .country(country)
                            .property(property)
                            .description(description)
                            .type(type)
                            .database(database)
                            .value1(value)
                            .value2(value2)
                            .length(length)
                            .rowLimit(rowLimit)
                            .nature(nature)
                            .retryNb(retryNb)
                            .retryPeriod(retryPeriod)
                            .cacheExpire(0)
                            .rank(rank)
                            .build()
                    );
                }
            }
        }
        return testCaseCountryProp;
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
