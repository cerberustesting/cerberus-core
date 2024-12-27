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
import java.util.List;
import org.cerberus.core.crud.dao.ITestDataLibDataDAO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.entity.TestDataLibData;
import org.cerberus.core.crud.service.ITestDataLibDataService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestDataLibDataService implements ITestDataLibDataService {

    @Autowired
    ITestDataLibDataDAO testDataLibDataDAO;

    private final String OBJECT_NAME = "TestDataLibData";

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(TestDataLibDataService.class);

    @Override
    public AnswerItem readByKey(Integer testDataLibID, String subData) {
        return testDataLibDataDAO.readByKey(testDataLibID, subData);
    }

    @Override
    public AnswerItem readByKeyTech(Integer testDataLibDataID) {
        return testDataLibDataDAO.readByKeyTech(testDataLibDataID);
    }

    @Override
    public AnswerList<TestDataLibData> readByVarious(Integer testDataLibID, String columnEmpty, String parsingAnswerEmpty, String columnPositionEmpty) {
        return testDataLibDataDAO.readByVarious(testDataLibID, columnEmpty, parsingAnswerEmpty, columnPositionEmpty);
    }

    @Override
    public AnswerList readAll() {
        return testDataLibDataDAO.readAll();
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        return testDataLibDataDAO.readByCriteria(start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList readByName(String testDataLibName) {
        return testDataLibDataDAO.readByName(testDataLibName);
    }

    @Override
    public Answer create(TestDataLibData testDataLibData) {
        return testDataLibDataDAO.create(testDataLibData);
    }

    @Override
    public Answer update(TestDataLibData testDataLibData) {
        return testDataLibDataDAO.update(testDataLibData);
    }

    @Override
    public Answer delete(TestDataLibData testDataLibData) {
        return testDataLibDataDAO.delete(testDataLibData);
    }

    @Override
    public Answer createList(List<TestDataLibData> objectList) {
        Answer ans = new Answer(null);
        for (TestDataLibData objectToCreate : objectList) {
            ans = testDataLibDataDAO.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<TestDataLibData> objectList) {
        Answer ans = new Answer(null);
        for (TestDataLibData objectToCreate : objectList) {
            ans = testDataLibDataDAO.delete(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(Integer testDataLibId, List<TestDataLibData> newList) {
    	
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<TestDataLibData> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByVarious(testDataLibId, null, null, null));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }

        /**
         * Update and Create all objects database Objects from newList
         */
        List<TestDataLibData> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<TestDataLibData> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);
        
        for (TestDataLibData objectDifference : listToUpdateOrInsertToIterate) {
            for (TestDataLibData objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
        List<TestDataLibData> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<TestDataLibData> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (TestDataLibData tcsDifference : listToDeleteToIterate) {
            for (TestDataLibData tcsInPage : newList) {
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
    public TestDataLibData convert(AnswerItem<TestDataLibData> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestDataLibData> convert(AnswerList<TestDataLibData> answerList) throws CerberusException {
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
