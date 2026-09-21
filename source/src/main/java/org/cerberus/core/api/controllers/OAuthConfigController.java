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
package org.cerberus.core.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cerberus.core.api.dto.oauth.OAuthConfigDTOV001;
import org.cerberus.core.config.cerberus.Property;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unauthenticated OAuth/Keycloak discovery endpoint, so that local third-party tools
 * (e.g. the local runner) can probe a Cerberus base URL and know upfront whether to
 * start an OAuth2 flow, and with which Keycloak realm/client, before making any
 * authenticated call.
 *
 * Deliberately not wrapped in {@link org.cerberus.core.api.controllers.wrappers.ResponseWrapper}
 * and not gated behind {@code PublicApiAuthenticationService} : the response shape is a fixed
 * external contract ({@code {"enabled": false}} or {@code {"enabled": true, "keycloakUrl": ...}})
 * and the whole point is that it must be reachable with no credentials at all, always answering
 * 200 (never 404) so callers can distinguish "valid Cerberus instance, OAuth disabled" from
 * "wrong URL / not Cerberus".
 *
 * @author bcivel
 */
@Tag(name = "OAuth Config", description = "Public OAuth/Keycloak discovery endpoint for local third-party tools")
@RestController
@RequestMapping(path = "/public/oauth-config")
public class OAuthConfigController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get OAuth configuration",
        description = "Returns the OAuth2/Keycloak configuration of this Cerberus instance when OAuth is enabled, "
                + "or {\"enabled\": false} otherwise. No authentication required.",
        responses = {
            @ApiResponse(responseCode = "200", description = "OAuth configuration", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = OAuthConfigDTOV001.class))}),
        }
    )
    public OAuthConfigDTOV001 getOAuthConfig() {
        if (!Property.isKeycloak()) {
            return OAuthConfigDTOV001.builder().enabled(false).build();
        }

        return OAuthConfigDTOV001.builder()
                .enabled(true)
                .keycloakUrl(System.getProperty(Property.KEYCLOAKURL))
                .realm(System.getProperty(Property.KEYCLOAKREALM))
                .clientId(System.getProperty(Property.KEYCLOAKCLIENT))
                .localRunnerClientId(System.getProperty(Property.KEYCLOAKLOCALRUNNERCLIENT))
                .build();
    }
}