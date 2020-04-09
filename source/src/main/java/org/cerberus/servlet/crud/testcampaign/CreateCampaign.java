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
package org.cerberus.servlet.crud.testcampaign;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.CampaignLabel;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.crud.entity.ScheduleEntry;
import org.cerberus.crud.factory.IFactoryCampaign;
import org.cerberus.crud.factory.IFactoryCampaignLabel;
import org.cerberus.crud.factory.IFactoryCampaignParameter;
import org.cerberus.crud.factory.IFactoryLogEvent;
import org.cerberus.crud.factory.IFactoryScheduleEntry;
import org.cerberus.crud.factory.impl.FactoryLogEvent;
import org.cerberus.crud.service.ICampaignLabelService;
import org.cerberus.crud.service.ICampaignParameterService;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IMyVersionService;
import org.cerberus.crud.service.IScheduleEntryService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author cte
 */
@WebServlet(name = "CreateCampaign", urlPatterns = {"/CreateCampaign"})
public class CreateCampaign extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(CreateCampaign.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    final void processRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Answer ans = null;
        Answer schedAns = new Answer();
        Answer finalAnswer = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String name = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("Campaign"), null, charset);
        String notifyStart = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("NotifyStart"), "N", charset);
        String notifyEnd = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("NotifyEnd"), "N", charset);
        String slackNotifyStartTagExecution = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("NotifySlackStart"), "N", charset);
        String slackNotifyEndTagExecution = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("NotifySlackEnd"), "N", charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String distribList = ParameterParserUtil.parseStringParam(request.getParameter("DistribList"), "");
        String parameter = ParameterParserUtil.parseStringParam(request.getParameter("Parameters"), null);
        String label = ParameterParserUtil.parseStringParam(request.getParameter("Labels"), null);
        String desc = ParameterParserUtil.parseStringParam(request.getParameter("Description"), null);
        String longDesc = ParameterParserUtil.parseStringParam(request.getParameter("LongDescription"), null);
        String group1 = ParameterParserUtil.parseStringParam(request.getParameter("Group1"), "");
        String group2 = ParameterParserUtil.parseStringParam(request.getParameter("Group2"), "");
        String group3 = ParameterParserUtil.parseStringParam(request.getParameter("Group3"), "");

        String slackWebhook = ParameterParserUtil.parseStringParam(request.getParameter("SlackWebhook"), "");
        String slackChannel = ParameterParserUtil.parseStringParam(request.getParameter("SlackChannel"), "");
        String cIScoreThreshold = ParameterParserUtil.parseStringParam(request.getParameter("CIScoreThreshold"), "");
        String tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
        String verbose = ParameterParserUtil.parseStringParam(request.getParameter("Verbose"), "");
        String screenshot = ParameterParserUtil.parseStringParam(request.getParameter("Screenshot"), "");
        String pageSource = ParameterParserUtil.parseStringParam(request.getParameter("PageSource"), "");
        String robotLog = ParameterParserUtil.parseStringParam(request.getParameter("RobotLog"), "");
        String timeout = ParameterParserUtil.parseStringParam(request.getParameter("Timeout"), "");
        String retries = ParameterParserUtil.parseStringParam(request.getParameter("Retries"), "");
        String priority = ParameterParserUtil.parseStringParam(request.getParameter("Priority"), "");
        String manualExecution = ParameterParserUtil.parseStringParam(request.getParameter("ManualExecution"), "");

        if (StringUtil.isNullOrEmpty(name)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Campaign")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Campaign name is missing!"));
            finalAnswer.setResultMessage(msg);
        } else {
            ICampaignService campaignService = appContext.getBean(ICampaignService.class);
            IFactoryCampaign factoryCampaign = appContext.getBean(IFactoryCampaign.class);

            Campaign camp = factoryCampaign.create(0, name, distribList, notifyStart, notifyEnd,
                    slackNotifyStartTagExecution, slackNotifyEndTagExecution, slackWebhook, slackChannel,
                    cIScoreThreshold,
                    tag, verbose, screenshot, pageSource, robotLog, timeout, retries, priority, manualExecution,
                    desc, longDesc, group1, group2, group3,
                    request.getRemoteUser(), null, null, null);

            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Scheduler").replace("%OPERATION%", "No Insert"));
            schedAns.setResultMessage(msg);

            finalAnswer = campaignService.create(camp);
            if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/CreateCampaign", "CREATE", "Create Campaign : ['" + camp.getCampaign() + "']", request);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);

                if (request.getParameter("SchedulerList") != null) {
                    // Getting list of Schedule Entries from JSON Call
                    JSONArray objApplicationArray = new JSONArray(request.getParameter("SchedulerList"));
                    List<ScheduleEntry> schList = new ArrayList<>();
                    schList = getScheduleEntryListFromParameter(request, appContext, name, objApplicationArray);

                    if (!schList.isEmpty()) {
                        IScheduleEntryService scheduleentryservice = appContext.getBean(IScheduleEntryService.class);
                        schedAns = scheduleentryservice.createListSched(schList);
                    }
                    if (!schedAns.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        finalAnswer = schedAns;
                    }
                }

                if (parameter != null) {
                    JSONArray parameters = new JSONArray(parameter);
                    ICampaignParameterService campaignParameterService = appContext.getBean(ICampaignParameterService.class);
                    IFactoryCampaignParameter factoryCampaignParameter = appContext.getBean(IFactoryCampaignParameter.class);
                    ans = campaignParameterService.deleteByCampaign(name);
                    int i = 0;
                    while (i < parameters.length() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        JSONArray bat = parameters.getJSONArray(i);
                        CampaignParameter co = factoryCampaignParameter.create(0, bat.getString(0), bat.getString(2), bat.getString(3));
                        ans = campaignParameterService.create(co);
                        i++;
                    }
                }

                if (label != null) {
                    JSONArray labels = new JSONArray(label);
                    ICampaignLabelService campaignLabelService = appContext.getBean(ICampaignLabelService.class);
                    IFactoryCampaignLabel factoryCampaignLabel = appContext.getBean(IFactoryCampaignLabel.class);
                    ArrayList<CampaignLabel> arr = new ArrayList<>();
                    for (int i = 0; i < labels.length(); i++) {
                        JSONArray bat = labels.getJSONArray(i);
                        CampaignLabel co = factoryCampaignLabel.create(0, bat.getString(0), Integer.valueOf(bat.getString(2)), request.getRemoteUser(), null, request.getRemoteUser(), null);
                        arr.add(co);
                    }

                    finalAnswer = campaignLabelService.compareListAndUpdateInsertDeleteElements(name, arr);
                }

                if (ans != null && !ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    finalAnswer = ans;
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

    private List<ScheduleEntry> getScheduleEntryListFromParameter(HttpServletRequest request, ApplicationContext appContext, String campaign, JSONArray json) throws JSONException {
        List<ScheduleEntry> scheList = new ArrayList<>();
        IScheduleEntryService scheService = appContext.getBean(IScheduleEntryService.class);
        IFactoryScheduleEntry scheFactory = appContext.getBean(IFactoryScheduleEntry.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        for (int i = 0; i < json.length(); i++) {
            JSONObject tcsaJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = tcsaJson.getBoolean("toDelete");
            String cronExpression = policy.sanitize(tcsaJson.getString("cronDefinition"));
            String active = policy.sanitize(tcsaJson.getString("active"));
            String desc = policy.sanitize(tcsaJson.getString("description"));
            String strId = tcsaJson.getString("ID");
            String lastExecutionStr = tcsaJson.getString("lastExecution");
            String type = "CAMPAIGN";
            String name = campaign;

            int id;
            if (strId.isEmpty()) {
                id = 0;
            } else {
                try {
                    id = Integer.parseInt(strId);
                } catch (NumberFormatException e) {
                    LOG.warn("Unable to parse pool size: " + strId + ". Applying default value");
                    id = 0;
                }
            }

            Timestamp timestampfactice = new Timestamp(System.currentTimeMillis());

            if (!delete) {
                ScheduleEntry sch = scheFactory.create(id, type, name, cronExpression, timestampfactice, active, desc, request.getRemoteUser(), timestampfactice, request.getRemoteUser(), timestampfactice);
                scheList.add(sch);
            }
        }
        return scheList;
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
            this.processRequest(request, response);
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
            this.processRequest(request, response);
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
