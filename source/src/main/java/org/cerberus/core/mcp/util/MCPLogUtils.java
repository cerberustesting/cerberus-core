/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.mcp.util;

import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.springframework.stereotype.Component;

@Component
public class MCPLogUtils {

    private final ILogEventService logEventService;

    public MCPLogUtils(ILogEventService logEventService) {
        this.logEventService = logEventService;
    }

    public void call(String toolName, String intent, String message) {
        log(toolName, intent, LogEvent.STATUS_INFO, message);
    }

    public void success(String toolName, String intent, String message) {
        log(toolName, intent, LogEvent.STATUS_INFO, message);
    }

    public void warning(String toolName, String intent, String message) {
        log(toolName, intent, LogEvent.STATUS_WARN, message);
    }

    public void error(String toolName, String intent, String message) {
        log(toolName, intent, LogEvent.STATUS_ERROR, message);
    }

    private void log(String toolName, String action, String status, String message) {
        logEventService.createForMcpCalls(
                toolName,
                action,
                status,
                message,
                "MCP"
        );
    }
}
