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

import org.apache.log4j.Level;
import org.cerberus.entity.ExecutionUUID;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ITestCaseExecutionWWWService;
import org.cerberus.service.impl.TestCaseExecutionWWWService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 01/03/2013
 * @since 2.0.0
 */
@WebServlet(name = "SaveStatistic", urlPatterns = {"/SaveStatistic"})
public class SaveStatistic extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        MyLogger.log(SaveStatistic.class.getName(), Level.DEBUG, "Starting to save statistics Servlet.");
        
        int i = request.getParameter("logId").indexOf('?');
        String runId = request.getParameter("logId");
        String page = request.getParameter("logId").substring(i).split("=")[1];

        MyLogger.log(SaveStatistic.class.getName(), Level.INFO, " --> save statistics servlet parameters : runid=" + runId + " page=" + page);

        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseExecutionWWWService testCaseExecutionWWWService = appContext.getBean(TestCaseExecutionWWWService.class);
        ExecutionUUID executionUUID = appContext.getBean(ExecutionUUID.class);
        Integer executionId = executionUUID.getExecutionID(runId);
        
        testCaseExecutionWWWService.registerDetail(executionId, sb.toString(), page);

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
