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
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.servlet.api.EmptyResponse;
import org.cerberus.servlet.api.HttpMapper;
import org.cerberus.servlet.api.PostableHttpServlet;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.cerberus.util.validity.Validity;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

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

    public ManageExecutionPool() {
        super(new DefaultJsonHttpMapper());
    }

    private IExecutionThreadPoolService executionThreadPoolService;

    @Override
    public void postInit() throws ServletException {
        executionThreadPoolService = getApplicationContext().getBean(IExecutionThreadPoolService.class);
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

    @Override
    protected String getUsageDescription() {
        // TODO describe the Json object structure
        return "Need to have the action to execute and the thread pool key from which execute action";
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
