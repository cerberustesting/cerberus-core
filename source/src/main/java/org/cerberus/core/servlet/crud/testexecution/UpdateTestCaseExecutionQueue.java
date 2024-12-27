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
import java.util.List;
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
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
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
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "UpdateTestCaseExecutionQueue", urlPatterns = {"/UpdateTestCaseExecutionQueue"})
public class UpdateTestCaseExecutionQueue extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateTestCaseExecutionQueue.class);

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Answer ans = new Answer();
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
        String test = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("test"), null, charset);
        String testcase = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testCase"), null, charset);
        int manualURL = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("manualURL"), 0, charset);
        String manualHost = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("manualHost"), null, charset);
        String manualContextRoot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("manualContextRoot"), "", charset);
        String manualLoginRelativeURL = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("manualLoginRelativeURL"), "", charset);
        String tag = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("tag"), null, charset);
        String robot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("robot"), null, charset);
        String robotIP = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("robotIP"), null, charset);
        String robotPort = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("robotPort"), null, charset);
        String browser = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("browser"), null, charset);
        String browserVersion = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("browserVersion"), null, charset);
        String platform = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("platform"), null, charset);
        String screenSize = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("screenSize"), null, charset);

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
        Integer priority = TestCaseExecutionQueue.PRIORITY_DEFAULT;
        boolean prio_error = false;
        try {
            if (request.getParameter("priority") != null && !request.getParameter("priority").isEmpty()) {
                priority = Integer.valueOf(policy.sanitize(request.getParameter("priority")));
            }
        } catch (Exception ex) {
            prio_error = true;
        }

        // Parameter that we cannot secure as we need the html --> We DECODE them
        String[] myIds = request.getParameterValues("id");
        long id = 0;

        // Create Tag when exist.
        if (!StringUtil.isEmptyOrNull(tag)) {
            // We create or update it.
            ITagService tagService = appContext.getBean(ITagService.class);
            List<String> envList = new ArrayList<>();
            envList.add(environment);
            List<String> countryList = new ArrayList<>();
            countryList.add(country);
            tagService.createAuto(tag, "", request.getRemoteUser(), new JSONArray(envList), new JSONArray(countryList));
        }

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        boolean id_error = false;
        for (String myId : myIds) {

            id_error = false;
            try {
                id = Long.valueOf(myId);
            } catch (NumberFormatException ex) {
                id_error = true;
            }

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
                /**
                 * All data seems cleans so we can call the services.
                 */
                ITestCaseExecutionQueueService executionQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);
                IExecutionThreadPoolService executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);

                AnswerItem resp = executionQueueService.readByKey(id, false);
                if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                    /**
                     * Object could not be found. We stop here and report the
                     * error.
                     */
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, resp);

                } else {

                    TestCaseExecutionQueue executionQueueData = (TestCaseExecutionQueue) resp.getItem();
                    if (actionSave.equals("save")) {
                        /**
                         * The service was able to perform the query and confirm
                         * the object exist, then we can update it.
                         */
                        executionQueueData.setTest(ParameterParserUtil.parseStringParam(test, executionQueueData.getTest()));
                        executionQueueData.setTestCase(ParameterParserUtil.parseStringParam(testcase, executionQueueData.getTestCase()));
                        executionQueueData.setTag(ParameterParserUtil.parseStringParam(tag, executionQueueData.getTag()));
                        executionQueueData.setEnvironment(ParameterParserUtil.parseStringParam(environment, executionQueueData.getEnvironment()));
                        executionQueueData.setCountry(ParameterParserUtil.parseStringParam(country, executionQueueData.getCountry()));
                        executionQueueData.setManualURL(ParameterParserUtil.parseIntegerParam(manualURL, executionQueueData.getManualURL()));
                        executionQueueData.setManualHost(ParameterParserUtil.parseStringParam(manualHost, executionQueueData.getManualHost()));
                        executionQueueData.setManualContextRoot(ParameterParserUtil.parseStringParam(manualContextRoot, executionQueueData.getManualContextRoot()));
                        executionQueueData.setManualLoginRelativeURL(ParameterParserUtil.parseStringParam(manualLoginRelativeURL, executionQueueData.getManualLoginRelativeURL()));
                        executionQueueData.setManualEnvData(ParameterParserUtil.parseStringParam(manualEnvData, executionQueueData.getManualEnvData()));
                        executionQueueData.setRobot(ParameterParserUtil.parseStringParam(robot, executionQueueData.getRobot()));
                        executionQueueData.setRobotIP(ParameterParserUtil.parseStringParam(robotIP, executionQueueData.getRobotIP()));
                        executionQueueData.setRobotPort(ParameterParserUtil.parseStringParam(robotPort, executionQueueData.getRobotPort()));
                        executionQueueData.setBrowser(ParameterParserUtil.parseStringParam(browser, executionQueueData.getBrowser()));
                        executionQueueData.setBrowserVersion(ParameterParserUtil.parseStringParam(browserVersion, executionQueueData.getBrowserVersion()));
                        executionQueueData.setPlatform(ParameterParserUtil.parseStringParam(platform, executionQueueData.getPlatform()));
                        executionQueueData.setScreenSize(ParameterParserUtil.parseStringParam(screenSize, executionQueueData.getScreenSize()));
                        executionQueueData.setVerbose(ParameterParserUtil.parseIntegerParam(verbose, executionQueueData.getVerbose()));
                        executionQueueData.setScreenshot(screenshot);
                        executionQueueData.setVideo(video);
                        executionQueueData.setPageSource(pageSource);
                        executionQueueData.setRobotLog(robotLog);
                        executionQueueData.setConsoleLog(consoleLog);
                        executionQueueData.setTimeout(timeout);
                        executionQueueData.setRetries(retries);
                        executionQueueData.setManualExecution(manualExecution);
                        executionQueueData.setDebugFlag(debugFlag);
                        executionQueueData.setPriority(priority);
                        executionQueueData.setUsrModif(request.getRemoteUser());
                        ans = executionQueueService.update(executionQueueData);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);

                        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                            /**
                             * Update was successful. Adding Log entry.
                             */
                            ILogEventService logEventService = appContext.getBean(LogEventService.class);
                            logEventService.createForPrivateCalls("/UpdateTestCaseExecutionQueue", "UPDATE", LogEvent.STATUS_INFO, "Updated ExecutionQueue : ['" + id + "']", request);
                        }

                    }

                    // Update is done, we now check what action needs to be performed.
                    if (actionState.equals("toQUEUED")) {
                        LOG.debug("toQUEUED");
                        ans = executionQueueService.updateToQueued(id, "Trigered by user " + request.getRemoteUser() + ".");
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                        executionThreadPoolService.executeNextInQueueAsynchroneously(false);
                    }

                    // Priority Update.
                    if (actionSave.equals("priority")) {
                        executionQueueData.setPriority(priority);
                        ans = executionQueueService.update(executionQueueData);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    }

                    // Update is done, we now check what action needs to be performed.
                    if (actionState.equals("toCANCELLED")) {
                        LOG.debug("toCANCELLED");
                        ans = executionQueueService.updateToCancelled(id, "Cancelled by user " + request.getRemoteUser() + ".");
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    }

                    // Update is done, we now check what action needs to be performed.
                    if (actionState.equals("toCANCELLEDForce")) {
                        LOG.debug("toCANCELLEDForce");
                        ans = executionQueueService.updateToCancelledForce(id, "Forced Cancelled by user " + request.getRemoteUser() + ".");
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    }

                    // Update is done, we now check what action needs to be performed.
                    if (actionState.equals("toERRORForce")) {
                        LOG.debug("toERRORForce");
                        ans = executionQueueService.updateToErrorForce(id, "Forced Eroor by user " + request.getRemoteUser() + ".");
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    }
                }
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());

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
            LOG.warn(ex, ex);
        } catch (JSONException ex) {
            LOG.warn(ex, ex);
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
