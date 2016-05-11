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
package org.cerberus.crud.service.impl;

import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IRobotCapabilitiesDAO;
import org.cerberus.crud.dao.IRobotDAO;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapabilities;
import org.cerberus.crud.service.IRobotService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bcivel
 */
@Service
public class RobotService implements IRobotService {

    private static final Logger LOGGER = Logger.getLogger(RobotService.class);

    @Autowired
    private IRobotDAO robotDao;

    @Autowired
    private IRobotCapabilitiesDAO robotCapabilitiesDAO;

    @Override
    public AnswerItem readByKeyTech(Integer robotid) {
        return fillCapabilities(robotDao.readByKeyTech(robotid));
    }

    @Override
    public AnswerItem readByKey(String robot) {
        return fillCapabilities(robotDao.readByKey(robot));
    }

    @Override
    public AnswerList readAll() {
        return readByCriteria(0, 0, "robot", "asc", null, null);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return fillCapabilities(robotDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, string));
    }

    @Override
    public Answer create(Robot robot) {
        // Create robot
        Answer robotAnswer = createRobot(robot);
        if (!robotAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return robotAnswer;
        }

        // Create capabilities
        Answer capabilitiesAnswer = createCapabilities(robot);
        if (!capabilitiesAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            LOGGER.warn("Unable to create robot capabilities for robot " + robot.getRobot() + " . Try to rollback...");
            delete(robot);
            return capabilitiesAnswer;
        }

        return robotAnswer;
    }

    @Override
    public Answer delete(Robot robot) {
        // Delete robot
        Answer robotAnswer = deleteRobot(robot);
        if (!robotAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return robotAnswer;
        }

        // Delete capabilities
        Answer capabilitiesAnswer = deleteCapabilities(robot);
        if (!capabilitiesAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            LOGGER.warn("Unable to delete robot capabilities for robot " + robot.getRobot() + " . Try to rollback...");
            create(robot);
            return capabilitiesAnswer;
        }

        return robotAnswer;
    }

    @Override
    public Answer update(Robot robot) {
        // Robot update
        AnswerItem<Robot> oldRobot = robotDao.readByKey(robot.getRobot());
        if (!oldRobot.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return oldRobot;
        }
        Answer robotAnswer = updateRobot(robot);
        if (!robotAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return robotAnswer;
        }

        // Cababilities update
        AnswerItem<RobotCapabilities> oldCapabilities = robotCapabilitiesDAO.findFromRobot(robot);
        if (!oldCapabilities.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            LOGGER.warn("Unable to find current robot capabilities for robot " + robot.getRobot() + " . Try to rollback...");
            updateRobot(oldRobot.getItem());
            return oldCapabilities;
        }
        Answer capabilitiesAnswer = updateCapabilities(oldCapabilities.getItem(), robot.getCapabilities());
        if (!capabilitiesAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            LOGGER.warn("Unable to update robot capabilities for robot " + robot.getRobot() + " . Try to rollback...");
            updateRobot(oldRobot.getItem());
            updateCapabilities(robot.getCapabilities(), oldCapabilities.getItem());
            return capabilitiesAnswer;
        }

        return robotAnswer;
    }

    @Override
    public Robot convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (Robot) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Robot> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<Robot>) answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    /**
     * Fill the given {@link AnswerItem<Robot>} with the associated {@link RobotCapabilities} of the inner given {@link Robot}
     *
     * @param robotAnswer the {@link AnswerItem<Robot>} to fill its {@link RobotCapabilities}
     * @return the same robotAnswer
     */
    private AnswerItem<Robot> fillCapabilities(final AnswerItem<Robot> robotAnswer) {
        // Check argument
        if (robotAnswer == null || !robotAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            LOGGER.warn("Unable to fill capabilities on null or invalid robot");
            return robotAnswer;
        }

        // Then fill capabilities to the inner robot
        robotAnswer.setItem(fillCapabilities(robotAnswer.getItem()));
        return robotAnswer;
    }

    /**
     * Fill the given {@link AnswerList<Robot>} with the associated {@link RobotCapabilities} of the inner given {@link Robot}
     *
     * @param robotAnswers the {@link AnswerList<Robot>} to fill its {@link RobotCapabilities}
     * @return the same robotAnswers
     */
    private AnswerList<Robot> fillCapabilities(final AnswerList<Robot> robotAnswers) {
        // Check argument
        if (robotAnswers == null || !robotAnswers.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            LOGGER.warn("Unable to fill capabilities on null or invalid robot");
            return robotAnswers;
        }

        List<Robot> filledRobots = new ArrayList<>();
        for (Robot robot : robotAnswers.getDataList()) {
            filledRobots.add(fillCapabilities(robot));
        }
        robotAnswers.setDataList(filledRobots);
        return robotAnswers;
    }

    /**
     * Fill the given {@link Robot} with its associated {@link RobotCapabilities}
     *
     * @param robot the {@link Robot} to fill
     * @return the same given {@link Robot} filled with is associated {@link RobotCapabilities}
     */
    private Robot fillCapabilities(final Robot robot) {
        // Check argument
        if (robot == null) {
            LOGGER.warn("Unable to fill capabilities on null or invalid robot");
            return robot;
        }

        // Try to get capabilities from the given robot
        AnswerItem<RobotCapabilities> capabilities = robotCapabilitiesDAO.findFromRobot(robot);
        if (!capabilities.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            LOGGER.warn("Unable to fill capabilities for Robot " + robot.getRobotID() + " due to " + capabilities.getMessageDescription());
            return robot;
        }
        robot.setCapabilities(capabilities.getItem());

        // Finally return it
        return robot;
    }

    private Answer createRobot(Robot robot) {
        return robotDao.create(robot);
    }

    private Answer createCapabilities(Robot robot) {
        return robotCapabilitiesDAO.create(robot.getCapabilities());
    }

    private Answer deleteRobot(Robot robot) {
        return robotDao.delete(robot);
    }

    private Answer deleteCapabilities(Robot robot) {
        return robotCapabilitiesDAO.delete(robot.getCapabilities());
    }

    private Answer updateRobot(Robot robot) {
        return robotDao.update(robot);
    }

    private Answer updateCapabilities(RobotCapabilities oldCapabilities, RobotCapabilities newCapabilities) {
        // Update part
        Map<String, String> toUpdate = new HashMap<>(oldCapabilities.getCapabilities());
        toUpdate.keySet().retainAll(newCapabilities.getCapabilities().keySet());
        RobotCapabilities toReallyUpdate = new RobotCapabilities(newCapabilities.getRobot());
        // Only chose those who really changed
        for (Map.Entry<String, String> update : toUpdate.entrySet()) {
            String newValue = newCapabilities.getCapability(update.getKey());
            if (!update.getValue().equals(newValue)) {
                toReallyUpdate.putCapability(update.getKey(), newValue);
            }
        }
        Answer ans = robotCapabilitiesDAO.update(toReallyUpdate);

        // Create part
        Map<String, String> toCreate = new HashMap<>(newCapabilities.getCapabilities());
        toCreate.keySet().removeAll(oldCapabilities.getCapabilities().keySet());
        ans = AnswerUtil.agregateAnswer(ans, robotCapabilitiesDAO.create(new RobotCapabilities(newCapabilities.getRobot()).setCapabilities(toCreate)));

        // Remove part
        Map<String, String> toRemove = new HashMap<>(oldCapabilities.getCapabilities());
        toRemove.keySet().removeAll(newCapabilities.getCapabilities().keySet());
        ans = AnswerUtil.agregateAnswer(ans, robotCapabilitiesDAO.create(new RobotCapabilities(newCapabilities.getRobot()).setCapabilities(toRemove)));

        return ans;
    }

}
