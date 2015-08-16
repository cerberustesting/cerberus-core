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
    public Application findApplicationByKey(String Application) throws CerberusException {
        return ApplicationDAO.findApplicationByKey(Application);
    }

    @Override
    public List<Application> findAllApplication() throws CerberusException {
        return ApplicationDAO.findAllApplication();
    }

    @Override
    public List<Application> findApplicationBySystem(String System) throws CerberusException {
        return ApplicationDAO.findApplicationBySystem(System);
    }

    @Override
    public AnswerList findApplicationListByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return ApplicationDAO.findApplicationListByCriteria(startPosition, length, columnName, sort, searchParameter, string);
    }
    
    @Override
    public AnswerItem findApplicationByString(String id) {
        return ApplicationDAO.findApplicationByString(id);
    }

    @Override
    public boolean updateApplication(Application application) throws CerberusException {
        return ApplicationDAO.updateApplication(application);
    }

    @Override
    public void createApplication(Application application) throws CerberusException {
        ApplicationDAO.createApplication(application);
    }

    @Override
    public void deleteApplication(Application application) throws CerberusException {
        ApplicationDAO.deleteApplication(application);
    }

    @Override
    public boolean isApplicationExist(String Application) {
        try {
            findApplicationByKey(Application);
            return true;
        } catch (CerberusException e) {
            return false;
        }
    }

    @Override
    public List<String> findDistinctSystem() {
        return this.ApplicationDAO.findDistinctSystem();
    }
}
