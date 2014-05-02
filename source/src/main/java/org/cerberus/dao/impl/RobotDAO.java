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
import org.cerberus.dao.IRobotDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Robot;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryRobot;
import org.cerberus.factory.impl.FactoryRobot;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 * @since 0.9.2
 */
@Repository
public class RobotDAO implements IRobotDAO{

    /**
     * Bean of the DatabaseSpring, Spring automatically links. Establishes
     * connection to database and return it to allow perform queries and
     * updates.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    /**
     * Bean of the IFactoryRobot, Spring automatically links. Creates new
     * objects {@link Robot}
     */
    @Autowired
    private IFactoryRobot factoryRobot;
    
    /**
     * Finds the Robot by the name. </p> Access to database to return the
     * {@link Robot} given by the unique name.<br/> If no Robot
     * found with the given name, returns CerberusException with
     * {@link MessageGeneralEnum#NO_DATA_FOUND}.<br/> If an SQLException occur,
     * returns null in the Robot object and writes the error on the logs.
     *
     * @param id id of the Robot to find
     * @return object Robot if exist
     * @throws CerberusException when Robot does not exist
     * @since 0.9.2
     */
    @Override
    public Robot findRobotByKey(Integer id) throws CerberusException {
       Robot result = null;
        final String query = "SELECT * FROM robot a WHERE a.id = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, id);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadRobotFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;
    }

    /**
     * Finds all Robot that exists. </p> Access to database to return all
     * existing {@link Robot}.<br/> If no Robot found, returns a
     * empty {@literal List<Robot>}.<br/> If an SQLException occur,
     * returns null in the list object and writes the error on the logs.
     *
     * @return list of Robot
     * @throws CerberusException
     * @since 0.9.2
     */
    @Override
    public List<Robot> findAllRobot() throws CerberusException {
        List<Robot> list = null;
        final String query = "SELECT * FROM robot a";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<Robot>();
                    while (resultSet.next()) {
                        Robot robot = this.loadRobotFromResultSet(resultSet);
                        list.add(robot);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(RobotDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    /**
     * Updates the information based on the object Robot. </p> Access to
     * database to update Robot information given by the object
     * Robot.
     * <br/> If an SQLException occur, throw CerberusException.
     *
     * @param robot object Robot to update
     * @throws CerberusException
     * @since 0.9.2
     */
    @Override
    public void updateRobot(Robot robot) throws CerberusException {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE robot SET name= ? , ip = ? , port = ? ,");
        query.append("platform = ?, os = ?, browser = ? , version = ?, description = ? WHERE id = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, robot.getName());
                preStat.setString(2, robot.getIp());
                preStat.setInt(3, robot.getPort());
                preStat.setString(4, robot.getPlatform());
                preStat.setString(5, robot.getOs());
                preStat.setString(6, robot.getBrowser());
                preStat.setString(7, robot.getVersion());
                preStat.setString(8, robot.getDescription());
                preStat.setInt(9, robot.getId());
                
                preStat.executeUpdate();
                } catch (SQLException exception) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(RobotDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        
    }

    @Override
    public void createRobot(Robot robot) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO robot (`name`, `ip`, `port`, `platform`, `os`, `browser`, `version`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, robot.getName());
                preStat.setString(2, robot.getIp());
                preStat.setInt(3, robot.getPort());
                preStat.setString(4, robot.getPlatform());
                preStat.setString(5, robot.getOs());
                preStat.setString(6, robot.getBrowser());
                preStat.setString(7, robot.getVersion());
                preStat.setString(8, robot.getDescription());
                
                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(RobotDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteRobot(Robot robot) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM robot WHERE id = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, robot.getId());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(RobotDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }
    
    /**
     * Uses data of ResultSet to create object {@link Robot}
     *
     * @param rs ResultSet relative to select from table Robot
     * @return object {@link Robot}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryRobot
     */
    private Robot loadRobotFromResultSet(ResultSet rs) throws SQLException {
        Integer id = ParameterParserUtil.parseIntegerParam(rs.getString("id"), 0);
        String name = ParameterParserUtil.parseStringParam(rs.getString("name"), "");
        String ip = ParameterParserUtil.parseStringParam(rs.getString("ip"), "");
        Integer port = ParameterParserUtil.parseIntegerParam(rs.getString("port"), 0);
        String platform = ParameterParserUtil.parseStringParam(rs.getString("platform"), "");
        String os = ParameterParserUtil.parseStringParam(rs.getString("os"), "");
        String browser = ParameterParserUtil.parseStringParam(rs.getString("browser"), "");
        String version = ParameterParserUtil.parseStringParam(rs.getString("version"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("description"), "");

        //TODO remove when working in test with mockito and autowired
        factoryRobot = new FactoryRobot();
        return factoryRobot.create(id, name, ip, port, platform, os, browser, version, description);
    }

    @Override
    public List<Robot> findRobotListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        List<Robot> robotList = new ArrayList<Robot>();
        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM robot ");

        gSearch.append(" where (`platform` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `ip` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `port` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `name` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `os` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `browser` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `version` like '%");
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
                        robotList.add(this.loadRobotFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, e.toString());
            }
        }

        return robotList;
    }

    @Override
    public Integer getNumberOfRobotPerCriteria(String searchTerm, String inds) {
        Integer result = 0;
        StringBuilder query = new StringBuilder();
        StringBuilder gSearch = new StringBuilder();
        String searchSQL = "";

        query.append("SELECT count(*) FROM robot");

        gSearch.append(" where (`platform` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `ip` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `port` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `name` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `os` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `browser` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `version` like '%");
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
                    MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;
    }

    @Override
    public Robot findRobotByName(String name) throws CerberusException {
        Robot result = null;
        final String query = "SELECT * FROM robot a WHERE a.name = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, name);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadRobotFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(RobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(RobotDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;
    }
    
}
