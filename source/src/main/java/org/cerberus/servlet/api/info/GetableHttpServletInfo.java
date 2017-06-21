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
 * A specific {@link SinglePointHttpServletInfo} for a {@link org.cerberus.servlet.api.GetableHttpServlet}
 *
 * @author Aurelien Bourdon
 */
public class GetableHttpServletInfo extends SinglePointHttpServletInfo {

    public static class GetableUsage extends Usage {
        private final Set<RequestParameter> queryParameters;

        public GetableUsage(final Set<RequestParameter> queryParameters) {
            super(HttpMethod.GET);
            this.queryParameters = queryParameters;
        }

        public Set<RequestParameter> getQueryParameters() {
            return queryParameters;
        }
    }

    public GetableHttpServletInfo(final String name, final String version, final String description, final GetableUsage usage) {
        super(name, version, description, usage);
    }

}
