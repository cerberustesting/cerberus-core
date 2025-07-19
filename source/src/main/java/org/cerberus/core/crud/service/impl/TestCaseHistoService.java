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

import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.cerberus.core.crud.dao.ITestCaseHistoDAO;
import org.cerberus.core.crud.entity.TestCaseHisto;
import org.cerberus.core.crud.service.ITestCaseHistoService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerList;

/**
 * @author vertigo17
 */
@Service
public class TestCaseHistoService implements ITestCaseHistoService {

    private static final Logger LOG = LogManager.getLogger(TestCaseHistoService.class);

    @Autowired
    private ITestCaseHistoDAO testCaseHistoDao;

    @Override
    public TestCaseHisto readByKey(String test, String testCase, int version) throws CerberusException {
        return testCaseHistoDao.readByKey(test, testCase, version);
    }

    @Override
    public AnswerList<TestCaseHisto> readByTestCase(String test, String testCase) {
        return testCaseHistoDao.readByTestCase(test, testCase);
    }

    @Override
    public List<TestCaseHisto> readByDate(Date from, Date to) throws CerberusException{
        return this.convert(testCaseHistoDao.readByDate(from, to));
    }

    @Override
    public Answer create(TestCaseHisto testCaseHisto) {
        return testCaseHistoDao.create(testCaseHisto);
    }
    
    
    @Override
    public TestCaseHisto convert(AnswerItem<TestCaseHisto> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseHisto> convert(AnswerList<TestCaseHisto> answerList) throws CerberusException {
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

    
    

}
