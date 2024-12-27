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
package org.cerberus.core.servlet.information;

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
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.IMyVersionService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.database.IDatabaseVersioningService;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.version.Infos;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "ReadCerberusInformation", urlPatterns = {"/ReadCerberusInformation"})
public class ReadCerberusInformation extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadCerberusInformation.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";

    private IDatabaseVersioningService databaseVersionService;
    private IMyVersionService myVersionService;
    private IExecutionThreadPoolService executionThreadPoolService;
    private IParameterService parameterService;

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Infos infos = new Infos();
        JSONObject data = new JSONObject();

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        try {
            data.put("projectName", infos.getProjectName());
            data.put("projectVersion", infos.getProjectVersion());
            data.put("projectBuild", infos.getProjectBuildId());
            data.put("environment", System.getProperty("org.cerberus.environment"));
            parameterService = appContext.getBean(IParameterService.class);
            data.put("isGlobalSplashPageActive", parameterService.getParameterBooleanByKey(Parameter.VALUE_cerberus_splashpage_enable, "", false));
            executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);
            data.put("isInstanceSplashPageActive", executionThreadPoolService.isSplashPageActive());
            data.put("messageInformation", parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_messageinfo_text, "", ""));
            data.put("isMessageInformationEnabled", parameterService.getParameterBooleanByKey(Parameter.VALUE_cerberus_messageinfo_enable, "", false));
            databaseVersionService = appContext.getBean(IDatabaseVersioningService.class);
            data.put("databaseCerberusTargetVersion", databaseVersionService.getSqlVersion());
            Date now = new Date();
            data.put("serverDate", new SimpleDateFormat(DATE_FORMAT).format(now));

            myVersionService = appContext.getBean(IMyVersionService.class);
            if (myVersionService.findMyVersionByKey("database") != null) {
                data.put("databaseCerberusCurrentVersion", myVersionService.findMyVersionByKey("database").getValue());
            } else {
                data.put("databaseCerberusCurrentVersion", "0");
            }
        } catch (Exception ex) {
            LOG.error(ex, ex);
        }
        response.getWriter().print(data.toString());
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
