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

import org.cerberus.crud.dao.ITestBatteryContentDAO;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.service.ITestBatteryContentService;
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
import java.util.Map;

/**
 * @author cerberus
 */
@Service
public class TestBatteryContentService implements ITestBatteryContentService {

    @Autowired
    ITestBatteryContentDAO testBatteryContentDAO;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CampaignContentService.class);

    @Override
    public AnswerList readByTestBatteryByCriteria(String testBattery, int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return testBatteryContentDAO.readByTestBatteryByCriteria(testBattery, startPosition, length, columnName, sort, searchParameter, string);
    }

    @Override
    public AnswerList readByCampaignByCriteria(String campaign, int start, int amount, String columnName, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch) {
        return testBatteryContentDAO.readByCampaignByCriteria(campaign, start, amount, columnName, sortInformation, searchTerm, individualSearch);
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String columnName, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch) {
        return testBatteryContentDAO.readByCriteria(start, amount, columnName, sortInformation, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCampaignByCriteria(String campaign, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return testBatteryContentDAO.readDistinctValuesByCampaignByCriteria(campaign, searchParameter, individualSearch, columnName);
    }

    @Override
    public AnswerList readByTestBattery(String key) {
        return testBatteryContentDAO.readByTestBattery(key);
    }

    @Override
    public Answer deleteByTestBattery(String key) {
        return testBatteryContentDAO.deleteByTestBattery(key);
    }

    @Override
    public Answer createList(List<TestBatteryContent> objectList) {
        Answer ans = new Answer(null);
        for (TestBatteryContent objectToCreate : objectList) {
            ans = testBatteryContentDAO.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<TestBatteryContent> objectList) {
        Answer ans = new Answer(null);
        for (TestBatteryContent objectToCreate : objectList) {
            ans = testBatteryContentDAO.delete(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String tb, List<TestBatteryContent> newList) {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<TestBatteryContent> oldList = new ArrayList();
        try {
            oldList = this.convert(this.readByTestBattery(tb));
        } catch (CerberusException ex) {
            LOG.error(ex);
        }

        /**
         * Iterate on (TestCaseStep From Page - TestCaseStep From Database) If
         * TestCaseStep in Database has same key : Update and remove from the
         * list. If TestCaseStep in database does ot exist : Insert it.
         */
        List<TestBatteryContent> listToUpdateOrInsert = new ArrayList(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<TestBatteryContent> listToUpdateOrInsertToIterate = new ArrayList(listToUpdateOrInsert);

        for (TestBatteryContent objectDifference : listToUpdateOrInsertToIterate) {
            for (TestBatteryContent objectInDatabase : oldList) {
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
        List<TestBatteryContent> listToDelete = new ArrayList(oldList);
        listToDelete.removeAll(newList);
        List<TestBatteryContent> listToDeleteToIterate = new ArrayList(listToDelete);

        for (TestBatteryContent tcsDifference : listToDeleteToIterate) {
            for (TestBatteryContent tcsInPage : newList) {
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
    public TestBatteryContent convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestBatteryContent) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestBatteryContent> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<TestBatteryContent>) answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

}
