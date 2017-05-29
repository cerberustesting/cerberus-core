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
package org.cerberus.servlet.crud.usermanagement;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.UserGroup;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.entity.UserSystem;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.IUserGroupService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.crud.service.IUserSystemService;
import org.cerberus.crud.service.impl.UserGroupService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class FindAllUsers extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
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
            String echo = request.getParameter("sEcho");
            String sStart = request.getParameter("iDisplayStart");
            String sAmount = request.getParameter("iDisplayLength");
            String sCol = request.getParameter("iSortCol_0");
            String sdir = request.getParameter("sSortDir_0");
            String dir = "asc";
            String[] cols = {"Groups", "Login", "Name", "Team", "Systems", "DefaultSystem", "Request New Password", "Email"};

            int amount = 10;
            int start = 0;
            int col = 0;

            String login = "";
            String name = "";
            String defaultSystem = "";
            String team = "";
            String requestNewPassword = "";
            String email = "";

            login = request.getParameter("sSearch_1");
            name = request.getParameter("sSearch_2");
            defaultSystem = request.getParameter("sSearch_5");
            team = request.getParameter("sSearch_3");
            requestNewPassword = request.getParameter("sSearch_6");
            email = request.getParameter("sSearch_7");

            List<String> sArray = new ArrayList<String>();
            if (!login.equals("")) {
                String slogin = " `login` like '%" + login + "%'";
                sArray.add(slogin);
            }
            if (!name.equals("")) {
                String sname = " name like '%" + name + "%'";
                sArray.add(sname);
            }
            if (!defaultSystem.equals("")) {
                String sdefaultSystem = " `defaultSystem` like '%" + defaultSystem + "%'";
                sArray.add(sdefaultSystem);
            }
            if (!team.equals("")) {
                String steam = " team like '%" + team + "%'";
                sArray.add(steam);
            }
            if (!requestNewPassword.equals("")) {
                String srequestNewPassword = " `request` like '%" + requestNewPassword + "%'";
                sArray.add(srequestNewPassword);
            }
            if (!email.equals("")) {
                String semail = " email like '%" + email + "%'";
                sArray.add(semail);
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
            IUserService userService = appContext.getBean(IUserService.class);
            IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);
            IUserGroupService userGroupService = appContext.getBean(UserGroupService.class);

            List<User> userList = userService.findUserListByCriteria(start, amount, colName, dir, searchTerm, inds);

            JSONObject jsonResponse = new JSONObject();
            try {
                for (User myUser : userList) {
                    JSONObject u = new JSONObject();
                    u.put("login", myUser.getLogin());
                    u.put("name", myUser.getName());
                    u.put("team", myUser.getTeam());
                    u.put("defaultSystem", myUser.getDefaultSystem());
                    u.put("request", myUser.getRequest());
                    u.put("email", myUser.getEmail());

                    JSONArray groups = new JSONArray();
                    for (UserGroup group : userGroupService.findGroupByKey(myUser.getLogin())) {
                        groups.put(group.getGroup());
                    }
                    u.put("group", groups);

                    JSONArray systems = new JSONArray();
                    for (UserSystem sys : userSystemService.findUserSystemByUser(myUser.getLogin())) {
                        systems.put(sys.getSystem());
                    }
                    u.put("system", systems);

                    data.put(u);
                }
            } catch (CerberusException ex) {
                response.setContentType("text/html");
                response.getWriter().print(ex.getMessageError().getDescription());

            }
            Integer iTotalRecords = userService.getNumberOfUserPerCrtiteria("", "");
            Integer iTotalDisplayRecords = userService.getNumberOfUserPerCrtiteria(searchTerm, inds);

            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", iTotalRecords);
            jsonResponse.put("iTotalDisplayRecords", iTotalDisplayRecords);
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            MyLogger.log(GetUsers.class.getName(), org.apache.log4j.Level.FATAL, "" + e);
            response.setContentType("text/html");
            response.getWriter().print(e.getMessage());
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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
