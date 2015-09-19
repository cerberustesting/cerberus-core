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
import org.cerberus.crud.dao.ICountryEnvironmentDatabaseDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryCountryEnvironmentDatabase;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
@Repository
public class CountryEnvironmentDatabaseDAO implements ICountryEnvironmentDatabaseDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCountryEnvironmentDatabase factoryCountryEnvironmentDatabase;
    private static final Logger LOG = Logger.getLogger(CountryEnvironmentDatabaseDAO.class);

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @param variable Description text text text.
     * @return Description text text text.
     */
    @Override
    public CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String system, String country, String environment, String database) throws CerberusException {
        CountryEnvironmentDatabase result = null;
        final String query = "SELECT * FROM countryenvironmentdatabase ced WHERE ced.database = ? AND ced.environment = ? AND ced.country = ? AND ced.system = ?";
        boolean throwEx = false;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, database);
                preStat.setString(2, environment);
                preStat.setString(3, country);
                preStat.setString(4, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        String connectionPoolName = resultSet.getString("ConnectionPoolName");
                        result = factoryCountryEnvironmentDatabase.create(database, environment, country, connectionPoolName);
                    } else {
                        throwEx = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CountryEnvironmentDatabaseDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CountryEnvironmentDatabaseDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CountryEnvironmentDatabaseDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CountryEnvironmentDatabaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
        return result;
    }
    
    private CountryEnvironmentDatabase loadFromResultSet(ResultSet resultSet) throws SQLException {
        String system = resultSet.getString("System");
        String count = resultSet.getString("Country");
        String env = resultSet.getString("Environment");
        String database = resultSet.getString("Database");
        String connectionpoolname = resultSet.getString("ConnectionPoolName");
        return factoryCountryEnvironmentDatabase.create(system, count, env, database, connectionpoolname);
    }
          

    @Override
    public List<CountryEnvironmentDatabase> findAll(String system) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM countryenvironmentdatabase c where `system`=?";

        List<CountryEnvironmentDatabase> result = new ArrayList<CountryEnvironmentDatabase>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        result.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    throwEx = true;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            throwEx = true;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing the connection :"+ e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public void update(CountryEnvironmentDatabase ced) throws CerberusException {
        final StringBuffer query = new StringBuffer("UPDATE `countryenvironmentdatabase` SET `connectionpoolname`=?, ");
        query.append(" where `system`=? and `country`=? and `environment`=? and `database`=?");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, ced.getConnectionPoolName());
            preStat.setString(2, ced.getSystem());
            preStat.setString(3, ced.getCountry());
            preStat.setString(4, ced.getEnvironment());
            preStat.setString(5, ced.getDatabase());

            try {
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
                LOG.warn("Exception closing the connection :"+ e.toString());
            }
        }
    }

    @Override
    public void delete(CountryEnvironmentDatabase ced) throws CerberusException {
        final StringBuffer query = new StringBuffer("DELETE FROM `countryenvironmentdatabase` WHERE `system`=? and `country`=? and `environment`=? and `database`=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, ced.getSystem());
            preStat.setString(2, ced.getCountry());
            preStat.setString(3, ced.getEnvironment());
            preStat.setString(4, ced.getDatabase());

            try {
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
                LOG.warn("Exception closing the connection :"+ e.toString());
            }
        }
    }

    @Override
    public void create(CountryEnvironmentDatabase ced) throws CerberusException {
        final StringBuffer query = new StringBuffer("INSERT INTO `countryenvironmentdatabase` ");
        query.append("(`system`, `country`, `environment`, `database`, `connectionpoolname`) VALUES ");
        query.append("(?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
                preStat.setString(1, ced.getSystem());
                preStat.setString(2, ced.getCountry());
                preStat.setString(3, ced.getEnvironment());
                preStat.setString(4, ced.getDatabase());
                preStat.setString(5, ced.getConnectionPoolName());
                
            try {
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
                LOG.warn("Exception closing the connection :"+ e.toString());
            }
        }
    }

    @Override
    public List<CountryEnvironmentDatabase> findListByCriteria(String system, String country, String environment) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM countryenvironmentdatabase c where `system` = ? and `country` = ? and `environment` = ?";

        List<CountryEnvironmentDatabase> result = new ArrayList<CountryEnvironmentDatabase>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                preStat.setString(2, country);
                preStat.setString(3, environment);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        result.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    throwEx = true;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            throwEx = true;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing the connection :"+ e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }
}
