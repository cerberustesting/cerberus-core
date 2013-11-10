package com.redcats.tst.servlet.guiPages;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.Application;
import com.redcats.tst.entity.User;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.IApplicationService;
import com.redcats.tst.service.IParameterService;
import com.redcats.tst.service.IUserService;
import com.redcats.tst.service.impl.ApplicationService;
import com.redcats.tst.service.impl.ParameterService;
import com.redcats.tst.service.impl.UserService;
import com.redcats.tst.statistics.BuildRevisionStatistics;
import com.redcats.tst.statistics.TestCaseExecutionStatisticsServiceImpl;
import com.redcats.tst.util.ParameterParserUtil;
import com.redcats.tst.util.StringUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author ip100003
 */
@WebServlet(name = "Homepage", urlPatterns = {"/Homepage"})
public class Homepage extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //TODO split this into Servlet + Service + DAO + Database

        Date DatePageStart = new Date();
        request.setAttribute("startPageGeneration", DatePageStart);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);
        Connection connection = database.connect();

        String MySystem = ParameterParserUtil.parseStringParam(request.getParameter("MySystem"), "");

        try {

            IUserService userService = appContext.getBean(UserService.class);
            User myUser = userService.findUserByKey(request.getUserPrincipal().getName());

            // Update MyDefaultSystem if different from user.
            if (MySystem.equals("")) {
                MySystem = myUser.getDefaultSystem();
            } else {
                if (!(myUser.getDefaultSystem().equals(MySystem))) {
                    myUser.setDefaultSystem(MySystem);
                    userService.updateUser(myUser);
                }
            }

            if (myUser.getRequest().equalsIgnoreCase("Y")) {
                request.getRequestDispatcher("/ChangePassword.jsp").forward(request, response);
            } else {

                IApplicationService applicationService = appContext.getBean(ApplicationService.class);
                List<Application> appliList = applicationService.findApplicationBySystem(MySystem);
                String inSQL = StringUtil.getInSQLClause(appliList);

                if (!(inSQL.equalsIgnoreCase(""))) {
                    inSQL = " and application " + inSQL + " ";
                } else {
                    inSQL = " and application in ('') ";
                }

                StringBuilder SQL = new StringBuilder();
                SQL.append("SELECT t.Test, count(*) as TOTAL,STANDBY, TBI, INPROGRESS , TBV , WORKING, TBD "
                        + "FROM testcase t "
                        + "left join "
                        + "(SELECT g.test, count(*) as TBI from testcase g "
                        + "where Status = 'TO BE IMPLEMENTED' ");
                SQL.append(inSQL);
                SQL.append("group by Test)s "
                        + "on s.test=t.test "
                        + "left join "
                        + "(SELECT h.test, count(*) as WORKING from testcase h "
                        + "where Status = 'WORKING' ");
                SQL.append(inSQL);
                SQL.append("group by Test)u "
                        + "on u.test=t.test "
                        + "left join "
                        + "(SELECT a.test, count(*) as STANDBY from testcase a "
                        + "where Status = 'Standby' ");
                SQL.append(inSQL);
                SQL.append("group by Test)v "
                        + "on v.test=t.test "
                        + "left join "
                        + "(SELECT b.test, count(*) as INPROGRESS from testcase b "
                        + "where Status = 'IN PROGRESS' ");
                SQL.append(inSQL);
                SQL.append("group by Test)w "
                        + "on w.test=t.test "
                        + "left join "
                        + "(SELECT i.test, count(*) as TBV from testcase i "
                        + "where Status = 'TO BE VALIDATED' ");
                SQL.append(inSQL);
                SQL.append("group by Test)x "
                        + "on x.test=t.test "
                        + "left join "
                        + "(SELECT j.test, count(*) as TBD from testcase j "
                        + "where Status = 'TO BE DELETED' ");
                SQL.append(inSQL);
                SQL.append("group by Test)y "
                        + "on y.test=t.test "
                        + "WHERE 1=1  ");
                SQL.append(inSQL);
                SQL.append(" GROUP BY test;");

                MyLogger.log(Homepage.class.getName(), Level.DEBUG, " SQL : " + SQL.toString());

                PreparedStatement stmt_teststatus = connection.prepareStatement(SQL.toString());

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
                IParameterService parameterService = appContext.getBean(ParameterService.class);

                int numberOfLastBR = Integer.valueOf(parameterService.findParameterByKey("cerberus_homepage_nbbuildhistorydetail").getValue());

                TestCaseExecutionStatisticsServiceImpl tceStatsService = appContext.getBean(TestCaseExecutionStatisticsServiceImpl.class);

                List<BuildRevisionStatistics> buildRev = tceStatsService.getListOfXLastBuildAndRevExecuted(MySystem, numberOfLastBR);

                ArrayList<ArrayList<String>> arrayExecution = new ArrayList<ArrayList<String>>();
                ArrayList<ArrayList<ArrayList<String>>> arrayContent = new ArrayList<ArrayList<ArrayList<String>>>();
                List<List<List<String>>> arrayExecutionEnv = new ArrayList<List<List<String>>>();

                for (BuildRevisionStatistics buildRevList : buildRev) {
                    //ITestCaseExecutionStatisticsService tceStatsS = appContext.getBean(ITestCaseExecutionStatisticsService.class);
                    String build = buildRevList.getBuild();
                    String revision = buildRevList.getRevision();
                    List<String> env = new ArrayList<String>();

                    env.add("PROD");
                    env.add("UAT");
                    env.add("QA");
                    BuildRevisionStatistics globalStats = tceStatsService.getStatisticsOfExecution(MySystem, build, revision, env);

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
                    for (String e : env) {
                        BuildRevisionStatistics globalStatsEnv = tceStatsService.getStatisticsOfExecution(MySystem, build, revision, e);
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

                    PreparedStatement stmtContent = connection.prepareStatement("SELECT t.Build, t.Revision, "
                            + " t.application, t.release, t.link "
                            + "FROM buildrevisionparameters t "
                            + "Where t.build = ? and t.revision = ? and t.application "
                            + inSQL);

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
        } catch (Exception ex) {
            request.getRequestDispatcher("/DatabaseMaintenance.jsp?GO=Y").forward(request, response);
            MyLogger.log(Homepage.class.getName(), Level.FATAL, " Exception catched : " + ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(Homepage.class.getName(), org.apache.log4j.Level.WARN, e.toString());
            }
        }
    }
}
