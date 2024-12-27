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
package org.cerberus.core.servlet.crud.test.testcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.version.Infos;
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
@WebServlet(name = "ExportTestCase", urlPatterns = {"/ExportTestCase"})
public class ExportTestCase extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ExportTestCase.class);
    private ITestCaseService testcaseService;
    private IApplicationService applicationService;

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
            testcaseService = appContext.getBean(ITestCaseService.class);
            applicationService = appContext.getBean(IApplicationService.class);

            PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
            String test = policy.sanitize(httpServletRequest.getParameter("test"));
            String testcase = policy.sanitize(httpServletRequest.getParameter("testcase"));

            // Target JSON structure for export.
            JSONObject export = new JSONObject();

            // Header export.
            export.put("version", Infos.getInstance().getProjectVersion());
            export.put("user", httpServletRequest.getUserPrincipal());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            export.put("date", formatter.format(new Date()));

            // Contain the main ** TestCase **
            TestCase tcInfo = testcaseService.findTestCaseByKeyWithDependency(test, testcase);
            ObjectMapper mapper = new ObjectMapper();
            JSONObject tcInfoJSON = new JSONObject(mapper.writeValueAsString(tcInfo));
            tcInfoJSON.remove("bugs");
            tcInfoJSON.put("bugs", tcInfo.getBugs());
            tcInfoJSON.remove("conditionOptions");
            tcInfoJSON.put("conditionOptions", tcInfo.getConditionOptions());
            
            JSONArray tcJA = new JSONArray();
            tcJA.put(tcInfoJSON);
            export.put("testcases", tcJA);

            // Contain the ** application ** of the testcase
            Application appInfo = applicationService.convert(applicationService.readByKey(tcInfo.getApplication()));
            // Java object to JSON string
            JSONObject app = new JSONObject(mapper.writeValueAsString(appInfo));
            export.put("application", app);

            // ** TODO : SYSTEMS, COUNTRIES ** invariant.
            export.put("invariants", new JSONArray());

            // ** TODO : Application objects **.
            export.put("applicationsObjects", new JSONArray());

            // ** TODO : Datalib **.
            export.put("datalibs", new JSONArray());

            // ** TODO : Services **.
            export.put("services", new JSONArray());

            // ** TODO : Libraries TestCases **.
            export.put("libraryTestcases", new JSONArray());

            httpServletResponse.setContentType("application/json");
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + getFilename(test, testcase) + ".json\"");
            // Nice formating the json result by putting indent 4 parameter.
            httpServletResponse.getOutputStream().print(export.toString(1));

        } catch (CerberusException | JSONException ex) {
            LOG.warn(ex);
        }
    }

    private String getFilename(String test, String testcase) {
        return test.replaceAll("\'", "") + "-" + testcase.replace("\'", "");
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
