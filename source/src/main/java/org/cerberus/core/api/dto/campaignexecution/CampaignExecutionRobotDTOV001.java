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

package org.cerberus.core.api.dto.campaignexecution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author lucashimpens
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
        "robotId", "executor", "host", "port", "declination", "browser", "version",
        "platform", "screenSize", "userAgent", "sessionId", "provider", "providerSessionId"
})
@Schema(name = "CampaignExecutionRobot")
public class CampaignExecutionRobotDTOV001 {

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "robotId", example = "robot-chrome-linux-001")
    private String robotId;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "executor", example = "cerberus")
    private String executor;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "host", example = "localhost")
    private String host;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "port", example = "4444")
    private String port;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "declination", example = "default")
    private String declination;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "browser", example = "chrome")
    private String browser;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "version", example = "115.0")
    private String version;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "platform", example = "LINUX")
    private String platform;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "screenSize", example = "1920x1080")
    private String screenSize;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "userAgent")
    private String userAgent;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "sessionId")
    private String sessionId;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "provider")
    private String provider;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "providerSessionId")
    private String providerSessionId;
}