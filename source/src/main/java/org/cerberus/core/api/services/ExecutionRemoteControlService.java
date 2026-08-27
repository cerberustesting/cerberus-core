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
package org.cerberus.core.api.services;

import lombok.AllArgsConstructor;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionAckDTOV001;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.util.StringUtil;
import org.springframework.stereotype.Service;

/**
 * Lets an external process attached to a running execution
 * push back live view/remote-control URLs, mirroring in-memory what the Selenium 4 node
 * stereotype lookup ({@code crb:remoteUrl} / {@code crb:remoteControlUrl}) already does for web
 * executions.
 *
 * @author bcivel
 */
@Service
@AllArgsConstructor
public class ExecutionRemoteControlService {

    private final ExecutionUUID executionUUID;

    public DebugExecutionAckDTOV001 setRemoteViewUrls(String uuid, String remoteLiveUrl, String remoteControlUrl) {
        if (StringUtil.isEmptyOrNull(remoteLiveUrl) && StringUtil.isEmptyOrNull(remoteControlUrl)) {
            throw new IllegalArgumentException("At least one of 'remoteLiveUrl' or 'remoteControlUrl' is mandatory.");
        }
        validateUrl("remoteLiveUrl", remoteLiveUrl);
        validateUrl("remoteControlUrl", remoteControlUrl);

        TestCaseExecution execution = executionUUID.getTestCaseExecution(uuid);
        if (execution == null) {
            throw new EntityNotFoundException(TestCaseExecution.class, "executionUUID", uuid);
        }

        if (!StringUtil.isEmptyOrNull(remoteLiveUrl)) {
            execution.setRemoteLiveUrl(remoteLiveUrl);
        }
        if (!StringUtil.isEmptyOrNull(remoteControlUrl)) {
            execution.setRemoteControlLiveUrl(remoteControlUrl);
        }

        return DebugExecutionAckDTOV001.builder().executionUUID(uuid).accepted(true).build();
    }

    private void validateUrl(String paramName, String url) {
        if (!StringUtil.isEmptyOrNull(url) && !url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("Parameter '" + paramName + "' must be a http(s) URL.");
        }
    }
}