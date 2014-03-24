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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.cerberus.log.MyLogger;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.impl.ApplicationService;
import org.cerberus.service.impl.InvariantService;
import org.cerberus.util.SqlUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author ip100003
 */
@WebServlet(name = "Homepage", urlPatterns = {"/Homepage"})
public class Homepage extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String echo = policy.sanitize(request.getParameter("sEcho"));
        String sStart = policy.sanitize(request.getParameter("iDisplayStart"));
        String sAmount = policy.sanitize(request.getParameter("iDisplayLength"));
        String sCol = policy.sanitize(request.getParameter("iSortCol_0"));
        String sdir = policy.sanitize(request.getParameter("sSortDir_0"));
        String dir = "asc";
        String[] cols = {"Test", "Total", "Created", "Poorly Described", "Described", "To Be Implemented", "Poorly Implemented"
                , "Implemented", "Working", "Not Applicable"};
        String mySystem = policy.sanitize(request.getParameter("MySystem"));
        Connection connection = null;

//        JSONObject result = new JSONObject();
        JSONObject jsonResponse = new JSONObject();
//        JSONArray array = new JSONArray();
        int amount = 10;
        int start = 0;
        int col = 0;

        try {

//            if (mySystem != null) {
//            String smySystem = " (";
//            for (int a = 0; a < mySystem.length - 1; a++) {
//                smySystem += " stream like '%" + mySystem[a] + "%' or";
//            }
//            smySystem += " stream like '%" + mySystem[mySystem.length - 1] + "%') ";
//            sArray.add(smySystem);
//        }
            List<String> sArray = new ArrayList<String>();
            if (!mySystem.equals("")) {
                String smySystem = " `system` like '%" + mySystem + "%'";
                sArray.add(smySystem);
            }

            StringBuilder individualSearch = new StringBuilder();
            if (sArray.size() == 1) {
                individualSearch.append(sArray.get(0));
            } else if (sArray.size() > 1) {
                for (int i = 0; i < sArray.size() - 1; i++) {
                    individualSearch.append(sArray.get(i));
                    individualSearch.append(" and ");
                }
                individualSearch.append(sArray.get(sArray.size() - 1));
            }

            if (sStart != null) {
                start = Integer.parseInt(sStart);
                if (start < 0) {
                    start = 0;
                }
            }
            if (sAmount != null) {
                amount = Integer.parseInt(sAmount);
                if (amount < 10 || amount > 100) {
                    amount = 10;
                }
            }

            if (sCol != null) {
                col = Integer.parseInt(sCol);
                if (col < 0 || col > 5) {
                    col = 0;
                }
            }
            if (sdir != null) {
                if (!sdir.equals("asc")) {
                    dir = "desc";
                }
            }
            String colName = cols[col];

            String searchTerm = "";
            if (!request.getParameter("sSearch").equals("")) {
                searchTerm = request.getParameter("sSearch");
            }

            String inds = individualSearch.toString();

            JSONArray data = new JSONArray();

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IApplicationService applicationService = appContext.getBean(ApplicationService.class);
            IInvariantService invariantService = appContext.getBean(InvariantService.class);
            DatabaseSpring database = appContext.getBean(DatabaseSpring.class);
            connection = database.connect();

            List<Application> appliList = applicationService.findApplicationBySystem(mySystem);
            String inSQL = SqlUtil.getInSQLClause(appliList);

            if (!(inSQL.equalsIgnoreCase(""))) {
                inSQL = " and application " + inSQL + " ";
            } else {
                inSQL = " and application in ('') ";
            }

            StringBuilder gSearch = new StringBuilder();
            String searchSQL = "";
            if (!searchTerm.equals("") && !inds.equals("")) {
                searchSQL = gSearch.toString() + " and " + inds;
            } else if (!inds.equals("")) {
                searchSQL = " where " + inds;
            } else if (!searchTerm.equals("")) {
                searchSQL = gSearch.toString();
            }

            List<Invariant> myInvariants = invariantService.findInvariantByIdGp1("TCSTATUS", "Y");
            StringBuilder SQL = new StringBuilder();
            StringBuilder SQLa = new StringBuilder();
            StringBuilder SQLb = new StringBuilder();
            SQLa.append("SELECT t.application, count(*) as TOTAL ");
            SQLb.append(" FROM testcase t ");
            for (Invariant i : myInvariants) {
                i.getSort();
                SQLa.append(", Col");
                SQLa.append(String.valueOf(i.getSort()));
                SQLb.append(" LEFT JOIN (SELECT g.application, count(*) as Col");
                SQLb.append(String.valueOf(i.getSort()));
                SQLb.append(" FROM testcase g WHERE Status = '");
                SQLb.append(i.getValue());
                SQLb.append("' ");
                SQLb.append(inSQL);
                SQLb.append(" GROUP BY g.application) Tab");
                SQLb.append(String.valueOf(i.getSort()));
                SQLb.append(" ON Tab");
                SQLb.append(String.valueOf(i.getSort()));
                SQLb.append(".application=t.application ");
            }
            SQLb.append(" WHERE 1=1  ");
            SQLb.append(inSQL.replace("application", "t.application"));
            SQLb.append(" GROUP BY t.application ");
            SQLb.append("order by ");
            SQLb.append(colName);
            SQLb.append(" ");
            SQLb.append(dir);
            SQLb.append(" limit ");
            SQLb.append(start);
            SQLb.append(" , ");
            SQLb.append(amount);

            SQL.append(SQLa);
            SQL.append(SQLb);
            MyLogger.log(Homepage.class.getName(), Level.DEBUG, " SQL1 : " + SQL.toString());

            PreparedStatement stmt_teststatus = connection.prepareStatement(SQL.toString());
            try {

                ResultSet rs_teststatus = stmt_teststatus.executeQuery();

//                Integer tot = 0;
                ArrayList<Integer> totLine;
                totLine = new ArrayList<Integer>();
                for (Invariant i : myInvariants) {
                    totLine.add(0);
                }

                try {
                    while (rs_teststatus.next()) {
                        JSONArray row = new JSONArray();
                        row.put(rs_teststatus.getString("t.application"));
                        row.put(rs_teststatus.getString("TOTAL"));
                        for (Invariant i : myInvariants) {
                            i.getSort();
                            row.put(rs_teststatus.getString("Col" + String.valueOf(i.getSort())));
                        }
                        //Integer numberOfTotalRows = datamapService.getNumberOfDatamapPerCrtiteria(searchTerm, inds);
                        data.put(row);
                    }

                    //data that will be shown in the table

                    jsonResponse.put("aaData", data);
                    jsonResponse.put("sEcho", echo);
                    jsonResponse.put("iTotalRecords", data.length());
                    jsonResponse.put("iDisplayLength", data.length());
                    jsonResponse.put("iTotalDisplayRecords", data.length());

                    response.setContentType("application/json");
                    response.getWriter().print(jsonResponse.toString());
                } catch (JSONException ex) {
                    MyLogger.log(Homepage.class.getName(), Level.FATAL, ex.toString());
                } finally {
                    out.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(Homepage.class.getName(), Level.FATAL, " Exception trying to query '"+SQL.toString()+"' : " + ex);
            } finally {
                stmt_teststatus.close();
            }
        } catch (Exception ex) {
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
