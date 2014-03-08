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
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Application;
import org.cerberus.entity.Invariant;
import org.cerberus.entity.User;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.IUserService;
import org.cerberus.service.impl.ApplicationService;
import org.cerberus.service.impl.InvariantService;
import org.cerberus.service.impl.UserService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
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

                IInvariantService invariantService = appContext.getBean(InvariantService.class);
                List<Invariant> myInvariants = invariantService.findInvariantByIdGp1("TCSTATUS", "Y");
                StringBuilder SQL = new StringBuilder();
                StringBuilder SQLa = new StringBuilder();
                StringBuilder SQLb = new StringBuilder();
                SQLa.append("SELECT t.Test, count(*) as TOTAL ");
                SQLb.append(" FROM testcase t ");
                for (Invariant i : myInvariants) {
                    i.getSort();
                    SQLa.append(", Col");
                    SQLa.append(String.valueOf(i.getSort()));
                    SQLb.append(" LEFT JOIN (SELECT g.test, count(*) as Col");
                    SQLb.append(String.valueOf(i.getSort()));
                    SQLb.append(" FROM testcase g WHERE Status = '");
                    SQLb.append(i.getValue());
                    SQLb.append("' ");
                    SQLb.append(inSQL);
                    SQLb.append(" GROUP BY Test) Tab");
                    SQLb.append(String.valueOf(i.getSort()));
                    SQLb.append(" ON Tab");
                    SQLb.append(String.valueOf(i.getSort()));
                    SQLb.append(".test=t.test ");
                }
                SQLb.append(" WHERE 1=1  ");
                SQLb.append(inSQL);
                SQLb.append(" GROUP BY test;");
                SQL.append(SQLa);
                SQL.append(SQLb);
                MyLogger.log(Homepage.class.getName(), Level.DEBUG, " SQL1 : " + SQL.toString());

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
                    ArrayList<Integer> totLine;
                    totLine = new ArrayList<Integer>();
                    for (Invariant i : myInvariants) {
                        totLine.add(0);
                    }

                    Integer j = 0;

                    String colName;
                    try {
                        while (rs_teststatus.next()) {
                            al = new ArrayList<String>();
                            al.add(rs_teststatus.getString("t.test"));
                            al.add(rs_teststatus.getInt("TOTAL") != 0 ? rs_teststatus.getString("TOTAL") : "");
                            tot += rs_teststatus.getInt("TOTAL");
                            j = 0;
                            for (Invariant i : myInvariants) {
                                colName = "Col" + String.valueOf(i.getSort());
                                al.add(rs_teststatus.getInt(colName) != 0 ? rs_teststatus.getString(colName) : "");
                                totLine.set(j, totLine.get(j) + rs_teststatus.getInt(colName));
                                j++;
                            }
                            arrayTest.add(al);
                        }
                    } finally {
                        rs_teststatus.close();
                    }
                    al = new ArrayList<String>();

                    al.add("-- GRAN TOTAL --");
                    al.add(tot.toString());

                    j = 0;
                    for (Invariant i : myInvariants) {
                        al.add(totLine.get(j).toString());
                        j++;
                    }

                    arrayTest.add(al);

                } finally {
                    stmt_teststatus.close();
                }


                request.setAttribute("arrayTest", arrayTest);
                request.getRequestDispatcher("/Homepage.jsp").forward(request, response);
            }
        } catch (Exception ex) {
            request.getRequestDispatcher("/DatabaseMaintenance.jsp").forward(request, response);
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
