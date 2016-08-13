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
import java.util.Map;
import org.cerberus.crud.dao.ITestDataLibDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestDataLibService implements ITestDataLibService {

    @Autowired
    private DatabaseSpring dbManager;
    @Autowired
    private ITestDataLibDAO testDataLibDAO;
    @Autowired
    private IFactoryTestDataLibData testDataLibDataFactory;
    @Autowired
    private ITestDataLibDataService testDataLibDataService;
    @Autowired
    private IParameterService parameterService;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TestDataLibService.class);

    @Override
    public AnswerItem readByNameBySystemByEnvironmentByCountry(String name, String system, String environment, String country, String type) {
        return testDataLibDAO.readByNameBySystemByEnvironmentByCountry(name, system, environment, country, type);
    }

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
    public AnswerList readByVariousByCriteria(String name, String system, String environment, String country, String type, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return testDataLibDAO.readByVariousByCriteria(name, system, environment, country, type, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<String> readDistinctGroups() {
        return testDataLibDAO.readDistinctGroups();
    }

    @Override
    public AnswerList<HashMap<String, String>> readINTERNALWithSubdataByCriteria(String dataName, String dataSystem, String dataCountry, String dataEnvironment, int rowLimit, String system) {
        AnswerList answer = new AnswerList();
        AnswerList answerData = new AnswerList();
        MessageEvent msg;

        List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

        // We start by calculating the max nb of row we can fetch. Either specified by rowLimit either defined by a parameter.
        int maxSecurityFetch = 100;
        try {
            String maxSecurityFetch1 = parameterService.findParameterByKey("cerberus_testdatalib_fetchmax", system).getValue();
            maxSecurityFetch = Integer.valueOf(maxSecurityFetch1);
        } catch (CerberusException ex) {
            LOG.error(ex);
        }
        int maxFetch = maxSecurityFetch;
        if (rowLimit > 0 && rowLimit < maxSecurityFetch) {
            maxFetch = rowLimit;
        } else {
            maxFetch = maxSecurityFetch;
        }
        answer = this.readByVariousByCriteria(dataName, dataSystem, dataEnvironment, dataCountry, "INTERNAL", 0, maxFetch, null, null, null, null);
        List<TestDataLib> objectList = new ArrayList<TestDataLib>();
        objectList = answer.getDataList();
        for (TestDataLib tdl : objectList) {

            answerData = testDataLibDataService.readByVarious(tdl.getTestDataLibID(), null, null, null);
            List<TestDataLibData> objectDataList = new ArrayList<TestDataLibData>();
            objectDataList = answerData.getDataList();
            HashMap<String, String> row = new HashMap<String, String>();
            for (TestDataLibData tdld : objectDataList) {
                row.put(tdld.getSubData(), tdld.getValue());
            }
            row.put("TestDataLibID", String.valueOf(tdl.getTestDataLibID()));
            result.add(row);
        }
        answer.setDataList(result);
        return answer;
    }

    @Override
    public void create(TestDataLib testDataLib) {
        testDataLibDAO.create(testDataLib);
    }

    @Override
    public Answer create(TestDataLib testDataLib, List<TestDataLibData> subDataList) {
        List<TestDataLibData> completeSubDataList = new ArrayList<TestDataLibData>();

        //validates if the subdata are not duplicated
        Answer answer = testDataLibDataService.validate(completeSubDataList);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR.getCode())) {
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
                            libData.getColumn(), libData.getParsingAnswer(), libData.getColumnPosition(), libData.getDescription());
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
                                libData.getColumn(), libData.getParsingAnswer(), libData.getColumnPosition(),libData.getDescription());
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
    public Answer duplicate(TestDataLib lib) {
        Answer answer;
        int originalID = lib.getTestDataLibID();
        dbManager.beginTransaction();
        //get all records from testdatalibdata that belong to the the lib that we are trying to duplicate

        answer = testDataLibDAO.create(lib);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            answer = testDataLibDataService.readByVarious(originalID, null, null, null);
            if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                //if there were no problems retrieving the sub-data list
                //gets the subdatalist
                List<TestDataLibData> originalList = (List<TestDataLibData>) ((AnswerList) answer).getDataList();
                List<TestDataLibData> newList = new ArrayList<TestDataLibData>();
                if (originalList != null && !originalList.isEmpty()) {
                    for (TestDataLibData libData : originalList) {
                        TestDataLibData data = testDataLibDataFactory.create(-1, lib.getTestDataLibID(), libData.getSubData(), libData.getValue(),
                                libData.getColumn(), libData.getParsingAnswer(),libData.getColumnPosition(), libData.getDescription());
                        newList.add(data);
                    }
                    answer = testDataLibDataService.create(newList);
                }
            }
        }

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            dbManager.commitTransaction();
        } else {
            dbManager.abortTransaction();
        }

        return answer;
    }

    @Override
    public AnswerList<List<String>> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        return testDataLibDAO.readDistinctValuesByCriteria(searchTerm, individualSearch, columnName);
    }
}
