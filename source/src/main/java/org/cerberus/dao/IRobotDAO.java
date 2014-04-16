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

import org.cerberus.entity.Robot;
import org.cerberus.entity.SqlLibrary;
import org.cerberus.exception.CerberusException;

/**
 * @author bcivel
 */
public interface IRobotDAO {

    /**
     * Finds the Robot by Key
     *
     * @param id Key of the Robot to find.
     * @return Object Robot if exist.
     * @throws CerberusException when Robot does not exist.
     * @since 0.9.2
     */
    Robot findRobotByKey(Integer id) throws CerberusException;

    /**
     * Finds all Robots that exists
     *
     * @return List of robot.
     * @throws CerberusException when no robot exist.
     * @since 0.9.2
     */
    List<Robot> findAllRobot() throws CerberusException;

    /**
     * Update the object robot
     *
     * @param robot Object Robot to update.
     * @throws CerberusException When occur a error on
     * @since 0.9.2
     */
    void updateRobot(Robot robot) throws CerberusException;

    /**
     *
     * @param robot Object Robot to insert
     * @throws CerberusException
     * @since 0.9.2
     */
    public void createRobot(Robot robot) throws CerberusException;

    /**
     *
     * @param robot Object Robot to delete
     * @throws CerberusException
     * @since 0.9.2
     */
    public void deleteRobot(Robot robot) throws CerberusException;
    
    /**
     *
     * @param start first row of the resultSet
     * @param amount number of row of the resultSet
     * @param column order the resultSet by this column
     * @param dir Asc or desc, information for the order by command
     * @param searchTerm search term on all the column of the resultSet
     * @param individualSearch search term on a dedicated column of the
     * resultSet
     * @return List of Robot
     * @since 0.9.2
     */
    List<Robot> findRobotListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param searchTerm words to be searched in every column (Exemple :
     * article)
     * @param inds part of the script to add to where clause (Exemple : `type` =
     * 'Article')
     * @return The number of records for these criterias
     * @since 0.9.2
     */
    Integer getNumberOfRobotPerCriteria(String searchTerm, String inds);

}
