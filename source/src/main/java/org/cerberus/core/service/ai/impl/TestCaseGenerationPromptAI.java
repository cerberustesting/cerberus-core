/**
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
package org.cerberus.core.service.ai.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.api.dto.testcase.TestcaseDTOV001;
import org.cerberus.core.api.dto.testcase.TestcaseMapperV001;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionDTOV001;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionMapperV001;
import org.cerberus.core.api.dto.testcasecontrol.TestcaseStepActionControlDTOV001;
import org.cerberus.core.api.dto.testcasecontrol.TestcaseStepActionControlMapperV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepDTOV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepMapperV001;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TestCaseGenerationPromptAI {

    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private ITestCaseStepActionService testCaseStepActionService;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private TestcaseMapperV001 testcaseMapper;
    @Autowired
    private TestcaseStepMapperV001 testcaseStepMapper;
    @Autowired
    private TestcaseStepActionMapperV001 testcaseStepActionMapper;
    @Autowired
    private TestcaseStepActionControlMapperV001 testcaseStepActionControlMapper;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    /**
     * Create TestCase from IA
     */
    public TestCase createTestCaseFromAiResponse(String aiJson, String testcaseId, String username) throws IOException {

        TestcaseDTOV001 dto = objectMapper.readValue(aiJson, TestcaseDTOV001.class);

        dto.setUsrCreated(username);
        dto.setVersion(1);
        dto.setStatus("STANDBY");
        dto.setTestcaseId(testcaseId);

        TestCase entity = testcaseMapper.fromAItoEntity(dto);
        System.out.println(aiJson);
        System.out.println(entity.toJson());
        testCaseService.create(entity);

        return entity;
    }


    public TestCaseStep createTestCaseStepFromAiResponse(String aiJson, TestCase testcase) throws IOException {

        TestcaseStepDTOV001 dto = objectMapper.readValue(aiJson, TestcaseStepDTOV001.class);
        dto.setTestFolderId(testcase.getTest());
        dto.setTestcaseId(testcase.getTestcase());

        TestCaseStep entity = testcaseStepMapper.toEntity(dto);

        testCaseStepService.create(entity);

        return entity;
    }

    public TestCaseStepAction createTestCaseStepActionFromAiResponse(String aiJson, TestCaseStep testcaseStep) throws IOException {

        TestcaseStepActionDTOV001 dto = objectMapper.readValue(aiJson, TestcaseStepActionDTOV001.class);
        dto.setTestFolderId(testcaseStep.getTest());
        dto.setTestcaseId(testcaseStep.getTestcase());

        TestCaseStepAction entity = testcaseStepActionMapper.toEntity(dto);

        testCaseStepActionService.create(entity);

        return entity;
    }

    public TestCaseStepActionControl createTestCaseStepActionControlFromAiResponse(String aiJson, TestCaseStep testcaseStepAction) throws IOException {

        TestcaseStepActionControlDTOV001 dto = objectMapper.readValue(aiJson, TestcaseStepActionControlDTOV001.class);
        dto.setTestFolderId(testcaseStepAction.getTest());
        dto.setTestcaseId(testcaseStepAction.getTestcase());

        TestCaseStepActionControl entity = testcaseStepActionControlMapper.toEntity(dto);

        testCaseStepActionControlService.create(entity);

        return entity;
    }

}


