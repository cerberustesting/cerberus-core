package org.cerberus.dto;

import org.cerberus.entity.TCase;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 22/11/2013
 * @since 0.9.1
 */
public interface ITestCaseManualExecutionDTO {

    List<TestCaseManualExecution> findTestCaseManualExecution(TCase testCase, String text, String system, String country, String env);
}
