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

import org.cerberus.crud.dao.ITestCaseCountryDAO;
import org.cerberus.crud.dao.ITestCaseExecutionInQueueDAO;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default {@link ITestCaseExecutionInQueueService} implementation
 *
 * @author abourdon
 */
@Service
public class TestCaseExecutionInQueueService implements ITestCaseExecutionInQueueService {

    @Autowired
    private ITestCaseExecutionInQueueDAO testCaseExecutionInQueueDAO;

    @Autowired
    private ITestCaseCountryDAO testCaseCountryDAO;

    @Override
    public boolean canInsert(TestCaseExecutionInQueue inQueue) throws CerberusException {
        try {
            testCaseCountryDAO.findTestCaseCountryByKey(inQueue.getTest(), inQueue.getTestCase(), inQueue.getCountry());
            return true;
        } catch (CerberusException ce) {
            MessageGeneral messageGeneral = ce.getMessageError();
            if (messageGeneral == null || messageGeneral.getCode() != MessageGeneralEnum.NO_DATA_FOUND.getCode()) {
                throw ce;
            }
            return false;
        }
    }

    @Override
    public void insert(TestCaseExecutionInQueue inQueue) throws CerberusException {
        testCaseExecutionInQueueDAO.insert(inQueue);
    }

    @Override
    public TestCaseExecutionInQueue getNextAndProceed() throws CerberusException {
        return testCaseExecutionInQueueDAO.getNextAndProceed();
    }

    @Override
    public List<TestCaseExecutionInQueue> getProceededByTag(String tag) throws CerberusException {
        return testCaseExecutionInQueueDAO.getProceededByTag(tag);
    }

    @Override
    public void remove(long id) throws CerberusException {
        testCaseExecutionInQueueDAO.remove(id);
    }

    @Override
    public List<TestCaseWithExecution> findTestCaseWithExecutionInQueuebyTag(String tag) throws CerberusException {
        return testCaseExecutionInQueueDAO.findTestCaseWithExecutionInQueuebyTag(tag);
    }

    @Override
    public TestCaseExecutionInQueue findByKey(long id) throws CerberusException {
        return testCaseExecutionInQueueDAO.findByKey(id);
    }

    @Override
    public List<TestCaseExecutionInQueue> findAllNotProcedeed() throws CerberusException {
        return testCaseExecutionInQueueDAO.getNotProceededAndProceed();
    }

    @Override
    public List<TestCaseExecutionInQueue> findAll() throws CerberusException {
        return testCaseExecutionInQueueDAO.findAll();
    }

    @Override
    public void setProcessedTo(Long l, String changeTo) throws CerberusException {
        testCaseExecutionInQueueDAO.setProcessedTo(l, changeTo);
    }

    @Override
    public void updateComment(Long queueId, String comment) throws CerberusException {
        testCaseExecutionInQueueDAO.updateComment(queueId, comment);
    }

    @Override
    public AnswerList readByTagByCriteria(String tag, int start, int amount, String column, String dir, String searchTerm, String individualSearch) throws CerberusException {
        return testCaseExecutionInQueueDAO.readByTagByCriteria(tag, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList readDistinctEnvCoutnryBrowserByTag(String tag) {
        return testCaseExecutionInQueueDAO.readDistinctEnvCoutnryBrowserByTag(tag);
    }

    @Override
    public AnswerList readDistinctColumnByTag(String tag, boolean env, boolean country, boolean browser, boolean app) {
        return testCaseExecutionInQueueDAO.readDistinctColumnByTag(tag, env, country, browser, app);
    }

    @Override
    public AnswerList findTagList(int tagnumber) {
        return testCaseExecutionInQueueDAO.findTagList(tagnumber);
    }
}
