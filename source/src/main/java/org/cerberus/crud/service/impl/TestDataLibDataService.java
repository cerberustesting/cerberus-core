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
import java.util.List;
import org.cerberus.crud.dao.ITestDataLibDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.dto.TestDataLibDataUpdateDTO;
import org.cerberus.service.engine.testdata.TestDataLibResult;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestDataLibDataService;
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
    public Answer create(TestDataLibData testDataLibData) {
        return testDataLibDataDAO.create(testDataLibData);
    }

    @Override
    public Answer update(TestDataLibData testDataLibData){
        return testDataLibDataDAO.update(testDataLibData);
    }

    @Override
    public Answer delete(TestDataLibData testDataLibData){
        return testDataLibDataDAO.delete(testDataLibData);
    }
    
    @Override
    public Answer delete(int testDataLibID){
        return testDataLibDataDAO.delete(testDataLibID);
    }

    @Override
    public AnswerItem<TestDataLibData> readByKey(Integer testDataLibID, String subData){
        return testDataLibDataDAO.readByKey(testDataLibID, subData);
    }

    @Override
    public AnswerList<TestDataLibData> readAll() {
        return testDataLibDataDAO.readAll();
    }

    @Override
    public AnswerList readById(Integer testDataLibID) {
        return testDataLibDataDAO.readById(testDataLibID);
    }

    @Override
    public AnswerList<TestDataLibData> readByCriteria(Integer testDataLibID, String subData, String value, String column, String parsingAnswer, String description){
        return testDataLibDataDAO.readByCriteria(testDataLibID, subData, value, column, parsingAnswer, description);
    }

    @Override
    public Answer createBatch(List<TestDataLibData> subdataSet) throws CerberusException{
        return testDataLibDataDAO.createBatch(subdataSet);
    }

    @Override
    public AnswerItem<String> fetchSubData(TestDataLibResult result, TestDataLibData subDataEntry) {
        return result.getValue(subDataEntry);
    }
 
    @Override
    public Answer createUpdateDelete(int testDataLibID, ArrayList<TestDataLibData> entriesToInsert, ArrayList<TestDataLibDataUpdateDTO> entriesToUpdate, 
            ArrayList<String> entriesToRemove) {
        
        dbmanager.beginTransaction();        
        
        Answer answer = new Answer();
        
        if(entriesToInsert.size() > 0){
            answer = testDataLibDataDAO.createBatch(entriesToInsert);
            if(!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                //if the insert does not succeed, then the transaction should be aborted
                dbmanager.abortTransaction();
                return answer;           
            }
        }
                
        if(entriesToUpdate.size() > 0){
            answer = testDataLibDataDAO.updateBatch(entriesToUpdate);
            if(!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                //if the update does not succeed, then the transaction should be aborted
                dbmanager.abortTransaction();
                return answer;           
            }
        }
        
        if(entriesToRemove.size() > 0){
            answer =  testDataLibDataDAO.deleteBatch(testDataLibID, entriesToRemove);
            if(!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                //if the delete does not succeed, then the transaction should be aborted
                dbmanager.abortTransaction();
                return answer;           
            }
        }
        
        dbmanager.commitTransaction();
        //if everything succeeds, then a success message is sent back
        
        MessageEvent ms = MessageEventUtil.createUpdateSuccessMessageDAO("Sub-data set (for ID: " + testDataLibID + ") ");
        answer.setResultMessage(ms);
        return answer;
        
    }

    @Override
    public AnswerList readByName(String testDataLibName) {
        return testDataLibDataDAO.readByName(testDataLibName);
    }

    @Override
    public AnswerList readByIdByName(String testDataLib, String nameToSearch, int limit) {
        return testDataLibDataDAO.readByIdByName(testDataLib, nameToSearch, limit);
    }
 
}
