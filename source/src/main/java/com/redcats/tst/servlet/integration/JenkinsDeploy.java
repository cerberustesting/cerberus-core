package com.redcats.tst.servlet.integration;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.redcats.tst.service.IParameterService;
import com.redcats.tst.service.impl.ParameterService;
import com.redcats.tst.util.HTTPSession;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import version.Version;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

/**
 * Servlet called from JQuery Datatable to request Jenkins to run deploy
 * pipeline
 *
 * @author Pete
 */
@WebServlet(name = "JenkinsDeploy", urlPatterns = {"/JenkinsDeploy"})
public class JenkinsDeploy extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IParameterService parameterService = appContext.getBean(ParameterService.class);

            String user = parameterService.findParameterByKey("jenkins_admin_user").getValue();
            String pass = parameterService.findParameterByKey("jenkins_admin_password").getValue();


            HTTPSession session = new HTTPSession();
            session.startSession(user, pass);

            String url = parameterService.findParameterByKey("jenkins_deploy_url").getValue();
            String final_url;
            final_url = url.replaceAll("%APPLI%", request.getParameter("application"));
            final_url = final_url.replaceAll("%JENKINSBUILDID%", request.getParameter("jenkinsbuildid"));
            final_url = final_url.replaceAll("%DEPLOYTYPE%", request.getParameter("deploytype"));
            final_url = final_url.replaceAll("%JENKINSAGENT%", request.getParameter("jenkinsagent"));
            final_url = final_url.replaceAll("%RELEASE%", request.getParameter("release"));

            //send request to Jenkins
            Integer responseCode = session.getURL(final_url);
            session.closeSession();
            if (responseCode != 200) {
                out.print("ERROR Contacting Jenkins HTTP Response " + responseCode);
            } else {
                out.print("Sent request : " + url);
            }

        } catch (Exception ex) {
            Logger.getLogger(JenkinsDeploy.class.getName()).log(Level.SEVERE,
                    Version.PROJECT_NAME_VERSION + " - Exception catched.", ex);
        }

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
