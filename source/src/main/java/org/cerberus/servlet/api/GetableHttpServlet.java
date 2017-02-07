package org.cerberus.servlet.api;

import org.cerberus.util.validity.Validity;
import org.springframework.http.HttpMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Base type for any 'only GETable' {@link SinglePointHttpServlet}
 *
 * @param <REQUEST>  the request parameters type
 * @param <RESPONSE> the response type
 * @author abourdon
 */
public abstract class GetableHttpServlet<REQUEST extends Validity, RESPONSE> extends SinglePointHttpServlet<REQUEST, RESPONSE> {

    @Override
    protected HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

}
