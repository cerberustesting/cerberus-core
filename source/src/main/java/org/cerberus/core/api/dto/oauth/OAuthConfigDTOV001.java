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
package org.cerberus.core.api.dto.oauth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * OAuth/Keycloak discovery payload served by {@code GET /api/public/oauth-config}.
 *
 * {@code keycloakUrl}, {@code realm} and {@code clientId} are omitted from the JSON
 * response when OAuth is disabled, so third-party tools (e.g. the local runner) can
 * tell a valid Cerberus instance with OAuth disabled from a wrong URL by the shape
 * of the {@code enabled: false} body alone.
 *
 * @author bcivel
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"enabled", "keycloakUrl", "realm", "clientId", "localRunnerClientId", "cerberusMcpClientId"})
@Schema(name = "OAuthConfig")
public class OAuthConfigDTOV001 {

    @Schema(description = "Whether OAuth2/Keycloak authentication is enabled on this instance", example = "true")
    private boolean enabled;

    @Schema(description = "Keycloak base URL", example = "https://sso.cerberus-testing.com")
    private String keycloakUrl;

    @Schema(description = "Keycloak realm", example = "cerberus")
    private String realm;

    @Schema(description = "Keycloak client id", example = "Cerberus")
    private String clientId;

    @Schema(description = "Keycloak client id used by local third-party tools", example = "cerberus-local-runner")
    private String localRunnerClientId;

    @Schema(description = "Keycloak client id used by the Cerberus MCP server", example = "cerberus-mcp")
    private String cerberusMcpClientId;
}