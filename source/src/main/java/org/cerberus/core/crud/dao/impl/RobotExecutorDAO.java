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

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IRobotExecutorDAO;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.factory.IFactoryRobotExecutor;
import org.cerberus.core.crud.factory.impl.FactoryRobotExecutor;
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

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@Repository
public class RobotExecutorDAO implements IRobotExecutorDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryRobotExecutor factoryRobotExecutor;

    private static final Logger LOG = LogManager.getLogger(RobotExecutorDAO.class);

    private final String OBJECT_NAME = "Robot Executor";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<RobotExecutor> readByKey(String robot, String executor) {
        AnswerItem<RobotExecutor> ans = new AnswerItem<>();
        RobotExecutor result = null;
        final String query = "SELECT * FROM `robotexecutor` rbe WHERE `robot` = ? and `executor` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.robot : " + robot);
            LOG.debug("SQL.param.executor : " + executor);
        }
        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
            preStat.setString(1, robot);
            preStat.setString(2, executor);

            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
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
    public List<RobotExecutor> readBestByKey(String robot) throws CerberusException {
        final String query = "SELECT * FROM `robotexecutor` rbe WHERE `robot` = ? and isactive = true order by DateLastExeSubmitted asc, `rank` asc";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL.param.robot : " + robot);
        }

        List<RobotExecutor> res = RequestDbUtils.executeQueryList(databaseSpring, query,
                ps -> ps.setString(1, robot),
                rs -> loadFromResultSet(rs));

        return res;
    }

    @Override
    public AnswerList<RobotExecutor> readByVariousByCriteria(List<String> robot, String active, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<RobotExecutor> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<RobotExecutor> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM robotexecutor rbe ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (rbe.`robot` like ?");
            searchSQL.append(" or rbe.`executor` like ?");
            searchSQL.append(" or rbe.`rank` like ?");
            searchSQL.append(" or rbe.`host` like ?");
            searchSQL.append(" or rbe.`port` like ?");
            searchSQL.append(" or rbe.`HostUser` like ?");
            searchSQL.append(" or rbe.`HostPassword` like ?");
            searchSQL.append(" or rbe.`deviceudid` like ?");
            searchSQL.append(" or rbe.`devicename` like ?");
            searchSQL.append(" or rbe.`usrCreated` like ?");
            searchSQL.append(" or rbe.`usrModif` like ?");
            searchSQL.append(" or rbe.`dateCreated` like ?");
            searchSQL.append(" or rbe.`dateModif` like ?");
            searchSQL.append(" or rbe.`description` like ?)");
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

        if ((robot != null) && (!robot.isEmpty())) {
            searchSQL.append(" and (").append(SqlUtil.generateInClause("rbe.`robot`", robot)).append(")");
        }
        if (!StringUtil.isEmptyOrNull(active)) {
            searchSQL.append(" and (`isactive` = ? )");
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
            LOG.debug("SQL.param.robot : " + robot);
            LOG.debug("SQL.param.active : " + active);
        }

        try (Connection connection = this.databaseSpring.connect();
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
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            if ((robot != null) && (!robot.isEmpty())) {
                for (String myrobot : robot) {
                    preStat.setString(i++, myrobot);
                }
            }
            if (!StringUtil.isEmptyOrNull(active)) {
                preStat.setString(i++, active);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }
                //get the total number of rows

                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }
                if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    response = new AnswerList<>(objectList, nrTotalRows);
                } else if (objectList.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    response = new AnswerList<>(objectList, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    response = new AnswerList<>(objectList, nrTotalRows);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public Answer create(RobotExecutor object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO robotexecutor (`robot`, `executor`, `isactive`, `rank`, `host`, `port`, `HostUser`, `HostPassword`, `deviceudid`, `devicename`, `deviceport`, `isdevicelockunlock`, `ExecutorProxyServiceHost`, `ExecutorProxyServicePort`, `ExecutorBrowserProxyHost`, `ExecutorBrowserProxyPort`, `ExecutorExtensionPort`, `executorproxytype`, `description`, `usrcreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {
            try {
                int i = 1;
                preStat.setString(i++, object.getRobot());
                preStat.setString(i++, object.getExecutor());
                preStat.setBoolean(i++, object.isActive());
                preStat.setInt(i++, object.getRank());
                preStat.setString(i++, object.getHost());
                preStat.setString(i++, object.getPort());
                preStat.setString(i++, object.getHostUser());
                preStat.setString(i++, object.getHostPassword());
                preStat.setString(i++, object.getDeviceUuid());
                preStat.setString(i++, object.getDeviceName());
                if (object.getDevicePort() != null) {
                    preStat.setInt(i++, object.getDevicePort());
                } else {
                    preStat.setNull(i++, Types.INTEGER);
                }
                preStat.setBoolean(i++, object.isDeviceLockUnlock());
                preStat.setString(i++, object.getExecutorProxyServiceHost());
                if (object.getExecutorProxyServicePort() != null) {
                    preStat.setInt(i++, object.getExecutorProxyServicePort());
                } else {
                    preStat.setNull(i++, Types.INTEGER);
                }
                preStat.setString(i++, object.getExecutorBrowserProxyHost());
                if (object.getExecutorBrowserProxyPort() != null) {
                    preStat.setInt(i++, object.getExecutorBrowserProxyPort());
                } else {
                    preStat.setNull(i++, Types.INTEGER);
                }
                preStat.setInt(i++, object.getExecutorExtensionPort());
                preStat.setString(i++, object.getExecutorProxyType());
                preStat.setString(i++, object.getDescription());
                preStat.setString(i++, object.getUsrCreated());
                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(RobotExecutor object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM robotexecutor WHERE `robot` = ? and `executor` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.robot : " + object.getRobot());
            LOG.debug("SQL.param.executor : " + object.getExecutor());
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            int i = 1;
            preStat.setString(i++, object.getRobot());
            preStat.setString(i++, object.getExecutor());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(String robot, String executor, RobotExecutor object) {
        MessageEvent msg = null;
        final String query = "UPDATE robotexecutor SET `robot` = ?, `executor` = ?, description = ?, isactive = ?, `rank` = ?, `host` = ?, `port` = ?, `HostUser` = ?, `HostPassword` = ?, `deviceudid` = ?, `devicename` = ?, `deviceport` = ?,  `isdevicelockunlock` = ?,  `ExecutorProxyServiceHost` = ?,  `ExecutorProxyServicePort` = ?, `ExecutorBrowserProxyHost` = ?,  `ExecutorBrowserProxyPort` = ?, `ExecutorExtensionPort` = ?, `executorproxytype` = ?, "
                + "dateModif = NOW(), usrModif= ?  WHERE `robot` = ? and `executor` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.robot : " + object.getRobot());
            LOG.debug("SQL.param.executor : " + object.getExecutor());
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            int i = 1;
            preStat.setString(i++, object.getRobot());
            preStat.setString(i++, object.getExecutor());
            preStat.setString(i++, object.getDescription());
            preStat.setBoolean(i++, object.isActive());
            preStat.setInt(i++, object.getRank());
            preStat.setString(i++, object.getHost());
            preStat.setString(i++, object.getPort());
            preStat.setString(i++, object.getHostUser());
            preStat.setString(i++, object.getHostPassword());
            preStat.setString(i++, object.getDeviceUuid());
            preStat.setString(i++, object.getDeviceName());
            if (object.getDevicePort() != null) {
                preStat.setInt(i++, object.getDevicePort());
            } else {
                preStat.setNull(i++, Types.INTEGER);
            }
            preStat.setBoolean(i++, object.isDeviceLockUnlock());
            preStat.setString(i++, object.getExecutorProxyServiceHost());
            if (object.getExecutorProxyServicePort() != null) {
                preStat.setInt(i++, object.getExecutorProxyServicePort());
            } else {
                preStat.setNull(i++, Types.INTEGER);
            }
            preStat.setString(i++, object.getExecutorBrowserProxyHost());
            if (object.getExecutorBrowserProxyPort() != null) {
                preStat.setInt(i++, object.getExecutorBrowserProxyPort());
            } else {
                preStat.setNull(i++, Types.INTEGER);
            }
            preStat.setInt(i++, object.getExecutorExtensionPort());
            preStat.setString(i++, object.getExecutorProxyType());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i++, robot);
            preStat.setString(i++, executor);
            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public Answer updateLastExe(String robot, String executor) {
        MessageEvent msg = null;
        final String query = "UPDATE robotexecutor SET DateLastExeSubmitted = ? "
                + " WHERE `robot` = ? and `executor` = ?";

        long now = new Date().getTime();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.robot : " + robot);
            LOG.debug("SQL.param.executor : " + executor);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            int i = 1;
            preStat.setLong(i++, now);
            preStat.setString(i++, robot);
            preStat.setString(i++, executor);
            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public RobotExecutor loadFromResultSet(ResultSet rs) throws SQLException {
        int id = ParameterParserUtil.parseIntegerParam(rs.getString("rbe.id"), 0);
        String robot = ParameterParserUtil.parseStringParam(rs.getString("rbe.robot"), "");
        String executor = ParameterParserUtil.parseStringParam(rs.getString("rbe.executor"), "");
        boolean isActive = rs.getBoolean("rbe.isActive");
        int rank = ParameterParserUtil.parseIntegerParam(rs.getString("rbe.rank"), 0);
        String host = ParameterParserUtil.parseStringParam(rs.getString("rbe.host"), "");
        String port = ParameterParserUtil.parseStringParam(rs.getString("rbe.port"), "");
        int executorExtensionProxyPort = ParameterParserUtil.parseIntegerParam(rs.getString("rbe.ExecutorExtensionProxyPort"), 0);
        String hostUser = ParameterParserUtil.parseStringParam(rs.getString("rbe.HostUser"), "");
        String hostPassword = ParameterParserUtil.parseStringParam(rs.getString("rbe.HostPassword"), "");
        String deviceudid = ParameterParserUtil.parseStringParam(rs.getString("rbe.deviceudid"), "");
        String devicename = ParameterParserUtil.parseStringParam(rs.getString("rbe.devicename"), "");
        Integer deviceport = rs.getInt("rbe.deviceport");
        boolean isDevicelockunlock = rs.getBoolean("rbe.isdevicelockunlock");
        String executorProxyServiceHost = rs.getString("rbe.ExecutorProxyServiceHost");
        Integer executorProxyServicePort = rs.getInt("rbe.ExecutorProxyServicePort");
        String executorBrowserProxyHost = rs.getString("rbe.ExecutorBrowserProxyHost");
        Integer executorBrowserProxyPort = rs.getInt("rbe.ExecutorBrowserProxyPort");
        Integer executorExtensionPort = rs.getInt("rbe.ExecutorExtensionPort");
        String executorProxyType = rs.getString("rbe.executorproxytype");
        if(deviceport == 0) {
            deviceport=null;
        }
        String description = ParameterParserUtil.parseStringParam(rs.getString("rbe.description"), "");
        long dateLastExe = rs.getLong("rbe.DateLastExeSubmitted");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("rbe.UsrModif"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("rbe.UsrCreated"), "");
        Timestamp dateModif = rs.getTimestamp("rbe.DateModif");
        Timestamp dateCreated = rs.getTimestamp("rbe.DateCreated");

        //TODO remove when working in test with mockito and autowired
        factoryRobotExecutor = new FactoryRobotExecutor();
        return factoryRobotExecutor.create(id, robot, executor, isActive, rank, host, port, hostUser, hostPassword, executorExtensionProxyPort, deviceudid, devicename, deviceport, isDevicelockunlock, executorProxyServiceHost, executorProxyServicePort, executorBrowserProxyHost, executorBrowserProxyPort, executorExtensionPort, executorProxyType, description, usrCreated, dateCreated, usrModif, dateModif);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String robot, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM robotexecutor ");

        searchSQL.append("WHERE 1=1");
        if (!StringUtil.isEmptyOrNull(robot)) {
            searchSQL.append(" and (`robot` = ? )");
        }

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (src.`robot` like ?");
            searchSQL.append(" or src.`executor` like ?");
            searchSQL.append(" or src.`rank` like ?");
            searchSQL.append(" or src.`host` like ?");
            searchSQL.append(" or src.`usrCreated` like ?");
            searchSQL.append(" or src.`usrModif` like ?");
            searchSQL.append(" or src.`dateCreated` like ?");
            searchSQL.append(" or src.`dateModif` like ?");
            searchSQL.append(" or src.`description` like ?)");
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
            if (!StringUtil.isEmptyOrNull(robot)) {
                preStat.setString(i++, robot);
            }
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
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
