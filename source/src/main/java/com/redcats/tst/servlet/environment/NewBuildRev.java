/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.environment;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.IParameterService;
import com.redcats.tst.service.impl.ParameterService;
import com.redcats.tst.serviceEmail.IEmailGeneration;
import com.redcats.tst.serviceEmail.impl.EmailGeneration;
import com.redcats.tst.serviceEmail.impl.sendMail;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import version.Version;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author vertigo
 */
@WebServlet(name = "NewBuildRev", urlPatterns = {"/NewBuildRev"})
public class NewBuildRev extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {

            String system = null;
            if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                system = request.getParameter("system");
            }
            String country = null;
            if (request.getParameter("country") != null && request.getParameter("country").compareTo("") != 0) {
                country = request.getParameter("country");
            }
            String env = null;
            if (request.getParameter("env") != null && request.getParameter("env").compareTo("") != 0) {
                env = request.getParameter("env");
            }
            String build = null;
            if (request.getParameter("build") != null && request.getParameter("build").compareTo("") != 0) {
                build = request.getParameter("build");
            }
            String rev = null;
            if (request.getParameter("revision") != null && request.getParameter("revision").compareTo("") != 0) {
                rev = request.getParameter("revision");
            }


            // Generate the content of the email
            IEmailGeneration emailGenerationService = appContext.getBean(EmailGeneration.class);

            //TODO remove passing connection
            String eMailContent = emailGenerationService.EmailGenerationRevisionChange(system, country, env, build, rev, connection);

            // Split the result to extract all the data
            String[] eMailContentTable = eMailContent.split("///");

            String to = eMailContentTable[0];
            String cc = eMailContentTable[1];
            String subject = eMailContentTable[2];
            String body = eMailContentTable[3];

            // Transaction and database update.
            Statement stmt = connection.createStatement();
            try {
                String req_update_active = "UPDATE countryenvparam "
                        + " SET Active='Y' , Build='" + build + "' , Revision='" + rev + "' "
                        + "WHERE `System`='" + system + "' and Country='" + country + "' and Environment='" + env + "'";
                stmt.executeUpdate(req_update_active);

                String req_insert_log = "INSERT INTO  countryenvparam_log "
                        + " ( `System`, `Country`, `Environment`, `Build`, `Revision`, `Description`) "
                        + " VALUES ('" + system + "', '" + country + "', '" + env + "', '" + build + "', '" + rev + "', 'New Sprint Revision.') ";
                stmt.execute(req_insert_log);
            } finally {
                stmt.close();
            }

            // Email sending.
            // Search the From, the Host and the Port defined in the database
            String from;
            String host;
            int port;

            IParameterService parameterService = appContext.getBean(ParameterService.class);

            from = parameterService.findParameterByKey("integration_smtp_from").getValue();
            host = parameterService.findParameterByKey("integration_smtp_host").getValue();
            port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port").getValue());


            //sendMail Mail = new sendMail();
            sendMail.sendHtmlMail(host, port, body, subject, from, to, cc);

            response.sendRedirect("Environment.jsp?system=" + system + "&country=" + country + "&env=" + env);


        } catch (Exception e) {
            Logger.getLogger(NewBuildRev.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
            out.println(e.getMessage());
        } finally {
            out.close();
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(NewBuildRev.class.getName(), org.apache.log4j.Level.WARN, e.toString());
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
