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
import org.cerberus.core.crud.dao.IAppServiceHeaderDAO;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceHeaderService;
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
public class AppServiceHeaderService implements IAppServiceHeaderService {

    private IAppServiceHeaderDAO appServiceHeaderDAO;

    private static final Logger LOG = LogManager.getLogger(AppServiceHeaderService.class);

    private final String OBJECT_NAME = "Service Header";

    @Override
    public AnswerItem<AppServiceHeader> readByKey(String service, String key) {
        return appServiceHeaderDAO.readByKey(service, key);
    }

    @Override
    public AnswerList<AppServiceHeader> readAll() {
        return this.readByVariousByCriteria(null, false, false, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList<AppServiceHeader> readByVarious(String service) {
        return appServiceHeaderDAO.readByVariousByCriteria(service, false, false, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList<AppServiceHeader> readByVarious(String service, boolean isActive) {
        return appServiceHeaderDAO.readByVariousByCriteria(service, true, isActive, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList<AppServiceHeader> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return appServiceHeaderDAO.readByVariousByCriteria(null, false, false, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<AppServiceHeader> readByVariousByCriteria(String service, boolean withActiveCriteria, boolean isActive, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return appServiceHeaderDAO.readByVariousByCriteria(service, withActiveCriteria, isActive, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public boolean exist(String service, String key) {
        AnswerItem<AppServiceHeader> objectAnswer = readByKey(service, key);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successful and object was found.
    }

    @Override
    public Answer create(AppServiceHeader object) {
        return appServiceHeaderDAO.create(object);
    }

    @Override
    public Answer createList(List<AppServiceHeader> objectList) {
        Answer ans = new Answer(null);
        for (AppServiceHeader objectToCreate : objectList) {
            ans = this.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer delete(AppServiceHeader object) {
        return appServiceHeaderDAO.delete(object);
    }

    @Override
    public Answer deleteList(List<AppServiceHeader> objectList) {
        Answer ans = new Answer(null);
        for (AppServiceHeader objectToDelete : objectList) {
            ans = this.delete(objectToDelete);
        }
        return ans;
    }

    @Override
    public Answer update(String service, String key, AppServiceHeader object) {
        return appServiceHeaderDAO.update(service, key, object);
    }

    @Override
    public AppServiceHeader convert(AnswerItem<AppServiceHeader> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<AppServiceHeader> convert(AnswerList<AppServiceHeader> answerList) throws CerberusException {
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
    public Answer compareListAndUpdateInsertDeleteElements(String service, List<AppServiceHeader> newList) {
        Answer ans;

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<AppServiceHeader> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByVarious(service));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }
        // Update and Create all objects database Objects from newList
        List<AppServiceHeader> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<AppServiceHeader> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);
        for (AppServiceHeader objectDifference : listToUpdateOrInsertToIterate) {
            for (AppServiceHeader objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference.getService(), objectDifference.getKey(), objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        // Delete all objects database Objects that do not exist from newList
        List<AppServiceHeader> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<AppServiceHeader> listToDeleteToIterate = new ArrayList<>(listToDelete);
        for (AppServiceHeader tcsDifference : listToDeleteToIterate) {
            for (AppServiceHeader tcsInPage : newList) {
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
        return appServiceHeaderDAO.readDistinctValuesByCriteria(service, searchParameter, individualSearch, columnName);
    }

    @Override
    public List<AppServiceHeader> addIfNotExist(List<AppServiceHeader> headerList, AppServiceHeader newHeader) {
        boolean exist = false;
        for (AppServiceHeader contentHeader : headerList) {
            if (newHeader.getKey() != null && newHeader.getKey().equalsIgnoreCase(contentHeader.getKey())) {
                exist = true;
            }
        }
        if (!exist) {
            headerList.add(newHeader);
        }
        return headerList;
    }
}
