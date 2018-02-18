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
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.cerberus.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.crud.service.ITestCaseExecutionFileService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionDataService implements ITestCaseExecutionDataService {

    @Autowired
    ITestCaseExecutionDataDAO testCaseExecutionDataDao;
    @Autowired
    ITestCaseExecutionFileService testCaseExecutionFileService;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionControlExecutionService.class);

    @Override
    public AnswerItem readByKey(long id, String property, int index) {
        return testCaseExecutionDataDao.readByKey(id, property, index);
    }

    @Override
    public AnswerList<TestCaseExecutionData> readByIdByCriteria(long id, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return testCaseExecutionDataDao.readByIdByCriteria(id, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerItem<TestCaseExecutionData> readLastCacheEntry(String system, String environment, String country, String property, int cacheExpire) {
        return testCaseExecutionDataDao.readLastCacheEntry(system, environment, country, property, cacheExpire);
    }

    @Override
    public AnswerList<TestCaseExecutionData> readById(long id) {
        return testCaseExecutionDataDao.readByIdByCriteria(id, 0, 0, "exd.id", "asc", null, null);
    }

    @Override
    public AnswerList<TestCaseExecutionData> readByIdWithDependency(long id) {
        AnswerList data = this.readByIdByCriteria(id, 0, 0, "exd.property", "asc", null, null);
        AnswerList response = null;
        List<TestCaseExecutionData> tcsaceList = new ArrayList();
        for (Object mydata : data.getDataList()) {

            TestCaseExecutionData tcsace = (TestCaseExecutionData) mydata;

            AnswerList files = testCaseExecutionFileService.readByVarious(id, tcsace.getProperty() + "-" + tcsace.getIndex());
            tcsace.setFileList((List<TestCaseExecutionFile>) files.getDataList());

            tcsaceList.add(tcsace);
        }
        response = new AnswerList(tcsaceList, data.getTotalRows());
        return response;

    }

    @Override
    public boolean exist(long id, String property, int index) {
        AnswerItem objectAnswer = readByKey(id, property, index);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public List<String> getPastValuesOfProperty(long id, String propName, String test, String testCase, String build, String environment, String country) {
        return testCaseExecutionDataDao.getPastValuesOfProperty(id, propName, test, testCase, build, environment, country);
    }

    @Override
    public List<String> getInUseValuesOfProperty(long id, String propName, String environment, String country, Integer timeoutInSecond) {
        return testCaseExecutionDataDao.getInUseValuesOfProperty(id, propName, environment, country, timeoutInSecond);
    }

    @Override
    public Answer create(TestCaseExecutionData object) {
        return testCaseExecutionDataDao.create(object);
    }

    @Override
    public Answer delete(TestCaseExecutionData object) {
        return testCaseExecutionDataDao.delete(object);
    }

    @Override
    public Answer update(TestCaseExecutionData object) {
        return testCaseExecutionDataDao.update(object);
    }

    @Override
    public TestCaseExecutionData convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestCaseExecutionData) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseExecutionData> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<TestCaseExecutionData>) answerList.getDataList();
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
    public Answer save(TestCaseExecutionData object) {
        if (this.exist(object.getId(), object.getProperty(), object.getIndex())) {
            return update(object);
        } else {
            return create(object);
        }
    }

}
