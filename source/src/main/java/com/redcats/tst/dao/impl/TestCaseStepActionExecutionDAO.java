package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestCaseStepActionExecutionDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.TestCaseStepActionExecution;
import com.redcats.tst.factory.IFactoryTestCaseStepActionExecution;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.util.ParameterParserUtil;
import com.redcats.tst.util.StringUtil;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
public class TestCaseStepActionExecutionDAO implements ITestCaseStepActionExecutionDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepActionExecution factoryTestCaseStepActionExecution;

    @Override
    public void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution) {

        final String query = "UPDATE testcasestepactionexecution SET ACTION = ?, object = ?, property = ?, start = ?, END = ?"
                + ", startlong = ?, endlong = ?, returnCode = ?, returnMessage = ?, screenshotfilename = ? "
                + " WHERE id = ? and test = ? and testcase = ? and step = ? and sequence = ? ;";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, testCaseStepActionExecution.getAction());
            preStat.setString(2, StringUtil.getLeftString(testCaseStepActionExecution.getObject(), 200));
            preStat.setString(3, StringUtil.getLeftString(ParameterParserUtil.securePassword(testCaseStepActionExecution.getProperty(), testCaseStepActionExecution.getPropertyName()), 200));
            if (testCaseStepActionExecution.getStart() != 0) {
                preStat.setTimestamp(4, new Timestamp(testCaseStepActionExecution.getStart()));
            } else {
                preStat.setString(4, "0000-00-00 00:00:00");
            }
            if (testCaseStepActionExecution.getEnd() != 0) {
                preStat.setTimestamp(5, new Timestamp(testCaseStepActionExecution.getEnd()));
            } else {
                preStat.setString(5, "0000-00-00 00:00:00");
            }
            preStat.setString(6, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(testCaseStepActionExecution.getStart()));
            preStat.setString(7, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(testCaseStepActionExecution.getEnd()));
            preStat.setString(8, testCaseStepActionExecution.getReturnCode());
            preStat.setString(9, StringUtil.getLeftString(testCaseStepActionExecution.getReturnMessage(), 500));
            preStat.setString(10, testCaseStepActionExecution.getScreenshotFilename());

            preStat.setLong(11, testCaseStepActionExecution.getId());
            preStat.setString(12, testCaseStepActionExecution.getTest());
            preStat.setString(13, testCaseStepActionExecution.getTestCase());
            preStat.setInt(14, testCaseStepActionExecution.getStep());
            preStat.setInt(15, testCaseStepActionExecution.getSequence());

            try {
                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
    }

    @Override
    public void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution) {

        final String query = "INSERT INTO testcasestepactionexecution(id, step, sequence, ACTION, object, property, start, END, startlong, endlong, returnCode, returnMessage, test, testcase, screenshotfilename) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setLong(1, testCaseStepActionExecution.getId());
            preStat.setInt(2, testCaseStepActionExecution.getStep());
            preStat.setInt(3, testCaseStepActionExecution.getSequence());
            preStat.setString(4, testCaseStepActionExecution.getAction());
            preStat.setString(5, StringUtil.getLeftString(testCaseStepActionExecution.getObject(), 200));
            preStat.setString(6, StringUtil.getLeftString(ParameterParserUtil.securePassword(testCaseStepActionExecution.getProperty(), testCaseStepActionExecution.getPropertyName()), 200));
            if (testCaseStepActionExecution.getStart() != 0) {
                preStat.setTimestamp(7, new Timestamp(testCaseStepActionExecution.getStart()));
            } else {
                preStat.setString(7, "0000-00-00 00:00:00");
            }
            if (testCaseStepActionExecution.getEnd() != 0) {
                preStat.setTimestamp(8, new Timestamp(testCaseStepActionExecution.getEnd()));
            } else {
                preStat.setString(8, "0000-00-00 00:00:00");
            }
            preStat.setString(9, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(testCaseStepActionExecution.getStart()));
            preStat.setString(10, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(testCaseStepActionExecution.getEnd()));
            preStat.setString(11, testCaseStepActionExecution.getReturnCode());
            preStat.setString(12, StringUtil.getLeftString(testCaseStepActionExecution.getReturnMessage(), 500));
            preStat.setString(13, testCaseStepActionExecution.getTest());
            preStat.setString(14, testCaseStepActionExecution.getTestCase());
            preStat.setString(15, testCaseStepActionExecution.getScreenshotFilename());

            try {
                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
    }

    @Override
    public List<List<String>> getListOfSequenceDuration(String idList) {
        List<List<String>> list = null;
        StringBuilder query = new StringBuilder();
        query.append("select a.ID, Step, Sequence, 'Action' as type, b.Start,");
        query.append("concat(substr(EndLong,1,4),'-',");
        query.append("substr(EndLong,5,2),'-',substr(EndLong,7,2),' ',substr(EndLong,9,2),");
        query.append("':',substr(EndLong,11,2),':',substr(EndLong,13,2),'.',");
        query.append("substr(EndLong,15,3)) as testEnd, concat(substr(StartLong,1,4),'-',");
        query.append("substr(StartLong,5,2),'-',substr(StartLong,7,2),' ',");
        query.append("substr(StartLong,9,2),':',substr(StartLong,11,2),':',");
        query.append("substr(StartLong,13,2),'.',substr(StartLong,15,3)) as testStart");
        query.append(" from testcasestepactionexecution a join testcaseexecution b on a.id=b.id where step != '0' and a.id in (");
        query.append(idList);
        query.append(") union select c.ID, c.Step, c.Sequence, 'Control', d.Start,");
        query.append("concat(substr(EndLong,1,4),'-',");
        query.append("substr(EndLong,5,2),'-',substr(EndLong,7,2),' ',substr(EndLong,9,2),");
        query.append("':',substr(EndLong,11,2),':',substr(EndLong,13,2),'.',");
        query.append("substr(EndLong,15,3)) as testEnd, concat(substr(StartLong,1,4),'-',");
        query.append("substr(StartLong,5,2),'-',substr(StartLong,7,2),' ',");
        query.append("substr(StartLong,9,2),':',substr(StartLong,11,2),':',");
        query.append("substr(StartLong,13,2),'.',substr(StartLong,15,3)) as testStart");
        query.append(" from testcasestepactioncontrolexecution c join testcaseexecution d on c.id=d.id where step != '0' and c.id in (");
        query.append(idList);
        query.append(") order by step, sequence, type, ID");

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<List<String>>();
                try {
                    while (resultSet.next()) {
                        List<String> array = new ArrayList<String>();
                        array.add(resultSet.getString(1));
                        array.add(resultSet.getString(2));
                        array.add(resultSet.getString(3));
                        array.add(resultSet.getString(4));
                        array.add(resultSet.getString(5));
                        array.add(resultSet.getString(6));
                        array.add(resultSet.getString(7));
                        list.add(array);
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
            databaseSpring.disconnect();
        }
        return list;
    }
    
    @Override
    public List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int step){
        List<TestCaseStepActionExecution> result = null;
        TestCaseStepActionExecution resultData;
        boolean throwEx = false;
        final String query = "SELECT * FROM TestCaseStepActionExecution WHERE id = ? and test = ? and testcase = ? and step = ? ORDER BY sequence";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, String.valueOf(id));
            preStat.setString(2, test);
            preStat.setString(3, testCase);
            preStat.setInt(4, step);

            try {
                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<TestCaseStepActionExecution>();
                try {
                    while (resultSet.next()) {
                        int seq = resultSet.getInt("sequence");
                        String returnCode = resultSet.getString("returncode");
                        String returnMessage = resultSet.getString("returnmessage");
                        String action = resultSet.getString("action");
                        String object = resultSet.getString("object");
                        String property = resultSet.getString("property");
                        long start = resultSet.getLong("start");
                        long end = resultSet.getLong("end");
                        long startlong = resultSet.getLong("startlong");
                        long endlong = resultSet.getLong("endlong");
                        String screenshot = resultSet.getString("ScreenshotFilename");
                        resultData = factoryTestCaseStepActionExecution.create(id, test, testCase, step, seq, returnCode, returnMessage, action, object, property, start, end, startlong, endlong, screenshot, null, null, null);
                        result.add(resultData);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return result;
    }
    
    
}
