/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.*;

import java.util.List;

/**
 * @author bcivel
 */
public interface IFactoryTCExecution {

    TCExecution create(long id, String test, String testCase, String build, String revision, String environment,
                       String country, String browser, long start, long end, String controlStatus, String controlMessage,
                       Application application, String ip, String url, String port, String tag, String finished, int verbose, int screenshot,
                       String outputFormat, String status, String crbVersion, TCase tCase, CountryEnvParam countryEnvParam,
                       CountryEnvironmentApplication countryEnvironmentApplication, boolean manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData,
                       String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution, MessageGeneral resultMessage);
}
