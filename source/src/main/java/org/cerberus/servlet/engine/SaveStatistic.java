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
package org.cerberus.servlet.engine;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.cerberus.entity.ExecutionUUID;
import org.cerberus.service.ITestCaseExecutionwwwDetService;
import org.cerberus.service.impl.TestCaseExecutionwwwDetService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.1, 29/10/2014
 * @since 1.0.0
 */
@WebServlet(name = "SaveStatistic", urlPatterns = {"/SaveStatistic"})
public class SaveStatistic extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SaveStatistic.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to save statistics Servlet.");
        }

        int i = request.getParameter("logId").indexOf('?');
        String runId = request.getParameter("logId").substring(0, i);
        String page = request.getParameter("logId").substring(i).split("=")[1];

        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseExecutionwwwDetService testCaseExecutionwwwDetService = appContext.getBean(TestCaseExecutionwwwDetService.class);
        ExecutionUUID executionUUID = appContext.getBean(ExecutionUUID.class);
        long executionId = 0; 
                
        try{
        executionId = executionUUID.getExecutionID(runId);
        } catch (Exception ex){
           LOG.warn("Error getting the ExecutionID from the memory :" +ex.toString());
        }

        if (executionId!=0){
        LOG.info(" --> save statistics servlet parameters : runid=" + executionId + " page=" + page);
        testCaseExecutionwwwDetService.registerDetail(executionId, sb.toString(), page);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
