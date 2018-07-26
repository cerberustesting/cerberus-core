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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.cerberus.crud.dao.ILabelDAO;
import org.cerberus.crud.entity.Label;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.service.ILabelService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.StringUtil;
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
public class LabelService implements ILabelService {

    @Autowired
    private ILabelDAO labelDAO;

    private static final Logger LOG = LogManager.getLogger("LabelService");

    private final String OBJECT_NAME = "Label";

    @Override
    public AnswerItem readByKey(Integer id) {
        return labelDAO.readByKey(id);
    }

    @Override
    public AnswerList readAll() {
        return readByVariousByCriteria(null, null, 0, 0, "Label", "asc", null, null);
    }

    @Override
    public AnswerList readBySystem(String System) {
        return labelDAO.readBySystemByCriteria(System, null, 0, 0, "Label", "asc", null, null);
    }

    @Override
    public AnswerList readByVarious(String system, String type) {
        return labelDAO.readBySystemByCriteria(system, type, 0, 0, "Label", "asc", null, null);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return labelDAO.readBySystemByCriteria(null, null, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList readByVariousByCriteria(String system, String type, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return labelDAO.readBySystemByCriteria(system, null, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public boolean exist(Integer id) {
        AnswerItem objectAnswer = readByKey(id);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(Label object) {
        Answer answerChecks = checkLabelParentconsistency(object);
        if (answerChecks == null) {
            return labelDAO.create(object);
        } else {
            return answerChecks;
        }
    }

    @Override
    public Answer delete(Label object) {
        return labelDAO.delete(object);
    }

    @Override
    public Answer update(Label object) {
        Answer answerChecks = checkLabelParentconsistency(object);
        if (answerChecks == null) {
            return labelDAO.update(object);
        } else {
            return answerChecks;
        }
    }

    private Answer checkLabelParentconsistency(Label object) {
        // If parent label exist we check that it is consistent.
        if (object.getParentLabelID() != 0) {
            Answer response = new Answer();
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_LABEL);
            // Getting parent label.
            AnswerItem<Label> answerLabelParent = readByKey(object.getParentLabelID());
            if ((answerLabelParent.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (answerLabelParent.getItem() != null)) {
                Label parentLabel = (Label) answerLabelParent.getItem();
                if ((!parentLabel.getSystem().equals(object.getSystem())) && (!StringUtil.isNullOrEmpty(parentLabel.getSystem()))) {
                    // Parent Label system is not empty and different from child label system.
                    msg.setDescription(msg.getDescription()
                            .replace("%LABEL%", object.getLabel())
                            .replace("%LABELPARENT%", parentLabel.getLabel())
                            .replace("%DESCRIPTION%", "Parent label does not belong to the same system as child."));
                    response.setResultMessage(msg);
                    return response;
                }
                if (!parentLabel.getType().equals(object.getType())) {
                    // Parent & Child have different types.
                    msg.setDescription(msg.getDescription()
                            .replace("%LABEL%", object.getLabel())
                            .replace("%LABELPARENT%", parentLabel.getLabel())
                            .replace("%DESCRIPTION%", "Cannot attach " + object.getType() + " Parent label to " + parentLabel.getType() + " child label. Types must be consistent."));
                    response.setResultMessage(msg);
                    return response;
                }
                if (object.getId() == object.getParentLabelID()) {
                    // Parent & Child have different types.
                    msg.setDescription(msg.getDescription()
                            .replace("%LABEL%", object.getLabel())
                            .replace("%LABELPARENT%", parentLabel.getLabel())
                            .replace("%DESCRIPTION%", "Label cannot be attached to itself."));
                    response.setResultMessage(msg);
                    return response;
                }
            } else {
                // Parent label does not exist.
                msg.setDescription(msg.getDescription()
                        .replace("%LABEL%", object.getLabel())
                        .replace("%LABELPARENT%", object.getParentLabelID().toString())
                        .replace("%DESCRIPTION%", "Parent label does not exist."));
                response.setResultMessage(msg);
                return response;
            }
        }
        return null;
    }

    @Override
    public Label convert(AnswerItem<Label> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (Label) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Label> convert(AnswerList<Label> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<Label>) answerList.getDataList();
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
    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return labelDAO.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }

}
