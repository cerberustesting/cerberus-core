/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.ITestCaseHistoDAO;
import org.cerberus.core.crud.utils.RequestDbUtils;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.crud.entity.TestCaseHisto;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Vertigo17
 */
@Repository
public class TestCaseHistoDAO implements ITestCaseHistoDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;

    private static final Logger LOG = LogManager.getLogger(TestCaseHistoDAO.class);

    private final String OBJECT_NAME = "TestCaseHisto";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public TestCaseHisto readByKey(String test, String testCase, int version) throws CerberusException {

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasehisto teh ");
        query.append("WHERE teh.test = ? , teh.testcase = ? , teh.version = ?  ");

        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.test : " + test);
        LOG.debug("SQL.param.testcase : " + testCase);
        LOG.debug("SQL.param.version : " + version);

        return RequestDbUtils.executeQuery(databaseSpring, query.toString(),
                ps -> {
                    ps.setString(1, test);
                    ps.setString(2, testCase);
                    ps.setInt(3, version);
                },
                resultSet -> {
                    return loadFromResultSet(resultSet);
                }
        );

    }

    @Override
    public Answer create(TestCaseHisto testcasehisto) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasehisto (test, testcase, version, description, testCaseContent, UsrCreated) ");
        query.append("VALUES (?, ?, ?, ?, ?, ?)");

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.test : " + testcasehisto.getTest());
        LOG.debug("SQL.param.testcase : " + testcasehisto.getTestCase());
        LOG.debug("SQL.param.version : " + testcasehisto.getVersion());

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setString(i++, testcasehisto.getTest());
                preStat.setString(i++, testcasehisto.getTestCase());
                preStat.setInt(i++, testcasehisto.getVersion());
                preStat.setString(i++, testcasehisto.getDescription());
                preStat.setString(i++, testcasehisto.getTestCaseContent().toString());
                preStat.setString(i++, testcasehisto.getUsrCreated());

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
                LOG.warn("Unable to execute query : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    private TestCaseHisto loadFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet == null) {
            return null;
        }

        String test = resultSet.getString("tch.test") == null ? "" : resultSet.getString("tch.test");
        String testcase = resultSet.getString("tch.testcase") == null ? "" : resultSet.getString("tch.testcase");
        int version = resultSet.getInt("tch.version");
        String description = resultSet.getString("tch.description") == null ? "" : resultSet.getString("tch.description");
        Timestamp dateVersion = resultSet.getTimestamp("tch.DateVersion");
        String usrCreated = resultSet.getString("tch.UsrCreated");
        Timestamp dateCreated = resultSet.getTimestamp("tch.DateCreated");
        String usrModif = resultSet.getString("tch.UsrModif");
        Timestamp dateModif = resultSet.getTimestamp("tch.DateModif");

        return TestCaseHisto.builder().test(test).testCase(testcase).version(version).description(description).dateCreated(dateCreated).dateModif(dateModif).dateVersion(dateVersion).usrCreated(usrCreated).usrModif(usrModif).build();
    }

    @Override
    public AnswerList<TestCaseHisto> readByDate(Date from, Date to) {
        AnswerList<TestCaseHisto> response = new AnswerList<>();
        List<TestCaseHisto> objectList = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        Timestamp t1;

        StringBuilder searchSQL = new StringBuilder();
        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcasehisto tch ");
        searchSQL.append(" where 1=1 ");

        searchSQL.append(" and DateVersion >= ? and DateVersion <= ? ");
        query.append(searchSQL);
        query.append(" limit ").append(MAX_ROW_SELECTED);

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.from : {}", from);
        LOG.debug("SQL.param.to : {}", to);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            t1 = new Timestamp(from.getTime());
            preStat.setTimestamp(i++, t1);
            t1 = new Timestamp(to.getTime());
            preStat.setTimestamp(i++, t1);

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (objectList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(objectList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerList<TestCaseHisto> readByTestCase(String test, String testcase) {
        AnswerList<TestCaseHisto> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseHisto> testList = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT teh.* FROM testcasehisto teh ");
        query.append("WHERE teh.test = ? and teh.testcase = ? ");

        // FIXME create a generic RequestDbUtils method to manage limit and DATA_OPERATION_WARNING_PARTIAL_RESULT constraint
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(1, testcase);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (testList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(testList, nrTotalRows);
                    } else if (testList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(testList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(testList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
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
        return response;
    }

}
