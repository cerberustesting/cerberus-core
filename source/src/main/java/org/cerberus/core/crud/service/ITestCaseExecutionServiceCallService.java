/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.crud.service;

import java.util.Date;
import java.util.List;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionServiceCall;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.util.answer.AnswerList;

public interface ITestCaseExecutionServiceCallService {

    /**
     * Records a call of a registered service made during an execution. Never throws : a failure
     * to record the statistic must not affect the execution.
     *
     * @param execution the execution that made the call
     * @param service the service as it was called, with the response, start and end of the call
     * @param result result of the call
     */
    void recordCall(TestCaseExecution execution, AppService service, MessageEvent result);

    /**
     * Links the recorded call of a service to the level under which its request / response files were
     * stored. Never throws.
     *
     * @param execution the execution that made the call
     * @param service the service as it was called
     * @param fileLevel level of the files in testcaseexecutionfile
     */
    void attachFiles(TestCaseExecution execution, AppService service, String fileLevel);

    /**
     * Deletes the calls older than the retention. Never throws.
     *
     * @param retentionDays number of days to keep, 0 or less keeps everything
     * @return the number of calls deleted
     */
    int purge(int retentionDays);

    AnswerList<TestCaseExecutionServiceCall> readByService(String service, Date from, Date to);

    List<String[]> readServicesWithCalls(Date from, Date to);
}
