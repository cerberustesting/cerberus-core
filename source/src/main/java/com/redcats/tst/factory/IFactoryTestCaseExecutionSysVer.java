/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.TestCaseExecutionSysVer;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseExecutionSysVer {

    TestCaseExecutionSysVer create(long id, String system, String build, String revision);
}
