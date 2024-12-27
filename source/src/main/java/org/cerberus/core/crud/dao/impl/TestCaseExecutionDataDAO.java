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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.core.crud.utils.RequestDbUtils;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.crud.entity.TestCaseExecutionData;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionData;
import org.cerberus.core.crud.factory.impl.FactoryTestCaseExecutionData;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.DateUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
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

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionDataDAO.class);

    private final String OBJECT_NAME = "TestCase Execution Data";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public TestCaseExecutionData readByKey(long id, String property, int index) throws CerberusException {
        final String query = "SELECT * FROM testcaseexecutiondata exd WHERE id = ? AND property = ? AND `index` = ?";

        return RequestDbUtils.executeQuery(databaseSpring, query,
                ps -> {
                    int idx = 1;
                    ps.setLong(idx++, id);
                    ps.setString(idx++, property);
                    ps.setInt(idx++, index);
                },
                rs -> loadFromResultSet(rs)
        );

    }

    @Override
    public List<TestCaseExecutionData> readByIdByCriteria(long id, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecutiondata exd ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`Property` like ?");
            searchSQL.append(" or `description` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `Type` like ?");
            searchSQL.append(" or `Value1` like ?");
            searchSQL.append(" or `Value2` like ?");
            searchSQL.append(" or `Value3` like ?");
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

        if (!StringUtil.isEmptyOrNull(column)) {
            query.append(" order by ").append(column).append(" ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        return RequestDbUtils.executeQueryList(databaseSpring, query.toString(),
                ps -> {
                    int i = 1;
                    if (!StringUtil.isEmptyOrNull(searchTerm)) {
                        ps.setString(i++, "%" + searchTerm + "%");
                        ps.setString(i++, "%" + searchTerm + "%");
                        ps.setString(i++, "%" + searchTerm + "%");
                        ps.setString(i++, "%" + searchTerm + "%");
                        ps.setString(i++, "%" + searchTerm + "%");
                        ps.setString(i++, "%" + searchTerm + "%");
                        ps.setString(i++, "%" + searchTerm + "%");
                        ps.setString(i++, "%" + searchTerm + "%");
                        ps.setString(i++, "%" + searchTerm + "%");
                    }
                    for (String individualColumnSearchValue : individalColumnSearchValues) {
                        ps.setString(i++, individualColumnSearchValue);
                    }
                    if (!(id == -1)) {
                        ps.setLong(i++, id);
                    }
                },
                rs -> this.loadFromResultSet(rs)
        );

    }

    @Override
    public TestCaseExecutionData readLastCacheEntry(String system, String environment, String country, String property, int cacheExpire) throws CerberusException {
        final String query = "select * from testcaseexecutiondata exd WHERE `System`=? and Environment=? and Country=? and FromCache='N' and Property=? and Start >= NOW()- INTERVAL ? SECOND and `index`=1 and jsonResult is not null and RC = 'OK' order by id desc;";

        return RequestDbUtils.executeQuery(databaseSpring, query,
                ps -> {
                    int i = 1;
                    ps.setString(i++, system);
                    ps.setString(i++, environment);
                    ps.setString(i++, country);
                    ps.setString(i++, property);
                    ps.setInt(i++, cacheExpire);
                },
                rs -> loadFromResultSet(rs)
        );
    }

    @Override
    public List<String> getPastValuesOfProperty(long id, String propName, String test, String testCase, String build, String environment, String country) throws CerberusException {
        List<String> list = null;
        final String query = "SELECT distinct exd.`VALUE` FROM testcaseexecution exe "
                + "JOIN testcaseexecutiondata exd ON exd.Property = ? and exd.ID = exe.ID "
                + "WHERE exe.test = ? AND exe.testcase = ? AND exe.build = ? AND exe.environment = ? "
                + "AND exe.country = ? AND exe.id <> ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL.param.property : " + propName);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testCase);
            LOG.debug("SQL.param.build : " + build);
            LOG.debug("SQL.param.environment : " + environment);
            LOG.debug("SQL.param.country : " + country);
            LOG.debug("SQL.param.id : " + id);
        }

        return RequestDbUtils.executeQueryList(databaseSpring, query,
                ps -> {
                    int i = 1;
                    ps.setString(i++, propName);
                    ps.setString(i++, test);
                    ps.setString(i++, testCase);
                    ps.setString(i++, build);
                    ps.setString(i++, environment);
                    ps.setString(i++, country);
                    ps.setLong(i++, id);
                },
                rs -> rs.getString("value")
        );
    }

    @Override
    public List<String> getInUseValuesOfProperty(long id, String propName, String environment, String country, Integer timeoutInSecond) throws CerberusException {
        final String query = "SELECT distinct exd.`VALUE` FROM testcaseexecution exe "
                + "JOIN testcaseexecutiondata exd ON exd.Property = ? and exd.ID = exe.ID "
                + "WHERE exe.environment = ? AND exe.country = ? AND exe.ControlSTATUS = 'PE' "
                + "AND TO_SECONDS(NOW()) - TO_SECONDS(exe.start) < ? AND exe.ID <> ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL.param : " + propName);
            LOG.debug("SQL.param : " + environment);
            LOG.debug("SQL.param : " + country);
            LOG.debug("SQL.param : " + String.valueOf(timeoutInSecond));
            LOG.debug("SQL.param : " + id);
        }

        return RequestDbUtils.executeQueryList(databaseSpring, query,
                ps -> {
                    int i = 1;
                    ps.setString(i++, propName);
                    ps.setString(i++, environment);
                    ps.setString(i++, country);
                    ps.setInt(i++, timeoutInSecond);
                    ps.setLong(i++, id);
                },
                rs -> rs.getString("value")
        );

    }

    @Override
    public void create(TestCaseExecutionData object, HashMap<String, String> secrets) throws CerberusException {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcaseexecutiondata (`id`, `property`, `index`, `description`, `value`, `type`, `rank`, `value1`, `value2`,`value3`, `rc`, ");
        query.append("`rmessage`, `start`, `end`, `startlong`, `endlong`, `database`, `value1Init`,`value2Init`,`value3Init`,`lengthInit`,`length`, `rowLimit`, `nature`, `retrynb`, `retryperiod`, ");
        query.append("`system`, `environment`, `country`, `dataLib`, `jsonResult`, `FromCache`) ");
        query.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL.param.id : " + object.getId());
            LOG.debug("SQL.param.property : " + object.getProperty());
            LOG.debug("SQL.param.index : " + object.getIndex());
            LOG.debug("SQL.param.value : " + StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue(), 65000), secrets));
            LOG.debug("SQL.param.value1 : " + StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue1(), 65000), secrets));
            LOG.debug("SQL.param.value2 : " + StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue2(), 65000), secrets));
            LOG.debug("SQL.param.value3 : " + StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue3(), 65000), secrets));
        }

        RequestDbUtils.executeUpdate(databaseSpring, query.toString(),
                ps -> {
                    DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                    int i = 1;
                    ps.setLong(i++, object.getId());
                    ps.setString(i++, object.getProperty());
                    ps.setInt(i++, object.getIndex());
                    ps.setString(i++, object.getDescription());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue(), 65000), secrets));
                    ps.setString(i++, object.getType());
                    ps.setInt(i++, object.getRank());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue1(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue2(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue3(), 65000), secrets));
                    ps.setString(i++, object.getRC());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getrMessage(), 65000), secrets));
                    ps.setTimestamp(i++, new Timestamp(object.getStart()));
                    ps.setTimestamp(i++, new Timestamp(object.getEnd()));
                    ps.setString(i++, df.format(object.getStart()));
                    ps.setString(i++, df.format(object.getEnd()));
                    ps.setString(i++, object.getDatabase());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue1Init(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue2Init(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue3Init(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(object.getLengthInit(), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(object.getLength(), secrets));
                    ps.setInt(i++, object.getRowLimit());
                    ps.setString(i++, object.getNature());
                    ps.setInt(i++, object.getRetryNb());
                    ps.setInt(i++, object.getRetryPeriod());
                    ps.setString(i++, object.getSystem());
                    ps.setString(i++, object.getEnvironment());
                    ps.setString(i++, object.getCountry());
                    ps.setString(i++, object.getDataLib());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getJsonResult(), 65000), secrets));
                    ps.setString(i++, object.getFromCache());
                }
        );

    }

    @Override
    public void delete(TestCaseExecutionData object) throws CerberusException {
        MessageEvent msg = null;
        final String query = "DELETE FROM testcaseexecutiondata WHERE id = ? AND property = ? AND `index` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL.param.id : " + String.valueOf(object.getId()));
            LOG.debug("SQL.param.property : " + object.getProperty());
            LOG.debug("SQL.param.index : " + String.valueOf(object.getIndex()));
        }

        RequestDbUtils.executeUpdate(databaseSpring, query,
                ps -> {
                    int i = 1;
                    ps.setLong(i++, object.getId());
                    ps.setString(i++, object.getProperty());
                    ps.setInt(i++, object.getIndex());
                }
        );
    }

    @Override
    public void update(TestCaseExecutionData object, HashMap<String, String> secrets) throws CerberusException {
        StringBuilder query = new StringBuilder();

        query.append("UPDATE testcaseexecutiondata SET DESCRIPTION = ?, VALUE = ?, TYPE = ?, `Rank` = ?, VALUE1 = ?, VALUE2 = ?, VALUE3 = ?, rc = ?, rmessage = ?, start = ?, ");
        query.append("END = ?, startlong = ?, endlong = ?, `database` = ?, `value1Init` = ?, `value2Init` = ?,`value3Init` = ?, ");
        query.append("`lengthInit` = ?, `length` = ?, `rowLimit` = ?, `nature` = ?, `retrynb` = ?, `retryperiod` = ?, ");
        query.append("`system` = ?, `environment` = ?, `country` = ?, `dataLib` = ?, `jsonResult` = ? , `FromCache` = ? ");
        query.append("WHERE id = ? AND property = ? AND `index` = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL.param.id : " + object.getId());
            LOG.debug("SQL.param.property : " + object.getProperty());
            LOG.debug("SQL.param.index : " + object.getIndex());
            LOG.debug("SQL.param.rank : " + object.getRank());
            LOG.debug("SQL.param.rowLimit : " + object.getRowLimit());
            LOG.debug("SQL.param.retrynb : " + object.getRetryNb());
            LOG.debug("SQL.param.retryperiod : " + object.getRetryPeriod());
            LOG.debug("SQL.param.value : " + StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue(), 65000), secrets));
            LOG.debug("SQL.param.value1 : " + StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue1(), 65000), secrets));
            LOG.debug("SQL.param.value2 : " + StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue2(), 65000), secrets));
            LOG.debug("SQL.param.value3 : " + StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue3(), 65000), secrets));
        }

        RequestDbUtils.executeUpdate(databaseSpring, query.toString(),
                ps -> {
                    DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                    int i = 1;
                    ps.setString(i++, object.getDescription());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue(), 65000), secrets));
                    ps.setString(i++, object.getType());
                    ps.setInt(i++, object.getRank());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue1(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue2(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue3(), 65000), secrets));
                    ps.setString(i++, object.getRC());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getrMessage(), 65000), secrets));
                    ps.setTimestamp(i++, new Timestamp(object.getStart()));
                    ps.setTimestamp(i++, new Timestamp(object.getEnd()));
                    ps.setString(i++, df.format(object.getStart()));
                    ps.setString(i++, df.format(object.getEnd()));
                    ps.setString(i++, object.getDatabase());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue1Init(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue2Init(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getValue3Init(), 65000), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(object.getLengthInit(), secrets));
                    ps.setString(i++, StringUtil.secureFromSecrets(object.getLength(), secrets));
                    ps.setInt(i++, object.getRowLimit());
                    ps.setString(i++, object.getNature());
                    ps.setInt(i++, object.getRetryNb());
                    ps.setInt(i++, object.getRetryPeriod());
                    ps.setString(i++, object.getSystem());
                    ps.setString(i++, object.getEnvironment());
                    ps.setString(i++, object.getCountry());
                    ps.setString(i++, object.getDataLib());
                    ps.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(object.getJsonResult(), 65000), secrets));
                    ps.setString(i++, object.getFromCache());
                    ps.setLong(i++, object.getId());
                    ps.setString(i++, object.getProperty());
                    ps.setInt(i++, object.getIndex());
                }
        );

    }

    @Override
    public List<TestCaseExecutionData> readTestCaseExecutionDataFromDependencies(TestCaseExecution tce) throws CerberusException {
        List<TestCaseExecutionQueueDep> testCaseDep = tce.getTestCaseExecutionQueueDepList();

        String query = "SELECT exd.*"
                + " FROM testcaseexecutionqueue exq"
                + " inner join testcaseexecutionqueuedep eqd on eqd.ExeQueueID = exq.ID"
                + " inner join testcaseexecutiondata exd on eqd.ExeID = exd.ID"
                + " WHERE exq.ExeID=? and exd.index=1";

        return RequestDbUtils.executeQueryList(databaseSpring, query,
                ps -> {
                    int i = 1;
                    ps.setLong(i++, tce.getId());
                },
                rs -> loadFromResultSet(rs)
        );
    }

    private TestCaseExecutionData loadFromResultSet(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("exd.id");
        String property = resultSet.getString("exd.property");
        int index = resultSet.getInt("exd.index");
        String description = resultSet.getString("exd.description");
        String value = resultSet.getString("exd.value");
        String type = resultSet.getString("exd.type");
        int rank = resultSet.getInt("exd.rank");
        String value1 = resultSet.getString("exd.value1");
        String value2 = resultSet.getString("exd.value2");
        String value3 = resultSet.getString("exd.value3");
        String value1Init = resultSet.getString("exd.value1Init");
        String value2Init = resultSet.getString("exd.value2Init");
        String value3Init = resultSet.getString("exd.value3Init");
        String returnCode = resultSet.getString("exd.rc");
        String returnMessage = resultSet.getString("exd.rmessage");
        long start = resultSet.getTimestamp("exd.start").getTime();
        long end = resultSet.getTimestamp("exd.end").getTime();
        long startLong = resultSet.getLong("exd.startlong");
        long endLong = resultSet.getLong("exd.endlong");
        String lengthInit = resultSet.getString("exd.lengthInit");
        String length = resultSet.getString("exd.length");
        int rowLimit = resultSet.getInt("exd.rowlimit");
        String nature = resultSet.getString("exd.nature");
        String database = resultSet.getString("exd.database");
        int retryNb = resultSet.getInt("exd.RetryNb");
        int retryPeriod = resultSet.getInt("exd.RetryPeriod");
        String system = resultSet.getString("exd.system");
        String environment = resultSet.getString("exd.environment");
        String country = resultSet.getString("exd.country");
        String dataLib = resultSet.getString("exd.dataLib");
        String jsonResult = resultSet.getString("exd.jsonResult");
        String fromCache = resultSet.getString("exd.FromCache");

        factoryTestCaseExecutionData = new FactoryTestCaseExecutionData();
        return factoryTestCaseExecutionData.create(id, property, index, description, value, type, rank, value1, value2, value3, returnCode, returnMessage,
                start, end, startLong, endLong, null, retryNb, retryPeriod, database, value1Init, value2Init, value3Init, lengthInit, length, rowLimit, nature,
                system, environment, country, dataLib, jsonResult, fromCache);
    }

}
