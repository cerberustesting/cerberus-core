/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.crud.dao;

import java.util.List;

import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface ICountryEnvironmentParametersDAO {

    CountryEnvironmentParameters findCountryEnvironmentParameterByKey(String system, String country, String environment, String application) throws CerberusException;

    List<String[]> getEnvironmentAvailable(String country, String application);

    List<CountryEnvironmentParameters> findCountryEnvironmentParametersByCriteria(CountryEnvironmentParameters countryEnvironmentParameter) throws CerberusException;

    List<String> getDistinctEnvironmentNames() throws CerberusException;

    /**
     * Find all CountryEnvironmentParameters by System
     *
     * @param system
     * @return
     * @throws CerberusException
     */
    List<CountryEnvironmentParameters> findAll(String system) throws CerberusException;

    /**
     * Update CountryEnvironmentParameters
     *
     * @param cea
     * @throws CerberusException
     */
    void update_deprecated(CountryEnvironmentParameters cea) throws CerberusException;

    /**
     * Delete CountryEnvironmentParameters
     *
     * @param cea
     * @throws CerberusException
     */
    void delete_deprecated(CountryEnvironmentParameters cea) throws CerberusException;

    /**
     * Create CountryEnvironmentParameters
     *
     * @param cea
     * @throws CerberusException
     */
    void create_deprecated(CountryEnvironmentParameters cea) throws CerberusException;

    Integer count(String searchTerm, String inds);

    List<CountryEnvironmentParameters> findListByCriteria(String system, String country, String env, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    List<CountryEnvironmentParameters> findListByCriteria(String system, String country, String environment);

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
    AnswerList readByVariousByCriteria(String system, String country, String environment, String application, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

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
