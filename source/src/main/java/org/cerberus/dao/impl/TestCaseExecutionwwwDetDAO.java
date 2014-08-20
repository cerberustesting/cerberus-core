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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseExecutionwwwDetDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.StatisticDetail;
import org.cerberus.entity.TestCase;
import org.cerberus.entity.TestCaseExecutionwwwDet;
import org.cerberus.entity.TestCaseExecutionwwwSumHistoric;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 2.0.0
 */
@Repository
public class TestCaseExecutionwwwDetDAO implements ITestCaseExecutionwwwDetDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here.
     * <p>
     * And even more explanations to follow in consecutive paragraphs separated
     * by HTML paragraph breaks.
     *
     * @param variable Description text text text.
     */
    @Override
    public void register(long runId, StatisticDetail detail) {

        final String query = "INSERT INTO testcaseexecutionwwwdet(ExecID, start, url, end, ext, statusCode, method, bytes, "
                + "timeInMillis, ReqHeader_Host, ResHeader_ContentType, ReqPage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.DEBUG, "Inserting detail. " + detail.getUrl());

            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.WARN, e.toString());
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
                    MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.WARN, e.toString());
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

    @Override
    public List<TestCaseExecutionwwwDet> getListOfDetail(int execId) {
        List<TestCaseExecutionwwwDet> list = null;
        final String query = "SELECT * FROM testcaseexecutionwwwdet WHERE execID = ?";
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(execId));
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseExecutionwwwDet>();
                    while (resultSet.next()) {
                        TestCaseExecutionwwwDet detail = new TestCaseExecutionwwwDet();
                        detail.setId(resultSet.getString("ID") == null ? 0 : resultSet.getInt("ID"));
                        detail.setExecID(resultSet.getString("EXECID") == null ? 0 : resultSet.getInt("EXECID"));
                        detail.setStart(resultSet.getString("START") == null ? "" : resultSet.getString("START"));
                        detail.setUrl(resultSet.getString("URL") == null ? "" : resultSet.getString("URL"));
                        detail.setEnd(resultSet.getString("END") == null ? "" : resultSet.getString("END"));
                        detail.setExt(resultSet.getString("EXT") == null ? "" : resultSet.getString("EXT"));
                        detail.setStatusCode(resultSet.getInt("StatusCode") == 0 ? 0 : resultSet.getInt("StatusCode"));
                        detail.setMethod(resultSet.getString("Method") == null ? "" : resultSet.getString("Method"));
                        detail.setBytes(resultSet.getString("Bytes") == null ? 0 : resultSet.getInt("Bytes"));
                        detail.setTimeInMillis(resultSet.getString("TimeInMillis") == null ? 0 : resultSet.getInt("TimeInMillis"));
                        detail.setReqHeader_Host(resultSet.getString("ReqHeader_Host") == null ? "" : resultSet.getString("ReqHeader_Host"));
                        detail.setResHeader_ContentType(resultSet.getString("ResHeader_ContentType") == null ? "" : resultSet.getString("ResHeader_ContentType"));
                        list.add(detail);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.FATAL, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseExecutionwwwSumHistoric> getHistoricForParameter(TestCase testcase, String parameter) {
        final String sql = "SELECT start, ? FROM testcaseexecutionwwwsum a JOIN testcaseexecution b ON a.id=b.id WHERE test = ? AND testcase = ? AND country = ? LIMIT 100";
        List<TestCaseExecutionwwwSumHistoric> historic = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, parameter);
                preStat.setString(2, testcase.getTest());
                preStat.setString(3, testcase.getTestCase());
                preStat.setString(4, testcase.getCountryList().get(0));
                ResultSet rs = preStat.executeQuery();
                try {
                    historic = new ArrayList<TestCaseExecutionwwwSumHistoric>();
                    while (rs.next()) {
                        TestCaseExecutionwwwSumHistoric histoToAdd = new TestCaseExecutionwwwSumHistoric();
                        histoToAdd.setStart(rs.getString(1));
                        histoToAdd.setParameter(rs.getString(2));
                        historic.add(histoToAdd);
                    }
                } catch (SQLException ex) {
                    MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.FATAL, ex.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionwwwDetDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return historic;
    }
}
