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
package com.redcats.tst.dao;

import com.redcats.tst.entity.Application;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 * Interface that defines the public methods to manage Application data on table
 * Insert, Delete, Update, Find
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
public interface IApplicationDAO {

    /**
     * Finds the Application by the name
     *
     * @param application name of the Application to find.
     * @return Object application if exist.
     * @throws CerberusException when Application does not exist.
     * @since 0.9.0
     */
    Application findApplicationByKey(String application) throws CerberusException;

    /**
     * Finds all Applications that exists
     *
     * @return List of applications.
     * @throws CerberusException when no application exist.
     * @since 0.9.0
     */
    List<Application> findAllApplication() throws CerberusException;

    /**
     * Finds Applications of the given system
     *
     * @param system Name of the System to filter
     * @return List of application.
     * @throws CerberusException
     * @since 0.9.0
     */
    List<Application> findApplicationBySystem(String system) throws CerberusException;

    /**
     *
     * @return
     * @throws CerberusException
     * @since 0.9.1
     */
    List<String> findDistinctSystem();

    /**
     * Updates the information based on the object application
     *
     * @param application Object Application to update.
     * @return true if updated successfully and false if no row updated
     * @throws CerberusException When occur a error on Connection/Statement/Update
     * @since 0.9.0
     */
    boolean updateApplication(Application application) throws CerberusException;

}
