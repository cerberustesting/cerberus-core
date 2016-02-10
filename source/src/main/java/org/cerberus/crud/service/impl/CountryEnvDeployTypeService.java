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

import org.cerberus.crud.dao.ICountryEnvDeployTypeDAO;
import org.cerberus.crud.service.ICountryEnvDeployTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.cerberus.crud.entity.CountryEnvDeployType;
import org.cerberus.crud.entity.CountryEnvLink;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

@Service
public class CountryEnvDeployTypeService implements ICountryEnvDeployTypeService {

    @Autowired
    private ICountryEnvDeployTypeDAO countryEnvDeployTypeDAO;

    @Override
    public List<String> findJenkinsAgentByKey(String system, String country, String env, String deploy) {
        return this.countryEnvDeployTypeDAO.findJenkinsAgentByKey(system, country, env, deploy);
    }

    @Override
    public AnswerList readByVariousByCriteria(String system, String country, String environment, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return countryEnvDeployTypeDAO.readByVariousByCriteria(system, country, environment, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Answer create(CountryEnvDeployType object) {
        return countryEnvDeployTypeDAO.create(object);
    }

    @Override
    public Answer delete(CountryEnvDeployType object) {
        return countryEnvDeployTypeDAO.delete(object);
    }

    @Override
    public CountryEnvDeployType convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (CountryEnvDeployType) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<CountryEnvDeployType> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<CountryEnvDeployType>) answerList.getDataList();
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
