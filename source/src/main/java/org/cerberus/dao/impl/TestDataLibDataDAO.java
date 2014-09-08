/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
import org.cerberus.dao.ITestDataLibDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestDataLib;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestDataLibData;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 */
@Repository
public class TestDataLibDataDAO implements ITestDataLibDataDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestDataLibData factoryTestDataLibData;

    @Override
    public void createTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalibdata (`TestDataLibID`, `subData`, `value`, `column`, `parsinganswer`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLibData.getTestDataLibID());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getSubData()));
                preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getValue()));
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getColumn()));
                preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getParsingAnswer()));
                preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getDescription()));

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update testdatalibdata set `value`= ?, `column`= ? , `parsinganswer`= ? , `description`= ? where `testdatalibID`= ? and `subdata`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testDataLibData.getValue());
                preStat.setString(2, testDataLibData.getColumn());
                preStat.setString(3, testDataLibData.getParsingAnswer());
                preStat.setString(4, testDataLibData.getDescription());
                preStat.setInt(5, testDataLibData.getTestDataLibID());
                preStat.setString(6, testDataLibData.getSubData());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("delete from testdatalibdata where `testdatalibID`=? and `subdata`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLibData.getTestDataLibID());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getSubData()));

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public TestDataLibData findTestDataLibDataByKey(Integer testDataLibID, String subData) throws CerberusException {
        TestDataLibData result = null;
        final String query = new StringBuilder("SELECT * FROM testdatalibdata where `testdatalibID`=?")
                .append(" and `subData` = ? ").toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testDataLibID);
            preStat.setString(2, subData);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadTestDataLibDataFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public List<TestDataLibData> findAllTestDataLibData() {
        List<TestDataLibData> list = null;
        final String query = "SELECT * FROM testdatalibdata";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestDataLibData>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestDataLibDataFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestDataLibData> findTestDataLibDataListByTestDataLib(Integer testDataLibID) {
        List<TestDataLibData> testDataLibListData = new ArrayList<TestDataLibData>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testdatalibdata where `testDataLibID` = ? ;");


        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setInt(1, testDataLibID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        testDataLibListData.add(this.loadTestDataLibDataFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, e.toString());
            }
        }

        return testDataLibListData;
    }

    
    @Override
    public List<TestDataLibData> findTestDataLibDataByCriteria(Integer testDataLibID, String subData, String value, String column, String parsingAnswer, String description) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("SELECT * FROM testdatalibdata c WHERE 1=1 ");

        if (testDataLibID != null) {
            query.append(" AND c.testDataLibID = ?");
        }
        if (subData != null && !"".equals(subData.trim())) {
            query.append(" AND c.subData LIKE ?");
        }
        if (value != null && !"".equals(value.trim())) {
            query.append(" AND c.value LIKE ?");
        }
        if (column != null && !"".equals(column.trim())) {
            query.append(" AND c.column LIKE ?");
        }
        if (parsingAnswer != null && !"".equals(parsingAnswer.trim())) {
            query.append(" AND c.parsingAnswer LIKE ?");
        }
        if (description != null && !"".equals(description.trim())) {
            query.append(" AND c.description LIKE ?");
        }

        // " c.campaignID = ? AND c.campaign LIKE ? AND c.description LIKE ?";
        List<TestDataLibData> testDataLibData = new ArrayList<TestDataLibData>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int index = 1;
            if (testDataLibID != null) {
                preStat.setInt(index, testDataLibID);
                index++;
            }
            if (subData != null && !"".equals(subData.trim())) {
                preStat.setString(index, "%" + subData.trim() + "%");
                index++;
            }
            if (value != null && !"".equals(value.trim())) {
                preStat.setString(index, "%" + value.trim() + "%");
                index++;
            }
            if (column != null && !"".equals(column.trim())) {
                preStat.setString(index, "%" + column.trim() + "%");
                index++;
            }
            if (parsingAnswer != null && !"".equals(parsingAnswer.trim())) {
                preStat.setString(index, "%" + parsingAnswer.trim() + "%");
                index++;
            }
            if (description != null && !"".equals(description.trim())) {
                preStat.setString(index, "%" + description.trim() + "%");
                index++;
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testDataLibData.add(this.loadTestDataLibDataFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testDataLibData = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testDataLibData = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testDataLibData = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testDataLibData;
    }

    
    private TestDataLibData loadTestDataLibDataFromResultSet(ResultSet resultSet) throws SQLException {
        Integer testDataLibID = resultSet.getInt("testDataLibID");
        String subData = resultSet.getString("subdata");
        String value = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("system"));
        String column = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("environment"));
        String parsingAnswer = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("country"));
        String description = resultSet.getString("group");

        return factoryTestDataLibData.create(testDataLibID, subData, value, column, parsingAnswer, description);
    }

    
}
