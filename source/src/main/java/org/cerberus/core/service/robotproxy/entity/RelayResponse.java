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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Response of the target, as returned by the relay of a runner. The body is
 * already decompressed.
 *
 * @author bcivel
 */
public class RelayResponse {

    private int status;
    private String statusText;
    private final List<String[]> headers = new ArrayList<>();
    private byte[] body = new byte[0];
    private boolean truncated;
    private long durationMs;
    private String finalUrl;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    /**
     * @return list of {name, value} pairs, in the order received.
     */
    public List<String[]> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body == null ? new byte[0] : body;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public void setFinalUrl(String finalUrl) {
        this.finalUrl = finalUrl;
    }

    /**
     * @param name header name (case insensitive)
     * @return the first value or null.
     */
    public String getHeader(String name) {
        for (String[] h : headers) {
            if (h[0] != null && h[0].equalsIgnoreCase(name)) {
                return h[1];
            }
        }
        return null;
    }

    /**
     * @return the body decoded with the charset of the Content-Type (UTF-8 by
     * default).
     */
    public String getBodyAsString() {
        Charset cs = StandardCharsets.UTF_8;
        String ct = getHeader("Content-Type");
        if (ct != null) {
            for (String part : ct.split(";")) {
                String p = part.trim();
                if (p.toLowerCase().startsWith("charset=")) {
                    try {
                        cs = Charset.forName(p.substring(8).trim().replace("\"", ""));
                    } catch (Exception ex) {
                        cs = StandardCharsets.UTF_8;
                    }
                }
            }
        }
        return new String(body, cs);
    }

}
