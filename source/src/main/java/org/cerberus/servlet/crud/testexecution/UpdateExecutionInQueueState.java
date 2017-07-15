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
import org.cerberus.crud.entity.TestCaseExecutionQueue;
import org.cerberus.exception.CerberusException;
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
import org.cerberus.crud.service.ITestCaseExecutionQueueService;

/**
 * Dedicated servlet to only set {@link TestCaseExecutionQueue}'s {@link TestCaseExecutionInQueue.State}
 *
 * @author abourdon
 */
@WebServlet(name = "UpdateExecutionInQueueState", urlPatterns = {"/UpdateExecutionInQueueState"})
public class UpdateExecutionInQueueState extends PostableHttpServlet<UpdateExecutionInQueueState.Request, UpdateExecutionInQueueState.Response> {

    /**
     * The associated request body to this {@link UpdateExecutionInQueueState}
     */
    public static class Request implements Validable {

        private TestCaseExecutionQueue.State state;

        private List<Long> ids;

        public TestCaseExecutionQueue.State getState() {
            return state;
        }

        public List<Long> getIds() {
            return ids;
        }


        @Override
        public Validity validate() {
            final Validity.Builder validity = Validity.builder();
            if (state == null || (state != TestCaseExecutionQueue.State.WAITING && state != TestCaseExecutionQueue.State.CANCELLED)) {
                validity.reason("`state` null or invalid (not WAITING nor CANCELLED)");
            }
            if (ids == null || ids.isEmpty()) {
                validity.reason("`ids` is null");
            }
            return validity.build();
        }
    }

    /**
     * The associated response to this {@link UpdateExecutionInQueueState}
     */
    public static class Response {

        private List<Long> inError;

        public Response(final List<Long> inError) {
            this.inError = inError;
        }

        public List<Long> getInError() {
            return inError;
        }
    }

    private HttpMapper httpMapper;
    private ITestCaseExecutionQueueService executionInQueueService;

    @Override
    public void postInit() throws ServletException {
        httpMapper = new DefaultJsonHttpMapper();
        executionInQueueService = getApplicationContext().getBean(ITestCaseExecutionQueueService.class);
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
                "Move the given list of executions in queue to the given state",
                new PostableHttpServletInfo.PostableUsage(
                        Collections.<RequestParameter>emptySet(),
                        Sets.newHashSet(
                                new RequestParameter("ids", "the list of execution in queue's identifier to move"),
                                new RequestParameter("state", "the state to which move the given list of execution in queue's. Could be WAITING (to wait for execution) or CANCELLED (to cancel execution).")
                        )
                )
        );
    }

    @Override
    protected Class<Request> getRequestType() {
        return Request.class;
    }

    @Override
    protected Response processRequest(final Request request) throws RequestProcessException {
        try {
            switch (request.getState()) {
                case WAITING:
                    return new Response(executionInQueueService.toWaiting(request.getIds()));
                case CANCELLED:
                    return new Response(executionInQueueService.toCancelled(request.getIds()));
                default:
                    throw new RequestProcessException(HttpStatus.BAD_REQUEST, "Invalid action");
            }
        } catch (CerberusException e) {
            throw new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to set test case executon in queues' state", e);
        }
    }

}
