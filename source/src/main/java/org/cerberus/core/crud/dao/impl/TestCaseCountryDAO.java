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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.ITestCaseCountryDAO;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseCountryDAO implements ITestCaseCountryDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseCountry factoryTestCaseCountry;

    private static final Logger LOG = LogManager.getLogger(TestCaseCountryDAO.class);

    private final String OBJECT_NAME = "TestCaseCountry";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testcase) {
        List<TestCaseCountry> testCaseCountries = null;
        final String query = "SELECT * FROM testcasecountry tcc WHERE test = ? AND testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testCase : " + testcase);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testcase);

            try (ResultSet resultSet = preStat.executeQuery();) {
                testCaseCountries = new ArrayList<>();

                while (resultSet.next()) {
                    testCaseCountries.add(loadFromResultSet(resultSet));
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return testCaseCountries;
    }

    @Override
    public AnswerItem<TestCaseCountry> readByKey(String test, String testcase, String country) {
        AnswerItem<TestCaseCountry> answer = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        TestCaseCountry testCaseCountry;
        final String query = "SELECT * FROM testcasecountry tcc WHERE test = ? AND testcase = ? AND country = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testCase : " + testcase);
            LOG.debug("SQL.param.country : " + country);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testcase);
            preStat.setString(i++, country);

            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    testCaseCountry = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer.setItem(testCaseCountry);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<TestCaseCountry> readByVarious1(List<String> system, String test, String testcase, List<TestCase> testCaseList) {
        AnswerList<TestCaseCountry> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseCountry> testCaseCountryList = new ArrayList<>();
        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM testcasecountry tcc ");
        if (system != null && !system.isEmpty()) {
            query.append(" LEFT OUTER JOIN testcase tec on tec.test = tcc.test and tec.testcase = tcc.testcase  ");
            query.append(" LEFT OUTER JOIN application app on app.application = tec.application ");
        }

        query.append(" WHERE 1=1");

        if ((testCaseList != null) && !testCaseList.isEmpty() && testCaseList.size() < 5000) {
            query.append(" AND (");
            int i = 0;
            for (TestCase testCase1 : testCaseList) {
                if (i != 0) {
                    query.append(" OR");
                }
                query.append(" (tcc.`test` = ? and tcc.testcase = ?) ");
                i++;
            }
            query.append(" )");
        }

        if (system != null && !system.isEmpty()) {
            query.append(" AND ");
            query.append(SqlUtil.generateInClause("app.`system`", system));
        }
        if (!StringUtil.isEmptyOrNull(test)) {
            query.append(" AND tcc.test = ?");
        }
        if (testcase != null) {
            query.append(" AND tcc.testcase = ?");
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testCase : " + testcase);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            if ((testCaseList != null) && !testCaseList.isEmpty() && testCaseList.size() < 5000) {
                for (TestCase testCase1 : testCaseList) {
                    preStat.setString(i++, testCase1.getTest());
                    preStat.setString(i++, testCase1.getTestcase());
                }
            }

            if (system != null && !system.isEmpty()) {
                for (String string : system) {
                    preStat.setString(i++, string);
                }
            }
            if (!StringUtil.isEmptyOrNull(test)) {
                preStat.setString(i++, test);
            }
            if (testcase != null) {
                preStat.setString(i++, testcase);
            }

            try (ResultSet resultSet = preStat.executeQuery();) {
                //gets the data
                while (resultSet.next()) {
                    testCaseCountryList.add(this.loadFromResultSet(resultSet));
                }

                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                answer = new AnswerList<>(testCaseCountryList, testCaseCountryList.size());

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public Answer create(TestCaseCountry testCaseCountry) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasecountry (`test`, `testCase`, `country`, `UsrCreated`) ");
        query.append("VALUES (?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.country : " + testCaseCountry.getCountry());
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, testCaseCountry.getTest());
            preStat.setString(i++, testCaseCountry.getTestcase());
            preStat.setString(i++, testCaseCountry.getCountry());
            preStat.setString(i++, testCaseCountry.getUsrCreated() == null ? "" : testCaseCountry.getUsrCreated());

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
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(TestCaseCountry testCaseCountry) {
        MessageEvent msg = null;
        final String query = "DELETE FROM testcasecountry WHERE test = ? and testcase = ? and country = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.country : " + testCaseCountry.getCountry());
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            int i = 1;
            preStat.setString(i++, testCaseCountry.getTest());
            preStat.setString(i++, testCaseCountry.getTestcase());
            preStat.setString(i++, testCaseCountry.getCountry());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(TestCaseCountry testCaseCountry) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE testcasecountry SET ");
        query.append("`UsrModif` = ?, ");
        query.append("`DateModif` = CURRENT_TIMESTAMP ");
        query.append("WHERE Test = ? and TestCase = ? and Country = ? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, testCaseCountry.getUsrModif() == null ? "" : testCaseCountry.getUsrModif());
            preStat.setString(i++, testCaseCountry.getTest());
            preStat.setString(i++, testCaseCountry.getTestcase());
            preStat.setString(i++, testCaseCountry.getCountry());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    private TestCaseCountry loadFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("tcc.test") == null ? "" : resultSet.getString("test");
        String testcase = resultSet.getString("tcc.testcase") == null ? "" : resultSet.getString("testcase");
        String country = resultSet.getString("tcc.country") == null ? "" : resultSet.getString("country");
        String usrCreated = resultSet.getString("tcc.usrCreated");
        Timestamp dateCreated = resultSet.getTimestamp("tcc.dateCreated");
        String usrModif = resultSet.getString("tcc.usrModif");
        Timestamp dateModif = resultSet.getTimestamp("tcc.dateModif");

        return factoryTestCaseCountry.create(test, testcase, country, dateCreated, usrCreated, dateModif, usrModif);
    }

}
