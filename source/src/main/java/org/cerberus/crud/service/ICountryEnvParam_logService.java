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
package org.cerberus.crud.service;

import java.util.List;
import org.cerberus.crud.entity.CountryEnvParam_log;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvParam_logService {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem readByKey(Long id);

    /**
     *
     * @return
     */
    AnswerList readAll();

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param build
     * @param revision
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    AnswerList readByVariousByCriteria(String system, String country, String environment, String build, String revision, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param system
     * @param country
     * @param nbDays
     * @param envGp
     * @return
     */
    AnswerList readLastChanges(String system, String country, Integer nbDays, String envGp);

    /**
     *
     * @param countryEnvParamLog
     * @return true if project exist. false if not.
     */
    boolean exist(Long countryEnvParamLog);

    /**
     *
     * @param countryEnvParamLog
     * @return
     */
    Answer create(CountryEnvParam_log countryEnvParamLog);

    /**
     *
     * @param countryEnvParamLog
     * @return
     */
    Answer delete(CountryEnvParam_log countryEnvParamLog);

    /**
     *
     * @param countryEnvParamLog
     * @return
     */
    Answer update(CountryEnvParam_log countryEnvParamLog);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param description
     * @param creator
     * @return
     */
    Answer createLogEntry(String system, String country, String environment, String build, String revision, String description, String creator);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    CountryEnvParam_log convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<CountryEnvParam_log> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;
}
