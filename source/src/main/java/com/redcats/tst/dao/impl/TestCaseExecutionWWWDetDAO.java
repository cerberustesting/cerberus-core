package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestCaseExecutionWWWDetDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.StatisticDetail;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 2.0.0
 */
@Repository
public class TestCaseExecutionWWWDetDAO implements ITestCaseExecutionWWWDetDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @param variable Description text text text.
     */
    @Override
    public void register(long runId, StatisticDetail detail) {

        final String query = "INSERT INTO testcaseexecutionwwwdet(ExecID, start, url, end, ext, statusCode, method, bytes, " +
                "timeInMillis, ReqHeader_Host, ResHeader_ContentType, ReqPage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setLong(1, runId);
            preStat.setTimestamp(2, new Timestamp(detail.getStart()));
            preStat.setString(3, detail.getUrl());
            preStat.setTimestamp(4, new Timestamp(detail.getEnd()));
            preStat.setString(5, detail.getExt());
            preStat.setInt(6, detail.getStatus());
            preStat.setString(7, detail.getMethod());
            preStat.setLong(8, detail.getBytes());
            preStat.setLong(9, detail.getTime());
            preStat.setString(10, detail.getHostReq());
            preStat.setString(11, detail.getContentType());
            preStat.setString(12, detail.getPageRes());

            try {
                preStat.executeUpdate();
                MyLogger.log(TestCaseExecutionWWWDetDAO.class.getName(), Level.DEBUG, "Inserting detail. " + detail.getUrl());

            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionWWWDetDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionWWWDetDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
    }

    @Override
    public List<StatisticDetail> getStatistics(long runId) {
        List<StatisticDetail> list = null;
        final String query = "SELECT * FROM testcaseexecutionwwwdet WHERE execid = ?";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setLong(1, runId);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<StatisticDetail>();
                try {
                    while (resultSet.next()) {
                        list.add(loadStatistic(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return list;
    }

    private StatisticDetail loadStatistic(ResultSet resultSet) throws SQLException {
        StatisticDetail statisticDetail = new StatisticDetail();

        statisticDetail.setStart(resultSet.getLong("start"));
        statisticDetail.setEnd(resultSet.getLong("end"));
        statisticDetail.setUrl(resultSet.getString("url"));
        statisticDetail.setExt(resultSet.getString("ext"));
        statisticDetail.setStatus(resultSet.getInt("statusCode"));
        statisticDetail.setMethod(resultSet.getString("method"));
        statisticDetail.setBytes(resultSet.getLong("bytes"));
        statisticDetail.setTime(resultSet.getLong("timeInMillis"));
        statisticDetail.setHostReq(resultSet.getString("ReqHeader_Host"));
        statisticDetail.setContentType(resultSet.getString("ResHeader_ContentType"));
        statisticDetail.setPageRes(resultSet.getString("ReqPage"));

        return statisticDetail;
    }
}
