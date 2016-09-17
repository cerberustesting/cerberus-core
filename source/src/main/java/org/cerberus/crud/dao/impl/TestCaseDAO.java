/*
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.cerberus.crud.factory.IFactoryTestCase;

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
    
    private static final Logger LOG = Logger.getLogger(TestCaseDAO.class);

    private final String OBJECT_NAME = "TestCase";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    /**
     * Get summary information of all test cases of one group.
     * <p/>
     * Used to display list of test cases on drop-down list
     *
     * @param test Name of test group.
     * @return List with a list of 3 strings (name of test case, type of
     * application, description of test case).
     */
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
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public AnswerList readByTestByCriteria(String system, String test, int start, int amount, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCase> testCaseList = new ArrayList<TestCase>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();

        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcase tec ");
        query.append(" LEFT OUTER JOIN testcaselabel tel on tec.test = tel.test AND tec.testcase = tel.testcase ");
        query.append(" LEFT OUTER JOIN label lab on tel.labelId = lab.id ");
        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" LEFT OUTER JOIN application app on app.application = tec.application ");
        }

        searchSQL.append("WHERE 1=1");

        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" AND app.`system` = ? ");
        }
        if (!StringUtil.isNullOrEmpty(test)) {
            searchSQL.append(" AND tec.`test` = ?");
        }

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (tec.`testcase` like ?");
            searchSQL.append(" or tec.`test` like ?");
            searchSQL.append(" or tec.`application` like ?");
            searchSQL.append(" or tec.`project` like ?");
            searchSQL.append(" or tec.`usrCreated` like ?");
            searchSQL.append(" or tec.`usrModif` like ?");
            searchSQL.append(" or tec.`tcactive` like ?");
            searchSQL.append(" or tec.`status` like ?");
            searchSQL.append(" or tec.`group` like ?");
            searchSQL.append(" or tec.`priority` like ?");
            searchSQL.append(" or tec.`dateCreated` like ?");
            searchSQL.append(" or tec.`description` like ?");
            searchSQL.append(" or lab.`label` like ?)");
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
                if (!StringUtil.isNullOrEmpty(system)) {
                    preStat.setString(i++, system);
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
                        answer = new AnswerList(testCaseList, nrTotalRows);
                    } else if (testCaseList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        answer = new AnswerList(testCaseList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        answer = new AnswerList(testCaseList, nrTotalRows);
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

    /**
     * Get test case information.
     *
     * @param test Name of test group.
     * @param testCase Name of test case.
     * @return TestCase object or null.
     * @throws org.cerberus.exception.CerberusException
     * @see org.cerberus.crud.entity.TestCase
     */
    @Override
    public TestCase findTestCaseByKey(String test, String testCase) throws CerberusException {
        boolean throwExcep = false;
        TestCase result = null;
        final String query = "SELECT * FROM testcase tec WHERE test = ? AND testcase = ?";

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
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public boolean updateTestCaseInformation(TestCase testCase) {
        boolean res = false;
        final String sql = "UPDATE testcase tc SET tc.Application = ?, tc.Project = ?, tc.BehaviorOrValueExpected = ?, tc.activeQA = ?, tc.activeUAT = ?, tc.activePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.TcActive = ?, tc.Description = ?, tc.Group = ?, tc.HowTo = ?, tc.Comment = ?, tc.Ticket = ?, tc.FromBuild = ?, "
                + "tc.FromRev = ?, tc.ToBuild = ?, tc.ToRev = ?, tc.BugID = ?, tc.TargetBuild = ?, tc.Implementer = ?, tc.LastModifier = ?, tc.TargetRev = ?, tc.`function` = ? "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, testCase.getApplication());
                preStat.setString(2, testCase.getProject());
                preStat.setString(3, testCase.getBehaviorOrValueExpected());
                preStat.setString(4, testCase.getActiveQA());
                preStat.setString(5, testCase.getActiveUAT());
                preStat.setString(6, testCase.getActivePROD());
                preStat.setString(7, Integer.toString(testCase.getPriority()));
                preStat.setString(8, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(9, testCase.getTcActive());
                preStat.setString(10, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(11, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(12, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(13, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(14, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(15, ParameterParserUtil.parseStringParam(testCase.getFromBuild(), ""));
                preStat.setString(16, ParameterParserUtil.parseStringParam(testCase.getFromRev(), ""));
                preStat.setString(17, ParameterParserUtil.parseStringParam(testCase.getToBuild(), ""));
                preStat.setString(18, ParameterParserUtil.parseStringParam(testCase.getToRev(), ""));
                preStat.setString(19, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(20, ParameterParserUtil.parseStringParam(testCase.getTargetBuild(), ""));
                preStat.setString(21, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(22, ParameterParserUtil.parseStringParam(testCase.getUsrModif(), ""));
                preStat.setString(23, ParameterParserUtil.parseStringParam(testCase.getTargetRev(), ""));
                preStat.setString(24, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(25, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(26, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
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
        for (TestCaseCountry tcCountry : tc.getTestCaseCountry()){
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
                                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                            } finally {
                                preStat2.close();
                            }
                        }
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    rsCount.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
                        MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    } finally {
                        preStat2.close();
                    }
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean createTestCase(TestCase testCase) {
        boolean res = false;

        final StringBuffer sql = new StringBuffer("INSERT INTO `testcase` ")
                .append(" ( `Test`, `TestCase`, `Application`, `Project`, `Ticket`, ")
                .append("`Description`, `BehaviorOrValueExpected`, ")
                .append("`Priority`, `Status`, `TcActive`, ")
                .append("`Group`, `Origine`, `RefOrigine`, `HowTo`, `Comment`, ")
                .append("`FromBuild`, `FromRev`, `ToBuild`, `ToRev`, ")
                .append("`BugID`, `TargetBuild`, `TargetRev`, `UsrCreated`, ")
                .append("`Implementer`, `UsrModif`, `function`, `activeQA`, `activeUAT`, `activePROD`) ")
                .append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ? ); ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                int i=1;
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getApplication(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getProject(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getBehaviorOrValueExpected(), ""));
                preStat.setString(i++, Integer.toString(testCase.getPriority()));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(i++, testCase.getTcActive() != null && !testCase.getTcActive().equals("Y") ? "N" : "Y");
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getOrigine(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getRefOrigine(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromBuild(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromRev(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToBuild(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToRev(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetBuild(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetRev(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUsrCreated(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUsrModif(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(i++, testCase.getActiveQA() != null && !testCase.getActiveQA().equals("Y") ? "N" : "Y");
                preStat.setString(i++, testCase.getActiveUAT() != null && !testCase.getActiveUAT().equals("Y") ? "N" : "Y");
                preStat.setString(i++, testCase.getActivePROD() != null && !testCase.getActivePROD().equals("N") ? "Y" : "N");

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<TestCase> findTestCaseByCriteria(String test, String application, String country, String active) {
        List<TestCase> list = null;
        final String query = "SELECT tec.* FROM testcase tec JOIN testcasecountry tcc "
                + "WHERE tec.test=tcc.test AND tec.testcase=tcc.testcase "
                + "AND tec.test = ? AND tec.application = ? AND tcc.country = ? AND tec.tcactive = ? ";

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
                preStat.setString(4, active);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TestCase> findTestCaseByCampaignName(String campaign) {
        List<TestCase> list = null;
        final String query = new StringBuilder("select tec.* ")
                .append("from testcase tec ")
                .append("inner join testbatterycontent tbc ")
                .append("on tbc.Test = tec.Test ")
                .append("and tbc.TestCase = tec.TestCase ")
                .append("inner join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ")
                .append("where cc.campaign = ? ")
                .toString();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, campaign);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    /**
     * @since 0.9.1
     */
    @Override
    public List<TestCase> findTestCaseByCriteria(TestCase testCase, String text, String system) {
        List<TestCase> list = null;
        String query = new StringBuilder()
                .append("SELECT tec.* FROM testcase tec LEFT OUTER JOIN application a ON a.application=tec.application ")
                .append(" WHERE (tec.test LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.test", testCase.getTest()))
                .append(") AND (tec.project LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.project", testCase.getProject()))
                .append(") AND (tec.ticket LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.ticket", testCase.getTicket()))
                .append(") AND (tec.bugid LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.bugid", testCase.getBugID()))
                .append(") AND (tec.origine LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.origine", testCase.getOrigine()))
                .append(") AND (a.system LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("a.system", system))
                .append(") AND (tec.application LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.application", testCase.getApplication()))
                .append(") AND (tec.priority LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfMinusOne("tec.priority", testCase.getPriority()))
                .append(") AND (tec.status LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.status", testCase.getStatus()))
                .append(") AND (tec.group LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.group", testCase.getGroup()))
                .append(") AND (tec.activePROD LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.activePROD", testCase.getActivePROD()))
                .append(") AND (tec.activeUAT LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.activeUAT", testCase.getActiveUAT()))
                .append(") AND (tec.activeQA LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.activeQA", testCase.getActiveQA()))
                .append(") AND (tec.description LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.description", text))
                .append(" OR tec.howto LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.howto", text))
                .append(" OR tec.behaviororvalueexpected LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.behaviororvalueexpected", text))
                .append(" OR tec.comment LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.comment", text))
                .append(") AND (tec.TcActive LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.TcActive", testCase.getTcActive()))
                .append(") AND (tec.frombuild LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.frombuild", testCase.getFromBuild()))
                .append(") AND (tec.fromrev LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.fromrev", testCase.getFromRev()))
                .append(") AND (tec.tobuild LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.tobuild", testCase.getToBuild()))
                .append(") AND (tec.torev LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.torev", testCase.getToRev()))
                .append(") AND (tec.targetbuild LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.targetbuild", testCase.getTargetBuild()))
                .append(") AND (tec.targetrev LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.targetrev", testCase.getTargetRev()))
                .append(") AND (tec.testcase LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.testcase", testCase.getTestCase()))
                .append(") AND (tec.function LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.function", testCase.getFunction()))
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
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    private String createInClauseFromList(String[] list, String column) {
        StringBuilder query = new StringBuilder();

        if (list != null) {
            query.append("AND ");
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
        }
        return query.toString();
    }

    @Override
    public AnswerList readByVariousCriteria(String[] test, String[] idProject, String[] app, String[] creator, String[] implementer, String[] system,
            String[] testBattery, String[] campaign, String[] priority, String[] group, String[] status) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCase> testCaseList = new ArrayList<TestCase>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM testcase tec ");
        query.append("LEFT JOIN application app ON tec.application = app.application ");
        query.append("LEFT JOIN testbatterycontent tb ON tec.test = tb.test AND tec.testcase = tb.testcase ");
        query.append("LEFT JOIN campaigncontent cc ON tb.testbattery = cc.testbattery ");
        query.append("WHERE 1=1 AND tec.tcactive = 'Y' ");
        query.append(createInClauseFromList(test, "tec.test"));
        query.append(createInClauseFromList(idProject, "tec.project"));
        query.append(createInClauseFromList(app, "tec.application"));
        query.append(createInClauseFromList(creator, "tec.usrCreated"));
        query.append(createInClauseFromList(implementer, "tec.implementer"));
        query.append(createInClauseFromList(system, "app.system"));
        query.append(createInClauseFromList(testBattery, "tb.testbattery"));
        query.append(createInClauseFromList(campaign, "cc.campaign"));
        query.append(createInClauseFromList(priority, "tec.priority"));
        query.append(createInClauseFromList(group, "tec.group"));
        query.append(createInClauseFromList(status, "tec.status"));
        query.append("GROUP BY tec.test, tec.testcase ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

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
                        answer = new AnswerList(testCaseList, testCaseList.size());
                    } else if (testCaseList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        answer = new AnswerList(testCaseList, testCaseList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        answer = new AnswerList(testCaseList, testCaseList.size());
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

    /**
     * @since 0.9.1
     */
    private TestCase loadFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("tec.Test");
        String testCase = resultSet.getString("tec.TestCase");
        String tcapplication = resultSet.getString("tec.Application");
        String project = resultSet.getString("tec.Project");
        String ticket = resultSet.getString("tec.Ticket");
        String description = resultSet.getString("tec.Description");
        String behavior = resultSet.getString("tec.BehaviorOrValueExpected");
        int priority = resultSet.getInt("tec.Priority");
        String status = resultSet.getString("tec.Status");
        String tcactive = resultSet.getString("tec.TcActive");
        String group = resultSet.getString("tec.Group");
        String origin = resultSet.getString("tec.Origine");
        String refOrigin = resultSet.getString("tec.RefOrigine");
        String howTo = resultSet.getString("tec.HowTo");
        String comment = resultSet.getString("tec.Comment");
        String fromSprint = resultSet.getString("tec.FromBuild");
        String fromRevision = resultSet.getString("tec.FromRev");
        String toSprint = resultSet.getString("tec.ToBuild");
        String toRevision = resultSet.getString("tec.ToRev");
        String bugID = resultSet.getString("tec.BugID");
        String targetSprint = resultSet.getString("tec.TargetBuild");
        String targetRevision = resultSet.getString("tec.TargetRev");
        String implementer = resultSet.getString("tec.Implementer");
        String runQA = resultSet.getString("tec.activeQA");
        String runUAT = resultSet.getString("tec.activeUAT");
        String runPROD = resultSet.getString("tec.activePROD");
        String function = resultSet.getString("tec.function");
        String usrCreated = resultSet.getString("tec.UsrCreated");
        String dateCreated = resultSet.getString("tec.DateCreated");
        String usrModif = resultSet.getString("tec.UsrModif");
        Timestamp dateModif = resultSet.getTimestamp("tec.DateModif");
        String userAgent = resultSet.getString("tec.useragent");

        return factoryTestCase.create(test, testCase, origin, refOrigin, usrCreated, implementer,
                usrModif, project, ticket, function, tcapplication, runQA, runUAT, runPROD, priority, group,
                status, description, behavior, howTo, tcactive, fromSprint, fromRevision, toSprint,
                toRevision, status, bugID, targetSprint, targetRevision, comment, dateCreated, userAgent, dateModif);
    }

    @Override
    public List<String> findUniqueDataOfColumn(String column) {
        List<String> list = null;
        final String query = "SELECT DISTINCT tec." + column + " FROM testcase tec ORDER BY tec." + column + " ASC";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<String>();
                try {
                    while (resultSet.next()) {
                        list.add(resultSet.getString(1));

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class
                            .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();

                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class
                    .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.WARN, e.toString());
            }
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
                MyLogger.log(UserDAO.class
                        .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class
                    .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    @Override
    public void updateTestCaseField(TestCase tc, String columnName, String value) {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update testcase set `");
        query.append(columnName);
        query.append("`=? where `test`=? and `testcase`=? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, value);
                preStat.setString(2, tc.getTest());
                preStat.setString(3, tc.getTestCase());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class
                    .getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }

    }

    /**
     * @param testCase
     * @param system
     * @return
     * @since 1.0.2
     */
    @Override
    public List<TestCase> findTestCaseByGroupInCriteria(TestCase testCase, String system) {
        List<TestCase> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tec.* FROM testcase tec LEFT OUTER JOIN application a ON a.application=tec.application WHERE 1=1");
        if (!StringUtil.isNull(testCase.getTest())) {
            query.append(" AND tec.test IN (");
            query.append(testCase.getTest());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getProject())) {
            query.append(" AND tec.project IN (");
            query.append(testCase.getProject());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTicket())) {
            query.append(" AND tec.ticket IN (");
            query.append(testCase.getTicket());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getBugID())) {
            query.append(" AND tec.bugid IN (");
            query.append(testCase.getBugID());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getOrigine())) {
            query.append(" AND tec.origine IN (");
            query.append(testCase.getOrigine());
            query.append(") ");
        }
        if (!StringUtil.isNull(system)) {
            query.append(" AND a.system IN (");
            query.append(system);
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getApplication())) {
            query.append(" AND tec.application IN (");
            query.append(testCase.getApplication());
            query.append(") ");
        }
        if (testCase.getPriority() != -1) {
            query.append(" AND tec.priority IN (");
            query.append(testCase.getPriority());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getStatus())) {
            query.append(" AND tec.status IN (");
            query.append(testCase.getStatus());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getGroup())) {
            query.append(" AND tec.group IN (");
            query.append(testCase.getGroup());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getActivePROD())) {
            query.append(" AND tec.activePROD IN (");
            query.append(testCase.getActivePROD());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getActiveUAT())) {
            query.append(" AND tec.activeUAT IN (");
            query.append(testCase.getActiveUAT());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getActiveQA())) {
            query.append(" AND tec.activeQA IN (");
            query.append(testCase.getActiveQA());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getDescription())) {
            query.append(" AND tec.description LIKE '%");
            query.append(testCase.getDescription());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getHowTo())) {
            query.append(" AND tec.howto LIKE '%");
            query.append(testCase.getHowTo());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getBehaviorOrValueExpected())) {
            query.append(" AND tec.behaviororvalueexpected LIKE '%");
            query.append(testCase.getBehaviorOrValueExpected());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getComment())) {
            query.append(" AND tec.comment LIKE '%");
            query.append(testCase.getComment());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getTcActive())) {
            query.append(" AND tec.TcActive IN (");
            query.append(testCase.getTcActive());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getFromBuild())) {
            query.append(" AND tec.frombuild IN (");
            query.append(testCase.getFromBuild());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getFromRev())) {
            query.append(" AND tec.fromrev IN (");
            query.append(testCase.getFromRev());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getToBuild())) {
            query.append(" AND tec.tobuild IN (");
            query.append(testCase.getToBuild());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getToRev())) {
            query.append(" AND tec.torev IN (");
            query.append(testCase.getToRev());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTargetBuild())) {
            query.append(" AND tec.targetbuild IN (");
            query.append(testCase.getTargetBuild());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTargetRev())) {
            query.append(" AND tec.targetrev IN (");
            query.append(testCase.getTargetRev());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTestCase())) {
            query.append(" AND tec.testcase IN (");
            query.append(testCase.getTestCase());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getFunction())) {
            query.append(" AND tec.function IN (");
            query.append(testCase.getFunction());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getUsrCreated())) {
            query.append(" AND tec.UsrCreated IN (");
            query.append(testCase.getUsrCreated());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getUsrModif())) {
            query.append(" AND tec.UsrModif IN (");
            query.append(testCase.getUsrModif());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getUserAgent())) {
            query.append(" AND tec.useragent IN (");
            query.append(testCase.getUserAgent());
            query.append(") ");
        }
        query.append(" ORDER BY tec.test, tec.testcase");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public void updateTestCase(TestCase testCase) throws CerberusException {
        final String sql = "UPDATE testcase tc SET tc.Application = ?, tc.Project = ?, tc.BehaviorOrValueExpected = ?, tc.activeQA = ?, tc.activeUAT = ?, tc.activePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.TcActive = ?, tc.Description = ?, tc.Group = ?, tc.HowTo = ?, tc.Comment = ?, tc.Ticket = ?, tc.FromBuild = ?, "
                + "tc.FromRev = ?, tc.ToBuild = ?, tc.ToRev = ?, tc.BugID = ?, tc.TargetBuild = ?, tc.Implementer = ?, tc.UsrModif = ?, tc.TargetRev = ?, tc.`function` = ?, dateModif = CURRENT_TIMESTAMP "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, testCase.getApplication());
                preStat.setString(2, testCase.getProject());
                preStat.setString(3, testCase.getBehaviorOrValueExpected());
                preStat.setString(4, testCase.getActiveQA().equals("Y") ? "Y" : "N");
                preStat.setString(5, testCase.getActiveUAT().equals("Y") ? "Y" : "N");
                preStat.setString(6, testCase.getActivePROD().equals("Y") ? "Y" : "N");
                preStat.setString(7, Integer.toString(testCase.getPriority()));
                preStat.setString(8, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(9, testCase.getTcActive().equals("Y") ? "Y" : "N");
                preStat.setString(10, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(11, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(12, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(13, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(14, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(15, ParameterParserUtil.parseStringParam(testCase.getFromBuild(), ""));
                preStat.setString(16, ParameterParserUtil.parseStringParam(testCase.getFromRev(), ""));
                preStat.setString(17, ParameterParserUtil.parseStringParam(testCase.getToBuild(), ""));
                preStat.setString(18, ParameterParserUtil.parseStringParam(testCase.getToRev(), ""));
                preStat.setString(19, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(20, ParameterParserUtil.parseStringParam(testCase.getTargetBuild(), ""));
                preStat.setString(21, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(22, ParameterParserUtil.parseStringParam(testCase.getUsrModif(), ""));
                preStat.setString(23, ParameterParserUtil.parseStringParam(testCase.getTargetRev(), ""));
                preStat.setString(24, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(25, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(26, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public List<TestCase> findTestCaseByTestSystems(String test, List<String> systems) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMaxNumberTestCase(String test) {
        String max = "";
        final String sql = "SELECT  convert ( Max( Testcase ) + 0, UNSIGNED) as MAXTC FROM testcase where test = ?";

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
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return max;
    }

    @Override
    public List<TestCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries) {
        List<TestCase> list = null;
        final StringBuilder query = new StringBuilder("select tec.* ")
                .append("from testcase tec ")
                .append("inner join testcasecountry tcc ")
                .append("on tcc.Test = tec.Test ")
                .append("and tcc.TestCase = tec.TestCase ")
                .append("inner join testbatterycontent tbc ")
                .append("on tbc.Test = tec.Test ")
                .append("and tbc.TestCase = tec.TestCase ")
                .append("inner join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ")
                .append("where cc.campaign = ? ");

        query.append(" and tcc.Country in (");
        for (int i = 0; i < countries.length; i++) {
            query.append("?");
            if (i < countries.length - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int index = 1;
                preStat.setString(index, campaign);
                index++;

                for (String c : countries) {
                    preStat.setString(index, c);
                    index++;
                }

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
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
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TestCase> findTestCaseByCriteria(String testClause, String projectClause, String appClause, String activeClause, String priorityClause, String statusClause, String groupClause, String targetBuildClause, String targetRevClause, String creatorClause, String implementerClause, String functionClause, String campaignClause, String batteryClause) {
        List<TestCase> list = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM testcase tec join application app on tec.application=app.application ")
                .append("left join testbatterycontent tbc ")
                .append("on tbc.Test = tec.Test ")
                .append("and tbc.TestCase = tec.TestCase ")
                .append("left join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ");
        sb.append(" WHERE 1=1 ");
        sb.append(testClause);
        sb.append(projectClause);
        sb.append(appClause);
        sb.append(activeClause);
        sb.append(priorityClause);
        sb.append(statusClause);
        sb.append(groupClause);
        sb.append(targetBuildClause);
        sb.append(targetRevClause);
        sb.append(creatorClause);
        sb.append(implementerClause);
        sb.append(functionClause);
        sb.append(campaignClause);
        sb.append(batteryClause);
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
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public String findSystemOfTestCase(String test, String testcase) throws CerberusException {
        String result = "";
        final String sql = "SELECT system from application a join testcase tec on tec.application=a.Application where tec.test= ? and tec.testcase= ?";

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
                        result = resultSet.getString("system");
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public AnswerList readTestCaseByStepsInLibrary(String test) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<TestCase> list = new ArrayList<TestCase>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcase tec  ");
        query.append("inner join testcasestep  tcs on tec.test = tcs.test and tec.testcase = tcs.testcase ");
        query.append("WHERE tec.test= ? and (tcs.inlibrary = 'Y' or tcs.inlibrary = 'y') ");
        query.append("group by tec.testcase order by tec.testcase ");
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
                    list = new ArrayList<TestCase>();

                    while (resultSet.next()) {
                        list.add(loadFromResultSet(resultSet));
                    }

                    if (list.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(list, list.size());
                    } else if (list.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(list, list.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(list, list.size());
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        response.setDataList(list);
        response.setResultMessage(msg);
        return response;
    }

    @Override
    public AnswerItem readByKey(String test, String testCase) {
        AnswerItem ans = new AnswerItem();
        TestCase result = null;
        final String query = "SELECT * FROM `testcase` tec WHERE tec.`test` = ? AND tec.`testcase` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

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
                } finally {
                    resultSet.close();
                }
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

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String test, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM testcase tec ");
        query.append(" LEFT OUTER JOIN testcaselabel tel on tec.test = tel.test AND tec.testcase = tel.testcase ");
        query.append(" LEFT OUTER JOIN label lab on tel.labelId = lab.id ");

        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" LEFT OUTER JOIN application app on app.application = tec.application ");
        }

        searchSQL.append("WHERE 1=1");

        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" AND app.`system` = ? ");
        }
        if (!StringUtil.isNullOrEmpty(test)) {
            searchSQL.append(" AND tec.`test` = ?");
        }

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (tec.`testcase` like ?");
            searchSQL.append(" or tec.`test` like ?");
            searchSQL.append(" or tec.`application` like ?");
            searchSQL.append(" or tec.`project` like ?");
            searchSQL.append(" or tec.`usrCreated` like ?");
            searchSQL.append(" or tec.`usrModif` like ?");
            searchSQL.append(" or tec.`tcactive` like ?");
            searchSQL.append(" or tec.`status` like ?");
            searchSQL.append(" or tec.`group` like ?");
            searchSQL.append(" or tec.`priority` like ?");
            searchSQL.append(" or tec.`dateCreated` like ?");
            searchSQL.append(" or lab.`label` like ?");
            searchSQL.append(" or tec.`description` like ?)");
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
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            if (!StringUtil.isNullOrEmpty(system)) {
                preStat.setString(i++, system);
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

            //gets the data
            while (resultSet.next()) {
                distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
            }

            //get the total number of rows
            resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
            int nrTotalRows = 0;

            if (resultSet != null && resultSet.next()) {
                nrTotalRows = resultSet.getInt(1);
            }

            if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                LOG.error("Partial Result in the query.");
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                answer = new AnswerList(distinctValues, nrTotalRows);
            } else if (distinctValues.size() <= 0) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                answer = new AnswerList(distinctValues, nrTotalRows);
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                answer = new AnswerList(distinctValues, nrTotalRows);
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
    public Answer update(TestCase tc) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder("UPDATE testcase SET");

        query.append(" implementer = ?,");
        query.append(" project = ?,");
        query.append(" ticket = ?,");
        query.append(" application = ?,");
        query.append(" activeQA = ?,");
        query.append(" activeUAT = ?,");
        query.append(" activeProd = ?,");
        query.append(" status = ?,");
        query.append(" description = ?,");
        query.append(" behaviorOrValueExpected = ?,");
        query.append(" howTo = ?,");
        query.append(" tcactive = ?,");
        query.append(" fromBuild = ?,");
        query.append(" fromRev = ?,");
        query.append(" toBuild = ?,");
        query.append(" toRev = ?,");
        query.append(" bugId = ?,");
        query.append(" targetBuild = ?,");
        query.append(" targetRev = ?,");
        query.append(" comment = ?,");
        query.append(" function = ?,");
        query.append(" priority = ?,");
        query.append(" `group` = ?,");
        query.append(" `origine` = ?,");
        query.append(" `userAgent` = ?,");
        query.append(" UsrModif = ?,");
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
                preStat.setString(i++, tc.getImplementer());
                preStat.setString(i++, tc.getProject());
                preStat.setString(i++, tc.getTicket());
                preStat.setString(i++, tc.getApplication());
                preStat.setString(i++, tc.getActiveQA());
                preStat.setString(i++, tc.getActiveUAT());
                preStat.setString(i++, tc.getActivePROD());
                preStat.setString(i++, tc.getStatus());
                preStat.setString(i++, tc.getDescription());
                preStat.setString(i++, tc.getBehaviorOrValueExpected());
                preStat.setString(i++, tc.getHowTo());
                preStat.setString(i++, tc.getTcActive());
                preStat.setString(i++, tc.getFromBuild());
                preStat.setString(i++, tc.getFromRev());
                preStat.setString(i++, tc.getToBuild());
                preStat.setString(i++, tc.getToRev());
                preStat.setString(i++, tc.getBugID());
                preStat.setString(i++, tc.getTargetBuild());
                preStat.setString(i++, tc.getTargetRev());
                preStat.setString(i++, tc.getComment());
                preStat.setString(i++, tc.getFunction());
                preStat.setString(i++, Integer.toString(tc.getPriority()));
                preStat.setString(i++, tc.getGroup());
                preStat.setString(i++, tc.getOrigine());
                preStat.setString(i++, tc.getUserAgent());
                preStat.setString(i++, tc.getUsrModif());
                preStat.setString(i++, tc.getTest());
                preStat.setString(i++, tc.getTestCase());

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
                .append(" ( `Test`, `TestCase`, `Application`, `Project`, `Ticket`, ")
                .append("`Description`, `BehaviorOrValueExpected`, ")
                .append("`Priority`, `Status`, `TcActive`, ")
                .append("`Group`, `Origine`, `RefOrigine`, `HowTo`, `Comment`, ")
                .append("`FromBuild`, `FromRev`, `ToBuild`, `ToRev`, ")
                .append("`BugID`, `TargetBuild`, `TargetRev`, `UsrCreated`, ")
                .append("`Implementer`, `UsrModif`, `function`, `activeQA`, `activeUAT`, `activePROD`) ")
                .append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ? ); ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                int i=1;
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getApplication(), ""));
                preStat.setString(i++, testCase.getProject());
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getBehaviorOrValueExpected(), ""));
                preStat.setString(i++, Integer.toString(testCase.getPriority()));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(i++, testCase.getTcActive() != null && !testCase.getTcActive().equals("Y") ? "N" : "Y");
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getOrigine(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getRefOrigine(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromBuild(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFromRev(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToBuild(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getToRev(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetBuild(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getTargetRev(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUsrCreated(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getUsrModif(), ""));
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(i++, testCase.getActiveQA() != null && !testCase.getActiveQA().equals("Y") ? "N" : "Y");
                preStat.setString(i++, testCase.getActiveUAT() != null && !testCase.getActiveUAT().equals("Y") ? "N" : "Y");
                preStat.setString(i++, testCase.getActivePROD() != null && !testCase.getActivePROD().equals("N") ? "Y" : "N");

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
}
