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
package org.cerberus.servlet.guiPages;

import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Application;
import org.cerberus.entity.User;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.IUserService;
import org.cerberus.service.impl.ApplicationService;
import org.cerberus.service.impl.UserService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
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
                SQL.append(" GROUP BY Test) s "
                        + "on s.test=t.test "
                        + "left join "
                        + "(SELECT h.test, count(*) as WORKING from testcase h "
                        + "where Status = 'WORKING' ");
                SQL.append(inSQL);
                SQL.append(" GROUP BY Test) u "
                        + "on u.test=t.test "
                        + "left join "
                        + "(SELECT a.test, count(*) as STANDBY from testcase a "
                        + "where Status = 'Standby' ");
                SQL.append(inSQL);
                SQL.append(" GROUP BY Test) v "
                        + "on v.test=t.test "
                        + "left join "
                        + "(SELECT b.test, count(*) as INPROGRESS from testcase b "
                        + "where Status = 'IN PROGRESS' ");
                SQL.append(inSQL);
                SQL.append(" GROUP BY Test) w "
                        + "on w.test=t.test "
                        + "left join "
                        + "(SELECT i.test, count(*) as TBV from testcase i "
                        + "where Status = 'TO BE VALIDATED' ");
                SQL.append(inSQL);
                SQL.append(" GROUP BY Test) x "
                        + "on x.test=t.test "
                        + "left join "
                        + "(SELECT j.test, count(*) as TBD from testcase j "
                        + "where Status = 'TO BE DELETED' ");
                SQL.append(inSQL);
                SQL.append(" GROUP BY Test) y "
                        + "on y.test=t.test "
                        + "WHERE 1=1  ");
                SQL.append(inSQL);
                SQL.append(" GROUP BY test;");

                MyLogger.log(Homepage.class.getName(), Level.DEBUG, " SQL : " + SQL.toString());

                PreparedStatement stmt_teststatus = connection.prepareStatement(SQL.toString());
//                stmt_teststatus.setString(1, inSQL);
//                stmt_teststatus.setString(2, inSQL);
//                stmt_teststatus.setString(3, inSQL);
//                stmt_teststatus.setString(4, inSQL);
//                stmt_teststatus.setString(5, inSQL);
//                stmt_teststatus.setString(6, inSQL);
//                stmt_teststatus.setString(7, inSQL);

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


                request.setAttribute("arrayTest", arrayTest);
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
