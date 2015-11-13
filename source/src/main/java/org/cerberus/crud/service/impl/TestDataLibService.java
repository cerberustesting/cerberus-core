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
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.ITestDataLibDAO; 
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.TestDataLibTypeEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.service.engine.ISQLService;
import org.cerberus.service.engine.ISoapService;
import org.cerberus.service.engine.IXmlUnitService;
import org.cerberus.service.engine.testdata.TestDataLibResult;
import org.cerberus.service.engine.testdata.TestDataLibResultSOAP;
import org.cerberus.service.engine.testdata.TestDataLibResultSQL;
import org.cerberus.service.engine.testdata.TestDataLibResultStatic;
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
    IFactoryTestDataLibData testDataLibDataFactory;
    @Autowired
    private ISoapService soapService;
    @Autowired
    private ISQLService sQLService;
    @Autowired
    private IXmlUnitService xmlUnitService;
    @Autowired
    private ITestDataLibDataService testDataLibDataService;

    @Override
    public AnswerItem readByKey(String name, String system, String environment, String country) {
        return testDataLibDAO.readByKey(name, system, environment, country);
    }

    @Override
    public AnswerItem readByKey(int testDatalib) {
        return testDataLibDAO.readByKey(testDatalib);
    }

    @Override
    public AnswerList readNameListByName(String testDataLibName, int limit) {
        return testDataLibDAO.readNameListByName(testDataLibName, limit);
    }

    @Override
    public AnswerList readAll() {
        return testDataLibDAO.readAll();
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return testDataLibDAO.readByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<String> readDistinctGroups() {
        return testDataLibDAO.readDistinctGroups();
    }

    @Override
    public void create(TestDataLib testDataLib){
        testDataLibDAO.create(testDataLib);
    }

    @Override
    public Answer create(TestDataLib testDataLib, List<TestDataLibData> subDataList) {
        List<TestDataLibData> completeSubDataList = new ArrayList<TestDataLibData>();
        
        //validates if the subdata are not duplicated
        Answer answer = testDataLibDataService.validate(completeSubDataList);
        
        if(answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR.getCode())){
            return answer;
        }
        //if not then we can start the insert
        dbManager.beginTransaction();
        //creates the test data lib
        answer = testDataLibDAO.create(testDataLib);
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if success, then creates the entries
            if (subDataList != null && !subDataList.isEmpty()) {                
                for (TestDataLibData libData : subDataList) {
                    TestDataLibData data = testDataLibDataFactory.create(-1, testDataLib.getTestDataLibID(), libData.getSubData(), libData.getValue(),
                            libData.getColumn(), libData.getParsingAnswer(), libData.getDescription());
                    completeSubDataList.add(data);
                }

                answer = testDataLibDataService.create(completeSubDataList);
            }
        }

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            dbManager.commitTransaction();
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib and  and Subdata entries ").replace("%OPERATION%", "INSERT"));
            answer.setResultMessage(msg);
        } else {
            dbManager.abortTransaction();
        }
    
        return answer;
    }
    
    @Override
    public Answer create(HashMap<TestDataLib, List<TestDataLibData>> entries) {
        
        List<TestDataLibData> completeSubDataList;
        
        Answer ansInsert = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        dbManager.beginTransaction();

        for (TestDataLib testDataLib : entries.keySet()) {
            ansInsert = testDataLibDAO.create(testDataLib);
            completeSubDataList = new ArrayList<TestDataLibData>();
            if (ansInsert.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                //if success, then creates the entries
                //gets the subdatalist
                List<TestDataLibData> subDataList = (List<TestDataLibData>) entries.get(testDataLib);
                if (subDataList != null && !subDataList.isEmpty()) {
                    for (TestDataLibData libData : subDataList) {
                        TestDataLibData data = testDataLibDataFactory.create(-1, testDataLib.getTestDataLibID(), libData.getSubData(), libData.getValue(),
                                libData.getColumn(), libData.getParsingAnswer(), libData.getDescription());
                        completeSubDataList.add(data);
                    }
                    ansInsert = testDataLibDataService.create(completeSubDataList);
                }
            } else {
                break;
            }
        }

        if (ansInsert.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            dbManager.commitTransaction();
        } else {
            dbManager.abortTransaction();
        }
        return ansInsert;

    }
   
    @Override
    public Answer delete(TestDataLib testDataLib) {

        dbManager.beginTransaction();
        //deletes the testdatalib
        Answer ansDelete = testDataLibDAO.delete(testDataLib);
        //if everything went well, then we can delete all the subdata entries
        if (ansDelete.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //as we can create testdatalib without subdata it is possible that there this call will return 0, 
            ansDelete = testDataLibDataService.delete(testDataLib);
        }
        if (ansDelete.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            dbManager.commitTransaction();//if success 
        } else {
            dbManager.abortTransaction(); //if error       
        }
        return ansDelete;
    }

    @Override
    public Answer update(TestDataLib testDataLib) {
        return testDataLibDAO.update(testDataLib);
    }

    @Override
    public AnswerItem fetchData(TestDataLib lib, int rowLimit, String propertyName) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        TestDataLibResult result = null;

        if (lib.getType().equals(TestDataLibTypeEnum.STATIC.getCode())) {
            result = fetchDataStatic(lib);

        } else if (lib.getType().equals(TestDataLibTypeEnum.SQL.getCode())) {
            AnswerItem sqlResult = fetchDataSQL(lib, rowLimit, propertyName);
            result = (TestDataLibResult) sqlResult.getItem();
            msg = sqlResult.getResultMessage();

        } else if (lib.getType().equals(TestDataLibTypeEnum.SOAP.getCode())) {
            AnswerItem soapResult = fetchDataSOAP(lib);
            result = (TestDataLibResult) soapResult.getItem();
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

    private AnswerItem fetchDataSQL(TestDataLib lib, int rowLimit, String propertyName) {
        AnswerItem answer;
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        TestDataLibResult result = null;
        //sql data needs to collect the values for the n columns
        answer = sQLService.calculateOnDatabaseNColumns(lib.getScript(), lib.getDatabase(),
                lib.getSystem(), lib.getCountry(), lib.getEnvironment(), rowLimit, propertyName);

        MyLogger.log(TestDataLibService.class.getName(), Level.INFO, "Test data libs ervice SQL " + lib.getScript());

        //if the sql service returns a success message then we can process it
        if (answer.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_SQL.getCode()) {
            HashMap<String, String> columns = (HashMap<String, String>) answer.getItem();
            result = new TestDataLibResultSQL();
            result.setTestDataLibID(lib.getTestDataLibID());

            ((TestDataLibResultSQL) result).setData(columns);

        } else if (answer.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_SQL_NODATA.getCode()) {
            //if the script does not return 
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NODATA);
            msg.setDescription(msg.getDescription().replace("%ENTRY%", lib.getName()).replace("%SQL%", lib.getScript())
                    .replace("%DATABASE%", lib.getDatabase()));
        } else {
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
        if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()) {
            result = new TestDataLibResultSOAP();
            ((TestDataLibResultSOAP) result).setSoapResponseKey(key);
            result.setTestDataLibID(lib.getTestDataLibID());
            Document xmlDocument = xmlUnitService.getXmlDocument(key);
            ((TestDataLibResultSOAP) result).setData(xmlDocument);
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
