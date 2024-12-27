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
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvLink;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

/**
 * @author bcivel
 */
public interface ICountryEnvLinkDAO {

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    AnswerList<CountryEnvLink> readByVariousByCriteria(String system, String country, String environment, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param object
     * @return
     */
    Answer create(CountryEnvLink object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(CountryEnvLink object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(CountryEnvLink object);

    /**
     * Uses data of ResultSet to create object {@link Application}
     *
     * @param rs ResultSet relative to select from table countryenvlink
     * @return object {@link CountryEnvLink}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryCountryEnvLink
     */
    CountryEnvLink loadFromResultSet(ResultSet rs) throws SQLException;

}
