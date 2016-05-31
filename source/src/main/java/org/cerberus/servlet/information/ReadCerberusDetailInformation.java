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
package org.cerberus.servlet.information;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.dao.ICerberusInformationDAO;
import org.cerberus.engine.entity.ExecutionThreadPool;
import org.cerberus.engine.entity.ExecutionUUID;
import org.cerberus.crud.entity.SessionCounter;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IBuildRevisionBatchService;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.version.Infos;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "ReadCerberusDetailInformation", urlPatterns = {"/ReadCerberusDetailInformation"})
public class ReadCerberusDetailInformation extends HttpServlet {

    private ICerberusInformationDAO cerberusDatabaseInformation;

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
        ExecutionThreadPool etp = appContext.getBean(ExecutionThreadPool.class);
        ExecutionUUID euuid = appContext.getBean(ExecutionUUID.class);
        SessionCounter sc = appContext.getBean(SessionCounter.class);
        Infos infos = new Infos();

        try {
            jsonResponse.put("size_queue", etp.getSize());
            jsonResponse.put("queue_in_execution", etp.getInExecution());
            jsonResponse.put("simultaneous_execution", euuid.size());
            JSONArray executionArray = new JSONArray();
            for (Object ex : euuid.getExecutionUUIDList().values()) {
                TestCaseExecution execution = (TestCaseExecution) ex;
                JSONObject object = new JSONObject();
                object.put("id", execution.getId());
                object.put("test", execution.getTest());
                object.put("testcase", execution.getTestCase());
                executionArray.put(object);
            }
            jsonResponse.put("simultaneous_execution_list", executionArray);
            jsonResponse.put("simultaneous_session", sc.getTotalActiveSession());
            jsonResponse.put("active_users", sc.getActiveUsers());
            jsonResponse.put("number_of_thread", etp.getNumberOfThread());

            cerberusDatabaseInformation = appContext.getBean(ICerberusInformationDAO.class);
            
            AnswerItem ans = cerberusDatabaseInformation.getDatabaseInformation();
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
            
            // JAVA Informations.
            jsonResponse.put("javaVersion", System.getProperty("java.version"));
            

        } catch (JSONException ex) {
            Logger.getLogger(ReadCerberusDetailInformation.class.getName()).log(Level.SEVERE, null, ex);
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
