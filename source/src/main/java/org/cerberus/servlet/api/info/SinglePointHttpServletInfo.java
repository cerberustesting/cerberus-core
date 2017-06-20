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

public class SinglePointHttpServletInfo {

    public static class Usage {
        private final HttpMethod httpMethod;

        public Usage(final HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
        }

        public HttpMethod getHttpMethod() {
            return httpMethod;
        }
    }

    private final String name;
    private final String version;
    private final String description;
    private final Usage usage;

    public SinglePointHttpServletInfo(final String name, final String version, final String description, final Usage usage) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.usage = usage;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public Usage getUsage() {
        return usage;
    }

}
