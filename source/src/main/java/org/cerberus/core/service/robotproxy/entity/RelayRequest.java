/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.service.robotproxy.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Request to be performed by the relay of a runner.
 *
 * @author bcivel
 */
public class RelayRequest {

    private String method;
    private String url;
    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private byte[] body;
    private boolean followRedirects = true;
    private int timeoutMs = 60000;
    private boolean acceptUnsignedSsl = false;

    public String getMethod() {
        return method;
    }

    public RelayRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public RelayRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Adds a header value (a name can have several values).
     *
     * @param name
     * @param value
     * @return this
     */
    public RelayRequest addHeader(String name, String value) {
        if (name != null) {
            headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value == null ? "" : value);
        }
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public RelayRequest setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public RelayRequest setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public RelayRequest setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    public boolean isAcceptUnsignedSsl() {
        return acceptUnsignedSsl;
    }

    public RelayRequest setAcceptUnsignedSsl(boolean acceptUnsignedSsl) {
        this.acceptUnsignedSsl = acceptUnsignedSsl;
        return this;
    }

}
