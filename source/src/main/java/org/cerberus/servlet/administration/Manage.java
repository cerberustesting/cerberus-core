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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.service.IMyVersionService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.database.IDatabaseVersioningService;
import org.cerberus.engine.entity.ExecutionUUID;
import org.cerberus.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "manage", urlPatterns = {"/manage"})
public class Manage extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(Manage.class);

    private IExecutionThreadPoolService executionThreadPoolService;
    private IParameterService parameterService;
    private ITestCaseExecutionQueueService tceiqService;

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
        JSONObject data = new JSONObject();

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        try {

            ExecutionUUID euuid = appContext.getBean(ExecutionUUID.class);

            executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);
            parameterService = appContext.getBean(IParameterService.class);

            String token = parameterService.getParameterStringByKey("cerberus_manage_token", "", UUID.randomUUID().toString());
            String message = "";

            if (token.equals(request.getParameter("token"))) {

                int maxIteration = parameterService.getParameterIntegerByKey("cerberus_manage_timeout", "", 300);
                int cntIteration = 0;
                int instancePendingExecutionNb = euuid.size();
                int globalPendingExecutionNb = getNbPendingExecutions(appContext);
                boolean globalActive = parameterService.getParameterBooleanByKey("cerberus_queueexecution_enable", "", true);

                if (request.getParameter("action") != null && request.getParameter("action").equals("stop")) {
                    if (request.getParameter("scope") != null && request.getParameter("scope").equals("instance")) {
                        /**
                         * We desactivate the instance to process new execution.
                         */
                        executionThreadPoolService.setInstanceActive(false);
                        /**
                         * We loop every second until maxIteration session in
                         * order to wait until no more executions are running on
                         * that instance.
                         */
                        while (instancePendingExecutionNb > 0 && cntIteration <= maxIteration) {
                            cntIteration++;
                            Thread.sleep(1000);
                            instancePendingExecutionNb = euuid.size();
                            LOG.info("Stopping instance : Check " + cntIteration + "/" + maxIteration + " on pending executions on that instance. Still running : " + instancePendingExecutionNb);
                        }
                        data.put("waitedIterations", cntIteration);

                    } else if (request.getParameter("scope") != null && request.getParameter("scope").equals("global")) {
                        /**
                         * We desactivate globally the queue processing accross
                         * all instances.
                         */
                        parameterService.setParameter("cerberus_queueexecution_enable", "", "N");
                        /**
                         * We loop every second until maxIteration session in
                         * order to wait until no more executions are running.
                         */
                        while (globalPendingExecutionNb > 0 && cntIteration <= maxIteration) {
                            cntIteration++;
                            Thread.sleep(1000);
                            // TODO
                            globalPendingExecutionNb = getNbPendingExecutions(appContext);
                            LOG.info("Stopping global : Check " + cntIteration + "/" + maxIteration + " on global pending executions. Still running : " + globalPendingExecutionNb);
                        }
                        data.put("waitedIterations", cntIteration);

                    } else {
                        message += "Scope parameter 'scope' not defined.";
                    }
                }
                if (request.getParameter("action") != null && request.getParameter("action").equals("start")) {
                    if (request.getParameter("scope") != null && request.getParameter("scope").equals("instance")) {
                        /**
                         * We activate the instance to process queue and start
                         * new executions.
                         */
                        executionThreadPoolService.setInstanceActive(true);
                        try {
                            // Run the Execution pool Job.
                            executionThreadPoolService.executeNextInQueueAsynchroneously(false);
                        } catch (CerberusException ex) {
                            LOG.error("Exception triggering the ThreadPool job.", ex);
                        }
                    } else if (request.getParameter("scope") != null && request.getParameter("scope").equals("global")) {
                        /**
                         * We activate the parameter to process queue (that will
                         * start new executions).
                         */
                        parameterService.setParameter("cerberus_queueexecution_enable", "", "Y");
                    } else {
                        message += "Scope parameter 'scope' not defined.";
                    }

                }

                JSONObject instance = new JSONObject();
                JSONObject global = new JSONObject();

                instance.put("active", executionThreadPoolService.isInstanceActive());
                instance.put("runningExecutions", instancePendingExecutionNb);
                instance.put("readyToStop", (instancePendingExecutionNb <= 0));
                data.put("instance", instance);

                global.put("active", globalActive);
                global.put("runningExecutions", globalPendingExecutionNb);
                global.put("readyToStop", (globalPendingExecutionNb <= 0));
                data.put("global", global);

                JSONObject fsSize = new JSONObject();
                fsSize.put("cerberus_exeautomedia_path", getFSSize(parameterService.getParameterStringByKey("cerberus_exeautomedia_path", "", "/")));
                fsSize.put("cerberus_applicationobject_path", getFSSize(parameterService.getParameterStringByKey("cerberus_applicationobject_path", "", "/")));
                fsSize.put("cerberus_exemanualmedia_path", getFSSize(parameterService.getParameterStringByKey("cerberus_exemanualmedia_path", "", "/")));
                fsSize.put("cerberus_ftpfile_path", getFSSize(parameterService.getParameterStringByKey("cerberus_ftpfile_path", "", "/")));
                fsSize.put("cerberus_testdatalibcsv_path", getFSSize(parameterService.getParameterStringByKey("cerberus_testdatalibcsv_path", "", "/")));
                data.put("fileSystemSize", fsSize);

            } else {
                message = "Invalid Token";
            }

            data.put("message", message);

        } catch (JSONException | InterruptedException ex) {
            LOG.error(ex);
        }
        response.getWriter().print(data.toString());
    }

    private int getNbPendingExecutions(ApplicationContext appContext) {
        try {

            tceiqService = appContext.getBean(ITestCaseExecutionQueueService.class);
            // Getting all executions already running in the queue.
            AnswerList<TestCaseExecutionQueueToTreat> answer = tceiqService.readQueueRunning();
            List<TestCaseExecutionQueueToTreat> executionsRunning = answer.getDataList();
            return executionsRunning.size();

        } catch (CerberusException ex) {
            LOG.error(ex);
        }
        return 0;
    }

    private JSONObject getFSSize(String path) {
        JSONObject exeFS = new JSONObject();
        LOG.debug(path);
        try {

            exeFS.put("path", path);
            if (new File(path).exists()) {

                long freeSpace = new File(path).getFreeSpace();
                exeFS.put("freeSpace", freeSpace);

                long totalSpace = new File(path).getTotalSpace();
                exeFS.put("totalSpace", totalSpace);
                if (totalSpace > 0) {
                    exeFS.put("perUsed", (totalSpace - freeSpace) * 100 / totalSpace);
                }
            } else {
                exeFS.put("message", "Folder does not Exist.");

            }
            return exeFS;

        } catch (JSONException ex) {
            LOG.error("Exception getting FS space for : " + path, ex);
        } catch (Exception ex) {
            LOG.error("Exception getting FS space for : " + path, ex);
        }
        return exeFS;
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
