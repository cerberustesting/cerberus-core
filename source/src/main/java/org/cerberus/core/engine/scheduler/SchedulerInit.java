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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.MyVersion;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.crud.factory.IFactoryScheduleEntry;
import org.cerberus.core.crud.service.IMyVersionService;
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
import org.cerberus.core.crud.service.IScheduleEntryService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;

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
    private Set<Trigger> myTriggersSet = new HashSet<>();

    public void setMyTriggersSet(Set<Trigger> myTriggersSet) {
        this.myTriggersSet = myTriggersSet;
    }

    public Set<Trigger> getMyTriggersSet() {
        return myTriggersSet;
    }

    public String getInstanceSchedulerVersion() {
        return instanceSchedulerVersion;
    }

    public void setInstanceSchedulerVersion(String version) {
        this.instanceSchedulerVersion = version;
    }

    public boolean isIsRunning() {
        return isRunning;
    }

    @PostConstruct
    public void init() {
        try {
            AnswerList<ScheduleEntry> ans = new AnswerList<>();
            List<ScheduleEntry> listSched = new ArrayList<>();

            // read myversion scheduler_version
            MyVersion databaseSchedulerVersion;
            try {
                databaseSchedulerVersion = MyversionService.findMyVersionByKey("scheduler_version");
                LOG.debug("Instance Quartz User scheduler version : " + instanceSchedulerVersion + " / DB scheduler version : " + databaseSchedulerVersion.getValueString());

                //Compare version between database and instance
                if (databaseSchedulerVersion.getValueString() == null || instanceSchedulerVersion.equalsIgnoreCase(databaseSchedulerVersion.getValueString())) {
                    LOG.debug("Instance Quartz User scheduler version is up to date.");
                } else {
                    if (isRunning == false) {
                        isRunning = true;
                        LOG.info("Start of Reload Quartz User Scheduler entries from database.");
                        Set<Trigger> myTriggersSetList = new HashSet<>();

                        try {
                            // Get all active entry of scheduleentry
                            ans = scheduleEntryService.readAllActive();
                            listSched = ans.getDataList();
                            if (ans.getMessageCodeString().equalsIgnoreCase("OK")) {
                                // Browse all entry
                                for (ScheduleEntry sched : listSched) {
                                    LOG.debug("Add to trigger : " + sched.getName());
                                    //Get info of scheduler : cron, name, type to parameter trigger
                                    String cron = sched.getCronDefinition();
                                    String name = sched.getName();
                                    String type = sched.getType();
                                    String id = String.valueOf(sched.getID());
                                    long schedulerId = sched.getID();

                                    String user = "";
                                    if (!StringUtil.isEmptyOrNull(sched.getUsrModif())) {
                                        user = sched.getUsrModif();
                                    } else {
                                        user = sched.getUsrCreated();
                                    }
                                    //Build trigger with cron settings name and type
                                    Trigger myTrigger = TriggerBuilder.newTrigger()
                                            .withIdentity(id, "group1")
                                            .usingJobData("schedulerId", schedulerId)
                                            .usingJobData("name", name)
                                            .usingJobData("type", type)
                                            .usingJobData("user", user)
                                            .usingJobData("cronDefinition", cron)
                                            .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                                            //                                                    .inTimeZone(TimeZone.getTimeZone("UTC+2"))
                                            )
                                            .forJob(scheduledJob).build();

                                    //Add trigger to list of trigger
                                    myTriggersSetList.add(myTrigger);
                                }
                                this.setMyTriggersSet(myTriggersSetList);
                                try {
                                    //Clean old entry
                                    closeScheduler();

                                    //Create scheduler
                                    myScheduler = schFactory.getScheduler();

                                    //run scheduler
                                    myScheduler.start();

                                    //run job on list of trigger
                                    myScheduler.scheduleJob(scheduledJob, myTriggersSetList, false);
                                    LOG.info("End of Reload Scheduler entries from database.");
                                    LOG.info("Scheduler version updated from : " + instanceSchedulerVersion + " to : " + databaseSchedulerVersion.getValueString());
                                    instanceSchedulerVersion = databaseSchedulerVersion.getValueString();

                                } catch (Exception e) {
                                    LOG.error("Failed to load Quartz User scheduler table. " + myTriggersSetList);
                                    LOG.error(e);
                                }
                            } else {
                                LOG.debug("Select new result in base not working, catch exception : " + ans.getMessageCodeString());
                            }

                        } catch (Exception e) {
                            LOG.debug("Failed to load Quartz User schedule entry : " + e);
                        } finally {
                            isRunning = false;
                        }
                    } else {
                        LOG.debug("Quartz User Scheduler version is already in updating");
                    }
                }
            } catch (Exception e) {
                LOG.debug("failed to launch Quartz User scheduler init :" + e);
            }
        } catch (Exception e) {
            LOG.debug("Execption : ", e);
        }
    }

    // 
    @PreDestroy
    public void closeScheduler() {
        try {
            LOG.info("Removing all Quartz User Schedule entries.");
            Collection<Scheduler> myCollectionScheduller = schFactory.getAllSchedulers();
            Iterator it = myCollectionScheduller.iterator();
            for (Scheduler mySched : myCollectionScheduller) {
                mySched.clear();
                mySched.shutdown(true);
            }
            LOG.info("end of Removing all Quartz User Schedule entries.");
        } catch (Exception e) {
            LOG.error("Failed to clear Quartz User Scheduler entries.");
            LOG.error(e);
        }
    }

}
