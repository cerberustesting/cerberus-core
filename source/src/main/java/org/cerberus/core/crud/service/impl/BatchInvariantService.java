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

import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.dao.IBatchInvariantDAO;
import org.cerberus.core.crud.entity.BatchInvariant;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.IBatchInvariantService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BatchInvariantService implements IBatchInvariantService {

    @Autowired
    private IBatchInvariantDAO batchInvariantDAO;

    @Override
    public AnswerItem<BatchInvariant> readByKey(String batch) {
        return batchInvariantDAO.readByKey(batch);
    }

    @Override
    public AnswerList<BatchInvariant> readBySystemByCriteria(List<String> system, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return batchInvariantDAO.readBySystemByCriteria(system, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public boolean exist(String object) {
        AnswerItem objectAnswer = readByKey(object);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(BatchInvariant object) {
        return batchInvariantDAO.create(object);
    }

    @Override
    public Answer delete(BatchInvariant object) {
        return batchInvariantDAO.delete(object);
    }

    @Override
    public Answer update(String batch, BatchInvariant object) {
        return batchInvariantDAO.update(batch, object);
    }

    @Override
    public BatchInvariant convert(AnswerItem<BatchInvariant> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<BatchInvariant> convert(AnswerList<BatchInvariant> answerList) throws CerberusException {
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
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return batchInvariantDAO.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }

}
