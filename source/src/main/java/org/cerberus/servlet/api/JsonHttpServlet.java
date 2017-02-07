package org.cerberus.servlet.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.util.json.ObjectMapperUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Base type for any Json type and UTF-8 encoded {@link HttpServlet}
 *
 * @author abourdon
 */
public class JsonHttpServlet extends HttpServlet {

    /**
     * The associated Content Type to a Json {@link HttpServlet}
     */
    public static final String CONTENT_TYPE = "application/json";

    /**
     * The default encoding of a {@link JsonHttpServlet}
     */
    public static final String CHARACTER_ENCODING = "UTF-8";

    /**
     * The associated {@link ObjectMapper} for Json/Object mapping.
     * <p>
     * By default, the associated {@link ObjectMapper} is equals to {@link ObjectMapperUtil#newDefaultInstance()}
     */
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        objectMapper = ObjectMapperUtil.newDefaultInstance();
    }

    /**
     * Get the associated {@link ObjectMapper} to this {@link JsonHttpServlet}
     *
     * @return the associated {@link ObjectMapper} to this {@link JsonHttpServlet}
     */
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Set the associated {@link ObjectMapper} to this {@link JsonHttpServlet}
     *
     * @param objectMapper the new {@link ObjectMapper} to set to this {@link JsonHttpServlet}
     */
    protected void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);
        super.service(req, resp);
    }

}
