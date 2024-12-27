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

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ICountryEnvParamDAO;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.factory.IFactoryCountryEnvParam;
import org.cerberus.core.crud.factory.IFactoryCountryEnvironmentParameters;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class CountryEnvParamService implements ICountryEnvParamService {

    private static final Logger LOG = LogManager.getLogger(CountryEnvParamService.class);

    @Autowired
    ICountryEnvParamDAO countryEnvParamDao;
    @Autowired
    IFactoryCountryEnvParam countryEnvParamFactory;
    @Autowired
    IFactoryCountryEnvironmentParameters countryEnvironmentParametersFactory;
    @Autowired
    ICountryEnvironmentParametersService countryEnvironmentParametersService;

    @Override
    public List<CountryEnvParam> findCountryEnvParamByCriteria(CountryEnvParam countryEnvParam) throws CerberusException {
        return countryEnvParamDao.findCountryEnvParamByCriteria(countryEnvParam);
    }

    @Override
    public List<JSONObject> findActiveEnvironmentBySystemCountryApplication(String system, String country, String application) throws CerberusException {
        List<JSONObject> result = new ArrayList<>();
        CountryEnvParam countryEnvParam = countryEnvParamFactory.create(system, country, true);
        CountryEnvironmentParameters countryEnvironmentParameters = countryEnvironmentParametersFactory.create(system, country, null, application, true, null, null, null, null, null, null, null, null, null, null, 
                CountryEnvironmentParameters.DEFAULT_POOLSIZE, null, null, null, null, null, null);

        List<CountryEnvironmentParameters> ceaList = countryEnvironmentParametersService.findCountryEnvironmentParametersByCriteria(countryEnvironmentParameters);
        List<CountryEnvParam> ceList = this.findCountryEnvParamByCriteria(countryEnvParam);

        try {
            for (CountryEnvironmentParameters cea : ceaList) {
                for (CountryEnvParam ce : ceList) {
                    if (cea.getEnvironment().equals(ce.getEnvironment())) {

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("environment", ce.getEnvironment());
                        jsonObject.put("build", ce.getBuild());
                        jsonObject.put("revision", ce.getRevision());
                        jsonObject.put("ip", cea.getIp());
                        jsonObject.put("url", cea.getUrl());
                        result.add(jsonObject);

                    }
                }
            }
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return result;
    }

    @Override
    public AnswerItem<CountryEnvParam> readByKey(String system, String country, String environment) {
        return countryEnvParamDao.readByKey(system, country, environment);
    }

    @Override
    public AnswerList<CountryEnvParam> readActiveBySystem(String system) {
        return countryEnvParamDao.readActiveBySystem(system);
    }

    @Override
    public AnswerList<CountryEnvParam> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        return countryEnvParamDao.readByCriteria(start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<CountryEnvParam> readByVariousByCriteria(List<String> systems, String country, String environment, String build, String revision, String active, String envGp, int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return countryEnvParamDao.readByVariousByCriteria(systems, country, environment, build, revision, active, envGp, start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<CountryEnvParam> readByVarious(String system, String country, String environment, String build, String revision, String active) {
        return countryEnvParamDao.readByVariousByCriteria(new ArrayList<>(Arrays.asList(system)), country, environment, build, revision, active, null, 0, 0, null, null, null, null);
    }

    @Override
    public AnswerList<CountryEnvParam> readDistinctEnvironmentByVarious(List<String> systems, String country, String environment, String build, String revision, String active) {
        return countryEnvParamDao.readDistinctEnvironmentByVariousByCriteria(systems, country, environment, build, revision, active, null, 0, 0, null, null, null, null);
    }

    @Override
    public AnswerList<CountryEnvParam> readDistinctCountryByVarious(List<String> systems, String country, String environment, String build, String revision, String active) {
        return countryEnvParamDao.readDistinctCountryByVariousByCriteria(systems, country, environment, build, revision, active, null, 0, 0, null, null, null, null);
    }

    @Override
    public boolean exist(String system, String country, String environment) {
        AnswerItem objectAnswer = readByKey(system, country, environment);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(CountryEnvParam cep) {
        return countryEnvParamDao.create(cep);
    }

    @Override
    public Answer delete(CountryEnvParam cep) {
        return countryEnvParamDao.delete(cep);
    }

    @Override
    public Answer update(CountryEnvParam cep) {
        return countryEnvParamDao.update(cep);
    }

    @Override
    public CountryEnvParam convert(AnswerItem<CountryEnvParam> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<CountryEnvParam> convert(AnswerList<CountryEnvParam> answerList) throws CerberusException {
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
        return countryEnvParamDao.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }
}
