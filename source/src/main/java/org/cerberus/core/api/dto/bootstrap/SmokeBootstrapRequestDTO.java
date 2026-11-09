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
package org.cerberus.core.api.dto.bootstrap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Variables of the instance on which the smoke content is created.
 */
@Data
@Schema(name = "SmokeBootstrapRequest", description = "Variables of the instance on which the smoke campaign is set up")
public class SmokeBootstrapRequestDTO {

    @Schema(description = "Public base URL of the instance, ex: https://acme.cerberus-testing.com/", requiredMode = Schema.RequiredMode.REQUIRED)
    private String baseUrl;

    @Schema(description = "Recipient of the end of campaign email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recipientEmail;

    @Schema(description = "Login of the smoke user. Used by the web test, and created in the database with a generated API key (used by the API tests) when it does not exist", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testUser;

    @Schema(description = "Password of testUser", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testPassword;

    @Schema(description = "Existing robot that runs the web test", requiredMode = Schema.RequiredMode.REQUIRED)
    private String robot;

    @Schema(description = "System", defaultValue = "CERBERUS")
    private String system = "CERBERUS";

    @Schema(description = "Country of the instance, stored in upper case (demo4 gives DEMO4)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String country;

    @Schema(description = "Environment", defaultValue = "PROD")
    private String environment = "PROD";

    @Schema(description = "Queue the campaign right after the setup", defaultValue = "false")
    private boolean launch = false;
}
