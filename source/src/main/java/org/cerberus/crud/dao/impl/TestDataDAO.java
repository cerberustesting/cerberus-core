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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestData;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestData;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 */
@Repository
public class TestDataDAO implements ITestDataDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestData factoryTestData;

    private static final Logger LOG = Logger.getLogger(TestDataDAO.class);
    
    @Override
    public void createTestData(TestData testData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdata (`key`, `value`, `description`, `application`, `environment`, `country`) ");
        query.append("VALUES (?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testData.getKey());
                preStat.setString(2, testData.getValue());
                preStat.setString(3, testData.getDescription());
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testData.getApplication()));
                preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(testData.getEnvironment()));
                preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(testData.getCountry()));

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateTestData(TestData testData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update testdata set `value`= ?, `description`= ? where `key`= ? and `application`=? and `environment`=? and `country`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testData.getValue());
                preStat.setString(2, testData.getDescription());
                preStat.setString(3, testData.getKey());
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testData.getApplication()));
                preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(testData.getEnvironment()));
                preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(testData.getCountry()));

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteTestData(TestData testData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("delete from testdata where `key`=? and `application`=? and `environment`=? and `country`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testData.getKey());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testData.getApplication()));
                preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(testData.getEnvironment()));
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testData.getCountry()));

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<TestData> findAllTestData() {
        List<TestData> list = null;
        final String query = "SELECT * FROM testdata";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestData>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestDataFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestData> findTestDataListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        List<TestData> testDataList = new ArrayList<TestData>();
        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testdata ");

        gSearch.append(" where (`key` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `value` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `application` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `environment` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `country` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !individualSearch.equals("")) {
            searchSQL.append(gSearch.toString());
            searchSQL.append(" and ");
            searchSQL.append(individualSearch);
        } else if (!individualSearch.equals("")) {
            searchSQL.append(" where `");
            searchSQL.append(individualSearch);
            searchSQL.append("`");
        } else if (!searchTerm.equals("")) {
            searchSQL.append(gSearch.toString());
        }

        query.append(searchSQL);
        query.append("order by `");
        query.append(column);
        query.append("` ");
        query.append(dir);
        query.append(" limit ");
        query.append(start);
        query.append(" , ");
        query.append(amount);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        testDataList.add(this.loadTestDataFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, e.toString());
            }
        }

        return testDataList;
    }

    private TestData loadTestDataFromResultSet(ResultSet resultSet) throws SQLException {
        String key = resultSet.getString("key");
        String value = resultSet.getString("value");
        String description = resultSet.getString("description");
        String application = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("application"));
        String environment = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("environment"));
        String country = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("country"));

        return factoryTestData.create(key, value, description, application, environment, country);
    }

    @Override
    public TestData findTestDataByKey(String key, String application, String environment, String country) throws CerberusException {
        TestData result = null;
        final String query = new StringBuilder("SELECT * FROM testdata where `key`=?")
                .append(" and (`application` = ? or `application` = '')")
                .append(" and (`environment` = ? or `environment` = '')")
                .append(" and (`country` = ? or `country` = '')")
                .append(" order by `key` DESC, application DESC, environment DESC, country DESC")
                .append(" limit 1").toString();

        LOG.debug("SQL : " + query);
        LOG.debug("SQL application : " + application);
        LOG.debug("SQL environment : " + environment);
        LOG.debug("SQL country : " + country);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, key);
            preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(application));
            preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(environment));
            preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(country));
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadTestDataFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public Integer getNumberOfTestDataPerCriteria(String searchTerm, String inds) {
        Integer result = 0;
        StringBuilder query = new StringBuilder();
        StringBuilder gSearch = new StringBuilder();
        String searchSQL = "";

        query.append("SELECT count(*) FROM testdata");

        gSearch.append(" where (`key` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `value` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `application` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `environment` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `country` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !inds.equals("")) {
            searchSQL = gSearch.toString() + " and " + inds;
        } else if (!inds.equals("")) {
            searchSQL = " where " + inds;
        } else if (!searchTerm.equals("")) {
            searchSQL = gSearch.toString();
        }

        query.append(searchSQL);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    if (resultSet.first()) {
                        result = resultSet.getInt(1);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;

    }
}
