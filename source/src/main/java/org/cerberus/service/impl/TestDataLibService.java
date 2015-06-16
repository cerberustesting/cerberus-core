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
import java.util.HashMap;
import java.util.List;
import org.cerberus.dao.ITestDataLibDAO;
import org.cerberus.dao.ITestDataLibDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.TestDataLib;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.entity.TestDataLibResult;
import org.cerberus.entity.TestDataLibResultSOAP;
import org.cerberus.entity.TestDataLibResultSQL;
import org.cerberus.entity.TestDataLibResultStatic;
import org.cerberus.entity.TestDataLibTypeEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestDataLibData;
import org.cerberus.service.ITestDataLibService;
import org.cerberus.serviceEngine.ISQLService;
import org.cerberus.serviceEngine.ISoapService;
import org.cerberus.serviceEngine.IXmlUnitService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class TestDataLibService implements ITestDataLibService {

    @Autowired
    private DatabaseSpring dbManager;
    @Autowired
    ITestDataLibDAO testDataLibDAO;
    @Autowired
    ITestDataLibDataDAO testDataLibDataDAO;
    @Autowired
    IFactoryTestDataLibData testDataLibDataFactory;
    @Autowired
    private ISoapService soapService;
    @Autowired
    private ISQLService sQLService;
    @Autowired
    private IXmlUnitService xmlUnitService;
    
    @Override
    public void createTestDataLib(TestDataLib testDataLib) throws CerberusException {
        testDataLibDAO.createTestDataLib(testDataLib);
    }

    @Override
    public Answer updateTestDataLib(TestDataLib testDataLib){
        return testDataLibDAO.updateTestDataLib(testDataLib);
    }

    @Override
    public void deleteTestDataLib(TestDataLib testDataLib) throws CerberusException {
        testDataLibDAO.deleteTestDataLib(testDataLib);
    }
    @Override
    public Answer deleteTestDataLib(int testDataLibID) throws CerberusException{ //TODO:FN tirar estas excepcoes?
        Answer answer = new Answer();
        dbManager.beginTransaction();
        //deletes the testdatalib
        Answer ansDelete = testDataLibDAO.deleteTestDataLib(testDataLibID);
        boolean isOk = true;
        //if everything went well, then we can delete all the subdata entries
        if(ansDelete.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()){
            //as we can create testdatalib without subdata it is possible that there this call will return 0, 
            ansDelete = testDataLibDataDAO.deleteByTestDataLibID(testDataLibID);
            if(ansDelete.getResultMessage().getCode() != MessageEventEnum.DATA_OPERATION_OK.getCode()){ 
                isOk = false;
            }
        }else{
            isOk = false;            
        }
        
        if(isOk){
            dbManager.commitTransaction();
        }else{
            dbManager.abortTransaction();
        }
        return answer;        
    }    
    
    @Override
    public List<TestDataLib> findAllTestDataLib() {
        return testDataLibDAO.findAllTestDataLib();
    }

    @Override
    public AnswerList findTestDataLibListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return testDataLibDAO.findTestDataLibListByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public TestDataLib findTestDataLibByKey(String name, String system, String environment, String country) throws CerberusException {
        return testDataLibDAO.findTestDataLibByKey(name, system, environment, country);
    }
    
    @Override
    public AnswerItem findTestDataLibByKey(int testDatalib) throws CerberusException {
        return testDataLibDAO.findTestDataLibByKey(testDatalib);
    }

    @Override
    public Integer getNumberOfTestDataLibPerCriteria(String searchTerm, String inds) {
        return testDataLibDAO.getNumberOfTestDataLibPerCriteria(searchTerm, inds);
    }
    
    @Override
    public List<String> getListOfGroupsPerType(String type){
        return testDataLibDAO.getListOfGroupsPerType(type);
    }

    @Override
    public AnswerItem fetchData(TestDataLib lib, int rowLimit, String propertyName) {
        AnswerItem answer  = new AnswerItem();
        MessageEvent ms = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        TestDataLibResult result = null;
        
        if(lib.getType().equals(TestDataLibTypeEnum.STATIC.getCode())){
            result = new TestDataLibResultStatic();         
            result.setTestDataLibID(lib.getTestDataLibID());            
            
        }else if(lib.getType().equals(TestDataLibTypeEnum.SQL.getCode())){
            
            answer = sQLService.calculateOnDatabaseNColumns(lib.getScript(), lib.getDatabase(), 
                    lib.getSystem(), lib.getCountry(), lib.getEnvironment(), rowLimit, propertyName); 
            
            //if the sql service returns a success message then we can process it
            if(answer.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_SQL.getCode()){
                HashMap<String, String> columns =  (HashMap<String, String>) answer.getItem();
                result = new TestDataLibResultSQL();                       
                result.setTestDataLibID(lib.getTestDataLibID());

                ((TestDataLibResultSQL)result).setData(columns);            
            }
            
        }else if(lib.getType().equals(TestDataLibTypeEnum.SOAP.getCode())){
            //TODO:FN tratar aqui a mensagem de erro; deveria ter um answer
            
            String key = TestDataLibTypeEnum.SOAP.getCode() + lib.getTestDataLibID();
            ms = soapService.callSOAPAndStoreResponseInMemory(key, lib.getEnvelope(), lib.getServicePath(), 
                    lib.getMethod(), null); 
            //if the call returns success then we can process the soap ressponse
            if(ms.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()){
                result = new TestDataLibResultSOAP();
                ((TestDataLibResultSOAP)result).setSoapResponseKey(key);
                result.setTestDataLibID(lib.getTestDataLibID());
                Document xmlDocument =  xmlUnitService.getXmlDocument(key);
                ((TestDataLibResultSOAP)result).setData(xmlDocument);   
            }
        } 
        answer.setItem(result);
        answer.setResultMessage(ms);
        return answer;
    }
    
    @Override
    public Answer createTestDataLib(TestDataLib testDataLib, List<TestDataLibData> subDataList) throws CerberusException {
        //TODO:FN implement
        List<TestDataLibData> completeSubDataList = new ArrayList<TestDataLibData>();
        dbManager.beginTransaction();
        //creates the entries 
        testDataLibDAO.createTestDataLib(testDataLib);
        if(subDataList != null && !subDataList.isEmpty()){
            
            for(TestDataLibData libData: subDataList){
                TestDataLibData data = testDataLibDataFactory.create(testDataLib.getTestDataLibID(), libData.getSubData(), libData.getValue(), 
                        libData.getColumn(), libData.getParsingAnswer(), libData.getDescription());                        
                completeSubDataList.add(data);                
            }
            
            testDataLibDataDAO.createTestDataLibDataBatch(completeSubDataList);
        }
        
        //TODO:FN ver aqui as mensagens de erro e terminar de forma abrupta se for preciso
        dbManager.commitTransaction();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib Set").replace("%OPERATION%", "INSERT"));
        return new Answer(msg);
    }

    @Override
    public Answer createTestDataLibBatch(List<TestDataLib> entries) throws CerberusException{
        dbManager.beginTransaction();
        testDataLibDAO.createTestDataLibBatch(entries);
        //TODO:FN mudar aqui a mensagem do DAO
        //TODO:FN check if there is any error, to change the mssagem
        dbManager.commitTransaction();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib Set").replace("%OPERATION%", "INSERT"));
        return new Answer(msg);
    }
    /**
     *
     * @param entries
     * @param subDataList
     * @return 
     * @throws CerberusException
     */
    @Override
    public Answer createTestDataLibBatch(List<TestDataLib> entries, List<TestDataLibData> subDataList) throws CerberusException{
        List<TestDataLibData> completeSubDataList = new ArrayList<TestDataLibData>();
        dbManager.beginTransaction();
        //creates the entries 
        testDataLibDAO.createTestDataLibBatch(entries);
        if(subDataList != null && !subDataList.isEmpty()){
            for(TestDataLib lib : entries){
                for(TestDataLibData libData: subDataList){
                    TestDataLibData data = testDataLibDataFactory.create(lib.getTestDataLibID(), libData.getSubData(), libData.getValue(), 
                            libData.getColumn(), libData.getParsingAnswer(), libData.getDescription());                        
                    completeSubDataList.add(data);                
                }
            }
            //TODO:FN check the messages returned
            testDataLibDataDAO.createTestDataLibDataBatch(completeSubDataList);
        }
        dbManager.commitTransaction();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib Set").replace("%OPERATION%", "%INSERT%"));
        return new Answer(msg);
    }

 
}   
