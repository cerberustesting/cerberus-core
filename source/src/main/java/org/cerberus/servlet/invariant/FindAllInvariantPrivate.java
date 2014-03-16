/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.servlet.invariant;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.Invariant;
import org.cerberus.service.IInvariantService;
import org.cerberus.util.ParameterParserUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class FindAllInvariantPrivate extends HttpServlet {

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
            String echo = request.getParameter("sEcho");
            String sStart = request.getParameter("iDisplayStart");
            String sAmount = request.getParameter("iDisplayLength");
            String sCol = request.getParameter("iSortCol_0");
            String sdir = request.getParameter("sSortDir_0");
            String dir = "asc";
            String[] cols = {"idname", "sort", "value", "description", "VeryShortDesc", "gp1", "gp2", "gp3"};

            JSONObject result = new JSONObject();
            JSONArray array = new JSONArray();
            int amount = 10;
            int start = 0;
            int col = 0;

            String sIdname = "";
            String sValue = "";

            sIdname = ParameterParserUtil.parseStringParam(request.getParameter("sSearch_0"), "");
            sValue = ParameterParserUtil.parseStringParam(request.getParameter("sSearch_2"), "");

            List<String> sArray = new ArrayList<String>();
            if (!sIdname.equals("")) {
                sArray.add(" `idname` like '%" + sIdname + "%'");
            }
            if (!sValue.equals("")) {
                sArray.add(" value like '%" + sValue + "%'");
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
            }else{
                amount = 10;
            }

            if (sCol != null) {
                col = Integer.parseInt(sCol);
                if (col < 0 || col > 10) {
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
            searchTerm = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");

            String inds = String.valueOf(individualSearch);

            JSONArray data = new JSONArray(); //data that will be shown in the table

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);

            List<Invariant> invariantList = invariantService.findInvariantPrivateListByCriteria(start, amount, colName, dir, searchTerm, inds);

            JSONObject jsonResponse = new JSONObject();

            for (Invariant InvariantData : invariantList) {
                JSONArray row = new JSONArray();
                row.put(InvariantData.getIdName())
                        .put(InvariantData.getValue())
                        .put(InvariantData.getSort())
                        .put(InvariantData.getDescription())
                        .put(InvariantData.getVeryShortDesc())
                        .put(InvariantData.getGp1())
                        .put(InvariantData.getGp2())
                        .put(InvariantData.getGp3());

                data.put(row);
            }
            Integer iTotalRecords = invariantService.getNumberOfPrivateInvariant("");
            Integer iTotalDisplayRecords = invariantService.getNumberOfPrivateInvariant(searchTerm);

            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            //Total records, before filtering (i.e. the total number of records in the database)
            jsonResponse.put("iTotalRecords", iTotalRecords);
            //Total records, after filtering (i.e. the total number of records after filtering has been applied - not just the number of records being returned in this result set)
            jsonResponse.put("iTotalDisplayRecords", iTotalDisplayRecords);

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            Logger.getLogger(FindAllInvariantPrivate.class.getName()).log(Level.SEVERE, null, ex);
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
