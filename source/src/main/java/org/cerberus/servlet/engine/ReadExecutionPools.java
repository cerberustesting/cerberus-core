package org.cerberus.servlet.engine;

import com.google.gson.Gson;
import org.cerberus.engine.threadpool.ExecutionThreadPoolService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by aurel on 17/01/2017.
 */
@WebServlet(name = "ReadExecutionPools", urlPatterns = {"/ReadExecutionPools"})
public class ReadExecutionPools extends HttpServlet {

    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        final ExecutionThreadPoolService executionThreadPoolService = appContext.getBean(ExecutionThreadPoolService.class);

        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);
        resp.getWriter().print(gson.toJson(executionThreadPoolService.getStats()));
        resp.getWriter().flush();
    }

}
