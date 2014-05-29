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
package org.cerberus.serviceEngine.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseExecutionDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.CountryEnvironmentDatabase;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.Property;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TestCaseExecutionData;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ICountryEnvironmentDatabaseService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.serviceEngine.ISQLService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class SQLService implements ISQLService{

    @Autowired
    private ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService;
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private ITestCaseExecutionDataDAO testCaseExecutionDataDAO;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    
        @Override
        public TestCaseExecutionData calculateOnDatabase(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseProperties, TestCaseExecution tCExecution) {
        String sql = testCaseProperties.getValue1();
        String db = testCaseProperties.getDatabase();

        String connectionName;
        CountryEnvironmentDatabase countryEnvironmentDatabase;

        try {
            countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.findCountryEnvironmentDatabaseByKey(tCExecution.getApplication().getSystem(), testCaseProperties.getCountry(), tCExecution.getEnvironmentData(), db);
            connectionName = countryEnvironmentDatabase.getConnectionPoolName();

            if (!(StringUtil.isNullOrEmpty(connectionName))) {
                try {
                    List<String> list = this.queryDatabase(connectionName, sql, testCaseProperties.getRowLimit());

                    if (list != null && !list.isEmpty()) {
                        if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_STATIC)) {
                            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL);
                            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                            mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                            mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                            testCaseExecutionData.setPropertyResultMessage(mes);
                            testCaseExecutionData.setValue(list.get(0));

                        } else if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_RANDOM)) {
                            testCaseExecutionData.setValue(this.getRandomStringFromList(list));
                            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_RANDOM);
                            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                            mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                            mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                            testCaseExecutionData.setPropertyResultMessage(mes);

                        } else if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_RANDOMNEW)) {
                            testCaseExecutionData.setValue(this.calculateNatureRandomNew(list, testCaseProperties.getProperty(), tCExecution));
                            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_RANDOM_NEW);
                            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                            mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                            mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                            testCaseExecutionData.setPropertyResultMessage(mes);

                        } else if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_NOTINUSE)) {
                            testCaseExecutionData.setValue(this.calculateNatureNotInUse(list, testCaseProperties.getProperty(), tCExecution));
                            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_NOTINUSE);
                            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                            mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                            mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                            testCaseExecutionData.setPropertyResultMessage(mes);

                        }
                    } else {
                        MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NODATA);
                        mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                        mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                        mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                        testCaseExecutionData.setPropertyResultMessage(mes);
                    }
                } catch (CerberusEventException ex) {
                    MessageEvent mes = ex.getMessageError();
                    testCaseExecutionData.setPropertyResultMessage(mes);
                }

            } else {
                MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_EMPTYJDBCPOOL);
                mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", tCExecution.getApplication().getSystem()));
                mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", testCaseProperties.getCountry()));
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
                mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                testCaseExecutionData.setPropertyResultMessage(mes);
            }
        } catch (CerberusException ex) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_JDBCPOOLNOTCONFIGURED);
            mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", tCExecution.getApplication().getSystem()));
            mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", testCaseProperties.getCountry()));
            mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
            testCaseExecutionData.setPropertyResultMessage(mes);
        }

        return testCaseExecutionData;
    }

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @return Description text text text.
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
            if(limit > 0 && limit < maxSecurityFetch) {
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
                    MyLogger.log(SQLService.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());

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

        if (pastValues.size() > 0) {
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
}
