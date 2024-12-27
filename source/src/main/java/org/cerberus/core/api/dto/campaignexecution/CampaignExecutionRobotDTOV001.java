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

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "CampaignExecutionRobot")
public class CampaignExecutionRobotDTOV001 {

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 0)
    private String robotId;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 1)
    private String executor;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 2)
    private String host;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 3)
    private String port;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 4)
    private String declination;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 5)
    private String browser;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 6)
    private String version;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 7)
    private String platform;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 8)
    private String screenSize;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 11)
    private String userAgent;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 12)
    private String sessionId;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 13)
    private String provider;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 14)
    private String providerSessionId;
}
