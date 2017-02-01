package org.cerberus.servlet.engine.threadpool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.threadpool.ExecutionWorkerThread;
import org.cerberus.engine.entity.threadpool.ManageableThreadPoolExecutor;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.util.json.ObjectMapperUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author abourdon
 */
@WebServlet(name = "ReadExecutionPool", urlPatterns = {"/ReadExecutionPool"})
public class ReadExecutionPool extends HttpServlet {

    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    private ObjectMapper objectMapper = ObjectMapperUtil.newInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Get the request key
        final CountryEnvironmentParameters.Key requestKey;
        try {
            requestKey = getBody(req);
            if (requestKey == null || !requestKey.isValid()) {
                throw new JsonSyntaxException("Invalid request body");
            }
        } catch (JsonSyntaxException e) {
            usage(req, resp);
            return;
        }

        // Get associated information to the request key
        final ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        final IExecutionThreadPoolService executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);

        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);

        final Map<ManageableThreadPoolExecutor.TaskState, List<ExecutionWorkerThread>> tasks = executionThreadPoolService.getTasks(requestKey);
        if (tasks == null) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        resp.getWriter().print(objectMapper.writeValueAsString(tasks));
        resp.getWriter().flush();
    }

    private CountryEnvironmentParameters.Key getBody(HttpServletRequest req) throws IOException, JsonSyntaxException {
        StringBuilder body = new StringBuilder();

        BufferedReader reader = new BufferedReader(req.getReader());
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            body.append(line);
        }

        return objectMapper.readValue(body.toString(), CountryEnvironmentParameters.Key.class);
    }

    private void usage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpStatus.BAD_REQUEST.value());
        resp.getWriter().print(
                String.format(
                        "{" +
                            "\"usage\": {" +
                                "\"description\": \"Get information about a Execution Thread Pool\"," +
                                "\"target\": \"POST %s\"," +
                                "\"body\": {" +
                                     "\"system\": \"<the execution pool's system>\"," +
                                     "\"application\": \"<the execution pool's application>\"," +
                                     "\"country\": \"<the execution pool's country>\"," +
                                     "\"environment\": \"<the execution pool's environment>\"" +
                                "}" +
                            "}" +
                        "}",
                        req.getRequestURL().toString()
                )
        );
        resp.getWriter().flush();
    }

}
