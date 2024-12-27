/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.crud.service.impl;

import java.util.*;

import org.apache.commons.fileupload.FileItem;
import org.cerberus.core.crud.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.core.crud.dao.ITestDataLibDAO;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.entity.TestDataLibData;
import org.cerberus.core.crud.factory.IFactoryTestDataLibData;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestDataLibDataService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
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
    public AnswerItem<TestDataLib> readByNameBySystemByEnvironmentByCountry(String name, String system, String environment, String country) {
        return testDataLibDAO.readByNameBySystemByEnvironmentByCountry(name, system, environment, country);
    }

    @Override
    public AnswerItem<TestDataLib> readByKey(int testDatalib) {
        return testDataLibDAO.readByKey(testDatalib);
    }

    @Override
    public Answer uploadFile(int id, FileItem file) {
        return testDataLibDAO.uploadFile(id, file);
    }

    @Override
    public AnswerList<TestDataLib> readNameListByName(String testDataLibName, int limit, boolean like) {
        return testDataLibDAO.readNameListByName(testDataLibName, limit, like);
    }

    @Override
    public AnswerList<TestDataLib> readAll() {
        return testDataLibDAO.readAll();
    }

    @Override
    public AnswerList<TestDataLib> readByVariousByCriteria(String name, List<String> systems, String environment, String country, String type, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return testDataLibDAO.readByVariousByCriteria(name, systems, environment, country, type, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<String> readDistinctGroups() {
        return testDataLibDAO.readDistinctGroups();
    }

    @Override
    public AnswerList<HashMap<String, String>> readINTERNALWithSubdataByCriteria(String dataName, String dataSystem, String dataCountry, String dataEnvironment, int rowLimit, String system, TestCaseExecution execution) {
        AnswerList<HashMap<String, String>> answer = new AnswerList<>();
        AnswerList<TestDataLib> answerDataLib = new AnswerList<>();
        AnswerList<TestDataLibData> answerData = new AnswerList<>();
        MessageEvent msg;

        List<HashMap<String, String>> result = new ArrayList<>();

        // We start by calculating the max nb of row we can fetch. Either specified by rowLimit either defined by a parameter.
        int maxSecurityFetch = parameterService.getParameterIntegerByKey("cerberus_testdatalib_fetchmax", system, 100);
        int maxFetch;
        if (rowLimit > 0 && rowLimit < maxSecurityFetch) {
            maxFetch = rowLimit;
        } else {
            maxFetch = maxSecurityFetch;
        }
        answerDataLib = this.readByVariousByCriteria(dataName, new ArrayList<>(Arrays.asList(dataSystem)), dataEnvironment, dataCountry, "INTERNAL", 0, maxFetch, null, null, null, null);
        List<TestDataLib> objectList = new ArrayList<>();
        objectList = answerDataLib.getDataList();
        for (TestDataLib tdl : objectList) {

            answerData = testDataLibDataService.readByVarious(tdl.getTestDataLibID(), null, null, null);
            List<TestDataLibData> objectDataList = new ArrayList<>();
            objectDataList = answerData.getDataList();
            HashMap<String, String> row = new HashMap<>();
            for (TestDataLibData tdld : objectDataList) {
                row.put(tdld.getSubData(), tdld.getValue());
                if (ParameterParserUtil.parseBooleanParam(tdld.getEncrypt(), false)) {
                    LOG.debug("Adding string to secret list : " + tdld.getSubData() + " - " + tdld.getValue() + " --> " + tdld.getEncrypt());
                    execution.addSecret(tdld.getValue());
                }
            }
            row.put("TestDataLibID", String.valueOf(tdl.getTestDataLibID()));
            result.add(row);
        }
        answer.setDataList(result);
        answer.setResultMessage(answerDataLib.getResultMessage());
        answer.setTotalRows(answerDataLib.getTotalRows());
        return answer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        return testDataLibDAO.readDistinctValuesByCriteria(searchTerm, individualSearch, columnName);
    }

    @Override
    public AnswerItem<TestDataLib> create(TestDataLib object) {
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
        Answer answerDataLib = testDataLibDAO.bulkRenameDataLib(oldName, newName);
        Answer answerProperties = testCaseCountryProperties.bulkRenameProperties(oldName, newName);
        List<Answer> ansList = new ArrayList<>();
        ansList.add(answerDataLib);
        ansList.add(answerProperties);
        return ansList;
        // TO DO : get the updated numbers of datalib and properties
    }

    @Override
    public TestDataLib convert(AnswerItem<TestDataLib> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestDataLib> convert(AnswerList<TestDataLib> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
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

    @Override
    public boolean userHasPermission(TestDataLib lib, String userName) {
        if ("Y".equals(lib.getPrivateData())) {
            if (!userName.equals(lib.getCreator())) {
                return false;
            }
        }
        return true;
    }

}
