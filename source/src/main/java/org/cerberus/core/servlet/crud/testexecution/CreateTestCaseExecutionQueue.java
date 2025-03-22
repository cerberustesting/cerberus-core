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
package org.cerberus.core.servlet.crud.testexecution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionQueue;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.exception.FactoryCreationException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "CreateTestCaseExecutionQueue", urlPatterns = {"/CreateTestCaseExecutionQueue"})
public class CreateTestCaseExecutionQueue extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(CreateTestCaseExecutionQueue.class);
    private ITestCaseExecutionQueueDepService tceQueueDep;
    private IExecutionThreadPoolService executionThreadPoolService;
    private ITagService tagService;
    private ITestCaseExecutionQueueService executionQueueService;
    private IFactoryTestCaseExecutionQueue executionQueueFactory;
    private ILogEventService logEventService;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.core.exception.CerberusException
     * @throws org.json.JSONException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        JSONObject executionQueue = new JSONObject();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Answer ans = new Answer();
        AnswerItem ansItem = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        String actionState = policy.sanitize(request.getParameter("actionState"));
        String actionSave = policy.sanitize(request.getParameter("actionSave"));
        String environment = policy.sanitize(request.getParameter("environment"));
        String country = policy.sanitize(request.getParameter("country"));
        String manualEnvData = policy.sanitize(request.getParameter("manualEnvData"));
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String test = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("test"), "", charset);
        String testcase = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testCase"), "", charset);
        int manualURL = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("manualURL"), 0, charset);
        String manualHost = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("manualHost"), "", charset);
        String manualContextRoot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("manualContextRoot"), "", charset);
        String manualLoginRelativeURL = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("manualLoginRelativeURL"), "", charset);
        String tag = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("tag"), "", charset);
        String robot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("robot"), "", charset);
        String robotIP = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("robotIP"), "", charset);
        String robotPort = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("robotPort"), "", charset);
        String browser = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("browser"), "", charset);
        String browserVersion = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("browserVersion"), "", charset);
        String platform = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("platform"), "", charset);
        String screenSize = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("screenSize"), "", charset);

        int verbose = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("verbose"), 1, charset);
        int screenshot = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("screenshot"), 0, charset);
        int video = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("video"), 0, charset);
        int pageSource = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("pageSource"), 0, charset);
        int robotLog = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("robotLog"), 0, charset);
        int consoleLog = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("consoleLog"), 0, charset);
        String timeout = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("timeout"), "", charset);
        int retries = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("retries"), 0, charset);
        String manualExecution = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("manualExecution"), "", charset);
        String debugFlag = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("debugFlag"), "N", charset);

        // Parameter that we cannot secure as we need the html --> We DECODE them
        String[] myIds = request.getParameterValues("id");
        if (myIds == null) {
            myIds = new String[1];
            myIds[0] = "0";
        }
        long id = 0;
        Integer priority = TestCaseExecutionQueue.PRIORITY_DEFAULT;
        boolean prio_error = false;
        try {
            if (request.getParameter("priority") != null && !request.getParameter("priority").isEmpty()) {
                priority = Integer.valueOf(policy.sanitize(request.getParameter("priority")));
            }
        } catch (Exception ex) {
            prio_error = true;
        }

        boolean id_error = false;

        executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);

        // Create Tag when exist.
        if (!StringUtil.isEmptyOrNull(tag)) {
            // We create or update it.
            tagService = appContext.getBean(ITagService.class);
            List<String> envList = new ArrayList<>();
            envList.add(environment);
            List<String> countryList = new ArrayList<>();
            countryList.add(country);

            tagService.createAuto(tag, "", request.getRemoteUser(), new JSONArray(envList), new JSONArray(countryList));
        }

        // If action is toQUEUEDwithDEP, that means that we also need to trigger the dependency executions.
        // So we enrish the input list with dependencies.
        boolean withNewDep = false;
        List<Long> idList = new ArrayList<>();
        for (String myId : myIds) {
            id_error = false;
            id = 0;
            try {
                id = Long.valueOf(myId);
            } catch (NumberFormatException ex) {
                id_error = true;
            }
            idList.add(id);
        }
        if (actionState.equals("toQUEUEDwithDep")) {
            withNewDep = true;
            tceQueueDep = appContext.getBean(ITestCaseExecutionQueueDepService.class);
            idList = tceQueueDep.enrichWithDependencies(idList);
        }

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);
        List<TestCaseExecutionQueue> insertedList = new ArrayList<>();

        // Feed all queue entries already existing in tag contaxt.
        executionQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);
        Map<String, TestCaseExecutionQueue> queueAlreadyInsertedInTag = new HashMap<>();
        if (StringUtil.isNotEmptyOrNull(tag)) {
            LOG.debug("We don't have the list of all already inserted entries. Let's get it from tag value : " + tag);
            List<TestCaseExecutionQueue> queueFromTag = executionQueueService.convert(executionQueueService.readMaxIdByTag(tag));
            for (TestCaseExecutionQueue tceQueue : queueFromTag) {
                queueAlreadyInsertedInTag.put(executionQueueService.getUniqKey(tceQueue.getTest(), tceQueue.getTestCase(), tceQueue.getCountry(), tceQueue.getEnvironment()), tceQueue);
            }
        }

        for (Long myId : idList) {

            id = myId;
            /**
             * Checking all constrains before calling the services.
             */
            if (id_error) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Execution Queue")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Could not manage to convert id to an integer value."));
                ans.setResultMessage(msg);
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
            } else if (prio_error || priority > 2147483647 || priority < -2147483648) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Execution Queue")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Could not manage to convert priority to an integer value."));
                ans.setResultMessage(msg);
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
            } else {
                try {
                    /**
                     * All data seems cleans so we can call the services.
                     */
                    executionQueueFactory = appContext.getBean(IFactoryTestCaseExecutionQueue.class);

                    if (actionSave.equals("save")) {
                        /**
                         * The service was able to perform the query and confirm
                         * the object exist, then we can update it.
                         */
                        TestCaseExecutionQueue executionQueueData;
                        if (id == 0) {
                            // If id is not defined, we build the execution queue from all request datas.
                            executionQueueData = executionQueueFactory.create("", test, testcase, country, environment, robot, robot, robotIP, robotPort, browser, browserVersion,
                                    platform, screenSize, manualURL, manualHost, manualContextRoot, manualLoginRelativeURL, manualEnvData, tag, screenshot, video, verbose, timeout,
                                    pageSource, robotLog, consoleLog, 0, retries, manualExecution, priority, request.getRemoteUser(), null, null, null);
                            executionQueueData.setDebugFlag(debugFlag);
                            ansItem.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
                        } else {
                            // If id is defined, we get the execution queue from database.
                            executionQueueData = executionQueueService.convert(executionQueueService.readByKey(id, false));
                            if (executionQueueData != null) {
                                executionQueueData.setState(TestCaseExecutionQueue.State.QUEUED);
                                executionQueueData.setComment("");
                                executionQueueData.setDebugFlag("N");
                                executionQueueData.setPriority(TestCaseExecutionQueue.PRIORITY_DEFAULT);
                                executionQueueData.setUsrCreated(request.getRemoteUser());
                                ansItem.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
                            } else {
                                ansItem.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED)
                                        .resolveDescription("ITEM", "Execution Queue")
                                        .resolveDescription("OPERATION", "Read")
                                        .resolveDescription("REASON", "Could not find previous queue entry " + id + ". Maybe it was purged."));
                            }
                        }

                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ansItem);
                        if (ansItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {

                            ansItem = executionQueueService.create(executionQueueData, withNewDep, id, TestCaseExecutionQueue.State.QUEUED, queueAlreadyInsertedInTag);
                            TestCaseExecutionQueue addedExecution = (TestCaseExecutionQueue) ansItem.getItem();
                            insertedList.add(addedExecution);
                        }

                        if (myIds.length <= 1) {
                            if (ansItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                                /**
                                 * Update was successful. Adding Log entry.
                                 */
                                logEventService = appContext.getBean(LogEventService.class);
                                logEventService.createForPrivateCalls("/CreateTestCaseExecutionQueue", "CREATE", LogEvent.STATUS_INFO, "Created ExecutionQueue : ['" + id + "']", request);
                            }
                        }

                    }

                } catch (FactoryCreationException | CerberusException | BeansException ex) {
                    LOG.error(ex, ex);
                }

            }
        }

        // Update is done, we now check what action needs to be performed.
        if (actionState.equals("toQUEUED") || actionState.equals("toQUEUEDwithDep")) {
            executionThreadPoolService.executeNextInQueueAsynchroneously(false);
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());
        if (insertedList.isEmpty()) {
            jsonResponse.put("addedEntries", 0);
        } else {
            JSONArray executionList = new JSONArray();

            for (TestCaseExecutionQueue testCaseExecutionQueue : insertedList) {
                JSONObject myExecution = new JSONObject();
                myExecution.append("id", testCaseExecutionQueue.getId());
                executionList.put(myExecution);
            }
            jsonResponse.put("testCaseExecutionQueueList", executionList);
            jsonResponse.put("addedEntries", insertedList.size());
        }

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
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

        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
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

        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
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
