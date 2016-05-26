package org.cerberus.service.engine;

import org.cerberus.engine.execution.IRunTestCaseService;
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
 * @version 1.0, 23/01/2013
 * @since 2.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class RunTestCaseServiceTest {

    @Autowired
    IRunTestCaseService runTestCaseService;

    @Ignore @Test
    public void runTestCase() {
//        TestCaseExecution testCaseExecution = runTestCaseService.runTestCase("Cerberus", "0001A", "RX", "PROD", "localhost", "5555", "*firefox", "logpath", "");

//        Assert.assertNotNull(testCaseExecution);
    }
}
