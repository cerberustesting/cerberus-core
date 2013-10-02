/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.Invariant;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.IInvariantService;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bcivel
 */
@Repository
public class TestCaseExecutionStatisticsDAOImpl implements ITestCaseExecutionStatisticsDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IInvariantService invariantService;

    @Override
    public TestCaseExecutionStatistics getStatisticsOfExecution(String build, String revision, List<String> environment) {
        TestCaseExecutionStatistics statsOfExecutions = new TestCaseExecutionStatistics();

        String env = "";
        try {
            List<Invariant> inv = new ArrayList<Invariant>();
            for (String e : environment) {
                inv.addAll(invariantService.findInvariantByIdGp1("ENVIRONMENT", e));
            }

            for (Invariant i : inv) {
                env += "'" + i.getValue() + "', ";
            }
            env = env.substring(0, env.length() - 2);
        } catch (CerberusException e) {
            //TODO ---
            return null;
        }

        StringBuilder sbquery = new StringBuilder();
        sbquery.append("SELECT t.build, t.revision, TO_DAYS(max(start))-TO_DAYS(min(start)) days, dtc.c NBTC ");
        sbquery.append(" , count(*) TOTAL, OK.c NBOK, KO.c NBKO, format(count(*)/dtc.c,0)  nb_exe_per_testcase, ");
        sbquery.append(" format(count(*)/dtc.c/(TO_DAYS(max(start))-TO_DAYS(min(start))),0)  nb_exe_per_testcase_day ,");
        sbquery.append(" format(OK.c/(count(*))*100,0) OK_percentage, dap.c ");
        sbquery.append(" FROM testcaseexecution t ");
        sbquery.append(" JOIN ( SELECT Build, Revision, count(*) c FROM testcaseexecution t1 ");
        sbquery.append(" WHERE t1.ControlStatus= 'OK' and test !='Business Activity Monitor' and (status='WORKING' or status is null) ");
        sbquery.append(" and test!= 'Performance Monitor' and Environment in (");
        sbquery.append(env);
        sbquery.append(") and build='");
        sbquery.append(build);
        sbquery.append("' and revision='");
        sbquery.append(revision);
        sbquery.append("') as OK ");
        sbquery.append(" JOIN ( SELECT Build, Revision, count(*) c FROM testcaseexecution t1 ");
        sbquery.append(" WHERE t1.ControlStatus= 'KO' and test !='Business Activity Monitor'  and (status='WORKING' or status is null) ");
        sbquery.append(" and test!= 'Performance Monitor' and Environment in (");
        sbquery.append(env);
        sbquery.append(") and build='");
        sbquery.append(build);
        sbquery.append("' and revision='");
        sbquery.append(revision);
        sbquery.append("') as KO ");
        sbquery.append(" JOIN ( SELECT Build, Revision, count(*) c FROM ( ");
        sbquery.append(" SELECT Build, Revision, test, testcase FROM testcaseexecution t ");
        sbquery.append(" where test !='Business Activity Monitor' and test!= 'Performance Monitor' and Environment in (");
        sbquery.append(env);
        sbquery.append(")  and (status='WORKING' or status is null) ");
        sbquery.append(" and ControlStatus in ('OK','KO') and t.build='");
        sbquery.append(build);
        sbquery.append("' and revision='");
        sbquery.append(revision);
        sbquery.append("' GROUP BY test, testcase ) as toto) as dtc ");
        sbquery.append(" JOIN ( SELECT Build, Revision, count(*) c FROM ( ");
        sbquery.append(" SELECT Build, Revision, application FROM testcaseexecution t ");
        sbquery.append(" where test !='Business Activity Monitor' and test!= 'Performance Monitor' and Environment in (");
        sbquery.append(env);
        sbquery.append(")  and (status='WORKING' or status is null) ");
        sbquery.append(" and ControlStatus in ('OK','KO') and t.build='");
        sbquery.append(build);
        sbquery.append("' and revision='");
        sbquery.append(revision);
        sbquery.append("' GROUP BY application ) as toto) as dap ");
        sbquery.append(" WHERE test !='Business Activity Monitor' and (status='WORKING' or status is null) and ");
        sbquery.append(" test!= 'Performance Monitor' and Environment in (");
        sbquery.append(env);
        sbquery.append(") and ControlStatus in ('OK','KO') and t.build='");
        sbquery.append(build);
        sbquery.append("' and t.revision='");
        sbquery.append(revision);
        sbquery.append("' limit 1");


        Connection connection = this.databaseSpring.connect();
        try {
            Statement stat = connection.createStatement();
            try {
                ResultSet rs = stat.executeQuery(sbquery.toString());
                try {
                    if (rs.first()) {
                        statsOfExecutions.setBuild(rs.getString(1));
                        statsOfExecutions.setRevision(rs.getString(2));
                        statsOfExecutions.setDays(rs.getInt(3));
                        statsOfExecutions.setNumberOfTestcaseExecuted(rs.getInt(4));
                        statsOfExecutions.setTotal(rs.getInt(5));
                        statsOfExecutions.setNumberOfOK(rs.getInt(6));
                        statsOfExecutions.setNumberOfKO(rs.getInt(7));
                        statsOfExecutions.setNumberOfExecPerTc(rs.getInt(8));
                        statsOfExecutions.setNumberOfExecPerTcPerDay(rs.getInt(9));
                        statsOfExecutions.setPercentageOfOK(rs.getInt(10));
                        statsOfExecutions.setNumberOfApplicationExecuted(rs.getInt(11));
                    }
                } catch (SQLException ex) {
                    MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.FATAL, ex.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.ERROR, exception.toString());
            } finally {
                stat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.WARN, e.toString());
            }
        }

        return statsOfExecutions;
    }

    @Override
    public List<TestCaseExecutionStatistics> getListOfXLastBuildAndRevExecuted(int listSize) {
        List<TestCaseExecutionStatistics> tenLastBuildAndRevExecuted = new ArrayList<TestCaseExecutionStatistics>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT build, revision FROM testcaseexecution");
        query.append(" WHERE build != 'NA' and build != 'null' and build != ' ' and build is not null ");
        query.append(" GROUP BY Build, Revision ORDER BY Build  desc, Revision desc LIMIT ");
        query.append(listSize);

        TestCaseExecutionStatistics tceToAdd;

        Connection connection = this.databaseSpring.connect();
        try {
            Statement stat = connection.createStatement();
            try {
                ResultSet rs = stat.executeQuery(query.toString());
                try {

                    while (rs.next()) {
                        //TODO factory
                        tceToAdd = new TestCaseExecutionStatistics();
                        tceToAdd.setBuild(rs.getString(1));
                        tceToAdd.setRevision(rs.getString(2));

                        tenLastBuildAndRevExecuted.add(tceToAdd);
                    }

                } catch (SQLException ex) {
                    MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.FATAL, ex.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.ERROR, exception.toString());
            } finally {
                stat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.WARN, e.toString());
            }
        }

        return tenLastBuildAndRevExecuted;
    }
}
