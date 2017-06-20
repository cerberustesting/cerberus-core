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
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.servlet.api.EmptyResponse;
import org.cerberus.servlet.api.HttpMapper;
import org.cerberus.servlet.api.PostableHttpServlet;
import org.cerberus.servlet.api.info.PostableHttpServletInfo;
import org.cerberus.servlet.api.info.RequestParameter;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.cerberus.servlet.crud.testexecution.DeleteExecutionInQueue;
import org.cerberus.util.validity.Validity;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.Collections;

/**
 * @author abourdon
 */
@WebServlet(name = "ManageExecutionPool", urlPatterns = {"/ManageExecutionPool"})
public class ManageExecutionPool extends PostableHttpServlet<ManageExecutionPool.Request, EmptyResponse> {

    /* default */ static class Request implements Validity {

        private Action action;

        private CountryEnvironmentParameters.Key executionPoolKey;

        public Action getAction() {
            return action;
        }

        public CountryEnvironmentParameters.Key getExecutionPoolKey() {
            return executionPoolKey;
        }

        @Override
        public boolean isValid() {
            return action != null && executionPoolKey != null && executionPoolKey.isValid();
        }

    }

    private enum Action {
        PAUSE,
        RESUME,
        STOP
    }

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
    protected PostableHttpServletInfo getInfo() {
        return new PostableHttpServletInfo(
                DeleteExecutionInQueue.class.getSimpleName(),
                VERSION,
                "Apply a given ation to the given execution pool",
                new PostableHttpServletInfo.PostableUsage(
                        Collections.<RequestParameter>emptySet(),
                        Sets.newHashSet(
                                new RequestParameter("executionPoolKey", "the key of the execution pool to which apply the given action in the following format: system, application, country, environment"),
                                new RequestParameter("action", "the action to apply to the given execution pool. Could be PAUSE (to pause it), RESUME (to resume it if previously in pause) or STOP (to stop and delete it)")
                        )
                )
        );
    }

    @Override
    protected Class<Request> getRequestType() {
        return Request.class;
    }

    @Override
    protected EmptyResponse processRequest(final Request request) throws RequestProcessException {
        switch (request.getAction()) {
            case PAUSE:
                pauseExecutionPool(request.getExecutionPoolKey());
                break;
            case RESUME:
                resumeExecutionPool(request.getExecutionPoolKey());
                break;
            case STOP:
                stopExecutionPool(request.getExecutionPoolKey());
                break;
        }
        return new EmptyResponse();
    }

    private void pauseExecutionPool(final CountryEnvironmentParameters.Key executionPoolKey) {
        executionThreadPoolService.pauseExecutionThreadPool(executionPoolKey);
    }

    private void resumeExecutionPool(final CountryEnvironmentParameters.Key executionPoolKey) {
        executionThreadPoolService.resumeExecutionThreadPool(executionPoolKey);
    }

    private void stopExecutionPool(final CountryEnvironmentParameters.Key executionPoolKey) {
        executionThreadPoolService.removeExecutionThreadPool(executionPoolKey);
    }

}
