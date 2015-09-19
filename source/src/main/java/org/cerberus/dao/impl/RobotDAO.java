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
import org.apache.log4j.Logger;
import org.cerberus.dao.IRobotDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.Robot;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryRobot;
import org.cerberus.factory.impl.FactoryRobot;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 * @since 0.9.2
 */
@Repository
public class RobotDAO implements IRobotDAO {

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

    private static final Logger LOG = Logger.getLogger(RobotDAO.class);

    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem readByKeyTech(Integer robotid) {
        AnswerItem ans = new AnswerItem();
        Robot result = null;
        final String query = "SELECT * FROM `robot` WHERE `robotID` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, robotid);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot").replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerItem readByKey(String robot) {
        AnswerItem ans = new AnswerItem();
        Robot result = null;
        final String query = "SELECT * FROM `robot` WHERE `robot` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, robot);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot").replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Robot> robotList = new ArrayList<Robot>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM robot ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`platform` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `host` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `port` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `description` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `robot` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `browser` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `useragent` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `version` like '%").append(searchTerm).append("%'");
        }
        if (!StringUtil.isNullOrEmpty(individualSearch)) {
            searchSQL.append(" and (`").append(individualSearch).append("`)");
        }
        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if (!(amount == 0)) {
            query.append(" limit ").append(start).append(" , ").append(amount);
        } else {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        robotList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot").replace("%OPERATION%", "SELECT"));
                    response = new AnswerList(robotList, nrTotalRows);

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
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
        response.setDataList(robotList);
        return response;
    }

    @Override
    public Answer create(Robot robot) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO robot (`robot`, `host`, `port`, `platform`,`browser`, `version`,`active` , `description`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, robot.getRobot());
                preStat.setString(2, robot.getHost());
                preStat.setString(3, robot.getPort());
                preStat.setString(4, robot.getPlatform());
                preStat.setString(5, robot.getBrowser());
                preStat.setString(6, robot.getVersion());
                preStat.setString(7, robot.getActive());
                preStat.setString(8, robot.getDescription());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot").replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_DUPLICATE_ERROR);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot").replace("%OPERATION%", "INSERT"));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(Robot application) {
        MessageEvent msg = null;
        final String query = "DELETE FROM robot WHERE robotID = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, application.getRobotID());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot").replace("%OPERATION%", "DELETE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(Robot robot) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE robot SET robot= ? , host = ? , port = ? ,");
        query.append("platform = ?, browser = ? , version = ?, active=?, description = ?, useragent = ? WHERE robotID = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, robot.getRobot());
                preStat.setString(2, robot.getHost());
                preStat.setString(3, robot.getPort());
                preStat.setString(4, robot.getPlatform());
                preStat.setString(5, robot.getBrowser());
                preStat.setString(6, robot.getVersion());
                preStat.setString(7, robot.getActive());
                preStat.setString(8, robot.getDescription());
                preStat.setString(9, robot.getUserAgent());
                preStat.setInt(10, robot.getRobotID());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot").replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
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
    @Override
    public Robot loadFromResultSet(ResultSet rs) throws SQLException {
        Integer robotID = ParameterParserUtil.parseIntegerParam(rs.getString("robotID"), 0);
        String robot = ParameterParserUtil.parseStringParam(rs.getString("robot"), "");
        String host = ParameterParserUtil.parseStringParam(rs.getString("host"), "");
        String port = ParameterParserUtil.parseStringParam(rs.getString("port"), "");
        String platform = ParameterParserUtil.parseStringParam(rs.getString("platform"), "");
        String browser = ParameterParserUtil.parseStringParam(rs.getString("browser"), "");
        String version = ParameterParserUtil.parseStringParam(rs.getString("version"), "");
        String active = ParameterParserUtil.parseStringParam(rs.getString("active"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("description"), "");
        String userAgent = ParameterParserUtil.parseStringParam(rs.getString("useragent"), "");
        //TODO remove when working in test with mockito and autowired
        factoryRobot = new FactoryRobot();
        return factoryRobot.create(robotID, robot, host, port, platform, browser, version, active, description, userAgent);
    }

    /**
     * Finds the Robot by the name. </p> Access to database to return the
     * {@link Robot} given by the unique name.<br/> If no Robot found with the
     * given name, returns CerberusException with
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
        final String query = "SELECT * FROM robot a WHERE a.robotID = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, id);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
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
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return result;
    }

    /**
     * Finds all Robot that exists. </p> Access to database to return all
     * existing {@link Robot}.<br/> If no Robot found, returns a empty
     * {@literal List<Robot>}.<br/> If an SQLException occur, returns null in
     * the list object and writes the error on the logs.
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
                        Robot robot = this.loadFromResultSet(resultSet);
                        list.add(robot);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
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
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return list;
    }

    /**
     * Updates the information based on the object Robot. </p> Access to
     * database to update Robot information given by the object Robot.
     * <br/> If an SQLException occur, throw CerberusException.
     *
     * @param robot object Robot to update
     * @throws CerberusException
     * @since 0.9.2
     */
    @Override
    public void updateRobot(Robot robot) throws CerberusException {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE robot SET robot= ? , host = ? , port = ? ,");
        query.append("platform = ?, browser = ? , version = ?, active=?, description = ?, useragent = ? WHERE robotID = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, robot.getRobot());
                preStat.setString(2, robot.getHost());
                preStat.setString(3, robot.getPort());
                preStat.setString(4, robot.getPlatform());
                preStat.setString(5, robot.getBrowser());
                preStat.setString(6, robot.getVersion());
                preStat.setString(7, robot.getActive());
                preStat.setString(8, robot.getDescription());
                preStat.setString(9, robot.getUserAgent());
                preStat.setInt(10, robot.getRobotID());

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
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

    }

    @Override
    public void createRobot(Robot robot) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO robot (`robot`, `host`, `port`, `platform`,`browser`, `version`,`active` , `description`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, robot.getRobot());
                preStat.setString(2, robot.getHost());
                preStat.setString(3, robot.getPort());
                preStat.setString(4, robot.getPlatform());
                preStat.setString(5, robot.getBrowser());
                preStat.setString(6, robot.getVersion());
                preStat.setString(7, robot.getActive());
                preStat.setString(8, robot.getDescription());

                preStat.executeUpdate();
                throwExcep = false;

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
            } catch (SQLException exception) {
                LOG.warn("Unable to close exception : " + exception.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteRobot(Robot robot) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM robot WHERE robotID = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, robot.getRobotID());

                throwExcep = preStat.executeUpdate() == 0;
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
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
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
        gSearch.append(" or `host` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `port` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `robot` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `browser` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `active` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `useragent` like '%");
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
                        robotList.add(this.loadFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

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
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
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
        gSearch.append(" or `host` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `port` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `robot` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `browser` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `active` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `useragent` like '%");
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
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

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
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return result;
    }

    @Override
    public Robot findRobotByName(String name) throws CerberusException {
        Robot result = null;
        final String query = "SELECT * FROM robot a WHERE a.robot = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, name);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
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
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return result;
    }

}
