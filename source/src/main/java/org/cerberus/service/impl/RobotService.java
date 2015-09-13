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
package org.cerberus.service.impl;

import java.util.List;
import org.cerberus.dao.IRobotDAO;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.Robot;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IRobotService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class RobotService implements IRobotService {

    @Autowired
    private IRobotDAO robotDao;

    @Override
    public AnswerItem readByKeyTech(Integer robotid) {
        return robotDao.readByKeyTech(robotid);
    }

    @Override
    public AnswerItem readByKey(String robot) {
        return robotDao.readByKey(robot);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return robotDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, string);
    }

    @Override
    public Answer create(Robot robot) {
        return robotDao.create(robot);
    }

    @Override
    public Answer delete(Robot robot) {
        return robotDao.delete(robot);
    }

    @Override
    public Answer update(Robot robot) {
        return robotDao.update(robot);
    }

    
    
    
    
    
    @Override
    public Robot findRobotByKey(Integer id) throws CerberusException {
        return robotDao.findRobotByKey(id);
    }

    @Override
    public List<Robot> findAllRobot() throws CerberusException {
        return robotDao.findAllRobot();
    }

    @Override
    public void updateRobot(Robot robot) throws CerberusException {
        robotDao.updateRobot(robot);
    }

    @Override
    public void createRobot(Robot robot) throws CerberusException {
        robotDao.createRobot(robot);
    }

    @Override
    public void deleteRobot(Robot robot) throws CerberusException {
        robotDao.deleteRobot(robot);
    }

    @Override
    public List<Robot> findRobotListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return robotDao.findRobotListByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Integer getNumberOfRobotPerCriteria(String searchTerm, String inds) {
        return robotDao.getNumberOfRobotPerCriteria(searchTerm, inds);
    }

    @Override
    public Robot findRobotByName(String name) throws CerberusException {
        return robotDao.findRobotByName(name);
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

}
