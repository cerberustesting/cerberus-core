/*
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

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "id",
        "robot",
        "executor",
        "isActive",
        "rank",
        "host",
        "port",
        "hostUser",
        "hostPassword",
        "executorProxyType",
        "executorProxyServiceHost",
        "executorProxyServicePort",
        "executorBrowserProxyHost",
        "executorBrowserProxyPort",
        "executorExtensionPort",
        "executorExtensionProxyPort",
        "deviceUdid",
        "deviceName",
        "devicePort",
        "isDeviceLockUnlock",
        "description",
        "dateLastExeSubmitted",
        "usrCreated",
        "dateCreated",
        "usrModif",
        "dateModif"
})
@Schema(name = "RobotExecutor")
public class RobotExecutorDTOV001 {

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Technical identifier", example = "10")
    private Integer id;

    @NotNull
    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Robot name", example = "CHROME_LINUX")
    private String robot;

    @NotNull
    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Executor name", example = "EXECUTOR_01", required = true)
    private String executor;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Executor activation status", example = "true")
    private Boolean isActive;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Executor ranking", example = "1")
    private Integer rank;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Host", example = "192.168.1.10")
    private String host;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Port", example = "4444")
    private String port;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Host user", example = "cerberus")
    private String hostUser;

    @JsonView({View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Host password (write-only)", example = "secret")
    private String hostPassword;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Executor proxy type",example = "NONE",allowableValues = {"NONE", "MANUAL", "NETWORKTRAFFIC"})
    private String executorProxyType;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Proxy service host", example = "proxy.cerberus.io")
    private String executorProxyServiceHost;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Proxy service port", example = "8080")
    private Integer executorProxyServicePort;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Browser proxy host", example = "proxy.browser.io")
    private String executorBrowserProxyHost;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Browser proxy port", example = "3128")
    private Integer executorBrowserProxyPort;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Executor extension port", example = "9090")
    private Integer executorExtensionPort;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Executor extension proxy port", example = "9091")
    private Integer executorExtensionProxyPort;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Device UDID", example = "emulator-5554")
    private String deviceUdid;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Device name", example = "Pixel_6_API_33")
    private String deviceName;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Device port", example = "5555")
    private Integer devicePort;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Device lock/unlock enabled", example = "false")
    private Boolean isDeviceLockUnlock;

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PATCH.class, View.Public.PUT.class})
    @Schema(description = "Executor description")
    private String description;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Last execution submission timestamp", example = "1700000000")
    private Long dateLastExeSubmitted;

    @JsonView(View.Public.GET.class)
    @Schema(description = "User who created the executor", example = "admin")
    private String usrCreated;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Creation date")
    private Timestamp dateCreated;

    @JsonView(View.Public.GET.class)
    @Schema(description = "User who last modified the executor", example = "qa_user")
    private String usrModif;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Last modification date")
    private Timestamp dateModif;
}
