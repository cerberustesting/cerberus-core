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
import org.apache.log4j.Logger;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.api.HttpMapper;
import org.cerberus.servlet.api.PostableHttpServlet;
import org.cerberus.servlet.api.info.PostableHttpServletInfo;
import org.cerberus.servlet.api.info.RequestParameter;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.cerberus.util.validity.Validity;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author bcivel
 * @author abourdon
 */
@WebServlet(name = "DeleteExecutionInQueue", urlPatterns = {"/DeleteExecutionInQueue"})
public class DeleteExecutionInQueue extends PostableHttpServlet<DeleteExecutionInQueue.Request, DeleteExecutionInQueue.Response> {

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

    /**
     * The associated response to this {@link DeleteExecutionInQueue}
     */
    public static class Response {

        private List<Long> inError;

        public Response() {
            inError = new ArrayList<>();
        }

        public List<Long> getInError() {
            return inError;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(DeleteExecutionInQueue.class);

    private HttpMapper httpMapper;
    private ITestCaseExecutionInQueueService executionInQueueService;

    @Override
    public void postInit() throws ServletException {
        httpMapper = new DefaultJsonHttpMapper();
        executionInQueueService = getApplicationContext().getBean(ITestCaseExecutionInQueueService.class);
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
                "Delete the given list of executions in queue",
                new PostableHttpServletInfo.PostableUsage(
                        Collections.<RequestParameter>emptySet(),
                        Sets.newHashSet(
                                new RequestParameter("ids", "the list of execution in queue' identifiers to delete")
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
        Response response = new Response();
        for (long idToRemove : request.getIds()) {
            try {
                executionInQueueService.remove(idToRemove);
            } catch (CerberusException e) {
                LOGGER.warn("Unable to remove execution in queue #" + idToRemove, e);
                response.getInError().add(idToRemove);
            }
        }
        return response;
    }

}
