/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.servlet.testdata;

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
import org.cerberus.entity.TestData;
import org.cerberus.service.ITestDataService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class FindAllTestData extends HttpServlet {

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
            String[] cols = {"Key", "Value", "Description"};

            /*            JSONObject result = new JSONObject();
             JSONArray array = new JSONArray();*/

            int amount = 10;
            int start = 0;
            int col = 0;

            String key = "";
            String value = "";

            key = request.getParameter("sSearch_0");
            value = request.getParameter("sSearch_1");

            List<String> sArray = new ArrayList<String>();
            if (!key.equals("")) {
                String sKey = " `key` like '%" + key + "%'";
                sArray.add(sKey);
            }
            if (!value.equals("")) {
                String sValue = " value like '%" + value + "%'";
                sArray.add(sValue);
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
            ITestDataService testDataService = appContext.getBean(ITestDataService.class);

            List<TestData> testList = testDataService.findTestDataListByCriteria(start, amount, colName, dir, searchTerm, inds);

            JSONObject jsonResponse = new JSONObject();

            for (TestData testData : testList) {
                JSONArray row = new JSONArray();
                row.put(testData.getKey())
                        .put(testData.getValue())
                        .put(testData.getDescription());

                data.put(row);
            }

            Integer iTotalRecords = testDataService.getNumberOfTestDataPerCriteria("", "");
            Integer iTotalDisplayRecords = testDataService.getNumberOfTestDataPerCriteria(searchTerm, inds);

            if (data.length() <= 0) {
                JSONArray row = new JSONArray();
                row.put("").put("").put("");
                data.put(row);
            }
            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", iTotalRecords);
            jsonResponse.put("iTotalDisplayRecords", iTotalDisplayRecords);

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            Logger.getLogger(FindAllTestData.class.getName()).log(Level.SEVERE, null, ex);
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
