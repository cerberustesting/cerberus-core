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
import java.util.List;

import org.cerberus.entity.Application;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * Interface that defines the public methods to manage Application data on table
 * Insert, Delete, Update, Find
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
public interface IApplicationDAO {

    public AnswerItem readByKey(String application);

    /**
     * Finds the Application by the name
     *
     * @param application name of the Application to find.
     * @return Object application if exist.
     * @throws CerberusException when Application does not exist.
     * @since 0.9.0
     */
    Application readByKey_Deprecated(String application) throws CerberusException;

    /**
     * Finds all Applications that exists
     *
     * @return List of applications.
     * @throws CerberusException when no application exist.
     * @since 0.9.0
     */
    List<Application> readAll_Deprecated() throws CerberusException;

    /**
     * Finds Applications of the given system
     *
     * @param system Name of the System to filter
     * @return List of application.
     * @throws CerberusException
     * @since 0.9.0
     */
    List<Application> readBySystem_Deprecated(String system) throws CerberusException;

    public AnswerList readBySystemByCriteria(String system, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    public Answer create(Application application);

    public Answer delete(Application application);

    public Answer update(Application application);

    /**
     *
     * @return @throws CerberusException
     * @since 0.9.1
     */
    List<String> readDistinctSystem();

    /**
     * Uses data of ResultSet to create object {@link Application}
     *
     * @param rs ResultSet relative to select from table Application
     * @return object {@link Application}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryApplication
     */
    public Application loadFromResultSet(ResultSet rs) throws SQLException;

}
