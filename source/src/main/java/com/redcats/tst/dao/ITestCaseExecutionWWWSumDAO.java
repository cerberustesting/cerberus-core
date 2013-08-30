package com.redcats.tst.dao;

import com.redcats.tst.entity.StatisticSummary;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 2.0.0
 */
public interface ITestCaseExecutionWWWSumDAO {

    void register(long runID, StatisticSummary summary);
}
