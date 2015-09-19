package org.cerberus.service;

import org.cerberus.crud.service.ITestService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 09/01/2013
 * @since 2.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
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
