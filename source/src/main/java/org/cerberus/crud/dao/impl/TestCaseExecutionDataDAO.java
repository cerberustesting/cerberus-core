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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionData;
import org.cerberus.crud.factory.impl.FactoryTestCaseExecutionData;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 0.9.0
 */
@Repository
public class TestCaseExecutionDataDAO implements ITestCaseExecutionDataDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;

    private static final Logger LOG = Logger.getLogger(TestCaseExecutionDataDAO.class);

    private final String OBJECT_NAME = "TestCase Execution Data";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<TestCaseExecutionData> readByKey(long id, String property, int index) {
        AnswerItem ans = new AnswerItem();
        TestCaseExecutionData result = null;
        final String query = "SELECT * FROM testcaseexecutiondata exd WHERE id = ? AND property = ? AND `index` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + String.valueOf(id));
            LOG.debug("SQL.param.property : " + property);
            LOG.debug("SQL.param.index : " + String.valueOf(index));
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, id);
                preStat.setString(2, property);
                preStat.setInt(3, index);
                ResultSet resultSet = preStat.executeQuery();
                try {
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
                } finally {
                    resultSet.close();
                }
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

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<TestCaseExecutionData> readByIdByCriteria(long id, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseExecutionData> objectList = new ArrayList<TestCaseExecutionData>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecutiondata exd ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`Property` like ?");
            searchSQL.append(" or `description` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `Type` like ?");
            searchSQL.append(" or `Value1` like ?");
            searchSQL.append(" or `Value2` like ?");
            searchSQL.append(" or `RC` like ?");
            searchSQL.append(" or `RMessage` like ?)");
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

        if (!(id == -1)) {
            searchSQL.append(" and (`id` = ? )");
        }
        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by ").append(column).append(" ").append(dir);
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
                if (!StringUtil.isNullOrEmpty(searchTerm)) {
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
                if (!(id == -1)) {
                    preStat.setLong(i++, id);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(objectList, nrTotalRows);
                    } else if (objectList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(objectList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(objectList, nrTotalRows);
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
        response.setDataList(objectList);
        return response;
    }

    @Override
    public List<String> getPastValuesOfProperty(long id, String propName, String test, String testCase, String build, String environment, String country) {
        List<String> list = null;
        final String query = "SELECT distinct exd.`VALUE` FROM testcaseexecution exe "
                + "JOIN testcaseexecutiondata exd ON exd.Property = ? and exd.ID = exe.ID "
                + "WHERE exe.test = ? AND exe.testcase = ? AND exe.build = ? AND exe.environment = ? "
                + "AND exe.country = ? AND exe.id <> ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.property : " + propName);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testCase);
            LOG.debug("SQL.param.build : " + build);
            LOG.debug("SQL.param.environment : " + environment);
            LOG.debug("SQL.param.country : " + country);
            LOG.debug("SQL.param.id : " + id);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, propName);
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setString(4, build);
                preStat.setString(5, environment);
                preStat.setString(6, country);
                preStat.setLong(7, id);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("value"));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<String> getInUseValuesOfProperty(long id, String propName, String environment, String country, Integer timeoutInSecond) {
        List<String> list = null;
        final String query = "SELECT distinct exd.`VALUE` FROM testcaseexecution exe "
                + "JOIN testcaseexecutiondata exd ON exd.Property = ? and exd.ID = exe.ID "
                + "WHERE exe.environment = ? AND exe.country = ? AND exe.ControlSTATUS = 'PE' "
                + "AND TO_SECONDS(NOW()) - TO_SECONDS(exe.start) < ? AND exe.ID <> ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param : " + propName);
            LOG.debug("SQL.param : " + environment);
            LOG.debug("SQL.param : " + country);
            LOG.debug("SQL.param : " + String.valueOf(timeoutInSecond));
            LOG.debug("SQL.param : " + id);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, propName);
                preStat.setString(2, environment);
                preStat.setString(3, country);
                preStat.setInt(4, timeoutInSecond);
                preStat.setLong(5, id);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("value"));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public Answer create(TestCaseExecutionData object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcaseexecutiondata (`id`, `property`, `index`, `description`, `value`, `type`, `value1`, `value2`, `rc`, ");
        query.append("`rmessage`, `start`, `end`, `startlong`, `endlong`, `database`, `value1Init`, `value2Init`, `length`, `rowLimit`, `nature`, `retrynb`, `retryperiod`) ");
        query.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + object.getId());
            LOG.debug("SQL.param.property : " + object.getProperty());
            LOG.debug("SQL.param.index : " + object.getIndex());
            LOG.debug("SQL.param.value : " + ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue(), 65000), object.getProperty()));
            LOG.debug("SQL.param.value1 : " + ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue1(), 65000), object.getProperty()));
            LOG.debug("SQL.param.value2 : " + ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue2(), 65000), object.getProperty()));
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                int i = 1;
                preStat.setLong(i++, object.getId());
                preStat.setString(i++, object.getProperty());
                preStat.setInt(i++, object.getIndex());
                preStat.setString(i++, object.getDescription());
                preStat.setString(i++, ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue(), 65000), object.getProperty()));
                preStat.setString(i++, object.getType());
                preStat.setString(i++, ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue1(), 65000), object.getProperty()));
                preStat.setString(i++, ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue2(), 65000), object.getProperty()));
                preStat.setString(i++, object.getRC());
                preStat.setString(i++, object.getrMessage());
                preStat.setTimestamp(i++, new Timestamp(object.getStart()));
                preStat.setTimestamp(i++, new Timestamp(object.getEnd()));
                preStat.setString(i++, df.format(object.getStart()));
                preStat.setString(i++, df.format(object.getEnd()));
                preStat.setString(i++, object.getDatabase());
                preStat.setString(i++, object.getValue1Init());
                preStat.setString(i++, object.getValue2Init());
                preStat.setInt(i++, object.getLength());
                preStat.setInt(i++, object.getRowLimit());
                preStat.setString(i++, object.getNature());
                preStat.setInt(i++, object.getRetryNb());
                preStat.setInt(i++, object.getRetryPeriod());

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
                LOG.error("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(TestCaseExecutionData object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM testcaseexecutiondata WHERE id = ? AND property = ? AND `index` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + String.valueOf(object.getId()));
            LOG.debug("SQL.param.property : " + object.getProperty());
            LOG.debug("SQL.param.index : " + String.valueOf(object.getIndex()));
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, object.getId());
                preStat.setString(2, object.getProperty());
                preStat.setInt(3, object.getIndex());

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
    public Answer update(TestCaseExecutionData object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();

        query.append("UPDATE testcaseexecutiondata SET DESCRIPTION = ?, VALUE = ?, TYPE = ?, VALUE1 = ?, VALUE2 = ?, rc = ?, rmessage = ?, start = ?, ");
        query.append("END = ?, startlong = ?, endlong = ?, `database` = ?, `value1Init` = ?, `value2Init` = ?, ");
        query.append("`length` = ?, `rowLimit` = ?, `nature` = ?, `retrynb` = ?, `retryperiod` = ? ");
        query.append("WHERE id = ? AND property = ? AND `index` = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.id : " + object.getId());
            LOG.debug("SQL.param.property : " + object.getProperty());
            LOG.debug("SQL.param.index : " + object.getIndex());
            LOG.debug("SQL.param.value : " + ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue(), 65000), object.getProperty()));
            LOG.debug("SQL.param.value1 : " + ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue1(), 65000), object.getProperty()));
            LOG.debug("SQL.param.value2 : " + ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue2(), 65000), object.getProperty()));
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                int i = 1;
                preStat.setString(i++, object.getDescription());
                preStat.setString(i++, ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue(), 65000), object.getProperty()));
                preStat.setString(i++, object.getType());
                preStat.setString(i++, ParameterParserUtil.securePassword(StringUtil.getLeftString(object.getValue1(), 65000), object.getProperty()));
                preStat.setString(i++, StringUtil.getLeftString(object.getValue2(), 65000));
                preStat.setString(i++, object.getRC());
                preStat.setString(i++, StringUtil.getLeftString(object.getrMessage(), 65000));
                preStat.setTimestamp(i++, new Timestamp(object.getStart()));
                preStat.setTimestamp(i++, new Timestamp(object.getEnd()));
                preStat.setString(i++, df.format(object.getStart()));
                preStat.setString(i++, df.format(object.getEnd()));
                preStat.setString(i++, object.getDatabase());
                preStat.setString(i++, object.getValue1Init());
                preStat.setString(i++, object.getValue2Init());
                preStat.setInt(i++, object.getLength());
                preStat.setInt(i++, object.getRowLimit());
                preStat.setString(i++, object.getNature());
                preStat.setInt(i++, object.getRetryNb());
                preStat.setInt(i++, object.getRetryPeriod());
                preStat.setLong(i++, object.getId());
                preStat.setString(i++, object.getProperty());
                preStat.setInt(i++, object.getIndex());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
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
    public TestCaseExecutionData loadFromResultSet(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("exd.id");
        String property = resultSet.getString("exd.property");
        int index = resultSet.getInt("exd.index");
        String description = resultSet.getString("exd.description");
        String value = resultSet.getString("exd.value");
        String type = resultSet.getString("exd.type");
        String value1 = resultSet.getString("exd.value1");
        String value2 = resultSet.getString("exd.value2");
        String value1Init = resultSet.getString("exd.value1Init");
        String value2Init = resultSet.getString("exd.value2Init");
        String returnCode = resultSet.getString("exd.rc");
        String returnMessage = resultSet.getString("exd.rmessage");
        long start = resultSet.getTimestamp("exd.start").getTime();
        long end = resultSet.getTimestamp("exd.end").getTime();
        long startLong = resultSet.getLong("exd.startlong");
        long endLong = resultSet.getLong("exd.endlong");
        int length = resultSet.getInt("exd.length");
        int rowLimit = resultSet.getInt("exd.rowlimit");
        String nature = resultSet.getString("exd.nature");
        String database = resultSet.getString("exd.database");
        int retryNb = resultSet.getInt("exd.RetryNb");
        int retryPeriod = resultSet.getInt("exd.RetryPeriod");

        factoryTestCaseExecutionData = new FactoryTestCaseExecutionData();
        return factoryTestCaseExecutionData.create(id, property, index, description, value, type, value1, value2, returnCode, returnMessage,
                start, end, startLong, endLong, null, retryNb, retryPeriod, database, value1Init, value2Init, length, rowLimit, nature);
    }

}
