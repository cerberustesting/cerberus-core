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
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.crud.service.impl.TestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.impl.TestCaseCountryService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 * @author FNogueira
 */
@WebServlet(name = "ImportPropertyOfATestCaseToAnOtherTestCase", urlPatterns = {"/ImportPropertyOfATestCaseToAnOtherTestCase"})
public class ImportPropertyOfATestCaseToAnOtherTestCase extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ImportPropertyOfATestCaseToAnOtherTestCase.class);
    
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
        MessageEvent rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
         
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        ITestCaseCountryPropertiesService testCaseCountryPropertiesService = appContext.getBean(TestCaseCountryPropertiesService.class);
        ITestCaseCountryService testCaseCountryService = appContext.getBean(TestCaseCountryService.class);

        try {

            String fromTest = request.getParameter("fromtest");
            String fromTestcase = request.getParameter("fromtestcase");
            String propertyName = request.getParameter("property");
            String toTest = request.getParameter("totest");
            String toTestCase = request.getParameter("totestcase");

            // We retrieve all country of the destination TestCase
            List<String> toCountriesAll = testCaseCountryService.findListOfCountryByTestTestCase(toTest, toTestCase);

            if (toCountriesAll != null && toCountriesAll.size() > 0) {
                // Variable for the countries of a property of the source TestCase
                List<String> fromCountriesProp;
                // Variable for the countries of a property of the destination TestCase
                List<String> toCountriesProp;

                // Variable for the countryProperty will be retrieve
                TestCaseCountryProperties countryProperties;
                
                // List of all country of the destination test for the current property
                List<String> toCountries = new ArrayList<>();
                toCountries.addAll(toCountriesAll);

                // Retrieve the country of the destination TestCase for the property,
                // if not empty remove it (property aleady exists for these countries)
                toCountriesProp = testCaseCountryPropertiesService.findCountryByPropertyNameAndTestCase(toTest, toTestCase, propertyName);
                if (toCountriesProp != null && toCountriesProp.size() > 0) {
                    toCountries.removeAll(toCountriesProp);
                }

                // Retrieve the country of the source TestCase for the property, if empty do nothing
                fromCountriesProp = testCaseCountryPropertiesService.findCountryByPropertyNameAndTestCase(fromTest, fromTestcase, propertyName);
                if (fromCountriesProp != null && fromCountriesProp.size() > 0) {
                    // Only retain country in the two TestCase for the property
                    toCountries.retainAll(fromCountriesProp);
                    
                    // If countries list is empty do nothing
                    if (toCountries.size() > 0) {
                        List<TestCaseCountryProperties> listOfPropertiesToInsert = new ArrayList<>();
                        for (String country : toCountries) {
                            try {
                                // retrieve the source property for the current country
                                countryProperties = testCaseCountryPropertiesService.findTestCaseCountryPropertiesByKey(fromTest, fromTestcase, country, propertyName);
                                if (countryProperties != null) {
                                    // change the TestCase information to the destination TestCase
                                    countryProperties.setTest(toTest);
                                    countryProperties.setTestcase(toTestCase);
                                    
                                    listOfPropertiesToInsert.add(countryProperties);
                                    // Insert the new property
                                    //testCaseCountryPropertiesService.insertTestCaseCountryProperties(countryProperties);
                                }
                            } catch (CerberusException ex) {
                                LOG.debug(ex.toString());
                            }
                        }
                        //insert the new property for all countries specified
                        Answer answer = testCaseCountryPropertiesService.createListTestCaseCountryPropertiesBatch(listOfPropertiesToInsert);
                        rs = answer.getResultMessage();

                        //if the operation retrieved success it means that we are able to create new records
                        //then a new entry should be added by the log service
                        if(answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                            //  Adding Log entry.
                            ILogEventService logEventService = appContext.getBean(LogEventService.class);
                            logEventService.createForPrivateCalls( "/ImportPropertyOfATestCaseToAnOtherTestCase", "CREATE", LogEvent.STATUS_INFO, "Override from imported test step: " + propertyName, request);
                        }                        
                    }
                }else{
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Property ").replace("%OPERATION%", "CREATE").replace("%REASON%", "Property was not imported. Please check if is"
                            + " correctly defined on the imported step."));
                }
                
            }else{
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                rs.setDescription(rs.getDescription().replace("%ITEM%", "Property ").replace("%OPERATION%", "CREATE").replace("%REASON%", "No countries were defined for the test case."));
            }
            //sets the message returned by the operations 
            jsonResponse.put("messageType", rs.getMessage().getCodeString());
            jsonResponse.put("message", rs.getDescription());

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();
          
        }  catch (JSONException ex) {

            LOG.warn(ex);
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
