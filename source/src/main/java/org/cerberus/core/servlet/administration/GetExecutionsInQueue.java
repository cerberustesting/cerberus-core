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
package org.cerberus.core.servlet.administration;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IMyVersionService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Executes the next test case contained into the execution queue.
 * <p>
 * <p>
 * No parameters needed.
 * </p>
 *
 * @author abourdon
 */
@WebServlet(name = "GetExecutionsInQueue", description = "Execute the next record from the execution queue.", urlPatterns = {"/GetExecutionsInQueue"})
public class GetExecutionsInQueue extends HttpServlet {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = LogManager.getLogger(GetExecutionsInQueue.class);

    private static final long serialVersionUID = 1L;

    private IExecutionThreadPoolService threadPoolService;
    private IMyVersionService myVersionService;
    private IParameterService parameterService;
    private ITestCaseExecutionQueueService testCaseExectionQueueService;
    private IExecutionThreadPoolService executionThreadPoolService;

    @Override
    public void init() throws ServletException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        threadPoolService = appContext.getBean(IExecutionThreadPoolService.class);
        myVersionService = appContext.getBean(IMyVersionService.class);
        parameterService = appContext.getBean(IParameterService.class);
        testCaseExectionQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);
        executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JSONObject jsonResponse = new JSONObject();
        Answer answer = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        answer.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        ILogEventService logEventService;
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        boolean forceExecution = ParameterParserUtil.parseBooleanParamAndDecode(request.getParameter("forceExecution"), false, charset);

        if (forceExecution) {
            try {

                logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/GetExecutionsInQueue", "START", LogEvent.STATUS_INFO, "Forcing Queue execution Job.", request);

                threadPoolService.executeNextInQueueAsynchroneously(true);
                response.setStatus(HttpStatus.OK.value());
            } catch (CerberusException e) {
                LOG.warn("Unable to execute next in queue", e);
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }

        try {

            String jobRunning = myVersionService.getMyVersionStringByKey("queueprocessingjobrunning", "");
            jsonResponse.put("jobRunning", jobRunning);

            String jobStart = myVersionService.getMyVersionStringByKey("queueprocessingjobstart", "");
            jsonResponse.put("jobStart", jobStart);

            boolean jobActive = parameterService.getParameterBooleanByKey("cerberus_queueexecution_enable", "", true);
            jsonResponse.put("jobActive", jobActive);

            jsonResponse.put("executionThreadPoolInstanceActive", executionThreadPoolService.isInstanceActive());

            jsonResponse.put("jobActiveHasPermissionsUpdate", parameterService.hasPermissionsUpdate("cerberus_queueexecution_enable", request));

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }

    }

}
