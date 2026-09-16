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
package org.cerberus.core.mcp.impl.appservice;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.appservice.AppServiceDTOV001;
import org.cerberus.core.api.dto.appservice.AppServiceMapperV001;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPProjectionUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.mcp.util.MCPUserContextService;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the services Cerberus can call, under the tool name
 * {@code cerberus_appservice_list}.
 *
 * <p>This used to return every service of every system, in full, with no way to narrow it. A system
 * filter passed by a caller was not rejected — it was not declared at all, so it went nowhere and
 * the answer looked like a successful, complete list of the wrong thing. On a real instance that is
 * a fifty-thousand-character dump, too large to read and silently mixing systems the caller never
 * asked about.</p>
 *
 * <p>It now filters by system — the caller's active context by default, like the other read tools —
 * and by application, returns a short projection instead of whole service definitions, and says
 * when the result was cut. The request body of a service, which is most of its weight, is only
 * returned when asked for by name or through {@code cerberus_appservice_get}.</p>
 */
@Component
public class ListAppServicesTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_list";

    /** Every field the projection can return. */
    private static final List<String> ALL_FIELDS = List.of(
            "service", "application", "type", "method", "servicePath", "group", "description",
            "operation", "fileName", "kafkaTopic", "kafkaKey", "serviceRequest", "usrModif", "dateModif");

    /**
     * What a listing returns unless asked otherwise: enough to recognise a service and to decide
     * which one to open. {@code serviceRequest} is deliberately out — one request body can be
     * larger than this whole list.
     */
    private static final List<String> DEFAULT_FIELDS = List.of(
            "service", "application", "type", "method", "servicePath", "group", "description");

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 500;

    private final IAppServiceService appServiceService;
    private final AppServiceMapperV001 mapper;
    private final MCPUserContextService userContext;
    private final MCPLogUtils mcpLogUtils;

    public ListAppServicesTool(IAppServiceService appServiceService, AppServiceMapperV001 mapper,
                               MCPUserContextService userContext, MCPLogUtils mcpLogUtils) {
        this.appServiceService = appServiceService;
        this.mapper = mapper;
        this.userContext = userContext;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> execute(MCPToolUtils.argumentsOrEmpty(request.arguments()), exchange)
        );
    }

    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("search", Map.of(
                "type", "string",
                "description", "Filter on the service name, matched anywhere in it. Leave it out to list them all."
        ));
        properties.put("system", Map.of(
                "type", "string",
                "description", "Restrict to the services of one system. Defaults to your active system context "
                        + "(cerberus_context_system_list). Services attached to no application are always "
                        + "included: they are shared across systems."
        ));
        properties.put("application", Map.of(
                "type", "string",
                "description", "Restrict to the services of one application."
        ));
        properties.put("fields", Map.of(
                "type", "array",
                "items", Map.of("type", "string", "enum", ALL_FIELDS),
                "description", "Fields to return for each service. Defaults to " + DEFAULT_FIELDS
                        + ". Ask for serviceRequest only on a narrowed search — one request body can be "
                        + "longer than an entire list."
        ));
        properties.put("limit", Map.of(
                "type", "integer",
                "description", "Maximum number of services to return. Defaults to " + DEFAULT_LIMIT
                        + ", maximum " + MAX_LIMIT + ". The answer says when it was cut."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the services Cerberus can call, narrowed to your active system context by default.

                Use it to find a service by name, or to see what an application already calls before writing
                a new one.

                Narrow before you read: without a search or an application, a real instance holds hundreds of
                services. The answer returns a short projection of each one, not its full definition — use
                cerberus_appservice_get for the request body, the headers and the contents of a single
                service.

                The answer echoes the filters actually applied, so you can tell a filtered result from an
                unfiltered one.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of(),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List app services", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args, McpSyncServerExchange exchange) {
        String search = MCPToolUtils.getString(args, "search", "").trim();
        String system = MCPToolUtils.getString(args, "system", "").trim();
        String application = MCPToolUtils.getString(args, "application", "").trim();
        List<String> fields = MCPToolUtils.getStringList(args, "fields", DEFAULT_FIELDS);
        int limit = Math.min(Math.max(MCPToolUtils.getInteger(args, "limit", DEFAULT_LIMIT), 1), MAX_LIMIT);

        String login = userContext.getLogin(exchange);
        mcpLogUtils.call(TOOL_NAME, "appservice_list",
                String.format("MCP tool %s called with search=%s system=%s application=%s",
                        TOOL_NAME, search, system, application), login);

        for (String field : fields) {
            if (!ALL_FIELDS.contains(field)) {
                return MCPToolUtils.errorText("Unknown field '" + field + "'. Supported fields: " + ALL_FIELDS);
            }
        }

        // readByCriteria mutates the list it is given (it appends the empty system to let through the
        // services attached to no application), so it always gets a fresh mutable one.
        List<String> systems = new ArrayList<>();
        if (!system.isBlank()) {
            systems.add(system);
        } else {
            if (login == null) {
                return MCPToolUtils.errorText("Unable to resolve the authenticated MCP user for this call.");
            }
            try {
                systems.addAll(userContext.getContextSystems(userContext.getUser(login)));
            } catch (CerberusException e) {
                return MCPToolUtils.errorText("Unable to read system context for '" + login + "': "
                        + e.getMessageError().getDescription());
            }
            if (systems.isEmpty()) {
                return MCPToolUtils.errorText("No active system in your MCP context, so there is nothing to "
                        + "list. Call cerberus_context_system_list, then cerberus_context_system_update "
                        + "(action=add) — or name a system directly with the system parameter.");
            }
        }
        List<String> appliedSystems = List.copyOf(systems);

        Map<String, List<String>> individualSearch = new LinkedHashMap<>();
        if (!application.isBlank()) {
            individualSearch.put("srv.application", List.of(application));
        }

        // One more than the limit, so "there are more" is a fact rather than a guess drawn from a
        // full page.
        AnswerList<AppService> answerList = appServiceService.readByCriteria(
                0, limit + 1, "srv.service", "asc", search, individualSearch, systems);

        List<AppService> found = answerList.getDataList() == null ? List.of() : answerList.getDataList();
        boolean truncated = found.size() > limit;

        List<Map<String, Object>> services = new ArrayList<>();
        for (AppService service : found.subList(0, Math.min(found.size(), limit))) {
            AppServiceDTOV001 dto = mapper.toDTO(service);
            services.add(MCPProjectionUtils.project(dto, fields));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", services.size());
        // Echoed so a caller can tell at a glance whether the filter it meant to apply was applied.
        response.put("appliedFilters", filters(search, appliedSystems, application, system.isBlank()));
        response.put("fields", fields);
        if (truncated) {
            response.put("truncated", true);
            response.put("message", "More services matched than the requested limit of " + limit
                    + ". Narrow with search, application or system, or raise limit.");
        }
        response.put("services", services);

        if (services.isEmpty()) {
            response.put("message", "No service matches these filters. Widen the search, or check the system: "
                    + "a service belongs to a system through its application, and one attached to no "
                    + "application is shared across all of them.");
        }

        return MCPToolUtils.successJson(response);
    }

    /**
     * Describes the filters that were really applied.
     */
    private Map<String, Object> filters(String search, List<String> systems, String application,
                                        boolean systemFromContext) {
        Map<String, Object> applied = new LinkedHashMap<>();
        applied.put("search", search);
        applied.put("systems", systems);
        applied.put("systemsFrom", systemFromContext ? "your active context" : "the system parameter");
        applied.put("application", application);
        return applied;
    }
}
