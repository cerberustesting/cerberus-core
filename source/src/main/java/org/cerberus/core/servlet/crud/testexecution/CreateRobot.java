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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.factory.IFactoryRobot;
import org.cerberus.core.crud.factory.IFactoryRobotExecutor;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IRobotExecutorService;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
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
@WebServlet(name = "CreateRobot", urlPatterns = {"/CreateRobot"})
public class CreateRobot extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(CreateRobot.class);

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
        Answer ans = new Answer();
        Gson gson = new Gson();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        /**
         * Parsing and securing all required parameters.
         */
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String originalRobotName = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("originalRobotName"), null, charset);
        String robot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("robot"), null, charset);
        String platform = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("platform"), null, charset);
        String browser = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("browser"), null, charset);
        String version = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("version"), "", charset);
        boolean isActive = ParameterParserUtil.parseBooleanParam(request.getParameter("isActive"), true);
        String description = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("description"), "", charset);
        String userAgent = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("useragent"), "", charset);
        String screenSize = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("screensize"), "", charset);
        String profileFolder = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("profileFolder"), "", charset);
        String robotDecli = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("robotDecli"), "", charset);
        String lbexemethod = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("lbexemethod"), "", charset);
        String type = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("type"), "", charset);
        Integer acceptNotifications = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("acceptNotifications"), 0, charset);
        String extraParam = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("extraParam"), "", charset);
        boolean isAcceptInsecureCerts = ParameterParserUtil.parseBooleanParamAndDecode(request.getParameter("isAcceptInsecureCerts"), true, charset);

        List<RobotCapability> capabilities;
        if (request.getParameter("capabilities") == null) {
            capabilities = Collections.emptyList();
        } else {
            capabilities = gson.fromJson(request.getParameter("capabilities"), new TypeToken<List<RobotCapability>>() {
            }.getType());
        }

        JSONArray objExecutorArray = new JSONArray(request.getParameter("executors"));
        List<RobotExecutor> executors = new ArrayList<>();
        executors = getExecutorsFromParameter(originalRobotName, robot, request, appContext, objExecutorArray);

        // Parameter that we cannot secure as we need the html --> We DECODE them
        // Securing capabilities by setting them the associated robot name
        // Check also if there is no duplicated capability
        Map<String, Object> capabilityMap = new HashMap<>();
        for (RobotCapability capability : capabilities) {
            capabilityMap.put(capability.getCapability(), null);
            capability.setRobot(robot);
        }

        Map<String, Object> executorMap = new HashMap<>();
        for (RobotExecutor executor : executors) {
            executorMap.put(executor.getExecutor(), null);
            executor.setRobot(robot);
        }

        Integer robotid = 0;
        boolean robotid_error = false;
        try {
            if (request.getParameter("robotid") != null && !request.getParameter("robotid").isEmpty()) {
                robotid = Integer.valueOf(policy.sanitize(request.getParameter("robotid")));
            }
        } catch (Exception ex) {
            robotid_error = true;
        }

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isEmptyOrNull(robot)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Robot name is missing."));
            ans.setResultMessage(msg);
        } else if (StringUtil.isEmptyOrNull(platform)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Robot platform is missing."));
            ans.setResultMessage(msg);
        } else if (robotid_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Could not manage to convert robotid to an integer value or robotid is missing."));
            ans.setResultMessage(msg);
        } else if (capabilityMap.size() != capabilities.size()) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "There is at least one duplicated capability. Please edit or remove it to continue."));
            ans.setResultMessage(msg);
        } else if (executorMap.size() != executors.size()) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "There is at least one duplicated executor. Please edit or remove it to continue."));
            ans.setResultMessage(msg);
        } else if (executorMap.size() < 1) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "You need to specify at least 1 executor with non empty host in order to submit execution. Please add it from Executor TAB to continue."));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            IRobotService robotService = appContext.getBean(IRobotService.class);
            IFactoryRobot robotFactory = appContext.getBean(IFactoryRobot.class);

            Robot robotData = robotFactory.create(robotid, robot, platform, browser, version, isActive, lbexemethod, description, userAgent, screenSize, profileFolder, acceptNotifications, extraParam, isAcceptInsecureCerts, capabilities, executors, robotDecli, type);
            ans = robotService.create(robotData);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Object created. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/CreateRobot", "CREATE", LogEvent.STATUS_INFO, "Create Robot : ['" + robot + "']", request);
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();

    }

    private List<RobotExecutor> getExecutorsFromParameter(String originalRobotName, String robot, HttpServletRequest request, ApplicationContext appContext, JSONArray json) throws JSONException, CerberusException {
        List<RobotExecutor> reList = new ArrayList<>();
        IFactoryRobotExecutor reFactory = appContext.getBean(IFactoryRobotExecutor.class);
        IRobotExecutorService reService = appContext.getBean(IRobotExecutorService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        //We pass the original robot name in case the robot is duplicated, otherwise we query with the new name, so no records found in DB.
        // If no change, originalRobotName is equal to robot.
        List<RobotExecutor> robotExecutorsFromDb = reService.convert(reService.readByRobot(originalRobotName));

        for (int i = 0; i < json.length(); i++) {
            JSONObject reJson = json.getJSONObject(i);

            boolean delete = reJson.getBoolean("toDelete");
            Integer id = reJson.getInt("ID");
            String executor = reJson.getString("executor");
            boolean isActive = reJson.getBoolean("isActive");
            Integer rank = reJson.getInt("rank");
            String host = reJson.getString("host");
            String port = reJson.getString("port");
            String hostUser = reJson.getString("hostUser");
            String deviceName = reJson.getString("deviceName");
            String deviceUdid = reJson.getString("deviceUdid");
            Integer devicePort = null;
            if (reJson.has("devicePort") && !StringUtil.isEmptyOrNull(reJson.getString("devicePort"))) {
                devicePort = reJson.getInt("devicePort");
            }
            String executorBrowserProxyHost = "";
            if (reJson.has("executorBrowserProxyHost") && !StringUtil.isEmptyOrNull(reJson.getString("executorBrowserProxyHost"))) {
                executorBrowserProxyHost = reJson.getString("executorBrowserProxyHost");
            }
            Integer executorBrowserProxyPort = null;
            if (reJson.has("executorBrowserProxyPort")) {
                executorBrowserProxyPort = reJson.getInt("executorBrowserProxyPort");
            }
            String executorProxyType = reJson.getString("executorProxyType");
            Integer executorProxyServicePort = null;
            String executorProxyServiceHost = "";
            if (reJson.has("executorProxyServiceHost") && !StringUtil.isEmptyOrNull(reJson.getString("executorProxyServiceHost"))) {
                executorProxyServiceHost = reJson.getString("executorProxyServiceHost");
            }
            if (reJson.has("executorProxyServicePort")) {
                executorProxyServicePort = reJson.getInt("executorProxyServicePort");
            }
            String description = reJson.getString("description");

            String hostPassword = reJson.getString("hostPassword");
            if (hostPassword.equals(StringUtil.SECRET_STRING)) {
                hostPassword = "";
                for (RobotExecutor robotExecutor : robotExecutorsFromDb) {
                    if (robotExecutor.getID() == id) {
                        hostPassword = robotExecutor.getHostPassword();
                        LOG.debug("Password not changed so reset to original value : " + robotExecutor.getHostPassword());
                    }
                }
            }

            //Front (json serialize) send string empty when user provides no value. So, need to check first if value is not an empty string to secure and affect 0.
            Integer executorExtensionPort = (reJson.has("executorExtensionPort") && !reJson.get("executorExtensionPort").toString().isEmpty()) ? reJson.getInt("executorExtensionPort") : 0;

            if (!delete) {
                RobotExecutor reo = reFactory.create(i, robot, executor, isActive, rank, host, port, hostUser, hostPassword, 0, deviceUdid, deviceName, devicePort, false, executorProxyServiceHost, executorProxyServicePort, executorBrowserProxyHost, executorBrowserProxyPort, executorExtensionPort, executorProxyType, description, "", null, "", null);
                reList.add(reo);
            }
        }
        return reList;
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
            LOG.warn(ex, ex);
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
