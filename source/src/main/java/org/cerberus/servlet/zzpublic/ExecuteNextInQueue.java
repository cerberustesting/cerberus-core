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
package org.cerberus.servlet.zzpublic;

import org.apache.log4j.Logger;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.exception.CerberusException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Executes the next test case contained into the execution queue.
 * <p>
 * <p>
 * No parameters needed.
 * </p>
 *
 * @author abourdon
 */
@WebServlet(name = "ExecuteNextInQueue", urlPatterns = {"/ExecuteNextInQueue"})
public class ExecuteNextInQueue extends HttpServlet {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = Logger.getLogger(ExecuteNextInQueue.class);

    private static final long serialVersionUID = 1L;

    private IExecutionThreadPoolService threadPoolService;

    @Override
    public void init() throws ServletException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        threadPoolService = appContext.getBean(IExecutionThreadPoolService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            threadPoolService.executeNextInQueue(1);
            resp.setStatus(HttpStatus.OK.value());
        } catch (CerberusException e) {
            LOG.warn("Unable to execute next in queue", e);
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

}
