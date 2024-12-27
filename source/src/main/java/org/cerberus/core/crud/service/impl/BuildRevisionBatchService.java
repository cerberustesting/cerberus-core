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
import org.cerberus.core.crud.dao.IBuildRevisionBatchDAO;
import org.cerberus.core.crud.entity.BuildRevisionBatch;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.factory.IFactoryBuildRevisionBatch;
import org.cerberus.core.crud.service.IBuildRevisionBatchService;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author vertigo17
 */
@Service
public class BuildRevisionBatchService implements IBuildRevisionBatchService {

    @Autowired
    private IBuildRevisionBatchDAO buildRevisionBatchDao;
    @Autowired
    private IFactoryBuildRevisionBatch buildRevisionBatchFactory;

    @Override
    public AnswerItem readByKey(Long id) {
        return buildRevisionBatchDao.readByKey(id);
    }

    @Override
    public AnswerList readAll() {
        return readByVariousByCriteria(null, null, null, 0, 0, "id", "asc", null, null);
    }

    @Override
    public AnswerList<BuildRevisionBatch> readByVariousByCriteria(String system, String country, String environment, int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return buildRevisionBatchDao.readByVariousByCriteria(system, country, environment, startPosition, length, columnName, sort, searchParameter, string);
    }

    @Override
    public boolean exist(Long id) {
        AnswerItem objectAnswer = readByKey(id);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(BuildRevisionBatch buildRevisionBatch) {
        return buildRevisionBatchDao.create(buildRevisionBatch);
    }

    @Override
    public Answer delete(BuildRevisionBatch buildRevisionBatch) {
        return buildRevisionBatchDao.delete(buildRevisionBatch);
    }

    @Override
    public Answer update(BuildRevisionBatch buildRevisionBatch) {
        return buildRevisionBatchDao.update(buildRevisionBatch);
    }

    @Override
    public Answer create(String system, String country, String environment, String build, String revision, String batch) {
        return this.create(buildRevisionBatchFactory.create(system, country, environment, build, revision, batch));
    }

    @Override
    public BuildRevisionBatch convert(AnswerItem<BuildRevisionBatch> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<BuildRevisionBatch> convert(AnswerList<BuildRevisionBatch> answerList) throws CerberusException {
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
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return buildRevisionBatchDao.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }

}
