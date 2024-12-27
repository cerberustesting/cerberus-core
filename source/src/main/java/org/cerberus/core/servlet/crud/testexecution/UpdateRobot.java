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
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.factory.IFactoryRobotExecutor;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IRobotExecutorService;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
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
@WebServlet(name = "UpdateRobot", urlPatterns = {"/UpdateRobot"})
public class UpdateRobot extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateRobot.class);

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
        String robot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("robot"), null, charset);
        String platform = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("platform"), null, charset);
        String browser = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("browser"), null, charset);
        String version = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("version"), "", charset);
        boolean isActive = ParameterParserUtil.parseBooleanParam(request.getParameter("isActive"), true);
        String description = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("description"), "", charset);
        String userAgent = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("useragent"), "", charset);
        String screenSize = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("screensize"), "", charset);
        String profileFolder = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("profileFolder"), "", charset);
        String robotDecli = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("robotDecli"), null, charset);
        String lbexemethod = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("lbexemethod"), null, charset);
        String type = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("type"), null, charset);
        Integer acceptNotifications = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("acceptNotifications"), 0, charset);
        String extraParam = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("extraParam"), null, charset);
        boolean isAcceptCerts = ParameterParserUtil.parseBooleanParam(request.getParameter("isAcceptInsecureCerts"), true);

        List<RobotCapability> capabilities;
        if (request.getParameter("capabilities") == null) {
            capabilities = Collections.emptyList();
        } else {
            capabilities = gson.fromJson(request.getParameter("capabilities"), new TypeToken<List<RobotCapability>>() {
            }.getType());
        }

        JSONArray objExecutorArray = new JSONArray(request.getParameter("executors"));
        List<RobotExecutor> executors = new ArrayList<>();
        executors = getExecutorsFromParameter(robot, request, appContext, objExecutorArray);

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
        boolean robotid_error = true;
        try {
            if (request.getParameter("robotid") != null && !request.getParameter("robotid").isEmpty()) {
                robotid = Integer.valueOf(policy.sanitize(request.getParameter("robotid")));
                robotid_error = false;
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
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Robot name is missing."));
            ans.setResultMessage(msg);
        } else if (StringUtil.isEmptyOrNull(platform)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Robot platform is missing."));
            ans.setResultMessage(msg);
        } else if (robotid_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Update")
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

            AnswerItem resp = robotService.readByKeyTech(robotid);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Robot does not exist."));
                ans.setResultMessage(msg);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                Robot robotData = (Robot) resp.getItem();
                robotData.setRobot(robot);
                robotData.setPlatform(platform);
                robotData.setBrowser(browser);
                robotData.setVersion(version);
                robotData.setIsActive(isActive);
                robotData.setDescription(description);
                robotData.setUserAgent(userAgent);
                robotData.setScreenSize(screenSize);
                robotData.setProfileFolder(profileFolder);
                robotData.setRobotDecli(robotDecli);
                robotData.setLbexemethod(lbexemethod);
                robotData.setAcceptNotifications(acceptNotifications);
                robotData.setExtraParam(extraParam);
                robotData.setIsAcceptInsecureCerts(isAcceptCerts);
                robotData.setCapabilities(capabilities);
                robotData.setExecutors(executors);
                robotData.setType(type);
                ans = robotService.update(robotData, request.getUserPrincipal().getName());

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Update was successful. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateRobot", "UPDATE", LogEvent.STATUS_INFO, "Updated Robot : ['" + robotid + "'|'" + robot + "']", request);
                }
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

    private List<RobotExecutor> getExecutorsFromParameter(String robot, HttpServletRequest request, ApplicationContext appContext, JSONArray json) throws JSONException, CerberusException {
        List<RobotExecutor> reList = new ArrayList<>();
        IFactoryRobotExecutor reFactory = appContext.getBean(IFactoryRobotExecutor.class);
        IRobotExecutorService reService = appContext.getBean(IRobotExecutorService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        List<RobotExecutor> reList1 = reService.convert(reService.readByRobot(robot));

        for (int i = 0; i < json.length(); i++) {
            JSONObject reJson = json.getJSONObject(i);

            boolean delete = reJson.getBoolean("toDelete");
            Integer id = reJson.getInt("ID");
            String executor = reJson.getString("executor");
            boolean isActive = reJson.getBoolean("isActive");
            Integer rank = reJson.getInt("rank");
            String host = reJson.getString("host");
            String port = reJson.getString("port");
            String host_user = reJson.getString("hostUser");
            String deviceName = reJson.getString("deviceName");
            String deviceUdid = reJson.getString("deviceUdid");
            boolean isDeviceLockUnlock = reJson.getBoolean("isDeviceLockUnlock");
            String executorProxyHost = "";
            if (reJson.has("executorProxyHost") && !StringUtil.isEmptyOrNull(reJson.getString("executorProxyHost"))) {
                executorProxyHost = reJson.getString("executorProxyHost");
            }
            Integer executorProxyPort = null;
            if (reJson.has("executorProxyPort")) {
                executorProxyPort = reJson.getInt("executorProxyPort");
            }
            String executorProxyType = reJson.getString("executorProxyType");
            Integer executorExtensionPort = null;
            String executorExtensionHost = "";
            if (reJson.has("executorExtensionHost")) {
                executorExtensionHost = reJson.getString("executorExtensionHost");
            }
            if (reJson.has("executorExtensionPort")) {
                executorExtensionPort = reJson.getInt("executorExtensionPort");
            }

            Integer devicePort = null;
            if (reJson.has("devicePort") && !StringUtil.isEmptyOrNull(reJson.getString("devicePort"))) {
                devicePort = reJson.getInt("devicePort");
            }

            String description = reJson.getString("description");

            String host_password = reJson.getString("hostPassword");
            if (host_password.equals(StringUtil.SECRET_STRING)) {
                host_password = "";
                for (RobotExecutor robotExecutor : reList1) {
                    if (robotExecutor.getID() == id) {
                        host_password = robotExecutor.getHostPassword();
                        LOG.debug("Password not changed so reset to original value : " + robotExecutor.getHostPassword());
                    }
                }
            }

            if (!delete) {
                RobotExecutor reo = reFactory.create(i, robot, executor, isActive, rank, host, port, host_user, host_password, 0, deviceUdid, deviceName, devicePort, isDeviceLockUnlock, executorExtensionHost, executorExtensionPort, executorProxyHost, executorProxyPort, executorProxyType, description, "", null, "", null);
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
