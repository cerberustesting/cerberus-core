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
package org.cerberus.core.api.dto.debugexecution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "test", "testCase", "country", "environment", "robot", "browser", "version", "platform", "tag", "timeoutMs"
})
@Schema(name = "DebugExecutionStart")
public class DebugExecutionStartDTOV001 {

    @JsonView(View.Public.POST.class)
    @Schema(description = "Test folder of the test case to debug", requiredMode = Schema.RequiredMode.REQUIRED)
    private String test;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Test case reference to debug", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testCase;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Country to execute on", requiredMode = Schema.RequiredMode.REQUIRED)
    private String country;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Environment to execute on", requiredMode = Schema.RequiredMode.REQUIRED)
    private String environment;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Robot to use for the execution")
    private String robot;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Browser to use (can be overridden by robot)")
    private String browser;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Browser version to use (can be overridden by robot)")
    private String version;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Platform to use (can be overridden by robot)")
    private String platform;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Tag stored on the execution")
    private String tag;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Element-wait timeout (ms) used for the whole debug session, so no action fails while the user is stepping through slowly. Defaults to a large value, clamped server-side.")
    private Integer timeoutMs;
}