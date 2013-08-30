/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;


import com.redcats.tst.entity.TestCase;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITCEwwwDetDAO {

    List<TestcaseExecutionwwwDet> getListOfDetail(int execId);
    
    List<TestCaseExecutionwwwSumHistoric> getHistoricForParameter(TestCase testcase, String parameter);
    
}
