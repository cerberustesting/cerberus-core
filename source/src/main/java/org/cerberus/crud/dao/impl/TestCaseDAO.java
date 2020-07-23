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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.dto.TestCaseListDTO;
import org.cerberus.dto.TestListDTO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.security.UserSecurity;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Used to manage TestCase table
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/12/2012
 * @since 0.9.0
 */
@Repository
public class TestCaseDAO implements ITestCaseDAO {

    /**
     * Class used to manage connection.
     *
     * @see org.cerberus.database.DatabaseSpring
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCase factoryTestCase;
    @Autowired
    private IParameterService parameterService;

    public static class Query {

        private static final String FIND_BY_APPLICATION = "SELECT * FROM `testcase` tec WHERE `application` = ?";
    }

    private static final Logger LOG = LogManager.getLogger(TestCaseDAO.class);

    private final String OBJECT_NAME = "TestCase";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public List<TestCase> findTestCaseByTest(String test) {
        List<TestCase> list = null;
        final String query = "SELECT * FROM testcase tec WHERE test = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCase>();

                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn(exception.toString());
            }
        }

        return list;
    }

    @Override
    public AnswerList<TestCase> readByTestByCriteria(List<String> system, String test, int start, int amount, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<TestCase> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCase> testCaseList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcase tec ");
        query.append(" LEFT OUTER JOIN application app on app.application = tec.application ");
        if (!StringUtil.isNullOrEmpty(searchTerm) || individualSearch.get("lab.label") != null
                || individualSearch.get("lab.labelsSTICKER") != null || individualSearch.get("lab.labelsREQUIREMENT") != null || individualSearch.get("lab.labelsBATTERY") != null) {
            // We don't join the label table if we don't need to.
            query.append(" LEFT OUTER JOIN testcaselabel tel on tec.test = tel.test AND tec.testcase = tel.testcase ");
            query.append(" LEFT OUTER JOIN label lab on tel.labelId = lab.id ");
        }

        searchSQL.append("WHERE 1=1");

        // Always filter on system user can view
        searchSQL.append(" AND " + UserSecurity.getSystemAllowForSQL("app.`system`") + " ");

        if (system != null && !system.isEmpty()) {
            searchSQL.append(" AND ");
            searchSQL.append(SqlUtil.generateInClause("app.`system`", system));
        }

        if (!StringUtil.isNullOrEmpty(test)) {
            searchSQL.append(" AND tec.`test` = ?");
        }

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (tec.`testcase` like ?");
            searchSQL.append(" or tec.`test` like ?");
            searchSQL.append(" or tec.`application` like ?");
            searchSQL.append(" or tec.`usrCreated` like ?");
            searchSQL.append(" or tec.`usrModif` like ?");
            searchSQL.append(" or tec.`isActive` like ?");
            searchSQL.append(" or tec.`status` like ?");
            searchSQL.append(" or tec.`type` like ?");
            searchSQL.append(" or tec.`priority` like ?");
            searchSQL.append(" or tec.`dateCreated` like ?");
            searchSQL.append(" or tec.`description` like ?");
            searchSQL.append(" or lab.`label` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");

                String toto = entry.getKey();
                if (entry.getKey().equals("lab.labelsSTICKER") || entry.getKey().equals("lab.labelsREQUIREMENT") || entry.getKey().equals("lab.labelsBATTERY")) {
                    toto = "lab.label";
                }

                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(toto, entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);

        query.append(" group by tec.test, tec.testcase ");

        if (!StringUtil.isNullOrEmpty(sortInformation)) {
            query.append(" order by ").append(sortInformation);
        }

        if (amount != 0) {
            query.append(" limit ").append(start).append(" , ").append(amount);
        } else {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        }

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
                    for (String string : system) {
                        preStat.setString(i++, string);
                    }
                }
                if (!StringUtil.isNullOrEmpty(test)) {
                    preStat.setString(i++, test);
                }
                if (!Strings.isNullOrEmpty(searchTerm)) {
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
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

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testCaseList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (testCaseList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        answer = new AnswerList<>(testCaseList, nrTotalRows);
                    } else if (testCaseList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        answer = new AnswerList<>(testCaseList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        answer = new AnswerList<>(testCaseList, nrTotalRows);
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

        answer.setResultMessage(msg);
        answer.setDataList(testCaseList);
        return answer;
    }

    @Override
    public TestCase findTestCaseByKey(String test, String testCase) throws CerberusException {
        boolean throwExcep = false;
        TestCase result = null;
        final String query = "SELECT * FROM testcase tec  LEFT OUTER JOIN application app on app.application = tec.application WHERE test = ? AND testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        result = this.loadFromResultSet(resultSet);
                    } else {
                        result = null;
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public AnswerList<TestListDTO> findTestCaseByService(String service) {
        AnswerList<TestListDTO> ansList = new AnswerList<>();
        MessageEvent rs;
        List<TestListDTO> listOfTests = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCase> testCaseList = new ArrayList<>();
        final String sql = " select count(*) as total, t.Test, tc.TestCase, t.Description as testDescription, tc.Description as testCaseDescription, tc.Application,"
                + "tc.isActive, tc.`Type`, tc.UsrCreated, tc.`Status` "
                + " from testcase tc INNER JOIN test t ON t.test = tc.test "
                + " INNER JOIN testcasestepaction tcsa ON tcsa.TestCase = tc.TestCase AND tcsa.Test = t.Test "
                + " INNER JOIN appservice ser ON ser.Service = tcsa.Value1 "
                + " WHERE ser.Service = ? "
                + " group by tc.test, tc.TestCase";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, service);

                HashMap<String, TestListDTO> map = new HashMap<String, TestListDTO>();

                String key, test, testCase;
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        TestListDTO testList;
                        TestCaseListDTO testCaseDTO;

                        test = resultSet.getString("Test");
                        testCase = resultSet.getString("TestCase");

                        if (map.containsKey(test)) {
                            testList = map.get(test);
                        } else {
                            testList = new TestListDTO();

                            testList.setDescription(resultSet.getString("testDescription"));
                            testList.setTest(test);
                        }

                        testCaseDTO = new TestCaseListDTO();
                        testCaseDTO.setTestCaseDescription(resultSet.getString("testCaseDescription"));
                        testCaseDTO.setTestCaseNumber(testCase);
                        testCaseDTO.setApplication(resultSet.getString("Application"));
                        testCaseDTO.setCreator(resultSet.getString("tc.UsrCreated"));
                        testCaseDTO.setStatus(resultSet.getString("Status"));

                        testCaseDTO.setGroup(resultSet.getString("Group"));
                        testCaseDTO.setIsActive(resultSet.getString("isActive"));
                        testList.getTestCaseList().add(testCaseDTO);
                        map.put(test, testList);

                    }

                    listOfTests = new ArrayList<TestListDTO>(map.values());

                    if (listOfTests.isEmpty()) {
                        rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        rs.setDescription(rs.getDescription().replace("%ITEM%", "List of Test Cases").replace("%OPERATION%", "Select"));
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        ansList.setResultMessage(rs);
        ansList.setDataList(listOfTests);

        return ansList;
    }

    @Override
    public AnswerList<TestListDTO> findTestCaseByServiceByDataLib(String service) {
        AnswerList<TestListDTO> ansList = new AnswerList<>();
        MessageEvent rs;
        List<TestListDTO> listOfTests = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCase> testCaseList = new ArrayList<>();

        final String sql = " select count(*) as total, t.Test, tc.TestCase, t.Description as testDescription, tc.Description as testCaseDescription, tc.Application,"
                + "tc.isActive, tc.`Type`, tc.UsrCreated, tc.`Status` "
                + " from testcase tc INNER JOIN test t ON t.test = tc.test"
                + " INNER JOIN testcasecountryproperties tccp ON tccp.Test = t.Test AND tccp.TestCase = tc.TestCase"
                + " INNER JOIN testdatalib td ON td.Name = tccp.Value1 AND (tccp.Country = td.Country or td.country='') and tccp.test = t.test and tccp.testcase = tc.testcase"
                + " INNER JOIN appservice ser on ser.Service = td.Service"
                + " WHERE ser.Service = ?"
                + " group by tc.test, tc.TestCase";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, service);

                HashMap<String, TestListDTO> map = new HashMap<>();

                String key, test, testCase;
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        TestListDTO testList;
                        TestCaseListDTO testCaseDTO;

                        test = resultSet.getString("Test");
                        testCase = resultSet.getString("TestCase");

                        if (map.containsKey(test)) {
                            testList = map.get(test);
                        } else {
                            testList = new TestListDTO();

                            testList.setDescription(resultSet.getString("testDescription"));
                            testList.setTest(test);
                        }

                        testCaseDTO = new TestCaseListDTO();
                        testCaseDTO.setTestCaseDescription(resultSet.getString("testCaseDescription"));
                        testCaseDTO.setTestCaseNumber(testCase);
                        testCaseDTO.setApplication(resultSet.getString("Application"));
                        testCaseDTO.setCreator(resultSet.getString("tc.UsrCreated"));
                        testCaseDTO.setStatus(resultSet.getString("Status"));

                        testCaseDTO.setGroup(resultSet.getString("Type"));
                        testCaseDTO.setIsActive(resultSet.getString("isActive"));
                        testList.getTestCaseList().add(testCaseDTO);
                        map.put(test, testList);

                    }

                    listOfTests = new ArrayList<>(map.values());

                    if (listOfTests.isEmpty()) {
                        rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        rs.setDescription(rs.getDescription().replace("%ITEM%", "List of Test Cases").replace("%OPERATION%", "Select"));
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        ansList.setResultMessage(rs);
        ansList.setDataList(listOfTests);

        return ansList;
    }

    @Override
    public boolean updateTestCaseInformation(TestCase testCase) {
        boolean res = false;
        final String sql = "UPDATE testcase tc SET tc.Application = ?, tc.DetailedDescription = ?, tc.isActiveQA = ?, tc.isActiveUAT = ?, tc.isActivePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.isActive = ?, tc.Description = ?, tc.Type = ?, tc.Comment = ?, tc.FromMajor = ?, "
                + "tc.FromMinor = ?, tc.ToMajor = ?, tc.ToMinor = ?, tc.Bugs = ?, tc.TargetMajor = ?, tc.Implementer = ?, tc.Executor = ?, tc.LastModifier = ?, tc.TargetMinor = ?, "
                + "tc.conditionOperator = ?, tc.conditionVal1 = ?, tc.conditionVal2 = ? , tc.conditionVal3 = ? "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                int i = 1;
                preStat.setString(i++, testCase.getApplication());
                preStat.setString(i++, testCase.getDetailedDescription());
                preStat.setBoolean(i++, testCase.isActiveQA());
                preStat.setBoolean(i++, testCase.isActiveUAT());
                preStat.setBoolean(i++, testCase.isActivePROD());
                preStat.setString(i++, Integer.toString(testCase.getPriority()));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setBoolean(i++, testCase.isActive());
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getType(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getBugs().toString(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getExecutor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUsrModif(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionOperator(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal1(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal2(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal3(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean updateTestCaseInformationCountries(TestCase tc) {
        boolean res = false;
        final String sql_count = "SELECT Country FROM testcasecountry WHERE Test = ? AND TestCase = ?";
        ArrayList<String> countriesDB = new ArrayList<String>();

        List<String> countryList = new ArrayList<String>();
        for (TestCaseCountry tcCountry : tc.getTestCaseCountries()) {
            countryList.add(tcCountry.getCountry());
        }
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql_count);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql_count);
            try {
                preStat.setString(1, tc.getTest());
                preStat.setString(2, tc.getTestCase());
                ResultSet rsCount = preStat.executeQuery();
                try {
                    while (rsCount.next()) {
                        countriesDB.add(rsCount.getString("Country"));
                        if (!countryList.contains(rsCount.getString("Country"))) {
                            final String sql_delete = "DELETE FROM testcasecountry WHERE Test = ? AND TestCase = ? AND Country = ?";

                            PreparedStatement preStat2 = connection.prepareStatement(sql_delete);
                            try {
                                preStat2.setString(1, tc.getTest());
                                preStat2.setString(2, tc.getTestCase());
                                preStat2.setString(3, rsCount.getString("Country"));

                                preStat2.executeUpdate();
                            } catch (SQLException exception) {
                                LOG.error("Unable to execute query : " + exception.toString());
                            } finally {
                                preStat2.close();
                            }
                        }
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    rsCount.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

            res = true;
            for (int i = 0; i < countryList.size() && res; i++) {
                if (!countriesDB.contains(countryList.get(i))) {
                    final String sql_insert = "INSERT INTO testcasecountry (test, testcase, country) VALUES (?, ?, ?)";

                    PreparedStatement preStat2 = connection.prepareStatement(sql_insert);
                    try {
                        preStat2.setString(1, tc.getTest());
                        preStat2.setString(2, tc.getTestCase());
                        preStat2.setString(3, countryList.get(i));

                        res = preStat2.executeUpdate() > 0;
                    } catch (SQLException exception) {
                        LOG.error("Unable to execute query : " + exception.toString());
                    } finally {
                        preStat2.close();
                    }
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean createTestCase(TestCase testCase) {
        boolean res = false;

        final StringBuffer sql = new StringBuffer("INSERT INTO `testcase` ")
                .append(" ( `Test`, `TestCase`, `Application`, ")
                .append("`Description`, `DetailedDescription`, ")
                .append("`Priority`, `Status`, `isActive`, ")
                .append("`Type`, `Origine`, `RefOrigine`, `Comment`, ")
                .append("`FromMajor`, `FromMinor`, `ToMajor`, `ToMinor`, ")
                .append("`Bugs`, `TargetMajor`, `TargetMinor`, `UsrCreated`, ")
                .append("`Implementer`, `Executor`, `UsrModif`, `isActiveQA`, `isActiveUAT`, `isActivePROD`, `useragent`, `screensize`, ")
                .append("`conditionOperator`, `conditionVal1`, `conditionVal2`, `conditionVal3`) ")
                .append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ); ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                int i = 1;
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getApplication(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getDetailedDescription(), ""));
                preStat.setString(i++, Integer.toString(testCase.getPriority()));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setBoolean(i++, testCase.isActive());
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getType(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getOrigine(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getRefOrigine(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getBugs().toString(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUsrCreated(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getExecutor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUsrModif(), ""));
                preStat.setBoolean(i++, testCase.isActiveQA());
                preStat.setBoolean(i++, testCase.isActiveUAT());
                preStat.setBoolean(i++, testCase.isActivePROD());
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUserAgent(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getScreenSize(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionOperator(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal1(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal2(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal3(), ""));

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return res;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<TestCase> findTestCaseByApplication(final String application) {
        List<TestCase> testCases = null;
        try (final Connection connection = databaseSpring.connect();
                final PreparedStatement statement = connection.prepareStatement(Query.FIND_BY_APPLICATION)) {
            statement.setString(1, application);
            testCases = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery();) {
                while (resultSet.next()) {
                    testCases.add(loadFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.warn("Unable to get test cases for application " + application, e);
        }
        return testCases;
    }

    @Override
    public List<TestCase> findTestCaseByCriteria(String test, String application, String country, String isActive) {
        List<TestCase> list = null;
        final String query = "SELECT tec.* FROM testcase tec "
                + "JOIN testcasecountry tcc "
                + "LEFT OUTER JOIN application app on app.application = tec.application "
                + "WHERE tec.test=tcc.test AND tec.testcase=tcc.testcase "
                + "AND tec.test = ? AND tec.application = ? AND tcc.country = ? AND tec.isActive = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, application);
                preStat.setString(3, country);
                preStat.setBoolean(4, ParameterParserUtil.parseBooleanParam(isActive, false));

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TestCase> findTestCaseByCriteria(TestCase testCase, String text, String system) {
        List<TestCase> list = null;
        String query = new StringBuilder()
                .append("SELECT tec.* FROM testcase tec ")
                .append("LEFT OUTER JOIN application app ON app.application=tec.application ")
                .append(" WHERE (tec.test LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.test", testCase.getTest()))
                .append(") AND (tec.bugs LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.bugs", testCase.getBugs().toString()))
                .append(") AND (tec.origine LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.origine", testCase.getOrigine()))
                .append(") AND (app.system LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("app.system", system))
                .append(") AND (tec.application LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.application", testCase.getApplication()))
                .append(") AND (tec.priority LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfMinusOne("tec.priority", testCase.getPriority()))
                .append(") AND (tec.status LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.status", testCase.getStatus()))
                .append(") AND (tec.Type LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.Type", testCase.getType()))
                .append(") AND (tec.isActivePROD LIKE ")
                .append(testCase.isActivePROD())
                .append(") AND (tec.isActiveUAT LIKE ")
                .append(testCase.isActiveUAT())
                .append(") AND (tec.isActiveQA LIKE ")
                .append(testCase.isActiveQA())
                .append(") AND (tec.description LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.description", text))
                .append(" OR tec.DetailedDescription LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.DetailedDescription", text))
                .append(" OR tec.comment LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.comment", text))
                .append(") AND (tec.isActive LIKE ")
                .append(testCase.isActive())
                .append(") AND (tec.FromMajor LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.FromMajor", testCase.getFromMajor()))
                .append(") AND (tec.FromMinor LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.FromMinor", testCase.getFromMinor()))
                .append(") AND (tec.ToMajor LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.ToMajor", testCase.getToMajor()))
                .append(") AND (tec.ToMinor LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.ToMinor", testCase.getToMinor()))
                .append(") AND (tec.TargetMajor LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.TargetMajor", testCase.getTargetMajor()))
                .append(") AND (tec.TargetMinor LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.TargetMinor", testCase.getTargetMinor()))
                .append(") AND (tec.testcase LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.testcase", testCase.getTestCase()))
                .append(") AND (tec.Executor LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.Executor", testCase.getExecutor()))
                .append(") AND (tec.Implementer LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.Implementer", testCase.getImplementer()))
                .append(")").toString();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return list;
    }

    @Override
    public AnswerList<TestCase> readByVarious(String[] test, String[] app, String[] creator, String[] implementer, String[] system,
            String[] campaign, List<Integer> labelid, String[] priority, String[] type, String[] status, int length) {
        AnswerList<TestCase> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCase> testCaseList = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM testcase tec ");
        query.append("LEFT JOIN application app ON tec.application = app.application ");
        if ((labelid != null) || (campaign != null)) {
            query.append("LEFT JOIN testcaselabel tel ON tec.test = tel.test AND tec.testcase = tel.testcase ");
            query.append("LEFT JOIN campaignlabel cpl ON cpl.labelId = tel.labelId ");
        }
        query.append("WHERE 1=1 AND tec.isActive = 1 ");
        query.append(createInClauseFromList(test, "tec.test", "AND ", " "));
        query.append(createInClauseFromList(app, "tec.application", "AND ", " "));
        query.append(createInClauseFromList(creator, "tec.usrCreated", "AND ", " "));
        query.append(createInClauseFromList(implementer, "tec.implementer", "AND ", " "));
        query.append(createInClauseFromList(priority, "tec.priority", "AND ", " "));
        query.append(createInClauseFromList(type, "tec.type", "AND ", " "));
        query.append(createInClauseFromList(status, "tec.status", "AND ", " "));
        query.append(createInClauseFromList(system, "app.system", "AND ", " "));
        query.append(SqlUtil.createWhereInClauseInteger("tel.labelid", labelid, "AND ", " "));
        if (campaign != null) {
            query.append(createInClauseFromList(campaign, "cpl.campaign", " AND (", ") "));
        }
        query.append("GROUP BY tec.test, tec.testcase ");
        if (length != -1) {
            query.append("LIMIT ?");
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            if (length != -1) {
                preStat.setInt(1, length);
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testCaseList.add(this.loadFromResultSet(resultSet));
                    }

                    if (testCaseList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        answer = new AnswerList<>(testCaseList, testCaseList.size());
                    } else if (testCaseList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        answer = new AnswerList<>(testCaseList, testCaseList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        answer = new AnswerList<>(testCaseList, testCaseList.size());
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

        answer.setResultMessage(msg);
        answer.setDataList(testCaseList);
        return answer;
    }

    @Override
    public List<String> findUniqueDataOfColumn(String column) {
        List<String> list = null;
        final String query = "SELECT DISTINCT tec." + column + " FROM testcase tec LEFT OUTER JOIN application a ON a.application=tec.application ORDER BY tec." + column + " ASC";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);
                ResultSet resultSet = preStat.executeQuery();) {

            list = new ArrayList<String>();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        return list;
    }

    @Override
    public boolean deleteTestCase(TestCase testCase) {
        boolean bool = false;
        final String query = "DELETE FROM testcase WHERE test = ? AND testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testCase.getTest());
                preStat.setString(2, testCase.getTestCase());

                bool = preStat.executeUpdate() > 0;

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return bool;
    }

    @Override
    public void updateTestCase(TestCase testCase) throws CerberusException {
        final String sql = "UPDATE testcase tc SET tc.Application = ?, tc.DetailedDescription = ?, tc.isActiveQA = ?, tc.isActiveUAT = ?, tc.isActivePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.isActive = ?, tc.Description = ?, tc.Type = ?, tc.Comment = ?, tc.FromMajor = ?, "
                + "tc.FromMinor = ?, tc.ToMajor = ?, tc.ToMinor = ?, tc.Bugs = ?, tc.TargetMajor = ?, tc.Implementer = ?, tc.Executor = ?, tc.UsrModif = ?, tc.TargetMinor = ?,"
                + " `conditionOperator` = ?, `conditionVal1` = ?, `conditionVal2` = ?, `conditionVal3` = ?, `useragent` = ?, `screensize` = ?, `version` = ?, dateModif = CURRENT_TIMESTAMP "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                int i = 1;
                preStat.setString(i++, testCase.getApplication());
                preStat.setString(i++, testCase.getDetailedDescription());
                preStat.setBoolean(i++, testCase.isActiveQA());
                preStat.setBoolean(i++, testCase.isActiveUAT());
                preStat.setBoolean(i++, testCase.isActivePROD());
                preStat.setString(i++, Integer.toString(testCase.getPriority()));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setBoolean(i++, testCase.isActive());
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getType(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getBugs().toString(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getExecutor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUsrModif(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionOperator(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal1(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal2(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal3(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUserAgent(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getScreenSize(), ""));
                preStat.setInt(i++, ParameterParserUtil.parseIntegerParam(testCase.getVersion(), 0));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));

                preStat.executeUpdate();
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
    }

    @Override
    public String getMaxNumberTestCase(String test) {
        String max = "";
        final String sql = "SELECT  Max( CAST(Testcase AS UNSIGNED) ) as MAXTC FROM testcase where test = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, test);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        max = resultSet.getString("MAXTC");
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return max;
    }

    @Override
    public AnswerList<TestCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries, List<Integer> labelIdList, String[] status, String[] system, String[] application, String[] priority, String[] type, Integer maxReturn) {

        List<TestCase> list = null;
        AnswerList<TestCase> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        HashMap<String, String[]> tcParameters = new HashMap<>();
        tcParameters.put("status", status);
        tcParameters.put("system", system);
        tcParameters.put("application", application);
        tcParameters.put("priority", priority);
        tcParameters.put("countries", countries);
        tcParameters.put("type", type);
        boolean withLabel = (labelIdList.size() > 0);

        StringBuilder query = new StringBuilder("SELECT tec.*, app.system FROM testcase tec ");

        if (withLabel) {
            query.append("LEFT OUTER JOIN application app ON app.application = tec.application ")
                    .append("INNER JOIN testcasecountry tcc ON tcc.Test = tec.Test and tcc.TestCase = tec.TestCase ")
                    .append("LEFT JOIN testcaselabel tel ON tec.test = tel.test AND tec.testcase = tel.testcase ")
                    .append("WHERE ")
                    .append(SqlUtil.createWhereInClauseInteger("tel.labelId", labelIdList, "", ""));
        } else if (!withLabel && (status != null || system != null || application != null || priority != null)) {
            query.append("LEFT OUTER JOIN application app ON app.application = tec.application ")
                    .append("INNER JOIN testcasecountry tcc ON tcc.Test = tec.Test and tcc.TestCase = tec.TestCase ")
                    .append("WHERE 1=1");
        } else {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "You have a problem in your campaign definition"));
            answer.setResultMessage(msg);
            return answer;
        }

        for (Entry<String, String[]> entry : tcParameters.entrySet()) {
            String cle = entry.getKey();
            String[] valeur = entry.getValue();
            if (valeur != null && valeur.length > 0) {
                if (!cle.equals("system") && !cle.equals("countries")) {
                    query.append(" AND tec." + cle + " in (?");
                } else if (cle.equals("system")) {
                    query.append(" AND app.system in (?");
                } else {
                    query.append(" AND tcc.Country in (?");
                }
                if (valeur.length > 1) {
                    for (int i = 0; i < valeur.length - 1; i++) {
                        query.append(",?");
                    }
                }
                query.append(")");
            }
        }

        query.append(" GROUP BY tec.test, tec.testcase LIMIT ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;

                for (Entry<String, String[]> entry : tcParameters.entrySet()) {
                    String[] valeur = entry.getValue();
                    if (valeur != null && valeur.length > 0) {
                        for (String c : valeur) {
                            preStat.setString(i++, c);
                            LOG.debug("SQL.param : " + c);
                        }
                    }
                }

                preStat.setInt(i++, maxReturn);
                LOG.debug("SQL.param : " + maxReturn);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                    if (list.size() >= maxReturn) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + maxReturn));
                        answer.setDataList(list);
                    } else if (list.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        answer.setDataList(list);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        answer.setDataList(list);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    answer.setResultMessage(msg);
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return answer;
    }

    @Override
    public List<TestCase> findTestCaseByTestSystem(String test, String system) {
        List<TestCase> list = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM testcase tec join application app on tec.application = app.application ");
        sb.append(" WHERE tec.test = ? and app.system = ? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sb.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sb.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCase>();

                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TestCase> findTestCaseByCriteria(String[] test, String[] app, String[] isActive, String[] priority, String[] status, String[] type, String[] targetMajor, String[] targetMinor, String[] creator, String[] implementer, String[] campaign) {
        List<TestCase> list = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM testcase tec join application app on tec.application=app.application ");
        sb.append(" WHERE 1=1 ");
        sb.append(SqlUtil.createWhereInClause(" AND tec.Test", test == null ? null : Arrays.asList(test), true));
        sb.append(SqlUtil.createWhereInClause(" AND tec.Application", app == null ? null : Arrays.asList(app), true));
        sb.append(SqlUtil.createWhereInClause(" AND tec.isActive", isActive == null ? null : Arrays.asList(isActive), true));
        sb.append(SqlUtil.createWhereInClause(" AND tec.priority", priority == null ? null : Arrays.asList(priority), true));
        sb.append(SqlUtil.createWhereInClause(" AND tec.status", status == null ? null : Arrays.asList(status), true));
        sb.append(SqlUtil.createWhereInClause(" AND tec.type", type == null ? null : Arrays.asList(type), true));
        sb.append(SqlUtil.createWhereInClause(" AND tec.TargetMajor", targetMajor == null ? null : Arrays.asList(targetMajor), true));
        sb.append(SqlUtil.createWhereInClause(" AND tec.TargetMinor", targetMinor == null ? null : Arrays.asList(targetMinor), true));
        sb.append(SqlUtil.createWhereInClause(" AND tec.creator", creator == null ? null : Arrays.asList(creator), true));
        sb.append(SqlUtil.createWhereInClause(" AND tec.implementer", implementer == null ? null : Arrays.asList(implementer), true));
        sb.append(" GROUP BY tec.test, tec.testcase ");
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sb.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sb.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCase>();

                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return list;
    }

    @Override
    public String findSystemOfTestCase(String test, String testcase) throws CerberusException {
        String result = "";
        final String sql = "SELECT system from application app join testcase tec on tec.application=app.Application where tec.test= ? and tec.testcase= ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        result = resultSet.getString("app.system");
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return result;
    }

    @Override
    public AnswerList<TestCase> readTestCaseByStepsInLibrary(String test) {
        AnswerList<TestCase> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<TestCase> list = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcase tec ");
        query.append("LEFT OUTER JOIN application app ON app.application=tec.application ");
        query.append("INNER JOIN testcasestep  tcs ON tec.test = tcs.test and tec.testcase = tcs.testcase ");
        query.append("WHERE tec.test= ? and (tcs.inlibrary = 'Y' or tcs.inlibrary = 'y') ");
        query.append("GROUP BY tec.testcase order by tec.testcase ");
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<>();

                    while (resultSet.next()) {
                        list.add(loadFromResultSet(resultSet));
                    }

                    if (list.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(list, list.size());
                    } else if (list.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(list, list.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(list, list.size());
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
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        response.setDataList(list);
        response.setResultMessage(msg);
        return response;
    }

    @Override
    public AnswerItem<TestCase> readByKey(String test, String testCase) {
        AnswerItem<TestCase> ans = new AnswerItem<>();
        TestCase result = null;
        final String query = "SELECT * FROM `testcase` tec "
                + "LEFT OUTER JOIN application app "
                + "ON app.application=tec.application "
                + "WHERE tec.`test` = ? "
                + "AND tec.`testcase` = ? "
                + "AND " + UserSecurity.getSystemAllowForSQL("app.`system`");

        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            preStat.setString(1, test);
            preStat.setString(2, testCase);
            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
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
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String test, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        String columnNameOri = columnName;
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();

        if (columnName.equals("lab.labelsSTICKER") || columnName.equals("lab.labelsREQUIREMENT") || columnName.equals("lab.labelsBATTERY")) {
            columnName = "lab.label";
        }

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM testcase tec ");
        query.append(" LEFT OUTER JOIN testcaselabel tel on tec.test = tel.test AND tec.testcase = tel.testcase ");
        query.append(" LEFT OUTER JOIN label lab on tel.labelId = lab.id ");
        query.append(" LEFT OUTER JOIN application app on app.application = tec.application ");

        searchSQL.append("WHERE 1=1");
        switch (columnNameOri) {
            case "lab.labelsSTICKER":
                searchSQL.append(" AND lab.`type` = 'STICKER' ");
                break;
            case "lab.labelsREQUIREMENT":
                searchSQL.append(" AND lab.`type` = 'REQUIREMENT' ");
                break;
            case "lab.labelsBATTERY":
                searchSQL.append(" AND lab.`type` = 'BATTERY' ");
                break;
        }
        if (system != null && !system.isEmpty()) {
            searchSQL.append(" AND ");
            searchSQL.append(SqlUtil.generateInClause("app.`system`", system));
        }
        if (!StringUtil.isNullOrEmpty(test)) {
            searchSQL.append(" AND tec.`test` = ?");
        }

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (tec.`testcase` like ?");
            searchSQL.append(" or tec.`test` like ?");
            searchSQL.append(" or tec.`application` like ?");
            searchSQL.append(" or tec.`usrCreated` like ?");
            searchSQL.append(" or tec.`usrModif` like ?");
            searchSQL.append(" or tec.`isActive` like ?");
            searchSQL.append(" or tec.`status` like ?");
            searchSQL.append(" or tec.`type` like ?");
            searchSQL.append(" or tec.`priority` like ?");
            searchSQL.append(" or tec.`dateCreated` like ?");
            searchSQL.append(" or lab.`label` like ?");
            searchSQL.append(" or tec.`description` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                String toto = entry.getKey();
                if (entry.getKey().equals("lab.labelsSTICKER") || entry.getKey().equals("lab.labelsREQUIREMENT") || entry.getKey().equals("lab.labelsBATTERY")) {
                    toto = "lab.label";
                }

                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(toto, entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement();) {

            int i = 1;
            if (system != null && !system.isEmpty()) {
                for (String string : system) {
                    preStat.setString(i++, string);
                }
            }
            if (!StringUtil.isNullOrEmpty(test)) {
                preStat.setString(i++, test);
            }
            if (!Strings.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
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
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else if (distinctValues.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }

        } catch (Exception e) {
            LOG.warn("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            // We always set the result message
            answer.setResultMessage(msg);
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    @Override
    public Answer update(String keyTest, String keyTestCase, TestCase tc) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder("UPDATE testcase SET");

        query.append(" test = ?,");
        query.append(" testcase = ?,");
        query.append(" implementer = ?,");
        query.append(" Executor = ?,");
        query.append(" application = ?,");
        query.append(" isActiveQA = ?,");
        query.append(" isActiveUAT = ?,");
        query.append(" isActivePROD = ?,");
        query.append(" status = ?,");
        query.append(" description = ?,");
        query.append(" DetailedDescription = ?,");
        query.append(" isActive = ?,");
        query.append(" FromMajor = ?,");
        query.append(" FromMinor = ?,");
        query.append(" ToMajor = ?,");
        query.append(" ToMinor = ?,");
        query.append(" Bugs = ?,");
        query.append(" targetMajor = ?,");
        query.append(" targetMinor = ?,");
        query.append(" comment = ?,");
        query.append(" priority = ?,");
        query.append(" `type` = ?,");
        query.append(" `origine` = ?,");
        query.append(" `userAgent` = ?,");
        query.append(" `screenSize` = ?,");
        query.append(" UsrModif = ?,");
        query.append(" conditionOperator = ?,");
        query.append(" conditionVal1 = ?,");
        query.append(" conditionVal2 = ?,");
        query.append(" conditionVal3 = ?,");
        query.append(" version = ?,");
        query.append(" DateModif = CURRENT_TIMESTAMP");
        query.append(" WHERE test = ? AND testcase = ?;");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setString(i++, tc.getTest());
                preStat.setString(i++, tc.getTestCase());
                preStat.setString(i++, tc.getImplementer());
                preStat.setString(i++, tc.getExecutor());
                preStat.setString(i++, tc.getApplication());
                preStat.setBoolean(i++, tc.isActiveQA());
                preStat.setBoolean(i++, tc.isActiveUAT());
                preStat.setBoolean(i++, tc.isActivePROD());
                preStat.setString(i++, tc.getStatus());
                preStat.setString(i++, tc.getDescription());
                preStat.setString(i++, tc.getDetailedDescription());
                preStat.setBoolean(i++, tc.isActive());
                preStat.setString(i++, tc.getFromMajor());
                preStat.setString(i++, tc.getFromMinor());
                preStat.setString(i++, tc.getToMajor());
                preStat.setString(i++, tc.getToMinor());
                preStat.setString(i++, tc.getBugs().toString());
                preStat.setString(i++, tc.getTargetMajor());
                preStat.setString(i++, tc.getTargetMinor());
                preStat.setString(i++, tc.getComment());
                preStat.setString(i++, Integer.toString(tc.getPriority()));
                preStat.setString(i++, tc.getType());
                preStat.setString(i++, tc.getOrigine());
                preStat.setString(i++, tc.getUserAgent());
                preStat.setString(i++, tc.getScreenSize());
                preStat.setString(i++, tc.getUsrModif());
                preStat.setString(i++, tc.getConditionOperator());
                preStat.setString(i++, tc.getConditionVal1());
                preStat.setString(i++, tc.getConditionVal2());
                preStat.setString(i++, tc.getConditionVal3());
                preStat.setInt(i++, tc.getVersion());
                preStat.setString(i++, keyTest);
                preStat.setString(i++, keyTestCase);

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
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
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer create(TestCase testCase) {
        MessageEvent msg = null;

        final StringBuffer sql = new StringBuffer("INSERT INTO `testcase` ")
                .append(" ( `Test`, `TestCase`, `Application`, ")
                .append("`Description`, `DetailedDescription`, ")
                .append("`Priority`, `Status`, `isActive`, ")
                .append("`Type`, `Origine`, `RefOrigine`, `Comment`, ")
                .append("`FromMajor`, `FromMinor`, `ToMajor`, `ToMinor`, ")
                .append("`Bugs`, `TargetMajor`, `TargetMinor`, `UsrCreated`, ")
                .append("`Implementer`, `Executor`, `isActiveQA`, `isActiveUAT`, `isActivePROD`, `useragent`, `screenSize`, ")
                .append("`conditionOperator`, `conditionVal1`, `conditionVal2`, `conditionVal3`, `version`) ")
                .append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?); ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                int i = 1;
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getApplication(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getDetailedDescription(), ""));
                preStat.setString(i++, Integer.toString(testCase.getPriority()));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setBoolean(i++, testCase.isActive());
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getType(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getOrigine(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getRefOrigine(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToMinor(), ""));
                if (testCase.getBugs() != null) {
                    preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getBugs().toString(), ""));
                } else {
                    preStat.setString(i++, "[]");
                }
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetMajor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetMinor(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUsrCreated(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getExecutor(), ""));
                preStat.setBoolean(i++, testCase.isActiveQA());
                preStat.setBoolean(i++, testCase.isActiveUAT());
                preStat.setBoolean(i++, testCase.isActivePROD());
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUserAgent(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getScreenSize(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionOperator(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal1(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal2(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getConditionVal3(), ""));
                preStat.setInt(i++, ParameterParserUtil.parseIntegerParam(testCase.getVersion(), 0));

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);

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
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(TestCase testCase) {
        MessageEvent msg = null;
        final String query = "DELETE FROM testcase WHERE test = ? AND testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testCase.getTest());
                preStat.setString(2, testCase.getTestCase());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
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
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public TestCase loadFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("tec.Test");
        String testCase = resultSet.getString("tec.TestCase");
        String tcapplication = resultSet.getString("tec.Application");
        String description = resultSet.getString("tec.Description");
        String detailedDescription = resultSet.getString("tec.DetailedDescription");
        int priority = resultSet.getInt("tec.Priority");
        int version = resultSet.getInt("tec.version");
        String status = resultSet.getString("tec.Status");
        boolean isActive = resultSet.getBoolean("tec.isActive");
        String conditionOperator = resultSet.getString("tec.conditionOperator");
        String conditionVal1 = resultSet.getString("tec.ConditionVal1");
        String conditionVal2 = resultSet.getString("tec.ConditionVal2");
        String conditionVal3 = resultSet.getString("tec.ConditionVal3");
        String type = resultSet.getString("tec.Type");
        String origin = resultSet.getString("tec.Origine");
        String refOrigin = resultSet.getString("tec.RefOrigine");
        String comment = resultSet.getString("tec.Comment");
        String fromMajor = resultSet.getString("tec.FromMajor");
        String fromMinor = resultSet.getString("tec.FromMinor");
        String toMajor = resultSet.getString("tec.ToMajor");
        String toMinor = resultSet.getString("tec.ToMinor");
        String bugsString = resultSet.getString("tec.Bugs");
        JSONArray bugs = new JSONArray();
        try {
            if (bugsString != null) {
                bugs = new JSONArray(bugsString);
            } else {
                bugs = new JSONArray();
            }
        } catch (JSONException ex) {
            LOG.error("Could not convert '" + bugsString + "' to JSONArray.", ex);
        }
        String targetMajor = resultSet.getString("tec.TargetMajor");
        String targetMinor = resultSet.getString("tec.TargetMinor");
        String implementer = resultSet.getString("tec.Implementer");
        String executor = resultSet.getString("tec.Executor");
        boolean isActiveQA = resultSet.getBoolean("tec.isActiveQA");
        boolean isActiveUAT = resultSet.getBoolean("tec.isActiveUAT");
        boolean isActivePROD = resultSet.getBoolean("tec.isActivePROD");
        String usrCreated = resultSet.getString("tec.UsrCreated");
        String dateCreated = resultSet.getString("tec.DateCreated");
        String usrModif = resultSet.getString("tec.UsrModif");
        Timestamp dateModif = resultSet.getTimestamp("tec.DateModif");
        String userAgent = resultSet.getString("tec.useragent");
        String screenSize = resultSet.getString("tec.screensize");
        String system = null;
        try {
            system = resultSet.getString("app.system");
        } catch (SQLException e) {
            LOG.debug("Column system does not Exist.");
        }

        TestCase newTestCase = factoryTestCase.create(test, testCase, origin, refOrigin, usrCreated, implementer, executor,
                usrModif, tcapplication, isActiveQA, isActiveUAT, isActivePROD, priority, type,
                status, description, detailedDescription, isActive, conditionOperator, conditionVal1, conditionVal2, conditionVal3, fromMajor, fromMinor, toMajor,
                toMinor, status, bugs, targetMajor, targetMinor, comment, dateCreated, userAgent, screenSize, dateModif, version);
        newTestCase.setSystem(system);
        return newTestCase;
    }

    private String createInClauseFromList(String[] list, String column, String preString, String postString) {
        StringBuilder query = new StringBuilder();

        if (list != null) {
            query.append(preString);
            query.append(column);
            query.append(" IN (");
            int i = 0;
            while (i < list.length - 1) {
                query.append("'");
                query.append(list[i]);
                query.append("',");
                i++;
            }
            query.append("'");
            query.append(list[i]);
            query.append("')");
            query.append(postString);
        }
        return query.toString();
    }

}
