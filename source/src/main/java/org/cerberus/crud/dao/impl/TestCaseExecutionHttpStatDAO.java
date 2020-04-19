/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionHttpStat;
import org.cerberus.crud.factory.impl.FactoryTestCaseExecutionHttpStat;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.cerberus.crud.dao.ITestCaseExecutionHttpStatDAO;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implements methods defined on ITestCaseExecutionHttpStatDAO
 *
 * @author vertigo17
 */
@Repository
public class TestCaseExecutionHttpStatDAO implements ITestCaseExecutionHttpStatDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecutionHttpStat factoryTestCaseExecutionHttpStat;

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionHttpStatDAO.class);

    private final String OBJECT_NAME = "TestCaseExecutionHttpStat";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public Answer create(TestCaseExecutionHttpStat object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcaseexecutionhttpstat (`id`, `start`, `controlstatus`, `system`, `application`, `test`, `testcase`, `country`, `environment`, `robotDecli`");
        query.append(", total_hits, total_size, total_time");
        query.append(", internal_hits, internal_size, internal_time");
        query.append(", img_size, img_size_max, img_hits");
        query.append(", js_size, js_size_max, js_hits");
        query.append(", css_size, css_size_max, css_hits");
        query.append(", html_size, html_size_max, html_hits");
        query.append(", media_size, media_size_max, media_hits");
        query.append(", nb_thirdparty, crbversion");
        query.append(", statDetail ");
        query.append(", UsrCreated");
        query.append(") VALUES (?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setLong(i++, object.getId());
//                preStat.setTimestamp(i++, object.getStart());
                preStat.setString(i++, object.getControlStatus());
                preStat.setString(i++, object.getSystem());
                preStat.setString(i++, object.getApplication());
                preStat.setString(i++, object.getTest());
                preStat.setString(i++, object.getTestCase());
                preStat.setString(i++, object.getCountry());
                preStat.setString(i++, object.getEnvironment());
                preStat.setString(i++, object.getRobotDecli());
                preStat.setInt(i++, object.getTotal_hits());
                preStat.setInt(i++, object.getTotal_size());
                preStat.setInt(i++, object.getTotal_time());
                preStat.setInt(i++, object.getInternal_hits());
                preStat.setInt(i++, object.getInternal_size());
                preStat.setInt(i++, object.getInternal_time());
                preStat.setInt(i++, object.getImg_size());
                preStat.setInt(i++, object.getImg_size_max());
                preStat.setInt(i++, object.getImg_hits());
                preStat.setInt(i++, object.getJs_size());
                preStat.setInt(i++, object.getJs_size_max());
                preStat.setInt(i++, object.getJs_hits());
                preStat.setInt(i++, object.getCss_size());
                preStat.setInt(i++, object.getCss_size_max());
                preStat.setInt(i++, object.getCss_hits());
                preStat.setInt(i++, object.getHtml_size());
                preStat.setInt(i++, object.getHtml_size_max());
                preStat.setInt(i++, object.getHtml_hits());
                preStat.setInt(i++, object.getMedia_size());
                preStat.setInt(i++, object.getMedia_size_max());
                preStat.setInt(i++, object.getMedia_hits());
                preStat.setInt(i++, object.getNb_thirdparty());
                preStat.setString(i++, object.getCrbVersion());
                preStat.setString(i++, object.getStatDetail().toString());
                preStat.setString(i++, object.getUsrCreated());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public AnswerList<TestCaseExecutionHttpStat> readByCriteria(String controlStatus, List<TestCase> testcases, Date from, Date to, List<String> system, List<String> countries, List<String> environments, List<String> robotDecli) {
        AnswerList<TestCaseExecutionHttpStat> response = new AnswerList<>();
        List<TestCaseExecutionHttpStat> objectList = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        StringBuilder searchSQL = new StringBuilder();
        Timestamp t1;

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecutionhttpstat ehs ");

        searchSQL.append(" where 1=1 ");

        // System
        if (system != null && !system.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", system));
        }
        // Country
        if (countries != null && !countries.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Country`", countries));
        }
        // System
        if (environments != null && !environments.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Environment`", environments));
        }
        // System
        if (robotDecli != null && !robotDecli.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`RobotDecli`", robotDecli));
        }
        // from and to
        searchSQL.append(" and start >= ? and start <= ? ");
        // testCase
        StringBuilder testcaseSQL = new StringBuilder();
        for (TestCase testcase : testcases) {
            testcaseSQL.append(" (test = ? and testcase = ?) or ");
        }
        if (!StringUtil.isNullOrEmpty(testcaseSQL.toString())) {
            searchSQL.append("and (").append(testcaseSQL).append(" (0=1) ").append(")");
        }
        // controlStatus
        if (controlStatus != null) {
            searchSQL.append(" and ControlStatus = ? ");
        }

        query.append(searchSQL);

        query.append(" limit ").append(MAX_ROW_SELECTED);

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (system != null && !system.isEmpty()) {
                    for (String syst : system) {
                        preStat.setString(i++, syst);
                    }
                }
                if (countries != null && !countries.isEmpty()) {
                    for (String val : countries) {
                        preStat.setString(i++, val);
                    }
                }
                if (environments != null && !environments.isEmpty()) {
                    for (String val : environments) {
                        preStat.setString(i++, val);
                    }
                }
                if (robotDecli != null && !robotDecli.isEmpty()) {
                    for (String val : robotDecli) {
                        preStat.setString(i++, val);
                    }
                }
                t1 = new Timestamp(from.getTime());
                preStat.setTimestamp(i++, t1);
                t1 = new Timestamp(to.getTime());
                preStat.setTimestamp(i++, t1);
                for (TestCase testcase : testcases) {
                    preStat.setString(i++, testcase.getTest());
                    preStat.setString(i++, testcase.getTestCase());
                }
                if (controlStatus != null) {
                    preStat.setString(i++, controlStatus);
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else if (objectList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public TestCaseExecutionHttpStat loadFromResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("ehs.id");
        Timestamp time = rs.getTimestamp("start");
        String controlStatus = ParameterParserUtil.parseStringParam(rs.getString("ehs.controlstatus"), "");
        String system = ParameterParserUtil.parseStringParam(rs.getString("ehs.system"), "");
        String application = ParameterParserUtil.parseStringParam(rs.getString("ehs.application"), "");
        String test = ParameterParserUtil.parseStringParam(rs.getString("ehs.test"), "");
        String testCase = ParameterParserUtil.parseStringParam(rs.getString("ehs.testcase"), "");
        String country = ParameterParserUtil.parseStringParam(rs.getString("ehs.country"), "");
        String environment = ParameterParserUtil.parseStringParam(rs.getString("ehs.environment"), "");
        String robotdecli = ParameterParserUtil.parseStringParam(rs.getString("ehs.robotdecli"), "");
        String stat = ParameterParserUtil.parseStringParam(rs.getString("ehs.statdetail"), "");
        String crbVersion = ParameterParserUtil.parseStringParam(rs.getString("ehs.crbversion"), "");
        int tothits = rs.getInt("ehs.total_hits");
        int totsize = rs.getInt("ehs.total_size");
        int tottime = rs.getInt("ehs.total_time");
        int inthits = rs.getInt("ehs.internal_hits");
        int intsize = rs.getInt("ehs.internal_size");
        int inttime = rs.getInt("ehs.internal_time");
        int imghits = rs.getInt("ehs.img_hits");
        int imgsize = rs.getInt("ehs.img_size");
        int imgsizem = rs.getInt("ehs.img_size_max");
        int jshits = rs.getInt("ehs.js_hits");
        int jssize = rs.getInt("ehs.js_size");
        int jssizem = rs.getInt("ehs.js_size_max");
        int csshits = rs.getInt("ehs.css_hits");
        int csssize = rs.getInt("ehs.css_size");
        int csssizem = rs.getInt("ehs.css_size_max");
        int htmlhits = rs.getInt("ehs.html_hits");
        int htmlsize = rs.getInt("ehs.html_size");
        int htmlsizem = rs.getInt("ehs.html_size_max");
        int mediahits = rs.getInt("ehs.media_hits");
        int mediasize = rs.getInt("ehs.media_size");
        int mediasizem = rs.getInt("ehs.media_size_max");
        int nbt = rs.getInt("ehs.nb_thirdparty");

        //TODO remove when working in test with mockito and autowired
        factoryTestCaseExecutionHttpStat = new FactoryTestCaseExecutionHttpStat();

        JSONObject statJS = new JSONObject();
        try {
            statJS = new JSONObject(stat);
        } catch (JSONException ex) {
            LOG.warn("Exception when parsing statdetail column to JSON.", ex);
        }

        return factoryTestCaseExecutionHttpStat.create(id, time, controlStatus, system, application, test, testCase, country, environment, robotdecli,
                tothits, totsize, tottime,
                inthits, intsize, inttime,
                imgsize, imgsizem, imghits,
                jssize, jssizem, jshits,
                csssize, csssizem, csshits,
                htmlsize, htmlsizem, htmlhits,
                mediasize, mediasizem, mediahits,
                nbt, crbVersion, statJS, testCase, time, system, time);
    }

}
