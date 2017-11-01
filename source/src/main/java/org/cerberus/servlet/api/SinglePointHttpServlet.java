/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.servlet.api;

import org.apache.log4j.Logger;
import org.cerberus.servlet.api.info.SinglePointHttpServletInfo;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.cerberus.servlet.api.mapper.HttpMapper;
import org.cerberus.util.StringUtil;
import org.cerberus.util.validity.Validable;
import org.cerberus.util.validity.Validity;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * Base class for any single point {@link ApplicationHttpServlet}.
 * <p>
 * A single point {@link ApplicationHttpServlet} is a specific {@link ApplicationHttpServlet} that can be only reached through a given {@link HttpMethod}
 *
 * @param <REQUEST>  the request type, according to the associated {@link HttpMethod} (request parameters for {@link HttpMethod#GET}, request body for {@link HttpMethod#POST}, ...)
 * @param <RESPONSE> the response type
 * @author Aurelien Bourdon
 */
public abstract class SinglePointHttpServlet<REQUEST extends Validable, RESPONSE> extends ApplicationHttpServlet {

    /**
     * Raised when an error occured during pre-request parsing
     *
     * @see #preRequestParsing(HttpServletRequest)
     */
    public static class PreRequestParsingException extends IOException {

        public PreRequestParsingException(final String message) {
            super(message);
        }

        public PreRequestParsingException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Raised when an error occurred during the request parsing
     *
     * @see #parseRequest(HttpServletRequest)
     */
    public static class RequestParsingException extends IOException {

        public RequestParsingException(final String message) {
            super(message);
        }

        public RequestParsingException(final String message, final Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Raised when an error occurred during request processing
     *
     * @see #processRequest(Validable)
     */
    public static class RequestProcessException extends IOException {

        private HttpStatus statusToReturn;

        public RequestProcessException(final HttpStatus statusToReturn) {
            this.statusToReturn = statusToReturn;
        }

        public RequestProcessException(final HttpStatus statusToReturn, final String message) {
            super(message);
            this.statusToReturn = statusToReturn;
        }

        public RequestProcessException(final HttpStatus statusToReturn, final String message, final Throwable cause) {
            super(message, cause);
            this.statusToReturn = statusToReturn;
        }

        public HttpStatus getStatusToReturn() {
            return statusToReturn;
        }

    }

    /**
     * Default version for any internal servlet
     */
    public static final String INTERNAL_VERSION = "internal";

    /**
     * The helper message query parameter for any {@link SinglePointHttpServlet}
     */
    public static final String HELP_PARAMETER = "help";

    protected static final HttpMapper JSON_HTTP_MAPPER = new DefaultJsonHttpMapper();

    private static final Logger LOG = Logger.getLogger(SinglePointHttpServlet.class);

    @Override
    public final void init() throws ServletException {
        super.init();
        postInit();
    }

    /**
     * Get the associated {@link SinglePointHttpServletInfo} to this {@link SinglePointHttpServlet}
     *
     * @return the associated {@link SinglePointHttpServletInfo} to this {@link SinglePointHttpServlet}
     */
    protected abstract SinglePointHttpServletInfo getInfo();

    /**
     * Get the version of this {@link SinglePointHttpServlet}
     *
     * @return the version of this {@link SinglePointHttpServlet}
     */
    protected String getVersion() {
        return INTERNAL_VERSION;
    }

    /**
     * Get the associated {@link HttpMapper} to this {@link SinglePointHttpServlet}
     *
     * @return the associated {@link HttpMapper} to this {@link SinglePointHttpServlet}
     */
    protected abstract HttpMapper getHttpMapper();

    /**
     * Get the associated {@link HttpMethod} to this {@link SinglePointHttpServlet}
     *
     * @return the associated {@link HttpMethod} to this {@link SinglePointHttpServlet}
     */
    protected abstract HttpMethod getHttpMethod();

    /**
     * Effectively parse the given {@link HttpServletRequest}
     *
     * @param req the {@link HttpServletRequest} to parse
     * @return the REQUEST type underlying the given {@link HttpServletRequest}
     * @throws RequestParsingException if an error occurred during request parsing
     */
    protected abstract REQUEST parseRequest(final HttpServletRequest req) throws RequestParsingException;

    /**
     * Effectively process the given REQUEST
     *
     * @param request the REQUEST to parse
     * @return the RESPONSE if request has been successfully processed
     * @throws RequestProcessException if an error occurred during request processing
     */
    protected abstract RESPONSE processRequest(final REQUEST request) throws RequestProcessException;

    /**
     * Convenience method to apply initialization from specific class
     *
     * @throws ServletException
     */
    protected void postInit() throws ServletException {
        // Do nothing by default
    }

    protected boolean helpRequired(final HttpServletRequest req) throws IOException {
        return req.getParameter(HELP_PARAMETER) != null;
    }

    /**
     * Convenience method to apply actions before the request parsing, i.e., before calling the {@link #parseRequest(HttpServletRequest)} method
     *
     * @param req the associated {@link HttpServletRequest} which is going to be parsed
     * @throws PreRequestParsingException if an error occurred during the pre-request parsing
     */
    protected void preRequestParsing(final HttpServletRequest req) throws PreRequestParsingException {
        // Do nothing by default
    }

    /**
     * Handle the given {@link HttpServletRequest} according to the associated {@link HttpMethod} to this {@link SinglePointHttpServlet}
     * <p>
     * Any {@link SinglePointHttpServlet}'s implementation should call this method during its specific {@link javax.servlet.http.HttpServlet}'s action ({@link javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)}, {@link javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)}, ...)
     *
     * @param req  the associated {@link HttpServletRequest} to this request
     * @param resp the associated {@link HttpServletResponse} to this request
     * @throws IOException if an I/O error occurred
     */
    protected void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            // First of all, check if help message display is required
            if (helpRequired(req)) {
                writeHelperMessage(resp);
                return;
            }

            // Then, process request by:

            // 1. Applying pre-request parsing
            preRequestParsing(req);

            // 2. Applying request parsing
            final REQUEST request = parseRequest(req);
            final Validity validity = request.validate();
            if (!validity.isValid()) {
                throw new RequestParsingException("Parameter(s) invalid. " + validity.getReasons());
            }

            // 3. Applying request processing and send response to client
            writeResponse(processRequest(request), resp);
        } catch (PreRequestParsingException e) {
            handlePreRequestParsingError(e, req, resp);
        } catch (RequestParsingException e) {
            handleRequestParsingError(e, req, resp);
        } catch (final RequestProcessException e) {
            handleRequestProcessError(e, req, resp);
        } catch (final Exception e) {
            LOG.warn("Handle unexpected exception", e);
            handleRequestProcessError(new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e), req, resp);
        }

    }

    protected void handlePreRequestParsingError(final PreRequestParsingException e, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        writeError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resp);
    }

    protected void handleRequestParsingError(final RequestParsingException e, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        writeError(
                HttpStatus.BAD_REQUEST.value(),
                new HashMap<String, Object>() {
                    {
                        put("reason", e.getMessage());
                        put("serviceInfo", getInfo());
                    }
                },
                resp
        );
    }

    protected void handleRequestProcessError(final RequestProcessException e, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        writeError(e.getStatusToReturn().value(), e.getMessage(), resp);
    }

    protected void writeHelperMessage(final HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON_HTTP_MAPPER.getResponseContentType());
        resp.setCharacterEncoding(JSON_HTTP_MAPPER.getResponseCharacterEncoding());
        resp.getWriter().print(JSON_HTTP_MAPPER.serialize(getInfo()).toString());
        resp.getWriter().flush();
    }

    protected void writeError(final int errorStatus, final Object errorMessage, final HttpServletResponse resp) throws IOException {
        resp.setStatus(errorStatus);
        resp.setContentType(JSON_HTTP_MAPPER.getResponseContentType());
        resp.setCharacterEncoding(JSON_HTTP_MAPPER.getResponseCharacterEncoding());
        if (errorMessage != null) {
            resp.getWriter().print(JSON_HTTP_MAPPER.serialize(errorMessage).toString());
        }
        resp.getWriter().flush();
    }

    protected void writeResponse(final Object response, final HttpServletResponse resp) throws IOException {
        resp.setContentType(getHttpMapper().getResponseContentType());
        resp.setCharacterEncoding(getHttpMapper().getResponseCharacterEncoding());
        resp.getWriter().print(getHttpMapper().serialize(response).toString());
        resp.getWriter().flush();
    }

}
