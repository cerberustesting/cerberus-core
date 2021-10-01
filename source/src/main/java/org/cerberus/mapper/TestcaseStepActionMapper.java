/**
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
package org.cerberus.mapper;

import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.dto.publicv1.TestcaseStepActionDTOV1;
import org.modelmapper.ModelMapper;

/**
 *
 * @author mlombard
 */


public class TestcaseStepActionMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    private TestcaseStepActionMapper() {
        throw new IllegalStateException("This is a utility class");
    }

    public static TestcaseStepActionDTOV1 convertToDto(TestCaseStepAction action) {
        return modelMapper.typeMap(TestCaseStepAction.class, TestcaseStepActionDTOV1.class)
                .addMappings(
                        mapper -> {
                            mapper.map(src -> src.getTest(), TestcaseStepActionDTOV1::setTestFolderId);
                            mapper.map(src -> src.getTestcase(), TestcaseStepActionDTOV1::setTestcaseId);
                            mapper.map(src -> src.getDateCreated().toString(), TestcaseStepActionDTOV1::setDateCreated);
                            mapper.map(src -> src.getDateModif().toString(), TestcaseStepActionDTOV1::setDateModif);
                            mapper.map(src -> src.getConditionOptions().toString(), TestcaseStepActionDTOV1::setConditionOptions);
                            mapper.map(src -> src.getOptions().toString(), TestcaseStepActionDTOV1::setOptions);
                        }
                ).map(action);
    }
    
}
