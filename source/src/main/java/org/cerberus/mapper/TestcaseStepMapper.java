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

import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.dto.publicv1.TestcaseStepDTOV1;
import org.modelmapper.ModelMapper;

/**
 *
 * @author mlombard
 */


public class TestcaseStepMapper {
    
    private static final ModelMapper modelMapper = new ModelMapper();

    private TestcaseStepMapper() {
        throw new IllegalStateException("This is a utility class");
    }

    public static TestcaseStepDTOV1 convertToDto(TestCaseStep step) {
        return modelMapper.typeMap(TestCaseStep.class, TestcaseStepDTOV1.class)
                .addMappings(
                        mapper -> {
                            mapper.map(src -> src.getTest(), TestcaseStepDTOV1::setTestFolderId);
                            mapper.map(src -> src.getTestcase(), TestcaseStepDTOV1::setTestcaseId);
                            mapper.map(src -> src.getDateCreated().toString(), TestcaseStepDTOV1::setDateCreated);
                            mapper.map(src -> src.getDateModif().toString(), TestcaseStepDTOV1::setDateModif);
                            mapper.map(src -> src.getConditionOptions().toString(), TestcaseStepDTOV1::setConditionOptions);
                        }
                ).map(step);
    }
    
}
