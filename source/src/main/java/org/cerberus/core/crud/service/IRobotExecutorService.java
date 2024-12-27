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
package org.cerberus.core.crud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface IRobotExecutorService {

    /**
     *
     * @param robot
     * @param executor
     * @return
     */
    AnswerItem<RobotExecutor> readByKey(String robot, String executor);

    /**
     *
     * @param robot
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    RobotExecutor readBestByKey(String robot) throws CerberusException ;

    /**
     *
     * @return
     */
    AnswerList<RobotExecutor> readAll();

    /**
     *
     * @param robot
     * @param active
     * @return
     */
    AnswerList<RobotExecutor> readByVarious(List<String> robot, String active);

    /**
     *
     * @param robot
     * @return
     */
    AnswerList<RobotExecutor> readByRobot(String robot);

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<RobotExecutor> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param robot
     * @param active
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<RobotExecutor> readByRobotByCriteria(List<String> robot, String active, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     * Enrich the robot_executors of Executor list for each robot.
     *
     * @param robot_executors
     * @return
     */
    HashMap<String, List<RobotExecutor>> getExecutorListFromRobotHash(HashMap<String, List<RobotExecutor>> robot_executors);

    /**
     *
     * @param robot
     * @param executor
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(String robot, String executor);

    /**
     *
     * @param object
     * @return
     */
    Answer create(RobotExecutor object);

    /**
     *
     * @param objectList
     * @return
     */
    Answer createList(List<RobotExecutor> objectList, String usrCreate);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(RobotExecutor object);

    /**
     *
     * @param objectList
     * @return
     */
    Answer deleteList(List<RobotExecutor> objectList);

    /**
     *
     * @param robot
     * @param executor
     * @param object
     * @return
     */
    Answer update(String robot, String executor, RobotExecutor object);

    /**
     *
     * @param robot
     * @param executor
     * @return
     */
    Answer updateLastExe(String robot, String executor);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    RobotExecutor convert(AnswerItem<RobotExecutor> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<RobotExecutor> convert(AnswerList<RobotExecutor> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param robot
     * @param newList
     * @param usrModif
     * @return
     */
    Answer compareListAndUpdateInsertDeleteElements(String robot, List<RobotExecutor> newList, String usrModif);

    /**
     *
     * @param robot
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesByCriteria(String robot, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
