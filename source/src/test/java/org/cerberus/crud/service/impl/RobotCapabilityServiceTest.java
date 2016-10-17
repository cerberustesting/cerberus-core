/*
 * Cerberus  Copyright (C) 2016  vertigo17
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

import java.util.Arrays;
import java.util.List;
import org.cerberus.crud.dao.IRobotCapabilityDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.AnswerList;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

/**
 * {@link RobotCapabilityService} unit tests
 *
 * @author Aurelien Bourdon
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class RobotCapabilityServiceTest {

    private static final String ROBOT_NAME = "FooRobot";

    private static List<RobotCapability> EXISTING_CAPABILITIES;

    @Mock
    private IRobotCapabilityDAO robotCapabilityDAO;

    @InjectMocks
    private RobotCapabilityService robotCapabilityService;

    @BeforeClass
    public static void beforeClass() {
        RobotCapability rc1 = new RobotCapability();
        rc1.setRobot(ROBOT_NAME);
        rc1.setCapability("capability(1)");
        rc1.setValue("value(1)");

        RobotCapability rc2 = new RobotCapability();
        rc2.setRobot(ROBOT_NAME);
        rc2.setCapability("capability(2)");
        rc2.setValue("value(2)");

        RobotCapability rc3 = new RobotCapability();
        rc3.setRobot(ROBOT_NAME);
        rc3.setCapability("capability(3)");
        rc3.setValue("value(3)");

        RobotCapability rc4 = new RobotCapability();
        rc4.setRobot(ROBOT_NAME);
        rc4.setCapability("capability(4)");
        rc4.setValue("value(4)");

        EXISTING_CAPABILITIES = Arrays.asList(rc1, rc2, rc3, rc4);
    }

    @Test
    public void testCompareListAndUpdateInsertDeleteElements() {
        when(robotCapabilityDAO.readByRobot(anyString())).thenReturn(dummyReadByRobot());
        
        RobotCapability rc1 = new RobotCapability();
        rc1.setRobot(ROBOT_NAME);
        rc1.setCapability("capability(1)");
        rc1.setValue("value(1)");
        
        RobotCapability rc2 = new RobotCapability();
        rc2.setRobot(ROBOT_NAME);
        rc2.setCapability("capability(2)");
        rc2.setValue("value(2 *changed*)");
        
        RobotCapability rc3 = new RobotCapability();
        rc3.setRobot(ROBOT_NAME);
        rc3.setCapability("capability(3)");
        rc3.setValue("value(3)");
        
        RobotCapability rc4 = new RobotCapability();
        rc4.setRobot(ROBOT_NAME);
        rc4.setCapability("capability(4)");
        rc4.setValue("value(4)");
        
        RobotCapability rc5 = new RobotCapability();
        rc5.setRobot(ROBOT_NAME);
        rc5.setCapability("capability(5)");
        rc5.setValue("value(5)");
        
        RobotCapability rc6 = new RobotCapability();
        rc5.setRobot(ROBOT_NAME);
        rc5.setCapability("capability(6)");
        rc5.setValue("value(6)");
        
        robotCapabilityService.compareListAndUpdateInsertDeleteElements(ROBOT_NAME, Arrays.asList(rc1, rc2, rc3, rc5, rc6));
        
        verify(robotCapabilityDAO).create(rc5);
        verify(robotCapabilityDAO).create(rc6);
        verify(robotCapabilityDAO).update(rc2);
        verify(robotCapabilityDAO).delete(rc4);
    }

    private AnswerList<RobotCapability> dummyReadByRobot() {
        AnswerList<RobotCapability> answer = new AnswerList<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        answer.setDataList(EXISTING_CAPABILITIES);
        return answer;
    }
}
