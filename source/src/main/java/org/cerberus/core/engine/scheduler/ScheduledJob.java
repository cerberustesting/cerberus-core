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
package org.cerberus.core.engine.scheduler;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.ScheduledExecution;
import org.cerberus.core.crud.factory.IFactoryScheduledExecution;
import org.cerberus.core.crud.factory.impl.FactoryScheduledExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.IScheduleEntryService;
import org.cerberus.core.crud.service.IScheduledExecutionService;
import org.cerberus.core.crud.service.impl.ScheduledExecutionService;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Component
@Qualifier("ScheduledJob")
public class ScheduledJob implements Job {

    @Autowired
    private IScheduledExecutionService scheduledExecutionService = new ScheduledExecutionService();
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IAPIKeyService apiKeyService;
    @Autowired
    private IScheduleEntryService scheduleEntryService;

    private static final Logger LOG = LogManager.getLogger(ScheduledJob.class);
    private static IFactoryScheduledExecution factoryScheduledExecution = new FactoryScheduledExecution();
    public static final String SERVLET_ADDTOEXECUTION = "/AddToExecutionQueueV003";

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        try {
            // Get variable parameter to scheduledExecution
            Date date = new Date();
            JobDataMap dataMap = arg0.getTrigger().getJobDataMap();
            String pattern = "yyyy-MM-dd HH:mm:ss.SSSZ";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC+2"));
            String scheduleName = arg0.getTrigger().getJobDataMap().getString("name");
            String type = arg0.getTrigger().getJobDataMap().getString("type");
            String user = arg0.getTrigger().getJobDataMap().getString("user");
            long schedulerId = arg0.getTrigger().getJobDataMap().getLong("schedulerId");
            Timestamp scheduledDate = new Timestamp(arg0.getScheduledFireTime().getTime());
            Timestamp scheduleFireTime = new Timestamp(arg0.getFireTime().getTime());
            String convertDateFireTime = simpleDateFormat.format(scheduleFireTime);
            Timestamp factice = new Timestamp(System.currentTimeMillis());

            LOG.info("Job " + schedulerId + " for '" + scheduleName + "' campaign is starting.");

            try {
                ScheduledExecution scheduledExecutionObject = factoryScheduledExecution.create(1, schedulerId, scheduleName, "TOLAUNCH", "Job is created", user, "", scheduledDate, scheduleFireTime, factice, factice);
                //if execution have type campaign
                if (type.equalsIgnoreCase("CAMPAIGN")) {
                    try {
                        long createScx;
                        createScx = scheduledExecutionService.create(scheduledExecutionObject);
                        if (createScx > 0) {

                            scheduledExecutionObject.setID(createScx);

                            try {
                                CloseableHttpClient httpclient = null;
                                HttpClientBuilder httpclientBuilder;
                                httpclientBuilder = HttpClientBuilder.create();
                                httpclient = httpclientBuilder.build();
                                String request = new String();
                                String encodeName = StringUtil.encodeURL(scheduleName);
                                request = parameterService.getParameterStringByKey("cerberus_url", "", "") + SERVLET_ADDTOEXECUTION + "?campaign=" + encodeName + "&outputformat=json";
                                HttpGet requesthttp = new HttpGet(request);
                                requesthttp.setHeader("apikey", apiKeyService.getServiceAccountAPIKey());
                                HttpResponse responsehttp = httpclient.execute(requesthttp);
                                int statusCode = responsehttp.getStatusLine().getStatusCode();

                                LOG.info("Url called to trigger Campaign : '" + request + "' status code : " + statusCode);

                                if (statusCode == 200 || statusCode == 201) {

                                    HttpEntity entity = responsehttp.getEntity();

                                    if (entity != null) {
                                        scheduledExecutionObject.setStatus(ScheduledExecution.STATUS_TRIGGERED);
                                        String json_string = EntityUtils.toString(entity);
                                        try {
                                            JSONObject temp1 = new JSONObject(json_string);
                                            StringBuilder message = new StringBuilder();
                                            message.append(temp1.getString("message"));
                                            if (!StringUtil.isEmptyOrNull(temp1.getString("tag"))) {
                                                message.append(" Tag Execution : ");
                                                message.append(temp1.getString("tag"));
                                            }
                                            if (!StringUtil.isEmptyOrNull(message.toString())) {
                                                scheduledExecutionObject.setComment(message.toString());
                                            } else {
                                                scheduledExecutionObject.setComment("Campaign triggered but result got no message output");
                                            }
                                        } catch (JSONException e) {
                                            LOG.error("Failed to parse JSON from URL call. " + request);
                                            scheduledExecutionObject.setStatus(ScheduledExecution.STATUS_ERROR);
                                            scheduledExecutionObject.setComment("Campaign triggered but result was not in json format");
                                        }
                                    } else {
                                        scheduledExecutionObject.setStatus(ScheduledExecution.STATUS_TRIGGERED);
                                        scheduledExecutionObject.setComment("Campaign triggered but empty result from Service " + SERVLET_ADDTOEXECUTION);
                                    }

                                    try {
                                        Answer updateScx = new Answer();
                                        updateScx = scheduledExecutionService.update(scheduledExecutionObject);
                                        scheduleEntryService.updateLastExecution(scheduledExecutionObject.getSchedulerId(), scheduledExecutionObject.getScheduledDate());
                                    } catch (Exception e) {
                                        LOG.error("Failed to update scheduledExecution", e);
                                    }

                                } else {
                                    // Http code <> 200 and <> 201
                                    LOG.error("Request " + request + " return http" + statusCode);
                                    scheduledExecutionObject.setStatus(ScheduledExecution.STATUS_ERROR);
                                    scheduledExecutionObject.setComment("Failed to trigger Campaign. Return code : " + statusCode + " From Request : " + request);
                                    Answer updateScx = scheduledExecutionService.update(scheduledExecutionObject);
                                }

                            } catch (Exception e) {
                                LOG.error("Failed to call " + SERVLET_ADDTOEXECUTION + ", catch exception", e);
                                scheduledExecutionObject.setStatus(ScheduledExecution.STATUS_ERROR);
                                scheduledExecutionObject.setComment("Failed to trigger Campaign. Error : '" + e.getMessage());
                                Answer updateScx = scheduledExecutionService.update(scheduledExecutionObject);
                            }
                        } else {
                            LOG.info("No execution inserted for JobId " + schedulerId + " Campaign '" + scheduleName + " FireTime '" + scheduleFireTime + "' in database (Potentialy another instance of Cerberus already triggered the job).");
                        }
                    } catch (Exception e) {
                        LOG.info("Cannot insert execution for JobId " + schedulerId + " Campaign '" + scheduleName + " FireTime '" + scheduleFireTime + "' in database (Potentialy another instance of Cerberus already triggered the job).");
                    }
                }

            } catch (Exception e) {
                //Log if executionScheduled is already insert in base
                LOG.error("Cannot create object, catch exception :" + e);

            }
        } catch (Exception e) {
            LOG.error("Exception catch on Job Execution :" + e);

        }

    }
}
