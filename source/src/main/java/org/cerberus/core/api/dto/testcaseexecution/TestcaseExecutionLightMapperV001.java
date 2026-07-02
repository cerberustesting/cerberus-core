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
package org.cerberus.core.api.dto.testcaseexecution;

import org.cerberus.core.crud.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class TestcaseExecutionLightMapperV001 {

    private static final Set<String> NOT_DONE_RETURN_CODES = Set.of("PE", "NE", "WE", "QU");

    public TestcaseExecutionLightDTOV001 toDTO(TestCaseExecution execution) {
        ExecutionProgress progress = calculateProgress(execution);

        return TestcaseExecutionLightDTOV001.builder()
                .testcaseExecutionId(execution.getId())
                .queueId(execution.getQueueID())
                .test(execution.getTest())
                .testcase(execution.getTestCase())
                .environment(execution.getEnvironment())
                .country(execution.getCountry())
                .application(execution.getApplication())
                .tag(execution.getTag())
                .controlStatus(execution.getControlStatus())
                .controlMessage(execution.getControlMessage())
                .progressPercent(progress.percent())
                .doneCount(progress.doneCount())
                .totalCount(progress.totalCount())
                .build();
    }

    public TestcaseExecutionLightDTOV001 toDTO(TestCaseExecutionLight execution) {

        return TestcaseExecutionLightDTOV001.builder()
                .testcaseExecutionId(execution.getId())
                .queueId(execution.getQueueId())
                .test(execution.getTest())
                .testcase(execution.getTestCase())
                .environment(execution.getEnvironment())
                .country(execution.getCountry())
                .application(execution.getApplication())
                .tag(execution.getTag())
                .controlStatus(execution.getControlStatus())
                .controlMessage(execution.getControlMessage())
                .build();
    }

    private ExecutionProgress calculateProgress(TestCaseExecution execution) {
        String status = execution.getControlStatus();

        if ("OK".equals(status)) {
            return new ExecutionProgress(100, 1, 1);
        }

        List<TestCaseStepExecution> steps = execution.getTestCaseStepExecutionList();

        if (steps == null || steps.isEmpty()) {
            int percent = isFinalStatus(status) ? 100 : 0;
            return new ExecutionProgress(percent, percent == 100 ? 1 : 0, 1);
        }

        int total = 0;
        int done = 0;

        for (TestCaseStepExecution step : steps) {
            total++;

            if (isDone(step.getReturnCode())) {
                done++;
            }

            List<TestCaseStepActionExecution> actions = step.getTestCaseStepActionExecutionList();

            if (actions == null) {
                continue;
            }

            for (TestCaseStepActionExecution action : actions) {
                total++;

                if (isDone(action.getReturnCode())) {
                    done++;
                }

                List<TestCaseStepActionControlExecution> controls =
                        action.getTestCaseStepActionControlExecutionList();

                if (controls == null) {
                    continue;
                }

                for (TestCaseStepActionControlExecution control : controls) {
                    total++;

                    if (isDone(control.getReturnCode())) {
                        done++;
                    }
                }
            }
        }

        int percent = total > 0
                ? (int) Math.round((done * 100.0) / total)
                : 0;

        return new ExecutionProgress(percent, done, total);
    }

    private boolean isDone(String returnCode) {
        return returnCode != null && !NOT_DONE_RETURN_CODES.contains(returnCode);
    }

    private boolean isFinalStatus(String status) {
        return status != null
                && !status.isBlank()
                && !"PE".equals(status)
                && !"NE".equals(status)
                && !"QU".equals(status);
    }

    private record ExecutionProgress(
            int percent,
            int doneCount,
            int totalCount
    ) {
    }
}