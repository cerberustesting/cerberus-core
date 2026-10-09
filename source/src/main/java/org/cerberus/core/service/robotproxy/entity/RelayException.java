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

/**
 * Failure of the relay itself (not of the target). The message never contains
 * the token.
 *
 * @author bcivel
 */
public class RelayException extends Exception {

    public static final String CODE_NOT_CONFIGURED = "not_configured";
    public static final String CODE_UNREACHABLE = "unreachable";
    public static final String CODE_UNSUPPORTED = "relay_unsupported";
    public static final String CODE_INVALID_RESPONSE = "invalid_response";
    public static final String CODE_INVALID_REQUEST = "invalid_request";
    public static final String CODE_UNAUTHORIZED = "unauthorized";
    public static final String CODE_TARGET_BLOCKED = "target_blocked";
    public static final String CODE_REQUEST_TOO_LARGE = "request_too_large";
    public static final String CODE_TOO_MANY_REQUESTS = "too_many_requests";
    public static final String CODE_CONNECT_FAILED = "connect_failed";
    public static final String CODE_RELAY_STOPPED = "relay_stopped";
    public static final String CODE_TIMEOUT = "timeout";

    private final String code;
    private final int httpStatus;

    public RelayException(String code, int httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public RelayException(String code, int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    /**
     * @return one of the CODE_ constants.
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the http status returned by the relay, or 0 when none.
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isTimeout() {
        return CODE_TIMEOUT.equals(code);
    }

}
