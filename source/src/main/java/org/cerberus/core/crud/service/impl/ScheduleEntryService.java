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
package org.cerberus.core.crud.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.crud.dao.IScheduleEntryDAO;
import org.cerberus.core.crud.service.IMyVersionService;
import org.cerberus.core.crud.service.IScheduleEntryService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.scheduler.SchedulerInit;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;

/**
 *
 * @author cdelage
 */
@Service
public class ScheduleEntryService implements IScheduleEntryService {

    @Autowired
    IScheduleEntryDAO schedulerDao;
    @Autowired
    private SchedulerInit schedulerInit;
    @Autowired
    private IMyVersionService myVersionService;

    private static final Logger LOG = LogManager.getLogger(ScheduleEntryService.class);

    @Override
    public AnswerItem<ScheduleEntry> readbykey(long id) {
        AnswerItem<ScheduleEntry> ans = new AnswerItem<>();
        ans = schedulerDao.readByKey(id);
        return ans;
    }

    @Override
    public AnswerList<ScheduleEntry> readAllActive() {
        AnswerList<ScheduleEntry> ans = new AnswerList<>();
        ans = schedulerDao.readAllActive();
        return ans;
    }

    @Override
    public Answer create(ScheduleEntry scheduleentry) {
        Answer response = new Answer();
        response = schedulerDao.create(scheduleentry);
        return response;
    }

    @Override
    public Answer update(ScheduleEntry scheduleentry) {
        Answer response = new Answer();
        Boolean validCron = true;
        if (!scheduleentry.getCronDefinition().isEmpty()) {
            validCron = org.quartz.CronExpression.isValidExpression(scheduleentry.getCronDefinition());
        }
        if (!validCron) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "'" + scheduleentry.getCronDefinition() + "' is not in a valid Quartz cron expression."));
            response.setResultMessage(msg);
        } else if (scheduleentry.getCronDefinition().isEmpty()) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Cron definition is empty"));
            response.setResultMessage(msg);
        } else if (scheduleentry.getName().isEmpty()) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Name of scheduledcampaign is empty"));
            response.setResultMessage(msg);
        } else {
            response = schedulerDao.update(scheduleentry);
        }

        return response;
    }

    @Override
    public Answer delete(ScheduleEntry object) {
        Answer response = new Answer();
        response = schedulerDao.delete(object);
        return response;
    }

    @Override
    public AnswerList<ScheduleEntry> readByName(String name) {
        AnswerList<ScheduleEntry> response = new AnswerList<>();
        response = schedulerDao.readByName(name);
        return response;
    }

    @Override
    public Answer deleteListSched(List<ScheduleEntry> objectList) {
        Answer ans = new Answer(null);
        for (ScheduleEntry objectToDelete : objectList) {
            ans = schedulerDao.delete(objectToDelete);
        }
        return ans;
    }

    @Override
    public Answer deleteByCampaignName(String name) {
        Answer ans = new Answer(null);
        List<ScheduleEntry> objectList = new ArrayList<>();
        objectList = this.readByName(name).getDataList();
        for (ScheduleEntry objectToDelete : objectList) {
            ans = this.delete(objectToDelete);
            if (!ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                return ans;
            }
        }
        return ans;
    }

    @Override
    public Answer createListSched(List<ScheduleEntry> objectList) {
        Answer ans = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        boolean changed = false;
        if (objectList.isEmpty()) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "No data to create."));
            ans.setResultMessage(msg);
            return ans;
        } else {
            for (ScheduleEntry objectToCreate : objectList) {
                Boolean validCron = org.quartz.CronExpression.isValidExpression(objectToCreate.getCronDefinition());

                if (!validCron) {
                    MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "'" + objectToCreate.getCronDefinition() + "' is not in a valid Quartz cron expression."));
                    ans.setResultMessage(msg);
                } else if (objectToCreate.getCronDefinition().isEmpty()) {
                    MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Cron definition is empty"));
                    ans.setResultMessage(msg);
                } else if (objectToCreate.getName().isEmpty()) {
                    MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Name of scheduledcampaign is empty"));
                    ans.setResultMessage(msg);
                } else {
                    ans = schedulerDao.create(objectToCreate);
                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Updating Scheduler Version.
                         */
                        myVersionService.updateMyVersionString("scheduler_version", String.valueOf(new Date()));
                        changed = true;
                    }

                }
            }
            if (changed) {
                // Reload Cheduler Version.
                schedulerInit.init();
            }
        }
        return ans;
    }

    @Override
    public Answer compareSchedListAndUpdateInsertDeleteElements(String campaign, List<ScheduleEntry> newList) {
        Answer ans = new Answer();

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        boolean scheduledChanged = false;

        List<ScheduleEntry> oldList = new ArrayList<>();
        oldList = schedulerDao.readByName(campaign).getDataList();
        List<ScheduleEntry> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<ScheduleEntry> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        /**
         * Update and Create all objects database Objects from newList
         */
        for (ScheduleEntry objectDifference : listToUpdateOrInsertToIterate) {
            for (ScheduleEntry objectInDatabase : oldList) {
                if (objectDifference.schedHasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    scheduledChanged = true;
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
        List<ScheduleEntry> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<ScheduleEntry> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (ScheduleEntry scheDifference : listToDeleteToIterate) {
            for (ScheduleEntry scheInPage : newList) {
                if (scheDifference.schedHasSameKey(scheInPage)) {
                    listToDelete.remove(scheDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteListSched(listToDelete);
            scheduledChanged = true;
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        // We insert only at the end (after deletion of all potencial enreg)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createListSched(listToUpdateOrInsert);
            scheduledChanged = true;
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        if (finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && scheduledChanged) {
            myVersionService.updateMyVersionString("scheduler_version", String.valueOf(new Date()));
            schedulerInit.init();
        }

        return finalAnswer;
    }

    @Override
    public Answer updateLastExecution(long schedulerId, Timestamp lastExecution) {
        Answer ans = new Answer();
        ans = schedulerDao.updateLastExecution(schedulerId, lastExecution);
        return ans;
    }

}
