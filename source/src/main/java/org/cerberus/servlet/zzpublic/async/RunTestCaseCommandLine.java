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
package org.cerberus.servlet.zzpublic.async;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.ExecutionSOAPResponse;
import org.cerberus.crud.entity.ExecutionUUID;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.Session;
import org.cerberus.crud.entity.SessionCapabilities;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.factory.IFactoryTCase;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.IRobotService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.engine.IRunTestCaseService;
import org.cerberus.service.engine.impl.RunTestCaseService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.version.Infos;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author FNogueira
 * @version 1.0, 24/10/2015
 * @since 1.1.3
 */
@WebServlet(name = "RunTestCaseCommandLine", urlPatterns = {"/RunTestCaseCommandLine"},  asyncSupported=true)
public class RunTestCaseCommandLine extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        org.apache.log4j.Logger.getLogger(RunTestCaseCommandLine.class.getName()).log(org.apache.log4j.Level.WARN, "AsyncLongRunningServlet Start::Name="
                        + Thread.currentThread().getName() + "::ID="
                        + Thread.currentThread().getId());
        
        System.out.println("AsyncLongRunningServlet Start::Name="
                        + Thread.currentThread().getName() + "::ID="
                        + Thread.currentThread().getId());

        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

        String test = request.getParameter("test");
        String testcase = request.getParameter("testcase");
        int secs = 10000;
        //int secs = Integer.valueOf(time);
        
        // max 10 seconds
        //if (secs > 10000)
        //        secs = 10000;

        AsyncContext asyncCtx = request.startAsync();
        asyncCtx.addListener(new AppAsyncListener());
        asyncCtx.setTimeout(300000);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) request.getServletContext().getAttribute("executor");
        executor.execute(new AsyncRequestProcessor(asyncCtx, secs));
        
        org.apache.log4j.Logger.getLogger(RunTestCaseCommandLine.class.getName()).log(org.apache.log4j.Level.WARN, "RunTestCaseCommandLine - Start::Name="
                        + Thread.currentThread().getName() + "::ID="
                        + Thread.currentThread().getId() + " TEST = " + test + "TESTCASE " + testcase);

        
        long endTime = System.currentTimeMillis();
        System.out.println("AsyncLongRunningServlet End::Name="
                        + Thread.currentThread().getName() + "::ID="
                        + Thread.currentThread().getId() + "::Time Taken="
                        + (endTime - startTime) + " ms.");
        org.apache.log4j.Logger.getLogger(RunTestCaseCommandLine.class.getName()).log(org.apache.log4j.Level.WARN, "AsyncLongRunningServlet End::Name="
                        + Thread.currentThread().getName() + "::ID="
                        + Thread.currentThread().getId() + "::Time Taken="
                        + (endTime - startTime) + " ms.");
        
        /*final AsyncContext asyncContext = req.startAsync();
        final PrintWriter writer = res.getWriter();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                writer.println("Done");
                asyncContext.complete();
            }
        };

        AsyncContext ctx=req.startAsync();
        Thread t = new Thread(new TaskExecutor(ctx) {

            @Override
            public void execute(Runnable r) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        t.start();*/
    }
}
