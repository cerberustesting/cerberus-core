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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IRobotExecutorDAO;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.service.IRobotExecutorService;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
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
public class RobotExecutorService implements IRobotExecutorService {

    @Autowired
    private IRobotExecutorDAO robotExecutorDAO;
    @Autowired
    private IRobotService robotService;

    private static final Logger LOG = LogManager.getLogger(RobotExecutorService.class);

    private final String OBJECT_NAME = "Robot Executor";

    @Override
    public AnswerItem<RobotExecutor> readByKey(String robot, String executor) {
        return robotExecutorDAO.readByKey(robot, executor);
    }

    @Override
    public RobotExecutor readBestByKey(String robot) throws CerberusException {
        List<RobotExecutor> lst = robotExecutorDAO.readBestByKey(robot);

        if (lst.size() > 0) {
            return lst.get(0);
        }

        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.GUI_NO_ROBOT_EXECUTOR_AVAILABLE)
                .resolveDescription("ROBOT", robot));
    }

    @Override
    public AnswerList<RobotExecutor> readAll() {
        return readByRobotByCriteria(null, null, 0, 0, "rank", "asc", null, null);
    }

    @Override
    public AnswerList<RobotExecutor> readByVarious(List<String> robot, String active) {
        // For each robot in the list we get the list of RobotExecutor
        return robotExecutorDAO.readByVariousByCriteria(robot, active, 0, 0, "rank", "asc", null, null);
    }

    @Override
    public AnswerList<RobotExecutor> readByRobot(String robot) {
        List<String> robotList = new ArrayList<>();
        robotList.add(robot);
        // For each robot in the list we get the list of RobotExecutor
        return robotExecutorDAO.readByVariousByCriteria(robotList, null, 0, 0, "rank", "asc", null, null);
    }

    @Override
    public AnswerList<RobotExecutor> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return robotExecutorDAO.readByVariousByCriteria(null, null, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<RobotExecutor> readByRobotByCriteria(List<String> robot, String active, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return robotExecutorDAO.readByVariousByCriteria(robot, active, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public HashMap<String, List<RobotExecutor>> getExecutorListFromRobotHash(HashMap<String, List<RobotExecutor>> robot_executors) {
        List<String> robotList = new ArrayList<>();
        for (Map.Entry<String, List<RobotExecutor>> entry : robot_executors.entrySet()) {
            String key = entry.getKey();
            if (!StringUtil.isEmptyOrNull(key)) {
                robotList.add(key);
            }
        }

        try {
            for (String myrobot : robotList) {
                //For each necessary robot, we get the Loadbalancing rule and corresponding list of executors in the correct order.
                Robot myrobotobj = robotService.readByKey(myrobot);
                if (myrobotobj != null) {
                    List<String> robotList2 = new ArrayList<>();
                    robotList2.add(myrobot);
                    AnswerList<RobotExecutor> rbtExecutor;
                    if (Robot.LOADBALANCINGEXECUTORMETHOD_ROUNDROBIN.equals(myrobotobj.getLbexemethod())) {
                        rbtExecutor = robotExecutorDAO.readByVariousByCriteria(robotList2, "1", 0, 0, "datelastexesubmitted", "asc", null, null);
                    } else {
                        rbtExecutor = robotExecutorDAO.readByVariousByCriteria(robotList2, "1", 0, 0, "rank", "asc", null, null);
                    }
                    robot_executors.put(myrobot, rbtExecutor.getDataList());
                }
            }
        } catch (CerberusException ex) {
            LOG.error(ex,ex);
        }

//        List<RobotExecutor> robotExeList = new ArrayList<>();
//        for (RobotExecutor robotExecutor : rbtExecutor.getDataList()) {
//            if (robot_executors.get(robotExecutor.getRobot()) != null) {
//                robotExeList = robot_executors.get(robotExecutor.getRobot());
//                robotExeList.add(robotExecutor);
//                robot_executors.put(robotExecutor.getRobot(), robotExeList);
//            }
//        }
        return robot_executors;
    }

    @Override
    public boolean exist(String robot, String executor) {
        AnswerItem objectAnswer = readByKey(robot, executor);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(RobotExecutor object) {
        return robotExecutorDAO.create(object);
    }

    @Override
    public Answer createList(List<RobotExecutor> objectList, String usrCreate) {
        Answer ans = new Answer(null);
        for (RobotExecutor objectToCreate : objectList) {
            if (usrCreate != null) {
                objectToCreate.setUsrCreated(usrCreate);
            }
            ans = this.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer delete(RobotExecutor object) {
        return robotExecutorDAO.delete(object);
    }

    @Override
    public Answer deleteList(List<RobotExecutor> objectList) {
        Answer ans = new Answer(null);
        for (RobotExecutor objectToDelete : objectList) {
            ans = this.delete(objectToDelete);
        }
        return ans;
    }

    @Override
    public Answer update(String service, String key, RobotExecutor object) {
        return robotExecutorDAO.update(service, key, object);
    }

    @Override
    public Answer updateLastExe(String robot, String executor) {
        return robotExecutorDAO.updateLastExe(robot, executor);
    }

    @Override
    public RobotExecutor convert(AnswerItem<RobotExecutor> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<RobotExecutor> convert(AnswerList<RobotExecutor> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
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

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String robot, List<RobotExecutor> newList, String usrModif) {
        Answer ans = new Answer(null);
        List<String> robotList = new ArrayList<>();
        robotList.add(robot);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<RobotExecutor> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByVarious(robotList, null));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }

        /**
         * Update and Create all objects database Objects from newList
         */
        LOG.debug(newList);
        List<RobotExecutor> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<RobotExecutor> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        for (RobotExecutor objectDifference : listToUpdateOrInsertToIterate) {
            for (RobotExecutor objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    objectDifference.setUsrModif(usrModif);
                    ans = this.update(objectDifference.getRobot(), objectDifference.getExecutor(), objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
        List<RobotExecutor> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<RobotExecutor> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (RobotExecutor tcsDifference : listToDeleteToIterate) {
            for (RobotExecutor tcsInPage : newList) {
                if (tcsDifference.hasSameKey(tcsInPage)) {
                    listToDelete.remove(tcsDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert, usrModif);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        return finalAnswer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String service, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return robotExecutorDAO.readDistinctValuesByCriteria(service, searchParameter, individualSearch, columnName);
    }

}
