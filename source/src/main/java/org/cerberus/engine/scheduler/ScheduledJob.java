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

import java.text.SimpleDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ScheduledJob implements Job {

    private static final Logger LOG = LogManager.getLogger(Job.class);

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        LOG.debug("Job is starting.");
        JobDataMap dataMap = arg0.getTrigger().getJobDataMap();
        String pattern = "yyyy-MM-dd HH:mm:ss.SSSZ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        try {
            // Procéder à l'insert en base  
            String name = arg0.getTrigger().getJobDataMap().getString("name");
            LOG.debug("Job running : " + name);
            LOG.debug(name + " getScheduledFireTime : " + simpleDateFormat.format(arg0.getScheduledFireTime()));
            LOG.debug(name + " getFireTime          : " + simpleDateFormat.format(arg0.getFireTime()));
        } catch (Exception e) {
            //Log if executionScheduled is already insert in base
            LOG.error("run job not working");

        }

    }

}
