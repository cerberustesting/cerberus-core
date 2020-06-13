/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
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
    AnswerItem<Robot> readByKeyTech(Integer robotid);

    /**
     *
     * @param robot
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    Robot readByKey(String robot) throws CerberusException;

    /**
     *
     * @return
     */
    AnswerList<Robot> readAll();

    /**
     *
     * @param robotList
     * @return
     */
    AnswerList<Robot> readByRobotList(List<String> robotList);

    AnswerList<Robot> readByRobotList(List<String> robotList, String typeRobot);

    /**
     *
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    HashMap<String, String> readToHashMapRobotDecli() throws CerberusException;



    /**
     *
     * @param robotList
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    HashMap<String, Robot> readToHashMapByRobotList(List<String> robotList) throws CerberusException;

    /**
     *
     * @param withCapabilities
     * @param startPosition
     * @param withExecutors
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<Robot> readByCriteria(boolean withCapabilities, boolean withExecutors, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

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
     * @param UsrModif
     * @return
     */
    Answer update(Robot robot, String UsrModif);

    /**
     *
     * @param robot
     * @param request
     * @return
     */
    boolean hasPermissionsRead(Robot robot, HttpServletRequest request);

    /**
     *
     * @param robot
     * @param request
     * @return
     */
    boolean hasPermissionsUpdate(Robot robot, HttpServletRequest request);

    /**
     *
     * @param robot
     * @param request
     * @return
     */
    boolean hasPermissionsCreate(Robot robot, HttpServletRequest request);

    /**
     *
     * @param robot
     * @param request
     * @return
     */
    boolean hasPermissionsDelete(Robot robot, HttpServletRequest request);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    Robot convert(AnswerItem<Robot> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<Robot> convert(AnswerList<Robot> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

}
