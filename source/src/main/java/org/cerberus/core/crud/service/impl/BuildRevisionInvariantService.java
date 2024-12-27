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

import org.cerberus.core.crud.dao.IBuildRevisionInvariantDAO;
import org.cerberus.core.crud.entity.BuildRevisionInvariant;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.IBuildRevisionInvariantService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuildRevisionInvariantService implements IBuildRevisionInvariantService {

    @Autowired
    private IBuildRevisionInvariantDAO BuildRevisionInvariantDAO;

    @Override
    public AnswerItem<BuildRevisionInvariant> readByKey(String system, Integer level, Integer seq) {
        return BuildRevisionInvariantDAO.readByKey(system, level, seq);
    }

    @Override
    public AnswerItem<BuildRevisionInvariant> readByKey(String system, Integer level, String versionName) {
        return BuildRevisionInvariantDAO.readByKey(system, level, versionName);
    }

    @Override
    public AnswerList<BuildRevisionInvariant> readBySystemByCriteria(List<String> system, Integer level, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return BuildRevisionInvariantDAO.readByVariousByCriteria(system, level, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<BuildRevisionInvariant> readBySystemLevel(List<String> system, Integer level) {
        return BuildRevisionInvariantDAO.readByVariousByCriteria(system, level, 0, 0, null, null, null, null);
    }

    @Override
    public AnswerList<BuildRevisionInvariant> readBySystem(List<String> system) {
        return BuildRevisionInvariantDAO.readByVariousByCriteria(system, -1, 0, 0, null, null, null, null);
    }

    @Override
    public boolean exist(String system, Integer level, Integer seq) {
        AnswerItem objectAnswer = readByKey(system, level, seq);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public boolean exist(String system, Integer level, String versionName) {
        AnswerItem objectAnswer = readByKey(system, level, versionName);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(BuildRevisionInvariant buildRevisionInvariant) {
        return BuildRevisionInvariantDAO.create(buildRevisionInvariant);
    }

    @Override
    public Answer delete(BuildRevisionInvariant buildRevisionInvariant) {
        return BuildRevisionInvariantDAO.delete(buildRevisionInvariant);
    }

    @Override
    public Answer update(String system, Integer level, Integer seq, BuildRevisionInvariant buildRevisionInvariant) {
        return BuildRevisionInvariantDAO.update(system, level, seq, buildRevisionInvariant);
    }

    @Override
    public BuildRevisionInvariant convert(AnswerItem<BuildRevisionInvariant> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<BuildRevisionInvariant> convert(AnswerList<BuildRevisionInvariant> answerList) throws CerberusException {
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
        return BuildRevisionInvariantDAO.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }

}
