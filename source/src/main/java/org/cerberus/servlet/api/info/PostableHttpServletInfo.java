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
package org.cerberus.servlet.api.info;

import org.springframework.http.HttpMethod;

import java.util.Set;

/**
 * A specific {@link SinglePointHttpServletInfo} for {@link org.cerberus.servlet.api.PostableHttpServlet}
 *
 * @author Aurelien Bourdon
 */
public class PostableHttpServletInfo extends SinglePointHttpServletInfo {

    public static class PostableUsage extends Usage {
        private final Set<RequestParameter> queryParameters;
        private final Set<RequestParameter> bodyParameters;

        public PostableUsage(final Set<RequestParameter> queryParameters, final Set<RequestParameter> bodyParameters) {
            super(HttpMethod.POST);
            this.queryParameters = queryParameters;
            this.bodyParameters = bodyParameters;
        }

        public Set<RequestParameter> getQueryParameters() {
            return queryParameters;
        }

        public Set<RequestParameter> getBodyParameters() {
            return bodyParameters;
        }
    }

    public PostableHttpServletInfo(final String name, final String version, final String description, final PostableUsage usage) {
        super(name, version, description, usage);
    }
}
