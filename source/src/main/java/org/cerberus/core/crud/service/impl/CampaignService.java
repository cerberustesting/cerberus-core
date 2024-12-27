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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.cerberus.core.crud.dao.ICampaignDAO;
import org.cerberus.core.crud.dao.ICampaignParameterDAO;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.IEventHookService;
import org.cerberus.core.crud.service.IMyVersionService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.scheduler.SchedulerInit;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author memiks
 */
@Service
public class CampaignService implements ICampaignService {

    @Autowired
    ICampaignDAO campaignDAO;
    @Autowired
    private SchedulerInit schedulerInit;
    @Autowired
    ICampaignParameterDAO campaignParameterDAO;
    @Autowired
    IMyVersionService myVersionService;
    @Autowired
    IEventHookService eventHookService;

    @Override
    public List<CampaignParameter> findCampaignParametersByCampaignName(String campaign) throws CerberusException {
        return campaignParameterDAO.findCampaignParametersByCampaign(campaign);
    }

    @Override
    public AnswerList<Campaign> readByCriteria(int start, int amount, String colName, String dir, String searchParameter, Map<String, List<String>> individualSearch) {
        return campaignDAO.readByCriteria(start, amount, colName, dir, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem<Campaign> readByKey(String key) {
        return campaignDAO.readByKey(key);
    }

    @Override
    public AnswerItem<Campaign> readByKeyTech(int key) {
        return campaignDAO.readByKeyTech(key);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return campaignDAO.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public Answer create(Campaign object) {
        return campaignDAO.create(object);
    }

    @Override
    public Answer update(String originalCampaign, Campaign object) {
        return campaignDAO.update(originalCampaign, object);
    }

    @Override
    public Answer delete(Campaign object) {
        Answer ans = campaignDAO.delete(object);
        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            /**
             * Delete corresponding eventHook.
             */
            eventHookService.deleteBycampaign(object.getCampaign());
            /**
             * Updating Scheduler Version.
             */
            myVersionService.updateMyVersionString("scheduler_version", String.valueOf(new Date()));
            schedulerInit.init();
        }
        return ans;

    }

    @Override
    public Campaign convert(AnswerItem<Campaign> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Campaign> convert(AnswerList<Campaign> answerList) throws CerberusException {
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

}
