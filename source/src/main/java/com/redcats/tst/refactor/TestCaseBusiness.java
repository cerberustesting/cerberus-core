/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

import com.redcats.tst.dao.ITestCaseDAO;
import com.redcats.tst.entity.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Bernardes <tbernardes@redoute.pt>
 * @version 1.0
 * @since 2012-08-20
 */
@Service("testcaseBusiness")
public class TestCaseBusiness implements ITestCaseBusiness {

    /**
     * TestCaseDAO class autowired by Springframework and used to access method.
     */
    @Autowired
    private ITestCaseDAO testcaseDAO;

    /**
     * Not yet implemented
     *
     * @param tc TestCase object
     * @return String with the result message of the operation
     */
    @Override
    public String createTestCase(TestCase tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not yet implemented
     *
     * @param test String with the name of the Test to search
     * @param testcase String with the name of the TestCase to search
     * @return TestCase object relative to params or null if don't exist.
     */
    @Override
    public TestCase getTestCase(String test, String testcase) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not yet implemented
     *
     * @param test String with the name of the Test to remove
     * @param testcase String with the name of the TestCase to remove
     * @return String with the result message of the operation
     */
    @Override
    public String removeTestCase(String test, String testcase) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Update the TestCase based on TestCase object and type of update
     *
     * The type of update depends on the information that exist on TestCase
     * object. UPDATE_INFORMATION = The header of the TestCase (TestCase
     * Information and Activation Criterias) Use {@link #updateTestCaseInformation(TestCase tc)}
     * to update the TestCase information. UPDATE_PROPERTIES = The list of
     * properties linked to the testcase UPDATE_ACTIONS = The list of actions
     * linked to the testcase UPDATE_CONTROLS = The list of controls linked to
     * the testcase UPDATE_ALL = Update the four (Information, Properties,
     * Actions and Controls)
     *
     * @param tc TestCase object
     * @param type type of update to execute
     * @return String with the result message of the operation
     */
    @Override
    public String updateTestCase(TestCase tc, int type) {
        String result = "";
        switch (type) {
            case ITestCaseBusiness.UPDATE_INFORMATION:
                result = this.updateTestCaseInformation(tc);
                break;
            case ITestCaseBusiness.UPDATE_PROPERTIES:
                result = this.updateTestCaseProperties(tc);
                break;
            case ITestCaseBusiness.UPDATE_ACTIONS:
                result = this.updateTestCaseActions(tc);
                break;
            case ITestCaseBusiness.UPDATE_CONTROLS:
                result = this.updateTestCaseControls(tc);
                break;
            case ITestCaseBusiness.UPDATE_ALL:
                result = this.updateTestCaseAll(tc);
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Update the TestCase information
     *
     * Use {@link #updateTestCaseInformation(TestCase tc)} to update the
     * TestCase information. Use {@link #updateTestCaseInformationCountries(TestCase tc)}
     * to update the TestCase Countries.
     *
     * @param tc TestCase object
     * @return String with the result message of the operation
     */
    private String updateTestCaseInformation(TestCase tc) {
        if (testcaseDAO.updateTestCaseInformation(tc)) {
            if (testcaseDAO.updateTestCaseInformation(tc)) {
                if (testcaseDAO.updateTestCaseInformationCountries(tc)) {
                    return StatusMessage.SUCCESS_TESTCASEUPDATE;
                }
            }
        }
        return StatusMessage.ERROR_TESTCASEERRORUPDATE;
    }

    private String updateTestCaseProperties(TestCase tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String updateTestCaseActions(TestCase tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String updateTestCaseControls(TestCase tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String updateTestCaseAll(TestCase tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
