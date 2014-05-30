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
package org.cerberus.servlet.testCase;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.CountryEnvironmentDatabase;
import org.cerberus.entity.SoapLibrary;
import org.cerberus.entity.SqlLibrary;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestData;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.ICountryEnvironmentDatabaseService;
import org.cerberus.service.ISoapLibraryService;
import org.cerberus.service.ISqlLibraryService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.service.ITestDataService;
import org.cerberus.service.impl.ApplicationService;
import org.cerberus.service.impl.CountryEnvironmentDatabaseService;
import org.cerberus.service.impl.SoapLibraryService;
import org.cerberus.service.impl.SqlLibraryService;
import org.cerberus.service.impl.TestCaseService;
import org.cerberus.service.impl.TestDataService;
import org.cerberus.serviceEngine.IConnectionPoolDAO;
import org.cerberus.serviceEngine.IPropertyService;
import org.cerberus.serviceEngine.impl.ConnectionPoolDAO;
import org.cerberus.serviceEngine.impl.PropertyService;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Frederic LESUR
 * @version 1.0, 24/03/2014
 * @since 0.9.0
 */
@WebServlet(name="CalculatePropertyForTestCase", value = "/CalculatePropertyForTestCase")
public class CalculatePropertyForTestCase extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
        String type = policy.sanitize(httpServletRequest.getParameter("type"));
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        String result = null;
        String description = null;
        String property = httpServletRequest.getParameter("property");
        try {
            if (type.equals("getFromTestData")) {
                ITestDataService testDataService = appContext.getBean(TestDataService.class);
                TestData td = testDataService.findTestDataByKey(property);
                result = td.getValue();
                description = td.getDescription();

            } else if (type.equals("executeSoapFromLib")) {
                ISoapLibraryService soapLibraryService = appContext.getBean(SoapLibraryService.class);
                SoapLibrary soapLib = soapLibraryService.findSoapLibraryByKey(property);
                if (soapLib != null) {
                    IPropertyService propertyService = appContext.getBean(PropertyService.class);
                    result = propertyService.calculatePropertyFromSOAPResponse(soapLib, null, null);
                    description = soapLib.getDescription();
                }
            } else {
                String system = null;
                try {
                    String testName = policy.sanitize(httpServletRequest.getParameter("test"));
                    String testCaseName = policy.sanitize(httpServletRequest.getParameter("testCase"));


                    ITestCaseService testCaseService = appContext.getBean(TestCaseService.class);
                    IApplicationService applicationService = appContext.getBean(ApplicationService.class);

                    TCase testCase = testCaseService.findTestCaseByKey(testName, testCaseName);
                    system = applicationService.findApplicationByKey(testCase.getApplication()).getSystem();
                } catch (CerberusException ex) {
                    Logger.getLogger(CalculatePropertyForTestCase.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                if (system != null) {
                    String country = policy.sanitize(httpServletRequest.getParameter("country"));
                    String environment = policy.sanitize(httpServletRequest.getParameter("environment"));
                    String database = policy.sanitize(httpServletRequest.getParameter("database"));

                    ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService = appContext.getBean(CountryEnvironmentDatabaseService.class);

                    CountryEnvironmentDatabase countryEnvironmentDatabase;
                    countryEnvironmentDatabase = countryEnvironmentDatabaseService.findCountryEnvironmentDatabaseByKey(system, country, environment, database);
                    String connectionName = countryEnvironmentDatabase.getConnectionPoolName();

                    if (type.equals("executeSqlFromLib")) {
                        ISqlLibraryService sqlLibraryService = appContext.getBean(SqlLibraryService.class);
                        SqlLibrary sl = sqlLibraryService.findSqlLibraryByKey(policy.sanitize(property));
                        property = sl.getScript();
                    
                    if (!(StringUtil.isNullOrEmpty(connectionName)) && !(StringUtil.isNullOrEmpty(policy.sanitize(property)))) {
                        IConnectionPoolDAO connectionPoolDAO = appContext.getBean(ConnectionPoolDAO.class);
                        result = connectionPoolDAO.queryDatabase(connectionName, policy.sanitize(property), 1).get(0);
                        description = sl.getDescription();
                        }
                }}
            }


        } catch (CerberusException ex) {
            Logger.getLogger(CalculatePropertyForTestCase.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            result = ex.getMessageError().getDescription();
            description = ex.getMessageError().getDescription();
        } catch (CerberusEventException ex) {
            Logger.getLogger(CalculatePropertyForTestCase.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            result = ex.getMessageError().getDescription();
            description = ex.getMessageError().getDescription();
        }

        if (result != null) {
            try {
                JSONArray array = new JSONArray();
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("resultList", result);
                jsonObject.put("description", description);

                httpServletResponse.setContentType("application/json");
                httpServletResponse.getWriter().print(jsonObject.toString());
            } catch (JSONException exception) {
                MyLogger.log(CalculatePropertyForTestCase.class.getName(), Level.WARN, exception.toString());
            }
        }
    }
}
