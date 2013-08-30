package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestCaseExecutionDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.TCExecution;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryTCExecution;
import com.redcats.tst.factory.IFactoryTestCaseExecution;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.util.StringUtil;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
@Repository
public class TestCaseExecutionDAO implements ITestCaseExecutionDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTCExecution factoryTCExecution;

    @Override
    public void insertTCExecution(TCExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "INSERT INTO testcaseexecution(test, testcase, build, revision, environment, country, browser, application, ip, "
                + "url, port, tag, verbose, status, start, end, controlstatus, controlMessage, crbversion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preStat.setString(1, tCExecution.getTest());
            preStat.setString(2, tCExecution.getTestCase());
            preStat.setString(3, tCExecution.getBuild());
            preStat.setString(4, tCExecution.getRevision());
            preStat.setString(5, tCExecution.getEnvironment());
            preStat.setString(6, tCExecution.getCountry());
            preStat.setString(7, tCExecution.getBrowser());
            preStat.setString(8, tCExecution.getApplication().getApplication());
            preStat.setString(9, tCExecution.getIp());
            preStat.setString(10, tCExecution.getUrl());
            preStat.setString(11, tCExecution.getPort());
            preStat.setString(12, tCExecution.getTag());
            preStat.setInt(13, tCExecution.getVerbose());
            preStat.setString(14, tCExecution.getStatus());
            if (tCExecution.getStart() != 0) {
                preStat.setTimestamp(15, new Timestamp(tCExecution.getStart()));
            } else {
                preStat.setString(15, "0000-00-00 00:00:00");
            }
            if (tCExecution.getEnd() != 0) {
                preStat.setTimestamp(16, new Timestamp(tCExecution.getEnd()));
            } else {
                preStat.setString(16, "0000-00-00 00:00:00");
            }
            preStat.setString(17, tCExecution.getControlStatus());
            preStat.setString(18, StringUtil.getLeftString(tCExecution.getControlMessage(), 500));
            preStat.setString(19, tCExecution.getCrbVersion());

            try {
                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        tCExecution.setId(resultSet.getInt(1));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        if (throwEx) {
            tCExecution.setId(-1);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
    }

    @Override
    public void updateTCExecution(TCExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "UPDATE testcaseexecution SET test = ?, testcase = ?, build = ?, revision = ?, environment = ?, country = ?"
                + ", browser = ?, application = ?, ip = ?, url = ?, port = ?, tag = ?, verbose = ?, status = ?"
                + ", start = ?, end = ? , controlstatus = ?, controlMessage = ?, crbversion = ?, finished = ? WHERE id = ?";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, tCExecution.getTest());
            preStat.setString(2, tCExecution.getTestCase());
            preStat.setString(3, tCExecution.getBuild());
            preStat.setString(4, tCExecution.getRevision());
            preStat.setString(5, tCExecution.getEnvironment());
            preStat.setString(6, tCExecution.getCountry());
            preStat.setString(7, tCExecution.getBrowser());
            preStat.setString(8, tCExecution.getApplication().getApplication());
            preStat.setString(9, tCExecution.getIp());
            preStat.setString(10, tCExecution.getUrl());
            preStat.setString(11, tCExecution.getPort());
            preStat.setString(12, tCExecution.getTag());
            preStat.setInt(13, tCExecution.getVerbose());
            preStat.setString(14, tCExecution.getStatus());
            if (tCExecution.getStart() != 0) {
                preStat.setTimestamp(15, new Timestamp(tCExecution.getStart()));
            } else {
                preStat.setString(15, "0000-00-00 00:00:00");
            }
            if (tCExecution.getEnd() != 0) {
                preStat.setTimestamp(16, new Timestamp(tCExecution.getEnd()));
            } else {
                preStat.setString(16, "0000-00-00 00:00:00");
            }
            preStat.setString(17, tCExecution.getControlStatus());
            preStat.setString(18, StringUtil.getLeftString(tCExecution.getControlMessage(), 500));
            preStat.setString(19, tCExecution.getCrbVersion());
            preStat.setString(20, tCExecution.getFinished());
            preStat.setLong(21, tCExecution.getId());

            try {
                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
    }

    @Override
    public List<String> getIDListOfLastExecutions(String test, String testcase, String country) {
        List<String> list = null;
        final String query = "SELECT ID FROM testcaseexecution WHERE test = ? AND testcase = ? AND country = ? AND controlStatus='OK' ORDER BY id DESC LIMIT 200";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setString(3, country);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<String>();
                try {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("ID"));
                    }
                } catch (SQLException exception) {
                    //TODO logger ERROR
                    //error on resultSet.getString
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                //TODO logger ERROR
                //preStat.executeQuery();
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            //TODO logger ERROR
            //conn.prepareStatement(query);
        } finally {
            this.databaseSpring.disconnect();
        }

        return list;
    }

