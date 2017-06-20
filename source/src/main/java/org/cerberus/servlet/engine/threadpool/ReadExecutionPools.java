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
package org.cerberus.servlet.engine.threadpool;

import org.cerberus.engine.entity.threadpool.ExecutionThreadPoolStats;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.servlet.api.EmptyRequest;
import org.cerberus.servlet.api.GetableHttpServlet;
import org.cerberus.servlet.api.HttpMapper;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author abourdon
 */
@WebServlet(name = "ReadExecutionPools", urlPatterns = {"/ReadExecutionPools"})
public class ReadExecutionPools extends GetableHttpServlet<EmptyRequest, Collection<ExecutionThreadPoolStats>> {

    private IExecutionThreadPoolService executionThreadPoolService;

    public ReadExecutionPools() {
        super(new DefaultJsonHttpMapper());
    }

    @Override
    public void postInit() throws ServletException {
        executionThreadPoolService = getApplicationContext().getBean(IExecutionThreadPoolService.class);
    }

    @Override
    protected EmptyRequest parseRequest(final HttpServletRequest req) throws RequestParsingException {
        return new EmptyRequest();
    }

    @Override
    protected Collection<ExecutionThreadPoolStats> processRequest(final EmptyRequest emptyRequest) throws RequestProcessException {
        return executionThreadPoolService.getStats();
    }

    @Override
    protected String getUsageDescription() {
        return "No parameter needed";
    }
}
