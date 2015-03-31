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
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestBatteryDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestBattery;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestBattery;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
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
                        testBatteryList.add(this.loadTestBatteryFromResultSet(resultSet));
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
                        testBattery = this.loadTestBatteryFromResultSet(resultSet);
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
                        testBatteryResult = this.loadTestBatteryFromResultSet(resultSet);
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
                        testBatteryList.add(this.loadTestBatteryFromResultSet(resultSet));
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
                        testBatteriesList.add(this.loadTestBatteryFromResultSet(resultSet));
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

    private TestBattery loadTestBatteryFromResultSet(ResultSet rs) throws SQLException {
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
                        testBatteriesList.add(this.loadTestBatteryFromResultSet(resultSet));
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
}
