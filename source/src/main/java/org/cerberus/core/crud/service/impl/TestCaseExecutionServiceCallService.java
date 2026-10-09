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
package org.cerberus.core.crud.service.impl;

import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseExecutionServiceCallDAO;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionServiceCall;
import org.cerberus.core.crud.service.ITestCaseExecutionServiceCallService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestCaseExecutionServiceCallService implements ITestCaseExecutionServiceCallService {

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionServiceCallService.class);
    private static final int PURGE_BATCH_SIZE = 5000;
    private static final int PURGE_MAX_BATCHES = 200;   // 1 million rows by run at most, the next run goes on

    @Autowired
    private ITestCaseExecutionServiceCallDAO testCaseExecutionServiceCallDAO;

    @Override
    public void recordCall(TestCaseExecution execution, AppService service, String serviceName, MessageEvent result) {
        try {
            // Registered services only, and only calls that were really sent (the protocol layer sets the start just before).
            if (execution == null || execution.getId() <= 0 || service == null || StringUtil.isEmptyOrNull(serviceName)
                    || "null".equals(serviceName) || service.getStart() == null) {
                return;
            }
            // A call that failed before the answer (timeout, unreachable) has no end: it lasted until now.
            long end = service.getEnd() != null ? service.getEnd().getTime() : System.currentTimeMillis();
            TestCaseExecutionServiceCall call = new TestCaseExecutionServiceCall();
            call.setExeId(execution.getId());
            call.setStart(service.getStart().getTime());
            call.setService(serviceName);
            call.setApplication(service.getApplication());
            call.setType(service.getType());
            call.setMethod(service.getMethod());
            call.setHttpCode(service.getResponseHTTPCode());
            call.setDurationMs((int) Math.max(0, end - service.getStart().getTime()));
            call.setResponseSize(service.getResponseHTTPBody() != null ? service.getResponseHTTPBody().length()
                    : (service.getFile() != null ? service.getFile().length : 0));
            call.setStatus(result == null ? "" : result.getCodeString());
            call.setSystem(StringUtil.isEmptyOrNull(execution.getSystem()) ? "" : execution.getSystem());
            call.setTest(execution.getTest());
            call.setTestcase(execution.getTestCase());
            call.setCountry(execution.getCountry());
            call.setEnvironment(execution.getEnvironment());
            call.setRobotDecli(execution.getRobotDecli());
            testCaseExecutionServiceCallDAO.create(call);
        } catch (Exception ex) {
            LOG.warn("Unable to record the service call statistic. " + ex.toString(), ex);
        }
    }

    @Override
    public void attachFiles(TestCaseExecution execution, AppService service, String fileLevel) {
        try {
            if (execution == null || execution.getId() <= 0 || service == null || service.getStart() == null || StringUtil.isEmptyOrNull(fileLevel)) {
                return;
            }
            testCaseExecutionServiceCallDAO.setFileLevel(execution.getId(), service.getStart().getTime(), fileLevel);
        } catch (Exception ex) {
            LOG.warn("Unable to link the service call statistic to its files. " + ex.toString(), ex);
        }
    }

    @Override
    public int purge(int retentionDays) {
        if (retentionDays <= 0) {
            return 0;
        }
        int total = 0;
        try {
            Date before = new Date(System.currentTimeMillis() - retentionDays * 24L * 3600 * 1000);
            // By small batches so that a first purge of a big table does not hold long locks.
            for (int batch = 0; batch < PURGE_MAX_BATCHES; batch++) {
                int deleted = testCaseExecutionServiceCallDAO.deleteOlderThan(before, PURGE_BATCH_SIZE);
                total += deleted;
                if (deleted < PURGE_BATCH_SIZE) {
                    break;
                }
            }
        } catch (Exception ex) {
            LOG.warn("Unable to purge the service call statistics. " + ex.toString(), ex);
        }
        return total;
    }

    @Override
    public AnswerList<TestCaseExecutionServiceCall> readByService(String service, Date from, Date to) {
        return testCaseExecutionServiceCallDAO.readByService(service, from, to);
    }

    @Override
    public List<String[]> readServicesWithCalls(Date from, Date to) {
        return testCaseExecutionServiceCallDAO.readServicesWithCalls(from, to);
    }
}
