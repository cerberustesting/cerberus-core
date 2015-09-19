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
package org.cerberus.service;

import org.cerberus.crud.service.ITestService;
import junit.framework.Assert;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author memiks
 */
public class TestServiceTest {

    @Autowired
    private IFactoryTest factoryTest;

    @Autowired
    private ITestService testService;

    @Ignore
    @Test
    public void testCreateTestInDatabase() throws CerberusException {
        boolean result;
        boolean expected = true;

        result = testService.createTest(factoryTest.create("TestServiceTest", "TestDescription", "Y", "Y", null));

        Assert.assertEquals(expected, result);
    }

    @Ignore
    @Test
    public void testDeleteTestInDatabase() {
        boolean result;
        boolean expected = true;

        result = testService.deleteTest(factoryTest.create("TestServiceTest", "TestDescription", "Y", "Y", null));

        Assert.assertEquals(expected, result);
    }
}
