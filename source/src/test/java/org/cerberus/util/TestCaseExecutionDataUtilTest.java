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
package org.cerberus.util;

import junit.framework.Assert;

import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.factory.impl.FactoryTestCaseExecutionData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class TestCaseExecutionDataUtilTest {

    private static final long START = 0L;
    private static final long START_LONG = 0L;
    private static final long END = 0L;
    private static final long END_LONG = 0L;

    @InjectMocks
    private FactoryTestCaseExecutionData factoryTestCaseExecutionData;

    private TestCaseExecutionData data;

    public TestCaseExecutionDataUtilTest() {
    }

    @Before
    public void setUp() {
        data = factoryTestCaseExecutionData.create(0, "property", 1, "description", "value", "type", "value1", "value2", "returnCode", "rMessage", START, START_LONG, END, END_LONG,
                new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_TEXT), 0, 0, "", "", "", 0, 0, "");
    }

    @Test
    public void testResetTimers() {
        Assert.assertEquals(START, data.getStart());
        Assert.assertEquals(START_LONG, data.getStartLong());
        Assert.assertEquals(END, data.getEnd());
        Assert.assertEquals(END_LONG, data.getEndLong());

        long newTime = 1234L;
        TestCaseExecutionDataUtil.resetTimers(data, newTime);

        Assert.assertEquals(newTime, data.getStart());
        Assert.assertEquals(newTime, data.getStartLong());
        Assert.assertEquals(newTime, data.getEnd());
        Assert.assertEquals(newTime, data.getEndLong());
    }

}
