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

import java.util.List;

import org.cerberus.entity.LogEvent;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author vertigo
 */
public interface ILogEventDAO {

    /**
     * @return a list of all LogEvent.
     * @throws CerberusException in case no LogEvent can be found.
     */
    List<LogEvent> findAllLogEvent() throws CerberusException;

    /**
     * @return a list of all LogEvent.
     * @throws CerberusException in case no LogEvent can be found.
     */
    List<LogEvent> findAllLogEvent(int start, int amount, String colName, String dir, String searchTerm) throws CerberusException;

    /**
     *
     * @return Total number of LogEvent inside the database.
     * @throws CerberusException
     */
    Integer getNumberOfLogEvent(String searchTerm) throws CerberusException;

    /**
     * Insert user into the database.
     *
     * @param logevent
     * @return true is log was inserted
     * @throws CerberusException if we did not manage to insert the user.
     */
    boolean insertLogEvent(LogEvent logevent) throws CerberusException;
}
