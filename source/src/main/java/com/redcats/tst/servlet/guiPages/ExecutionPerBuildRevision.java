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
@WebServlet(name = "ExecutionPerBuildRevision", urlPatterns = {"/ExecutionPerBuildRevision"})
public class ExecutionPerBuildRevision extends HttpServlet {

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

                ArrayList<String> al;

                IParameterService parameterService = appContext.getBean(ParameterService.class);

                int numberOfLastBR = Integer.valueOf(parameterService.findParameterByKey("cerberus_homepage_nbbuildhistorydetail").getValue());

                TestCaseExecutionStatisticsServiceImpl tceStatsService = appContext.getBean(TestCaseExecutionStatisticsServiceImpl.class);

                List<BuildRevisionStatistics> buildRev = tceStatsService.getListOfXLastBuildAndRev(MySystem, numberOfLastBR);

                ArrayList<ArrayList<String>> arrayBuildRevision = new ArrayList<ArrayList<String>>();
                List<List<List<String>>> arrayBuildRevisionEnv = new ArrayList<List<List<String>>>();
                ArrayList<ArrayList<ArrayList<String>>> arrayBuildRevisionContent = new ArrayList<ArrayList<ArrayList<String>>>();

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
                    arrayBuildRevision.add(al);


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
                    arrayBuildRevisionEnv.add(arrayEnv);

                    String contentSQL = "SELECT t.Build, t.Revision, "
                            + " t.application, t.release, t.link "
                            + "FROM buildrevisionparameters t "
                            + "Where t.build = ? and t.revision = ? "
                            + inSQL;
                    MyLogger.log(ExecutionPerBuildRevision.class.getName(), Level.DEBUG, contentSQL);
                    PreparedStatement stmtContent = connection.prepareStatement(contentSQL);

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
                        arrayBuildRevisionContent.add(array);
                    } finally {
                        stmtContent.close();
                    }
                }

                request.setAttribute("arrayExecution", arrayBuildRevision);
                request.setAttribute("arrayContent", arrayBuildRevisionContent);
                request.setAttribute("arrayExecutionEnv", arrayBuildRevisionEnv);

                /**
                 * Section that calculate the stats on application outside the
                 * main system
                 */
                arrayBuildRevision = new ArrayList<ArrayList<String>>();
                arrayBuildRevisionEnv = new ArrayList<List<List<String>>>();

                for (BuildRevisionStatistics buildRevList : buildRev) {

                    String build = buildRevList.getBuild();
                    String revision = buildRevList.getRevision();
                    List<String> env = new ArrayList<String>();

                    env.add("PROD");
                    env.add("UAT");
                    env.add("QA");
                    BuildRevisionStatistics globalStats = tceStatsService.getStatisticsOfExternalExecution(MySystem, build, revision, env);

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
                    arrayBuildRevision.add(al);


                    List<List<String>> arrayEnv = new ArrayList<List<String>>();
                    for (String e : env) {
                        BuildRevisionStatistics globalStatsEnv = tceStatsService.getStatisticsOfExternalExecution(MySystem, build, revision, e);
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
                    arrayBuildRevisionEnv.add(arrayEnv);

                }

                request.setAttribute("arrayExecutionExternal", arrayBuildRevision);
                request.setAttribute("arrayExecutionEnvExternal", arrayBuildRevisionEnv);




                request.getRequestDispatcher("/ExecutionPerBuildRevision.jsp").forward(request, response);
            }
        } catch (Exception ex) {
            request.getRequestDispatcher("/DatabaseMaintenance.jsp?GO=Y").forward(request, response);
            MyLogger.log(ExecutionPerBuildRevision.class.getName(), Level.FATAL, " Exception catched : " + ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ExecutionPerBuildRevision.class.getName(), org.apache.log4j.Level.WARN, e.toString());
            }
        }
    }
}
