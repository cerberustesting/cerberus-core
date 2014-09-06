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
import org.cerberus.dao.ITestDataLibDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestDataLib;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestDataLib;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 */
@Repository
public class TestDataLibDAO implements ITestDataLibDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestDataLib factoryTestDataLib;

    @Override
    public void createTestDataLib(TestDataLib testDataLib) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalib (`name`, `system`, `environment`, `country`, `group`, `type`, `database`, `script`, `servicePath`, `method`, `envelope`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testDataLib.getName());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getSystem()));
                preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getEnvironment()));
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCountry()));
                preStat.setString(5, testDataLib.getGroup());
                preStat.setString(6, testDataLib.getType());
                preStat.setString(7, testDataLib.getDatabase());
                preStat.setString(8, testDataLib.getScript());
                preStat.setString(9, testDataLib.getServicePath());
                preStat.setString(10, testDataLib.getMethod());
                preStat.setString(11, testDataLib.getEnvelope());
                preStat.setString(12, testDataLib.getDescription());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateTestDataLib(TestDataLib testDataLib) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update testdatalib set `group`= ?, `type`= ? , `database`= ? , `script`= ? , `servicepath`= ? , `method`= ? , `envelope`= ? , `description`= ? where `name`= ? and `system`=? and `environment`=? and `country`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testDataLib.getGroup());
                preStat.setString(2, testDataLib.getType());
                preStat.setString(3, testDataLib.getDatabase());
                preStat.setString(4, testDataLib.getScript());
                preStat.setString(5, testDataLib.getServicePath());
                preStat.setString(6, testDataLib.getMethod());
                preStat.setString(7, testDataLib.getEnvelope());
                preStat.setString(8, testDataLib.getDescription());
                preStat.setString(9, testDataLib.getName());
                preStat.setString(10, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getSystem()));
                preStat.setString(11, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getEnvironment()));
                preStat.setString(12, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCountry()));

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteTestDataLib(TestDataLib testDataLib) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("delete from testdatalib where `name`=? and `system`=? and `environment`=? and `country`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testDataLib.getName());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getSystem()));
                preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getEnvironment()));
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCountry()));

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<TestDataLib> findAllTestDataLib() {
        List<TestDataLib> list = null;
        final String query = "SELECT * FROM testdatalib";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestDataLib>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestDataLibFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestDataLib> findTestDataLibListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        List<TestDataLib> testDataLibList = new ArrayList<TestDataLib>();
        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testdatalib ");

        gSearch.append(" where (`name` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `group` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `type` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `database` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `script` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `servicepath` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `method` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `envelope` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `system` like '%");
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
                        testDataLibList.add(this.loadTestDataLibFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, e.toString());
            }
        }

        return testDataLibList;
    }

    private TestDataLib loadTestDataLibFromResultSet(ResultSet resultSet) throws SQLException {
        Integer testDataLibID = resultSet.getInt("testDataLibID");
        String name = resultSet.getString("name");
        String system = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("system"));
        String environment = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("environment"));
        String country = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("country"));
        String group = resultSet.getString("group");
        String type = resultSet.getString("type");
        String database = resultSet.getString("database");
        String script = resultSet.getString("script");
        String servicePath = resultSet.getString("servicePath");
        String method = resultSet.getString("method");
        String envelope = resultSet.getString("envelope");
        String description = resultSet.getString("description");

        return factoryTestDataLib.create(testDataLibID, name, system, environment, country, group, type, database, script, servicePath, method, envelope, description);
    }

    @Override
    public TestDataLib findTestDataLibByKey(String name, String system, String environment, String country) throws CerberusException {
        TestDataLib result = null;
        final String query = new StringBuilder("SELECT * FROM testdatalib where `name`=?")
                .append(" and (`system` = ? or `system` = '')")
                .append(" and (`environment` = ? or `environment` = '')")
                .append(" and (`country` = ? or `country` = '')")
                .append(" order by `name` DESC, system DESC, environment DESC, country DESC")
                .append(" limit 1").toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, system);
            preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(system));
            preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(environment));
            preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(country));
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadTestDataLibFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public Integer getNumberOfTestDataLibPerCriteria(String searchTerm, String inds) {
        Integer result = 0;
        StringBuilder query = new StringBuilder();
        StringBuilder gSearch = new StringBuilder();
        String searchSQL = "";

        query.append("SELECT count(*) FROM testdatalib");

        gSearch.append(" where (`name` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `group` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `type` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `database` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `script` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `servicepath` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `method` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `envelope` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `system` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `environment` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `country` like '%");
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
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;

    }
}
