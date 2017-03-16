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
package org.cerberus.servlet.crud.testexecution;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.crud.factory.IFactoryRobot;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IRobotService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
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
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");
        String charset = request.getCharacterEncoding();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IFactoryRobot robotFactory = appContext.getBean(IFactoryRobot.class);

        /**
         * Parsing and securing all required parameters.
         */
        String robot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("robot"), null, charset);
        String host = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("host"), null, charset);
        String port = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("port"), null, charset);
        String platform = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("platform"), null, charset);
        String browser = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("browser"), null, charset);
        String version = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("version"), "", charset);
        String active = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("active"), "Y", charset);
        String description = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("description"), "", charset);
        String userAgent = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("useragent"), "", charset);
        String screenSize = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("screensize"), "", charset);
        List<RobotCapability> capabilities = (List<RobotCapability>) (request.getParameter("capabilities") == null ? Collections.emptyList() : gson.fromJson(request.getParameter("capabilities"), new TypeToken<List<RobotCapability>>(){}.getType()));
        // Securing capabilities by setting them the associated robot name
        // Check also if there is no duplicated capability
        Map<String, Object> capabilityMap = new HashMap<String, Object>();
        for (RobotCapability capability : capabilities) {
            capabilityMap.put(capability.getCapability(), null);
        }
        Integer robotid = 0;
        boolean robotid_error = true;
        try {
            if (request.getParameter("robotid") != null && !request.getParameter("robotid").equals("")) {
                robotid = Integer.valueOf(policy.sanitize(request.getParameter("robotid")));
                robotid_error = false;
            }
        } catch (Exception ex) {
            robotid_error = true;
        }

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(robot)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Robot name is missing."));
            ans.setResultMessage(msg);
        } else if (StringUtil.isNullOrEmpty(host)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Robot host is missing."));
            ans.setResultMessage(msg);
        } else if (StringUtil.isNullOrEmpty(port)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Robot port is missing."));
            ans.setResultMessage(msg);
        } else if (StringUtil.isNullOrEmpty(platform)) {
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
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            IRobotService robotService = appContext.getBean(IRobotService.class);

            AnswerItem resp = robotService.readByKeyTech(robotid);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem()!=null)) {
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

                for (RobotCapability capability : capabilities) {
                    capability.setRobot(robotData);
                }

                robotData.setRobot(robot);
                robotData.setHost(host);
                robotData.setPort(port);
                robotData.setPlatform(platform);
                robotData.setBrowser(browser);
                robotData.setVersion(version);
                robotData.setActive(active);
                robotData.setDescription(description);
                robotData.setUserAgent(userAgent);
                robotData.setCapabilities(capabilities);
                robotData.setScreenSize(screenSize);
                ans = robotService.update(robotData);

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Update was succesfull. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createPrivateCalls("/UpdateRobot", "UPDATE", "Updated Robot : ['" + robotid + "'|'" + robot + "']", request);
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
    private static final Logger LOG = Logger.getLogger(UpdateRobot.class.getName());

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
            Logger.getLogger(UpdateRobot.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateRobot.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UpdateRobot.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateRobot.class.getName()).log(Level.SEVERE, null, ex);
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
