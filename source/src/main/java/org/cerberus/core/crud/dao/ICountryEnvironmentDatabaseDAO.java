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

import org.cerberus.core.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface ICountryEnvironmentDatabaseDAO {

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param database
     * @return
     */
    AnswerItem<CountryEnvironmentDatabase> readByKey(String system, String country, String environment, String database);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<CountryEnvironmentDatabase> readByVariousByCriteria(String system, String country, String environment, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param object
     * @return
     */
    Answer create(CountryEnvironmentDatabase object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(CountryEnvironmentDatabase object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(CountryEnvironmentDatabase object);

}
