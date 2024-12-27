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
package org.cerberus.core.crud.service.impl;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.IRobotDAO;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.service.IRobotCapabilityService;
import org.cerberus.core.crud.service.IRobotExecutorService;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class RobotService implements IRobotService {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOGGER = LogManager.getLogger(RobotService.class);

    @Autowired
    private IRobotDAO robotDao;
    @Autowired
    private IRobotCapabilityService robotCapabilityService;
    @Autowired
    private IRobotExecutorService robotExecutorService;

    @Override
    public AnswerItem<Robot> readByKeyTech(Integer robotid) {
        return fillCapabilities(robotDao.readByKeyTech(robotid));
    }

    @Override
    public Robot readByKey(String robot) throws CerberusException {
        Robot resultRobot = robotDao.readByKey(robot);
        if (resultRobot != null) {
            resultRobot = fillCapabilities(resultRobot);
            resultRobot = fillExecutors(resultRobot);
        }
        return resultRobot;
    }

    @Override
    public AnswerList<Robot> readAll() {
        return readByCriteria(false, false, 0, 0, "robot", "asc", null, null);
    }

    @Override
    public AnswerList<Robot> readByRobotList(List<String> robotList) {
        return robotDao.readByRobotList(robotList);
    }

    @Override
    public AnswerList<Robot> readByRobotList(List<String> robotList, String typeRobot) {
        return robotDao.readByRobotList(robotList, typeRobot);
    }

    @Override
    public HashMap<String, String> readToHashMapRobotDecli() throws CerberusException {
        HashMap<String, String> result = new HashMap<>();

        List<Robot> robots = convert(readAll());
        for (Robot rob : robots) {
            String robotDecli = ParameterParserUtil.parseStringParam(rob.getRobotDecli(), "");
            result.put(rob.getRobot(), robotDecli);
        }
        return result;
    }

    @Override
    public HashMap<String, Robot> readToHashMapByRobotList(List<String> robotList) throws CerberusException {
        HashMap<String, Robot> result = new HashMap<>();

        List<Robot> robots = convert(readByRobotList(robotList));
        for (Robot rob : robots) {
            rob.setRobotDecli(ParameterParserUtil.parseStringParam(rob.getRobotDecli(), ""));
            result.put(rob.getRobot(), rob);
        }
        return result;
    }

    @Override
    public AnswerList<Robot> readByCriteria(boolean withCapabilities, boolean withExecutors, int startPosition, int length, String columnName, String sort,
            String searchParameter, Map<String, List<String>> individualSearch) {
        if (withCapabilities) {
            return fillCapabilities(robotDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch));
        } else {
            return robotDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
        }
    }

    @Override
    public Answer create(Robot robot) {
        // First, create the robot
        Answer finalAnswer = robotDao.create(robot);
        if (!finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return finalAnswer;
        }

        // Second, create its capabilities
        for (RobotCapability capability : robot.getCapabilities()) {
            Answer robotCapabilityAnswer = robotCapabilityService.create(capability);
            // We try to create as many capabilities as possible, even if an error occurred.
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, robotCapabilityAnswer);
        }
        // Then, create its capabilities
        for (RobotExecutor executor : robot.getExecutors()) {
            Answer robotExecutorAnswer = robotExecutorService.create(executor);
            // We try to create as many capabilities as possible, even if an error occurred.
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, robotExecutorAnswer);
        }
        return finalAnswer;
    }

    @Override
    public Answer delete(Robot robot) {
        // First, delete the robot
        Answer finalAnswer = robotDao.delete(robot);
        if (!finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return finalAnswer;
        }

        // Finally return aggregated answer
        return finalAnswer;
    }

    @Override
    public Answer update(Robot robot, String usrModif) {
        // First, update the robot
        Answer finalAnswer = robotDao.update(robot);
        if (!finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return finalAnswer;
        }

        // Second, update its capabilities
        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, robotCapabilityService.compareListAndUpdateInsertDeleteElements(robot.getRobot(), robot.getCapabilities(), usrModif));

        // Then, update its executors
        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, robotExecutorService.compareListAndUpdateInsertDeleteElements(robot.getRobot(), robot.getExecutors(), usrModif));

        // Finally return aggregated answer
        return finalAnswer;
    }

    @Override
    public boolean hasPermissionsRead(Robot robot, HttpServletRequest request) {
        // Access right calculation.
        return true;
    }

    @Override
    public boolean hasPermissionsUpdate(Robot robot, HttpServletRequest request) {
        // Access right calculation.
        return (request.isUserInRole("RunTest"));
    }

    @Override
    public boolean hasPermissionsCreate(Robot robot, HttpServletRequest request) {
        // Access right calculation.
        return (request.isUserInRole("RunTest"));
    }

    @Override
    public boolean hasPermissionsDelete(Robot robot, HttpServletRequest request) {
        // Access right calculation.
        return (request.isUserInRole("RunTest"));
    }

    @Override
    public Robot convert(AnswerItem<Robot> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            // if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Robot> convert(AnswerList<Robot> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            // if the service returns an OK message then we can get the item
            return answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            // if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    private Robot fillCapabilities(Robot robotItem) throws CerberusException {
        if (robotItem != null) {
            robotItem.setCapabilities(robotCapabilityService.convert(robotCapabilityService.readByRobot(robotItem.getRobot())));
        }
        return robotItem;
    }

    private Robot fillExecutors(Robot robotItem) throws CerberusException {
        if (robotItem != null) {
            robotItem.setExecutors(robotExecutorService.convert(robotExecutorService.readByRobot(robotItem.getRobot())));
        }
        return robotItem;
    }

    private AnswerItem<Robot> fillCapabilities(AnswerItem<Robot> robotItem) {
        try {
            Robot robot = convert(robotItem);
            robot.setCapabilities(robotCapabilityService.convert(robotCapabilityService.readByRobot(robot.getRobot())));
        } catch (CerberusException e) {
            LOGGER.warn("Unable to flll robot capabilities due to " + e.getMessage());
        }
        return robotItem;
    }

    private AnswerList<Robot> fillCapabilities(AnswerList<Robot> robotList) {
        try {
            List<Robot> robots = convert(robotList);
            if (robots != null) {
                for (Robot robot : robots) {
                    robot.setCapabilities(
                            robotCapabilityService.convert(robotCapabilityService.readByRobot(robot.getRobot())));
                }
            }
        } catch (CerberusException e) {
            LOGGER.warn("Unable to fill robot capabilities due to " + e.getMessage());
        }
        return robotList;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return robotDao.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

}
