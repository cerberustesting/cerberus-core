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
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.service.IDocumentationService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.impl.DocumentationService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.crud.service.impl.TestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.impl.TestCaseCountryService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.stream.Collectors;
import org.cerberus.core.crud.entity.LogEvent;

/**
 * Creates a new property for the test case.
 *
 * @author memiks
 * @author FNogueira
 */
@WebServlet(name = "CreateNotDefinedProperty", urlPatterns = {"/CreateNotDefinedProperty"})
public class CreateNotDefinedProperty extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(CreateNotDefinedProperty.class);

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
            throws ServletException, IOException {

        JSONObject jsonResponse = new JSONObject();
        MessageEvent rs = null;

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        ITestCaseCountryPropertiesService testCaseCountryPropertiesService = appContext.getBean(TestCaseCountryPropertiesService.class);
        ITestCaseCountryService testCaseCountryService = appContext.getBean(TestCaseCountryService.class);

        try {

            final String propertyName = request.getParameter("property") == null ? request.getParameter("property").replace("%", "") : request.getParameter("property");
            String toTest = request.getParameter("totest");
            String toTestCase = request.getParameter("totestcase");
            String propertyType = request.getParameter("propertyType");
            String userLanguage = request.getParameter("userLanguage");

            // We retrieve all country of the destination TestCase
            List<String> toCountriesAll = testCaseCountryService.findListOfCountryByTestTestCase(toTest, toTestCase);

            if (toCountriesAll != null && toCountriesAll.size() > 0) {

                // Variable for the countries of a property of the destination TestCase
                List<String> toCountriesProp;
                IDocumentationService docService = appContext.getBean(DocumentationService.class);
                String notDefinedProperty = docService.findLabel("page_testcase", "txt_property_not_defined", "** Property not defined **", userLanguage);
                // List of all country of the destination test for the current property
                List<String> toCountries = new ArrayList<>();
                toCountries.addAll(toCountriesAll);

                // Retrieve the country of the destination TestCase for the property,
                // if not empty remove it (property aleady exists for these countries)
                toCountriesProp = testCaseCountryPropertiesService.findCountryByPropertyNameAndTestCase(toTest, toTestCase, propertyName);
                if (toCountriesProp != null && toCountriesProp.size() > 0) {
                    toCountries.removeAll(toCountriesProp);
                }

                // Variable for the properties list of the destination TestCase
                List<TestCaseCountryProperties> listOfPropertiesToInsert = toCountries
                        .stream()
                        .map(country -> TestCaseCountryProperties.builder()
                                .test(toTest)
                                .testcase(toTestCase)
                                .country(country)
                                .description("")
                                .property(propertyName)
                                .type(propertyType)
                                .database("---")
                                .value1(notDefinedProperty)
                                .value2("")
                                .length("0")
                                .rowLimit(0)
                                .nature("STATIC")
                                .retryNb(0)
                                .retryPeriod(10000)
                                .cacheExpire(0)
                                .rank(1)
                                .build())
                        .collect(Collectors.toList());

                Answer answer = testCaseCountryPropertiesService.createListTestCaseCountryPropertiesBatch(listOfPropertiesToInsert);
                rs = answer.getResultMessage();

                //if the operation retrieved success it means that we are able to create new records
                //then a new entry should be added by the log service
                if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    //  Adding Log entry.
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/CreateNotDefinedProperty", "CREATE", LogEvent.STATUS_INFO, "Create NotDefinedProperty:" + " " + propertyName, request);
                }
            } else {
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                rs.setDescription(rs.getDescription().replace("%ITEM%", "Property ").replace("%OPERATION%", "CREATE").replace("%REASON%", "No countries were defined for the test case."));
            }

            //sets the message returned by the operations
            jsonResponse.put("messageType", rs.getMessage().getCodeString());
            jsonResponse.put("message", rs.getDescription());

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();

        } catch (JSONException ex) {

            LOG.warn(ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
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
        processRequest(request, response);
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
