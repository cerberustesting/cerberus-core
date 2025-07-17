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
package org.cerberus.core.apiprivate;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.service.ai.impl.AISessionManager;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author bcivel
 */
@RestController
@RequestMapping("/ai")
public class AIPrivateController {

    private static final Logger LOG = LogManager.getLogger(AIPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    AISessionManager aISessionManager;

    @Operation(hidden=true)
    @GetMapping("/prompts")
    public String getAllUserPrompts(HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {
            return jsonResponse.put("prompts", aISessionManager.getAllPromptByUser(request.getRemoteUser())).toString();
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

    @Operation(hidden=true)
    @GetMapping("/messagesFromPrompt/{sessionID}")
    public String getAllRequests(@PathVariable("sessionID") String sessionID, HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {
            return jsonResponse.put("messages", aISessionManager.getAllMessagesFromPrompt(sessionID)).toString();
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

}
