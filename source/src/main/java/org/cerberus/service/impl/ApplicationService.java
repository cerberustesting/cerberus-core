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
package org.cerberus.service.impl;

import java.util.List;

import org.cerberus.dao.IApplicationDAO;
import org.cerberus.entity.Application;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IApplicationService;
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

    @Override
    public AnswerItem readByKey(String id) {
        return ApplicationDAO.readByKey(id);
    }

    @Override
    public Application readByKey_Deprecated(String Application) throws CerberusException {
        return ApplicationDAO.readByKey_Deprecated(Application);
    }

    @Override
    public List<Application> readAll_Deprecated() throws CerberusException {
        return ApplicationDAO.readAll_Deprecated();
    }

    @Override
    public List<Application> readBySystem_Deprecated(String System) throws CerberusException {
        return ApplicationDAO.readBySystem_Deprecated(System);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return ApplicationDAO.readBySystemByCriteria(null, startPosition, length, columnName, sort, searchParameter, string);
    }

    @Override
    public AnswerList readBySystemByCriteria(String system, int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return ApplicationDAO.readBySystemByCriteria(system, startPosition, length, columnName, sort, searchParameter, string);
    }

    @Override
    public boolean exist(String Application) {
        try {
            readByKey_Deprecated(Application);
            return true;
        } catch (CerberusException e) {
            return false;
        }
    }

    @Override
    public Answer create(Application application) {
        return ApplicationDAO.create(application);
    }

    @Override
    public Answer delete(Application application) {
        return ApplicationDAO.delete(application);
    }

    @Override
    public Answer update(Application application) {
        return ApplicationDAO.update(application);
    }

    @Override
    public List<String> readDistinctSystem() {
        return this.ApplicationDAO.readDistinctSystem();
    }
}
