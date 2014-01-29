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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.IZZTestDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.ZZTestData;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryZZTestData;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 */
@Repository
public class ZZTestDataDAO implements IZZTestDataDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryZZTestData factoryZZTestData;

    @Override
    public void createZZTestData(ZZTestData testData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO zztestdata (`key`, `value`) ");
        query.append("VALUES (?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testData.getKey());
                preStat.setString(2, testData.getValue());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateZZTestData(ZZTestData testData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update zztestdata set `value`=? where `key`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testData.getValue());
                preStat.setString(2, testData.getKey());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteZZTestData(ZZTestData testData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("delete from zztestdata where `key`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testData.getKey());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<ZZTestData> findAllZZTestData() {
        List<ZZTestData> list = null;
        final String query = "SELECT * FROM zztestdata";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<ZZTestData>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadZZTestDataFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<ZZTestData> findZZTestDataListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        List<ZZTestData> testDataList = new ArrayList<ZZTestData>();
        StringBuilder gSearch = new StringBuilder();
        String searchSQL = "";

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM zztestdata ");

        gSearch.append(" where (`key` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `value` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !individualSearch.equals("")) {
            searchSQL = gSearch.toString() + " and " + individualSearch;
        } else if (!individualSearch.equals("")) {
            searchSQL = " where `" + individualSearch + "`";
        } else if (!searchTerm.equals("")) {
            searchSQL = gSearch.toString();
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

        ZZTestData testData;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        testDataList.add(this.loadZZTestDataFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ZZTestDataDAO.class.getName(), Level.ERROR, e.toString());
            }
        }

        return testDataList;
    }

    private ZZTestData loadZZTestDataFromResultSet(ResultSet resultSet) throws SQLException {
        String key = resultSet.getString("key");
        String value = resultSet.getString("value");

        return factoryZZTestData.create(key, value);
    }
}
