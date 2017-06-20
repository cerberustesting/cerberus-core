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

import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.threadpool.ExecutionWorkerThread;
import org.cerberus.engine.entity.threadpool.ManageableThreadPoolExecutor;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.servlet.api.HttpMapper;
import org.cerberus.servlet.api.PostableHttpServlet;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.List;
import java.util.Map;

/**
 * @author abourdon
 */
@WebServlet(name = "ReadExecutionPool", urlPatterns = {"/ReadExecutionPool"})
public class ReadExecutionPool extends PostableHttpServlet<CountryEnvironmentParameters.Key, Map<ManageableThreadPoolExecutor.TaskState, List<ExecutionWorkerThread>>> {

    private IExecutionThreadPoolService executionThreadPoolService;

    public ReadExecutionPool() {
        super(new DefaultJsonHttpMapper());
    }

    @Override
    public void postInit() throws ServletException {
        executionThreadPoolService = getApplicationContext().getBean(IExecutionThreadPoolService.class);
    }

    @Override
    protected Map<ManageableThreadPoolExecutor.TaskState, List<ExecutionWorkerThread>> processRequest(final CountryEnvironmentParameters.Key key) throws RequestProcessException {
        final Map<ManageableThreadPoolExecutor.TaskState, List<ExecutionWorkerThread>> tasks = executionThreadPoolService.getTasks(key);
        if (tasks == null) {
            throw new RequestProcessException(HttpStatus.NOT_FOUND);
        }
        return tasks;
    }

    @Override
    protected String getUsageDescription() {
        // TODO describe the Json object structure
        return "Need to have the thread pool key from which read information";
    }

    @Override
    protected Class<CountryEnvironmentParameters.Key> getRequestType() {
        return CountryEnvironmentParameters.Key.class;
    }

}
