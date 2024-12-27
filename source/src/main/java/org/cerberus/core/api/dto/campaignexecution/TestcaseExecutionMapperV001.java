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
package org.cerberus.core.api.dto.campaignexecution;

import org.cerberus.core.api.dto.invariant.InvariantMapperV001;
import org.cerberus.core.api.dto.testcase.TestcaseMapperV001;
import org.cerberus.core.api.mappers.BooleanMapper;
import org.cerberus.core.api.mappers.TimestampMapper;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author lucashimpens
 */
@Mapper(componentModel = "spring",
        uses = {
            InvariantMapperV001.class,
            TestcaseMapperV001.class,
            TimestampMapper.class,
            BooleanMapper.class
        }
)

public interface TestcaseExecutionMapperV001 {

    @Mapping(source = "execution.id", target = "testcaseExecutionId")
    @Mapping(source = "execution.testCaseObj", target = "testcase")
    @Mapping(source = "execution.testCaseVersion", target = "testcaseVersion")
    @Mapping(source = "execution.executor", target = "usrExecuted")
    @Mapping(source = "execution.robot", target = "robot.robotId")
    @Mapping(source = "execution.robotExecutor", target = "robot.executor")
    @Mapping(source = "execution.robotHost", target = "robot.host")
    @Mapping(source = "execution.robotPort", target = "robot.port")
    @Mapping(source = "execution.robotDecli", target = "robot.declination")
    @Mapping(source = "execution.robotProviderSessionID", target = "robot.providerSessionId")
    @Mapping(source = "execution.robotProvider", target = "robot.provider")
    @Mapping(source = "execution.robotSessionID", target = "robot.sessionId")
    @Mapping(source = "execution.browser", target = "robot.browser")
    @Mapping(source = "execution.version", target = "robot.version")
    @Mapping(source = "execution.platform", target = "robot.platform")
    @Mapping(source = "execution.screenSize", target = "robot.screenSize")
    @Mapping(source = "execution.userAgent", target = "robot.userAgent")
    @Mapping(source = "execution.manualExecution", target = "isManualExecution")
    @Mapping(source = "execution.priorityObj", target = "priority")
    @Mapping(source = "execution.environmentObj", target = "environment")
    @Mapping(source = "execution.countryObj", target = "country")
    @Mapping(source = "execution.start", target = "startDate")
    @Mapping(source = "execution.end", target = "endDate")
    @Mapping(source = "execution.queueID", target = "queueId")
    @Mapping(target = "durationInMillis", expression = "java(execution.getEnd()-execution.getStart())")
    TestcaseExecutionDTOV001 toDTO(TestCaseExecution execution);
}
