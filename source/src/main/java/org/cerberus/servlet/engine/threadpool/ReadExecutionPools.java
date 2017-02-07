package org.cerberus.servlet.engine.threadpool;

import org.cerberus.engine.entity.threadpool.ExecutionThreadPoolStats;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.servlet.api.EmptyRequest;
import org.cerberus.servlet.api.GetableHttpServlet;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author abourdon
 */
@WebServlet(name = "ReadExecutionPools", urlPatterns = {"/ReadExecutionPools"})
public class ReadExecutionPools extends GetableHttpServlet<EmptyRequest, Collection<ExecutionThreadPoolStats>> {

    private IExecutionThreadPoolService executionThreadPoolService;

    @Override
    public void init() throws ServletException {
        super.init();
        executionThreadPoolService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean(IExecutionThreadPoolService.class);
    }

    @Override
    protected EmptyRequest parseRequest(final HttpServletRequest req) throws RequestParsingException {
        return new EmptyRequest();
    }

    @Override
    protected Collection<ExecutionThreadPoolStats> processRequest(final EmptyRequest emptyRequest) throws RequestProcessException {
        return executionThreadPoolService.getStats();
    }

    @Override
    protected String getUsageDescription() {
        return "No parameter needed";
    }
}
