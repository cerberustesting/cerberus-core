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

import java.util.List;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.exception.CerberusException;
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
public interface ICountryEnvironmentParametersDAO {

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param application
     * @return
     */
    AnswerItem<CountryEnvironmentParameters> readByKey(String system, String country, String environment, String application);

    /**
     *
     * @param application
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    List<CountryEnvironmentParameters> readByKeyByApplication(String application) throws CerberusException;

    /**
     *
     * @param country
     * @param application
     * @return
     */
    List<String[]> getEnvironmentAvailable(String country, String application);

    /**
     *
     * @param countryEnvironmentParameter
     * @return
     * @throws CerberusException
     */
    List<CountryEnvironmentParameters> findCountryEnvironmentParametersByCriteria(CountryEnvironmentParameters countryEnvironmentParameter) throws CerberusException;

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param application
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<CountryEnvironmentParameters> readByVariousByCriteria(String system, String country, String environment, String application, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @return
     */
    AnswerList<CountryEnvironmentParameters> readDependenciesByVarious(String system, String country, String environment);

    /**
     *
     * @param object
     * @return
     */
    Answer create(CountryEnvironmentParameters object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(CountryEnvironmentParameters object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(CountryEnvironmentParameters object);

}
