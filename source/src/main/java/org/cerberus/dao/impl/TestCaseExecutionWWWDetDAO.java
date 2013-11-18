/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.dao.impl;

import org.cerberus.dao.ITestCaseExecutionWWWDetDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.StatisticDetail;
import org.cerberus.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
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

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
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
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionWWWDetDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public List<StatisticDetail> getStatistics(long runId) {
        List<StatisticDetail> list = null;
        final String query = "SELECT * FROM testcaseexecutionwwwdet WHERE execid = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, runId);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<StatisticDetail>();
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
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionWWWDetDAO.class.getName(), Level.WARN, e.toString());
            }
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
