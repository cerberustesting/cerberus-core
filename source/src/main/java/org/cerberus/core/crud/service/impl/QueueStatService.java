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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.cerberus.core.crud.dao.IQueueStatDAO;
import org.cerberus.core.crud.entity.QueueStat;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.IQueueStatService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class QueueStatService implements IQueueStatService {

    @Autowired
    private IQueueStatDAO queueStatDAO;
    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger("QueueStat");

    private final String OBJECT_NAME = "QueueStat";

    @Override
    public AnswerList<QueueStat> readByCriteria(Date from, Date to) {
        Integer nbRows = readNbRowsByCriteria(from, to).getItem();
        LOG.debug("Total Rows : " + nbRows);
        Integer maxRows = parameterService.getParameterIntegerByKey("cerberus_queueshistorystatgraph_maxnbpoints", "", 1000);
        int modulo = 0;
        if (nbRows > maxRows) {
            modulo = nbRows / maxRows;
        }
        return queueStatDAO.readByCriteria(from, to, modulo);
    }

    @Override
    public AnswerItem<Integer> readNbRowsByCriteria(Date from, Date to) {
        return queueStatDAO.readNbRowsByCriteria(from, to);
    }

    @Override
    public Answer create(QueueStat object) {
        return queueStatDAO.create(object);
    }

    @Override
    public QueueStat convert(AnswerItem<QueueStat> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<QueueStat> convert(AnswerList<QueueStat> answerList) throws CerberusException {
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
