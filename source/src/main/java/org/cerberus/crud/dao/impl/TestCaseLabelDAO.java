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

import com.google.common.base.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.ILabelDAO;
import org.cerberus.crud.dao.ITestCaseLabelDAO;
import org.cerberus.crud.entity.Label;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.factory.IFactoryTestCaseLabel;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implements methods defined on ILabelDAO
 *
 */
@Repository
public class TestCaseLabelDAO implements ITestCaseLabelDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseLabel factoryTestCaseLabel;
    @Autowired
    private ILabelDAO labelDAO;

    private static final Logger LOG = LogManager.getLogger(TestCaseLabelDAO.class);

    private final String OBJECT_NAME = "TestCaseLabel";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<TestCaseLabel> readByKeyTech(Integer id) {
        AnswerItem<TestCaseLabel> ans = new AnswerItem<>();
        TestCaseLabel result = null;
        final String query = "SELECT * FROM `testcaselabel` tel WHERE `id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.label : " + id);
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            //prepare and execute query
            preStat.setInt(1, id);

            try (ResultSet resultSet = preStat.executeQuery();) {
                //parse query
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet, null);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (Exception e) {
            LOG.warn("Unable to readByKey TestCaseLabel: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public AnswerItem<TestCaseLabel> readByKey(String test, String testCase, Integer labelId) {
        AnswerItem<TestCaseLabel> ans = new AnswerItem<>();
        TestCaseLabel result = null;
        final String query = "SELECT * FROM `testcaselabel` tel WHERE `labelid` = ? and `test` = ? and `testcase` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.label : " + labelId);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testCase);
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            //prepare and execute query
            preStat.setInt(1, labelId);
            preStat.setString(2, test);
            preStat.setString(3, testCase);

            try (ResultSet resultSet = preStat.executeQuery();) {
                //parse query
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet, null);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (Exception e) {
            LOG.warn("Unable to readByKey TestCaseLabel: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public AnswerList<TestCaseLabel> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<TestCaseLabel> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseLabel> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaselabel tel");
        query.append(" LEFT OUTER JOIN label lab on lab.id = tel.labelId  ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (tel.`id` like ?");
            searchSQL.append(" or tel.`test` like ?");
            searchSQL.append(" or tel.`testcase` like ?");
            searchSQL.append(" or tel.`labelid` like ?");
            searchSQL.append(" or tel.`usrCreated` like ?");
            searchSQL.append(" or tel.`dateCreated` like ?");
            searchSQL.append(" or tel.`usrModif` like ?");
            searchSQL.append(" or tel.`dateModif` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement();) {

            int i = 1;
            if (!StringUtil.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    Label label = labelDAO.loadFromResultSet(resultSet);
                    objectList.add(this.loadFromResultSet(resultSet, label));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
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
                response.setDataList(objectList);

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (Exception e) {
            LOG.warn("Unable to readByCriteria TestCaseLabel: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }
        return response;
    }

    @Override
    public Answer create(TestCaseLabel object) {
        Answer response = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcaselabel ( `test`, `testcase`, `labelId`, `usrCreated`) ");
        query.append("VALUES (?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            preStat.setString(1, object.getTest());
            preStat.setString(2, object.getTestcase());
            preStat.setInt(3, object.getLabelId());
            preStat.setString(4, object.getUsrCreated());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

        } catch (Exception e) {
            LOG.warn("Unable to create TestCaseLabel: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }

        return response;
    }

    @Override
    public Answer delete(TestCaseLabel object) {
        Answer response = new Answer();
        MessageEvent msg = null;
        final String query = "DELETE FROM testcaselabel WHERE id = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            preStat.setInt(1, object.getId());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
        } catch (Exception e) {
            LOG.warn("Unable to delete TestCaseLabel: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }

        return response;
    }

    @Override
    public Answer update(TestCaseLabel object) {
        Answer response = new Answer();
        MessageEvent msg = null;
        final String query = "UPDATE testcaselabel SET `test` = ?, `testcase` = ?, `labelId` = ?,  `usrModif` = ?, `dateModif` = NOW()  WHERE id = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            preStat.setString(1, object.getTest());
            preStat.setString(2, object.getTestcase());
            preStat.setInt(3, object.getLabelId());
            preStat.setString(4, object.getUsrModif());
            preStat.setInt(5, object.getId());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
        } catch (Exception e) {
            LOG.warn("Unable to update TestCaseLabel: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }

        return response;
    }

    @Override
    public TestCaseLabel loadFromResultSet(ResultSet rs, Label label) throws SQLException {
        Integer id = ParameterParserUtil.parseIntegerParam(rs.getString("tel.id"), 0);
        String test = ParameterParserUtil.parseStringParam(rs.getString("tel.test"), "");
        String testcase = ParameterParserUtil.parseStringParam(rs.getString("tel.testcase"), "");
        Integer labelId = ParameterParserUtil.parseIntegerParam(rs.getString("tel.labelId"), 0);
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("tel.usrCreated"), "");
        Timestamp dateCreated = rs.getTimestamp("tel.dateCreated");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("tel.usrModif"), "");
        Timestamp dateModif = rs.getTimestamp("tel.dateModif");
        return factoryTestCaseLabel.create(id, test, testcase, labelId, usrCreated, dateCreated, usrModif, dateModif, label);
    }

    @Override
    public AnswerList<TestCaseLabel> readByTestTestCase(String test, String testCase, List<TestCase> testCaseList) {
        AnswerList<TestCaseLabel> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseLabel> objectList = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaselabel tel ");
        query.append(" LEFT OUTER JOIN label lab on lab.id = tel.labelId  ");

        query.append(" WHERE 1=1");

        HashMap<String, String> testCaseMap = new HashMap<>();
        if ((testCaseList != null) && !testCaseList.isEmpty()) {
            if (testCaseList.size() < 101) {
                // if more than 100 testcases to filter, we only filter by testfolder (this is to reduce the size of SQL sent to database engine)
                query.append(" AND (");
                int j = 0;
                for (TestCase testCase1 : testCaseList) {
                    if (j != 0) {
                        query.append(" OR");
                    }
                    query.append(" (tel.`test` = ? and tel.testcase = ?) ");
                    j++;
                }
                query.append(" )");
            } else {
                for (TestCase testCaseObject : testCaseList) {
                    testCaseMap.put(testCaseObject.getTest(), null);
                }
                query.append(" AND (");
                int j = 0;
                for (Map.Entry<String, String> entry : testCaseMap.entrySet()) {
                    if (j != 0) {
                        query.append(" OR");
                    }
                    query.append(" tel.`test` = ? ");
                    j++;
                }
                query.append(" )");
            }
        }

        if (!Strings.isNullOrEmpty(test)) {
            query.append(" AND tel.test = ?");
        }
        if (testCase != null) {
            query.append(" AND tel.testcase = ?");
        }

        query.append(" ORDER BY Label ASC ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement();) {
            int i = 1;
            if ((testCaseList != null) && !testCaseList.isEmpty()) {
                if (testCaseList.size() < 101) {
                    for (TestCase testCase1 : testCaseList) {
                        preStat.setString(i++, testCase1.getTest());
                        preStat.setString(i++, testCase1.getTestCase());
                    }
                } else {
                    for (Map.Entry<String, String> entry : testCaseMap.entrySet()) {
                        String key = entry.getKey();
                        preStat.setString(i++, key);
                    }
                }
            }
            if (!Strings.isNullOrEmpty(test)) {
                preStat.setString(i++, test);
            }
            if (testCase != null) {
                preStat.setString(i++, testCase);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    Label label = labelDAO.loadFromResultSet(resultSet);
                    TestCaseLabel tcl = this.loadFromResultSet(resultSet, label);
                    objectList.add(tcl);
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
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
                response.setDataList(objectList);
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (Exception e) {
            LOG.warn("Unable to readByTestTestCase TestCaseLabel: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }
        return response;
    }

    @Override
    public AnswerList<TestCaseLabel> readByTypeSystem(String type, String system) {
        AnswerList<TestCaseLabel> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseLabel> objectList = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 

        query.append("SELECT lab.*, tel.* from testcaselabel tel");
        query.append(" JOIN label lab ON lab.id = tel.labelid");

        query.append(" WHERE 1=1");

        if (!Strings.isNullOrEmpty(type)) {
            query.append(" AND lab.type = ?");
        }
        if (system != null) {
            query.append(" AND (lab.system = '' or lab.system = ?)");
        }

        query.append(" ORDER BY Label ASC ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement();) {
            int i = 1;
            if (!Strings.isNullOrEmpty(type)) {
                preStat.setString(i++, type);
            }
            if (system != null) {
                preStat.setString(i++, system);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    Label label = labelDAO.loadFromResultSet(resultSet);
                    TestCaseLabel tcl = this.loadFromResultSet(resultSet, label);
                    objectList.add(tcl);
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
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
                response.setDataList(objectList);
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (Exception e) {
            LOG.warn("Unable to readByTestTestCase TestCaseLabel: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }
        return response;
    }

}
