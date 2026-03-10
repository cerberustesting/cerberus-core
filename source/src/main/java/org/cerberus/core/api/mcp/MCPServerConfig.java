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
package org.cerberus.core.api.mcp;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ServletContextAware;

import java.util.List;

@Configuration
public class MCPServerConfig implements ServletContextAware {

    private final MCPToolRegistry registry;
    private final PublicApiAuthenticationService authenticationService;
    private ServletContext servletContext;

    public MCPServerConfig(MCPToolRegistry registry,
                           PublicApiAuthenticationService authenticationService) {
        this.registry = registry;
        this.authenticationService = authenticationService;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Bean
    public HttpServletStreamableServerTransportProvider mcpTransportProvider() {
        // Récupère le provider créé et enregistré dans WebAppInitializer
        return (HttpServletStreamableServerTransportProvider)
                servletContext.getAttribute("mcpTransportProvider");
    }

    @Bean
    public McpSyncServer mcpServer(HttpServletStreamableServerTransportProvider transportProvider) {
        List<McpServerFeatures.SyncToolSpecification> tools = registry.listTools();
        return McpServer.sync(transportProvider)
                .serverInfo("Cerberus MCP", "1.0.0")
                .capabilities(
                        McpSchema.ServerCapabilities.builder()
                                .tools(false)
                                .build()
                )
                .tools(tools)
                .build();
    }
}