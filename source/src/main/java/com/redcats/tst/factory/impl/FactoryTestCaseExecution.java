/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.*;
import com.redcats.tst.factory.IFactoryTestCaseExecution;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseExecution implements IFactoryTestCaseExecution {

    @Override
    public TestCaseExecution create(String environmentRun, String tag, String output, int verbose,
                                    long runID, Country countryExecute, TestCase testCase, List<Property> properties, List<Step> steps,
                                    MessageGeneral result, long start, long end, Environment environmentTest, Map<String, String> devEnvironment) {
        TestCaseExecution newTce = new TestCaseExecution();
        newTce.setEnvironmentRun(environmentRun);
        newTce.setTag(tag);
        newTce.setOutput(output);
        newTce.setVerbose(verbose);
        newTce.setRunID(runID);
        newTce.setCountryExecute(countryExecute);
        newTce.setTestCase(testCase);
        newTce.setProperties(properties);
        newTce.setSteps(steps);
        newTce.setResult(result);
        newTce.setStart(start);
        newTce.setEnd(end);
        newTce.setEnvironmentTest(environmentTest);
        newTce.setDevEnvironment(devEnvironment);

        return newTce;
    }

}
