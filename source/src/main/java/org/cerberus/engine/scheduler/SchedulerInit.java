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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.MyVersion;
import org.cerberus.crud.service.IMyVersionService;
import org.cerberus.crud.service.IParameterService;
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

@Component
public class SchedulerInit {

    private static final Logger LOG = LogManager.getLogger(Scheduler.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IMyVersionService MyversionService;
    private static SchedulerFactory schFactory = new StdSchedulerFactory();
    private String schedulerVersion = "INIT";
    private boolean isRunning = false;
    Scheduler myScheduler;

    //Création du jon
    JobDetail scheduledJob = JobBuilder.newJob(ScheduledJob.class).withIdentity("ScheduledJob", "group1").build();

    @PostConstruct
    public void init() {
        LOG.debug("Reload Scheduler entries from database.");

        // read myversion scheduler_version
        MyVersion MVersion;
        MVersion = MyversionService.findMyVersionByKey("scheduler_version");
        LOG.debug("Current version of scheduler in Cerberus: " + schedulerVersion);
        LOG.debug("Current version of scheduler in DB: " + MVersion.getValueString());

        if (schedulerVersion.equalsIgnoreCase(MVersion.getValueString())) {
            LOG.debug("the current version is up to date");
        } else {
            if (isRunning == false) {
                isRunning = true;

                //Création d'une liste de Trigger
                Set<Trigger> myTriggersSet = new HashSet();

                //récupération des expression CRON
                // Load + Boucle sur ScheduledEntry table active = Y
                String cronFirst = parameterService.getParameterStringByKey("cron_expression_first", "", "");
                String cronSecond = parameterService.getParameterStringByKey("cron_expression_second", "", "");

                //String myFirstDesc = parameterService.findParameterByKey("cron_expression_first", "CRON").getDescription();
                //Création du Job
                //JobDetail myJob = JobBuilder.newJob(myInstructions.class).withIdentity("myJob1", "group1").build();
                //Création des Triggers
                Trigger myTrigger = TriggerBuilder.newTrigger().withIdentity("ID1", "group1").usingJobData("name", "First Job").withSchedule(CronScheduleBuilder.cronSchedule(cronFirst)).forJob(scheduledJob).build();
                Trigger myTriggerTwo = TriggerBuilder.newTrigger().withIdentity("ID2", "group1").usingJobData("name", "Second Job").withSchedule(CronScheduleBuilder.cronSchedule(cronSecond)).forJob(scheduledJob).build();

                //Ajouter mes Triggers dans la liste Set
                myTriggersSet.add(myTrigger);
                myTriggersSet.add(myTriggerTwo);

                try {
                    //Nettoyage des anciennes entree
                    closeScheduler();

                    //Creation du scheduler
                    myScheduler = schFactory.getScheduler();

                    //Lancement du scheduler
                    myScheduler.start();

                    //Lancement du job avec la liste de trigger
                    myScheduler.scheduleJob(scheduledJob, myTriggersSet, false);
                    LOG.debug("end of Reload Scheduler entries from database.");

                    LOG.debug("update of scheduler version from : " + schedulerVersion + " to : " + MVersion.getValueString());
                    schedulerVersion = MVersion.getValueString();

                } catch (Exception e) {
                    LOG.error("Failed to run scheduler Job");
                    LOG.error(e);
                }
                isRunning = false;
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
