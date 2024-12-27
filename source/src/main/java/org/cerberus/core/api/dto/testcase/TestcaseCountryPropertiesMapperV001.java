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
package org.cerberus.core.api.dto.testcase;

import org.cerberus.core.api.dto.invariant.InvariantMapperV001;
import org.cerberus.core.api.mappers.TimestampMapper;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author MorganLmd
 */
@Mapper(componentModel = "spring",
        uses = {
            InvariantMapperV001.class,
            TimestampMapper.class
        }
)

public interface TestcaseCountryPropertiesMapperV001 {

    @Mapping(source = "test", target = "testFolderId")
    @Mapping(source = "testcase", target = "testcaseId")
    @Mapping(source = "invariantCountries", target = "countries")
    TestcaseCountryPropertiesDTOV001 toDTO(TestCaseCountryProperties testCaseCountryProperties);

    @Mapping(source = "testFolderId", target = "test")
    @Mapping(source = "testcaseId", target = "testcase")
    @Mapping(source = "countries", target = "invariantCountries")
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "cacheExpire", ignore = true)
    @Mapping(target = "retryNb", ignore = true)
    @Mapping(target = "retryPeriod", ignore = true)
    @Mapping(target = "result", ignore = true)
    @Mapping(target = "testcaseCountry", ignore = true)
    @Mapping(target = "testcaseCountries", ignore = true)
    TestCaseCountryProperties toEntity(TestcaseCountryPropertiesDTOV001 testcaseCountryPropertiesDTO);
}
