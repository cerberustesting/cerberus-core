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

import org.cerberus.core.crud.dao.ICampaignParameterDAO;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cerberus
 */
@Service
public class CampaignParameterService implements ICampaignParameterService {

    @Autowired
    ICampaignParameterDAO campaignParameterDAO;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(CampaignParameterService.class);

    @Override
    public AnswerList<CampaignParameter> readByCampaignByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return campaignParameterDAO.readByCampaignByCriteria(campaign, startPosition, length, columnName, sort, searchParameter, string);
    }

    @Override
    public AnswerList<CampaignParameter> readByCampaign(String campaign) {
        return campaignParameterDAO.readByCampaign(campaign);
    }

    @Override
    public AnswerItem<Map<String, List<String>>> parseParametersByCampaign(final String campaignName) {
        final AnswerList<CampaignParameter> campaignParameters = readByCampaign(campaignName);
        if (!campaignParameters.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return new AnswerItem<>(campaignParameters.getResultMessage());
        }
        final Map<String, List<String>> sortedCampaignParameters = new HashMap<>();
        for (final CampaignParameter campaignParameter : campaignParameters.getDataList()) {
            if (!sortedCampaignParameters.containsKey(campaignParameter.getParameter())) {
                sortedCampaignParameters.put(campaignParameter.getParameter(), new ArrayList<>());
            }
            sortedCampaignParameters.get(campaignParameter.getParameter()).add(campaignParameter.getValue());
        }
        return new AnswerItem<>(sortedCampaignParameters, new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
    }

    @Override
    public Answer deleteByCampaign(String key) {
        return campaignParameterDAO.deleteByCampaign(key);
    }

    @Override
    public Answer delete(CampaignParameter object) {
        return campaignParameterDAO.delete(object);
    }

    @Override
    public Answer update(CampaignParameter object) {
        return campaignParameterDAO.update(object);
    }

    @Override
    public Answer create(CampaignParameter object) {
        return campaignParameterDAO.create(object);
    }

    @Override
    public Answer createList(List<CampaignParameter> objectList) {
        Answer ans = new Answer(null);
        for (CampaignParameter objectToCreate : objectList) {
            ans = campaignParameterDAO.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<CampaignParameter> objectList) {
        Answer ans = new Answer(null);
        for (CampaignParameter objectToCreate : objectList) {
            ans = campaignParameterDAO.delete(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String campaign, List<CampaignParameter> newList) {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<CampaignParameter> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByCampaign(campaign));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }

        /**
         * Iterate on (TestCaseStep From Page - TestCaseStep From Database) If
         * TestCaseStep in Database has same key : Update and remove from the
         * list. If TestCaseStep in database does ot exist : Insert it.
         */
        List<CampaignParameter> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<CampaignParameter> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        for (CampaignParameter objectDifference : listToUpdateOrInsertToIterate) {
            for (CampaignParameter objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    /*ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);*/
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }
        
        /**
         * Iterate on (TestCaseStep From Database - TestCaseStep From Page). If
         * TestCaseStep in Page has same key : remove from the list. Then delete
         * the list of TestCaseStep
         */
        List<CampaignParameter> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<CampaignParameter> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (CampaignParameter tcsDifference : listToDeleteToIterate) {
            for (CampaignParameter tcsInPage : newList) {
                if (tcsDifference.hasSameKey(tcsInPage)) {
                    listToDelete.remove(tcsDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }
        return finalAnswer;
    }

    @Override
    public CampaignParameter convert(AnswerItem<CampaignParameter> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<CampaignParameter> convert(AnswerList<CampaignParameter> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

}
