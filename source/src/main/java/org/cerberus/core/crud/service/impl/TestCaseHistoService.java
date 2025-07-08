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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.cerberus.core.crud.dao.ITestCaseHistoDAO;
import org.cerberus.core.crud.entity.TestCaseHisto;
import org.cerberus.core.crud.service.ITestCaseHistoService;
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
    public Answer create(TestCaseHisto testCaseHisto) {
        return testCaseHistoDao.create(testCaseHisto);
    }

}
