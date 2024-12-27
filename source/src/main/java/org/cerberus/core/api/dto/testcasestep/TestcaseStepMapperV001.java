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
package org.cerberus.core.api.dto.testcasestep;

import org.cerberus.core.api.dto.testcase.TestcaseCountryPropertiesMapperV001;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionMapperV001;
import org.cerberus.core.api.dto.testcasecontrol.TestcaseStepActionControlMapperV001;
import org.cerberus.core.api.mappers.JSONArrayMapper;
import org.cerberus.core.api.mappers.TimestampMapper;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author MorganLmd
 */
@Mapper(
        componentModel = "spring",
        uses = {
            TestcaseStepActionMapperV001.class,
            TestcaseStepActionControlMapperV001.class,
            TestcaseCountryPropertiesMapperV001.class,
            JSONArrayMapper.class,
            TimestampMapper.class
        }
)

public interface TestcaseStepMapperV001 {

    @Mapping(source = "test", target = "testFolderId")
    @Mapping(source = "testcase", target = "testcaseId")
    @Mapping(source = "libraryStepTest", target = "libraryStepTestFolderId")
    @Mapping(source = "libraryStepTestcase", target = "libraryStepTestcaseId")
    @Mapping(source = "usingLibraryStep", target = "isUsingLibraryStep")
    @Mapping(source = "stepInUseByOtherTestcase", target = "isStepInUseByOtherTestcase")
    @Mapping(source = "libraryStep", target = "isLibraryStep")
    @Mapping(source = "executionForced", target = "isExecutionForced")
    TestcaseStepDTOV001 toDTO(TestCaseStep step);

    @Mapping(source = "testFolderId", target = "test")
    @Mapping(source = "testcaseId", target = "testcase")
    @Mapping(source = "libraryStepTestFolderId", target = "libraryStepTest")
    @Mapping(source = "libraryStepTestcaseId", target = "libraryStepTestcase")
    @Mapping(source = "stepInUseByOtherTestcase", target = "isStepInUseByOtherTestcase")
    @Mapping(source = "executionForced", target = "isExecutionForced")
    @Mapping(source = "libraryStep", target = "isLibraryStep")
    @Mapping(source = "usingLibraryStep", target = "isUsingLibraryStep")
    @Mapping(target = "testcaseObj", ignore = true)
    TestCaseStep toEntity(TestcaseStepDTOV001 stepDTO);

}
