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
package org.cerberus.core.servlet.crud.testcampaign;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.entity.CampaignLabel;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.crud.factory.IFactoryCampaignLabel;
import org.cerberus.core.crud.factory.IFactoryCampaignParameter;
import org.cerberus.core.crud.factory.IFactoryEventHook;
import org.cerberus.core.crud.factory.IFactoryScheduleEntry;
import org.cerberus.core.crud.service.ICampaignLabelService;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.IEventHookService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IScheduleEntryService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.crud.entity.LogEvent;

/**
 * @author bcivel
 */
@WebServlet(name = "UpdateCampaign", urlPatterns = {"/UpdateCampaign"})
public class UpdateCampaign extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateCampaign.class);

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
        Answer schedAns = new Answer();
        Answer finalAnswer = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        PrintWriter out = response.getWriter();
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        int cID = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("CampaignID"), 0, charset);
        String campaign = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("Campaign"), null, charset);
        String originalCampaign = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("OriginalCampaign"), null, charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String desc = ParameterParserUtil.parseStringParam(request.getParameter("Description"), null);
        String longDesc = ParameterParserUtil.parseStringParam(request.getParameter("LongDescription"), null);
        String group1 = ParameterParserUtil.parseStringParam(request.getParameter("Group1"), "");
        String group2 = ParameterParserUtil.parseStringParam(request.getParameter("Group2"), "");
        String group3 = ParameterParserUtil.parseStringParam(request.getParameter("Group3"), "");

        String cIScoreThreshold = ParameterParserUtil.parseStringParam(request.getParameter("CIScoreThreshold"), "");
        String tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
        String verbose = ParameterParserUtil.parseStringParam(request.getParameter("Verbose"), "");
        String screenshot = ParameterParserUtil.parseStringParam(request.getParameter("Screenshot"), "");
        String video = ParameterParserUtil.parseStringParam(request.getParameter("Video"), "");
        String pageSource = ParameterParserUtil.parseStringParam(request.getParameter("PageSource"), "");
        String robotLog = ParameterParserUtil.parseStringParam(request.getParameter("RobotLog"), "");
        String consoleLog = ParameterParserUtil.parseStringParam(request.getParameter("ConsoleLog"), "");
        String timeout = ParameterParserUtil.parseStringParam(request.getParameter("Timeout"), "");
        String retries = ParameterParserUtil.parseStringParam(request.getParameter("Retries"), "");
        String priority = ParameterParserUtil.parseStringParam(request.getParameter("Priority"), "");
        String manualExecution = ParameterParserUtil.parseStringParam(request.getParameter("ManualExecution"), "");

        // Getting list of application from JSON Call
        if (StringUtil.isEmptyOrNull(originalCampaign)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Campaign")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Campaign name is missing!"));
            finalAnswer.setResultMessage(msg);
        } else {
            // Parameter that we cannot secure as we need the html --> We DECODE them
            //String battery = ParameterParserUtil.parseStringParam(request.getParameter("Batteries"), null);
            String parameter = ParameterParserUtil.parseStringParam(request.getParameter("Parameters"), null);
            String label = ParameterParserUtil.parseStringParam(request.getParameter("Labels"), null);

            ICampaignService campaignService = appContext.getBean(ICampaignService.class);

            AnswerItem resp = campaignService.readByKey(originalCampaign);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, resp);
            } else {
                Campaign camp = (Campaign) resp.getItem();
                camp.setCampaign(campaign);
                camp.setDescription(desc);
                camp.setLongDescription(longDesc);
                camp.setGroup1(group1);
                camp.setGroup2(group2);
                camp.setGroup3(group3);
                camp.setCIScoreThreshold(cIScoreThreshold);
                camp.setTag(tag);
                camp.setVerbose(verbose);
                camp.setScreenshot(screenshot);
                camp.setVideo(video);
                camp.setPageSource(pageSource);
                camp.setRobotLog(robotLog);
                camp.setConsoleLog(consoleLog);
                camp.setTimeout(timeout);
                camp.setRetries(retries);
                camp.setPriority(priority);
                camp.setManualExecution(manualExecution);
                camp.setUsrModif(request.getRemoteUser());

                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Scheduler").replace("%OPERATION%", "No Insert"));
                schedAns.setResultMessage(msg);

                finalAnswer = campaignService.update(originalCampaign, camp);

                if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {

                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateCampaign", "UPDATE", LogEvent.STATUS_INFO, "Update Campaign : ['" + originalCampaign + "']", request);

                    if (request.getParameter("ScheduledEntries") != null) {
                        // Getting list of Schedule Entries from JSON Call
                        IScheduleEntryService scheduleEntryService = appContext.getBean(IScheduleEntryService.class);
                        JSONArray objScheduleArray = new JSONArray(request.getParameter("ScheduledEntries"));
                        List<ScheduleEntry> schList = new ArrayList<>();
                        schList = getScheduleEntryListFromParameter(request, appContext, campaign, objScheduleArray);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, scheduleEntryService.compareSchedListAndUpdateInsertDeleteElements(originalCampaign, schList));
                    }

                    if (request.getParameter("EventEntries") != null) {
                        // Getting list of Schedule Entries from JSON Call
                        IEventHookService eventHookService = appContext.getBean(IEventHookService.class);
                        JSONArray objEventHookArray = new JSONArray(request.getParameter("EventEntries"));
                        List<EventHook> schList = new ArrayList<>();
                        schList = getEventHookEntryListFromParameter(request, appContext, campaign, objEventHookArray);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, eventHookService.compareListAndUpdateInsertDeleteElements(originalCampaign, schList));
                    }

                    if (parameter != null) {
                        JSONArray parameters = new JSONArray(parameter);
                        ICampaignParameterService campaignParameterService = appContext.getBean(ICampaignParameterService.class);
                        IFactoryCampaignParameter factoryCampaignParameter = appContext.getBean(IFactoryCampaignParameter.class);
                        ArrayList<CampaignParameter> arr = new ArrayList<>();
                        for (int i = 0; i < parameters.length(); i++) {
                            JSONArray bat = parameters.getJSONArray(i);
                            CampaignParameter co = factoryCampaignParameter.create(0, campaign, bat.getString(2), bat.getString(3));
                            arr.add(co);
                        }
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, campaignParameterService.compareListAndUpdateInsertDeleteElements(campaign, arr));
                    }

                    if (label != null) {
                        JSONArray labels = new JSONArray(label);
                        ICampaignLabelService campaignLabelService = appContext.getBean(ICampaignLabelService.class);
                        IFactoryCampaignLabel factoryCampaignLabel = appContext.getBean(IFactoryCampaignLabel.class);
                        ArrayList<CampaignLabel> arr = new ArrayList<>();
                        for (int i = 0; i < labels.length(); i++) {
                            JSONArray bat = labels.getJSONArray(i);
                            CampaignLabel co = factoryCampaignLabel.create(0, campaign, bat.getInt(2), request.getRemoteUser(), null, request.getRemoteUser(), null);
                            arr.add(co);
                        }
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, campaignLabelService.compareListAndUpdateInsertDeleteElements(campaign, arr));
                    }

                } else {
                    finalAnswer = schedAns;
                }
            }
        }

        /**
         * Formatting and returning the json result.
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
            String active = policy.sanitize(tcsaJson.getString("isActive"));
            long id = tcsaJson.getLong("id");
            String desc = tcsaJson.getString("description");
            String type = "CAMPAIGN";
            String name = campaign;

//            int id;
//            if (strId.isEmpty()) {
//                id = 0;
//            } else {
//                try {
//                    id = Integer.parseInt(strId);
//                } catch (NumberFormatException e) {
//                    LOG.warn("Unable to parse pool size: " + strId + ". Applying default value");
//                    id = 0;
//                }
//            }

            Timestamp timestampfactice = new Timestamp(System.currentTimeMillis());

            if (!delete) {
                ScheduleEntry sch = scheFactory.create(id, type, name, cronExpression, timestampfactice, active, desc, request.getRemoteUser(), timestampfactice, request.getRemoteUser(), timestampfactice);
                scheList.add(sch);
            }
        }
        return scheList;
    }

    private List<EventHook> getEventHookEntryListFromParameter(HttpServletRequest request, ApplicationContext appContext, String campaign, JSONArray json) throws JSONException {
        List<EventHook> evtList = new ArrayList<>();
        IFactoryEventHook evtFactory = appContext.getBean(IFactoryEventHook.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objJson.getBoolean("toDelete");
            String objectKey1 = campaign;
            String hookConnector = policy.sanitize(objJson.getString("hookConnector"));
            String eventReference = policy.sanitize(objJson.getString("eventReference"));
            String hookRecipient = objJson.getString("hookRecipient");
            String description = policy.sanitize(objJson.getString("description"));
            boolean isActive = objJson.getBoolean("isActive");
            String hookChannel = policy.sanitize(objJson.getString("hookChannel"));
            int id = objJson.getInt("id");

//            int id;
//            if (strId.isEmpty()) {
//                id = 0;
//            } else {
//                try {
//                    id = Integer.parseInt(strId);
//                } catch (NumberFormatException e) {
//                    LOG.warn("Unable to parse pool size: " + strId + ". Applying default value");
//                    id = 0;
//                }
//            }

            Timestamp timestampfactice = new Timestamp(System.currentTimeMillis());

            if (!delete) {
                EventHook evt = evtFactory.create(id, eventReference, objectKey1, "", isActive, hookConnector, hookRecipient, hookChannel, description, request.getRemoteUser(), timestampfactice, request.getRemoteUser(), timestampfactice);
                evtList.add(evt);
            }
        }
        return evtList;
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
            LOG.warn(ex,ex);
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
            String t = request.getParameter("value");
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex,ex);
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
