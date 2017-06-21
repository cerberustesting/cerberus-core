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

import com.google.common.collect.Sets;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.api.EmptyResponse;
import org.cerberus.servlet.api.mapper.HttpMapper;
import org.cerberus.servlet.api.PostableHttpServlet;
import org.cerberus.servlet.api.info.PostableHttpServletInfo;
import org.cerberus.servlet.api.info.RequestParameter;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.cerberus.util.validity.Validable;
import org.cerberus.util.validity.Validity;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.Collections;
import java.util.List;

/**
 * @author abourdon
 */
@WebServlet(name = "RunExecutionInQueue", urlPatterns = {"/RunExecutionInQueue"})
public class RunExecutionInQueue extends PostableHttpServlet<RunExecutionInQueue.Request, EmptyResponse> {

    /**
     * The associated request to this {@link DeleteExecutionInQueue}
     */
    public static class Request implements Validable {

        private List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        @Override
        public Validity validate() {
            final Validity.Builder validity = Validity.builder();
            if (ids == null || ids.isEmpty()) {
                validity.reason("`ids` is null or empty");
            }
            return validity.build();
        }
    }

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
                getVersion(),
                "Run the given list of executions in queue",
                new PostableHttpServletInfo.PostableUsage(
                        Collections.<RequestParameter>emptySet(),
                        Sets.newHashSet(
                                new RequestParameter("ids", "the list of execution in queue's identifier to run")
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
        try {
            executionThreadPoolService.executeNextInQueue(request.getIds());
            return new EmptyResponse();
        } catch (CerberusException e) {
            throw new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to run executions in queue", e);
        }
    }

}
