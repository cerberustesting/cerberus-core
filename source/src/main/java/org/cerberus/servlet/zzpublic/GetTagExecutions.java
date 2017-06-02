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
package org.cerberus.servlet.zzpublic;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.impl.LogEventService;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 */
@WebServlet(name = "GetTagExecutions", urlPatterns = {"/GetTagExecutions"})
public class GetTagExecutions extends HttpServlet {

    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
    private ITestCaseExecutionService testCaseExecutionService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createForPublicCalls("/GetTagExecutions", "CALL", "GetTagExecutions called : " + request.getRequestURL(), request);

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);

        String withUUID = policy.sanitize(request.getParameter("withUUID"));

        List<String> listOfTags;

        try{
            JSONObject jsonResponse = new JSONObject();
            if(withUUID != null && "true".equalsIgnoreCase(withUUID)) {
                listOfTags = testCaseExecutionService.findDistinctTag(true);
            } else {
                listOfTags = testCaseExecutionService.findDistinctTag(false);

            }
            
            jsonResponse.put("tags", listOfTags);
            
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
            
        }catch(CerberusException ex) {
            response.setContentType("text/html");
            response.getWriter().print(ex.getMessageError().getDescription());

        }catch(JSONException ex) {
            response.setContentType("text/html");
            response.getWriter().print(ex.getMessage());

        }

    }

}
