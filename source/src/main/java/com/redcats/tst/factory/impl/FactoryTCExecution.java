/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.*;
import com.redcats.tst.factory.IFactoryTCExecution;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bcivel
 */
@Service
public class FactoryTCExecution implements IFactoryTCExecution {

    @Override
    public TCExecution create(long id, String test, String testCase, String build, String revision, String environment, String country, String browser, long start, long end, String controlStatus, String controlMessage, Application application, String ip, String url, String port, String tag, String finished, int verbose, int screenshot, String outputFormat, String status, String crbVersion, TCase tCase, CountryEnvParam countryEnvParam,
                              CountryEnvironmentApplication countryEnvironmentApplication, boolean manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData,
                              String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution,
                              MessageGeneral resultMessage) {
        TCExecution newTce = new TCExecution();
        newTce.setApplication(application);
        newTce.setBrowser(browser);
        newTce.setBuild(build);
        newTce.setControlMessage(controlMessage);
        newTce.setControlStatus(controlStatus);
        newTce.setCountry(country);
        newTce.setCrbVersion(crbVersion);
        newTce.setEnd(end);
        newTce.setEnvironment(environment);
        newTce.setFinished(finished);
        newTce.setId(id);
        newTce.setIp(ip);
        newTce.setPort(port);
        newTce.setRevision(revision);
        newTce.setStart(start);
        newTce.setStatus(status);
        newTce.setTag(tag);
        newTce.setTest(test);
        newTce.setTestCase(testCase);
        newTce.setUrl(url);
        newTce.setVerbose(verbose);
        newTce.setScreenshot(screenshot);
        newTce.settCase(tCase);
        newTce.setCountryEnvParam(countryEnvParam);
        newTce.setCountryEnvironmentApplication(countryEnvironmentApplication);
        newTce.setManualURL(manualURL);
        newTce.setMyHost(myHost);
        newTce.setMyContextRoot(myContextRoot);
        newTce.setMyLoginRelativeURL(myLoginRelativeURL);
        newTce.setEnvironmentData(myEnvData);
        newTce.setSeleniumIP(seleniumIP);
        newTce.setSeleniumPort(seleniumPort);
        newTce.setTestCaseStepExecutionList(testCaseStepExecution);
        newTce.setResultMessage(resultMessage);
        newTce.setOutputFormat(outputFormat);
        return newTce;
    }

}
