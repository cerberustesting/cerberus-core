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
package org.cerberus.core.api.dto.robot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * Test of the connection to a Cerberus Robot Proxy, with the authentication as
 * currently defined (not necessarily saved yet).
 *
 * @author bcivel
 */
@ToString(exclude = {"authToken", "oauthClientSecret"})
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"host", "port", "authMode", "authToken", "oauthTokenUrl", "oauthClientId", "oauthClientSecret", "robot", "executor"})
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "RobotProxyCheck")
public class RobotProxyCheckDTOV001 {

    @JsonView({View.Public.POST.class})
    @Schema(description = "Host of the Cerberus Robot Proxy", example = "proxy.mycompany.com", required = true)
    private String host;

    @JsonView({View.Public.POST.class})
    @Schema(description = "Port of the Cerberus Robot Proxy", example = "8093")
    private Integer port;

    @JsonView({View.Public.POST.class})
    @Schema(description = "Authentication towards the proxy", example = "NONE", allowableValues = {"NONE", "TOKEN", "OAUTH"})
    private String authMode;

    @JsonView({View.Public.POST.class})
    @Schema(description = "Bearer token (mode TOKEN)")
    private String authToken;

    @JsonView({View.Public.POST.class})
    @Schema(description = "OAuth token endpoint (mode OAUTH)")
    private String oauthTokenUrl;

    @JsonView({View.Public.POST.class})
    @Schema(description = "OAuth client id (mode OAUTH)")
    private String oauthClientId;

    @JsonView({View.Public.POST.class})
    @Schema(description = "OAuth client secret (mode OAUTH)")
    private String oauthClientSecret;

    @JsonView({View.Public.POST.class})
    @Schema(description = "Robot name. With the executor, lets a secret sent masked be read from the saved executor, only if host and port are unchanged.")
    private String robot;

    @JsonView({View.Public.POST.class})
    @Schema(description = "Executor name (see robot)")
    private String executor;
}
