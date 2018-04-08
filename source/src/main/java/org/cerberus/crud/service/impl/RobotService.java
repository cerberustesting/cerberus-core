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
package org.cerberus.crud.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.IRobotDAO;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.service.IRobotCapabilityService;
import org.cerberus.crud.service.IRobotService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
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

    @Override
    public AnswerItem<Robot> readByKeyTech(Integer robotid) {
        return fillCapabilities(robotDao.readByKeyTech(robotid));
    }

    @Override
    public Robot readByKey(String robot) throws CerberusException {
        return fillCapabilities(robotDao.readByKey(robot));
    }

    @Override
    public AnswerList<Robot> readAll() {
        return readByCriteria(0, 0, "robot", "asc", null, null);
    }

    @Override
    public HashMap<String, String> readToHashMapRobotDecli() {
        HashMap<String, String> result = new HashMap<>();

        AnswerList<Robot> answer = readAll(); //TODO: handle if the response does not turn ok
        for (Robot rob : (List<Robot>) answer.getDataList()) {
            String robotDecli = ParameterParserUtil.parseStringParam(rob.getRobotDecli(), "");
            result.put(rob.getRobot(), robotDecli);
        }
        return result;
    }

    @Override
    public AnswerList<Robot> readByCriteria(int startPosition, int length, String columnName, String sort,
            String searchParameter, Map<String, List<String>> individualSearch) {
        return fillCapabilities(robotDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch));
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
            AnswerUtil.agregateAnswer(finalAnswer, robotCapabilityAnswer);
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

        // Second, delete its capabilities
        AnswerUtil.agregateAnswer(finalAnswer, robotCapabilityService.delete(robot.getCapabilities()));

        // Finally return aggregated answer
        return finalAnswer;
    }

    @Override
    public Answer update(Robot robot) {
        // First, update the robot
        Answer finalAnswer = robotDao.update(robot);
        if (!finalAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return finalAnswer;
        }

        // Second, update its capabilities
        AnswerUtil.agregateAnswer(finalAnswer, robotCapabilityService.compareListAndUpdateInsertDeleteElements(robot.getRobot(), robot.getCapabilities()));

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
        robotItem.setCapabilities(robotCapabilityService.convert(robotCapabilityService.readByRobot(robotItem.getRobot())));
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
    public AnswerList<List<String>> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return robotDao.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

}
