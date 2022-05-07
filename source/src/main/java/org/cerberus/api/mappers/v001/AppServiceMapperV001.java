/*
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

package org.cerberus.api.mappers.v001;

import org.cerberus.api.dto.v001.AppServiceDTOV001;
import org.cerberus.api.mappers.TimestampMapper;
import org.cerberus.crud.entity.AppService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TimestampMapper.class})
public interface AppServiceMapperV001 {

    @Mapping(source = "followRedir", target = "isFollowingRedirection")
    AppServiceDTOV001 toDTO(AppService appService);

    @Mapping(source = "followingRedirection", target = "followRedir")
    AppService toEntity(AppServiceDTOV001 appServiceDTO);
}
