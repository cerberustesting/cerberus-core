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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.MyVersion;
import org.cerberus.crud.entity.ScheduleEntry;
import org.cerberus.crud.entity.ScheduledExecution;
import org.cerberus.crud.factory.IFactoryScheduleEntry;
import org.cerberus.crud.factory.IFactoryScheduledExecution;
import org.cerberus.crud.factory.impl.FactoryScheduledExecution;
import org.cerberus.crud.service.IMyVersionService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.util.answer.AnswerItem;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.cerberus.crud.service.IScheduleEntryService;
import org.cerberus.crud.service.IScheduledExecutionService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;

@Component
public class SchedulerInit {

    private static final Logger LOG = LogManager.getLogger(SchedulerInit.class);
    @Autowired
    private IScheduleEntryService scheduleEntryService;
    @Autowired
    private IMyVersionService MyversionService;
    @Autowired
    private IFactoryScheduleEntry factoryScheduleEntry;

    private static SchedulerFactory schFactory = new StdSchedulerFactory();
    private String instanceSchedulerVersion = "INIT";
    private boolean isRunning = false;
    Scheduler myScheduler;
    //Création du job
    JobDetail scheduledJob = JobBuilder.newJob(ScheduledJob.class).withIdentity("ScheduledJob", "group1").build();

    @PostConstruct
    public void init() {
        AnswerItem<List> ans = new AnswerItem();
        List<ScheduleEntry> listSched = new ArrayList<ScheduleEntry>();

        // read myversion scheduler_version
        MyVersion databaseSchedulerVersion;
        databaseSchedulerVersion = MyversionService.findMyVersionByKey("scheduler_version");
        LOG.debug("Current version scheduler in Cerberus : " + instanceSchedulerVersion);
        LOG.debug("Current version scheduler in DB       : " + databaseSchedulerVersion.getValueString());

        //Compare version between database and instance
        if (databaseSchedulerVersion.getValueString() == null || instanceSchedulerVersion.equalsIgnoreCase(databaseSchedulerVersion.getValueString())) {
            LOG.debug("the current version is up to date");
        } else {
            if (isRunning == false) {
                isRunning = true;
                LOG.debug("Reload Scheduler entries from database.");
                //Création d'une liste de Trigger
                Set<Trigger> myTriggersSet = new HashSet();

                try {
                    // Get all active entry of scheduleentry
                    ans = scheduleEntryService.readAllActive();
                    listSched = ans.getItem();
                    if (ans.getMessageCodeString().equalsIgnoreCase("OK")) {
                        // Browse all entry
                        for (ScheduleEntry sched : listSched) {
                            LOG.debug("Add to trigger : " + sched.getName());
                            //Get info of scheduler : cron, name, type to parameter trigger
                            String cron = sched.getCronDefinition();
                            String name = sched.getName();
                            String type = sched.getType();
                            String id = sched.getID().toString();
                            int schedulerId = sched.getID();

                            String user = "";
                            if (!StringUtil.isNullOrEmpty(sched.getUsrModif())) {
                                user = sched.getUsrModif();
                            } else {
                                user = sched.getUsrCreated();
                            }
                            //Build trigger with cron settings name and type
                            Trigger myTrigger = TriggerBuilder.newTrigger().withIdentity(id, "group1").usingJobData("schedulerId", schedulerId).usingJobData("name", name).usingJobData("type", type).usingJobData("user", user).withSchedule(CronScheduleBuilder.cronSchedule(cron).inTimeZone(TimeZone.getTimeZone("UTC+2"))).forJob(scheduledJob).build();

                            //Add trigger to list of trigger
                            myTriggersSet.add(myTrigger);
                        }

                        try {
                            //Clean old entry
                            closeScheduler();

                            //Create scheduler
                            myScheduler = schFactory.getScheduler();

                            //run scheduler
                            myScheduler.start();

                            //run job on list of trigger
                            myScheduler.scheduleJob(scheduledJob, myTriggersSet, false);
                            LOG.debug("end of Reload Scheduler entries from database.");
                            LOG.debug("update of scheduler version from : " + instanceSchedulerVersion + " to : " + databaseSchedulerVersion.getValueString());
                            instanceSchedulerVersion = databaseSchedulerVersion.getValueString();

                        } catch (Exception e) {
                            LOG.error("Failed to run scheduler Job");
                            LOG.error(e);
                        }
                    } else {
                        LOG.debug("Select new result in base not working, catch exception : " + ans.getMessageCodeString());
                    }

                } catch (Exception e) {
                    LOG.debug("Failed to load schedule entry : " + e);
                } finally {
                    isRunning = false;
                }
            } else {
                LOG.debug("Scheduler version is already in updating");
            }
        }
    }

    // 
    @PreDestroy
    public void closeScheduler() {
        try {
            LOG.debug("Removing all Schedule entries.");
            Collection<Scheduler> myCollectionScheduller = schFactory.getAllSchedulers();
            Iterator it = myCollectionScheduller.iterator();
            for (Scheduler mySched : myCollectionScheduller) {
                mySched.clear();
            }
            LOG.debug("end of Removing all Schedule entries.");
        } catch (Exception e) {
            LOG.error("Failed to clear");
            LOG.error(e);
        }
    }

}