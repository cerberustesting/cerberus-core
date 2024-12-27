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
package org.cerberus.core.api.dto.invariant;

import org.cerberus.core.crud.entity.Invariant;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author mlombard
 */
@Mapper(componentModel = "spring")

public interface InvariantMapperV001 {

    @Mapping(source = "invariant.gp1", target = "attribute1")
    @Mapping(source = "invariant.gp2", target = "attribute2")
    @Mapping(source = "invariant.gp3", target = "attribute3")
    @Mapping(source = "invariant.gp4", target = "attribute4")
    @Mapping(source = "invariant.gp5", target = "attribute5")
    @Mapping(source = "invariant.gp6", target = "attribute6")
    @Mapping(source = "invariant.gp7", target = "attribute7")
    @Mapping(source = "invariant.gp8", target = "attribute8")
    @Mapping(source = "invariant.gp9", target = "attribute9")
    @Mapping(target = "shortDescription", ignore = true)
    InvariantDTOV001 toDTO(Invariant invariant);

    @InheritInverseConfiguration
    @Mapping(target = "veryShortDesc", ignore = true)
    Invariant toEntity(InvariantDTOV001 invariantDTO);
}
