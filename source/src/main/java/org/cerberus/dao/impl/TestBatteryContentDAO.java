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
package org.cerberus.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.cerberus.entity.TestBatteryContent;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestBatteryContentDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestBatteryContent;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author memiks
 */
@Repository
public class TestBatteryContentDAO implements ITestBatteryContentDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestBatteryContent factoryTestBatteryContent;

    @Override
    public List<TestBatteryContent> findAll() throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbatterycontent t";

        List<TestBatteryContent> testBatteryContentsList = new ArrayList<TestBatteryContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteryContentsList.add(this.loadTestBatteryContentFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteryContentsList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteryContentsList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteryContentsList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryContentsList;
    }

    @Override
    public TestBatteryContent findTestBatteryContentByKey(Integer testBatteryContentID) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbatterycontent t WHERE t.testbatterycontentID = ?";

        TestBatteryContent testBatteryContent = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testBatteryContentID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        testBatteryContent = this.loadTestBatteryContentFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryContent;
    }

    @Override
    public List<TestBatteryContent> findTestBatteryContentsByTestBatteryName(String testBattery) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbatterycontent t where t.testbattery = ?";

        List<TestBatteryContent> testBatteryContentsList = new ArrayList<TestBatteryContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testBattery);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteryContentsList.add(this.loadTestBatteryContentFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteryContentsList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteryContentsList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteryContentsList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryContentsList;
    }

    @Override
    public boolean updateTestBatteryContent(TestBatteryContent testBatteryContent) {
        final StringBuffer query = new StringBuffer("UPDATE `testbatterycontent` set `testbattery` = ?, `Test` = ?, `TestCase` = ? WHERE `testbatterycontentID` = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, testBatteryContent.getTestbattery());
            preStat.setString(2, testBatteryContent.getTest());
            preStat.setString(3, testBatteryContent.getTestCase());
            preStat.setInt(4, testBatteryContent.getTestbatterycontentID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public List<TestBatteryContent> findTestBatteryContentsByCriteria(Integer testBatteryContentID, String testBattery, String test, String testCase) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("SELECT * FROM testbatterycontent t WHERE ");

        if (testBatteryContentID != null) {
            query.append(" t.testBatterycontentID = ?");
        }
        if (testBattery != null && !"".equals(testBattery.trim())) {
            query.append(" t.testBattery LIKE ?");
        }
        if (test != null && !"".equals(test.trim())) {
            query.append(" t.Test LIKE ?");
        }
        if (testCase != null && !"".equals(testCase.trim())) {
            query.append(" t.TestCase LIKE ?");
        }

        List<TestBatteryContent> testBatteryContentsList = new ArrayList<TestBatteryContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int index = 1;
            if (testBatteryContentID != null) {
                preStat.setInt(index, testBatteryContentID);
                index++;
            }
            if (testBattery != null && !"".equals(testBattery.trim())) {
                preStat.setString(index, "%" + testBattery.trim() + "%");
                index++;
            }
            if (test != null && !"".equals(test.trim())) {
                preStat.setString(index, "%" + test.trim() + "%");
                index++;
            }
            if (testCase != null && !"".equals(testCase.trim())) {
                preStat.setString(index, "%" + testCase.trim() + "%");
                index++;
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteryContentsList.add(this.loadTestBatteryContentFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteryContentsList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteryContentsList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteryContentsList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryContentsList;
    }

    @Override
    public boolean createTestBatteryContent(TestBatteryContent testBatteryContent) {
        final StringBuffer query = new StringBuffer("INSERT INTO `testbatterycontent` (`testbattery`, `Test`, `TestCase`) VALUES (?, ?, ?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, testBatteryContent.getTestbattery());
            preStat.setString(2, testBatteryContent.getTest());
            preStat.setString(3, testBatteryContent.getTestCase());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    private TestBatteryContent loadTestBatteryContentFromResultSet(ResultSet rs) throws SQLException {
        Integer testbatterycontentID = ParameterParserUtil.parseIntegerParam(rs.getString("testbatterycontentID"), -1);
        String testbattery = ParameterParserUtil.parseStringParam(rs.getString("testbattery"), "");
        String test = ParameterParserUtil.parseStringParam(rs.getString("Test"), "");
        String testCase = ParameterParserUtil.parseStringParam(rs.getString("TestCase"), "");

        return factoryTestBatteryContent.create(testbatterycontentID, test, testCase, testbattery);
    }

}
