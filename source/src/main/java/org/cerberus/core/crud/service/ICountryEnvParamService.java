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
package org.cerberus.core.crud.service;

import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvParamService {

    List<CountryEnvParam> findCountryEnvParamByCriteria(CountryEnvParam countryEnvParam) throws CerberusException;

    List<JSONObject> findActiveEnvironmentBySystemCountryApplication(String system, String country, String application) throws CerberusException;

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @return
     */
    AnswerItem<CountryEnvParam> readByKey(String system, String country, String environment);

    /**
     *
     * @param system
     * @return
     */
    public AnswerList<CountryEnvParam> readActiveBySystem(String system);

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    public AnswerList<CountryEnvParam> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param build
     * @param revision
     * @param Active
     * @param envGp
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    public AnswerList<CountryEnvParam> readByVariousByCriteria(List<String> system, String country, String environment, String build, String revision, String Active, String envGp, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> string);

    /**
     *
     * @param systems
     * @param country
     * @param environment
     * @param build
     * @param revision
     * @param Active
     * @return
     */
    public AnswerList<CountryEnvParam> readByVarious(String systems , String country, String environment, String build, String revision, String Active);

    /**
     *
     * @param systems
     * @param country
     * @param environment
     * @param build
     * @param revision
     * @param Active
     * @return
     */
    public AnswerList<CountryEnvParam> readDistinctEnvironmentByVarious(List<String> systems, String country, String environment, String build, String revision, String Active);

    /**
     *
     * @param systems
     * @param country
     * @param environment
     * @param build
     * @param revision
     * @param Active
     * @return
     */
    public AnswerList<CountryEnvParam> readDistinctCountryByVarious(List<String> systems, String country, String environment, String build, String revision, String Active);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(String system, String country, String environment);

    /**
     *
     * @param cep
     * @return
     */
    Answer create(CountryEnvParam cep);

    /**
     *
     * @param cep
     * @return
     */
    Answer delete(CountryEnvParam cep);

    /**
     *
     * @param cep
     * @return
     */
    Answer update(CountryEnvParam cep);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    CountryEnvParam convert(AnswerItem<CountryEnvParam> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<CountryEnvParam> convert(AnswerList<CountryEnvParam> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     * 
     * @param system
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return 
     */
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

}
