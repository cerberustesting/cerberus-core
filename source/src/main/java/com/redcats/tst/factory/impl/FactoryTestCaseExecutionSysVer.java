/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.TestCaseExecutionSysVer;
import com.redcats.tst.factory.IFactoryTestCaseExecutionSysVer;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseExecutionSysVer implements IFactoryTestCaseExecutionSysVer {

    @Override
    public TestCaseExecutionSysVer create(long id, String system, String build, String revision) {
        TestCaseExecutionSysVer testCaseExecutionSysVer = new TestCaseExecutionSysVer();
        testCaseExecutionSysVer.setID(id);
        testCaseExecutionSysVer.setSystem(system);
        testCaseExecutionSysVer.setBuild(build);
        testCaseExecutionSysVer.setRevision(revision);
        return testCaseExecutionSysVer;
    }

}
