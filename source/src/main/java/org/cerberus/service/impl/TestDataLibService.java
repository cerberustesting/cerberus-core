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
import org.apache.log4j.Level;
import org.cerberus.dao.ITestDataLibDAO;
import org.cerberus.dao.ITestDataLibDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestDataLib;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.entity.TestDataLibResult;
import org.cerberus.entity.TestDataLibResultSOAP;
import org.cerberus.entity.TestDataLibResultSQL;
import org.cerberus.entity.TestDataLibResultStatic;
import org.cerberus.entity.TestDataLibTypeEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestDataLibData;
import org.cerberus.log.MyLogger;
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
    public Answer deleteTestDataLib(int testDataLibID){  
        
        dbManager.beginTransaction();
        //deletes the testdatalib
        Answer ansDelete = testDataLibDAO.deleteUnusedTestDataLib(testDataLibID);
        //if everything went well, then we can delete all the subdata entries
        if(ansDelete.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
            //as we can create testdatalib without subdata it is possible that there this call will return 0, 
            ansDelete = testDataLibDataDAO.deleteByTestDataLibID(testDataLibID);
        }
        if(ansDelete.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
            dbManager.commitTransaction();//if success 
        }else{            
            dbManager.abortTransaction(); //if error       
        } 
        return ansDelete;        
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
    public AnswerItem findTestDataLibByKey(String name, String system, String environment, String country) throws CerberusException {
        return testDataLibDAO.findTestDataLibByKey(name, system, environment, country);
    }
    
    @Override
    public AnswerItem findTestDataLibByKey(int testDatalib){
        return testDataLibDAO.findTestDataLibByKey(testDatalib);
    }

    @Override
    public Integer getNumberOfTestDataLibPerCriteria(String searchTerm, String inds) {
        return testDataLibDAO.getNumberOfTestDataLibPerCriteria(searchTerm, inds);
    }
    
    @Override
    public AnswerList<String> getListOfGroupsPerType(String type){
        return testDataLibDAO.getListOfGroupsPerType(type);
    }

    @Override
    public AnswerItem fetchData(TestDataLib lib, int rowLimit, String propertyName) {
        AnswerItem answer  = new AnswerItem();
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        TestDataLibResult result = null;
        
        if(lib.getType().equals(TestDataLibTypeEnum.STATIC.getCode())){
            result = fetchDataStatic(lib);  
            
        }else if(lib.getType().equals(TestDataLibTypeEnum.SQL.getCode())){
            AnswerItem sqlResult = fetchDataSQL(lib, rowLimit, propertyName);
            result = (TestDataLibResult)sqlResult.getItem();
            msg = sqlResult.getResultMessage();
            
        }else if(lib.getType().equals(TestDataLibTypeEnum.SOAP.getCode())){
            AnswerItem soapResult = fetchDataSOAP(lib);
            result = (TestDataLibResult)soapResult.getItem();
            msg = soapResult.getResultMessage();
        } 
        answer.setItem(result);
        answer.setResultMessage(msg);
        return answer;
    }

    private TestDataLibResult fetchDataStatic(TestDataLib lib) {
        //static data does need pre processing to retrieve the subdataentries
        TestDataLibResult result = new TestDataLibResultStatic();
        result.setTestDataLibID(lib.getTestDataLibID());
        
        return result;
    }
    
    @Override
    public Answer createTestDataLib(TestDataLib testDataLib, List<TestDataLibData> subDataList) {
        List<TestDataLibData> completeSubDataList = new ArrayList<TestDataLibData>();
        dbManager.beginTransaction();
        //creates the test data lib
        Answer ansInsert = testDataLibDAO.createTestDataLib(testDataLib);
        if(ansInsert.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
            //if success, then creates the entries
            if(subDataList != null && !subDataList.isEmpty()){            
                for(TestDataLibData libData: subDataList){
                    TestDataLibData data = testDataLibDataFactory.create(testDataLib.getTestDataLibID(), libData.getSubData(), libData.getValue(), 
                            libData.getColumn(), libData.getParsingAnswer(), libData.getDescription());                        
                    completeSubDataList.add(data);                
                }            

                ansInsert = testDataLibDataDAO.createTestDataLibDataBatch(completeSubDataList);
            }
        }
        
        MessageEvent msg;
        if(ansInsert.getResultMessage().getCode() == MessageGeneralEnum.DATA_OPERATION_SUCCESS.getCode()){
            dbManager.commitTransaction();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib and  and Subdata entries ").replace("%OPERATION%", "INSERT"));
            ansInsert.setResultMessage(msg);
        }else{
            dbManager.abortTransaction();            
        }
        
        return ansInsert;
    }

    @Override
    public Answer createTestDataLibBatch(List<TestDataLib> entries) throws CerberusException{
        dbManager.beginTransaction();
        Answer ansInsert = testDataLibDAO.createTestDataLibBatch(entries);
        
        if(ansInsert.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
            dbManager.commitTransaction();
        }else{
            dbManager.abortTransaction();
        }
        return ansInsert;
    }

    @Override
    public Answer createTestDataLibBatch(List<TestDataLib> testDataLibList, List<TestDataLibData> subDataList){
        List<TestDataLibData> completeSubDataList = new ArrayList<TestDataLibData>();
        dbManager.beginTransaction();
        //creates the entries 
        Answer ansInsert = testDataLibDAO.createTestDataLibBatch(testDataLibList);
        //if the insert went well then we can insert the subdataentries
        if(ansInsert.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
            if(subDataList != null && !subDataList.isEmpty()){
                for(TestDataLib lib : testDataLibList){
                    for(TestDataLibData libData: subDataList){
                        //recreates the testdatalib elements because at first they don't have the testdatalib id that is part of its primary key
                        TestDataLibData data = testDataLibDataFactory.create(lib.getTestDataLibID(), libData.getSubData(), libData.getValue(), 
                                libData.getColumn(), libData.getParsingAnswer(), libData.getDescription());                        
                        completeSubDataList.add(data);                
                    }
                }
                ansInsert = testDataLibDataDAO.createTestDataLibDataBatch(completeSubDataList);                
            }
        } 
        
        if(ansInsert.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
            dbManager.commitTransaction();
            //we will replace the last success message for the following one
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib and Subdata ").replace("%OPERATION%", "INSERT"));        
            ansInsert.setResultMessage(msg);
        }else{//if an error had occurred then we will propagate its message
            dbManager.abortTransaction();
        }
                
        return ansInsert;
    }
    
    @Override
    public Answer createTestDataLibBatch(HashMap<TestDataLib, List<TestDataLibData>> entries){
        List<TestDataLibData> completeSubDataList = null;
        Answer ansInsert = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        dbManager.beginTransaction();
        
        for (TestDataLib testDataLib : entries.keySet()) {
            ansInsert = testDataLibDAO.createTestDataLib(testDataLib);
            completeSubDataList = new ArrayList<TestDataLibData>();
            if(ansInsert.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                //if success, then creates the entries
                //gets the subdatalist
                List<TestDataLibData> subDataList = (List<TestDataLibData>) entries.get(testDataLib);
                if(subDataList != null && !subDataList.isEmpty()){
                    for(TestDataLibData libData: subDataList){
                        TestDataLibData data = testDataLibDataFactory.create(testDataLib.getTestDataLibID(), libData.getSubData(), libData.getValue(),
                                libData.getColumn(), libData.getParsingAnswer(), libData.getDescription());
                        completeSubDataList.add(data);
                    }
                    ansInsert = testDataLibDataDAO.createTestDataLibDataBatch(completeSubDataList);
                }
            }else{
                break;
            }
        }
        
        if(ansInsert.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
            dbManager.commitTransaction();
        }else{
            dbManager.abortTransaction();
        }
        return ansInsert;

    }
 
    
    @Override
    public AnswerList findTestDataLibNameList(String testDataLibName, int limit) {
        return testDataLibDAO.findTestDataLibNameList(testDataLibName, limit);
    }

    private AnswerItem fetchDataSQL(TestDataLib lib, int rowLimit, String propertyName) {
        AnswerItem answer;
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        TestDataLibResult result = null;
        //sql data needs to collect the values for the n columns
        answer = sQLService.calculateOnDatabaseNColumns(lib.getScript(), lib.getDatabase(), 
                lib.getSystem(), lib.getCountry(), lib.getEnvironment(), rowLimit, propertyName); 
        
        MyLogger.log(TestDataLibService.class.getName(), Level.INFO, "Test data libs ervice SQL " + lib.getScript());
        
        //if the sql service returns a success message then we can process it
        if(answer.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_SQL.getCode()){            
            HashMap<String, String> columns =  (HashMap<String, String>) answer.getItem();
            result = new TestDataLibResultSQL();                       
            result.setTestDataLibID(lib.getTestDataLibID());

            ((TestDataLibResultSQL)result).setData(columns); 

        }else if(answer.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_SQL_NODATA.getCode()){
            //if the script does not return 
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NODATA);
            msg.setDescription(msg.getDescription().replace("%ENTRY%", lib.getName()).replace("%SQL%", lib.getScript())
                    .replace("%DATABASE%", lib.getDatabase()));
        }else{
            //other error had occured
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GENERIC);
            msg.setDescription(msg.getDescription().replace("%ENTRY%", lib.getName()).replace("%SQL%", lib.getScript())
                    .replace("%DATABASE%", lib.getDatabase()));
        }
        answer.setItem(result);
        answer.setResultMessage(msg);
        return answer;    
    }

    private AnswerItem fetchDataSOAP(TestDataLib lib) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg;
        TestDataLibResult result = null;
        
        //soap data needs to get the soap response
        String key = TestDataLibTypeEnum.SOAP.getCode() + lib.getTestDataLibID();
        msg = soapService.callSOAPAndStoreResponseInMemory(key, lib.getEnvelope(), lib.getServicePath(), 
                lib.getMethod(), null, true); 
        //if the call returns success then we can process the soap ressponse
        if(msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()){
            result = new TestDataLibResultSOAP();
            ((TestDataLibResultSOAP)result).setSoapResponseKey(key);
            result.setTestDataLibID(lib.getTestDataLibID());
            Document xmlDocument =  xmlUnitService.getXmlDocument(key);
            ((TestDataLibResultSOAP)result).setData(xmlDocument);   
            //the code for action success call soap is different from the
            //code return from the property success soap
            //if the action succeeds then, we can assume that the SOAP request was performed with success
            msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SOAP); 
        } 
        answer.setItem(result);
        answer.setResultMessage(msg);
        return answer;
    }
}   
