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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IApplicationDAO;
import org.cerberus.core.crud.dao.ICountryEnvironmentParametersDAO;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
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
public class ApplicationService implements IApplicationService {

    @Autowired
    private IApplicationDAO applicationDAO;
    @Autowired
    private ICountryEnvironmentParametersDAO countryEnvironnementParametersDAO;

    private static final Logger LOG = LogManager.getLogger("ApplicationService");

    private final String OBJECT_NAME = "Application";

    @Override
    public AnswerItem<Application> readByKey(String id) {
        return applicationDAO.readByKey(id);
    }

    @Override
    public Application readByKeyWithDependency(String id) throws CerberusException {
        Application appli = null;
        AnswerItem<Application> app = applicationDAO.readByKey(id);
        if (app.getItem() != null) {
            appli = app.getItem();
            List<CountryEnvironmentParameters> env = countryEnvironnementParametersDAO.readByKeyByApplication(id);
            appli.setEnvironmentList(env);
        }
        return appli;
    }

    @Override
    public AnswerList<Application> readAll() {
        return readBySystemByCriteria(null, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public Integer getNbApplications(List<String> systems) {
        return applicationDAO.getNbApplications(systems);
    }

    @Override
    public AnswerList<Application> readBySystem(List<String> system) {
        return applicationDAO.readBySystemByCriteria(system, 0, 0, "sort", "asc", null, null);
    }

    @Override
    public AnswerList<Application> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return applicationDAO.readBySystemByCriteria(null, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<Application> readBySystemByCriteria(List<String> system, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return applicationDAO.readBySystemByCriteria(system, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem<HashMap<String, HashMap<String, Integer>>> readTestCaseCountersBySystemByStatus(List<String> system) {
        return this.applicationDAO.readTestCaseCountersBySystemByStatus(system);
    }

    @Override
    public boolean exist(String Object) {
        AnswerItem objectAnswer = readByKey(Object);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(Application object) {
        return applicationDAO.create(object);
    }

    @Override
    public Answer delete(Application object) {
        return applicationDAO.delete(object);
    }

    @Override
    public Answer update(String application, Application object) {
        return applicationDAO.update(application, object);
    }

    @Override
    public AnswerList readDistinctSystem() {
        return this.applicationDAO.readDistinctSystem();
    }

    @Override
    public Application convert(AnswerItem<Application> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Application> convert(AnswerList<Application> answerList) throws CerberusException {
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
        return applicationDAO.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }

}
