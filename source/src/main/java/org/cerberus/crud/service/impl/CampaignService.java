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
package org.cerberus.crud.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.cerberus.crud.dao.ICampaignDAO;
import org.cerberus.crud.dao.ICampaignParameterDAO;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.IMyVersionService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.scheduler.SchedulerInit;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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
    public Answer update(Campaign object) {
        return campaignDAO.update(object);
    }

    @Override
    public Answer delete(Campaign object) {
        Answer ans = campaignDAO.delete(object);
        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
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
            return (Campaign) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Campaign> convert(AnswerList<Campaign> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<Campaign>) answerList.getDataList();
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
