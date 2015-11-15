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
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;

/**
 * @author bcivel
 */
public interface ICountryEnvParamDAO {

    CountryEnvParam findCountryEnvParamByKey(String system, String country, String environment) throws CerberusException;

    List<CountryEnvParam> findCountryEnvParamByCriteria(CountryEnvParam countryEnvParam) throws CerberusException;

    /**
     * Find all countryEnvParam by System
     *
     * @param system
     * @return
     * @throws CerberusException
     */
    List<CountryEnvParam> findAll(String system) throws CerberusException;

    /**
     * Update countryEnvParam
     *
     * @param cep
     * @throws CerberusException
     */
    void update(CountryEnvParam cep) throws CerberusException;

    /**
     * Delete countryEnvParam
     *
     * @param cep
     * @throws CerberusException
     */
    void delete(CountryEnvParam cep) throws CerberusException;

    /**
     * Create countryEnvParam
     *
     * @param cep
     * @throws CerberusException
     */
    void create(CountryEnvParam cep) throws CerberusException;

    /**
     *
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    public List<CountryEnvParam> findListByCriteria(String system, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param searchTerm
     * @param inds
     * @return
     */
    public Integer count(String searchTerm, String inds);

    public List<CountryEnvParam> findListByCriteria(String system);

    /**
     *
     * @param system
     * @return
     */
    public AnswerList readActiveBySystem(String system);

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
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param system
     * @param active
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    public AnswerList readByVariousByCriteria(String system, String active, int startPosition, int length, String columnName, String sort, String searchParameter, String string);
}
