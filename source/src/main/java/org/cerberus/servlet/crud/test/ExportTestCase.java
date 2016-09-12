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
package org.cerberus.servlet.crud.test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.crud.dao.impl.TestCaseCountryPropertiesDAO;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILoadTestCaseService;
import org.cerberus.crud.service.ITestCaseService;
import org.json.JSONArray;
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
public class ExportTestCase extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param httpServletRequest servlet request
     * @param httpServletResponse servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        try {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestCaseService testService = appContext.getBean(ITestCaseService.class);

            //TODO pass DAO to Service
            ITestCaseCountryPropertiesDAO testCaseDAO = appContext.getBean(TestCaseCountryPropertiesDAO.class);

            ILoadTestCaseService loadTestCaseService = appContext.getBean(ILoadTestCaseService.class);

            PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
            String test = policy.sanitize(httpServletRequest.getParameter("test"));
            String testcase = policy.sanitize(httpServletRequest.getParameter("testcase"));

            TestCase tcInfo = testService.findTestCaseByKeyWithDependency(test, testcase);

            JSONObject jsonObject = new JSONObject();
            try {

                jsonObject.put("origin", tcInfo.getOrigine());
                jsonObject.put("refOrigin", tcInfo.getRefOrigine());
                jsonObject.put("creator", tcInfo.getUsrCreated());
                jsonObject.put("implementer", tcInfo.getImplementer());
                jsonObject.put("lastModifier", tcInfo.getUsrModif());
                jsonObject.put("project", tcInfo.getProject());
                jsonObject.put("ticket", tcInfo.getTicket());
                jsonObject.put("application", tcInfo.getApplication());
                jsonObject.put("runQA", tcInfo.getActiveQA());
                jsonObject.put("runUAT", tcInfo.getActiveUAT());
                jsonObject.put("runPROD", tcInfo.getActivePROD());
                jsonObject.put("priority", tcInfo.getPriority());
                jsonObject.put("group", tcInfo.getGroup());
                jsonObject.put("status", tcInfo.getStatus());
                JSONArray countryList = new JSONArray();
                for(TestCaseCountry tcc : tcInfo.getTestCaseCountry()){
                    countryList.put(tcc.getCountry());
                }
                jsonObject.put("countriesList", countryList);
                jsonObject.put("shortDescription", tcInfo.getDescription());
                jsonObject.put("description", tcInfo.getBehaviorOrValueExpected());
                jsonObject.put("howTo", tcInfo.getHowTo());
                jsonObject.put("active", tcInfo.getTcActive());
                jsonObject.put("fromSprint", tcInfo.getFromBuild());
                jsonObject.put("fromRevision", tcInfo.getFromRev());
                jsonObject.put("toSprint", tcInfo.getToBuild());
                jsonObject.put("toRevision", tcInfo.getToRev());
                jsonObject.put("lastExecutionStatus", tcInfo.getLastExecutionStatus());
                jsonObject.put("bugID", tcInfo.getBugID());
                jsonObject.put("targetSprint", tcInfo.getTargetBuild());
                jsonObject.put("targetRevision", tcInfo.getTargetRev());
                jsonObject.put("comment", tcInfo.getComment());
                jsonObject.put("test", tcInfo.getTest());
                jsonObject.put("testcase", tcInfo.getTestCase());

                JSONArray propertyList = new JSONArray();
                List<TestCaseCountryProperties> properties = testCaseDAO.findDistinctPropertiesOfTestCase(test, testcase);

                for (TestCaseCountryProperties prop : properties) {
                    JSONObject property = new JSONObject();

                    property.put("property", prop.getProperty());
                    property.put("description", prop.getDescription());
                    property.put("type", prop.getType());
                    property.put("database", prop.getDatabase());
                    property.put("value1", prop.getValue1());
                    property.put("value2", prop.getValue2());
                    property.put("length", prop.getLength());
                    property.put("rowLimit", prop.getRowLimit());
                    property.put("nature", prop.getNature());
                    List<String> countriesSelected = testCaseDAO.findCountryByProperty(prop);
                    for (TestCaseCountry tcc : tcInfo.getTestCaseCountry()) {
                        if (countriesSelected.contains(tcc.getCountry())) {
                            property.put(tcc.getCountry(), true);
                        } else {
                            property.put(tcc.getCountry(), false);
                        }
                    }
                    propertyList.put(property);
                }
                jsonObject.put("properties", propertyList);

                List<TestCaseStep> tcs = loadTestCaseService.loadTestCaseStep(tcInfo);
                JSONArray list = new JSONArray();

                for (TestCaseStep step : tcs) {
                    JSONObject stepObject = new JSONObject();
                    stepObject.put("number", step.getStep());
                    stepObject.put("name", step.getDescription());
                    int i = 1;
                    JSONArray actionList = new JSONArray();
                    JSONArray controlList = new JSONArray();
                    JSONArray sequenceList = new JSONArray();

                    for (TestCaseStepAction action : step.getTestCaseStepAction()) {
                        JSONObject actionObject = new JSONObject();
                        actionObject.put("sequence", i);
                        actionObject.put("action", action.getAction());
                        actionObject.put("object", action.getValue1());
                        actionObject.put("property", action.getValue2());
                        actionObject.put("fatal", "");
                        actionList.put(actionObject);
                        sequenceList.put(actionObject);

                        for (TestCaseStepActionControl control : action.getTestCaseStepActionControl()){
                            JSONObject controlObject = new JSONObject();
                            controlObject.put("step", control.getStep());
                            controlObject.put("sequence", control.getSequence());
                            controlObject.put("order", control.getControl());
                            controlObject.put("action", control.getType());
                            controlObject.put("object", control.getControlProperty());
                            controlObject.put("property", control.getControlValue());
                            controlObject.put("fatal", control.getFatal());
                            controlList.put(controlObject);
                            //test
                            controlObject = new JSONObject();
                            controlObject.put("sequence", i);
                            controlObject.put("action", control.getType());
                            controlObject.put("object", control.getControlProperty());
                            controlObject.put("property", control.getControlValue());
                            controlObject.put("fatal", control.getFatal());
                            sequenceList.put(controlObject);
                        }
                        i++;
                    }
                    stepObject.put("actions", actionList);
                    stepObject.put("controls", controlList);
                    stepObject.put("sequences", sequenceList);
                    list.put(stepObject);
                }
//                jsonObject.put("actions", actionList);
//                jsonObject.put("controls", controlList);
                jsonObject.put("list", list);

                httpServletResponse.setContentType("application/json");
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename="+test+testcase+".json");
        httpServletResponse.getOutputStream().print(jsonObject.toString());
        
        } catch (JSONException exception) {
                MyLogger.log(GetTestCase.class.getName(), Level.WARN, exception.toString());
            }
        } catch (CerberusException ex) {
            Logger.getLogger(GetTestCase.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
