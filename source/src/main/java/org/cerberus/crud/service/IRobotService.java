/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.crud.service;

import java.util.List;
import org.cerberus.crud.entity.Robot;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface IRobotService {

    /**
     *
     * @param robotid
     * @return
     */
    AnswerItem readByKeyTech(Integer robotid);    

    /**
     *
     * @param robot
     * @return
     */
    AnswerItem readByKey(String robot);    
    
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
    AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param robot
     * @return
     */
    Answer create(Robot robot);

    /**
     *
     * @param robot
     * @return
     */
    Answer delete(Robot robot);

    /**
     *
     * @param robot
     * @return
     */
    Answer update(Robot robot);

    
    
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
     * @return List of Robot.
     * @throws CerberusException when no Robot exist.
     * @since 0.9.2
     */
    List<Robot> findAllRobot() throws CerberusException;

    /**
     * Update the object Robot
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
    void createRobot(Robot robot) throws CerberusException;

    /**
     *
     * @param robot Object Robot to delete
     * @throws CerberusException
     * @since 0.9.2
     */
    void deleteRobot(Robot robot) throws CerberusException;

    /**
     *
     * @param start first row of the resultSet
     * @param amount number of row of the resultSet
     * @param column order the resultSet by this column
     * @param dir Asc or desc, information for the order by command
     * @param searchTerm search term on all the column of the resultSet
     * @param individualSearch search term on a dedicated column of the
     * resultSet
     * @return List of robot
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
    
    /**
     * 
     * @param name Name of the robot
     * @return the first robot found
     */
    Robot findRobotByName(String name) throws CerberusException ;

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    Robot convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<Robot> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;
}
