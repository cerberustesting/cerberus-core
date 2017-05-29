/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.service.sql.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.service.ICountryEnvironmentDatabaseService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class SQLService implements ISQLService {

    @Autowired
    private ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService;
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private ITestDataLibDataService testDataLibDataService;
    @Autowired
    private IParameterService parameterService;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SQLService.class);

    @Override
    public TestCaseExecutionData calculateOnDatabase(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseProperties,
            TestCaseExecution tCExecution) {
        String sql = testCaseExecutionData.getValue1();
        String db = testCaseProperties.getDatabase();

        String connectionName;
        CountryEnvironmentDatabase countryEnvironmentDatabase;
        MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL);

        try {
            String system = tCExecution.getApplicationObj().getSystem();
            String country = testCaseProperties.getCountry();
            String environment = tCExecution.getEnvironmentData();
            countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system, country, environment, db));

            if (countryEnvironmentDatabase == null) {
                mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_DATABASENOTCONFIGURED);
                mes.setDescription(mes.getDescription().replace("%SYSTEM%", system).replace("%COUNTRY%", country).replace("%ENV%", environment).replace("%DATABASE%", db));

            } else {

                connectionName = countryEnvironmentDatabase.getConnectionPoolName();

                if (!(StringUtil.isNullOrEmpty(connectionName))) {
                    try {
                        Integer sqlTimeout = parameterService.getParameterIntegerByKey("cerberus_propertyexternalsql_timeout", system, 60);
                        List<String> list = this.queryDatabase(connectionName, sql, testCaseProperties.getRowLimit(), sqlTimeout);

                        if (list != null && !list.isEmpty()) {
                            if (testCaseProperties.getNature().equalsIgnoreCase(TestCaseCountryProperties.NATURE_STATIC)) {
                                testCaseExecutionData.setValue(list.get(0));

                            } else if (testCaseProperties.getNature().equalsIgnoreCase(TestCaseCountryProperties.NATURE_RANDOM)) {
                                testCaseExecutionData.setValue(this.getRandomStringFromList(list));
                                mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_RANDOM);

                            } else if (testCaseProperties.getNature().equalsIgnoreCase(TestCaseCountryProperties.NATURE_RANDOMNEW)) {
                                testCaseExecutionData.setValue(this.calculateNatureRandomNew(list, testCaseProperties.getProperty(), tCExecution));
                                mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NATURERANDOMNEW_NOTIMPLEMENTED);

                            } else if (testCaseProperties.getNature().equalsIgnoreCase(TestCaseCountryProperties.NATURE_NOTINUSE)) {
                                mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NATURENOTINUSE_NOTIMPLEMENTED);

                            }
                        } else {
                            mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NODATA);
                        }
                        mes.setDescription(mes.getDescription().replace("%DATABASE%", db));
                        mes.setDescription(mes.getDescription().replace("%SQL%", sql));
                        mes.setDescription(mes.getDescription().replace("%JDBCPOOLNAME%", connectionName));
                        testCaseExecutionData.setPropertyResultMessage(mes);

                    } catch (CerberusEventException ex) {
                        mes = ex.getMessageError();
                    }
                } else {
                    mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_EMPTYJDBCPOOL);
                    mes.setDescription(mes.getDescription().replace("%SYSTEM%", tCExecution.getApplicationObj().getSystem()));
                    mes.setDescription(mes.getDescription().replace("%COUNTRY%", testCaseProperties.getCountry()));
                    mes.setDescription(mes.getDescription().replace("%ENV%", tCExecution.getEnvironmentData()));
                    mes.setDescription(mes.getDescription().replace("%DATABASE%", db));
                }
            }
        } catch (CerberusException ex) {
            mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_JDBCPOOLNOTCONFIGURED);
            mes.setDescription(mes.getDescription().replace("%SYSTEM%", tCExecution.getApplicationObj().getSystem()));
            mes.setDescription(mes.getDescription().replace("%COUNTRY%", testCaseProperties.getCountry()));
            mes.setDescription(mes.getDescription().replace("%ENV%", tCExecution.getEnvironmentData()));
            mes.setDescription(mes.getDescription().replace("%DATABASE%", db));
        }

        testCaseExecutionData.setPropertyResultMessage(mes);
        return testCaseExecutionData;
    }

    private String getRandomStringFromList(List<String> list) {
        Random random = new Random();
        if (!list.isEmpty()) {
            return list.get(random.nextInt(list.size()));
        }
        return null;
    }

    private String calculateNatureRandomNew(List<String> list, String propName, TestCaseExecution tCExecution) {
        //TODO clean code
        List<String> pastValues = this.testCaseExecutionDataService.getPastValuesOfProperty(tCExecution.getId(), propName, tCExecution.getTest(),
                tCExecution.getTestCase(), tCExecution.getCountryEnvParam().getBuild(), tCExecution.getEnvironmentData(),
                tCExecution.getCountry());

        if (!pastValues.isEmpty()) {
            for (String value : list) {
                if (!pastValues.contains(value)) {
                    return value;
                }
            }
        } else {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<String> queryDatabase(String connectionName, String sql, int limit, int defaultTimeOut) throws CerberusEventException {
        List<String> list = null;
        boolean throwEx = false;
        int maxSecurityFetch = 100;
        int nbFetch = 0;
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_GENERIC);
        msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));

        Connection connection = this.databaseSpring.connect(connectionName);
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            preStat.setQueryTimeout(defaultTimeOut);
            if (limit > 0 && limit < maxSecurityFetch) {
                preStat.setMaxRows(limit);
            } else {
                preStat.setMaxRows(maxSecurityFetch);
            }
            //TODO add limit of select
            /*
             ORACLE      => * WHERE ROWNUM <= limit *
             DB2         => * FETCH FIRST limit ROWS ONLY
             MYSQL       => * LIMIT 0, limit
             SQL SERVER  => SELECT TOP limit *
             SYBASE      => SET ROWCOUNT limit *
             if (limit > 0) {
             sql.concat(Util.DbLimit(databaseType, limit));
             }
             */
            try {
                LOG.info("Sending to external Database (queryDatabase) : '" + connectionName + "' SQL '" + sql + "'");
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<String>();
                try {
                    while ((resultSet.next()) && (nbFetch < maxSecurityFetch)) {
                        list.add(resultSet.getString(1));
                        nbFetch++;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(SQLService.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());

                } finally {
                    resultSet.close();
                }
            } catch (SQLTimeoutException exception) {
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_TIMEOUT);
                msg.setDescription(msg.getDescription().replace("%SQL%", sql));
                msg.setDescription(msg.getDescription().replace("%TIMEOUT%", String.valueOf(defaultTimeOut)));
                msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));

            } catch (SQLException exception) {
                MyLogger.log(SQLService.class.getName(), Level.WARN, exception.toString());
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_ERROR);
                msg.setDescription(msg.getDescription().replace("%SQL%", sql));
                msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
            throwEx = true;
        } catch (NullPointerException exception) {
            //TODO check where exception occur
            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
            throwEx = true;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SQLService.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusEventException(msg);
        }
        return list;
    }

    @Override
    public MessageEvent executeUpdate(String system, String country, String environment, String database, String sql) {
        String connectionName;
        CountryEnvironmentDatabase countryEnvironmentDatabase;
        MessageEvent msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);

        try {
            countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system,
                    country, environment, database));
            if (countryEnvironmentDatabase != null) {
                connectionName = countryEnvironmentDatabase.getConnectionPoolName();
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_GENERIC);
                msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));

                if (!(StringUtil.isNullOrEmpty(connectionName))) {
                    if (connectionName.equals("cerberus" + System.getProperty("org.cerberus.environment"))) {
                        return new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_AGAINST_CERBERUS);
                    } else {

                        Connection connection = this.databaseSpring.connect(connectionName);
                        try {
                            PreparedStatement preStat = connection.prepareStatement(sql);
                            Integer sqlTimeout = parameterService.getParameterIntegerByKey("cerberus_actionexecutesqlupdate_timeout", system, 60);
                            preStat.setQueryTimeout(sqlTimeout);

                            try {
                                LOG.info("Sending to external Database (executeUpdate) : '" + connectionName + "' SQL '" + sql + "'");
                                preStat.executeUpdate();
                                int nbUpdate = preStat.getUpdateCount();
                                msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_EXECUTESQLUPDATE)
                                        .resolveDescription("NBROWS", String.valueOf(nbUpdate))
                                        .resolveDescription("JDBC", connectionName).resolveDescription("SQL", sql);
                            } catch (SQLTimeoutException exception) {
                                MyLogger.log(SQLService.class.getName(), Level.WARN, exception.toString());
                                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_TIMEOUT);
                                msg.setDescription(msg.getDescription().replace("%SQL%", sql));
                                msg.setDescription(msg.getDescription().replace("%TIMEOUT%", String.valueOf(sqlTimeout)));
                                msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
                            } catch (SQLException exception) {
                                MyLogger.log(SQLService.class.getName(), Level.WARN, exception.toString());
                                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_ERROR);
                                msg.setDescription(msg.getDescription().replace("%SQL%", sql));
                                msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
                            } finally {
                                preStat.close();
                            }
                        } catch (SQLException exception) {
                            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
                            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_CANNOTACCESSJDBC);
                            msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));
                            msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
                        } catch (NullPointerException exception) {
                            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
                            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_CANNOTACCESSJDBC);
                            msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));
                            msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
                        } finally {
                            try {
                                if (connection != null) {
                                    connection.close();
                                }
                            } catch (SQLException e) {
                                MyLogger.log(SQLService.class.getName(), Level.WARN, e.toString());
                            }
                        }
                    }
                } else {
                    msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_DATABASECONFIGUREDBUTJDBCPOOLEMPTY);
                    msg.setDescription(msg.getDescription().replace("%SYSTEM%", system));
                    msg.setDescription(msg.getDescription().replace("%COUNTRY%", country));
                    msg.setDescription(msg.getDescription().replace("%ENV%", environment));
                    msg.setDescription(msg.getDescription().replace("%DATABASE%", database));
                }
            } else {
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_DATABASENOTCONFIGURED);
                msg.setDescription(msg.getDescription().replace("%SYSTEM%", system));
                msg.setDescription(msg.getDescription().replace("%COUNTRY%", country));
                msg.setDescription(msg.getDescription().replace("%ENV%", environment));
                msg.setDescription(msg.getDescription().replace("%DATABASE%", database));
            }
        } catch (CerberusException ex) {
            LOG.error(ex.toString());
        }
        return msg;
    }

    @Override
    public MessageEvent executeCallableStatement(String system, String country, String environment, String database, String sql) {
        String connectionName;
        CountryEnvironmentDatabase countryEnvironmentDatabase;
        MessageEvent msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);

        try {
            countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system,
                    country, environment, database));
            if (countryEnvironmentDatabase != null) {
                connectionName = countryEnvironmentDatabase.getConnectionPoolName();
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_GENERIC);
                msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));

                if (!(StringUtil.isNullOrEmpty(connectionName))) {
                    if (connectionName.contains("cerberus")) {
                        return new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_AGAINST_CERBERUS);
                    } else {

                        Connection connection = this.databaseSpring.connect(connectionName);
                        try {
                            CallableStatement cs = connection.prepareCall(sql);
                            Integer sqlTimeout = parameterService.getParameterIntegerByKey("cerberus_actionexecutesqlstoredprocedure_timeout", system, 60);
                            cs.setQueryTimeout(sqlTimeout);
                            try {
                                cs.execute();
                                int nbUpdate = cs.getUpdateCount();
                                msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_EXECUTESQLSTOREDPROCEDURE)
                                        .resolveDescription("NBROWS", String.valueOf(nbUpdate))
                                        .resolveDescription("JDBC", connectionName).resolveDescription("SQL", sql);
                            } catch (SQLTimeoutException exception) {
                                MyLogger.log(SQLService.class.getName(), Level.WARN, exception.toString());
                                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_TIMEOUT);
                                msg.setDescription(msg.getDescription().replace("%SQL%", sql));
                                msg.setDescription(msg.getDescription().replace("%TIMEOUT%", String.valueOf(sqlTimeout)));
                                msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
                            } catch (SQLException exception) {
                                MyLogger.log(SQLService.class.getName(), Level.WARN, exception.toString());
                                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_ERROR);
                                msg.setDescription(msg.getDescription().replace("%SQL%", sql));
                                msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
                            } finally {
                                cs.close();
                            }
                        } catch (SQLException exception) {
                            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
                            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_CANNOTACCESSJDBC);
                            msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));
                            msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
                        } catch (NullPointerException exception) {
                            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
                            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_CANNOTACCESSJDBC);
                            msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));
                            msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
                        } finally {
                            try {
                                if (connection != null) {
                                    connection.close();
                                }
                            } catch (SQLException e) {
                                MyLogger.log(SQLService.class.getName(), Level.WARN, e.toString());
                            }
                        }
                    }
                } else {
                    msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_DATABASECONFIGUREDBUTJDBCPOOLEMPTY);
                    msg.setDescription(msg.getDescription().replace("%SYSTEM%", system));
                    msg.setDescription(msg.getDescription().replace("%COUNTRY%", country));
                    msg.setDescription(msg.getDescription().replace("%ENV%", environment));
                    msg.setDescription(msg.getDescription().replace("%DATABASE%", database));
                }
            } else {
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_DATABASENOTCONFIGURED);
                msg.setDescription(msg.getDescription().replace("%SYSTEM%", system));
                msg.setDescription(msg.getDescription().replace("%COUNTRY%", country));
                msg.setDescription(msg.getDescription().replace("%ENV%", environment));
                msg.setDescription(msg.getDescription().replace("%DATABASE%", database));
            }
        } catch (CerberusException ex) {
            LOG.error(ex.toString());
        }
        return msg;
    }

    @Override
    public AnswerList queryDatabaseNColumns(String connectionName, String sql, int rowLimit, int defaultTimeOut, String system, HashMap<String, String> columnsToGet) {
        AnswerList listResult = new AnswerList();
        List<HashMap<String, String>> list;
        int maxSecurityFetch = 100;
        try {
            String maxSecurityFetch1 = parameterService.findParameterByKey("cerberus_testdatalib_fetchmax", system).getValue();
            maxSecurityFetch = Integer.valueOf(maxSecurityFetch1);
        } catch (CerberusException ex) {
            LOG.error(ex);
        }
        int maxFetch = maxSecurityFetch;
        if (rowLimit > 0 && rowLimit < maxSecurityFetch) {
            maxFetch = rowLimit;
        } else {
            maxFetch = maxSecurityFetch;
        }
        int nbFetch = 0;
        int nbColMatch = 0;
        String error_desc = "";

        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));

        Connection connection = this.databaseSpring.connect(connectionName);
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            preStat.setQueryTimeout(defaultTimeOut);
            try {
                LOG.info("Sending to external Database (queryDatabaseNColumns) : '" + connectionName + "' SQL '" + sql + "'");
                ResultSet resultSet = preStat.executeQuery();

                int nrColumns = resultSet.getMetaData().getColumnCount();

                list = new ArrayList<HashMap<String, String>>();
                try {
                    while ((resultSet.next()) && (nbFetch < maxFetch)) {

                        nbColMatch = 0;
                        HashMap<String, String> row = new HashMap<String, String>();

                        for (Map.Entry<String, String> entry : columnsToGet.entrySet()) {
                            String column = entry.getValue();
                            String name = entry.getKey();
                            try {
                                String valueSQL = resultSet.getString(column);
                                if (valueSQL == null) { // If data is null from the database, we convert it to the static string <NULL>. 
                                    valueSQL = "<NULL>";
                                }
                                row.put(name, valueSQL); // We put the result of the subData.
                                nbColMatch++;
                            } catch (SQLException exception) {
                                if (nbFetch == 0) {
                                    if ("".equals(error_desc)) {
                                        error_desc = column;
                                    } else {
                                        error_desc = error_desc + ", " + column;
                                    }
                                }
                            }
                        }

                        list.add(row);
                        nbFetch++;

                    }
                    listResult.setDataList(list);
                    listResult.setTotalRows(list.size());

                    if (list.isEmpty()) { // No data was fetched.
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NODATA);
                    } else if (nbColMatch == 0) { // None of the columns could be match.
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NOCOLUMNMATCH);
                        msg.setDescription(msg.getDescription().replace("%BADCOLUMNS%", error_desc));
                    } else if (!("".equals(error_desc))) { // At least a column could not be parsed
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_COLUMNNOTMATCHING);
                        msg.setDescription(msg.getDescription().replace("%BADCOLUMNS%", error_desc));
                    }

                } catch (SQLTimeoutException exception) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_TIMEOUT);
                    msg.setDescription(msg.getDescription().replace("%SQL%", sql));
                    msg.setDescription(msg.getDescription().replace("%TIMEOUT%", String.valueOf(defaultTimeOut)));
                    msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));

                } catch (SQLException exception) {
                    MyLogger.log(SQLService.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLTimeoutException exception) {
                LOG.warn("TimeOut " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_TIMEOUT);
                msg.setDescription(msg.getDescription().replace("%SQL%", sql));
                msg.setDescription(msg.getDescription().replace("%TIMEOUT%", String.valueOf(defaultTimeOut)));
                msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
            } catch (SQLException exception) {
                LOG.warn(exception.toString());
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_ERROR);
                msg.setDescription(msg.getDescription().replace("%SQL%", sql));
                msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
        } catch (NullPointerException exception) {
            //TODO check where exception occur
            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replace("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replace("%EX%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SQLService.class.getName(), Level.WARN, e.toString());
            }
        }
        listResult.setResultMessage(msg);
        return listResult;
    }
}
