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
package org.cerberus.servlet.crud.testexecution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestService;
import org.cerberus.dto.ExecutionValidator;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.engine.IExecutionCheckService;
import org.cerberus.util.ParameterParserUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "GetExecutionQueue", urlPatterns = {"/GetExecutionQueue"})
public class GetExecutionQueue extends HttpServlet {

    private ITestCaseExecutionService testCaseExecutionService;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.json.JSONException
     * @throws org.cerberus.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException, CerberusException {
        JSONObject jsonResponse = new JSONObject();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        boolean check = ParameterParserUtil.parseBooleanParam(request.getParameter("check"), false);
        boolean push = ParameterParserUtil.parseBooleanParam(request.getParameter("push"), false);

        if (check) {
            IApplicationService applicationService = appContext.getBean(IApplicationService.class);
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);
            ITestService testService = appContext.getBean(ITestService.class);
            ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
            ICountryEnvParamService cepService = appContext.getBean(ICountryEnvParamService.class);
            testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
            List<ExecutionValidator> inQueue = new ArrayList<ExecutionValidator>();

            JSONArray testCaseList = new JSONArray(request.getParameter("testcase"));
            JSONArray environmentList = new JSONArray(request.getParameter("environment"));
            JSONArray countryList = new JSONArray(request.getParameter("countries"));

            List<TCase> TCList = new ArrayList<TCase>();
            List<CountryEnvParam> envList = new ArrayList<CountryEnvParam>();
            List<String> countries = new ArrayList<String>();

            for (int index = 0; index < testCaseList.length(); index++) {
                JSONObject testCaseJson = testCaseList.getJSONObject(index);
                TCase tc = new TCase();

                tc.setTest(testCaseJson.getString("test"));
                tc.setTestCase(testCaseJson.getString("testcase"));
                TCList.add(tc);
            }

            for (int index = 0; index < environmentList.length(); index++) {
                JSONObject envJson = environmentList.getJSONObject(index);
                CountryEnvParam cep = new CountryEnvParam();

                cep.setEnvironment(envJson.getString("env"));
                envList.add(cep);
            }

            for (int index = 0; index < countryList.length(); index++) {
                String country = countryList.getString(index);
                countries.add(country);
            }

            List<TestCaseExecution> tceList = testCaseExecutionService.createAllTestCaseExecution(TCList, envList, countries);

            IExecutionCheckService execCheckService = appContext.getBean(IExecutionCheckService.class);

            for (TestCaseExecution execution : tceList) {
                boolean exception = false;
                ExecutionValidator validator = new ExecutionValidator();

                try {
                    execution.setTestObj(testService.convert(testService.readByKey(execution.getTest())));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TEST_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replaceAll("%TEST%", execution.getTest()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                try {
                    execution.settCase(testCaseService.findTestCaseByKey(execution.getTest(), execution.getTestCase()));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replaceAll("%TEST%", execution.getTest()));
                    mes.setDescription(mes.getDescription().replaceAll("%TESTCASE%", execution.getTestCase()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                try {
                    execution.setApplication(applicationService.convert(applicationService.readByKey(execution.gettCase().getApplication())));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_APPLICATION_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replaceAll("%APPLI%", execution.gettCase().getApplication()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                execution.setEnvironmentData(execution.getEnvironment());

                try {
                    execution.setCountryEnvParam(cepService.findCountryEnvParamByKey(execution.getApplication().getSystem(), execution.getCountry(), execution.getEnvironment()));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENV_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", execution.getApplication().getSystem()));
                    mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", execution.getCountry()));
                    mes.setDescription(mes.getDescription().replaceAll("%ENV%", execution.getEnvironmentData()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                try {
                    execution.setEnvironmentDataObj(invariantService.findInvariantByIdValue("ENVIRONMENT", execution.getEnvironmentData()));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST);
                    mes.setDescription(mes.getDescription().replaceAll("%ENV%", execution.getEnvironmentData()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                execution.setBrowser("firefox");

                if (exception == false) {
                    MessageGeneral message = execCheckService.checkTestCaseExecution(execution);
                    if (!(message.equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS)))) {
                        validator.setValid(false);
                        validator.setMessage(message.getDescription());
                    } else {
                        validator.setValid(true);
                        validator.setMessage(message.getDescription());
                    }
                }
                validator.setExecution(execution);
                inQueue.add(validator);
            }

            JSONArray answer = new JSONArray();
            for (ExecutionValidator tce : inQueue) {
                JSONObject exec = new JSONObject();

                exec.put("test", tce.getExecution().getTest());
                exec.put("testcase", tce.getExecution().getTestCase());
                exec.put("env", tce.getExecution().getEnvironment());
                exec.put("country", tce.getExecution().getCountry());
                exec.put("isValid", tce.isValid());
                exec.put("message", tce.getMessage());
                answer.put(exec);
            }
            jsonResponse.put("contentTable", answer);
        }

        response.setContentType("application/json");
        response.getWriter().print(jsonResponse.toString());
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
        } catch (JSONException ex) {
            Logger.getLogger(GetExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CerberusException ex) {
            Logger.getLogger(GetExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (JSONException ex) {
            Logger.getLogger(GetExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CerberusException ex) {
            Logger.getLogger(GetExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
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
