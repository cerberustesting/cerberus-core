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
import java.util.Map;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * @author bcivel
 */
public interface ICountryEnvParamDAO {

    List<CountryEnvParam> findCountryEnvParamByCriteria(CountryEnvParam countryEnvParam) throws CerberusException;

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @return
     */
    public AnswerItem readByKey(String system, String country, String environment);

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
     * @param country
     * @param environment
     * @param build
     * @param revision
     * @param active
     * @param envGp
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    public AnswerList readByVariousByCriteria(String system, String country, String environment, String build, String revision, String active, String envGp, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> string);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param build
     * @param revision
     * @param active
     * @param envGp
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    public AnswerList readDistinctEnvironmentByVariousByCriteria(String system, String country, String environment, String build, String revision, String active, String envGp, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

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

}
