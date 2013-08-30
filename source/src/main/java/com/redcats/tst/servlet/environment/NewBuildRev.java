/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.environment;

import com.redcats.tst.refactor.DbMysqlController;
import com.redcats.tst.service.IParameterService;
import com.redcats.tst.service.impl.ParameterService;
import com.redcats.tst.serviceEmail.IEmailGeneration;
import com.redcats.tst.serviceEmail.impl.EmailGeneration;
import com.redcats.tst.serviceEmail.impl.sendMail;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import version.Version;

/**
 *
 * @author vertigo
 */
@WebServlet(name = "NewBuildRev", urlPatterns = {"/NewBuildRev"})
public class NewBuildRev extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {

            //Create Connexion // Statement
            DbMysqlController db;
            db = new DbMysqlController();
            Connection conn = db.connect();


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
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IEmailGeneration emailGenerationService = appContext.getBean(EmailGeneration.class);

            String eMailContent = emailGenerationService.EmailGenerationRevisionChange(country, env, build, rev, conn);

            // Split the result to extract all the data
            String[] eMailContentTable = eMailContent.split("///");

            String to = eMailContentTable[0];
            String cc = eMailContentTable[1];
            String subject = eMailContentTable[2];
            String body = eMailContentTable[3];




            // Transaction and database update.
            Statement stmt = conn.createStatement();

            String req_update_active = "UPDATE countryenvparam "
                    + " SET Active='Y' , Build='" + build + "' , Revision='" + rev + "' "
                    + "WHERE Country='" + country + "' and Environment='" + env + "'";
            stmt.executeUpdate(req_update_active);

            String req_insert_log = "INSERT INTO  countryenvparam_log "
                    + " ( `Country`, `Environment`, `Build`, `Revision`, `Description`) "
                    + " VALUES ('" + country + "', '" + env + "', '" + build + "', '" + rev + "', 'New Sprint Revision.') ";
            stmt.execute(req_insert_log);


            stmt.close();

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

            conn.close();

            response.sendRedirect("Environment.jsp?country=" + country + "&env=" + env);


        } catch (Exception e) {
            Logger.getLogger(NewBuildRev.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
            out.println(e.getMessage());
        } finally {
            out.close();
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
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
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
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
