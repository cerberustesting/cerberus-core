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

package org.cerberus.api.mapper.v001;

import org.cerberus.api.dto.v001.LabelDTOV001;
import org.cerberus.api.mapper.TimestampMapper;
import org.cerberus.crud.entity.Label;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author MorganLmd
 */
@Mapper(componentModel = "spring", uses = {TimestampMapper.class})
public interface LabelMapperV001 {

    @Mapping(source = "requirementCriticity", target = "requirementCriticality")
    LabelDTOV001 toDTO(Label label);

    @InheritInverseConfiguration
    Label toEntity(LabelDTOV001 labelDTO);
}
