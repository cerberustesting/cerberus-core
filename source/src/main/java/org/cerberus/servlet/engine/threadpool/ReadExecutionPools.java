package org.cerberus.servlet.engine.threadpool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.util.json.ObjectMapperUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author abourdon
 */
@WebServlet(name = "ReadExecutionPools", urlPatterns = {"/ReadExecutionPools"})
public class ReadExecutionPools extends HttpServlet {

    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    private ObjectMapper objectMapper = ObjectMapperUtil.newInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        final IExecutionThreadPoolService executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);

        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);
        resp.getWriter().print(objectMapper.writeValueAsString(executionThreadPoolService.getStats()));
        resp.getWriter().flush();
    }

}
