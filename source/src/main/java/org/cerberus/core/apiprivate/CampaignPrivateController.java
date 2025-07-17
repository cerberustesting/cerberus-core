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
package org.cerberus.core.apiprivate;

import io.swagger.v3.oas.annotations.Operation;
import org.cerberus.core.util.datatable.DataTableInformation;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.factory.IFactoryLogEvent;
import org.cerberus.core.crud.factory.IFactoryTest;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.scheduler.SchedulerInit;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.servlet.information.ReadCerberusDetailInformation;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author bcivel
 */
@RestController
@RequestMapping("/campaigns")
public class CampaignPrivateController {

    private static final Logger LOG = LogManager.getLogger(CampaignPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Autowired
    TestCaseExecutionService testCaseExecutionService;
    @Autowired
    ITestService testService;
    @Autowired
    IFactoryTest factoryTest;
    @Autowired
    ILogEventService logEventService;
    @Autowired
    IFactoryLogEvent factoryLogEvent;
    @Autowired
    IParameterService parameterService;
    @Autowired
    SchedulerInit scInit;

    /**
     * Read By Key
     *
     * @param request
     * @param test
     * @return
     */
    @Operation(hidden=true)
    @GetMapping("/scheduled")
    public String readByKey(HttpServletRequest request, String test) {

        JSONObject object = new JSONObject();

        try {
            // Calling Servlet Transversal Util.
            ServletUtil.servletStart(request);

            if (scInit != null) {
                object.put("schedulerInstanceVersion", scInit.getInstanceSchedulerVersion());
                object.put("schedulerReloadIsRunning", scInit.isIsRunning());
                // We get here the list of triggers of Quartz scheduler.
                List<JSONObject> triggerList = new ArrayList<>();
                for (Trigger triggerSet : scInit.getMyTriggersSet()) {

                    Date now = new Date();
                    Date nextTrigger = triggerSet.getFireTimeAfter(new Date());

                    for (int i = 0; i < 10; i++) {

                        JSONObject objectTrig = new JSONObject();
                        objectTrig.put("triggerId", triggerSet.getJobDataMap().getLong("schedulerId"));
                        objectTrig.put("triggerName", triggerSet.getJobDataMap().getString("name"));
                        objectTrig.put("triggerType", triggerSet.getJobDataMap().getString("type"));
                        objectTrig.put("triggerUserCreated", triggerSet.getJobDataMap().getString("user"));
                        objectTrig.put("triggerNextFiretime", nextTrigger);
                        objectTrig.put("triggerNextFiretimeTimestamp", new SimpleDateFormat(DATE_FORMAT).format(nextTrigger));
                        objectTrig.put("triggerNextFiretimeDurationToTriggerInMs", Math.abs(nextTrigger.getTime() - now.getTime()));
                        objectTrig.put("triggerCronDefinition", triggerSet.getJobDataMap().getString("cronDefinition"));
                        triggerList.add(objectTrig);

                        nextTrigger = triggerSet.getFireTimeAfter(nextTrigger);

                    }
                }
                Collections.sort(triggerList, new SortTriggers());
                JSONArray object1 = new JSONArray(triggerList);
                object.put("schedulerTriggers", object1);
                Date now = new Date();
                object.put("serverDate", new SimpleDateFormat(DATE_FORMAT).format(now));
                object.put("serverTimeZone", TimeZone.getDefault().getDisplayName());
            }

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();

    }

    class SortTriggers implements Comparator<JSONObject> {

        // Used for sorting Triggers 
        @Override
        public int compare(JSONObject a, JSONObject b) {

            if (a != null && b != null) {
                Date dateA;
                Date dateB;
                try {
                    dateA = (Date) a.get("triggerNextFiretime");
                    dateB = (Date) b.get("triggerNextFiretime");
                    if (dateA.equals(dateB)) {

                    } else {
                        return (dateA.compareTo(dateB));
                    }
                } catch (JSONException ex) {
                    LOG.error("Exception on JSON Parse.", ex);
                } catch (ClassCastException ex) {
                    return 1;
                }

            } else {
                return 1;
            }

            return 1;
        }
    }
}
