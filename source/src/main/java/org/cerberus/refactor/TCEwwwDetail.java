/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.refactor;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


import java.util.List;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * @author bcivel
 */
@WebServlet(name = "TCEwwwDetail", urlPatterns = {"/TCEwwwDetail"})
public class TCEwwwDetail extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException  {
        String echo = httpServletRequest.getParameter("sEcho");
        String sStart = httpServletRequest.getParameter("iDisplayStart");
        String sAmount = httpServletRequest.getParameter("iDisplayLength");
        String sCol = httpServletRequest.getParameter("iSortCol_0");
        String sdir = httpServletRequest.getParameter("sSortDir_0");
        String dir = "asc";
        String[] cols = {"id","execID","start","url",
                        "end","ext","statusCode","method","bytes","timeInMillis","reqHeader_Host","resHeader_ContentType"};

        int start = 0;
        int amount = 0;
        int col = 0;

        if (sStart != null) {
        start = Integer.parseInt(sStart);
        if (start < 0)
            start = 0;
        }
        if (sAmount != null) {
        amount = Integer.parseInt(sAmount);
        if (amount < 10 || amount > 100)
            amount = 10;}
        if (sCol != null) {
        col = Integer.parseInt(sCol);
        if (col < 0 || col > 5)
            col = 0;
    }
    if (sdir != null) {
        if (!sdir.equals("asc"))
            dir = "desc";
    }
//    String colName = cols[col];

        JSONArray data = new JSONArray(); //data that will be shown in the table
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITCEwwwDetService tCEwwwDetService = appContext.getBean(ITCEwwwDetService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String id = policy.sanitize(httpServletRequest.getParameter("id"));

        List<TestcaseExecutionwwwDet> detailList = tCEwwwDetService.getListOfDetail(Integer.valueOf(id));

            try {
            JSONObject jsonResponse = new JSONObject();

            for (TestcaseExecutionwwwDet detail : detailList) {
                JSONArray row = new JSONArray();
                //"<a href=\'javascript:popup(\"qualitynonconformitydetails.jsp?ncid="+listofnonconformities.getIdqualitynonconformities()+"\")\'>"+listofnonconformities.getIdqualitynonconformities()+"</a>"
                row.put(detail.getId())
                   .put(detail.getExecID()).put(detail.getStart())
                   .put(detail.getUrl()).put(detail.getEnd())
                   .put(detail.getExt()).put(detail.getStatusCode())
                   .put(detail.getMethod())
                   .put(detail.getBytes())
                   .put(detail.getTimeInMillis()).put(detail.getReqHeader_Host())
                   .put(detail.getResHeader_ContentType());
                data.put(row);
            }
            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);

            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            httpServletResponse.setContentType("text/html");
            httpServletResponse.getWriter().print(e.getMessage());
        }
    }
}
