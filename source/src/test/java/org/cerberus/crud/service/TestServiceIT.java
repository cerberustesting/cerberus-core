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
package org.cerberus.crud.service;

import org.cerberus.crud.service.ITestService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 09/01/2013
 * @since 2.0.0
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class TestServiceIT {

    @Autowired
    ITestService testService;

    @Ignore @Test
    public void loadAllStepSequences() {
//        List<Step> list = testService.loadAllStepSequences("ZTest", "0001A");
//        List<Step> list = testService.loadAllStepSequences("Articles", "0001A");

//        Assert.assertEquals(list.get(0).getSequences().get(8).getSequence(), 60);
    }

    @Ignore @Test
    public void loadTestCaseInformation() {
//        TestCase testCase = testService.loadAllStepSequences("ZTest", "0001A");
//        TestCase testCase = testService.loadTestCaseInformation("Articles", "0001A");
//
//        Assert.assertEquals(testCase.getShortDescription(), "Wrong Ref Search");
    }
}
