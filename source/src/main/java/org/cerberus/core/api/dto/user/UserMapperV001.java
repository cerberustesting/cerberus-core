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
package org.cerberus.core.api.dto.user;

import org.cerberus.core.api.mappers.TimestampMapper;
import org.cerberus.core.crud.entity.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author vertigo17
 */
@Mapper(componentModel = "spring",
        uses = {
            TimestampMapper.class
        }
)

public interface UserMapperV001 {

    UserDTOV001 toDTO(User user);

    @InheritInverseConfiguration
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "resetPasswordToken", ignore = true)
    @Mapping(target = "userID", ignore = true)
    @Mapping(target = "request", ignore = true)
    @Mapping(target = "apiKey", ignore = true)
    @Mapping(target = "reportingFavorite", ignore = true)
    @Mapping(target = "robotHost", ignore = true)
    @Mapping(target = "robotPort", ignore = true)
    @Mapping(target = "robotPlatform", ignore = true)
    @Mapping(target = "robotVersion", ignore = true)
    @Mapping(target = "robotBrowser", ignore = true)
    @Mapping(target = "robot", ignore = true)
    @Mapping(target = "defaultSystem", ignore = true)
    @Mapping(target = "userPreferences", ignore = true)
    @Mapping(target = "userSystems", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    User toEntity(UserDTOV001 userDTO);
}
