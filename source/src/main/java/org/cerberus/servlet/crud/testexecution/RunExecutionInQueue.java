package org.cerberus.servlet.crud.testexecution;

import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.api.EmptyResponse;
import org.cerberus.servlet.api.PostableHttpServlet;
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

    @Override
    public void init() throws ServletException {
        super.init();
        executionThreadPoolService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean(IExecutionThreadPoolService.class);
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
