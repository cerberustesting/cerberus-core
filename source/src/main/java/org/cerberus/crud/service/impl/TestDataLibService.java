/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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

import org.apache.commons.fileupload.FileItem;
import org.cerberus.crud.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.crud.dao.ITestDataLibDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
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
    @Autowired
    private ITestCaseCountryPropertiesDAO testCaseCountryProperties;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(TestDataLibService.class);

    @Override
    public AnswerItem readByNameBySystemByEnvironmentByCountry(String name, String system, String environment, String country) {
        return testDataLibDAO.readByNameBySystemByEnvironmentByCountry(name, system, environment, country);
    }

    @Override
    public AnswerItem readByKey(int testDatalib) {
        return testDataLibDAO.readByKey(testDatalib);
    }
    
    @Override
    public Answer uploadFile(int id, FileItem file) {
        return testDataLibDAO.uploadFile(id, file);
    }

    @Override
    public AnswerList readNameListByName(String testDataLibName, int limit, boolean like) {
        return testDataLibDAO.readNameListByName(testDataLibName, limit, like);
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
        AnswerList answer = new AnswerList<>();
        AnswerList answerData = new AnswerList<>();
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
    public AnswerList<List<String>> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        return testDataLibDAO.readDistinctValuesByCriteria(searchTerm, individualSearch, columnName);
    }

    @Override
    public AnswerItem create(TestDataLib object) {
        return testDataLibDAO.create(object);
    }

    @Override
    public Answer delete(TestDataLib object) {
        return testDataLibDAO.delete(object);
    }

    @Override
    public Answer update(TestDataLib object) {
        return testDataLibDAO.update(object);
    }
    
    @Override
    public List<Answer> bulkRename(String oldName, String newName) {
    	// Call the 2 DAO updates
    	Answer answerDataLib = testDataLibDAO.bulkRenameDataLib(oldName,newName);
    	Answer answerProperties = testCaseCountryProperties.bulkRenameProperties(oldName,newName);
    	List<Answer> ansList = new ArrayList<Answer>();
    	ansList.add(answerDataLib);
    	ansList.add(answerProperties);
    	return ansList;
       // TO DO : get the updated numbers of datalib and properties
    }

    @Override
    public TestDataLib convert(AnswerItem<TestDataLib> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestDataLib) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestDataLib> convert(AnswerList<TestDataLib> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<TestDataLib>) answerList.getDataList();
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
