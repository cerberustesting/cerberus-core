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
package org.cerberus.core.crud.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.CountryEnvParam_log;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvParam_logDAO {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<CountryEnvParam_log> readByKey(long id);

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
     * @param individualSearch
     * @return
     */
    AnswerList<CountryEnvParam_log> readByVariousByCriteria(String system, String country, String environment, String build, String revision, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param system
     * @param country
     * @param nbdays
     * @param envGp
     * @return
     */
    AnswerList<CountryEnvParam_log> readLastChanges(String system, String country, Integer nbdays, String envGp);
    
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
     * @param resultSet
     * @return
     * @throws SQLException
     */
    CountryEnvParam_log loadFromResultSet(ResultSet resultSet) throws SQLException;

    /**
     * 
     * @param system
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return 
     */
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
