package com.redcats.tst.servlet.guiPages;

import com.redcats.tst.log.MyLogger;
import com.redcats.tst.refactor.DbMysqlController;
import com.redcats.tst.refactor.TestCaseExecutionStatistics;
import com.redcats.tst.refactor.TestCaseExecutionStatisticsServiceImpl;
import com.redcats.tst.service.IParameterService;
import com.redcats.tst.service.impl.ParameterService;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author ip100003
 */
public class Homepage extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //TODO split this into Servlet + Service + DAO + Database

        DbMysqlController db = new DbMysqlController();
        Connection conn = db.connect();

        try {

            PreparedStatement stmt_testnewpassword = conn.prepareStatement("SELECT * FROM User WHERE Login LIKE ?");
            try {
                stmt_testnewpassword.setString(1, request.getUserPrincipal().getName());

                ResultSet rs_testnewpassword = stmt_testnewpassword.executeQuery();
                try {
                    if (rs_testnewpassword.next()) {
                        if (rs_testnewpassword.getString("Request").equalsIgnoreCase("Y")) {
                            request.getRequestDispatcher("/ChangePassword.jsp").forward(request, response);
                        } else {

                            PreparedStatement stmt_teststatus = conn.prepareStatement("SELECT t.Test, count(*) as TOTAL,STANDBY, TBI, INPROGRESS , TBV , WORKING, TBD "
                                    + "FROM testcase t "
                                    + "left join "
                                    + "(SELECT g.test, count(*) as TBI from testcase g "
                                    + "where Status = 'TO BE IMPLEMENTED' "
                                    + "group by Test)s "
                                    + "on s.test=t.test "
                                    + "left join "
                                    + "(SELECT h.test, count(*) as WORKING from testcase h "
                                    + "where Status = 'WORKING' "
                                    + "group by Test)u "
                                    + "on u.test=t.test "
                                    + "left join "
                                    + "(SELECT a.test, count(*) as STANDBY from testcase a "
                                    + "where Status = 'Standby' "
                                    + "group by Test)v "
                                    + "on v.test=t.test "
                                    + "left join "
                                    + "(SELECT b.test, count(*) as INPROGRESS from testcase b "
                                    + "where Status = 'IN PROGRESS' "
                                    + "group by Test)w "
                                    + "on w.test=t.test "
                                    + "left join "
                                    + "(SELECT i.test, count(*) as TBV from testcase i "
                                    + "where Status = 'TO BE VALIDATED' "
                                    + "group by Test)x "
                                    + "on x.test=t.test "
                                    + "left join "
                                    + "(SELECT j.test, count(*) as TBD from testcase j "
                                    + "where Status = 'TO BE DELETED' "
                                    + "group by Test)y "
                                    + "on y.test=t.test "
                                    + "group by test");

                            ArrayList<ArrayList<String>> arrayTest = new ArrayList<ArrayList<String>>();
                            ArrayList<String> al;
                            try {
                                ResultSet rs_teststatus = stmt_teststatus.executeQuery();

                                Integer tot = 0;
                                Integer sb = 0;
                                Integer tbi = 0;
                                Integer ip = 0;
                                Integer tbv = 0;
                                Integer working = 0;
                                try {
                                    while (rs_teststatus.next()) {
                                        al = new ArrayList<String>();
                                        al.add(rs_teststatus.getString("t.test"));
                                        al.add(rs_teststatus.getInt("TOTAL") != 0 ? rs_teststatus.getString("TOTAL") : "");
                                        tot += rs_teststatus.getInt("TOTAL");
                                        al.add(rs_teststatus.getInt("STANDBY") != 0 ? rs_teststatus.getString("STANDBY") : "");
                                        sb += rs_teststatus.getInt("STANDBY");
                                        al.add(rs_teststatus.getInt("TBI") != 0 ? rs_teststatus.getString("TBI") : "");
                                        tbi += rs_teststatus.getInt("TBI");
                                        al.add(rs_teststatus.getInt("INPROGRESS") != 0 ? rs_teststatus.getString("INPROGRESS") : "");
                                        ip += rs_teststatus.getInt("INPROGRESS");
                                        al.add(rs_teststatus.getInt("TBV") != 0 ? rs_teststatus.getString("TBV") : "");
                                        tbv += rs_teststatus.getInt("TBV");
                                        al.add(rs_teststatus.getInt("WORKING") != 0 ? rs_teststatus.getString("WORKING") : "");
                                        working += rs_teststatus.getInt("WORKING");
                                        arrayTest.add(al);
                                    }
                                } finally {
                                    rs_teststatus.close();
                                }
                                al = new ArrayList<String>();
                                al.add("-- GRAN TOTAL --");
                                al.add(tot.toString());
                                al.add(sb.toString());
                                al.add(tbi.toString());
                                al.add(ip.toString());
                                al.add(tbv.toString());
                                al.add(working.toString());
                                arrayTest.add(al);

                            } finally {
                                stmt_teststatus.close();
                            }
                            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                            IParameterService parameterService = appContext.getBean(ParameterService.class);

                            int numberOfLastBR = Integer.valueOf(parameterService.findParameterByKey("cerberus_homepage_nbbuildhistorydetail").getValue());

                            TestCaseExecutionStatisticsServiceImpl tceStatsService = appContext.getBean(TestCaseExecutionStatisticsServiceImpl.class);

                            List<TestCaseExecutionStatistics> buildRev = tceStatsService.getListOfXLastBuildAndRevExecuted(numberOfLastBR);

                            ArrayList<ArrayList<String>> arrayExecution = new ArrayList<ArrayList<String>>();
                            ArrayList<ArrayList<ArrayList<String>>> arrayContent = new ArrayList<ArrayList<ArrayList<String>>>();
                            List<List<List<String>>> arrayExecutionEnv = new ArrayList<List<List<String>>>();

                            for (TestCaseExecutionStatistics buildRevList : buildRev) {
                                //ITestCaseExecutionStatisticsService tceStatsS = appContext.getBean(ITestCaseExecutionStatisticsService.class);
                                String build = buildRevList.getBuild();
                                String revision = buildRevList.getRevision();
                                List<String> env = new ArrayList<String>();
                                env.add("UAT");
                                env.add("QA");
                                TestCaseExecutionStatistics globalStats = tceStatsService.getStatisticsOfExecution(build, revision, env);

                                al = new ArrayList<String>();
                                al.add(build);
                                al.add(revision);
                                al.add(String.valueOf(globalStats.getTotal()));
                                al.add(String.valueOf(globalStats.getNumberOfOK()));
                                al.add(String.valueOf(globalStats.getPercentageOfOK()));
                                al.add(String.valueOf(globalStats.getNumberOfTestcaseExecuted()));
                                al.add(String.valueOf(globalStats.getNumberOfExecPerTc()));
                                al.add(String.valueOf(globalStats.getDays()));
                                al.add(String.valueOf(globalStats.getNumberOfExecPerTcPerDay()));
                                al.add(String.valueOf(globalStats.getNumberOfApplicationExecuted()));
                                arrayExecution.add(al);


                                List<List<String>> arrayEnv = new ArrayList<List<String>>();
                                for(String e : env){
                                    TestCaseExecutionStatistics globalStatsEnv = tceStatsService.getStatisticsOfExecution(build, revision, e);
                                    al = new ArrayList<String>();
                                    al.add(e);
                                    al.add(String.valueOf(globalStatsEnv.getTotal()));
                                    al.add(String.valueOf(globalStatsEnv.getNumberOfOK()));
                                    al.add(String.valueOf(globalStatsEnv.getPercentageOfOK()));
                                    al.add(String.valueOf(globalStatsEnv.getNumberOfTestcaseExecuted()));
                                    al.add(String.valueOf(globalStatsEnv.getNumberOfExecPerTc()));
                                    al.add(String.valueOf(globalStatsEnv.getDays()));
                                    al.add(String.valueOf(globalStatsEnv.getNumberOfExecPerTcPerDay()));
                                    al.add(String.valueOf(globalStatsEnv.getNumberOfApplicationExecuted()));
                                    arrayEnv.add(al);
                                }
                                arrayExecutionEnv.add(arrayEnv);

                                PreparedStatement stmtContent = conn.prepareStatement("SELECT t.Build, t.Revision, "
                                        + " t.application, t.release, t.link "
                                        + "FROM buildrevisionparameters t "
                                        + "Where build = ? and revision = ?");
                                try {
                                    stmtContent.setString(1, build);
                                    stmtContent.setString(2, revision);
                                    ResultSet rsContent = stmtContent.executeQuery();
                                    ArrayList<ArrayList<String>> array = new ArrayList<ArrayList<String>>();
                                    try {
                                        if (rsContent.first()) {
                                            do {
                                                al = new ArrayList<String>();
                                                al.add(rsContent.getString("t.Build"));
                                                al.add(rsContent.getString("t.Revision"));
                                                al.add(rsContent.getString("t.application"));
                                                al.add(rsContent.getString("t.release"));
                                                al.add(rsContent.getString("t.link"));
                                                array.add(al);
                                            } while (rsContent.next());
                                        }
                                    } finally {
                                        rsContent.close();
                                    }
                                    arrayContent.add(array);
                                } finally {
                                    stmtContent.close();
                                }
                            }

                            request.setAttribute("arrayTest", arrayTest);
                            request.setAttribute("arrayExecution", arrayExecution);
                            request.setAttribute("arrayContent", arrayContent);
                            request.setAttribute("arrayExecutionEnv", arrayExecutionEnv);
                            request.getRequestDispatcher("/Homepage.jsp").forward(request, response);
                        }
                    }
                } finally {
                    rs_testnewpassword.close();
                }
            } finally {
                stmt_testnewpassword.close();
            }
        } catch (Exception ex) {
            request.getRequestDispatcher("/DatabaseMaintenance.jsp?GO=Y").forward(request, response);
            MyLogger.log(Homepage.class.getName(), Level.FATAL, " Exception catched : " + ex);
        } finally {
            db.disconnect();
        }
    }
}
