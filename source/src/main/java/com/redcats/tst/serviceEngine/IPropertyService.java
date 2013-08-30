package com.redcats.tst.serviceEngine;


import com.redcats.tst.entity.TCExecution;
import com.redcats.tst.entity.TestCaseCountryProperties;
import com.redcats.tst.entity.TestCaseExecutionData;
import com.redcats.tst.entity.TestCaseStepActionExecution;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public interface IPropertyService {

    TestCaseExecutionData calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty);
    
    String decodeValue(String myString, List<TestCaseExecutionData> properties, TCExecution tCExecution);
}
