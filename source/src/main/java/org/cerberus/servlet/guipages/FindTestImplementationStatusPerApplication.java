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
package org.cerberus.servlet.guipages;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.impl.InvariantService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "FindTestImplementationStatusPerApplication", urlPatterns = {"/FindTestImplementationStatusPerApplication"})
public class FindTestImplementationStatusPerApplication extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(FindTestImplementationStatusPerApplication.class);
    
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
        String mySystem = policy.sanitize(request.getParameter("MySystem"));
        String application = policy.sanitize(request.getParameter("Application"));
        Connection connection = null;

        JSONObject jsonResponse = new JSONObject();

        try {

            List<String> sArray = new ArrayList<String>();
            if (!mySystem.equals("")) {
                String smySystem = " `system` like '%" + mySystem + "%'";
                sArray.add(smySystem);
            }

            JSONArray data = new JSONArray();

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IInvariantService invariantService = appContext.getBean(InvariantService.class);
            DatabaseSpring database = appContext.getBean(DatabaseSpring.class);
            connection = database.connect();

            AnswerList<Invariant> answer = invariantService.readByIdnameGp1("TCSTATUS", "Y");
            List<Invariant> myInvariants = answer.getDataList();
            StringBuilder SQL = new StringBuilder();
            StringBuilder SQLa = new StringBuilder();
            StringBuilder SQLb = new StringBuilder();
            SQLa.append("SELECT t.test, count(*) as TOTAL ");
            SQLb.append(" FROM testcase t ");
            for (Invariant i : myInvariants) {
                i.getSort();
                SQLa.append(", Col");
                SQLa.append(String.valueOf(i.getSort()));
                SQLb.append(" LEFT JOIN (SELECT g.test, count(*) as Col");
                SQLb.append(String.valueOf(i.getSort()));
                SQLb.append(" FROM testcase g WHERE Status = '");
                SQLb.append(i.getValue());
                SQLb.append("' and application ='");
                SQLb.append(application);
                SQLb.append("' GROUP BY g.test) Tab");
                SQLb.append(String.valueOf(i.getSort()));
                SQLb.append(" ON Tab");
                SQLb.append(String.valueOf(i.getSort()));
                SQLb.append(".test=t.test ");
            }
            SQLb.append(" where t.application ='");
            SQLb.append(application);
            SQLb.append("'");
            SQLb.append(" group by t.test");
            SQL.append(SQLa);
            SQL.append(SQLb);
            LOG.debug(" SQL1 : " + SQL.toString());

            PreparedStatement stmt_teststatus = connection.prepareStatement(SQL.toString());
            try(ResultSet rs_teststatus = stmt_teststatus.executeQuery();) {
                //Integer tot = 0;
                List<Integer> totLine;
                totLine = new ArrayList<Integer>();
                for (Invariant i : myInvariants) {
                    totLine.add(0);
                }

                try {
                    while (rs_teststatus.next()) {
                        JSONArray row = new JSONArray();
                        StringBuilder testLink = new StringBuilder();
                        testLink.append("<a href=\"TestCaseList.jsp?test=");
                        testLink.append(rs_teststatus.getString("t.test"));
                        testLink.append("\">");
                        testLink.append(rs_teststatus.getString("t.test"));
                        testLink.append("</a>");
                        row.put(testLink.toString());
                        row.put(rs_teststatus.getString("TOTAL"));
                        for (Invariant i : myInvariants) {
                            i.getSort();
                            row.put(rs_teststatus.getString("Col" + String.valueOf(i.getSort())));
                        }
                        data.put(row);
                    }

                    //data that will be shown in the table

                    jsonResponse.put("aaData", data);
                    jsonResponse.put("sEcho", echo);
                    jsonResponse.put("iTotalRecords", data.length());
                    jsonResponse.put("iTotalDisplayRecords", data.length());

                    response.setContentType("application/json");
                    response.getWriter().print(jsonResponse.toString());
                } catch (JSONException ex) {
                    LOG.warn(ex.toString());
                } finally {
                    out.close();
                }
            } catch (SQLException ex) {
                LOG.warn(" Exception trying to query '"+SQL.toString()+"' : " + ex);
            } finally {
                stmt_teststatus.close();
            }
        } catch (Exception ex) {
            LOG.warn(" Exception catched : " + ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
    }
}
