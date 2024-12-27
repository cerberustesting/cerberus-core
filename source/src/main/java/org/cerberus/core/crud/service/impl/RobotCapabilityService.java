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
import java.util.List;

import org.cerberus.core.crud.dao.IRobotCapabilityDAO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.service.IRobotCapabilityService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {@link IRobotCapabilityService} default implementation
 *
 * @author Aurelien Bourdon
 */
@Service
public class RobotCapabilityService implements IRobotCapabilityService {

    @Autowired
    private IRobotCapabilityDAO robotCapabilityDAO;

    @Override
    public AnswerList<RobotCapability> readByRobot(String robot) {
        // Check argument
        if (robot == null) {
            return new AnswerList<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR).resolveDescription("DESCRIPTION", "null robot"));
        }

        // Ready by robot
        return robotCapabilityDAO.readByRobot(robot);
    }

    @Override
    public Answer create(RobotCapability capability) {
        // Check argument
        if (capability == null) {
            return new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR).resolveDescription("DESCRIPTION", "null capability"));
        }

        // Create capability
        return robotCapabilityDAO.create(capability);
    }

    @Override
    public Answer create(List<RobotCapability> capabilities) {
        // Check argument
        if (capabilities == null) {
            return new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR).resolveDescription("DESCRIPTION", "null capabilities"));
        }

        // Create capabilities
        Answer finalAnswer = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        for (RobotCapability capability : capabilities) {
            AnswerUtil.agregateAnswer(finalAnswer, create(capability));
        }
        return finalAnswer;
    }

    @Override
    public Answer update(RobotCapability capability) {
        // Check argument
        if (capability == null) {
            return new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR).resolveDescription("DESCRIPTION", "null capability"));
        }

        // Update capability
        return robotCapabilityDAO.update(capability);
    }

    @Override
    public Answer update(List<RobotCapability> capabilities) {
        // Check argument
        if (capabilities == null) {
            return new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR).resolveDescription("DESCRIPTION", "null capabilities"));
        }

        // Update capabilities
        Answer finalAnswer = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        for (RobotCapability capability : capabilities) {
            AnswerUtil.agregateAnswer(finalAnswer, update(capability));
        }
        return finalAnswer;
    }

    @Override
    public Answer delete(RobotCapability capability) {
        // Check argument
        if (capability == null) {
            return new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR).resolveDescription("DESCRIPTION", "null capability"));
        }

        // Delete capability
        return robotCapabilityDAO.delete(capability);
    }

    @Override
    public Answer delete(List<RobotCapability> capabilities) {
        // Check argument
        if (capabilities == null) {
            return new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR).resolveDescription("DESCRIPTION", "null capabilities"));
        }

        // Delete capabilities
        Answer finalAnswer = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        for (RobotCapability capability : capabilities) {
            AnswerUtil.agregateAnswer(finalAnswer, delete(capability));
        }
        return finalAnswer;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String robot, List<RobotCapability> newCapabilities, String usrModif) {
        // Check arguments
        if (robot == null || newCapabilities == null) {
            return new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR).resolveDescription("DESCRIPTION", "null robot or capabilities"));
        }

        // Get the existing capabilities
        AnswerList<RobotCapability> existingCapabilities = readByRobot(robot);
        if (!existingCapabilities.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return existingCapabilities;
        }
        List<RobotCapability> oldCapabilities = existingCapabilities.getDataList();
        Answer finalAnswer = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        // Process smart udpate (only entities which have to be updated)
        List<RobotCapability> sameKeys = new ArrayList<>();
        List<RobotCapability> toUpdate = new ArrayList<>();
        for (RobotCapability oldCapability : oldCapabilities) {
            for (RobotCapability newCapability : newCapabilities) {
                if (oldCapability.hasSameKey(newCapability)) {
                    sameKeys.add(oldCapability);
                    sameKeys.add(newCapability);
                    if (!oldCapability.equals(newCapability)) {
                        toUpdate.add(newCapability);
                    }
                    break;
                }
            }
        }
        AnswerUtil.agregateAnswer(finalAnswer, update(toUpdate));

        // Process delete
        List<RobotCapability> toDelete = new ArrayList<>(oldCapabilities);
        toDelete.removeAll(sameKeys);
        AnswerUtil.agregateAnswer(finalAnswer, delete(toDelete));

        // Process create
        List<RobotCapability> toCreate = new ArrayList<>(newCapabilities);
        toCreate.removeAll(sameKeys);
        AnswerUtil.agregateAnswer(finalAnswer, create(toCreate));

        // Finally return the aggregated answer
        return finalAnswer;
    }

    @Override
    public List<RobotCapability> convert(AnswerList<RobotCapability> capabilityAnswers) throws CerberusException {
        if (capabilityAnswers != null && capabilityAnswers.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            // if the service returns an OK message then we can get the item
            return capabilityAnswers.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

}
