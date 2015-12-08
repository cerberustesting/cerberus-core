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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.crud.dao.ICountryEnvParamDAO;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.CountryEnvironmentApplication;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryCountryEnvParam;
import org.cerberus.crud.factory.IFactoryCountryEnvironmentApplication;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.ICountryEnvironmentApplicationService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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

    @Autowired
    ICountryEnvParamDAO countryEnvParamDao;
    @Autowired
    IFactoryCountryEnvParam countryEnvParamFactory;
    @Autowired
    IFactoryCountryEnvironmentApplication countryEnvironmentApplicationFactory;
    @Autowired
    ICountryEnvironmentApplicationService countryEnvironmentApplicationService;

    @Override
    public CountryEnvParam findCountryEnvParamByKey(String system, String country, String environment) throws CerberusException {
        return countryEnvParamDao.findCountryEnvParamByKey(system, country, environment);
    }

    @Override
    public List<CountryEnvParam> findCountryEnvParamByCriteria(CountryEnvParam countryEnvParam) throws CerberusException {
        return countryEnvParamDao.findCountryEnvParamByCriteria(countryEnvParam);

    }

    @Override
    public List<JSONObject> findActiveEnvironmentBySystemCountryApplication(String system, String country, String application) throws CerberusException {
        List<JSONObject> result = new ArrayList();
        CountryEnvParam countryEnvParam = countryEnvParamFactory.create(system, country, true);
        CountryEnvironmentApplication countryEnvironmentApplication = countryEnvironmentApplicationFactory.create(system, country, null, application, null, null, null, null);

        List<CountryEnvironmentApplication> ceaList = countryEnvironmentApplicationService.findCountryEnvironmentApplicationByCriteria(countryEnvironmentApplication);
        List<CountryEnvParam> ceList = this.findCountryEnvParamByCriteria(countryEnvParam);

        try {
            for (CountryEnvironmentApplication cea : ceaList) {
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
            Logger.getLogger(CountryEnvParamService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public List<CountryEnvParam> findAll(String system) throws CerberusException {
        return countryEnvParamDao.findAll(system);
    }

    @Override
    public void update_deprecated(CountryEnvParam cep) throws CerberusException {
        countryEnvParamDao.update_deprecated(cep);
    }

    @Override
    public void delete_deprecated(CountryEnvParam cep) throws CerberusException {
        countryEnvParamDao.delete_deprecated(cep);
    }

    @Override
    public void create_deprecated(CountryEnvParam cep) throws CerberusException {
        countryEnvParamDao.create_deprecated(cep);
    }

    @Override
    public List<CountryEnvParam> findListByCriteria(String system, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return countryEnvParamDao.findListByCriteria(system, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Integer count(String searchTerm, String inds) {
        return countryEnvParamDao.count(searchTerm, inds);
    }

    @Override
    public List<CountryEnvParam> findListByCriteria(String system) {
        return countryEnvParamDao.findListByCriteria(system);
    }

    @Override
    public AnswerItem readByKey(String system, String country, String environment) {
        return countryEnvParamDao.readByKey(system, country, environment);
    }

    @Override
    public AnswerList readActiveBySystem(String system) {
        return countryEnvParamDao.readActiveBySystem(system);
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        return countryEnvParamDao.readByCriteria(start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList readByVariousByCriteria(String system, String country, String environment, String build, String revision, String active, int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        return countryEnvParamDao.readByVariousByCriteria(system, country, environment, build, revision, active, start, amount, colName, dir, searchTerm, individualSearch);
    }

    @Override
    public boolean exist(String system, String country, String environment) {
//        try {
//            convert(readByKey(system, country, environment));
        return true;
//        } catch (CerberusException e) {
//            return false;
//        }
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
    public CountryEnvParam convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (CountryEnvParam) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<CountryEnvParam> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<CountryEnvParam>) answerList.getDataList();
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
