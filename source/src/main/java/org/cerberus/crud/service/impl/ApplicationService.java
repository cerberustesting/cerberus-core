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

import org.cerberus.crud.dao.IApplicationDAO;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.IApplicationService;
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
public class ApplicationService implements IApplicationService {

    @Autowired
    private IApplicationDAO ApplicationDAO;

    private static final Logger LOG = Logger.getLogger("ApplicationService");

    private final String OBJECT_NAME = "Application";

    @Override
    public AnswerItem readByKey(String id) {
        return ApplicationDAO.readByKey(id);
    }

    @Override
    public AnswerList readAll() {
        return readBySystemByCriteria(null, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList readBySystem(String System) {
        return ApplicationDAO.readBySystemByCriteria(System, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return ApplicationDAO.readBySystemByCriteria(null, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList readBySystemByCriteria(String system, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return ApplicationDAO.readBySystemByCriteria(system, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem readTestCaseCountersBySystemByStatus(String system) {
        return this.ApplicationDAO.readTestCaseCountersBySystemByStatus(system);
    }

    @Override
    public boolean exist(String Object) {
        AnswerItem objectAnswer = readByKey(Object);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(Application object) {
        return ApplicationDAO.create(object);
    }

    @Override
    public Answer delete(Application object) {
        return ApplicationDAO.delete(object);
    }

    @Override
    public Answer update(Application object) {
        return ApplicationDAO.update(object);
    }

    @Override
    public AnswerList readDistinctSystem() {
        return this.ApplicationDAO.readDistinctSystem();
    }

    @Override
    public Application convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (Application) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Application> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<Application>) answerList.getDataList();
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
