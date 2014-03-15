/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cerberus.servlet.soaplibrary;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.SoapLibrary;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ISoapLibraryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cte
 */
public class FindAllSoapLibrary extends HttpServlet{
    
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
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        
        try {
            String echo = policy.sanitize(request.getParameter("sEcho"));
            String sStart = policy.sanitize(request.getParameter("iDisplayStart"));
            String sAmount = policy.sanitize(request.getParameter("iDisplayLength"));
            String sCol = policy.sanitize(request.getParameter("iSortCol_0"));
            String sdir = policy.sanitize(request.getParameter("sSortDir_0"));
            String dir = "asc";
            String[] cols = { "Name", "Type","Envelope", "Description", "ServicePath", "ParsingAnswer", "Method"};

            //JSONObject result = new JSONObject();
            //JSONArray array = new JSONArray();
            int amount = 10;
            int start = 0;
            int col = 0;

            String type = "";
            String name = "";
            String envelope = "";
            String description = "";
            String servicePath = "";
            String parsingAnswer = "";
            String method = "";
            
            name = policy.sanitize(request.getParameter("sSearch_0"));
            type = policy.sanitize(request.getParameter("sSearch_1"));
            envelope = policy.sanitize(request.getParameter("sSearch_2"));
            description = policy.sanitize(request.getParameter("sSearch_3"));
            servicePath = policy.sanitize(request.getParameter("sSearch_4"));
            parsingAnswer = policy.sanitize(request.getParameter("sSearch_5"));
            method = policy.sanitize(request.getParameter("sSearch_6"));

            List<String> sArray = new ArrayList<String>();
            if (!type.equals("")) {
                String sType = " `type` like '%" + type + "%'";
                sArray.add(sType);
            }
            if (!name.equals("")) {
                String sName = " `name` like '%" + name + "%'";
                sArray.add(sName);
            }
            if (!envelope.equals("")) {
                String sScript = " `envelope` like '%" + envelope + "%'";
                sArray.add(sScript);
            }
            if (!description.equals("")) {
                String sDescription = " `description` like '%" + description + "%'";
                sArray.add(sDescription);
            }
            if (!servicePath.equals("")) {
                String sServicePath = " `servicePath` like '%" + servicePath + "%'";
                sArray.add(sServicePath);
            }
            if (!parsingAnswer.equals("")) {
                String sParsingAnswer = " `parsingAnswer` like '%" + parsingAnswer + "%'";
                sArray.add(sParsingAnswer);
            }
            if (!method.equals("")) {
                String sMethod = " `parsingAnswer` like '%" + method + "%'";
                sArray.add(sMethod);
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
            ISoapLibraryService soapLibService = appContext.getBean(ISoapLibraryService.class);

            List<SoapLibrary> soapLibList = soapLibService.findSoapLibraryListByCriteria(start, amount, colName, dir, searchTerm, inds);

            JSONObject jsonResponse = new JSONObject();

            for (SoapLibrary soapLib : soapLibList) {
                JSONArray row = new JSONArray();
                row.put(soapLib.getName())
                        .put(soapLib.getType())
                        .put(soapLib.getEnvelope())
                        .put(soapLib.getDescription())
                        .put(soapLib.getEnvelope())
                        .put(soapLib.getParsingAnswer())
                        .put((soapLib.getMethod()));

                data.put(row);
            }
            
            Integer iTotalDisplayRecords = soapLibService.getNumberOfSoapLibraryPerCrtiteria(searchTerm, inds);
            Integer iTotalRecords = soapLibService.getNumberOfSoapLibraryPerCrtiteria("", "");
            
            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", iTotalRecords);
            jsonResponse.put("iTotalDisplayRecords", iTotalDisplayRecords);
            

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            MyLogger.log(FindAllSoapLibrary.class.getName(), Level.FATAL, ex.toString());
        } finally {
            out.close();
        }
    }

}
