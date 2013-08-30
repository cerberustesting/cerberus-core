package com.redcats.tst.dao;

import com.redcats.tst.entity.StatisticDetail;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 2.0.0
 */
public interface ITestCaseExecutionWWWDetDAO {

    void register(long runId, StatisticDetail detail);

    List<StatisticDetail> getStatistics(long runId);
}
