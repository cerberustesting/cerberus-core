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
package org.cerberus.servlet.administration;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.service.IMyVersionService;
import org.cerberus.database.IDatabaseVersioningService;
import org.cerberus.engine.entity.ExecutionUUID;
import org.cerberus.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.version.Infos;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "StopInstance", urlPatterns = {"/StopInstance"})
public class StopInstance extends HttpServlet {
    
    private static final Logger LOG = LogManager.getLogger(StopInstance.class);
    
    private IExecutionThreadPoolService executionThreadPoolService;
    
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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Infos infos = new Infos();
        JSONObject data = new JSONObject();
        
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        
        try {
            
            ExecutionUUID euuid = appContext.getBean(ExecutionUUID.class);
            
            executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);

            /**
             * We start to desactivte the instance to process queue and start
             * new executions.
             */
            executionThreadPoolService.setInstanceActive(false);

            /**
             * We loop every second until maxIteration session in order to wait
             * until no more executions are running on that instance.
             */
            int maxIteration = 300;
            int cntIteration = 0;
            int pendingExecutionNb = euuid.size();
            while (pendingExecutionNb > 0 && cntIteration <= maxIteration) {
                cntIteration++;
                Thread.sleep(1000);
                pendingExecutionNb = euuid.size();
                LOG.info("Stopping instance : Check " + cntIteration + "/" + maxIteration + " on pending executions on that instance. Still running : " + pendingExecutionNb);
            }
            
            data.put("executionThreadPool", executionThreadPoolService.isInstanceActive());
            data.put("runningExecutions", pendingExecutionNb);
            data.put("waitedIterations", cntIteration);
            data.put("readyToStop", (pendingExecutionNb <= 0));
            
        } catch (JSONException | InterruptedException ex) {
            LOG.error(ex);
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
