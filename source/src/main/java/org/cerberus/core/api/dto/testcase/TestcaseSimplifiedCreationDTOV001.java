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
package org.cerberus.core.api.dto.testcase;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "Testcase")
public class TestcaseSimplifiedCreationDTOV001 {

    @NotBlank(message = "Test folder id is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "Examples", position = 0)
    private String testFolderId;

    @NotBlank(message = "Testcase id is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "0001A", position = 1)
    private String testcaseId;

    @NotBlank(message = "Application attribute is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "Google", position = 2)
    private String application;

    @NotBlank(message = "A description is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "Search for Cerberus Website", position = 3)
    private String description;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "www.cerberus-testing.com", position = 4)
    private String type;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "www.cerberus-testing.com", position = 5)
    private String url;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "FR", position = 6)
    private String country;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "PROD", position = 7)
    private String environment;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "DEFAULT", position = 8)
    private String system;

}
