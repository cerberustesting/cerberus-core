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
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.factory.IFactoryCountryEnvironmentParameters;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
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
@WebServlet(name = "UpdateApplication", urlPatterns = {"/UpdateApplication"})
public class UpdateApplication extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateApplication.class);

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        ICountryEnvironmentParametersService ceaService = appContext.getBean(ICountryEnvironmentParametersService.class);
        IFactoryCountryEnvironmentParameters cedFactory = appContext.getBean(IFactoryCountryEnvironmentParameters.class);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        String system = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("system"), "", charset);
        String type = policy.sanitize(request.getParameter("type"));
        String deployType = policy.sanitize(request.getParameter("deploytype"));
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String application = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("application"), null, charset);
        String originalApplication = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("originalApplication"), null, charset);
        String subSystem = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("subsystem"), "", charset);
        String mavenGpID = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("mavengroupid"), "", charset);
        String description = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("description"), "", charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        int poolSize = ParameterParserUtil.parseIntegerParam(request.getParameter("poolSize"), 0);
        String repoURL = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("repourl"), "", charset);
        String bugTrackerConnector = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("bugtrackerconnector"), "", charset);
        String bugTrackerParam1 = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("bugtrackerparam1"), "", charset);
        String bugTrackerParam2 = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("bugtrackerparam2"), "", charset);
        String bugTrackerParam3 = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("bugtrackerparam3"), "", charset);
        String bugTrackerURL = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("bugtrackerurl"), "", charset);
        String newBugURL = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("bugtrackernewurl"), "", charset);
        Integer sort = 10;
        boolean sort_error = false;
        try {
            if (request.getParameter("sort") != null && !request.getParameter("sort").isEmpty()) {
                sort = Integer.valueOf(policy.sanitize(request.getParameter("sort")));
            }
        } catch (Exception ex) {
            sort_error = true;
        }

        // Getting list of application from JSON Call
        JSONArray objApplicationArray = new JSONArray(request.getParameter("environmentList"));
        List<CountryEnvironmentParameters> ceaList = new ArrayList<>();
        ceaList = getCountryEnvironmentApplicationFromParameter(request, appContext, system, application, objApplicationArray);

        boolean emptycountryenv_error = false;
        for (CountryEnvironmentParameters cea : ceaList) {
            if ((StringUtil.isEmptyOrNull(cea.getCountry())) || (StringUtil.isEmptyOrNull(cea.getEnvironment()))) {
                emptycountryenv_error = true;
            }
        }

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isEmptyOrNull(application)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Application")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Application ID (application) is missing."));
            finalAnswer.setResultMessage(msg);
        } else if (StringUtil.isEmptyOrNull(system)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Application")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "System is missing!"));
            finalAnswer.setResultMessage(msg);
        } else if (sort_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Application")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Could not manage to convert sort to an integer value."));
            finalAnswer.setResultMessage(msg);
        } else if (emptycountryenv_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Application")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "One of the environment defined has an empty value on country or environment value."));
            finalAnswer.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            IApplicationService applicationService = appContext.getBean(IApplicationService.class);

            AnswerItem resp = applicationService.readByKey(originalApplication);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, resp);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                Application applicationData = (Application) resp.getItem();
                applicationData.setApplication(application);
                applicationData.setSystem(system);
                applicationData.setSubsystem(subSystem);
                applicationData.setType(type);
                applicationData.setMavengroupid(mavenGpID);
                applicationData.setDeploytype(deployType);
                applicationData.setRepoUrl(repoURL);
                applicationData.setPoolSize(poolSize);
                applicationData.setBugTrackerUrl(bugTrackerURL);
                applicationData.setBugTrackerConnector(bugTrackerConnector);
                applicationData.setBugTrackerParam1(bugTrackerParam1);
                applicationData.setBugTrackerParam2(bugTrackerParam2);
                applicationData.setBugTrackerParam3(bugTrackerParam3);
                applicationData.setBugTrackerNewUrl(newBugURL);
                applicationData.setDescription(description);
                applicationData.setSort(sort);
                applicationData.setUsrModif(request.getRemoteUser());
                ans = applicationService.update(originalApplication, applicationData);
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Update was successful. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateApplication", "UPDATE", LogEvent.STATUS_INFO, "Updated Application : ['" + originalApplication + "']", request);

                    // Update the Database with the new list.
                    ans = ceaService.compareListAndUpdateInsertDeleteElements(system, application, ceaList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);

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

    private List<CountryEnvironmentParameters> getCountryEnvironmentApplicationFromParameter(HttpServletRequest request, ApplicationContext appContext, String system, String application, JSONArray json) throws JSONException {
        List<CountryEnvironmentParameters> cedList = new ArrayList<>();
        ICountryEnvironmentParametersService ceaService = appContext.getBean(ICountryEnvironmentParametersService.class);
        IFactoryCountryEnvironmentParameters cedFactory = appContext.getBean(IFactoryCountryEnvironmentParameters.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        for (int i = 0; i < json.length(); i++) {
            JSONObject tcsaJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = tcsaJson.getBoolean("toDelete");
            String country = policy.sanitize(tcsaJson.getString("country"));
            String environment = policy.sanitize(tcsaJson.getString("environment"));
            boolean isActive = tcsaJson.getBoolean("isActive");
            // Parameter that needs to be secured --> We SECURE+DECODE them
            // Parameter that we cannot secure as we need the html --> We DECODE them
            String ip = tcsaJson.getString("ip");
            String domain = tcsaJson.getString("domain");
            String url = tcsaJson.getString("url");
            String urlLogin = tcsaJson.getString("urlLogin");
            String var1 = tcsaJson.getString("var1");
            String var2 = tcsaJson.getString("var2");
            String var3 = tcsaJson.getString("var3");
            String var4 = tcsaJson.getString("var4");
            String secret1 = tcsaJson.getString("secret1");
            String secret2 = tcsaJson.getString("secret2");
            int poolSize = tcsaJson.getInt("poolSize");
            String mobileActivity = tcsaJson.getString("mobileActivity");
            String mobilePackage = tcsaJson.getString("mobilePackage");

            if (!delete) {
                CountryEnvironmentParameters ced = cedFactory.create(system, country, environment, application, isActive, ip, domain, url, urlLogin, var1, var2, var3, var4, secret1, secret2, poolSize, mobileActivity, mobilePackage, request.getRemoteUser(), null, request.getRemoteUser(), null);
                cedList.add(ced);
            }
        }
        return cedList;
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

        } catch (CerberusException | JSONException ex) {
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

        } catch (CerberusException | JSONException ex) {
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
