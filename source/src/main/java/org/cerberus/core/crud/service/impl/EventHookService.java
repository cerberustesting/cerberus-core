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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IEventHookDAO;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.service.IEventHookService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
 */
@Service
public class EventHookService implements IEventHookService {

    @Autowired
    private IEventHookDAO eventHookDAO;

    private static final Logger LOG = LogManager.getLogger("EventHookService");

    private final String OBJECT_NAME = "EventHook";

    @Override
    public AnswerItem<EventHook> readByKey(Integer id) {
        return eventHookDAO.readByKey(id);
    }

    @Override
    public AnswerList<EventHook> readByEventReference(List<String> eventReference) {
        return eventHookDAO.readByEventReferenceByCriteria(eventReference, null, false, 0, 0, "eventreference", "asc", null, null);
    }

    @Override
    public AnswerList<EventHook> readByEventReference(List<String> eventReference, List<String> objectKey1) {
        return eventHookDAO.readByEventReferenceByCriteria(eventReference, objectKey1, false, 0, 0, "eventreference", "asc", null, null);
    }

    @Override
    public AnswerList<EventHook> readByCampaign(String campaign) {
        List<String> evtList = new ArrayList<>(Arrays.asList(EventHook.EVENTREFERENCE_CAMPAIGN_START, EventHook.EVENTREFERENCE_CAMPAIGN_END, EventHook.EVENTREFERENCE_CAMPAIGN_END_CIKO));
        List<String> obj1List = new ArrayList<>(Arrays.asList(campaign));
        return eventHookDAO.readByEventReferenceByCriteria(evtList, obj1List, false, 0, 0, "eventreference", "asc", null, null);
    }

    @Override
    public AnswerList<EventHook> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return eventHookDAO.readByEventReferenceByCriteria(null, null, false, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<EventHook> readByEventReferenceByCriteria(List<String> eventReference, boolean activeOnly, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return eventHookDAO.readByEventReferenceByCriteria(eventReference, null, activeOnly, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public boolean exist(Integer id) {
        AnswerItem objectAnswer = readByKey(id);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(EventHook object) {
        return eventHookDAO.create(object);
    }

    @Override
    public Answer delete(EventHook object) {
        return eventHookDAO.delete(object);
    }

    @Override
    public Answer deleteBycampaign(String campaign) {
        Answer ans = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
        try {
            for (EventHook object : convert(readByCampaign(campaign))) {
                ans = eventHookDAO.delete(object);
            }
            return ans;
        } catch (CerberusException ex) {
            LOG.error(ex.toString(), ex);
        }
        return ans;
    }

    @Override
    public Answer update(EventHook object) {
        return eventHookDAO.update(object);
    }

    @Override
    public EventHook convert(AnswerItem<EventHook> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<EventHook> convert(AnswerList<EventHook> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return eventHookDAO.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }

    @Override
    public Answer deleteList(List<EventHook> objectList) {
        Answer ans = new Answer(null);
        for (EventHook objectToDelete : objectList) {
            ans = eventHookDAO.delete(objectToDelete);
        }
        return ans;
    }

    @Override
    public Answer createList(List<EventHook> objectList) {
        Answer ans = new Answer(null);
        boolean changed = false;
        if (objectList.isEmpty()) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unvalid SchedulerEntry data"));
            ans.setResultMessage(msg);
            return ans;
        } else {
            for (EventHook objectToCreate : objectList) {

                ans = eventHookDAO.create(objectToCreate);

            }
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String campaign, List<EventHook> newList) {
        Answer ans = new Answer();

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<EventHook> oldList = new ArrayList<>();
        oldList = readByCampaign(campaign).getDataList();
        List<EventHook> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<EventHook> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        /**
         * Update and Create all objects database Objects from newList
         */
        for (EventHook objectDifference : listToUpdateOrInsertToIterate) {
            for (EventHook objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    LOG.debug(objectDifference);
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
        List<EventHook> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<EventHook> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (EventHook scheDifference : listToDeleteToIterate) {
            for (EventHook scheInPage : newList) {
                if (scheDifference.hasSameKey(scheInPage)) {
                    listToDelete.remove(scheDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        // We insert only at the end (after deletion of all potencial enreg)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        return finalAnswer;
    }

}
