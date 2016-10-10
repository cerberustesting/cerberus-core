/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.crud.service.impl;

import org.cerberus.crud.dao.ICampaignContentDAO;
import org.cerberus.crud.entity.CampaignContent;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.service.ICampaignContentService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cerberus
 */
@Service
public class CampaignContentService implements ICampaignContentService {

    @Autowired
    ICampaignContentDAO campaignContentDAO;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CampaignContentService.class);

    @Override
    public AnswerList readByCampaignByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return campaignContentDAO.readByCampaignByCriteria(campaign, startPosition, length, columnName, sort, searchParameter, string);
    }

    @Override
    public AnswerList readByCampaign(String campaign) {
        return campaignContentDAO.readByCampaign(campaign);
    }

    @Override
    public AnswerItem<CampaignContent> findCampaignContentByKey(int key) {
        return campaignContentDAO.readByKey(key);
    }

    @Override
    public Answer deleteByCampaign(String key) {
        return campaignContentDAO.deleteByCampaign(key);
    }

    @Override
    public Answer delete(CampaignContent object) {
        return campaignContentDAO.delete(object);
    }

    @Override
    public Answer update(CampaignContent object) {
        return campaignContentDAO.update(object);
    }

    @Override
    public Answer create(CampaignContent object) {
        return campaignContentDAO.create(object);
    }

    @Override
    public Answer createList(List<CampaignContent> objectList) {
        Answer ans = new Answer(null);
        for (CampaignContent objectToCreate : objectList) {
            ans = campaignContentDAO.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<CampaignContent> objectList) {
        Answer ans = new Answer(null);
        for (CampaignContent objectToCreate : objectList) {
            ans = campaignContentDAO.delete(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String campaign, List<CampaignContent> newList) {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<CampaignContent> oldList = new ArrayList();
        try {
            oldList = this.convert(this.readByCampaign(campaign));
        } catch (CerberusException ex) {
            LOG.error(ex);
        }

        /**
         * Iterate on (TestCaseStep From Page - TestCaseStep From Database) If
         * TestCaseStep in Database has same key : Update and remove from the
         * list. If TestCaseStep in database does ot exist : Insert it.
         */
        List<CampaignContent> listToUpdateOrInsert = new ArrayList(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<CampaignContent> listToUpdateOrInsertToIterate = new ArrayList(listToUpdateOrInsert);

        for (CampaignContent objectDifference : listToUpdateOrInsertToIterate) {
            for (CampaignContent objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    /*ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);*/
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }

        /**
         * Iterate on (TestCaseStep From Database - TestCaseStep From Page). If
         * TestCaseStep in Page has same key : remove from the list. Then delete
         * the list of TestCaseStep
         */
        List<CampaignContent> listToDelete = new ArrayList(oldList);
        listToDelete.removeAll(newList);
        List<CampaignContent> listToDeleteToIterate = new ArrayList(listToDelete);

        for (CampaignContent tcsDifference : listToDeleteToIterate) {
            for (CampaignContent tcsInPage : newList) {
                if (tcsDifference.hasSameKey(tcsInPage)) {
                    listToDelete.remove(tcsDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }
        return finalAnswer;
    }

    @Override
    public CampaignContent convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (CampaignContent) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<CampaignContent> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<CampaignContent>) answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

}
