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

import org.cerberus.crud.entity.Invariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
public interface IInvariantDAO {

    Invariant readByKey(String idName, String value) throws CerberusException;

    Invariant readByIdnameBySort(String idName, Integer sort) throws CerberusException;

    AnswerList readByIdname(String idName);

    AnswerList readByIdnameByGp1(String idName, String gp);

    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch, String PublicPrivateFilter);

    public Integer getNumberOfInvariant(String searchTerm, String PublicPrivateFilter) throws CerberusException;

    public void create(Invariant invariant) throws CerberusException;

    public void delete(Invariant invariant) throws CerberusException;

    public void update(Invariant invariant) throws CerberusException;
}
