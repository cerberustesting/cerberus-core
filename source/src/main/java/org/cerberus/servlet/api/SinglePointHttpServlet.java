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
import org.cerberus.crud.entity.Application;
import org.cerberus.util.StringUtil;
import org.cerberus.util.validity.Validity;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Base class for any single point {@link HttpServlet}.
 * <p>
 * A single point {@link HttpServlet} is a specific {@link HttpServlet} that can be only reached through a given {@link HttpMethod}
 *
 * @param <REQUEST>  the request type, according to the associated {@link HttpMethod} (request parameters for {@link HttpMethod#GET}, request body for {@link HttpMethod#POST}, ...)
 * @param <RESPONSE> the response type
 */
public abstract class SinglePointHttpServlet<REQUEST extends Validity, RESPONSE> extends HttpServlet {

    /**
     * Raised when an error occured during pre-request parsing
     *
     * @see #preRequestParsing(HttpServletRequest)
     */
    public static class PreRequestParsingException extends Exception {

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
    public static class RequestParsingException extends Exception {

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
     * @see #processRequest(Validity)
     */
    public static class RequestProcessException extends Exception {

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
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = Logger.getLogger(SinglePointHttpServlet.class);

    /**
     * The associated {@link ApplicationContext} to this servlet
     */
    private ApplicationContext applicationContext;

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        initCore();
        postInit();
    }

    /**
     * Get the associated {@link HttpMapper} to this {@link SinglePointHttpServlet}
     *
     * @return the associated {@link HttpMapper} to this {@link SinglePointHttpServlet}
     */
    public abstract HttpMapper getHttpMapper();

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
     * Get the usage description of this {@link SinglePointHttpServlet}
     *
     * @return the usage description of this {@link SinglePointHttpServlet}
     */
    protected abstract String getUsageDescription();

    /**
     * Convenience method to apply initialization from specific class
     *
     * @throws ServletException
     */
    protected void postInit() throws ServletException {
        // Do nothing by default
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
            // First, call pre-request parsing process
            preRequestParsing(req);

            // Second, parse and valid request
            final REQUEST request = parseRequest(req);
            if (!isRequestValid(request)) {
                throw new RequestParsingException("Invalid request " + request);
            }

            // Third, process request
            final RESPONSE response = processRequest(request);

            // Finally send response to client
            resp.setContentType(getHttpMapper().getResponseContentType());
            resp.setCharacterEncoding(getHttpMapper().getResponseCharacterEncoding());
            resp.getWriter().print(getHttpMapper().serialize(response));
            resp.getWriter().flush();
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
        resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        if (!StringUtil.isNullOrEmpty(getUsageDescription())) {
            resp.getWriter().print(getUsageDescription());
        }
        resp.getWriter().flush();
    }

    protected void handleRequestParsingError(final RequestParsingException e, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpStatus.BAD_REQUEST.value());
        if (!StringUtil.isNullOrEmpty(getUsageDescription())) {
            resp.getWriter().print(getUsageDescription());
        }
        resp.getWriter().flush();
    }

    protected void handleRequestProcessError(final RequestProcessException e, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        resp.setStatus(e.getStatusToReturn().value());
        if (!StringUtil.isNullOrEmpty(e.getMessage())) {
            resp.getWriter().print(e.getMessage());
        }
        resp.getWriter().flush();
    }

    private void initCore() {
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
    }

    private boolean isRequestValid(REQUEST request) {
        return request != null && request.isValid();
    }

}
