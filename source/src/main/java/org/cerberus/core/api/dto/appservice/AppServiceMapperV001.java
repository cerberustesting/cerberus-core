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
import org.cerberus.core.crud.entity.AppService;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {
            TimestampMapper.class,
            AppServiceHeaderMapperV001.class,
            AppServiceContentMapperV001.class
        }
)

public interface AppServiceMapperV001 {

    @Mapping(source = "followRedir", target = "isFollowingRedirection")
    @Mapping(source = "contentList", target = "contents")
    @Mapping(source = "headerList", target = "headers")
    @Mapping(source = "collection", target = "group")
    AppServiceDTOV001 toDTO(AppService appService);

//    @InheritInverseConfiguration
    @Mapping(source = "followingRedirection", target = "isFollowRedir")
    @Mapping(target = "kafkaFilterHeaderPath", ignore = true)
    @Mapping(target = "kafkaFilterHeaderValue", ignore = true)
    @Mapping(target = "isAvroEnable", ignore = true)
    @Mapping(target = "schemaRegistryURL", ignore = true)
    @Mapping(target = "isAvroEnableKey", ignore = true)
    @Mapping(target = "avroSchemaKey", ignore = true)
    @Mapping(target = "avroSchemaValue", ignore = true)
    @Mapping(target = "isAvroEnableValue", ignore = true)
    @Mapping(target = "parentContentService", ignore = true)
    @Mapping(target = "proxyHost", ignore = true)
    @Mapping(target = "proxyPort", ignore = true)
    @Mapping(target = "proxy", ignore = true)
    @Mapping(target = "proxyWithCredential", ignore = true)
    @Mapping(target = "proxyUser", ignore = true)
    @Mapping(target = "responseHTTPVersion", ignore = true)
    @Mapping(target = "responseHTTPCode", ignore = true)
    @Mapping(target = "responseHTTPBody", ignore = true)
    @Mapping(target = "responseHTTPBodyContentType", ignore = true)
    @Mapping(target = "responseHeaderList", ignore = true)
    @Mapping(target = "timeoutms", ignore = true)
    @Mapping(target = "file", ignore = true)
    @Mapping(target = "kafkaResponseOffset", ignore = true)
    @Mapping(target = "kafkaResponsePartition", ignore = true)
    @Mapping(target = "kafkaWaitNbEvent", ignore = true)
    @Mapping(target = "kafkaWaitSecond", ignore = true)
    @Mapping(target = "recordTraceFile", ignore = true)
    @Mapping(target = "responseNb", ignore = true)
    @Mapping(target = "bodyType", ignore = true)
    @Mapping(target = "simulationParameters", ignore = true)
    @Mapping(target = "authType", ignore = true)
    @Mapping(target = "collection", ignore = true)
    @Mapping(target = "authUser", ignore = true)
    @Mapping(target = "contentList", ignore = true)
    @Mapping(target = "headerList", ignore = true)
    @Mapping(target = "authPassword", ignore = true)
    @Mapping(target = "authAddTo", ignore = true)
    @Mapping(target = "start", ignore = true)
    @Mapping(target = "end", ignore = true)
    AppService toEntity(AppServiceDTOV001 appServiceDTO);
}
