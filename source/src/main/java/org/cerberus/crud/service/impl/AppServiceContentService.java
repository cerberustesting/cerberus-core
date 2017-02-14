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
import org.cerberus.crud.dao.IAppServiceContentDAO;

import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.service.IAppServiceContentService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
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
public class AppServiceContentService implements IAppServiceContentService {

    @Autowired
    private IAppServiceContentDAO AppServiceContentDAO;

    private static final Logger LOG = Logger.getLogger(AppServiceContentService.class);

    private final String OBJECT_NAME = "Service Content";

    @Override
    public AnswerItem readByKey(String service, String key) {
        return AppServiceContentDAO.readByKey(service, key);
    }

    @Override
    public AnswerList readAll() {
        return readByServiceByCriteria(null, null, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList readByVarious(String service, String active) {
        return AppServiceContentDAO.readByVariousByCriteria(service, active, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return AppServiceContentDAO.readByVariousByCriteria(null, null, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList readByServiceByCriteria(String service, String active, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return AppServiceContentDAO.readByVariousByCriteria(service, active, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public boolean exist(String service, String key) {
        AnswerItem objectAnswer = readByKey(service, key);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(AppServiceContent object) {
        return AppServiceContentDAO.create(object);
    }

    @Override
    public Answer delete(AppServiceContent object) {
        return AppServiceContentDAO.delete(object);
    }

    @Override
    public Answer update(AppServiceContent object) {
        return AppServiceContentDAO.update(object);
    }

    @Override
    public AppServiceContent convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (AppServiceContent) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<AppServiceContent> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<AppServiceContent>) answerList.getDataList();
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
    public AnswerList<String> readDistinctValuesByCriteria(String service, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return AppServiceContentDAO.readDistinctValuesByCriteria(service, searchParameter, individualSearch, columnName);
    }

}
