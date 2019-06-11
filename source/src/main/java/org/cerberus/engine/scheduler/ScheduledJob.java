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
package org.cerberus.engine.scheduler;

import java.io.InputStream;
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
import org.cerberus.crud.entity.ScheduledExecution;
import org.cerberus.crud.factory.IFactoryScheduledExecution;
import org.cerberus.crud.factory.impl.FactoryScheduledExecution;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.IScheduledExecutionService;
import org.cerberus.crud.service.impl.ScheduledExecutionService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
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
    private static final Logger LOG = LogManager.getLogger(ScheduledJob.class);
    private static IFactoryScheduledExecution factoryScheduledExecution = new FactoryScheduledExecution();

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOG.debug("Job is starting.");

        // Get variable parameter to scheduledExecution
        Date date = new Date();
        JobDataMap dataMap = arg0.getTrigger().getJobDataMap();
        String pattern = "yyyy-MM-dd HH:mm:ss.SSSZ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC+2"));
        String scheduleName = arg0.getTrigger().getJobDataMap().getString("name");
        String type = arg0.getTrigger().getJobDataMap().getString("type");
        String user = arg0.getTrigger().getJobDataMap().getString("user");
        int schedulerId = arg0.getTrigger().getJobDataMap().getInt("schedulerId");
        Timestamp scheduledDate = new Timestamp(arg0.getScheduledFireTime().getTime());
        Timestamp scheduleFireTime = new Timestamp(arg0.getFireTime().getTime());
        String convertDateFireTime = simpleDateFormat.format(scheduleFireTime);
        LOG.debug("Convert date : " + convertDateFireTime);
        Timestamp factice = new Timestamp(System.currentTimeMillis());
        try {
            LOG.debug(scheduledExecutionService.toString());
        } catch (Exception e) {
            LOG.debug(e);
        }
        try {
            ScheduledExecution scheduledExecutionObject = factoryScheduledExecution.create(1, schedulerId, scheduleName, "TOLAUNCH", "Job is created", user, "", scheduledDate, scheduleFireTime, factice, factice);
            LOG.debug(scheduledExecutionObject.getScheduleName());
            //if execution have type campaign
            if (type.equalsIgnoreCase("CAMPAIGN")) {
                try {
                    AnswerItem<Integer> createScx = new AnswerItem();
                    createScx = scheduledExecutionService.create(scheduledExecutionObject);
                    LOG.debug("Insert message : " + createScx.getMessageDescription());
                    try {
                        CloseableHttpClient httpclient = null;
                        HttpClientBuilder httpclientBuilder;
                        httpclientBuilder = HttpClientBuilder.create();
                        httpclient = httpclientBuilder.build();
                        String request = new String();
                        request = parameterService.getParameterStringByKey("cerberus_url", "", "") + "/AddToExecutionQueueV003?campaign=" + scheduleName + "&outputformat=json";
                        HttpGet requesthttp = new HttpGet(request);
                        LOG.debug(request);
                        HttpResponse responsehttp = httpclient.execute(requesthttp);
                        int statusCode = responsehttp.getStatusLine().getStatusCode();

                        LOG.debug("Execution statut : " + statusCode);
                        try {
                            if (statusCode == 200 || statusCode == 201) {
                                scheduledExecutionObject.setStatus("TRIGGERED");

                            }
                            //= Set comment from servlet
                            if ((statusCode != 200) && (statusCode != 201)) {
                                scheduledExecutionObject.setStatus("ERROR");
                                LOG.debug("ERROR HTTP Response " + statusCode);
                                LOG.debug("Sent request : " + request);
                            }

                            HttpEntity entity = responsehttp.getEntity();
                            if (entity != null) {
                                String json_string = EntityUtils.toString(entity);
                                JSONObject temp1 = new JSONObject(json_string);
                                StringBuilder message = new StringBuilder();
                                message.append(temp1.getString("message"));
                                if (!StringUtil.isNullOrEmpty(temp1.getString("tag"))) {
                                    message.append(" Tag Execution : ");
                                    message.append(temp1.getString("tag"));
                                }
                                if (!StringUtil.isNullOrEmpty(message.toString())) {
                                    scheduledExecutionObject.setComment(message.toString());
                                } else {
                                    LOG.debug("Failed to catch message from servlet response");
                                    scheduledExecutionObject.setComment("Empty comment, probably due to bug in execution submission");
                                }
                                LOG.debug(scheduledExecutionObject.getComment());
                            }
                            scheduledExecutionObject.setID(createScx.getItem());
                            LOG.debug("Id of scheduledExecution object : " + scheduledExecutionObject.getID());
                            Answer updateScx = new Answer();
                            updateScx = scheduledExecutionService.update(scheduledExecutionObject);
                            LOG.debug(updateScx.getMessageDescription());
                        } catch (Exception e) {
                            LOG.debug("Failed to update scheduledExecution", e);
                        }

                    } catch (Exception e) {
                        LOG.debug("Failed to call AddToExecutionQueueV003, catch exception", e);
                    }
                    try {
                        // Insert in queue 
                        LOG.debug("Job running : " + scheduleName);
                    } catch (Exception e) {
                        LOG.error("Run job not working, catch exception :", e);
                    }
                } catch (Exception e) {
                    LOG.debug("Cannot insert execution in database (Potentialy another instance of Cerberus already triggered the job), catch exception :", e);
                    scheduledExecutionObject.setStatus("IGNORED");
                    //AnswerItem updateScx = scheduledExecutionService.update(scheduledExecutionObject);

                }
            }

        } catch (Exception e) {
            //Log if executionScheduled is already insert in base
            LOG.debug("Cannot create object, catch exception :" + e);

        }

    }
}
