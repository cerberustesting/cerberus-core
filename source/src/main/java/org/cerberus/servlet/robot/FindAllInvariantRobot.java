/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.servlet.robot;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.InvariantRobot;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IInvariantRobotService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class FindAllInvariantRobot extends HttpServlet {

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
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        try {
            String echo = policy.sanitize(request.getParameter("sEcho"));
            String sStart = policy.sanitize(request.getParameter("iDisplayStart"));
            String sAmount = policy.sanitize(request.getParameter("iDisplayLength"));
            String sCol = policy.sanitize(request.getParameter("iSortCol_0"));
            String sdir = policy.sanitize(request.getParameter("sSortDir_0"));
            String dir = "asc";
            String[] cols = {"Platform", "Os", "Browser", "Version"};

            int amount = 10;
            int start = 0;
            int col = 0;

            String platform = "";
            String browser = "";
            String version = "";
            String os = "";
            
            platform = policy.sanitize(request.getParameter("sSearch_1"));
            os = policy.sanitize(request.getParameter("sSearch_2"));
            browser = policy.sanitize(request.getParameter("sSearch_3"));
            version = policy.sanitize(request.getParameter("sSearch_4"));
            
            List<String> sArray = new ArrayList<String>();
            if (!platform.equals("")) {
                String sPlatform = " `platform` like '%" + platform + "%'";
                sArray.add(sPlatform);
            }
            if (!os.equals("")) {
                String sOs = " `os` like '%" + os + "%'";
                sArray.add(sOs);
            }
            if (!browser.equals("")) {
                String sBrowser = " `browser` like '%" + browser + "%'";
                sArray.add(sBrowser);
            }
            if (!version.equals("")) {
                String sVersion = " `version` like '%" + version + "%'";
                sArray.add(sVersion);
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

            String inds = String.valueOf(individualSearch);

            JSONArray data = new JSONArray(); //data that will be shown in the table

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IInvariantRobotService robotService = appContext.getBean(IInvariantRobotService.class);

            List<InvariantRobot> robotList = robotService.findInvariantRobotListByCriteria(start, amount, colName, dir, searchTerm, inds);

            JSONObject jsonResponse = new JSONObject();

            for (InvariantRobot robot : robotList) {
                JSONArray row = new JSONArray();
                row.put(robot.getId())
                        .put(robot.getPlatform())
                        .put(robot.getOs())
                        .put(robot.getBrowser())
                        .put(robot.getVersion());

                data.put(row);
            }

            Integer iTotalRecords = robotService.getNumberOfInvariantRobotPerCriteria("", "");
            Integer iTotalDisplayRecords = robotService.getNumberOfInvariantRobotPerCriteria(searchTerm, inds);

            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", iTotalRecords);
            jsonResponse.put("iTotalDisplayRecords", iTotalDisplayRecords);


            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException ex) {
            MyLogger.log(FindAllInvariantRobot.class.getName(), Level.FATAL, ex.toString());
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
