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
import java.util.Map;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
public interface IInvariantDAO {

    /**
     * Get a {@link Invariant} in database
     *
     * @param id
     * @param value
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    Invariant readByKey(String id, String value) throws CerberusException;

    /**
     * Get a {@link Invariant} in database
     *
     * @param id
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    Invariant readFirstByIdName(String id) throws CerberusException;

    /**
     * @param idName
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    List<Invariant> readByIdname(String idName) throws CerberusException;

    /**
     * @param idName
     * @param gp
     * @return
     */
    AnswerList<Invariant> readByIdnameByGp1(String idName, String gp);

    /**
     * @param idName
     * @param gp
     * @return
     */
    AnswerList<Invariant> readByIdnameByNotGp1(String idName, String gp);

    /**
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @param PublicPrivateFilter
     * @return
     */
    public AnswerList<Invariant> readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch, String PublicPrivateFilter);

    /**
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @param PublicPrivateFilter
     * @return
     */
    public AnswerList<Invariant> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String PublicPrivateFilter);

    /**
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @param PublicPrivateFilter
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String PublicPrivateFilter, String columnName);

    /**
     * Getting the list of country invariant for which exist at least 1 change
     * performed before nbdays parameters in the corresonding system
     *
     * @param system
     * @param nbdays
     * @return
     */
    public AnswerList<Invariant> readCountryListEnvironmentLastChanges(String system, Integer nbdays);

    /**
     * Create an {@link Invariant} in database
     *
     * @param object
     * @return
     */
    Answer create(Invariant object);

    /**
     * Delete an {@link Invariant} in database
     *
     * @param object
     * @return
     */
    Answer delete(Invariant object);

    /**
     * Update an {@link Invariant} in database
     *
     * @param idname
     * @param value
     * @param object
     * @return
     */
    Answer update(String idname, String value, Invariant object);
}
