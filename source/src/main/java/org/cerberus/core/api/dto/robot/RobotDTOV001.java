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
package org.cerberus.core.api.dto.robot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author bcivel
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "robotID",
        "robot",
        "type",
        "platform",
        "browser",
        "version",
        "isActive",
        "userAgent",
        "screenSize",
        "profileFolder",
        "extraParam",
        "acceptNotifications",
        "isAcceptInsecureCerts",
        "robotDecli",
        "lbexemethod",
        "description",
        "usrCreated",
        "dateCreated",
        "usrModif",
        "dateModif",
        "capabilities",
        "executors"
})
@Schema(name = "Robot")
public class RobotDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "Robot unique identifier", example = "12")
    private Integer robotID;

    @NotNull
    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Robot name", example = "CHROME_LINUX")
    private String robot;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Robot type", example = "GUI")
    private String type;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Platform", example = "LINUX")
    private String platform;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Browser", example = "chrome")
    private String browser;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Browser version", example = "120.0")
    private String version;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Robot activation status", example = "true")
    private Boolean isActive;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "User agent", example = "Mozilla/5.0 ...")
    private String userAgent;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Screen resolution", example = "1920x1080")
    private String screenSize;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Profile folder", example = "/opt/cerberus/profiles/chrome")
    private String profileFolder;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Extra parameters (JSON or text)", example = "{\"headless\":true}")
    private String extraParam;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Accept notifications", example = "1")
    private Integer acceptNotifications;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Accept insecure certificates", example = "false")
    private Boolean isAcceptInsecureCerts;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Robot declination", example = "CHROME_LINUX_01")
    private String robotDecli;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(
            description = "Load balancing executor method",
            example = "ROUNDROBIN",
            allowableValues = {"ROUNDROBIN", "BYRANKING"}
    )
    private String lbexemethod;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Robot description", example = "Chrome robot for Linux execution")
    private String description;

    @JsonView(View.Public.GET.class)
    @Schema(description = "User who created the robot", example = "admin")
    private String usrCreated;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Creation date")
    private Timestamp dateCreated;

    @JsonView(View.Public.GET.class)
    @Schema(description = "User who last modified the robot", example = "qa_user")
    private String usrModif;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Last modification date")
    private Timestamp dateModif;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Robot capabilities")
    private List<RobotCapabilityDTOV001> capabilities;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Robot executors")
    private List<RobotExecutorDTOV001> executors;
}