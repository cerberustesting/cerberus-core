/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.publi;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.log.MyLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import version.Version;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

/**
 * @author vertigo
 */
@WebServlet(name = "NewRelease", urlPatterns = {"/NewRelease"})
public class NewRelease extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String application = null;
        if (request.getParameter("application") != null && request.getParameter("application").compareTo("") != 0) {
            application = request.getParameter("application");
        } else {
            application = "";
        }
        String release = null;
        if (request.getParameter("release") != null && request.getParameter("release").compareTo("") != 0) {
            release = request.getParameter("release");
        } else {
            release = "";
        }
        String project = null;
        if (request.getParameter("project") != null && request.getParameter("project").compareTo("") != 0) {
            project = request.getParameter("project");
        } else {
            project = "";
        }
        String ticket = null;
        if (request.getParameter("ticket") != null && request.getParameter("ticket").compareTo("") != 0) {
            ticket = request.getParameter("ticket");
        } else {
            ticket = "";
        }
        String bug = null;
        if (request.getParameter("bug") != null && request.getParameter("bug").compareTo("") != 0) {
            bug = request.getParameter("bug");
        } else {
            bug = "";
        }
        String subject = null;
        if (request.getParameter("subject") != null && request.getParameter("subject").compareTo("") != 0) {
            subject = request.getParameter("subject");
        } else {
            subject = "";
        }
        String owner = null;
        if (request.getParameter("owner") != null && request.getParameter("owner").compareTo("") != 0) {
            owner = request.getParameter("owner");
        } else {
            owner = "";
        }
        String link = null;
        if (request.getParameter("link") != null && request.getParameter("link").compareTo("") != 0) {
            link = request.getParameter("link");
        } else {
            link = "";
        }

        // Those Parameters will be used when Integration application send the deploy request to Jenkins. 
        String jenkinsbuildid = null;
        if (request.getParameter("jenkinsbuildid") != null && request.getParameter("jenkinsbuildid").compareTo("") != 0) {
            jenkinsbuildid = request.getParameter("jenkinsbuildid");
        } else {
            jenkinsbuildid = "";
        }
        String mavengroupid = null;
        if (request.getParameter("mavengroupid") != null && request.getParameter("mavengroupid").compareTo("") != 0) {
            mavengroupid = request.getParameter("mavengroupid");
        } else {
            mavengroupid = "";
        }
        String mavenartifactid = null;
        if (request.getParameter("mavenartifactid") != null && request.getParameter("mavenartifactid").compareTo("") != 0) {
            mavenartifactid = request.getParameter("mavenartifactid");
        } else {
            mavenartifactid = "";
        }
        String mavenversion = null;
        if (request.getParameter("mavenversion") != null && request.getParameter("mavenversion").compareTo("") != 0) {
            mavenversion = request.getParameter("mavenversion");
        } else {
            mavenversion = "";
        }

        //Create Connexion // Statement
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {

            boolean error = false;

            // Application Verification. We verify here that the Application exist on the database.
            Statement stmt1 = connection.createStatement();
            try {
                String req_sel1 = "Select Application FROM  Application "
                        + " WHERE Application = '" + application + "' and internal = 'Y'";
                ResultSet rsBC1 = stmt1.executeQuery(req_sel1);
                try {
                    if (rsBC1.first() != true) {
                        out.println("Error - Application does not exist or not an internal application : " + application);
                        error = true;
                    }
                } finally {
                    rsBC1.close();
                }
            } finally {
                stmt1.close();
            }

            // User Verification. We verify here that the User exist on the database.
            Statement stmt2 = connection.createStatement();
            try {
                String req_sel2 = "Select Login FROM  User WHERE Login = '" + owner + "'";
                ResultSet rsBC2 = stmt2.executeQuery(req_sel2);
                try {
                    if (rsBC2.first() != true) {
                        out.println("Warning - User does not exist : " + owner);
                    }
                } finally {
                    rsBC2.close();
                }
            } finally {
                stmt2.close();
            }

            // Project Verification. We verify here that the Project exist on the database.
            Statement stmt3 = connection.createStatement();
            try {
                String req_sel3 = "Select idproject FROM  project WHERE idproject = '" + project + "'";
                ResultSet rsBC3 = stmt3.executeQuery(req_sel3);
                try {
                    if (rsBC3.first() != true) {
                        out.println("Warning - Project does not exist : " + project);
                    }
                } finally {
                    rsBC3.close();
                }
            } finally {
                stmt3.close();
            }

            // Starting the database update only when no blocking error has been detected.
            if (error == false) {
                // Transaction and database update.
                // Duplicate entry Verification. On the build/relivion not yet assigned (NONE/NONE),
                //  we verify that the application + release has not been submitted yet.
                //  if it exist, we update it in stead of inserting a new row.
                //  That coorespond in the cases where the Jenkins pipe is executed several times 
                //  on a single svn commit.
                Statement stmt4 = connection.createStatement();
                try {
                    String req_sel4 = "Select id FROM  buildrevisionparameters "
                            + " WHERE build='NONE' and revision='NONE' and application = '" + application + "' "
                            + "   and `release` = '" + release + "'";
                    ResultSet rsBC4 = stmt4.executeQuery(req_sel4);
                    try {
                        if (rsBC4.first()) {
                            out.println("Warning - Release entry already exist. Updating the existing entry : " + rsBC4.getString("id"));

                            String req_update = "UPDATE buildrevisionparameters "
                                    + "SET Project = '" + project + "', "
                                    + " TicketIDFixed = '" + ticket + "', "
                                    + " BugIDFixed = '" + bug + "', "
                                    + " Link = '" + link + "', "
                                    + " ReleaseOwner = '" + owner + "', "
                                    + " Subject = '" + subject + "', "
                                    + " jenkinsbuildid = '" + jenkinsbuildid + "', "
                                    + " mavengroupid = '" + mavengroupid + "', "
                                    + " mavenartifactid = '" + mavenartifactid + "', "
                                    + " mavenversion = '" + mavenversion + "'"
                                    + "WHERE id = '" + rsBC4.getString("id") + "' ";
                            Statement stmt = connection.createStatement();
                            try {
                                stmt.execute(req_update);
                            } finally {
                                stmt.close();
                            }
                        } else {
                            String req_insert = "INSERT INTO  buildrevisionparameters "
                                    + " ( `Build`, `Revision`, `Release`, `Application`"
                                    + ", `Project`, `TicketIDFixed`, `BugIDFixed`"
                                    + ", `Link`, `ReleaseOwner`, `Subject`, `jenkinsbuildid`"
                                    + ", `mavengroupid`, `mavenartifactid`, `mavenversion`) "
                                    + " VALUES ('NONE', 'NONE', '" + release + "', '" + application + "'"
                                    + ", '" + project + "', '" + ticket + "', '" + bug + "'"
                                    + ", '" + link + "', '" + owner + "', '" + subject + "', '" + jenkinsbuildid + "'"
                                    + ", '" + mavengroupid + "', '" + mavenartifactid + "', '" + mavenversion + "') ";
                            Statement stmt = connection.createStatement();
                            try {
                                stmt.execute(req_insert);
                            } finally {
                                stmt.close();
                            }
                            out.println("Release Inserted : '" + release + "' on '" + application + "' for user '" + owner + "'");
                        }
                    } finally {
                        rsBC4.close();
                    }
                } finally {
                    stmt4.close();
                }
            }

        } catch (Exception e) {
            Logger.getLogger(NewRelease.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
            out.print("Error while inserting the release : ");
            out.println(e.getMessage());
        } finally {
            out.close();
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(NewRelease.class.getName(), org.apache.log4j.Level.WARN, e.toString());
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