//    @Override
//    public TCExecution findLastTCExecutionByCriteria(String test, String testcase, String environment, String country) throws CerberusException {
//        TCExecution result = null;
//        boolean throwEx = false;
//        final String query = "SELECT * FROM testcaseexecution WHERE test = ? AND testcase = ? AND environment = ? AND country = ? ORDER BY id DESC";
//
//        try {
//            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
//            preStat.setString(1, test);
//            preStat.setString(2, testcase);
//            preStat.setString(3, environment);
//            preStat.setString(4, country);
//
//            try {
//                ResultSet resultSet = preStat.executeQuery();
//                try {
//                    if (!(resultSet.first())) {
//                        throwEx = true;
//                    }
//                    //TODO get Application
//                    long id = resultSet.getLong("ID");
//                    String build = resultSet.getString("build");
//                    String revision = resultSet.getString("revision");
//                    String browser = resultSet.getString("browser");
//                    long start = resultSet.getLong("start");
//                    long end = resultSet.getLong("end");
//                    String controlStatus = resultSet.getString("controlStatus");
//                    String controlMessage = resultSet.getString("controlMessage");
//                    String ip = resultSet.getString("ip"); // Host the Selenium IP
//                    String url = resultSet.getString("url");
//                    String port = resultSet.getString("port"); // host the Selenium Port
//                    String tag = resultSet.getString("tag");
//                    String finished = resultSet.getString("finished");
//                    int verbose = resultSet.getInt("verbose");
//                    String status = resultSet.getString("status");
//                    String crbVersion = resultSet.getString("crbVersion");
//                    result = factoryTCExecution.create(id, test, testcase, build, revision, environment,
//                            country, browser, start, end, controlStatus, controlMessage, null, ip, url,
//                            port, tag, finished, verbose, 0, "", status, crbVersion, null, null, null,
//                            false, null, null, null, null, null, null, null, null);
//
//                } catch (SQLException exception) {
//                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
//                } finally {
//                    resultSet.close();
//                }
//            } catch (Exception exception) {
//                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
//            } finally {
//                preStat.close();
//            }
//        } catch (Exception exception) {
//            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
//        } finally {
//            this.databaseSpring.disconnect();
//        }
//        if (throwEx) {
//            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
//        }
//        return result;
//    }

    @Override
    public List<TCExecution> findExecutionbyCriteria1(String dateLimit, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException {
        List<TCExecution> myTestCaseExecutions = null;
        TCExecution Execution;
        boolean throwException = false;
        final String query = "SELECT * FROM testcaseexecution WHERE start > ? AND test LIKE ? AND testcase LIKE ? AND environment LIKE ? AND country LIKE ? "
                + " AND application LIKE ? AND controlstatus LIKE ? AND status LIKE ? ";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, dateLimit);
            preStat.setString(2, test);
            preStat.setString(3, testCase);
            preStat.setString(4, environment);
            preStat.setString(5, country);
            preStat.setString(6, application);
            preStat.setString(7, controlStatus);
            preStat.setString(8, status);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (!(resultSet.first())) {
                        throwException = true;
                    } else {
                        myTestCaseExecutions = new ArrayList<TCExecution>();
                        do {
                            long id = resultSet.getLong("ID");
                            String test1 = resultSet.getString("test");
                            String testcase1 = resultSet.getString("testcase");
                            String build = resultSet.getString("build");
                            String revision = resultSet.getString("revision");
                            String browser = resultSet.getString("browser");
                            long start = resultSet.getLong("start");
                            long end = resultSet.getLong("end");
                            String controlStatus1 = resultSet.getString("controlStatus");
                            String controlMessage = resultSet.getString("controlMessage");
                            String ip = resultSet.getString("ip"); // Host the Selenium IP
                            String url = resultSet.getString("url");
                            String port = resultSet.getString("port"); // host the Selenium Port
                            String tag = resultSet.getString("tag");
                            String finished = resultSet.getString("finished");
                            int verbose = resultSet.getInt("verbose");
                            String status1 = resultSet.getString("status");
                            String crbVersion = resultSet.getString("crbVersion");
                            Execution = factoryTCExecution.create(id, test1, testcase1, build, revision, environment,
                                    country, browser, start, end, controlStatus1, controlMessage, null, ip, url,
                                    port, tag, finished, verbose, 0, "", status1, crbVersion, null, null, null,
                                    false, null, null, null, null, null, null, null, null);

                            myTestCaseExecutions.add(Execution);
                        } while (resultSet.next());
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    if (!(resultSet == null)) {
                        resultSet.close();
                    }
                }
            } catch (Exception exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (Exception exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }

        return myTestCaseExecutions;
    }
}
