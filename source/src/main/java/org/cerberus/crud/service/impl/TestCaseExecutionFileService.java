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

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import org.cerberus.crud.dao.ITestCaseExecutionFileDAO;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionFile;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseExecutionFileService;
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
public class TestCaseExecutionFileService implements ITestCaseExecutionFileService {

    @Autowired
    private ITestCaseExecutionFileDAO testCaseExecutionFileDAO;
    @Autowired
    private IFactoryTestCaseExecutionFile testCaseExecutionFileFactory;

    private static final Logger LOG = Logger.getLogger("TestCaseExecutionFileService");

    private final String OBJECT_NAME = "TestCaseExecutionFile";

    @Override
    public AnswerItem<TestCaseExecutionFile> readByKey(long id) {
        return testCaseExecutionFileDAO.readByKey(id);
    }

    @Override
    public AnswerList<List<TestCaseExecutionFile>> readByVariousByCriteria(long id, String level, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return testCaseExecutionFileDAO.readByVariousByCriteria(id, level, length, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<List<TestCaseExecutionFile>> readByVarious(long id, String level) {
        return testCaseExecutionFileDAO.readByVariousByCriteria(id, level, 0, 0, null, null, null, null);
    }

    @Override
    public Answer create(long exeid, String level, String fileDesc, String fileName, String fileType, String usrCreated) {
        TestCaseExecutionFile object = null;
        object = testCaseExecutionFileFactory.create(0, exeid, level, fileDesc, fileName, fileType, usrCreated, null, "", null);
        return testCaseExecutionFileDAO.create(object);
    }

    @Override
    public boolean exist(long id) {
        AnswerItem objectAnswer = readByKey(id);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(TestCaseExecutionFile object) {
        return testCaseExecutionFileDAO.create(object);
    }

    @Override
    public Answer delete(TestCaseExecutionFile object) {
        return testCaseExecutionFileDAO.delete(object);
    }

    @Override
    public Answer update(TestCaseExecutionFile object) {
        return testCaseExecutionFileDAO.update(object);
    }

    @Override
    public TestCaseExecutionFile convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestCaseExecutionFile) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseExecutionFile> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<TestCaseExecutionFile>) answerList.getDataList();
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
