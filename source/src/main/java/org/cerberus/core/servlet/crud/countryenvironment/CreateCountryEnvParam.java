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
package org.cerberus.core.servlet.crud.countryenvironment;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.factory.IFactoryCountryEnvParam;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "CreateCountryEnvParam", urlPatterns = {"/CreateCountryEnvParam"})
public class CreateCountryEnvParam extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(CreateCountryEnvParam.class);
    private final String OBJECT_NAME = "CountryEnvParam";

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
        String system = policy.sanitize(request.getParameter("system"));
        String country = policy.sanitize(request.getParameter("country"));
        String environment = policy.sanitize(request.getParameter("environment"));
        String type = policy.sanitize(request.getParameter("type"));
        boolean active = (request.getParameter("active") != null);
        boolean maintenanceAct = "Y".equals(policy.sanitize(request.getParameter("maintenanceAct")));
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String description = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("description"), "", charset);
        String maintenanceStr = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("maintenanceStr"), "01:00:00", charset);
        String maintenanceEnd = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("maintenanceEnd"), "01:00:00", charset);
        maintenanceStr = maintenanceStr.isEmpty() ? "00:00:00" : maintenanceStr;
        maintenanceEnd = maintenanceEnd.isEmpty() ? "00:00:00" : maintenanceEnd;
        String build = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("build"), "", charset);
        if ("ALL".equalsIgnoreCase(build)) {
            build = "";
        }
        String revision = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("revision"), "", charset);
        if ("ALL".equalsIgnoreCase(revision)) {
            revision = "";
        }
        String chain = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("chain"), "", charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String distribList = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("distribList"), "", charset);
        String emailBodyRevision = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("eMailBodyRevision"), "", charset);
        String emailBodyChain = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("eMailBodyChain"), "", charset);
        String emailBodyDisableEnvironment = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("eMailBodyDisableEnvironment"), "", charset);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isEmptyOrNull(system)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "System is missing!"));
            ans.setResultMessage(msg);
        } else if (StringUtil.isEmptyOrNull(country)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Country is missing!"));
            ans.setResultMessage(msg);
        } else if (StringUtil.isEmptyOrNull(environment)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Environment is missing!"));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
            IFactoryCountryEnvParam factoryCountryEnvParam = appContext.getBean(IFactoryCountryEnvParam.class);

            CountryEnvParam countryEnvParamData = factoryCountryEnvParam.create(system, country, environment, description, build, revision, chain, distribList,
                     emailBodyRevision, type, emailBodyChain, emailBodyDisableEnvironment, active, maintenanceAct, maintenanceStr, maintenanceEnd, "");
            ans = countryEnvParamService.create(countryEnvParamData);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Object created. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/CreateCountryEnvParam", "CREATE", LogEvent.STATUS_INFO, "Create CountryEnvParam : ['" + system + "'|'" + country + "'|'" + environment + "']", request);
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
