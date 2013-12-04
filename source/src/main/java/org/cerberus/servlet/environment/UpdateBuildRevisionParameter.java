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
package org.cerberus.servlet.environment;

import org.cerberus.database.DatabaseSpring;
import org.cerberus.log.MyLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.version.Version;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bcivel
 */
@WebServlet(name = "UpdateBuildRevisionParameter", urlPatterns = {"/UpdateBuildRevisionParameter"})
public class UpdateBuildRevisionParameter extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {

            //Get the data from the page
            String whereclause = " 1 = 1 ";


            String buildFilter = "";
            if (request.getParameter("ubcBuildFilter") != null && !request.getParameter("ubcBuildFilter").equals("ALL")) {
                buildFilter = request.getParameter("ubcBuildFilter");
                whereclause = whereclause + " and Build = '" + buildFilter + "'";
            }

            String revisionFilter = "";
            if (request.getParameter("ubcRevisionFilter") != null && !request.getParameter("ubcRevisionFilter").equals("ALL")) {
                revisionFilter = request.getParameter("ubcRevisionFilter");
                whereclause = whereclause + " and Revision = '" + revisionFilter + "'";
            } else {
                if (request.getParameter("ubcRevisionFilter").equals("ALL")) {
                    revisionFilter = request.getParameter("ubcRevisionFilter");
                }
            }

            String[] delete = new String[0];
            if (request.getParameterValues("ubcDelete") != null) {
                delete = request.getParameterValues("ubcDelete");
            }

            String[] build = {"", ""};
            if (request.getParameterValues("ubcBuild") != null) {
                build = request.getParameterValues("ubcBuild");
            }

            String[] revision = {"", ""};
            if (request.getParameterValues("ubcRevision") != null) {
                revision = request.getParameterValues("ubcRevision");
            }

            String[] application = {"", ""};
            if (request.getParameterValues("ubcApplication") != null) {
                application = request.getParameterValues("ubcApplication");
            }

            String[] release = {"", ""};
            if (request.getParameterValues("ubcRelease") != null) {
                release = request.getParameterValues("ubcRelease");
            }

            String[] releaseID = {"", ""};
            if (request.getParameterValues("ubcReleaseID") != null) {
                releaseID = request.getParameterValues("ubcReleaseID");
            }


            String[] link = {"", ""};
            if (request.getParameterValues("ubcLink") != null) {
                link = request.getParameterValues("ubcLink");
            }

            String[] releaseOwner = {"", ""};
            if (request.getParameterValues("ubcReleaseOwner") != null) {
                releaseOwner = request.getParameterValues("ubcReleaseOwner");
            }

            String[] project = {"", ""};
            if (request.getParameterValues("ubcProject") != null) {
                project = request.getParameterValues("ubcProject");
            }

            String[] bugIDFixed = {"", ""};
            if (request.getParameterValues("ubcBugIDFixed") != null) {
                bugIDFixed = request.getParameterValues("ubcBugIDFixed");
            }

            String[] ticketIDFixed = {"", ""};
            if (request.getParameterValues("ubcTicketIDFixed") != null) {
                ticketIDFixed = request.getParameterValues("ubcTicketIDFixed");
            }

            String[] subject = {"", ""};
            if (request.getParameterValues("ubcSubject") != null) {
                subject = request.getParameterValues("ubcSubject");
            }
            //End of Get Data

            //Put all the data into a List
            List<String[]> build_rev_release = new ArrayList<String[]>();
            build_rev_release.add(build);
            build_rev_release.add(revision);
            build_rev_release.add(application);
            build_rev_release.add(release);
            build_rev_release.add(link);
            build_rev_release.add(releaseID);
            build_rev_release.add(releaseOwner);
            build_rev_release.add(project);
            build_rev_release.add(bugIDFixed);
            build_rev_release.add(ticketIDFixed);
            build_rev_release.add(subject);

            //Select all the existing data for the build/revision selected
            Statement stmt = connection.createStatement();
            List<String> releaseIDList = new ArrayList<String>();
            try {
                ResultSet rs_bcContent = stmt.executeQuery("SELECT ID " + " FROM buildrevisionparameters WHERE " + whereclause);
                try {
                    //Put all the data from the database into a List
                    if (rs_bcContent.first()) {
                        do {
                            releaseIDList.add(rs_bcContent.getString("ID"));
                        } while (rs_bcContent.next());
                    }
                } finally {
                    rs_bcContent.close();
                }
            } finally {
                stmt.close();
            }


