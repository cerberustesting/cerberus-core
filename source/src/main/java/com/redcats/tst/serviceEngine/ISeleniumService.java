package com.redcats.tst.serviceEngine;

import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.TestCaseStepActionExecution;
import com.redcats.tst.exception.CerberusEventException;
import com.redcats.tst.exception.CerberusException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public interface ISeleniumService {

    MessageGeneral startSeleniumServer(long runId, String host, String port, String browser, String ip, String login, int verbose, String country);

    boolean isSeleniumServerReachable(String host, String port);

    boolean stopSeleniumServer();

    boolean startSeleniumFirefox(long runId, boolean record, String country) throws CerberusException;

    String getValueFromHTMLVisible(String locator);
    
    String getValueFromHTML(String locator);

    boolean isElementPresent(String locator);

    boolean isElementVisible(String locator);

    String getPageSource();

    String getTitle();

     /**
     * @return Method return a string with the right part of the URL in order to be agnostic of the environment.
     * ex : http://redoute.com/mypathlevel1/mypathlevel2/file.aspx
     * will return /mypathlevel1/mypathlevel2/file.aspx
     * @throws CerberusException 
     *      in case the URL does not contain the host of the application beeing tested.
     *      That could happen if the application redirect to a different host during the testcase execution.
     */
    String getCurrentUrl() throws CerberusEventException;

    void doScreenShot(String runId, String path);

    TestCaseStepActionExecution doAction(TestCaseStepActionExecution testCaseStepActionExecution);
}
