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
package org.cerberus.servlet.zzpublic;

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
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IMyVersionService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.database.IDatabaseVersioningService;
import org.cerberus.engine.entity.ExecutionUUID;
import org.cerberus.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.engine.scheduler.SchedulerInit;
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
@WebServlet(name = "manageV001", urlPatterns = {"/manageV001"})
public class ManageV001 extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ManageV001.class);

    private static final String SERVLETNAME = "ManageV001";

    private IExecutionThreadPoolService executionThreadPoolService;
    private IParameterService parameterService;
    private ITestCaseExecutionQueueService tceiqService;
    private SchedulerInit cerberusScheduler;
    private ILogEventService logEventService;

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
        String resultS = "";

        try {

            ExecutionUUID euuid = appContext.getBean(ExecutionUUID.class);

            executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);
            parameterService = appContext.getBean(IParameterService.class);
            cerberusScheduler = appContext.getBean(SchedulerInit.class);
            logEventService = appContext.getBean(ILogEventService.class);

            String token = parameterService.getParameterStringByKey("cerberus_manage_token", "", UUID.randomUUID().toString());
            String message = "";
            String returnCode = "OK";

            if (token.equals(request.getParameter("token"))) {

                int maxIteration = parameterService.getParameterIntegerByKey("cerberus_manage_timeout", "", 300);
                int cntIteration = 0;
                int instancePendingExecutionNb = euuid.size();
                int globalPendingExecutionNb = getNbPendingExecutions(appContext);
                int globalQueueingExecutionNb = getNbQueueingExecutions(appContext);
                boolean globalActive = parameterService.getParameterBooleanByKey("cerberus_queueexecution_enable", "", true);

                if (request.getParameter("action") != null && request.getParameter("action").equals("cleanmemory")) {
                    if (request.getParameter("scope") != null && request.getParameter("scope").equals("instance")) {
                        logEventService.createForPrivateCalls(SERVLETNAME, "CLEANMEMORY", "Cerberus Instance requested to Garbage collection.", request);
                        System.gc();
                        message = "Memory Cleaned.";
                        returnCode = "OK";
                    }
                }

                if (request.getParameter("action") != null && request.getParameter("action").equals("stop")) {
                    if (request.getParameter("scope") != null && request.getParameter("scope").equals("instance")) {
                        logEventService.createForPrivateCalls(SERVLETNAME, "STOP", "Cerberus Instance requested to stop.", request);
                        /**
                         * We deactivate the instance to process new execution.
                         */
                        executionThreadPoolService.setInstanceActive(false);
                        /**
                         * We clean all scheduler entries.
                         */
                        cerberusScheduler.closeScheduler();
                        /**
                         * Now that we stopped the submissions of new executions
                         * and also stopped the scheduler, no more executions
                         * should be triggered on that instance. We now wait a
                         * bit until we check the pending executions. Some
                         * executions could be submitted but not yet visible
                         * from the instance yet.
                         */
                        Thread.sleep(10000);
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
                        message = "Instance Stopped.";
                        returnCode = "OK";

                    } else if (request.getParameter("scope") != null && request.getParameter("scope").equals("global")) {
                        logEventService.createForPrivateCalls(SERVLETNAME, "STOP", "Cerberus (global system) requested to stop.", request);
                        /**
                         * We deactivate globally the queue processing accross
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
                        message = "Cerberus Stopped.";
                        returnCode = "OK";

                    } else {
                        message += "Parameter 'scope' not defined.";
                        returnCode = "KO";
                    }
                }

                if (request.getParameter("action") != null && request.getParameter("action").equals("start")) {
                    if (request.getParameter("scope") != null && request.getParameter("scope").equals("instance")) {
                        logEventService.createForPrivateCalls(SERVLETNAME, "START", "Instance requested to start.", request);
                        /**
                         * We activate the instance to process queue and start
                         * new executions.
                         */
                        executionThreadPoolService.setInstanceActive(true);
                        /**
                         * We reactivate and force reload all scheduler entries.
                         */
                        cerberusScheduler.setInstanceSchedulerVersion("INIT");
                        cerberusScheduler.init();
                        /**
                         * We run the execution pool.
                         */
                        try {
                            executionThreadPoolService.executeNextInQueueAsynchroneously(false);
                        } catch (CerberusException ex) {
                            LOG.error("Exception triggering the ThreadPool job.", ex);
                        }
                        message = "Instance Started.";
                        returnCode = "OK";

                    } else if (request.getParameter("scope") != null && request.getParameter("scope").equals("global")) {
                        logEventService.createForPrivateCalls(SERVLETNAME, "START", "Cerberus (global system)  requested to start.", request);
                        /**
                         * We activate the parameter to process queue (that will
                         * start new executions).
                         */
                        parameterService.setParameter("cerberus_queueexecution_enable", "", "Y");

                        message = "Cerberus Started.";
                        returnCode = "OK";

                    } else {
                        message += "Scope parameter 'scope' not defined.";
                        returnCode = "KO";
                    }

                }

                JSONObject instance = new JSONObject();
                JSONObject global = new JSONObject();

                instance.put("active", executionThreadPoolService.isInstanceActive());
                instance.put("runningExecutions", instancePendingExecutionNb);
                instance.put("readyToStop", (instancePendingExecutionNb <= 0));
                JSONObject memory = new JSONObject();
                Runtime instance1 = Runtime.getRuntime();
                int mb = 1024 * 1024;
                long usedMem = (instance1.totalMemory() - instance1.freeMemory()) / mb;
                long totalMem = instance1.maxMemory() / mb;
                memory.put("javaFreeMemory", instance1.freeMemory() / mb);
                memory.put("javaTotalMemory", instance1.totalMemory() / mb);
                memory.put("javaUsedMemory", usedMem);
                memory.put("javaMaxMemory", totalMem);
                memory.put("perUsed", usedMem * 100 / totalMem);

                data.put("memory", memory);

                data.put("instance", instance);

                global.put("active", globalActive);
                global.put("runningExecutions", globalPendingExecutionNb);
                global.put("readyToStop", (globalPendingExecutionNb <= 0));
                global.put("queuedExecutions", globalQueueingExecutionNb);

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
                returnCode = "KO";
            }

            data.put("message", message);
            data.put("returnCode", returnCode);
            resultS = data.toString(1);

        } catch (JSONException | InterruptedException ex) {
            LOG.error(ex);
        }
        response.getWriter().print(resultS);
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

    private int getNbQueueingExecutions(ApplicationContext appContext) {
        try {

            tceiqService = appContext.getBean(ITestCaseExecutionQueueService.class);
            // Getting all executions already running in the queue.
            AnswerList<TestCaseExecutionQueueToTreat> answer = tceiqService.readQueueToTreat();
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
