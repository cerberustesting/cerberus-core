/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.servlet.information;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.database.dao.ICerberusInformationDAO;
import org.cerberus.engine.entity.ExecutionUUID;
import org.cerberus.crud.entity.SessionCounter;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IMyVersionService;
import org.cerberus.database.IDatabaseVersioningService;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.version.Infos;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "ReadCerberusDetailInformation", urlPatterns = {"/ReadCerberusDetailInformation"})
public class ReadCerberusDetailInformation extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadCerberusDetailInformation.class);
    
    private ICerberusInformationDAO cerberusDatabaseInformation;
    private IDatabaseVersioningService databaseVersionService;
    private IMyVersionService myVersionService;

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ExecutionUUID euuid = appContext.getBean(ExecutionUUID.class);
        SessionCounter sc = appContext.getBean(SessionCounter.class);
        Infos infos = new Infos();

        try {
            jsonResponse.put("simultaneous_execution", euuid.size());
            JSONArray executionArray = new JSONArray();
            for (Object ex : euuid.getExecutionUUIDList().values()) {
                TestCaseExecution execution = (TestCaseExecution) ex;
                JSONObject object = new JSONObject();
                object.put("id", execution.getId());
                object.put("test", execution.getTest());
                object.put("testcase", execution.getTestCase());
                object.put("system", execution.getApplicationObj().getSystem());
                object.put("application", execution.getApplication());
                object.put("environment", execution.getEnvironmentData());
                object.put("country", execution.getCountry());
                object.put("robotIP", execution.getSeleniumIP());
                object.put("tag", execution.getTag());
                object.put("start", new Timestamp(execution.getStart()));
                executionArray.put(object);
            }
            jsonResponse.put("simultaneous_execution_list", executionArray);
            jsonResponse.put("simultaneous_session", sc.getTotalActiveSession());
            jsonResponse.put("active_users", sc.getActiveUsers());

            cerberusDatabaseInformation = appContext.getBean(ICerberusInformationDAO.class);

            AnswerItem<HashMap<String, String>> ans = cerberusDatabaseInformation.getDatabaseInformation();
            HashMap<String, String> cerberusInformation = (HashMap<String, String>) ans.getItem();

            // Database Informations.
            jsonResponse.put("DatabaseProductName", cerberusInformation.get("DatabaseProductName"));
            jsonResponse.put("DatabaseProductVersion", cerberusInformation.get("DatabaseProductVersion"));
            jsonResponse.put("DatabaseMajorVersion", cerberusInformation.get("DatabaseMajorVersion"));
            jsonResponse.put("DatabaseMinorVersion", cerberusInformation.get("DatabaseMinorVersion"));

            jsonResponse.put("DriverName", cerberusInformation.get("DriverName"));
            jsonResponse.put("DriverVersion", cerberusInformation.get("DriverVersion"));
            jsonResponse.put("DriverMajorVersion", cerberusInformation.get("DriverMajorVersion"));
            jsonResponse.put("DriverMinorVersion", cerberusInformation.get("DriverMinorVersion"));

            jsonResponse.put("JDBCMajorVersion", cerberusInformation.get("JDBCMajorVersion"));
            jsonResponse.put("JDBCMinorVersion", cerberusInformation.get("JDBCMinorVersion"));

            // Cerberus Informations.
            jsonResponse.put("projectName", infos.getProjectName());
            jsonResponse.put("projectVersion", infos.getProjectVersion());
            jsonResponse.put("environment", System.getProperty("org.cerberus.environment"));

            databaseVersionService = appContext.getBean(IDatabaseVersioningService.class);
            jsonResponse.put("databaseCerberusTargetVersion", databaseVersionService.getSQLScript().size());

            myVersionService = appContext.getBean(IMyVersionService.class);
            if (myVersionService.findMyVersionByKey("database") != null) {
                jsonResponse.put("databaseCerberusCurrentVersion", myVersionService.findMyVersionByKey("database").getValue());
            } else {
                jsonResponse.put("databaseCerberusCurrentVersion", "0");
            }

            // JAVA Informations.
            jsonResponse.put("javaVersion", System.getProperty("java.version"));
            Runtime instance = Runtime.getRuntime();
            int mb = 1024 * 1024;
            jsonResponse.put("javaFreeMemory", instance.freeMemory() / mb);
            jsonResponse.put("javaTotalMemory", instance.totalMemory() / mb);
            jsonResponse.put("javaUsedMemory", (instance.totalMemory() - instance.freeMemory()) / mb);
            jsonResponse.put("javaMaxMemory", instance.maxMemory() / mb);

            String str1 = getServletContext().getServerInfo();
            jsonResponse.put("applicationServerInfo", str1);

        } catch (JSONException ex) {
            LOG.warn(ex);
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
