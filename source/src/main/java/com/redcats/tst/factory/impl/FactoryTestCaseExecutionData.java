/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.MessageEvent;
import com.redcats.tst.entity.TestCaseExecutionData;
import com.redcats.tst.factory.IFactoryTestCaseExecutionData;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseExecutionData implements IFactoryTestCaseExecutionData {

    @Override
    public TestCaseExecutionData create(long id, String property, String value, String type, String object,
                                        String returnCode,String rMessage, long start, long end, long startLong, long endLong, MessageEvent message) {
        TestCaseExecutionData testCaseExecutionData = new TestCaseExecutionData();
        testCaseExecutionData.setId(id);
        testCaseExecutionData.setProperty(property);
        testCaseExecutionData.setValue(value);
        testCaseExecutionData.setType(type);
        testCaseExecutionData.setObject(object);
        testCaseExecutionData.setRC(returnCode);
        testCaseExecutionData.setrMessage(rMessage);
        testCaseExecutionData.setStart(start);
        testCaseExecutionData.setEnd(end);
        testCaseExecutionData.setStartLong(startLong);
        testCaseExecutionData.setEndLong(endLong);
        testCaseExecutionData.setPropertyResultMessage(message);
        return testCaseExecutionData;

    }
}
