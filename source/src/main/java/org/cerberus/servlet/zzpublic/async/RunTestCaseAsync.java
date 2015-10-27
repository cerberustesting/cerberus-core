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
import java.util.concurrent.ThreadPoolExecutor;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {Insert class description here}
 *
 * @author FNogueira
 * @version 1.0, 24/10/2015
 * @since 1.1.3
 */
@WebServlet(name = "RunTestCaseAsync", urlPatterns = {"/RunTestCaseAsync"},  asyncSupported=true)
public class RunTestCaseAsync extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        org.apache.log4j.Logger.getLogger(RunTestCaseAsync.class.getName()).log(org.apache.log4j.Level.DEBUG, "AsyncLongRunningServlet Start::Name="
                        + Thread.currentThread().getName() + "::ID="
                        + Thread.currentThread().getId());
         
        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

        
        AsyncContext asyncCtx = request.startAsync();
        asyncCtx.addListener(new AppAsyncListener());
        asyncCtx.setTimeout(300000);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) request.getServletContext().getAttribute("executor");
        executor.execute(new AsyncRequestProcessor(asyncCtx));
        
        long endTime = System.currentTimeMillis();
        
        org.apache.log4j.Logger.getLogger(RunTestCaseAsync.class.getName()).log(org.apache.log4j.Level.DEBUG, "AsyncLongRunningServlet End::Name="
                        + Thread.currentThread().getName() + "::ID="
                        + Thread.currentThread().getId() + "::Time Taken="
                        + (endTime - startTime) + " ms.");
        
    }

    
}
