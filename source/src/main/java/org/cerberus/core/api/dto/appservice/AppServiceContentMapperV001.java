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
package org.cerberus.core.api.dto.appservice;

import org.cerberus.core.api.mappers.TimestampMapper;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {
            TimestampMapper.class
        }
)

public interface AppServiceContentMapperV001 {

    @Mapping(source = "active", target = "isActive")
    AppServiceContentDTOV001 toDTO(AppServiceContent appServiceContent);

    @Mapping(target = "inherited", ignore = true)
    AppServiceContent toEntity(AppServiceContentDTOV001 appServiceContentDTO);
}
