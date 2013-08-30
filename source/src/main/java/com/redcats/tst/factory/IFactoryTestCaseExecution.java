/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.*;

import java.util.List;
import java.util.Map;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseExecution {

    TestCaseExecution create(String environmentRun, String tag, String output, int verbose,
                             long runID, Country countryExecute, TestCase testCase, List<Property> properties,
                             List<Step> steps, MessageGeneral result, long start, long end, Environment environmentTest,
                             Map<String, String> devEnvironment);

}
