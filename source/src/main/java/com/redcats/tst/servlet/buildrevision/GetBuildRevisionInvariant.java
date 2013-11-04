/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.buildrevision;

import com.redcats.tst.entity.BuildRevisionInvariant;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.IBuildRevisionInvariantService;
import com.redcats.tst.service.impl.BuildRevisionInvariantService;
import com.redcats.tst.servlet.user.GetUsers;
import com.redcats.tst.util.ParameterParserUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author vertigo
 */
@WebServlet(name = "GetBuildRevisionInvariant", urlPatterns = {"/GetBuildRevisionInvariant"})
public class GetBuildRevisionInvariant extends HttpServlet {

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
        String echo = request.getParameter("sEcho");

        JSONArray arrayData = new JSONArray(); //data that will be shown in the table

        String MySystem = ParameterParserUtil.parseStringParam(request.getParameter("System"), "%");
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);
        try {
            JSONObject jsonResponse = new JSONObject();
            try {
                for (BuildRevisionInvariant myBuildRevisionInvariant : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystem(MySystem)) {
                    JSONObject row = new JSONObject();
                    row.put("system", myBuildRevisionInvariant.getSystem());
                    row.put("level", myBuildRevisionInvariant.getLevel());
                    row.put("seq", myBuildRevisionInvariant.getSeq());
                    row.put("versionName", myBuildRevisionInvariant.getVersionName());
                    arrayData.put(row);
                }
            } catch (CerberusException ex) {
                response.setContentType("text/html");
                response.getWriter().print(ex.getMessageError().getDescription());

            }
            jsonResponse.put("aaData", arrayData);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", arrayData.length());
            jsonResponse.put("iTotalDisplayRecords", arrayData.length());
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            MyLogger.log(GetUsers.class.getName(), Level.FATAL, "" + e);
            response.setContentType("text/html");
            response.getWriter().print(e.getMessage());
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
