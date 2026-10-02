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
package org.cerberus.core.mcpdelta.host;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStartDTOV001;
import org.cerberus.core.api.entity.ManualUrlParameters;
import org.cerberus.core.api.entity.QueuedExecution;
import org.cerberus.core.api.entity.QueuedExecutionResult;
import org.cerberus.core.api.entity.QueuedExecutionTestcase;
import org.cerberus.core.api.services.DebugExecutionService;
import org.cerberus.core.api.services.QueuedExecutionService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagSystemService;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.mcpdelta.cerberus.CerberusGateway;
import org.cerberus.core.mcpdelta.tools.Tool;
import org.cerberus.core.mcpdelta.util.CallContext;
import org.cerberus.core.service.xray.IXRayService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP Delta inside the Cerberus webapp: runs, the debug mode, cache purge and the queue job go through Cerberus'
 * own services, in-process and on behalf of the authenticated caller — the queue entries and the tag carry its
 * login, as when the same user queues from the web interface or the public API.
 */
public final class InProcessGateway implements CerberusGateway {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final QueuedExecutionService queue;
    private final DebugExecutionService debug;
    private final IParameterService parameters;
    private final IXRayService xray;
    private final ITagSystemService tagSystem;
    private final IExecutionThreadPoolService pool;

    public InProcessGateway(QueuedExecutionService queue, DebugExecutionService debug, IParameterService parameters, IXRayService xray,
                            ITagSystemService tagSystem, IExecutionThreadPoolService pool) {
        this.queue = queue;
        this.debug = debug;
        this.parameters = parameters;
        this.xray = xray;
        this.tagSystem = tagSystem;
        this.pool = pool;
    }

    private static String login() {
        String login = CallContext.user();
        if (login == null) {
            throw new Tool.ToolError("no authenticated Cerberus user for this call");
        }
        return login;
    }

    @Override
    public Queued enqueue(Order order) {
        String login = login();
        Principal principal = () -> login;
        Map<String, String> o = order.options();
        List<QueuedExecutionTestcase> testcases = new ArrayList<>();
        for (String[] t : order.testcases()) {
            testcases.add(QueuedExecutionTestcase.builder().testFolderId(t[0]).testcaseId(t[1]).build());
        }
        // A browser, device or desktop test names its robot; an API or batch test has none, which this service
        // still wants as a non-empty list (the robot is not used for those application types).
        List<String> robots = order.robots().stream().filter(r -> r != null && !r.isBlank()).toList();
        boolean campaign = order.campaign() != null;
        QueuedExecution qe = QueuedExecution.builder()
                .testcases(testcases)
                // A campaign brings its own countries, environments and robots unless the call names some.
                .countries(order.countries().isEmpty() ? null : order.countries())
                .environments(order.environments().isEmpty() ? null : order.environments())
                .robots(robots.isEmpty() ? (campaign ? null : List.of("")) : robots)
                .tag(order.tag())
                .screenshot(integer(o, "screenshot"))
                .video(integer(o, "video"))
                .verbose(integer(o, "verbose"))
                .pageSource(integer(o, "pageSource"))
                .robotLog(integer(o, "robotLog"))
                .consoleLog(integer(o, "consoleLog"))
                .timeout(o.get("timeout"))
                .retries(integer(o, "retries"))
                .priority(integer(o, "priority"))
                .manualExecution(o.get("manualExecution"))
                .manualUrl(integer(o, "manualUrl"))
                .manualUrlParameters(ManualUrlParameters.builder()
                        .host(o.getOrDefault("manualHost", ""))
                        .contextRoot(o.getOrDefault("manualContextRoot", ""))
                        .loginRelativeUrl(o.getOrDefault("manualLoginRelativeUrl", ""))
                        .envData(o.getOrDefault("manualEnvData", ""))
                        .build())
                .build();
        if (o.containsKey("executor")) {
            throw new Tool.ToolError("the executor option is not available here: Cerberus picks the robot executor itself. Nothing was queued.");
        }
        QueuedExecutionResult r;
        try {
            r = order.campaign() != null ? queue.addCampaignToExecutionQueue(order.campaign(), qe, principal)
                    : queue.addTestcasesToExecutionQueue(qe, principal);
        } catch (RuntimeException e) {
            throw new Tool.ToolError("Cerberus refused the run: " + e.getMessage());
        }
        if (r == null || r.getNbExecutions() == 0) {
            String why = r == null ? "no answer" : String.join(" ", r.getMessages() == null ? List.of() : r.getMessages());
            throw new Tool.ToolError("Cerberus queued nothing: " + (why.isBlank() ? "no testcase matched the countries, environments and robots" : why));
        }
        return new Queued(r.getNbExecutions(), r.getTag());
    }

    private static Integer integer(Map<String, String> options, String key) {
        String v = options.get(key);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            throw new Tool.ToolError("run option " + key + " must be a number, got '" + v + "'. Nothing was queued.");
        }
    }

    @Override
    public JsonNode debug(String method, String path, String body) {
        String login = login();
        try {
            if (path.isEmpty()) {
                DebugExecutionStartDTOV001 start = JSON.readValue(body == null ? "{}" : body, DebugExecutionStartDTOV001.class);
                return JSON.valueToTree(debug.startDebugExecution(start, login));
            }
            int slash = path.indexOf('/');
            String uuid = slash < 0 ? path : path.substring(0, slash);
            String action = slash < 0 ? "status" : path.substring(slash + 1);
            return switch (action) {
                case "next" -> JSON.valueToTree(debug.sendNext(uuid));
                case "retry" -> JSON.valueToTree(debug.sendRetry(uuid));
                case "stop" -> JSON.valueToTree(debug.stopDebugSession(uuid));
                case "status" -> JSON.valueToTree(debug.getStatus(uuid));
                default -> throw new Tool.ToolError("unknown debug action " + action);
            };
        } catch (Tool.ToolError e) {
            throw e;
        } catch (Exception e) {
            throw new Tool.ToolError("Cerberus debug mode: " + e.getMessage());
        }
    }

    @Override
    public String purgeCache() {
        // What ManageV001?action=purgeCache does.
        xray.purgeAllCacheEntries();
        parameters.purgeCacheEntry(null);
        tagSystem.purgeTagSystemCache();
        return "Cerberus caches purged";
    }

    @Override
    public String runQueueJob() {
        try {
            pool.executeNextInQueueAsynchroneously(false);
            return "Cerberus queue job triggered";
        } catch (Exception e) {
            return "could not trigger the Cerberus queue job: " + e.getMessage();
        }
    }
}
