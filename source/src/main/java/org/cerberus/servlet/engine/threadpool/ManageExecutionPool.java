package org.cerberus.servlet.engine.threadpool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.util.json.ObjectMapperUtil;
import org.cerberus.util.validity.Validity;
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
 * @author abourdon
 */
@WebServlet(name = "ManageExecutionPool", urlPatterns = {"/ManageExecutionPool"})
public class ManageExecutionPool extends HttpServlet {

    private static class RequestBody implements Validity {

        private Action action;

        private CountryEnvironmentParameters.Key executionPoolKey;

        public Action getAction() {
            return action;
        }

        public CountryEnvironmentParameters.Key getExecutionPoolKey() {
            return executionPoolKey;
        }

        @Override
        public boolean isValid() {
            return action != null && executionPoolKey != null && executionPoolKey.isValid();
        }
    }

    private enum Action {
        PAUSE,
        RESUME,
        STOP
    }

    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    private ObjectMapper objectMapper;;

    private IExecutionThreadPoolService executionThreadPoolService;

    @Override
    public void init() throws ServletException {
        objectMapper = ObjectMapperUtil.newInstance();

        final ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);

        final RequestBody requestBody;
        try {
            requestBody = getBody(req);
            if (requestBody == null || !requestBody.isValid()) {
                throw new JsonSyntaxException("Invalid request body");
            }
        } catch (JsonSyntaxException e) {
            usage(req, resp);
            return;
        }

        switch (requestBody.getAction()) {
            case PAUSE:
                pauseExecutionPool(requestBody.getExecutionPoolKey());
                break;
            case RESUME:
                resumeExecutionPool(requestBody.getExecutionPoolKey());
                break;
            case STOP:
                stopExecutionPool(requestBody.getExecutionPoolKey());
                break;
        }
    }

    private void pauseExecutionPool(final CountryEnvironmentParameters.Key executionPoolKey) {
        executionThreadPoolService.pauseExecutionThreadPool(executionPoolKey);
    }

    private void resumeExecutionPool(final CountryEnvironmentParameters.Key executionPoolKey) {
        executionThreadPoolService.resumeExecutionThreadPool(executionPoolKey);
    }

    private void stopExecutionPool(final CountryEnvironmentParameters.Key executionPoolKey) {
        executionThreadPoolService.removeExecutionThreadPool(executionPoolKey);
    }

    private RequestBody getBody(HttpServletRequest req) throws IOException, JsonSyntaxException {
        StringBuilder body = new StringBuilder();

        BufferedReader reader = new BufferedReader(req.getReader());
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            body.append(line);
        }

        return objectMapper.readValue(body.toString(), RequestBody.class);
    }

    private void usage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpStatus.BAD_REQUEST.value());
        resp.getWriter().print(
                String.format(
                        "{" +
                            "\"usage\": {" +
                                "\"description\": \"Pause, Resume or Stop a given Execution Thread Pool\"," +
                                "\"target\": \"POST %s\"," +
                                "\"body\": {" +
                                    "\"action\": \"[PAUSE, RESUME, STOP]\"," +
                                    "\"executionPoolKey\": {" +
                                        "\"system\": \"<the execution pool's system>\"," +
                                        "\"application\": \"<the execution pool's application>\"," +
                                        "\"country\": \"<the execution pool's country>\"," +
                                        "\"environment\": \"<the execution pool's environment>\"" +
                                    "}" +
                                "}" +
                            "}" +
                        "}",
                        req.getRequestURL().toString()
                )
        );
        resp.getWriter().flush();
    }

}
