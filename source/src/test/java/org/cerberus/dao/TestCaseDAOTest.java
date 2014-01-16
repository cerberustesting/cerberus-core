package org.cerberus.dao;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/01/2013
 * @since 2.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class TestCaseDAOTest {


    @Ignore @Test(expected = NullPointerException.class)
    public void getTestCaseNullDatabase() {
//        ITestCaseDAO tcd = new TestCaseDAO();
//        tcd.getTestCase("", "");

    }
}
