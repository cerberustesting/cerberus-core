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
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionMapperV001;
import org.cerberus.core.api.dto.testcasecontrol.TestcaseStepActionControlMapperV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepMapperV001;
import org.cerberus.core.api.mappers.JSONArrayMapper;
import org.cerberus.core.api.mappers.TimestampMapper;
import org.cerberus.core.crud.entity.TestCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author mlombard
 */
@Mapper(
        componentModel = "spring",
        uses = {
            TestcaseStepMapperV001.class,
            TestcaseStepActionMapperV001.class,
            TestcaseStepActionControlMapperV001.class,
            TestcaseCountryPropertiesMapperV001.class,
            TestcaseDepMapperV001.class,
            LabelMapperV001.class,
            InvariantMapperV001.class,
            JSONArrayMapper.class,
            TimestampMapper.class
        }
)

public interface TestcaseMapperV001 {

    @Mapping(source = "test", target = "testFolderId")
    @Mapping(source = "testcase", target = "testcaseId")
    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "activeQA", target = "isActiveQA")
    @Mapping(source = "activeUAT", target = "isActiveUAT")
    @Mapping(source = "activePROD", target = "isActivePROD")
    @Mapping(source = "origine", target = "externalProvider")
    @Mapping(source = "refOrigine", target = "externalReference")
    @Mapping(source = "invariantCountries", target = "countries")
    @Mapping(source = "testCaseCountryProperties", target = "properties")
    @Mapping(source = "testCaseInheritedProperties", target = "inheritedProperties")
    TestcaseDTOV001 toDTO(TestCase testcase);

    @Mapping(source = "testFolderId", target = "test")
    @Mapping(source = "testcaseId", target = "testcase")
    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "activeQA", target = "isActiveQA")
    @Mapping(source = "activeUAT", target = "isActiveUAT")
    @Mapping(source = "activePROD", target = "isActivePROD")
    @Mapping(source = "externalProvider", target = "origine")
    @Mapping(source = "externalReference", target = "refOrigine")
    @Mapping(source = "countries", target = "invariantCountries")
    @Mapping(source = "properties", target = "testCaseCountryProperties")
    @Mapping(source = "inheritedProperties", target = "testCaseInheritedProperties")
    @Mapping(target = "ticket", ignore = true)
    @Mapping(target = "system", ignore = true)
    @Mapping(target = "lastExecutionStatus", ignore = true)
    @Mapping(target = "testCaseCountries", ignore = true)
    @Mapping(target = "testCaseLabels", ignore = true)
    @Mapping(target = "refOrigineUrl", ignore = true)
    TestCase toEntity(TestcaseDTOV001 testcaseDTO);
}
