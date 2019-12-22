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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.IAppServiceHeaderDAO;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.service.IAppServiceHeaderService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class AppServiceHeaderService implements IAppServiceHeaderService {

    @Autowired
    private IAppServiceHeaderDAO AppServiceHeaderDAO;

    private static final Logger LOG = LogManager.getLogger(AppServiceHeaderService.class);

    private final String OBJECT_NAME = "Service Header";

    @Override
    public AnswerItem<AppServiceHeader> readByKey(String service, String key) {
        return AppServiceHeaderDAO.readByKey(service, key);
    }

    @Override
    public AnswerList<AppServiceHeader> readAll() {
        return readByVariousByCriteria(null, null, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList<AppServiceHeader> readByVarious(String service, String active) {
        return AppServiceHeaderDAO.readByVariousByCriteria(service, active, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList<AppServiceHeader> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return AppServiceHeaderDAO.readByVariousByCriteria(null, null, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<AppServiceHeader> readByVariousByCriteria(String service, String active, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return AppServiceHeaderDAO.readByVariousByCriteria(service, active, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public boolean exist(String service, String key) {
        AnswerItem objectAnswer = readByKey(service, key);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(AppServiceHeader object) {
        return AppServiceHeaderDAO.create(object);
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
        return AppServiceHeaderDAO.delete(object);
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
        return AppServiceHeaderDAO.update(service, key, object);
    }

    @Override
    public AppServiceHeader convert(AnswerItem<AppServiceHeader> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (AppServiceHeader) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<AppServiceHeader> convert(AnswerList<AppServiceHeader> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<AppServiceHeader>) answerList.getDataList();
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
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<AppServiceHeader> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByVarious(service, null));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }

        /**
         * Update and Create all objects database Objects from newList
         */
        List<AppServiceHeader> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<AppServiceHeader> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        for (AppServiceHeader objectDifference : listToUpdateOrInsertToIterate) {
            for (AppServiceHeader objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference.getService(), objectDifference.getKey(), objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
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
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }

        return finalAnswer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String service, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return AppServiceHeaderDAO.readDistinctValuesByCriteria(service, searchParameter, individualSearch, columnName);
    }

}
