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
package org.cerberus.servlet.crud.testexecution;

import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.api.EmptyResponse;
import org.cerberus.servlet.api.HttpMapper;
import org.cerberus.servlet.api.PostableHttpServlet;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.cerberus.util.validity.Validity;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.List;

/**
 * @author abourdon
 */
@WebServlet(name = "RunExecutionInQueue", urlPatterns = {"/RunExecutionInQueue"})
public class RunExecutionInQueue extends PostableHttpServlet<RunExecutionInQueue.Request, EmptyResponse> {

    /**
     * The associated request to this {@link DeleteExecutionInQueue}
     */
    public static class Request implements Validity {

        private List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        @Override
        public boolean isValid() {
            return ids != null && !ids.isEmpty();
        }
    }

    private IExecutionThreadPoolService executionThreadPoolService;

    public RunExecutionInQueue() {
        super(new DefaultJsonHttpMapper());
    }

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
        try {
            executionThreadPoolService.executeNextInQueue(request.getIds());
            return new EmptyResponse();
        } catch (CerberusException e) {
            throw new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to run executions in queue", e);
        }
    }

    @Override
    protected String getUsageDescription() {
        // TODO describe the Json object structure
        return "Need to have the list of execution in queue identifiers to run";
    }

}
