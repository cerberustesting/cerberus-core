package com.redcats.tst.service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 04/03/2013
 * @since 2.0.0
 */
public interface ITestCaseExecutionWWWService {

    void registerDetail(long runId, String file, String page);

    void registerSummary(long runId);
}
