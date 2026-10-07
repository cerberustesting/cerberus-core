/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.servlet.crud.scheduleentry;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.crud.entity.ScheduledExecution;
import org.cerberus.core.crud.service.IScheduleEntryService;
import org.cerberus.core.crud.service.IScheduledExecutionService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.CronExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Everything the Scheduled Runs page needs in one call: the schedule entries,
 * the next fire times computed from their cron definition, and the history of
 * the scheduled executions of the last days.
 * <p>
 * Parameters (all optional) : <code>days</code> history depth, 7 by default and
 * 90 at most; <code>next</code> number of upcoming fire times computed for each
 * entry, 5 by default and 20 at most.
 * <p>
 * Times are sent as epoch milliseconds. The cron definitions are evaluated in
 * the time zone of the server, like the Quartz scheduler that fires them.
 */
@WebServlet(name = "ReadScheduledRuns", urlPatterns = {"/ReadScheduledRuns"})
public class ReadScheduledRuns extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadScheduledRuns.class);

    private static final int DEFAULT_DAYS = 7;
    private static final int MAX_DAYS = 90;
    private static final int DEFAULT_NEXT = 5;
    private static final int MAX_NEXT = 20;
    private static final int MAX_HISTORY_ROWS = 5000;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException {
        response.setContentType("application/json");
        ServletUtil.servletStart(request);

        int days = Math.max(1, Math.min(MAX_DAYS, ParameterParserUtil.parseIntegerParam(request.getParameter("days"), DEFAULT_DAYS)));
        int next = Math.max(0, Math.min(MAX_NEXT, ParameterParserUtil.parseIntegerParam(request.getParameter("next"), DEFAULT_NEXT)));

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IScheduleEntryService scheduleEntryService = appContext.getBean(IScheduleEntryService.class);
        IScheduledExecutionService scheduledExecutionService = appContext.getBean(IScheduledExecutionService.class);

        long now = System.currentTimeMillis();
        AnswerList<ScheduleEntry> entriesAnswer = scheduleEntryService.readAll();
        AnswerList<ScheduledExecution> historyAnswer = scheduledExecutionService.readSince(new Timestamp(now - days * 86400000L), MAX_HISTORY_ROWS);

        JSONObject jsonResponse = new JSONObject();
        JSONArray entries = new JSONArray();
        JSONArray upcoming = new JSONArray();
        JSONArray history = new JSONArray();

        if (entriesAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            List<JSONObject> upcomingList = new ArrayList<>();
            for (ScheduleEntry entry : entriesAnswer.getDataList()) {
                JSONObject jsonEntry = new JSONObject();
                jsonEntry.put("id", entry.getID());
                jsonEntry.put("type", entry.getType());
                jsonEntry.put("name", entry.getName());
                jsonEntry.put("cronDefinition", entry.getCronDefinition());
                jsonEntry.put("active", "Y".equals(entry.getActive()));
                jsonEntry.put("description", entry.getDescription());
                jsonEntry.put("usrCreated", entry.getUsrCreated());
                jsonEntry.put("dateCreated", time(entry.getDateCreated()));
                jsonEntry.put("lastExecution", time(entry.getLastExecution()));

                JSONArray nextFires = new JSONArray();
                try {
                    CronExpression cron = new CronExpression(entry.getCronDefinition());
                    Date fire = new Date(now);
                    // An inactive entry is not loaded in the scheduler: it has no upcoming run.
                    for (int i = 0; i < next && "Y".equals(entry.getActive()); i++) {
                        fire = cron.getNextValidTimeAfter(fire);
                        if (fire == null) {
                            break;
                        }
                        nextFires.put(fire.getTime());
                        JSONObject up = new JSONObject();
                        up.put("schedulerId", entry.getID());
                        up.put("name", entry.getName());
                        up.put("type", entry.getType());
                        up.put("time", fire.getTime());
                        upcomingList.add(up);
                    }
                } catch (java.text.ParseException ex) {
                    LOG.warn("Invalid cron '{}' on schedule entry {} : {}", entry.getCronDefinition(), entry.getID(), ex.getMessage());
                    jsonEntry.put("cronError", ex.getMessage());
                }
                jsonEntry.put("nextFires", nextFires);
                entries.put(jsonEntry);
            }
            upcomingList.sort(Comparator.comparingLong(o -> o.optLong("time")));
            for (JSONObject up : upcomingList) {
                upcoming.put(up);
            }
        }

        if (historyAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            for (ScheduledExecution exe : historyAnswer.getDataList()) {
                JSONObject jsonExe = new JSONObject();
                jsonExe.put("id", exe.getID());
                jsonExe.put("schedulerId", exe.getSchedulerId());
                jsonExe.put("name", exe.getScheduleName());
                jsonExe.put("status", exe.getStatus());
                jsonExe.put("comment", exe.getComment());
                jsonExe.put("scheduledDate", time(exe.getScheduledDate()));
                jsonExe.put("fireTime", time(exe.getScheduleFireTime()));
                jsonExe.put("user", exe.getUsrCreated());
                history.put(jsonExe);
            }
        }

        boolean ok = entriesAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())
                && historyAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode());
        jsonResponse.put("messageType", ok ? "OK" : "KO");
        jsonResponse.put("message", ok ? "" : (entriesAnswer.getResultMessage().getDescription() + " " + historyAnswer.getResultMessage().getDescription()).trim());
        jsonResponse.put("serverTime", now);
        jsonResponse.put("serverTimeZone", TimeZone.getDefault().getID());
        jsonResponse.put("days", days);
        jsonResponse.put("entries", entries);
        jsonResponse.put("upcoming", upcoming);
        jsonResponse.put("history", history);

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * The tables default their dates to 1970-01-01 01:01:01 when never set.
     */
    private static Object time(Timestamp timestamp) {
        if (timestamp == null || timestamp.getTime() < 86400000L * 366) {
            return JSONObject.NULL;
        }
        return timestamp.getTime();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
    }
}
