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
package org.cerberus.service.sql.impl;

import org.cerberus.engine.gwt.impl.PropertyService;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Property;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.service.ICountryEnvironmentDatabaseService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
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
    private ITestCaseExecutionDataDAO testCaseExecutionDataDAO;
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
            countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(tCExecution.getApplication().getSystem(),
                    testCaseProperties.getCountry(), tCExecution.getEnvironmentData(), db));
            connectionName = countryEnvironmentDatabase.getConnectionPoolName();

            if (!(StringUtil.isNullOrEmpty(connectionName))) {
                try {
                    List<String> list = this.queryDatabase(connectionName, sql, testCaseProperties.getRowLimit());

                    if (list != null && !list.isEmpty()) {
                        if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_STATIC)) {
                            testCaseExecutionData.setValue(list.get(0));

                        } else if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_RANDOM)) {
                            testCaseExecutionData.setValue(this.getRandomStringFromList(list));
                            mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_RANDOM);

                        } else if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_RANDOMNEW)) {
                            testCaseExecutionData.setValue(this.calculateNatureRandomNew(list, testCaseProperties.getProperty(), tCExecution));
                            mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_RANDOM_NEW);

                        } else if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_NOTINUSE)) {
                            testCaseExecutionData.setValue(this.calculateNatureNotInUse(list, testCaseProperties.getProperty(), tCExecution));
                            mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_NOTINUSE);

                        }
                    } else {
                        mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NODATA);
                    }
                    mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                    mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                    mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                    testCaseExecutionData.setPropertyResultMessage(mes);

                } catch (CerberusEventException ex) {
                    mes = ex.getMessageError();
                }

            } else {
                mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_EMPTYJDBCPOOL);
                mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", tCExecution.getApplication().getSystem()));
                mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", testCaseProperties.getCountry()));
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
                mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
            }
        } catch (CerberusException ex) {
            mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_JDBCPOOLNOTCONFIGURED);
            mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", tCExecution.getApplication().getSystem()));
            mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", testCaseProperties.getCountry()));
            mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
        }

        testCaseExecutionData.setPropertyResultMessage(mes);
        return testCaseExecutionData;
    }

    @Override
    public AnswerItem<HashMap<String, String>> calculateOnDatabaseNColumns(String sql, String db, String system, String country, String environment, TestCaseCountryProperties testCaseCountryProperty, String keyColumn, TestCaseExecution tCExecution, Integer dataLibID) {
        AnswerItem answer = new AnswerItem();
        String connectionName;
        CountryEnvironmentDatabase countryEnvironmentDatabase;
        MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL);
        List<HashMap<String, String>> list;
        int rowLimit = testCaseCountryProperty.getRowLimit();

        try {

            if (StringUtil.isNullOrEmpty(db)) {
                mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_DATABASEEMPTY);

            } else {

                countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system,
                        country, environment, db));
                if (countryEnvironmentDatabase == null) {
                    mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_DATABASENOTCONFIGURED);
                    mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", system).replaceAll("%COUNTRY%", country).replaceAll("%ENV%", environment).replaceAll("%DATABASE%", db));

                } else {

                    connectionName = countryEnvironmentDatabase.getConnectionPoolName();

                    if (!(StringUtil.isNullOrEmpty(connectionName))) {
                        if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_STATIC)) { // If Nature of the property is static, we don't need to getch more than 1 record.
                            rowLimit = 1;
                        }

                        // Gets the list of colomns to get from TestDataLibData.
                        AnswerList answerData = new AnswerList();
                        answerData = testDataLibDataService.readByVarious(dataLibID, "N", null);
                        List<TestDataLibData> objectDataList = new ArrayList<TestDataLibData>();
                        objectDataList = answerData.getDataList();
                        HashMap<String, String> row = new HashMap<String, String>();
                        for (TestDataLibData tdld : objectDataList) {
                            row.put(tdld.getColumn(), tdld.getSubData());
                        }

                        //performs a query that returns several rows containing n columns
                        AnswerList responseList = this.queryDatabaseNColumns(connectionName, sql, rowLimit, system, row);

                        //if the query returns sucess then we can get the data
                        if (responseList.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_SQL.getCode()) {
                            list = responseList.getDataList();
                            if (list != null && !list.isEmpty()) {

                                if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_STATIC)) {
                                    answer.setItem((list.get(0)));
                                    mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SQL_STATIC);

                                } else if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_RANDOM)) {
                                    Random r = new Random();
                                    int position = r.nextInt(list.size());
                                    answer.setItem(list.get(position));
                                    mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SQL_RANDOM);
                                    mes.setDescription(mes.getDescription().replaceAll("%POS%", Integer.toString(position)).replaceAll("%TOTALPOS%", Integer.toString(list.size())));

                                } else if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_RANDOMNEW)) {

                                    int initNB = list.size();
                                    // We get the list of values that are already used.
                                    List<String> pastValues = this.testCaseExecutionDataDAO.getPastValuesOfProperty(testCaseCountryProperty.getProperty(), tCExecution.getTest(),
                                            tCExecution.getTestCase(), tCExecution.getCountryEnvParam().getBuild(), tCExecution.getEnvironmentData(),
                                            tCExecution.getCountry());

                                    int removedNB = 0;
                                    // We save all rows that needs to be removed to listToremove.
                                    List<Map<String, String>> listToremove = new ArrayList<Map<String, String>>();
                                    for (String valueToRemove : pastValues) {
                                        for (Map<String, String> curentRow : list) {
                                            if (curentRow.get("").equals(valueToRemove)) {
                                                if (true) {
                                                    listToremove.add(curentRow);
                                                    removedNB++;
                                                }
                                            }
                                        }
                                    }
                                    // We remove all listToremove entries from list.
                                    list.removeAll(listToremove);

                                    if (list != null && !list.isEmpty()) { // We pick a random value from the left entries of the list.
                                        Random r = new Random();
                                        int position = r.nextInt(list.size());
                                        answer.setItem(list.get(position));
                                        mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SQL_RANDOMNEW);
                                        mes.setDescription(mes.getDescription().replaceAll("%TOTNB%", Integer.toString(initNB))
                                                .replaceAll("%REMNB%", Integer.toString(removedNB))
                                                .replaceAll("%POS%", Integer.toString(position))
                                                .replaceAll("%TOTALPOS%", Integer.toString(list.size())));
                                    } else { // No more entries available.
                                        mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_RANDOMNEW_NOMORERECORD);
                                        mes.setDescription(mes.getDescription().replaceAll("%TOTNB%", Integer.toString(initNB)));
                                    }

                                } else if (testCaseCountryProperty.getNature().equalsIgnoreCase(Property.NATURE_NOTINUSE)) {

                                    int initNB = list.size();
                                    // We get the list of values that are already used.
                                    Integer peTimeout = Integer.valueOf(parameterService.findParameterByKey("cerberus_notinuse_timeout", system).getValue());
                                    List<String> pastValues = this.testCaseExecutionDataDAO.getInUseValuesOfProperty(testCaseCountryProperty.getProperty(), tCExecution.getEnvironmentData(), tCExecution.getCountry(), peTimeout);

                                    int removedNB = 0;
                                    // We save all rows that needs to be removed to listToremove.
                                    List<Map<String, String>> listToremove = new ArrayList<Map<String, String>>();
                                    for (String valueToRemove : pastValues) {
                                        for (Map<String, String> curentRow : list) {
                                            if (curentRow.get("").equals(valueToRemove)) {
                                                if (true) {
                                                    listToremove.add(curentRow);
                                                    removedNB++;
                                                }
                                            }
                                        }
                                    }
                                    // We remove all listToremove entries from list.
                                    list.removeAll(listToremove);

                                    if (list != null && !list.isEmpty()) { // We pick a random value from the left entries of the list.
                                        Random r = new Random();
                                        int position = r.nextInt(list.size());
                                        answer.setItem(list.get(position));
                                        mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SQL_NOTINUSE);
                                        mes.setDescription(mes.getDescription().replaceAll("%TOTNB%", Integer.toString(initNB))
                                                .replaceAll("%REMNB%", Integer.toString(removedNB))
                                                .replaceAll("%POS%", Integer.toString(position))
                                                .replaceAll("%TOTALPOS%", Integer.toString(list.size())));
                                    } else { // No more entries available.
                                        mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_NOTINUSE_NOMORERECORD);
                                        mes.setDescription(mes.getDescription().replaceAll("%TOTNB%", Integer.toString(initNB)));
                                    }

                                }

                                // If the return is successfull, we convert the result to JSON and add it to the message.
                                if (!list.isEmpty()) {
                                    mes.setDescription(mes.getDescription().replaceAll("%RESULTVALUE%", answer.getItem().toString()));
                                }

                            } else {
                                mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NODATA);
                            }

                        } else {
                            mes = responseList.getResultMessage();
                        }

                        mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                        mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                        mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));

                    } else {
                        mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_JDBCRESSOURCEMPTY);
                        mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", system).replaceAll("%COUNTRY%", country).replaceAll("%ENV%", environment).replaceAll("%DATABASE%", db));
                    }
                }
            }
        } catch (CerberusException ex) {
            mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_DATABASENOTCONFIGURED);
            mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", system).replaceAll("%COUNTRY%", country).replaceAll("%ENV%", environment).replaceAll("%DATABASE%", db));
        }

        answer.setResultMessage(mes);
        return answer;
    }

    /**
     * Performs a query in the database
     *
     * @param connectionName
     * @param sql
     * @param limit
     * @return
     * @throws CerberusEventException
     */
    @Override
    public List<String> queryDatabase(String connectionName, String sql, int limit) throws CerberusEventException {
        List<String> list = null;
        boolean throwEx = false;
        int maxSecurityFetch = 100;
        int nbFetch = 0;
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_GENERIC);
        msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));

        Connection connection = this.databaseSpring.connect(connectionName);
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
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
            } catch (SQLException exception) {
                MyLogger.log(SQLService.class.getName(), Level.WARN, exception.toString());
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_ERROR);
                msg.setDescription(msg.getDescription().replaceAll("%SQL%", sql));
                msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
            throwEx = true;
        } catch (NullPointerException exception) {
            //TODO check where exception occur
            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
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
    public String getRandomStringFromList(List<String> list) {
        Random random = new Random();
        if (!list.isEmpty()) {
            return list.get(random.nextInt(list.size()));
        }
        return null;
    }

    @Override
    public String getRandomNewStringFromList(List<String> list1, List<String> list2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getRandomStringNotInUse(List<String> resultSet, List<String> valuesInUse) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String calculateNatureRandomNew(List<String> list, String propName, TestCaseExecution tCExecution) {
        //TODO clean code
        List<String> pastValues = this.testCaseExecutionDataDAO.getPastValuesOfProperty(propName, tCExecution.getTest(),
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

    private String calculateNatureNotInUse(List<String> list, String propName, TestCaseExecution tCExecution) {
        try {
//            List<TCExecution> exelist = this.testCaseExecutionService.findTCExecutionbyCriteria1(DateUtil.getMySQLTimestampTodayDeltaMinutes(10), "%", "%", "%", "%", "%", "PE", "%");
            this.testCaseExecutionService.findTCExecutionbyCriteria1(DateUtil.getMySQLTimestampTodayDeltaMinutes(10), "%", "%", "%", "%", "%", "PE", "%");
            // boucle sur list
            for (String value : list) {
                /**
                 * TODO
                 */
//        List<TestCaseExecutionData> pastValues = this.testCaseExecutionDataService.findTestCaseExecutionDataByCriteria1(propName, value, exelist);
            }
        } catch (CerberusException ex) {
            return list.get(0);
        }

        return null;
    }

    private String calculateNatureNotInUseNew(List<String> list, String propName, TestCaseExecution tCExecution) {
        boolean notFound = true;
        TestCaseExecutionData pastValue;

        try {
            List<TestCaseExecution> testCaseExecutionsLastTenMinutes = this.testCaseExecutionService.findTCExecutionbyCriteria1(DateUtil.getMySQLTimestampTodayDeltaMinutes(10), "%", "%", "%", "%", "%", "PE", "%");

            // loop on list
            for (String value : list) {
                if (value != null) {
                    // loop on past execution.
                    for (TestCaseExecution testCaseExecution : testCaseExecutionsLastTenMinutes) {
                        // retrieve past value
                        pastValue = this.testCaseExecutionDataDAO.findTestCaseExecutionDataByKey(testCaseExecution.getId(), propName);

                        // compare it, if equal
                        if (value.equals(pastValue.getValue())) {
                            // modify notFound boolean
                            notFound = false;

                            // and break loop
                            break;
                        }
                    }

                    // if value not found in the last 10 minutes execution, we use it now !
                    if (notFound) {
                        return value;
                    }
                }
            }
        } catch (CerberusException exception) {
            MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
        }

        // if issue during search or if all are already used, we use the first
        return list.get(0);
    }

    private AnswerList queryDatabaseNColumns(String connectionName, String sql, int rowLimit, String system, HashMap<String, String> columnsToGet) {
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
        msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));

        Connection connection = this.databaseSpring.connect(connectionName);
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                LOG.info("Sending to external Database : '" + connectionName + "' SQL '" + sql + "'");
                ResultSet resultSet = preStat.executeQuery();

                int nrColumns = resultSet.getMetaData().getColumnCount();

                list = new ArrayList<HashMap<String, String>>();
                try {
                    while ((resultSet.next()) && (nbFetch < maxFetch)) {

                        nbColMatch = 0;
                        HashMap<String, String> row = new HashMap<String, String>();

                        for (Map.Entry<String, String> entry : columnsToGet.entrySet()) {
                            String column = entry.getKey();
                            String name = entry.getValue();
                            try {
                                String valueSQL = resultSet.getString(column);
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

                    if (nbColMatch == 0) { // None of the columns could be match.
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_NOCOLUMNMATCH);
                        msg.setDescription(msg.getDescription().replaceAll("%BADCOLUMNS%", error_desc));
                    } else if (!("".equals(error_desc))) { // At least a column could not be parsed
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_COLUMNNOTMATCHING);
                        msg.setDescription(msg.getDescription().replaceAll("%BADCOLUMNS%", error_desc));
                    } else if (list.isEmpty()) { // All columns were found but no data was fetched.
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NODATA);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(SQLService.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(SQLService.class.getName(), Level.WARN, exception.toString());
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_ERROR);
                msg.setDescription(msg.getDescription().replaceAll("%SQL%", sql));
                msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
        } catch (NullPointerException exception) {
            //TODO check where exception occur
            MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
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

    @Override
    public MessageEvent executeUpdate(String system, String country, String environment, String database, String sql) {
        String connectionName;
        CountryEnvironmentDatabase countryEnvironmentDatabase;
        MessageEvent msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);

        try {
            countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system,
                    country, environment, database));
            connectionName = countryEnvironmentDatabase.getConnectionPoolName();
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_GENERIC);
            msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));

            if (!(StringUtil.isNullOrEmpty(connectionName))) {
                Connection connection = this.databaseSpring.connect(connectionName);
                try {
                    PreparedStatement preStat = connection.prepareStatement(sql);
                    try {
                        preStat.executeUpdate();
                        msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
                    } catch (SQLException exception) {
                        MyLogger.log(SQLService.class.getName(), Level.WARN, exception.toString());
                        msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_ERROR);
                        msg.setDescription(msg.getDescription().replaceAll("%SQL%", sql));
                        msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
                    } finally {
                        preStat.close();
                    }
                } catch (SQLException exception) {
                    MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
                    msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_CANNOTACCESSJDBC);
                    msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
                    msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
                } catch (NullPointerException exception) {
                    MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
                    msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_CANNOTACCESSJDBC);
                    msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
                    msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
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
        } catch (CerberusException ex) {
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_JDBCPOOLNOTCONFIGURED);
            msg.setDescription(msg.getDescription().replaceAll("%SYSTEM%", system));
            msg.setDescription(msg.getDescription().replaceAll("%COUNTRY%", country));
            msg.setDescription(msg.getDescription().replaceAll("%ENV%", environment));
            msg.setDescription(msg.getDescription().replaceAll("%DB%", database));
            MyLogger.log(SQLService.class.getName(), Level.FATAL, ex.getMessageError().getDescription());
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
            connectionName = countryEnvironmentDatabase.getConnectionPoolName();
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_GENERIC);
            msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));

            if (!(StringUtil.isNullOrEmpty(connectionName))) {
                Connection connection = this.databaseSpring.connect(connectionName);
                try {
                    CallableStatement cs = connection.prepareCall(sql);
                    try {
                        cs.execute();
                        msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
                    } catch (SQLException exception) {
                        MyLogger.log(SQLService.class.getName(), Level.WARN, exception.toString());
                        msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_ERROR);
                        msg.setDescription(msg.getDescription().replaceAll("%SQL%", sql));
                        msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
                    } finally {
                        cs.close();
                    }
                } catch (SQLException exception) {
                    MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
                    msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_CANNOTACCESSJDBC);
                    msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
                    msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
                } catch (NullPointerException exception) {
                    MyLogger.log(SQLService.class.getName(), Level.FATAL, exception.toString());
                    msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_CANNOTACCESSJDBC);
                    msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
                    msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
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
        } catch (CerberusException ex) {
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SQL_JDBCPOOLNOTCONFIGURED);
            msg.setDescription(msg.getDescription().replaceAll("%SYSTEM%", system));
            msg.setDescription(msg.getDescription().replaceAll("%COUNTRY%", country));
            msg.setDescription(msg.getDescription().replaceAll("%ENV%", environment));
            msg.setDescription(msg.getDescription().replaceAll("%DB%", database));
        }
        return msg;
    }
}
