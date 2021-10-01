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

import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.dto.publicv1.TestcaseStepActionControlDTOV1;
import org.modelmapper.ModelMapper;

/**
 *
 * @author mlombard
 */
public class TestcaseStepActionControlMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    private TestcaseStepActionControlMapper() {
        throw new IllegalStateException("This is a utility class");
    }

    public static TestcaseStepActionControlDTOV1 convertToDto(TestCaseStepActionControl control) {
        return modelMapper.typeMap(TestCaseStepActionControl.class, TestcaseStepActionControlDTOV1.class)
                .addMappings(
                        mapper -> {
                            mapper.map(src -> src.getTest(), TestcaseStepActionControlDTOV1::setTestFolderId);
                            mapper.map(src -> src.getTestcase(), TestcaseStepActionControlDTOV1::setTestcaseId);
                            mapper.map(src -> src.getDateCreated().toString(), TestcaseStepActionControlDTOV1::setDateCreated);
                            mapper.map(src -> src.getDateModif().toString(), TestcaseStepActionControlDTOV1::setDateModif);
                            mapper.map(src -> src.getConditionOptions().toString(), TestcaseStepActionControlDTOV1::setConditionOptions);
                            mapper.map(src -> src.getOptions().toString(), TestcaseStepActionControlDTOV1::setOptions);
                        }
                ).map(control);
    }

}
