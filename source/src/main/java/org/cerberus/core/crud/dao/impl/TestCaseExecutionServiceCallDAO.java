/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseExecutionServiceCallDAO;
import org.cerberus.core.crud.entity.TestCaseExecutionServiceCall;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseExecutionServiceCallDAO implements ITestCaseExecutionServiceCallDAO {

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionServiceCallDAO.class);
    private static final String OBJECT_NAME = "TestCaseExecutionServiceCall";
    private static final int MAX_ROW_SELECTED = 50000;

    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public Answer create(TestCaseExecutionServiceCall object) {
        final String query = "INSERT INTO testcaseexecutionservicecall (`exeid`, `start`, `service`, `application`, `type`, `method`, `httpcode`, `durationms`, `responsesize`,"
                + " `status`, `system`, `test`, `testcase`, `country`, `environment`, `robotdecli`, `usrcreated`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'')";
        MessageEvent msg;
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            int i = 1;
            preStat.setLong(i++, object.getExeId());
            preStat.setTimestamp(i++, new Timestamp(object.getStart()));
            preStat.setString(i++, object.getService());
            preStat.setString(i++, object.getApplication());
            preStat.setString(i++, object.getType());
            preStat.setString(i++, object.getMethod());
            preStat.setInt(i++, object.getHttpCode());
            preStat.setInt(i++, object.getDurationMs());
            preStat.setInt(i++, object.getResponseSize());
            preStat.setString(i++, object.getStatus());
            preStat.setString(i++, object.getSystem());
            preStat.setString(i++, object.getTest());
            preStat.setString(i++, object.getTestcase());
            preStat.setString(i++, object.getCountry());
            preStat.setString(i++, object.getEnvironment());
            preStat.setString(i++, object.getRobotDecli());
            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public int deleteOlderThan(Date before, int limit) {
        final String query = "DELETE FROM testcaseexecutionservicecall WHERE `start` < ? LIMIT ?";
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setTimestamp(1, new Timestamp(before.getTime()));
            preStat.setInt(2, limit);
            return preStat.executeUpdate();
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            return 0;
        }
    }

    @Override
    public void setFileLevel(long exeId, String service, long start, String fileLevel) {
        final String query = "UPDATE testcaseexecutionservicecall SET `filelevel` = ? WHERE `exeid` = ? AND `service` = ? AND `start` = ?";
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setString(1, fileLevel);
            preStat.setLong(2, exeId);
            preStat.setString(3, service);
            preStat.setTimestamp(4, new Timestamp(start));
            preStat.executeUpdate();
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
    }

    @Override
    public AnswerList<TestCaseExecutionServiceCall> readByService(String service, Date from, Date to) {
        final String query = "SELECT * FROM testcaseexecutionservicecall WHERE `service` = ? AND `start` >= ? AND `start` <= ? ORDER BY `start` DESC LIMIT " + MAX_ROW_SELECTED;
        List<TestCaseExecutionServiceCall> list = new ArrayList<>();
        MessageEvent msg;
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setString(1, service);
            preStat.setTimestamp(2, new Timestamp(from.getTime()));
            preStat.setTimestamp(3, new Timestamp(to.getTime()));
            try (ResultSet rs = preStat.executeQuery()) {
                while (rs.next()) {
                    list.add(loadFromResultSet(rs));
                }
            }
            // The newest calls were selected first (so that the limit drops the oldest ones), the caller wants them oldest first.
            java.util.Collections.reverse(list);
            if (list.size() >= MAX_ROW_SELECTED) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        AnswerList<TestCaseExecutionServiceCall> answer = new AnswerList<>(list, list.size());
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public List<String[]> readServicesWithCalls(Date from, Date to) {
        final String query = "SELECT `service`, COUNT(*) FROM testcaseexecutionservicecall WHERE `start` >= ? AND `start` <= ? GROUP BY `service` ORDER BY `service`";
        List<String[]> list = new ArrayList<>();
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setTimestamp(1, new Timestamp(from.getTime()));
            preStat.setTimestamp(2, new Timestamp(to.getTime()));
            try (ResultSet rs = preStat.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{rs.getString(1), String.valueOf(rs.getInt(2))});
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        return list;
    }

    private TestCaseExecutionServiceCall loadFromResultSet(ResultSet rs) throws SQLException {
        TestCaseExecutionServiceCall o = new TestCaseExecutionServiceCall();
        o.setId(rs.getLong("id"));
        o.setExeId(rs.getLong("exeid"));
        Timestamp start = rs.getTimestamp("start");
        o.setStart(start == null ? 0 : start.getTime());
        o.setService(rs.getString("service"));
        o.setApplication(rs.getString("application"));
        o.setType(rs.getString("type"));
        o.setMethod(rs.getString("method"));
        o.setHttpCode(rs.getInt("httpcode"));
        o.setDurationMs(rs.getInt("durationms"));
        o.setResponseSize(rs.getInt("responsesize"));
        o.setStatus(rs.getString("status"));
        o.setSystem(rs.getString("system"));
        o.setTest(rs.getString("test"));
        o.setTestcase(rs.getString("testcase"));
        o.setCountry(rs.getString("country"));
        o.setEnvironment(rs.getString("environment"));
        o.setRobotDecli(rs.getString("robotdecli"));
        o.setFileLevel(rs.getString("filelevel"));
        return o;
    }
}
