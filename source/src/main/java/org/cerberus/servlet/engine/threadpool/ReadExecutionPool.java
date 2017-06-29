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

import com.google.common.collect.Sets;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.threadpool.ExecutionWorkerThread;
import org.cerberus.engine.entity.threadpool.ManageableThreadPoolExecutor;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.servlet.api.GetableHttpServlet;
import org.cerberus.servlet.api.SinglePointHttpServlet;
import org.cerberus.servlet.api.info.GetableHttpServletInfo;
import org.cerberus.servlet.api.mapper.HttpMapper;
import org.cerberus.servlet.api.PostableHttpServlet;
import org.cerberus.servlet.api.info.PostableHttpServletInfo;
import org.cerberus.servlet.api.info.RequestParameter;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.cerberus.servlet.crud.testexecution.DeleteExecutionInQueue;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author abourdon
 */
@WebServlet(name = "ReadExecutionPool", urlPatterns = {"/ReadExecutionPool"})
public class ReadExecutionPool extends GetableHttpServlet<CountryEnvironmentParameters.Key, Map<ManageableThreadPoolExecutor.TaskState, List<ExecutionWorkerThread>>> {
    
    public static final RequestParameter SYSTEM = new RequestParameter("system", "the execution pool's system from which getting information");
    public static final RequestParameter APPLICATION = new RequestParameter("application", "the execution pool's application from which getting information");
    public static final RequestParameter COUNTRY = new RequestParameter("country", "the execution pool's country from which getting information");
    public static final RequestParameter ENVIRONMENT = new RequestParameter("environment", "the execution pool's environment from which getting information");

    private static final String VERSION = "V1";

    private HttpMapper httpMapper;
    private IExecutionThreadPoolService executionThreadPoolService;

    @Override
    public void postInit() throws ServletException {
        httpMapper = new DefaultJsonHttpMapper();
        executionThreadPoolService = getApplicationContext().getBean(IExecutionThreadPoolService.class);
    }

    @Override
    public HttpMapper getHttpMapper() {
        return httpMapper;
    }

    @Override
    protected CountryEnvironmentParameters.Key parseRequest(final HttpServletRequest req) throws RequestParsingException {
        try {
            return new CountryEnvironmentParameters.Key(
                    ParameterParserUtil.parseStringParamAndSanitize(req.getParameter(SYSTEM.getName()), null),
                    ParameterParserUtil.parseStringParamAndSanitize(req.getParameter(APPLICATION.getName()), null),
                    ParameterParserUtil.parseStringParamAndSanitize(req.getParameter(COUNTRY.getName()), null),
                    ParameterParserUtil.parseStringParamAndSanitize(req.getParameter(ENVIRONMENT.getName()), null)
            );
        } catch (final UnsupportedEncodingException e) {
            throw new RequestParsingException("Fail to decode parameter", e);
        }
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
    protected GetableHttpServletInfo getInfo() {
        return new GetableHttpServletInfo(
                DeleteExecutionInQueue.class.getSimpleName(),
                VERSION,
                "Read information of a given execution pool",
                new GetableHttpServletInfo.GetableUsage(
                        Sets.newHashSet(
                                SYSTEM,
                                APPLICATION,
                                COUNTRY,
                                ENVIRONMENT
                        )
                )
        );
    }

}
