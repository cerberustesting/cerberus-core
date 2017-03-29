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
package org.cerberus.statistics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author bcivel
 */
@Repository
public class TestCaseExecutionStatisticsDAOImpl implements ITestCaseExecutionStatisticsDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IInvariantService invariantService;
    @Autowired
    private IApplicationService applicationService;

    @Override
    public BuildRevisionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, List<String> environment) {
        BuildRevisionStatistics statsOfExecutions = new BuildRevisionStatistics();
        String appliSQL = "";
        try {
            List<Application> appliList = applicationService.convert(applicationService.readBySystem(MySystem));
            appliSQL = SqlUtil.getInSQLClause(appliList);
        } catch (CerberusException ex) {
            Logger.getLogger(TestCaseExecutionStatisticsDAOImpl.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        String env;
        try {
            List<Invariant> inv = new ArrayList<Invariant>();
            AnswerList answer;
            for (String e : environment) {
                answer = invariantService.findInvariantByIdGp1("ENVIRONMENT", e);
                if (!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    //TODO: temporary fix that should be solved while refactoring
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
                } else {
                    inv.addAll(answer.getDataList());
                }
            }

            StringBuffer buf = new StringBuffer();
            for (Invariant i : inv) {
                buf.append("'" + i.getValue() + "', ");
//                env += "'" + i.getValue() + "', ";
            }
            env = buf.toString().substring(0, buf.length() - 2);
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
        sbquery.append(" WHERE t1.ControlStatus= 'OK' and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append(" and application ");
        sbquery.append(appliSQL);
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(") and build='");
        sbquery.append(build);
        sbquery.append("' and revision='");
        sbquery.append(revision);
        sbquery.append("') as OK ");
        sbquery.append(" JOIN ( SELECT Build, Revision, count(*) c FROM testcaseexecution t1 ");
        sbquery.append(" WHERE t1.ControlStatus= 'KO' and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append(" and application ");
        sbquery.append(appliSQL);
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(") and build='");
        sbquery.append(build);
        sbquery.append("' and revision='");
        sbquery.append(revision);
        sbquery.append("') as KO ");
        sbquery.append(" JOIN ( SELECT Build, Revision, count(*) c FROM ( ");
        sbquery.append(" SELECT Build, Revision, test, testcase FROM testcaseexecution t ");
        sbquery.append(" where ControlStatus in ('OK','KO') and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append(" and application ");
        sbquery.append(appliSQL);
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(") and t.build='");
        sbquery.append(build);
        sbquery.append("' and t.revision='");
        sbquery.append(revision);
        sbquery.append("' GROUP BY test, testcase ) as toto) as dtc ");
        sbquery.append(" JOIN ( SELECT Build, Revision, count(*) c FROM ( ");
        sbquery.append(" SELECT Build, Revision, application FROM testcaseexecution t ");
        sbquery.append(" where ControlStatus in ('OK','KO') and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append(" and application ");
        sbquery.append(appliSQL);
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(") and t.build='");
        sbquery.append(build);
        sbquery.append("' and revision='");
        sbquery.append(revision);
        sbquery.append("' GROUP BY application ) as toto) as dap ");
        sbquery.append(" WHERE ControlStatus in ('OK','KO') and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append("and application ");
        sbquery.append(appliSQL);
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(") and  t.build='");
        sbquery.append(build);
        sbquery.append("' and t.revision='");
        sbquery.append(revision);
        sbquery.append("' ");
        sbquery.append(" limit 1");

        Connection connection = this.databaseSpring.connect();
        try {
            Statement stat = connection.createStatement();
            try {
                MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.DEBUG, sbquery.toString());
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
    public BuildRevisionStatistics getStatisticsOfExternalExecution(String MySystem, String build, String revision, List<String> environment) {
        BuildRevisionStatistics statsOfExecutions = new BuildRevisionStatistics();
        String appliSQL = "";
        try {
            List<Application> appliList = applicationService.convert(applicationService.readBySystem(MySystem));
            appliSQL = SqlUtil.getInSQLClause(appliList);
        } catch (CerberusException ex) {
            Logger.getLogger(TestCaseExecutionStatisticsDAOImpl.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        String env = "";
        try {
            List<Invariant> inv = new ArrayList<Invariant>();
            AnswerList answer;
            for (String e : environment) {
                answer = invariantService.findInvariantByIdGp1("ENVIRONMENT", e);
                if (!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    //TODO: temporary fix that should be solved while refactoring
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
                } else {
                    inv.addAll(answer.getDataList());
                }
            }

            StringBuffer buf = new StringBuffer();
            for (Invariant i : inv) {
                buf.append("'" + i.getValue() + "', ");
//                env += "'" + i.getValue() + "', ";
            }
            env = buf.toString().substring(0, buf.length() - 2);
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
        sbquery.append(" JOIN  testcaseexecutionsysver tc ON tc.ID=t.ID ");
        sbquery.append(" JOIN ( SELECT t1b.Build, t1b.Revision, count(*) c FROM testcaseexecution t1 ");
        sbquery.append(" JOIN  testcaseexecutionsysver t1b ON t1b.ID=t1.ID ");
        sbquery.append(" WHERE t1.ControlStatus= 'OK' and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append(" and application not ");
        sbquery.append(appliSQL);
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(") and t1b.build='");
        sbquery.append(build);
        sbquery.append("' and t1b.revision='");
        sbquery.append(revision);
        sbquery.append("' and t1b.`system`='");
        sbquery.append(MySystem);
        sbquery.append("') as OK ");
        sbquery.append(" JOIN ( SELECT t1b.Build, t1b.Revision, count(*) c FROM testcaseexecution t1 ");
        sbquery.append(" JOIN  testcaseexecutionsysver t1b ON t1b.ID=t1.ID ");
        sbquery.append(" WHERE t1.ControlStatus= 'KO' and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append(" and application not ");
        sbquery.append(appliSQL);
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(") and t1b.build='");
        sbquery.append(build);
        sbquery.append("' and t1b.revision='");
        sbquery.append(revision);
        sbquery.append("' and t1b.`system`='");
        sbquery.append(MySystem);
        sbquery.append("') as KO ");
        sbquery.append(" JOIN ( SELECT Build, Revision, count(*) c FROM ( ");
        sbquery.append(" SELECT tb.Build, tb.Revision, test, testcase FROM testcaseexecution t ");
        sbquery.append(" JOIN  testcaseexecutionsysver tb ON tb.ID=t.ID ");
        sbquery.append(" where ControlStatus in ('OK','KO') and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append(" and application not ");
        sbquery.append(appliSQL);
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(")  ");
        sbquery.append(" and tb.build='");
        sbquery.append(build);
        sbquery.append("' and tb.revision='");
        sbquery.append(revision);
        sbquery.append("' and tb.`system`='");
        sbquery.append(MySystem);
        sbquery.append("' GROUP BY test, testcase ) as toto) as dtc ");
        sbquery.append(" JOIN ( SELECT Build, Revision, count(*) c FROM ( ");
        sbquery.append(" SELECT tb.Build, tb.Revision, application FROM testcaseexecution t ");
        sbquery.append(" JOIN  testcaseexecutionsysver tb ON tb.ID=t.ID ");
        sbquery.append(" where ControlStatus in ('OK','KO') and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append(" and application not ");
        sbquery.append(appliSQL);
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(")   ");
        sbquery.append(" and tb.build='");
        sbquery.append(build);
        sbquery.append("' and tb.revision='");
        sbquery.append(revision);
        sbquery.append("' and tb.`system`='");
        sbquery.append(MySystem);
        sbquery.append("' GROUP BY application ) as toto) as dap ");
        sbquery.append(" WHERE ControlStatus in ('OK','KO') and test not in ('Performance Monitor', 'Business Activity Monitor', 'Data Integrity Monitor') and status='WORKING' ");
        sbquery.append(" and Environment in (");
        sbquery.append(env);
        sbquery.append(") and tc.build='");
        sbquery.append(build);
        sbquery.append("' and tc.revision='");
        sbquery.append(revision);
        sbquery.append("' and tc.`system`='");
        sbquery.append(MySystem);
        sbquery.append("' ");
        sbquery.append("and application not ");
        sbquery.append(appliSQL);
        sbquery.append(" limit 1");

        Connection connection = this.databaseSpring.connect();
        try {
            Statement stat = connection.createStatement();
            try {
                MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.DEBUG, sbquery.toString());
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
    public List<BuildRevisionStatistics> getListOfXLastBuildAndRevExecuted(String MySystem, int listSize) {
        List<BuildRevisionStatistics> tenLastBuildAndRevExecuted = new ArrayList<BuildRevisionStatistics>();
        StringBuilder query = new StringBuilder();
        String appliSQL = "";
        try {
            List<Application> appliList = applicationService.convert(applicationService.readBySystem(MySystem));
            appliSQL = SqlUtil.getInSQLClause(appliList);
        } catch (CerberusException ex) {
            Logger.getLogger(TestCaseExecutionStatisticsDAOImpl.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        query.append("SELECT build, revision FROM testcaseexecution");
        query.append(" WHERE build != 'NA' and build != 'null' and build != ' ' and build is not null ");
        query.append(" and application ");
        query.append(appliSQL);
        query.append(" GROUP BY Build, Revision ORDER BY Build  desc, Revision desc LIMIT ");
        query.append(listSize);

        BuildRevisionStatistics tceToAdd;

        Connection connection = this.databaseSpring.connect();
        try {
            Statement stat = connection.createStatement();
            try {
                ResultSet rs = stat.executeQuery(query.toString());
                try {

                    while (rs.next()) {
                        //TODO factory
                        tceToAdd = new BuildRevisionStatistics();
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

    @Override
    public List<BuildRevisionStatistics> getListOfXLastBuildAndRev(String MySystem, int listSize, String sprint) {
        List<BuildRevisionStatistics> lastBuildAndRevisionFromContent = new ArrayList<BuildRevisionStatistics>();
        StringBuilder query = new StringBuilder();
        String appliSQL = "";
        try {
            List<Application> appliList = applicationService.convert(applicationService.readBySystem(MySystem));
            appliSQL = SqlUtil.getInSQLClause(appliList);
        } catch (CerberusException ex) {
            Logger.getLogger(TestCaseExecutionStatisticsDAOImpl.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        query.append("SELECT distinct build, revision , l1.seq seq1 , l2.seq seq2");
        query.append(" FROM buildrevisionparameters br");
        query.append(" JOIN buildrevisioninvariant l1 on br.build = l1.versionname AND l1.`level`=1 ");
        query.append(" JOIN buildrevisioninvariant l2 on br.revision = l2.versionname AND l2.`level`=2 ");
        query.append(" WHERE build <> 'NONE' ");
        if (!sprint.equals("")) {
            query.append(" and build like '");
            query.append(sprint);
            query.append("' ");
        }
        query.append(" and application ");
        query.append(appliSQL);
        query.append(" ORDER BY seq1 desc, seq2 desc ");
        query.append(" LIMIT ");
        query.append(listSize);

        BuildRevisionStatistics tceToAdd;

        Connection connection = this.databaseSpring.connect();
        try {
            Statement stat = connection.createStatement();
            try {
                MyLogger.log(TestCaseExecutionStatisticsDAOImpl.class.getName(), Level.DEBUG, query.toString());
                ResultSet rs = stat.executeQuery(query.toString());
                try {

                    while (rs.next()) {
                        //TODO factory
                        tceToAdd = new BuildRevisionStatistics();
                        tceToAdd.setBuild(rs.getString(1));
                        tceToAdd.setRevision(rs.getString(2));

                        lastBuildAndRevisionFromContent.add(tceToAdd);
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

        return lastBuildAndRevisionFromContent;
    }
}
