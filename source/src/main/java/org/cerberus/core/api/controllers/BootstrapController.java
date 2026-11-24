/*
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
package org.cerberus.core.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.cerberus.core.api.dto.bootstrap.SmokeBootstrapRequestDTO;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.api.services.SmokeBootstrapService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IUserRoleService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sets up the smoke campaigns (one call for the web one, one for the API one) on a freshly created instance. Administrator only.
 */
@AllArgsConstructor
@Tag(name = "Bootstrap", description = "Endpoints to set up the content of a new instance")
@RestController
@RequestMapping(path = "/public/bootstrap/")
public class BootstrapController {

    private static final String API_KEY = "X-API-KEY";
    private static final String ROLE_ADMINISTRATOR = "Administrator";
    private static final String SMOKE_DESCRIPTION = "Creates, or updates when they already exist, the shared content (system, applications, "
            + "environment, login data, smoke user) and the test folder, testcases, label, campaign and end of campaign email of this smoke. "
            + "The web and the API smokes are set up by two separate calls. Idempotent: call it again to change the recipient, "
            + "the URL, the credentials or the robot. Set launch=true to queue the campaign and get its tag. "
            + "Requires the Administrator role.";

    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;
    private final IUserRoleService userRoleService;
    private final SmokeBootstrapService smokeBootstrapService;

    @PostMapping(path = "/smoke/web", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create or update the web smoke campaign (CERBERUS_SMOKE_TESTS_WEB) of the instance",
            description = SMOKE_DESCRIPTION
    )
    public ResponseEntity<Map<String, Object>> smokeWeb(
            @RequestBody SmokeBootstrapRequestDTO request,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) Principal principal) {
        return smoke("web", true, request, apiKey, httpRequest, principal);
    }

    @PostMapping(path = "/smoke/api", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create or update the API smoke campaign (CERBERUS_SMOKE_TESTS_API) of the instance",
            description = SMOKE_DESCRIPTION
    )
    public ResponseEntity<Map<String, Object>> smokeApi(
            @RequestBody SmokeBootstrapRequestDTO request,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) Principal principal) {
        return smoke("api", false, request, apiKey, httpRequest, principal);
    }

    private ResponseEntity<Map<String, Object>> smoke(String kind, boolean web, SmokeBootstrapRequestDTO request, String apiKey,
            HttpServletRequest httpRequest, Principal principal) {
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/bootstrap/smoke/" + kind, "CALL-POST",
                LogEvent.STATUS_INFO, "API /bootstrap/smoke/" + kind + " called", httpRequest, login);

        if (!isAdministrator(principal, login)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "The Administrator role is required"));
        }
        try {
            return ResponseEntity.ok(smokeBootstrapService.setup(request, login, web));
        } catch (IllegalStateException ex) {
            // Without this the error is turned into a redirect to the login page.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", String.valueOf(ex.getMessage())));
        }
    }

    private boolean isAdministrator(Principal principal, String login) {
        if (principal instanceof Authentication authentication
                && authentication.getAuthorities().stream().anyMatch(a -> ("ROLE_" + ROLE_ADMINISTRATOR).equals(a.getAuthority()))) {
            return true;
        }
        try {
            for (UserRole role : userRoleService.findRoleByKey(login)) {
                if (ROLE_ADMINISTRATOR.equals(role.getRole())) {
                    return true;
                }
            }
        } catch (CerberusException ex) {
            return false;
        }
        return false;
    }
}
