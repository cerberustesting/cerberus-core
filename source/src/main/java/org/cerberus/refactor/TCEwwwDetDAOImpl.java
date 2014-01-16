/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.refactor;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.TestCase;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author bcivel
 */
@Repository
public class TCEwwwDetDAOImpl implements ITCEwwwDetDAO {

    @Autowired
    DatabaseSpring databaseSpring;

    @Override
    public List<TestcaseExecutionwwwDet> getListOfDetail(int execId) {
        List<TestcaseExecutionwwwDet> list = null;
        final String query = "SELECT * FROM testcaseexecutionwwwdet WHERE execID = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(execId));
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestcaseExecutionwwwDet>();
                    while (resultSet.next()) {
                        TestcaseExecutionwwwDet detail = new TestcaseExecutionwwwDet();
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
                    MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.FATAL, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.WARN, e.toString());
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
                    MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.FATAL, ex.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.WARN, e.toString());
            }
        }

        return historic;
    }

}
