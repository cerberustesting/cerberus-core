/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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

package org.cerberus.api.dto.v001;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.api.dto.views.View;

import javax.validation.constraints.NotNull;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@Schema(name = "Label")
public class LabelDTOV001 {

    @NotNull(message = "Id is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private Integer id;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private String system;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private String label;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private String type;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private String color;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private Integer parentLabelID;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private String requirementType;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private String requirementStatus;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private String requirementCriticality;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private String description;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    private String longDescription;

    @JsonView(value = {View.Public.GET.class})
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    private String dateModif;
}
