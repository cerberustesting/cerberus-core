package org.cerberus.dao;

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
 * @version 1.0, 29/12/2012
 * @since 2.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class TestCaseDAOIT {

    @Autowired
    private ITestCaseDAO testCaseDAO;

    @Ignore @Test
    public void getShortList() {
//        List<List<String>> list = testCaseDAO.getListOfTestCases("ZTest");
//        List<List<String>> list = testCaseDAO.getListOfTestCases("Cerberus");
//
////        assertEquals(list.get(0).get(0), "0001A");
//        assertEquals(list.get(0).get(0), "0001A");
////        assertEquals(list.get(0).get(1), "TST");
//        assertEquals(list.get(0).get(1), "Cerberus");
////        assertEquals(list.get(0).get(2), "Integration Test I");
//        assertEquals(list.get(0).get(2), "Load Test List");
    }

    @Ignore @Test
    public void getShortListNotExistingTestCase() {
//        List<List<String>> list = testCaseDAO.getListOfTestCases("ZNoTestCase");
//        List<List<String>> list = testCaseDAO.getListOfTestCases("Statements");
//
//        assertEquals(list.size(), 0);
    }

    @Ignore @Test
    public void getShortListNotExistingTest() {
//        List<List<String>> list = testCaseDAO.getListOfTestCases("");
//
//        assertEquals(list.size(), 0);
    }

    @Ignore @Test
    public void getTestCase() {
//        TestCase tc = testCaseDAO.getTestCase("Cerberus", "0001A");
//        TestCase tc = testCaseDAO.getTestCase("Cerberus", "0001A");
//
//        assertEquals(tc.getTestCase(), "0001A");
////        assertEquals(tc.getApplication(), "TST");
//        assertEquals(tc.getApplication(), "Cerberus");
////        assertEquals(tc.getDescription(), "Integration Test I");
//        assertEquals(tc.getDescription(), "Load Test List");
    }

   @Ignore @Test
    public void getTestCaseNotExistingTestCase() {
//        TestCase tc = testCaseDAO.getTestCase("ZNoTestCase", "0001A");
//        TestCase tc = testCaseDAO.getTestCase("Cerberus", "");
//
//        assertNull(tc);
    }

    @Ignore @Test
    public void getTestCaseNotExistingTest() {
//        TestCase tc = testCaseDAO.getTestCase("", "0001A");
//
//        assertNull(tc);
    }
}
