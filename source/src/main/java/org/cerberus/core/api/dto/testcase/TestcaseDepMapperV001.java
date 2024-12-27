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

import org.cerberus.core.crud.entity.TestCaseDep;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author MorganLmd
 */
@Mapper(componentModel = "spring")

public interface TestcaseDepMapperV001 {

    @Mapping(source = "dependencyTest", target = "dependencyTestFolderId")
    @Mapping(source = "dependencyTestcase", target = "dependencyTestcaseId")
    @Mapping(source = "active", target = "isActive")
    TestcaseDepDTOV001 toDTO(TestCaseDep testCaseDep);

    @Mapping(source = "dependencyTestFolderId", target = "dependencyTest")
    @Mapping(source = "dependencyTestcaseId", target = "dependencyTestcase")
    @Mapping(source = "active", target = "isActive")
    @Mapping(target = "test", ignore = true)
    @Mapping(target = "testcase", ignore = true)
    @Mapping(target = "testcaseDescription", ignore = true)
    TestCaseDep toEntity(TestcaseDepDTOV001 testcaseDepDTO);
}
