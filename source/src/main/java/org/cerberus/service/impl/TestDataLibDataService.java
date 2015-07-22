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
package org.cerberus.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.cerberus.dao.ITestDataLibDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.entity.TestDataLibResult;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ITestDataLibDataService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestDataLibDataService implements ITestDataLibDataService {

    @Autowired
    ITestDataLibDataDAO testDataLibDataDAO;
    @Autowired
    private DatabaseSpring dbmanager;

    @Override
    public void createTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        testDataLibDataDAO.createTestDataLibData(testDataLibData);
    }

    @Override
    public void updateTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        testDataLibDataDAO.updateTestDataLibData(testDataLibData);
    }

    @Override
    public void deleteTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        testDataLibDataDAO.deleteTestDataLibData(testDataLibData);
    }
    
    @Override
    public void deleteByTestDataLibID(int testDataLibID) throws CerberusException{
        testDataLibDataDAO.deleteByTestDataLibID(testDataLibID);
    }

    @Override
    public TestDataLibData findTestDataLibDataByKey(Integer testDataLibID, String subData) throws CerberusException {
        return testDataLibDataDAO.findTestDataLibDataByKey(testDataLibID, subData);
    }

    @Override
    public List<TestDataLibData> findAllTestDataLibData() {
        return testDataLibDataDAO.findAllTestDataLibData();
    }

    @Override
    public AnswerList findTestDataLibDataListByTestDataLib(Integer testDataLibID) {
        return testDataLibDataDAO.findTestDataLibDataListByID(testDataLibID);
    }

    @Override
    public List<TestDataLibData> findTestDataLibDataByCriteria(Integer testDataLibID, String subData, String value, String column, String parsingAnswer, String description) throws CerberusException {
        return testDataLibDataDAO.findTestDataLibDataByCriteria(testDataLibID, subData, value, column, parsingAnswer, description);
    }

    @Override
    public void createTestDataLibDataBatch(List<TestDataLibData> subdataSet) throws CerberusException{
        testDataLibDataDAO.createTestDataLibDataBatch(subdataSet);
    }

 

    @Override
    public String fetchSubData(TestDataLibResult result, TestDataLibData subDataEntry) {
        
        return result.getValue(subDataEntry);
    }
 
    @Override
    public Answer cudTestDataLibData(int testDataLibID, ArrayList<TestDataLibData> entriesToInsert, ArrayList<TestDataLibData> entriesToUpdate, ArrayList<String> entriesToRemove) {
        dbmanager.beginTransaction();        
        
        Answer answer = new Answer();
        
        if(entriesToInsert.size() > 0){
            answer = testDataLibDataDAO.createTestDataLibDataBatch(entriesToInsert);
            if(!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                //if the insert does not succeed, then the transaction should be aborted
                dbmanager.abortTransaction();
                return answer;           
            }
        }
        
        
        
        if(entriesToUpdate.size() > 0){
            answer = testDataLibDataDAO.updateTestDataLibDataBatch(entriesToUpdate);
            if(!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                //if the update does not succeed, then the transaction should be aborted
                dbmanager.abortTransaction();
                return answer;           
            }
        }
        
        if(entriesToRemove.size() > 0){
            answer =  testDataLibDataDAO.deleteTestDataLibDataBatch(testDataLibID, entriesToRemove);
            if(!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                //if the delete does not succeed, then the transaction should be aborted
                dbmanager.abortTransaction();
                return answer;           
            }
        }
        
        dbmanager.commitTransaction();
        //if everything succeeds, then a success message is sent back
        MessageEvent ms = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        ms.setDescription(ms.getDescription().replace("%ITEM%", "Test data lib data").replace("%OPERATION%", "Modification of the subdata set "));
        
        answer.setResultMessage(ms);
        return answer;
        
    }

    @Override
    public AnswerList findTestDataLibDataByName(String testDataLibName) {
        return testDataLibDataDAO.findTestDataLibDataByName(testDataLibName);
    }

    @Override
    public AnswerList findTestDataLibSubData(String testDataLib, String nameToSearch, int limit) {
        return testDataLibDataDAO.findTestDataLibSubData(testDataLib, nameToSearch, limit);
    }
 
}
