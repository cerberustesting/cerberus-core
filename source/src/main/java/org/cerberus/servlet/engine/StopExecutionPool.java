package org.cerberus.servlet.engine;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.threadpool.ExecutionThreadPoolService;
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

/**
 * Created by aurel on 18/01/2017.
 */
@WebServlet(name = "StopExecutionPool", urlPatterns = {"/StopExecutionPool"})
public class StopExecutionPool extends HttpServlet {

    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);

        CountryEnvironmentParameters.Key poolKey;
        try {
            poolKey = getBody(req);
        } catch (JsonSyntaxException e) {
            usage(req, resp);
            return;
        }

        final ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        final ExecutionThreadPoolService executionThreadPoolService = appContext.getBean(ExecutionThreadPoolService.class);
        executionThreadPoolService.stopExecutionThreadPool(poolKey);
    }

    private CountryEnvironmentParameters.Key getBody(HttpServletRequest req) throws IOException, JsonSyntaxException {
        BufferedReader reader = new BufferedReader(req.getReader());

        StringBuilder body = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            body.append(line);
        }

        return gson.fromJson(body.toString(), CountryEnvironmentParameters.Key.class);
    }

    private void usage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpStatus.BAD_REQUEST.value());
        resp.getWriter().print(
                String.format(
                        "{" +
                            "\"usage\":" +
                            "{" +
                                "\"action\": \"POST %s:\"," +
                                "\"body\":" +
                                "{" +
                                    "\"system\": \"<the execution pool's system>\"," +
                                    "\"application\": \"<the execution pool's application>\"," +
                                    "\"country\": \"<the execution pool's country>\"," +
                                    "\"environment\": \"<the execution pool's environment>\"" +
                                "}" +
                            "}" +
                        "}",
                        req.getPathInfo()
                )
        );
        resp.getWriter().flush();
    }

}