            //For all the data from the webpage
            for (int i = 0; i < releaseID.length; i++) {
                // Update if the data already exists
                if (releaseIDList.contains(releaseID[i])) {

                    String update = (" UPDATE buildrevisionparameters "
                            + " SET `Link` = '" + link[i] + "' , `releaseOwner` = '" + releaseOwner[i] + "'"
                            + " , subject = '" + subject[i] + "' "
                            + " , `build` = '" + build[i] + "' "
                            + " , `revision` = '" + revision[i] + "' "
                            + " , `release` = '" + release[i] + "' "
                            + " , `Project` = '" + project[i] + "'"
                            + " , `bugIDFixed` = '" + bugIDFixed[i] + "'"
                            + " , `TicketIDFixed` = '" + ticketIDFixed[i] + "'"
                            + " , `application` = '" + application[i] + "'"
                            + " WHERE ID = '" + releaseID[i] + "'");
                    Statement stmt2 = connection.createStatement();
                    try {
                        stmt2.executeUpdate(update);
                    } finally {
                        stmt2.close();
                    }
                } else
                // Insert if it's a new one
                {
                    String insert = ("INSERT INTO buildrevisionparameters (`Build`,`Revision`,`Release`,`Link` , `Application`, `releaseOwner`, `Project`, `BugIDFixed`, `TicketIDFixed` , `Subject`) "
                            + " VALUES('"
                            + build[i]
                            + "', "
                            + "'"
                            + revision[i]
                            + "', '"
                            + release[i]
                            + "' , "
                            + "'"
                            + link[i]
                            + "', "
                            + "'"
                            + application[i]
                            + "', "
                            + "'"
                            + releaseOwner[i]
                            + "', "
                            + "'"
                            + project[i]
                            + "', "
                            + "'"
                            + bugIDFixed[i]
                            + "', "
                            + "'"
                            + ticketIDFixed[i]
                            + "', "
                            + "'"
                            + subject[i] + "')");
                    // System.out.println ( sql ) ;
                    Statement stmt3 = connection.createStatement();
                    try {
                        stmt3.executeUpdate(insert);
                    } finally {
                        stmt3.close();
                    }
                }
            }

            //DELETE THE SELECTED LINES
            if (delete != null) { // If some properties
                // check for delete
                for (int i = 0; i < delete.length; i++) {
                    Statement stmt4 = connection.createStatement();
                    try {
                        stmt4.execute("DELETE FROM buildrevisionparameters " + " WHERE `ID` = '" + delete[i] + "'");
                    } finally {
                        stmt4.close();
                    }
                }
            }

            // Now that all updates are done, if we are updating the build/release NONE/NONE,
            //  we update also the earlier release inside the updated applications.
            if ((request.getParameter("ubcRevisionFilter").equalsIgnoreCase("NONE")) &&
                    (request.getParameter("ubcBuildFilter").equalsIgnoreCase("NONE"))) {

                String SQL1 = "SELECT application, max(`release`) maxrel "
                        + " FROM  buildrevisionparameters  "
                        + " WHERE build<>'NONE' AND revision<>'NONE' "
                        + "    AND `release` NOT LIKE '%.%' AND `release` NOT LIKE '%e%' AND `release` NOT LIKE 'VC%' "
                        + " GROUP BY application;";
//                Logger.getLogger(UpdateContent.class.getName()).log(Level.INFO,
//					Version.PROJECT_NAME_VERSION + " - " + SQL1);
                Statement stmt5 = connection.createStatement();
                try {
                    ResultSet rsSQL1 = stmt5.executeQuery(SQL1);
                    try {
                        while (rsSQL1.next()) {

                            String SQL2 = "select build, revision, concat(build,revision) br "
                                    + " FROM  buildrevisionparameters  "
                                    + " WHERE application='" + rsSQL1.getString("application") + "' and `release`='" + rsSQL1.getString("maxrel") + "'"
                                    + " ORDER BY br desc; ";
//                Logger.getLogger(UpdateContent.class.getName()).log(Level.INFO,
//					Version.PROJECT_NAME_VERSION + " - " + SQL2);
                            Statement stmt6 = connection.createStatement();
                            try {
                                ResultSet rsSQL2 = stmt6.executeQuery(SQL2);
                                try {
                                    if (rsSQL2.first()) {
                                        String SQL3 = "UPDATE buildrevisionparameters "
                                                + " SET build='" + rsSQL2.getString("build") + "', revision='" + rsSQL2.getString("revision") + "' "
                                                + " WHERE Build='NONE' and Revision='NONE' "
                                                + "     and application='" + rsSQL1.getString("application") + "' "
                                                + "     and `release`<= '" + rsSQL1.getString("maxrel") + "' ";
//                Logger.getLogger(UpdateContent.class.getName()).log(Level.INFO,
//					Version.PROJECT_NAME_VERSION + " - " + SQL3);
                                        Statement stmt7 = connection.createStatement();
                                        try {
                                            stmt7.executeUpdate(SQL3);
                                        } finally {
                                            stmt7.close();
                                        }
                                    }
                                } finally {
                                    rsSQL2.close();
                                }
                            } finally {
                                stmt6.close();
                            }
                        }
                    } finally {
                        rsSQL1.close();
                    }
                } finally {
                    stmt5.close();
                }
            }

            //REDIRECT
            response.sendRedirect("BuildContent.jsp?build=" + buildFilter + "&revision=" + revisionFilter + "&FilterApply=Apply");


        } catch (SQLException ex) {
            Logger.getLogger(UpdateBuildRevisionParameter.class.getName()).log(Level.SEVERE,
                    Version.PROJECT_NAME_VERSION + " - Exception catched.", ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(UpdateBuildRevisionParameter.class.getName()).log(Level.SEVERE,
                    Version.PROJECT_NAME_VERSION + " - Exception catched.", ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            Logger.getLogger(UpdateBuildRevisionParameter.class.getName()).log(Level.SEVERE,
                    Version.PROJECT_NAME_VERSION + " - Exception catched.", ex);
        } catch (Exception ex) {
            Logger.getLogger(UpdateBuildRevisionParameter.class.getName()).log(Level.SEVERE,
                    Version.PROJECT_NAME_VERSION + " - Exception catched.", ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UpdateBuildRevisionParameter.class.getName(), org.apache.log4j.Level.WARN, e.toString());
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
