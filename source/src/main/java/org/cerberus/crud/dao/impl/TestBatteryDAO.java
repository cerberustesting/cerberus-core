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
package org.cerberus.crud.dao.impl;

import com.google.common.base.Strings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestBatteryDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestBattery;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestBattery;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author memiks
 */
@Repository
public class TestBatteryDAO implements ITestBatteryDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestBattery factoryTestBattery;
    
    private static final Logger LOG = Logger.getLogger(TestBatteryDAO.class);

    private final String OBJECT_NAME = "TestBattery";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;


    @Override
    public List<TestBattery> findAll() throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbattery t order by testbattery asc, description asc";

        List<TestBattery> testBatteryList = new ArrayList<TestBattery>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteryList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteryList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteryList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteryList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryList;
    }

    @Override
    public TestBattery findTestBatteryByKey(Integer testBatteryID) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbattery t WHERE t.testbatteryID = ?";

        TestBattery testBattery = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testBatteryID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        testBattery = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBattery;
    }

    @Override
    public TestBattery findTestBatteryByTestBatteryName(String testBattery) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbattery t WHERE t.testbattery = ?";

        TestBattery testBatteryResult = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, testBattery);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        testBatteryResult = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryResult;
    }

    @Override
    public List<TestBattery> findTestBatteriesByDescription(String description) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbattery t WHERE t.description = ?";

        List<TestBattery> testBatteryList = new ArrayList<TestBattery>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, description);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteryList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteryList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteryList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteryList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryList;
    }

    @Override
    public List<TestBattery> findTestBatteryByCriteria(Integer testBatteryID, String testBattery, String Description) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("SELECT * FROM testbattery t WHERE 1=1");

        if (testBatteryID != null) {
            query.append(" AND t.testBatteryID = ?");
        }
        if (testBattery != null && !"".equals(testBattery.trim())) {
            query.append(" AND t.testBattery LIKE ?");
        }
        if (Description != null && !"".equals(Description.trim())) {
            query.append(" AND t.Description LIKE ?");
        }

        List<TestBattery> testBatteriesList = new ArrayList<TestBattery>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int index = 1;
            if (testBatteryID != null) {
                preStat.setInt(index, testBatteryID);
                index++;
            }
            if (testBattery != null && !"".equals(testBattery.trim())) {
                preStat.setString(index, "%" + testBattery.trim() + "%");
                index++;
            }
            if (Description != null && !"".equals(Description.trim())) {
                preStat.setString(index, "%" + Description.trim() + "%");
                index++;
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteriesList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteriesList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteriesList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteriesList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteriesList;
    }

    @Override
    public boolean updateTestBattery(TestBattery testBattery) {
        final StringBuffer query = new StringBuffer("UPDATE `testbattery` set `testbattery` = ?, `Description` = ? WHERE `testbatteryID` = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, testBattery.getTestbattery());
            preStat.setString(2, testBattery.getDescription());
            preStat.setInt(3, testBattery.getTestbatteryID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public boolean createTestBattery(TestBattery testBattery) {
        final StringBuffer query = new StringBuffer("INSERT INTO `testbattery` (`testbattery`, `Description`) VALUES (?, ?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, testBattery.getTestbattery());
            preStat.setString(2, testBattery.getDescription());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    private TestBattery loadFromResultSet(ResultSet rs) throws SQLException {
        Integer testbatteryID = ParameterParserUtil.parseIntegerParam(rs.getString("testbatteryID"), -1);
        String testbattery = ParameterParserUtil.parseStringParam(rs.getString("testbattery"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("description"), "");

        return factoryTestBattery.create(testbatteryID, testbattery, description);
    }

    @Override
    public boolean deleteTestBattery(TestBattery testBattery) {
        final StringBuffer query = new StringBuffer("DELETE FROM `testbattery` WHERE testbatteryID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setInt(1, testBattery.getTestbatteryID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public List<TestBattery> findTestBatteriesByTestCase(String test, String testCase) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT tb.* FROM testbattery tb inner join testbatterycontent tbc on tb.testbattery = tbc.testbattery where tbc.test = ? and tbc.testcase = ?";

        List<TestBattery> testBatteriesList = new ArrayList<TestBattery>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteriesList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteriesList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteriesList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteriesList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteriesList;
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestBattery> testBatteryList = new ArrayList<TestBattery>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testbattery ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`testbatteryid` like ?");
            searchSQL.append(" or `testbattery` like ?");
            searchSQL.append(" or `description` like ?)");
        }
        if (!StringUtil.isNullOrEmpty(individualSearch)) {
            searchSQL.append(" and ( ? )");
        }
        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(colName)) {
            query.append("order by `").append(colName).append("` ").append(dir);
        }
        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (!Strings.isNullOrEmpty(searchTerm)) {
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                }
                if (!StringUtil.isNullOrEmpty(individualSearch)) {
                    preStat.setString(i++, individualSearch);
                }
                
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testBatteryList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (testBatteryList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(testBatteryList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(testBatteryList, nrTotalRows);
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
