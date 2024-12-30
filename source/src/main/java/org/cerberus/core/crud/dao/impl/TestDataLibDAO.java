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

import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestDataLibDAO;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.factory.IFactoryTestDataLib;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.security.UserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bcivel
 * @author FNogueira
 */
@Repository
public class TestDataLibDAO implements ITestDataLibDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestDataLib factoryTestDataLib;
    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(TestDataLibDAO.class);

    private final String OBJECT_NAME = "Test Data Library";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 10000000;

    @Override
    public AnswerItem<TestDataLib> readByKey(int testDataLibID) {
        AnswerItem<TestDataLib> answer = new AnswerItem<>();
        MessageEvent msg;
        TestDataLib result;
        final String query = "SELECT * FROM testdatalib tdl "
                + " LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID = tdd.TestDataLibID and tdd.Subdata='' "
                + " WHERE tdl.`TestDataLibID` = ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();

        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preStat.setInt(1, testDataLibID);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        answer.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        //sets the message
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerItem<TestDataLib> readByNameBySystemByEnvironmentByCountry(String name, String system, String environment, String country) {
        AnswerItem<TestDataLib> answer = new AnswerItem<>();
        TestDataLib result = null;
        MessageEvent msg;

        final String query = new StringBuilder("SELECT * FROM testdatalib tdl")
                .append(" LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID = tdd.TestDataLibID and tdd.Subdata='' ")
                .append(" WHERE `name` LIKE ?")
                .append(" and (`system` = ? or `system` = '')")
                .append(" and (`environment` = ? or `environment` = '')")
                .append(" and (`country` = ? or `country` = '')")
                .append(" ORDER BY `name` DESC, `system` DESC, environment DESC, country DESC, tdl.TestDataLibID ASC")
                .append(" LIMIT 0,1").toString();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL name : " + name);
            LOG.debug("SQL system : " + system);
            LOG.debug("SQL environment : " + environment);
            LOG.debug("SQL country : " + country);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preStat.setString(1, name);
            preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(system));
            preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(environment));
            preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(country));
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);

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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        answer.setItem(result);
        answer.setResultMessage(msg);
        return answer;
    }

    private static void deleteFolder(File folder, boolean deleteit) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f, true);
                } else {
                    f.delete();
                }
            }
        }
        if (deleteit) {
            folder.delete();
        }
    }

    @Override
    public Answer uploadFile(int id, FileItem file) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                "cerberus_testdatalibfile_path Parameter not found");
        AnswerItem a = parameterService.readByKey("", Parameter.VALUE_cerberus_testdatalibfile_path);
        if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            Parameter p = (Parameter) a.getItem();
            String uploadPath = p.getValue();
            File appDir = new File(uploadPath + File.separator + id);
            if (!appDir.exists()) {
                try {
                    appDir.mkdirs();
                } catch (SecurityException se) {
                    LOG.warn("Unable to create testdatalib csv dir: " + se.getMessage());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                            se.toString());
                    a.setResultMessage(msg);
                }
            }
            if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                deleteFolder(appDir, false);
                File picture = new File(uploadPath + File.separator + id + File.separator + file.getName());
                try {
                    file.write(picture);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("DESCRIPTION",
                            "TestDataLib File uploaded");
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "testDatalib File").replace("%OPERATION%", "Upload"));
                } catch (Exception e) {
                    LOG.warn("Unable to upload testdatalib csv file: " + e.getMessage());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                            e.toString());
                }
            }
        } else {
            LOG.warn("cerberus_testdatalibCSV_path Parameter not found");
        }
        a.setResultMessage(msg);
        return a;
    }

    @Override
    public AnswerList<TestDataLib> readNameListByName(String testDataLibName, int limit, boolean like) {
        AnswerList<TestDataLib> answer = new AnswerList<>();
        MessageEvent msg;
        List<TestDataLib> list = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT * ")
                .append("FROM testdatalib tdl ");
        if (like) {
            query.append(" WHERE `name` like  ? ");

        } else {
            query.append(" WHERE `name` =  ? ");
        }
        query.append(" limit ? ");

        if ((limit <= 0) || (limit >= MAX_ROW_SELECTED)) {
            limit = MAX_ROW_SELECTED;
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            if (like) {
                preStat.setString(1, "%" + testDataLibName + "%");
            } else {
                preStat.setString(1, testDataLibName);
            }

            preStat.setInt(2, limit);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }

                    if (list.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    list.clear();
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }

        answer.setDataList(list);
        answer.setTotalRows(list.size());
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<TestDataLib> readAll() {
        AnswerList<TestDataLib> answer = new AnswerList<>();
        MessageEvent msg;

        List<TestDataLib> list = new ArrayList<>();
        final String query = "SELECT * FROM testdatalib tdl"
                + " LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID = tdd.TestDataLibID and tdd.Subdata=''; ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                    if (list.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    list.clear();

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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setDataList(list);
        answer.setTotalRows(list.size());
        return answer;
    }

    @Override
    public AnswerList<TestDataLib> readByVariousByCriteria(String name, List<String> systems, String environment, String country, String type, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {

        AnswerList<TestDataLib> answer = new AnswerList<>();
        MessageEvent msg;
        int nrTotalRows = 0;
        List<TestDataLib> objectList = new ArrayList<>();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testdatalib tdl ");

        query.append("LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID=tdd.TestDataLibID and tdd.SubData='' ");

        searchSQL.append(" WHERE 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (tdl.`name` like ?");
            searchSQL.append(" or tdl.`privateData` like ?");
            searchSQL.append(" or tdl.`group` like ?");
            searchSQL.append(" or tdl.`type` like ?");
            searchSQL.append(" or tdl.`database` like ?");
            searchSQL.append(" or tdl.`databaseUrl` like ?");
            searchSQL.append(" or tdl.`script` like ?");
            searchSQL.append(" or tdl.`service` like ?");
            searchSQL.append(" or tdl.`servicepath` like ?");
            searchSQL.append(" or tdl.`method` like ?");
            searchSQL.append(" or tdl.`envelope` like ?");
            searchSQL.append(" or tdl.`databaseCsv` like ?");
            searchSQL.append(" or tdl.`csvUrl` like ?");
            searchSQL.append(" or tdl.`separator` like ?");
            searchSQL.append(" or tdl.`description` like ?");
            searchSQL.append(" or tdl.`system` like ?");
            searchSQL.append(" or tdl.`environment` like ?");
            searchSQL.append(" or tdl.`country` like ?) ");
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
        if (name != null) {
            searchSQL.append(" and tdl.`name` = ? ");
        }
        if (environment != null) {
            searchSQL.append(" and tdl.`environment` = ? ");
        }
        if (country != null) {
            searchSQL.append(" and tdl.`country` = ? ");
        }
        if (!StringUtil.isEmptyOrNull(type)) {
            searchSQL.append(" and tdl.`type` = ? ");
        }
        query.append(searchSQL);

        if (systems != null && !systems.isEmpty()) {
            // authorize transversal object
            systems.add("");
            query.append(" AND ");
            query.append(SqlUtil.generateInClause("tdl.`system`", systems));
        }
        query.append(" AND ");
        query.append(UserSecurity.getSystemAllowForSQL("tdl.`system`"));

        if (!StringUtil.isEmptyOrNull(column)) {
            query.append(" order by ").append(column).append(" ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount).append(" ");
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.name : " + name);
            LOG.debug("SQL.system : " + systems);
            LOG.debug("SQL.environment : " + environment);
            LOG.debug("SQL.country : " + country);
            LOG.debug("SQL.type : " + type);
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
                if (name != null) {
                    preStat.setString(i++, name);
                }
                if (environment != null) {
                    preStat.setString(i++, environment);
                }
                if (country != null) {
                    preStat.setString(i++, country);
                }
                if (!StringUtil.isEmptyOrNull(type)) {
                    preStat.setString(i++, type);
                }

                if (systems != null && !systems.isEmpty()) {
                    for (String sys : systems) {
                        preStat.setString(i++, sys);
                    }
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }
                    if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    } else if (objectList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    objectList.clear();
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        answer.setTotalRows(nrTotalRows);
        answer.setResultMessage(msg);
        answer.setDataList(objectList);
        return answer;
    }

    @Override
    public AnswerList<String> readDistinctGroups() {
        AnswerList<String> answerList = new AnswerList<>();
        ArrayList<String> listOfGroups = new ArrayList<>();
        MessageEvent msg;
        String query = "SELECT distinct(`Group`) FROM testdatalib  WHERE `Group` <> '' ORDER BY `Group`";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();

        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        listOfGroups.add(resultSet.getString(1));
                    }
                    if (listOfGroups.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    listOfGroups.clear();
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        answerList.setTotalRows(listOfGroups.size());
        answerList.setDataList(listOfGroups);
        answerList.setResultMessage(msg);
        return answerList;
    }

    @Override
    public AnswerItem<TestDataLib> create(TestDataLib testDataLib) {
        MessageEvent msg;
        AnswerItem<TestDataLib> answer = new AnswerItem<>();
        StringBuilder query = new StringBuilder();
        TestDataLib createdTestDataLib;
        query.append("INSERT INTO testdatalib (`name`, `system`, `environment`, `country`, `privateData`, `group`, `type`, `database`, `script`, `databaseUrl`, ");
        query.append("`service`, `servicePath`, `method`, `envelope`, `databaseCsv`, `csvUrl`,`separator`,`ignoreFirstLine`, `description`, `creator`) ");
        if ((testDataLib.getService() != null) && (!testDataLib.getService().isEmpty())) {
            query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        } else {
            query.append("VALUES (?,?,?,?,?,?,?,?,?,?,null,?,?,?,?,?,?,?,?,?)");

        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.name : " + testDataLib.getName());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
            try {
                int i = 1;
                preStat.setString(i++, testDataLib.getName());
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getSystem()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getEnvironment()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCountry()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getPrivateData()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getGroup()));
                preStat.setString(i++, testDataLib.getType());
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getDatabase()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getScript()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getDatabaseUrl()));
                if ((testDataLib.getService() != null) && (!testDataLib.getService().isEmpty())) {
                    preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getService()));
                }
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getServicePath()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getMethod()));
                preStat.setString(i++, testDataLib.getEnvelope()); //is the one that allows null values
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getDatabaseCsv()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCsvUrl()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getSeparator()));
                preStat.setBoolean(i++, testDataLib.isIgnoreFirstLine());
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getDescription()));
                preStat.setString(i++, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCreator()));

                preStat.executeUpdate();

                ResultSet keys = preStat.getGeneratedKeys();
                try {
                    if (keys != null && keys.next()) {
                        testDataLib.setTestDataLibID(keys.getInt(1));
                        // Debug message on SQL.
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("SQL.result.TestDataLibID : " + testDataLib.getTestDataLibID());
                        }
                        answer.setItem(testDataLib);
                    }
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    if (keys != null) {
                        keys.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }

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
            } catch (SQLException ex) {
                LOG.error("Unable to close connection : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public Answer delete(TestDataLib testDataLib) {
        Answer ans = new Answer();
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM testdatalib WHERE testdatalibid = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLib.getTestDataLibID());

                int rowsDeleted = preStat.executeUpdate();

                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public Answer update(TestDataLib testDataLib) {
        Answer answer = new Answer();
        MessageEvent msg;
        String query = "UPDATE testdatalib SET `name`=?, `type`=?, `privateData`=?, `group`= ?, `system`=?, `environment`=?, `country`=?, `database`= ? , `script`= ? , "
                + "`databaseUrl`= ? , `servicepath`= ? , `method`= ? , `envelope`= ? , `DatabaseCsv` = ? , `csvUrl` = ? , `separator`= ?, `ignoreFirstLine`= ?,  `description`= ? , `LastModifier`= ?, `LastModified` = NOW() ";
        if ((testDataLib.getService() != null) && (!testDataLib.getService().isEmpty())) {
            query += " ,`service` = ? ";
        } else {
            query += " ,`service` = null ";
        }
        query += "WHERE `TestDataLibID`= ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.service : " + testDataLib.getService());
            LOG.debug("SQL.param.servicePath : " + testDataLib.getServicePath());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, testDataLib.getName());
                preStat.setString(i++, testDataLib.getType());
                preStat.setString(i++, testDataLib.getPrivateData());
                preStat.setString(i++, testDataLib.getGroup());
                preStat.setString(i++, testDataLib.getSystem());
                preStat.setString(i++, testDataLib.getEnvironment());
                preStat.setString(i++, testDataLib.getCountry());
                preStat.setString(i++, testDataLib.getDatabase());
                preStat.setString(i++, testDataLib.getScript());
                preStat.setString(i++, testDataLib.getDatabaseUrl());
                preStat.setString(i++, testDataLib.getServicePath());
                preStat.setString(i++, testDataLib.getMethod());
                preStat.setString(i++, testDataLib.getEnvelope());
                preStat.setString(i++, testDataLib.getDatabaseCsv());
                preStat.setString(i++, testDataLib.getCsvUrl());
                preStat.setString(i++, testDataLib.getSeparator());
                preStat.setBoolean(i++, testDataLib.isIgnoreFirstLine());
                preStat.setString(i++, testDataLib.getDescription());
                preStat.setString(i++, testDataLib.getLastModifier());
                if ((testDataLib.getService() != null) && (!testDataLib.getService().isEmpty())) {
                    preStat.setString(i++, testDataLib.getService());
                }
                preStat.setInt(i++, testDataLib.getTestDataLibID());

                int rowsUpdated = preStat.executeUpdate();

                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "UPDATE").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public Answer bulkRenameDataLib(String oldName, String newName) {
        Answer answer = new Answer();
        MessageEvent msg;

        String query = "UPDATE testdatalib SET `name`=? ";
        query += " WHERE `name`=? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect()) {
            try (PreparedStatement preStat = connection.prepareStatement(query)) {
                int i = 1;
                preStat.setString(i++, newName);
                preStat.setString(i++, oldName);

                int rowsUpdated = preStat.executeUpdate();

                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                // Message to customize : X datalib updated using the rowsUpdated variable
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE").replace("success!", "success! - Row(s) updated : " + String.valueOf(rowsUpdated)));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);
                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "UPDATE").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public TestDataLib loadFromResultSet(ResultSet resultSet) throws SQLException {
        Integer testDataLibID = resultSet.getInt("tdl.testDataLibID");
        String name = resultSet.getString("tdl.name");
        String system = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.system"));
        String environment = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.environment"));
        String country = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.country"));
        String group = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.group"));
        String privateData = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.privateData"));
        String type = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.type"));
        String database = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.database"));
        String script = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.script"));
        String databaseUrl = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.databaseUrl"));
        String service = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.service"));
        String servicePath = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.servicePath"));
        String method = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.method"));
        String envelope = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.envelope"));
        String databaseCsv = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.databaseCsv"));
        String csvUrl = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.csvUrl"));
        String separator = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.separator"));
        boolean ignoreFirstLine = resultSet.getBoolean("tdl.ignoreFirstLine");
        String description = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.description"));
        String creator = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.Creator"));
        Timestamp created = resultSet.getTimestamp("tdl.Created");
        String lastModifier = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.LastModifier"));
        Timestamp lastModified = resultSet.getTimestamp("tdl.LastModified");
        String subDataValue = null;
        String subDataColumn = null;
        String subDataParsingAnswer = null;
        String subDataColumnPosition = null;
        try {
            subDataValue = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdd.Value"));
            subDataColumn = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdd.Column"));
            subDataParsingAnswer = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdd.parsingAnswer"));
            subDataColumnPosition = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdd.columnPosition"));
        } catch (Exception ex) {
            LOG.debug(ex.toString());
        }

        return factoryTestDataLib.create(testDataLibID, name, system, environment, country, privateData, group, type, database, script, databaseUrl, service, servicePath,
                method, envelope, databaseCsv, csvUrl, separator, ignoreFirstLine, description, creator, created, lastModifier, lastModified, subDataValue, subDataColumn, subDataParsingAnswer, subDataColumnPosition);
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
        query.append(" as distinctValues FROM testdatalib tdl  ");
        query.append("LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID=tdd.TestDataLibID and tdd.SubData='' ");

        searchSQL.append("WHERE 1=1");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (tdl.`name` like ?");
            searchSQL.append(" or tdl.`privateData` like ?");
            searchSQL.append(" or tdl.`group` like ?");
            searchSQL.append(" or tdl.`type` like ?");
            searchSQL.append(" or tdl.`database` like ?");
            searchSQL.append(" or tdl.`databaseUrl` like ?");
            searchSQL.append(" or tdl.`script` like ?");
            searchSQL.append(" or tdl.`servicepath` like ?");
            searchSQL.append(" or tdl.`method` like ?");
            searchSQL.append(" or tdl.`envelope` like ?");
            searchSQL.append(" or tdl.`csvUrl` like ?");
            searchSQL.append(" or tdl.`separator` like ?");
            searchSQL.append(" or tdl.`description` like ?");
            searchSQL.append(" or tdl.`system` like ?");
            searchSQL.append(" or tdl.`environment` like ?");
            searchSQL.append(" or tdl.`country` like ?) ");
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
        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement();) {

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
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
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
            } catch (SQLException e) {
                LOG.warn("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                        e.toString());
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
