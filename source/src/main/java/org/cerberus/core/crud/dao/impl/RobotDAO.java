/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.crud.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IRobotDAO;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.factory.IFactoryRobot;
import org.cerberus.core.crud.factory.impl.FactoryRobot;
import org.cerberus.core.crud.utils.RequestDbUtils;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bcivel
 * @since 0.9.2
 */
@Repository
public class RobotDAO implements IRobotDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryRobot factoryRobot;

    private static final Logger LOG = LogManager.getLogger(RobotDAO.class);

    private final String OBJECT_NAME = "Robot";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<Robot> readByKeyTech(Integer robotid) {
        AnswerItem<Robot> ans = new AnswerItem<>();
        Robot result = null;
        final String query = "SELECT * FROM `robot` WHERE `robotID` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
            preStat.setInt(1, robotid);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public Robot readByKey(String robot) throws CerberusException {
        Robot result;
        final String query = "SELECT * FROM `robot` WHERE `robot` = ?";

        result = RequestDbUtils.executeQuery(databaseSpring, query,
                ps -> ps.setString(1, robot),
                rs -> loadFromResultSet(rs)
        );

        //sets the message
        return result;
    }

    @Override
    public AnswerList<Robot> readByRobotList(List<String> robotList) {
        return readByRobotList(robotList, null);
    }

    @Override
    public AnswerList<Robot> readByRobotList(List<String> robotList, String typeRobot) {
        AnswerList<Robot> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Robot> robotListResult = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM robot rbt ");

        searchSQL.append(" where 1=1 ");

        if ((robotList != null) && (!robotList.isEmpty())) {
            searchSQL.append(" and (").append(SqlUtil.generateInClause("rbt.`robot`", robotList)).append(")");
        }
        query.append(searchSQL);

        if (!StringUtil.isEmptyOrNull(typeRobot)) {
            query.append(" and type=? ");
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if ((robotList != null) && (!robotList.isEmpty())) {
                    for (String myrobot : robotList) {
                        preStat.setString(i++, myrobot);
                    }
                }

                if (!StringUtil.isEmptyOrNull(typeRobot)) {
                    preStat.setString(i++, typeRobot);
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        robotListResult.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (robotListResult.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(robotListResult, nrTotalRows);
                    } else if (robotListResult.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(robotListResult, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(robotListResult, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
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
        response.setDataList(robotListResult);
        return response;
    }

    @Override
    public AnswerList<Robot> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<Robot> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Robot> robotList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM robot ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`platform` like ?");
            searchSQL.append(" or `description` like ?");
            searchSQL.append(" or `robot` like ?");
            searchSQL.append(" or `browser` like ?");
            searchSQL.append(" or `useragent` like ?");
            searchSQL.append(" or `screensize` like ?");
            searchSQL.append(" or `robotdecli` like ?");
            searchSQL.append(" or `version` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);

        if (!StringUtil.isEmptyOrNull(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (!StringUtil.isEmptyOrNull(searchTerm)) {
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                }
                for (String individualColumnSearchValue : individalColumnSearchValues) {
                    preStat.setString(i++, individualColumnSearchValue);
                }
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

                    if (robotList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(robotList, nrTotalRows);
                    } else if (robotList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(robotList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(robotList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
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
        query.append("INSERT INTO robot (`robot`, `platform`,`browser`, `version`,`isactive` , `description`, `useragent`, `screensize`, `ProfileFolder`, `AcceptNotifications`, `ExtraParam`, `IsAcceptInsecureCerts`, `robotdecli`, `lbexemethod`, `type`) ");

        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setString(i++, robot.getRobot());
                preStat.setString(i++, robot.getPlatform());
                preStat.setString(i++, robot.getBrowser());
                preStat.setString(i++, robot.getVersion());
                preStat.setBoolean(i++, robot.isActive());
                preStat.setString(i++, robot.getDescription());
                preStat.setString(i++, robot.getUserAgent());
                preStat.setString(i++, robot.getScreenSize());
                preStat.setString(i++, robot.getProfileFolder());
                preStat.setInt(i++, robot.getAcceptNotifications());
                preStat.setString(i++, robot.getExtraParam());
                preStat.setBoolean(i++, robot.isAcceptInsecureCerts());
                preStat.setString(i++, robot.getRobotDecli());
                preStat.setString(i++, robot.getLbexemethod());
                preStat.setString(i++, robot.getType());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
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

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, application.getRobotID());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
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
        query.append("UPDATE robot SET robot= ? ,");
        query.append("platform = ?, browser = ? , version = ?, isactive=?, description = ?, useragent = ?, screensize = ?, ProfileFolder = ?, AcceptNotifications = ?, ExtraParam = ?, IsAcceptInsecureCerts = ?, robotdecli = ?, lbexemethod = ?, type = ?, dateModif = NOW() ");
        query.append("WHERE robotID = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int cpt = 1;
                preStat.setString(cpt++, robot.getRobot());
                preStat.setString(cpt++, robot.getPlatform());
                preStat.setString(cpt++, robot.getBrowser());
                preStat.setString(cpt++, robot.getVersion());
                preStat.setBoolean(cpt++, robot.isActive());
                preStat.setString(cpt++, robot.getDescription());
                preStat.setString(cpt++, robot.getUserAgent());
                preStat.setString(cpt++, robot.getScreenSize());
                preStat.setString(cpt++, robot.getProfileFolder());
                preStat.setInt(cpt++, robot.getAcceptNotifications());
                preStat.setString(cpt++, robot.getExtraParam());
                preStat.setBoolean(cpt++, robot.isAcceptInsecureCerts());
                preStat.setString(cpt++, robot.getRobotDecli());
                preStat.setString(cpt++, robot.getLbexemethod());
                preStat.setString(cpt++, robot.getType());
                preStat.setInt(cpt++, robot.getRobotID());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
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
    public Robot loadFromResultSet(ResultSet rs) throws SQLException {
        Integer robotID = ParameterParserUtil.parseIntegerParam(rs.getString("robotID"), 0);
        String robot = ParameterParserUtil.parseStringParam(rs.getString("robot"), "");
        String platform = ParameterParserUtil.parseStringParam(rs.getString("platform"), "");
        String browser = ParameterParserUtil.parseStringParam(rs.getString("browser"), "");
        String version = ParameterParserUtil.parseStringParam(rs.getString("version"), "");
        boolean isActive = ParameterParserUtil.parseBooleanParam(rs.getString("isActive"), true);
        String lbexemethod = ParameterParserUtil.parseStringParam(rs.getString("lbexemethod"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("description"), "");
        String userAgent = ParameterParserUtil.parseStringParam(rs.getString("useragent"), "");
        String screenSize = ParameterParserUtil.parseStringParam(rs.getString("screensize"), "");
        String profileFolder = ParameterParserUtil.parseStringParam(rs.getString("ProfileFolder"), "");
        String robotDecli = ParameterParserUtil.parseStringParam(rs.getString("robotdecli"), "");
        String type = ParameterParserUtil.parseStringParam(rs.getString("type"), "");
        Integer acceptNotifications = ParameterParserUtil.parseIntegerParam(rs.getString("AcceptNotifications"), 0);
        String extraParam = ParameterParserUtil.parseStringParam(rs.getString("ExtraParam"), "");
        boolean isAcceptInsecureCerts = rs.getBoolean("isAcceptInsecureCerts");

        //TODO remove when working in test with mockito and autowired
        factoryRobot = new FactoryRobot();
        return factoryRobot.create(robotID, robot, platform, browser, version, isActive, lbexemethod, description, userAgent, screenSize, profileFolder, acceptNotifications, extraParam, isAcceptInsecureCerts, robotDecli, type);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM robot ");

        searchSQL.append("WHERE 1=1");

        // Not allowing distinct on forbiden data.
        if (columnName.contains("host_pasword")) {
            searchSQL.append(" and 1=0 ");
        }

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`platform` like ?");
            searchSQL.append(" or `description` like ?");
            searchSQL.append(" or `robot` like ?");
            searchSQL.append(" or `browser` like ?");
            searchSQL.append(" or `useragent` like ?");
            searchSQL.append(" or `screensize` like ?");
            searchSQL.append(" or `ProfileFolder` like ?");
            searchSQL.append(" or `robotdecli` like ?");
            searchSQL.append(" or `version` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement();) {

            int i = 1;
            if (!StringUtil.isEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }
                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else if (distinctValues.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (Exception e) {
            LOG.warn("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            // We always set the result message
            answer.setResultMessage(msg);
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

}
