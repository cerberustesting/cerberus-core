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
package org.cerberus.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cerberus.entity.LogEvent;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface ILogEventDAO {

    /**
     *
     * @param logEventID
     * @return
     */
    AnswerItem readByKey(long logEventID);

    /**
     * @param start
     * @param amount
     * @param colName
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return a list of all LogEvent.
     */
    AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch);

    /**
     * Insert user into the database.
     *
     * @param logevent
     * @return true is log was inserted
     */
    Answer create(LogEvent logevent);

    /**
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    LogEvent loadFromResultSet(ResultSet resultSet) throws SQLException;

}
