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

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IAppServiceContentDAO;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.service.IAppServiceContentService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bcivel
 */
@AllArgsConstructor
@Service
public class AppServiceContentService implements IAppServiceContentService {

    private final IAppServiceContentDAO appServiceContentDAO;

    private static final Logger LOG = LogManager.getLogger(AppServiceContentService.class);

    private final String OBJECT_NAME = "Service Content";

    @Override
    public AnswerItem<AppServiceContent> readByKey(String service, String key) {
        return appServiceContentDAO.readByKey(service, key);
    }

    @Override
    public AnswerList<AppServiceContent> readAll() {
        return readByServiceByCriteria(null, false, false, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList<AppServiceContent> readByVarious(String service) {
        return appServiceContentDAO.readByVariousByCriteria(service, false, false, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList<AppServiceContent> readByVarious(String service, boolean isActive) {
        return appServiceContentDAO.readByVariousByCriteria(service, true, isActive, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList<AppServiceContent> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return appServiceContentDAO.readByVariousByCriteria(null, false, false, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<AppServiceContent> readByServiceByCriteria(String service, boolean withActiveCriteria, boolean isActive, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return appServiceContentDAO.readByVariousByCriteria(service, withActiveCriteria, isActive, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public boolean exist(String service, String key) {
        AnswerItem<AppServiceContent> objectAnswer = readByKey(service, key);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successful and object was found.
    }

    @Override
    public Answer create(AppServiceContent object) {
        return appServiceContentDAO.create(object);
    }

    @Override
    public Answer createList(List<AppServiceContent> objectList) {
        Answer ans = new Answer(null);
        for (AppServiceContent objectToCreate : objectList) {
            ans = this.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer delete(AppServiceContent object) {
        return appServiceContentDAO.delete(object);
    }

    @Override
    public Answer deleteList(List<AppServiceContent> objectList) {
        Answer ans = new Answer(null);
        for (AppServiceContent objectToDelete : objectList) {
            ans = this.delete(objectToDelete);
        }
        return ans;
    }

    @Override
    public Answer update(String service, String key, AppServiceContent object) {
        return appServiceContentDAO.update(service, key, object);
    }

    @Override
    public AppServiceContent convert(AnswerItem<AppServiceContent> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<AppServiceContent> convert(AnswerList<AppServiceContent> answerList) throws CerberusException {
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
    public Answer compareListAndUpdateInsertDeleteElements(String service, List<AppServiceContent> newList) {
        Answer ans;

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<AppServiceContent> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByVarious(service));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }

        // Update and Create all objects database Objects from newList
        List<AppServiceContent> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<AppServiceContent> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        for (AppServiceContent objectDifference : listToUpdateOrInsertToIterate) {
            for (AppServiceContent objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference.getService(), objectDifference.getKey(), objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        // Delete all objects database Objects that do not exist from newList
        List<AppServiceContent> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<AppServiceContent> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (AppServiceContent tcsDifference : listToDeleteToIterate) {
            for (AppServiceContent tcsInPage : newList) {
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
    public AnswerList<String> readDistinctValuesByCriteria(String service, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return appServiceContentDAO.readDistinctValuesByCriteria(service, searchParameter, individualSearch, columnName);
    }

}
