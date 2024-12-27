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
import org.cerberus.core.crud.dao.IDeployTypeDAO;
import org.cerberus.core.crud.entity.DeployType;
import org.cerberus.core.crud.service.IDeployTypeService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeployTypeService implements IDeployTypeService {

    @Autowired
    private IDeployTypeDAO deployTypeDAO;

    @Override
    public AnswerItem<DeployType> readByKey(String deployType) {
        return deployTypeDAO.readByKey(deployType);
    }

    @Override
    public AnswerList<DeployType> readAll() {
        return deployTypeDAO.readAll();
    }

    @Override
    public AnswerList<DeployType> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return deployTypeDAO.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public Answer create(DeployType deployType) {
        return deployTypeDAO.create(deployType);
    }

    @Override
    public Answer delete(DeployType deployType) {
        return deployTypeDAO.delete(deployType);
    }

    @Override
    public Answer update(DeployType deployType) {
        return deployTypeDAO.update(deployType);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return deployTypeDAO.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

}
