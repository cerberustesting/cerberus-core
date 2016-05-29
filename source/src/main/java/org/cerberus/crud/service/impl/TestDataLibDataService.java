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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cerberus.crud.dao.ITestDataLibDataDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.engine.entity.TestDataLibResult;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.MessageEventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestDataLibDataService implements ITestDataLibDataService {

    @Autowired
    ITestDataLibDataDAO testDataLibDataDAO;
    @Autowired
    private DatabaseSpring dbmanager;

    @Override
    public AnswerItem readByKey(Integer testDataLibID, String subData) {
        return testDataLibDataDAO.readByKey(testDataLibID, subData);
    }

    @Override
    public AnswerItem readByKeyTech(Integer testDataLibDataID) {
        return testDataLibDataDAO.readByKeyTech(testDataLibDataID);
    }

    @Override
    public AnswerList readByVarious(Integer testDataLibID, String columnEmpty, String parsingAnswerEmpty) {
        return testDataLibDataDAO.readByVarious(testDataLibID, columnEmpty, parsingAnswerEmpty);
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
    public Answer create(List<TestDataLibData> completeSubDataList) {
        return testDataLibDataDAO.create(completeSubDataList);
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
    public Answer delete(TestDataLib testDataLib) {
        return testDataLibDataDAO.delete(testDataLib);
    }

    @Override
    public Answer createUpdateDelete(ArrayList<TestDataLibData> entriesToInsert, ArrayList<TestDataLibData> entriesToUpdate,
            ArrayList<TestDataLibData> entriesToRemove) {

        dbmanager.beginTransaction();

        Answer answer = new Answer();
        //first we delete to avoid errors related to duplicate keys
        if (entriesToRemove.size() > 0) {
            //gets the list of entries to remove
            answer = testDataLibDataDAO.delete(entriesToRemove);
            if (!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                //if the delete does not succeed, then the transaction should be aborted
                dbmanager.abortTransaction();
                return answer;
            }
        }

        if (entriesToUpdate.size() > 0) {
            answer = testDataLibDataDAO.update(entriesToUpdate);
            if (!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                //if the update does not succeed, then the transaction should be aborted
                dbmanager.abortTransaction();
                return answer;
            }
        }

        if (entriesToInsert.size() > 0) {
            answer = testDataLibDataDAO.create(entriesToInsert);
            if (!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                //if the insert does not succeed, then the transaction should be aborted
                dbmanager.abortTransaction();
                return answer;
            }
        }

        dbmanager.commitTransaction();
        //if everything succeeds, then a success message is sent back

        MessageEvent ms = MessageEventUtil.createUpdateSuccessMessageDAO("Sub-data set ");
        answer.setResultMessage(ms);
        return answer;

    }

    @Override
    public Answer validate(List<TestDataLibData> subDataList) {

        Answer ans = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_OK));
        //check if the entries are duplicated 
        if (subDataList.size() > 1) {
            //check if the entries are duplicated 
            boolean hasDuplicates = containsDuplicates(subDataList);
            if (hasDuplicates) {
                MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "You have entries with duplicated names."));
                ans.setResultMessage(msg);
                return ans;
            }
        }

        return ans;
    }

    @Override
    public AnswerItem<String> fetchSubData(TestDataLibResult result, TestDataLibData subDataEntry) {
        return result.getValue(subDataEntry);
    }

    private boolean containsDuplicates(List<TestDataLibData> subDataList) {
        Set<String> entries = new HashSet<String>();

        //if is not valid then creates a new message
        for (TestDataLibData subData : subDataList) {
            if (entries.contains(subData.getSubData())) {
                return false; //not ok
            }

            entries.add(subData.getSubData());
        }

        return true;
    }

}
