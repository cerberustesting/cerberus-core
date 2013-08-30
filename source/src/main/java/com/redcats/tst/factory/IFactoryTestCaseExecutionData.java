/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.MessageEvent;
import com.redcats.tst.entity.TestCaseExecutionData;

/**
 *
 * @author bcivel
 */
public interface IFactoryTestCaseExecutionData {
    
    TestCaseExecutionData create(long id,String property,String value,String type,String object,
            String returnCode, String rMessage, long start,long end,long startLong,long endLong, MessageEvent message);
}
