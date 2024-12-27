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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.ITagSystemDAO;
import org.cerberus.core.crud.entity.TagSystem;
import org.cerberus.core.crud.factory.IFactoryTagSystem;
import org.cerberus.core.crud.service.ITagSystemService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
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
public class TagSystemService implements ITagSystemService {

    @Autowired
    private ITagSystemDAO tagSystemDAO;
    @Autowired
    private IFactoryTagSystem factoryTagSystem;

    private static final Logger LOG = LogManager.getLogger("TagSystemService");

    private final String OBJECT_NAME = "TagSystem";

    private List<String> tagSystemCache = new ArrayList<>();

    @Override
    public List<String> getTagSystemCache() {
        return tagSystemCache;
    }

    @Override
    public void purgeTagSystemCache() {
        tagSystemCache.clear();
    }

    @Override
    public AnswerItem<TagSystem> readByKey(String tag, String system) {
        return tagSystemDAO.readByKey(tag, system);
    }

    @Override
    public AnswerList<TagSystem> readAll() {
        return tagSystemDAO.readByVariousByCriteria(null, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList readBySystem(String system) {
        return tagSystemDAO.readByVariousByCriteria(system, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return tagSystemDAO.readByVariousByCriteria(null, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList readByVariousByCriteria(String system, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return tagSystemDAO.readByVariousByCriteria(system, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public boolean exist(String tag, String system) {
        AnswerItem objectAnswer = readByKey(tag, system);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer createIfNotExist(String tag, String system, String user) {
        if (!StringUtil.isEmptyOrNull(tag) && !StringUtil.isEmptyOrNull(system)) {
            String keyCacheEntry = tag + "//!//" + system;
            if (!tagSystemCache.contains(keyCacheEntry) && !exist(tag, system)) {
                tagSystemCache.add(keyCacheEntry);
                return create(factoryTagSystem.create(tag, system, user, null, "", null));
            }
        }
        return null;
    }

    @Override
    public Answer create(TagSystem object) {
        return tagSystemDAO.create(object);
    }

    @Override
    public Answer delete(TagSystem object) {
        return tagSystemDAO.delete(object);
    }

    @Override
    public Answer update(String tag, String system, TagSystem object) {
        return tagSystemDAO.update(tag, system, object);
    }

    @Override
    public TagSystem convert(AnswerItem<TagSystem> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TagSystem> convert(AnswerList<TagSystem> answerList) throws CerberusException {
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
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return tagSystemDAO.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

}
