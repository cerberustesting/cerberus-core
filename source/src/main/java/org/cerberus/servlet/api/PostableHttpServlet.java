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

import org.cerberus.util.validity.Validity;
import org.springframework.http.HttpMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Base type for any 'only POSTable' {@link SinglePointHttpServlet}
 *
 * @param <REQUEST>  the request body type
 * @param <RESPONSE> the response type
 * @author abourdon
 */
public abstract class PostableHttpServlet<REQUEST extends Validity, RESPONSE> extends SinglePointHttpServlet<REQUEST, RESPONSE> {

    @Override
    protected HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    protected REQUEST parseRequest(final HttpServletRequest req) throws RequestParsingException {
        try {
            final StringBuilder body = new StringBuilder();
            final BufferedReader reader = new BufferedReader(req.getReader());
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                body.append(line);
            }
            return getHttpMapper().deserialize(body.toString(), getRequestType());
        } catch (Exception e) {
            throw new RequestParsingException("Unable to get body from request", e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    /**
     * Get the underlying {@link Class} to the associated REQUEST type
     *
     * @return the underlying {@link Class} to the associated REQUEST type
     */
    protected abstract Class<REQUEST> getRequestType();

}
