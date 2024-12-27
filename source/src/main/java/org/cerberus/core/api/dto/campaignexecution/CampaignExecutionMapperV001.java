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

package org.cerberus.core.api.dto.campaignexecution;

import org.cerberus.core.api.mappers.TimestampMapper;
import org.cerberus.core.crud.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author lucashimpens
 */
@Mapper(componentModel = "spring",
        uses = {
                TimestampMapper.class,
                TestcaseExecutionMapperV001.class
        }
)

public interface CampaignExecutionMapperV001 {

    @Mapping(source = "tag", target = "campaignExecutionId")
    @Mapping(source = "campaign", target = "campaignId")
    @Mapping(source = "dateCreated", target = "startDate")
    @Mapping(source = "dateEndQueue", target = "endDate")
    @Mapping(target = "durationInMillis", expression = "java(tag.getDateEndQueue().getTime() - tag.getDateCreated().getTime())")
    @Mapping(source = "nbExe", target = "result.totalWithRetries")
    @Mapping(source = "nbExeUsefull", target = "result.total")
    @Mapping(source = "nbOK", target = "result.ok")
    @Mapping(source = "nbKO", target = "result.ko")
    @Mapping(source = "nbFA", target = "result.fa")
    @Mapping(source = "nbNA", target = "result.na")
    @Mapping(source = "nbPE", target = "result.pe")
    @Mapping(source = "nbCA", target = "result.ca")
    @Mapping(source = "nbQU", target = "result.qu")
    @Mapping(source = "nbWE", target = "result.we")
    @Mapping(source = "nbNE", target = "result.ne")
    @Mapping(source = "nbQE", target = "result.qe")
    @Mapping(source = "executionsNew", target = "executions")
    CampaignExecutionDTOV001 toDto(Tag tag);
}
