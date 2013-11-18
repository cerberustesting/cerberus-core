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

import org.cerberus.dao.IApplicationDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Application;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryApplication;
import org.cerberus.factory.impl.FactoryApplication;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@Repository
public class ApplicationDAO implements IApplicationDAO {

    /**
     * Bean of the DatabaseSpring, Spring automatically links.
     * Establishes connection to database and return it to allow
     * perform queries and updates.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    /**
     * Bean of the IFactoryApplication, Spring automatically links.
     * Creates new objects {@link Application}
     */
    @Autowired
    private IFactoryApplication factoryApplication;

    /**
     * Finds the Application by the name.
     * </p>
     * Access to database to return the {@link Application} given by the
     * unique name.<br/>
     * If no application found with the given name, returns CerberusException
     * with {@link MessageGeneralEnum#NO_DATA_FOUND}.<br/>
     * If an SQLException occur, returns null in the application object and
     * writes the error on the logs.
     *
     * @param application name of the Application to find
     * @return object application if exist
     * @throws CerberusException when Application does not exist
     * @since 0.9.0
     */
    @Override
    public Application findApplicationByKey(String application) throws CerberusException {
        Application result = null;
        final String query = "SELECT * FROM application a WHERE a.application = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, application);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadApplicationFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;
    }

    /**
     * Finds all Applications that exists.
     * </p>
     * Access to database to return all existing {@link Application}.<br/>
     * If no application found, returns a empty {@literal List<Application>}.<br/>
     * If an SQLException occur, returns null in the list object and
     * writes the error on the logs.
     *
     * @return list of applications
     * @throws CerberusException
     * @since 0.9.0
     */
    @Override
    public List<Application> findAllApplication() throws CerberusException {
        List<Application> list = null;
        final String query = "SELECT * FROM application a ORDER BY a.sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<Application>();
                    while (resultSet.next()) {
                        Application app = this.loadApplicationFromResultSet(resultSet);
                        list.add(app);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    /**
     * Finds Applications of the given system.
     * </p>
     * Access to database to return a list of {@link Application} filtering by system.<br/>
     * If no application found, returns a empty {@literal List<Application>}.<br/>
     * If an SQLException occur, returns null in the list object and
     * writes the error on the logs.
     *
     * @param system name of the System to filter
     * @return list of applications
     * @throws CerberusException
     * @since 0.9.0
     */
    @Override
    public List<Application> findApplicationBySystem(String system) throws CerberusException {
        List<Application> list = null;
        final String query = "SELECT * FROM application a WHERE `System` LIKE ? ORDER BY a.sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<Application>();
                    while (resultSet.next()) {
                        Application app = this.loadApplicationFromResultSet(resultSet);
                        list.add(app);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    /**
     * Updates the information based on the object application.
     * </p>
     * Access to database to update application information given by the object Application and
     * returns boolean of PreparedStatement.executeUpdate() > 0. <br/>
     * If an SQLException occur, returns false and writes the error on the logs.
     *
     * @param application object Application to update
     * @return true if updated successfully and false if no row updated or error
     * @throws CerberusException
     * @since 0.9.0
     */
    @Override
    public boolean updateApplication(Application application) throws CerberusException {
        boolean bool = false;
        final String query = "UPDATE application SET description = ?, internal = ?, sort = ?, `type` = ?, `system` = ?, SubSystem = ?, svnurl = ?, BugTrackerUrl = ?, BugTrackerNewUrl = ?, deploytype = ?, mavengroupid = ?  WHERE Application = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, application.getDescription());
                preStat.setString(2, application.getInternal());
                preStat.setInt(3, application.getSort());
                preStat.setString(4, application.getType());
                preStat.setString(5, application.getSystem());
                preStat.setString(6, application.getSubsystem());
                preStat.setString(7, application.getSvnurl());
                preStat.setString(8, application.getBugTrackerUrl());
                preStat.setString(9, application.getBugTrackerNewUrl());
                preStat.setString(10, application.getDeploytype());
                preStat.setString(11, application.getMavengroupid());
                preStat.setString(12, application.getApplication());

                int res = preStat.executeUpdate();
                bool = res > 0;
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    /**
     *
     * @return
     * @throws CerberusException
     * @since 0.9.1
     */
    @Override
    public List<String> findDistinctSystem() {
        List<String> list = null;
        final String query = "SELECT DISTINCT a.system FROM application a ORDER BY a.system ASC";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();
                    while (resultSet.next()) {
                        list.add(resultSet.getString("system"));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    /**
     * Uses data of ResultSet to create object {@link Application}
     *
     * @param rs ResultSet relative to select from table Application
     * @return object {@link Application}
     * @throws SQLException when trying to get value from {@link java.sql.ResultSet#getString(String)}
     * @see FactoryApplication
     */
    private Application loadApplicationFromResultSet(ResultSet rs) throws SQLException {
        String application = ParameterParserUtil.parseStringParam(rs.getString("application"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("description"), "");
        String internal = ParameterParserUtil.parseStringParam(rs.getString("internal"), "");
        int sort = ParameterParserUtil.parseIntegerParam(rs.getString("sort"), 0);
        String type = ParameterParserUtil.parseStringParam(rs.getString("type"), "");
        String system = ParameterParserUtil.parseStringParam(rs.getString("system"), "");
        String subsystem = ParameterParserUtil.parseStringParam(rs.getString("subsystem"), "");
        String svnUrl = ParameterParserUtil.parseStringParam(rs.getString("svnurl"), "");
        String deployType = ParameterParserUtil.parseStringParam(rs.getString("deploytype"), "");
        String mavenGroupId = ParameterParserUtil.parseStringParam(rs.getString("mavengroupid"), "");
        String bugTrackerUrl = ParameterParserUtil.parseStringParam(rs.getString("bugtrackerurl"), "");
        String bugTrackerNewUrl = ParameterParserUtil.parseStringParam(rs.getString("bugtrackernewurl"), "");

        //TODO remove when working in test with mockito and autowired
        factoryApplication = new FactoryApplication();
        return factoryApplication.create(application, description, internal, sort, type, system
                , subsystem, svnUrl, deployType, mavenGroupId, bugTrackerUrl, bugTrackerNewUrl);
    }
}
