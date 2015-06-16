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
import java.util.logging.Level;
import java.util.logging.Logger;
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
        return testDataLibDataDAO.findTestDataLibDataListByTestDataLib(testDataLibID);
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
        
        try {
            Answer ansCreate = testDataLibDataDAO.createTestDataLibDataBatch(entriesToInsert);
            //TODO:FN verificar os outros cenaŕios para terminar a transaccao
            Answer ansUpdate = testDataLibDataDAO.updateTestDataLibDataBatch(entriesToUpdate);
            //testDataLibDataDAO.createTestDataLibDataBatch(entriesToInsert);
            //testDataLibDataDAO.updateTestDataLibData(null);
            Answer ansDelete =  testDataLibDataDAO.deleteTestDataLibDataBatch(testDataLibID, entriesToRemove);
            if(!ansDelete.getMessageType().equals(MessageEventEnum.DATA_OPERATION_OK.getCodeString())){
                dbmanager.abortTransaction();
                return ansDelete;
            }
        } catch (CerberusException ex) {
            Logger.getLogger(TestDataLibDataService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        dbmanager.commitTransaction();
        MessageEvent ms = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        ms.setDescription(ms.getDescription().replace("%ITEM%", "Test data lib data").replace("%OPERATION%", "INSERT/UPDATE/DELETE"));
        return new Answer(ms);
    }

  
}
