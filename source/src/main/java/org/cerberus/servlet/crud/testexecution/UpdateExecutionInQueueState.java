package org.cerberus.servlet.crud.testexecution;

import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.api.PostableHttpServlet;
import org.cerberus.util.validity.Validity;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.List;

/**
 * Dedicated servlet to only set {@link TestCaseExecutionInQueue}'s {@link TestCaseExecutionInQueue.State}
 *
 * @author abourdon
 */
@WebServlet(name = "UpdateExecutionInQueueState", urlPatterns = {"/UpdateExecutionInQueueState"})
public class UpdateExecutionInQueueState extends PostableHttpServlet<UpdateExecutionInQueueState.Request, UpdateExecutionInQueueState.Response> {

    /**
     * The associated request body to this {@link UpdateExecutionInQueueState}
     */
    public static class Request implements Validity {

        private TestCaseExecutionInQueue.State state;

        private List<Long> ids;

        public TestCaseExecutionInQueue.State getState() {
            return state;
        }

        public List<Long> getIds() {
            return ids;
        }

        @Override
        public boolean isValid() {
            boolean stateValidity = state != null && (state == TestCaseExecutionInQueue.State.WAITING || state == TestCaseExecutionInQueue.State.CANCELLED);
            if (!stateValidity) {
                return false;
            }
            return ids != null && !ids.isEmpty();
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

    private ITestCaseExecutionInQueueService executionInQueueService;

    @Override
    public void init() throws ServletException {
        super.init();
        executionInQueueService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean(ITestCaseExecutionInQueueService.class);
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

    @Override
    protected String getUsageDescription() {
        // TODO describe the Json object structure
        return "Need to have the state and list of execution in queue identifiers to set";
    }

}
