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
package org.cerberus.core.api.dto.application;

import org.cerberus.core.crud.entity.ApplicationObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper between {@link ApplicationObject} and {@link ApplicationObjectDTOV001}.
 *
 * <p>The entity field {@code ID} (uppercase) maps to the DTO field {@code id} (lowercase).
 * All date fields are already {@code String} on the entity so no {@code TimestampMapper} is needed.</p>
 */
@Mapper(componentModel = "spring")
public interface ApplicationObjectMapperV001 {

    @Mapping(source = "ID", target = "id")
    ApplicationObjectDTOV001 toDTO(ApplicationObject applicationObject);

    @Mapping(source = "id", target = "ID")
    ApplicationObject toEntity(ApplicationObjectDTOV001 dto);
}
