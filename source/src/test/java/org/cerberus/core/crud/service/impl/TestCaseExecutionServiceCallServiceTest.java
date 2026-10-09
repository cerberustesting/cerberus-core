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

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.cerberus.core.crud.dao.ITestCaseExecutionServiceCallDAO;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionServiceCall;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The AppService returned by the protocol services (REST, SOAP...) is built by them and has an empty name: the
 * statistic must use the name of the registered service that was called.
 */
class TestCaseExecutionServiceCallServiceTest {

    private static final class StubDAO implements ITestCaseExecutionServiceCallDAO {

        final List<TestCaseExecutionServiceCall> created = new ArrayList<>();
        long linkedStart = -1;
        String linkedLevel;

        @Override
        public Answer create(TestCaseExecutionServiceCall object) {
            created.add(object);
            return new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        }

        @Override
        public int deleteOlderThan(Date before, int limit) {
            return 0;
        }

        @Override
        public void setFileLevel(long exeId, long start, String fileLevel) {
            linkedStart = start;
            linkedLevel = fileLevel;
        }

        @Override
        public AnswerList<TestCaseExecutionServiceCall> readByService(String service, Date from, Date to) {
            return new AnswerList<>();
        }

        @Override
        public List<String[]> readServicesWithCalls(Date from, Date to) {
            return new ArrayList<>();
        }
    }

    private StubDAO dao;
    private TestCaseExecutionServiceCallService service;
    private TestCaseExecution execution;

    @BeforeEach
    void setUp() throws Exception {
        dao = new StubDAO();
        service = new TestCaseExecutionServiceCallService();
        Field f = TestCaseExecutionServiceCallService.class.getDeclaredField("testCaseExecutionServiceCallDAO");
        f.setAccessible(true);
        f.set(service, dao);
        execution = new TestCaseExecution();
        execution.setId(42);
        execution.setSystem("SYS");
        execution.setTest("Test");
        execution.setTestCase("0001A");
        execution.setCountry("FR");
        execution.setEnvironment("PROD");
        execution.setRobotDecli("chrome");
    }

    /** What RestService returns : an AppService created with an empty name. */
    private AppService restAnswer(long start, Long end, int httpCode) {
        AppService s = new AppService();
        s.setService("");
        s.setType(AppService.TYPE_REST);
        s.setMethod(AppService.METHOD_HTTPGET);
        s.setApplication("shop");
        s.setStart(new Timestamp(start));
        if (end != null) {
            s.setEnd(new Timestamp(end));
        }
        s.setResponseHTTPCode(httpCode);
        s.setResponseHTTPBody("hello");
        return s;
    }

    @Test
    void recordsTheCallUnderTheNameOfTheRegisteredService() {
        service.recordCall(execution, restAnswer(1000, 1250L, 200), "payment-api", new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE));

        assertEquals(1, dao.created.size());
        TestCaseExecutionServiceCall c = dao.created.get(0);
        assertEquals("payment-api", c.getService());
        assertEquals(250, c.getDurationMs());
        assertEquals(200, c.getHttpCode());
        assertEquals(5, c.getResponseSize());
        assertEquals("OK", c.getStatus());
        assertEquals(42, c.getExeId());
        assertEquals("0001A", c.getTestcase());
        assertEquals("shop", c.getApplication());
    }

    @Test
    void aCallWithoutAnswerLastedUntilNow() {
        long start = System.currentTimeMillis() - 3000;
        service.recordCall(execution, restAnswer(start, null, 0), "payment-api", new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE));

        assertEquals(1, dao.created.size());
        assertTrue(dao.created.get(0).getDurationMs() >= 3000, "duration " + dao.created.get(0).getDurationMs());
        assertEquals("FA", dao.created.get(0).getStatus());
    }

    @Test
    void doesNotRecordWhatIsNotARegisteredServiceNorACallThatWasSent() {
        service.recordCall(execution, restAnswer(1000, 1100L, 200), "", new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE));
        service.recordCall(execution, restAnswer(1000, 1100L, 200), "null", new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE));
        service.recordCall(execution, restAnswer(1000, 1100L, 200), null, new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE));
        AppService neverSent = restAnswer(1000, 1100L, 0);
        neverSent.setStart(null);
        service.recordCall(execution, neverSent, "payment-api", new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE));
        TestCaseExecution noExecution = new TestCaseExecution();
        service.recordCall(noExecution, restAnswer(1000, 1100L, 200), "payment-api", new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE));
        service.recordCall(null, restAnswer(1000, 1100L, 200), "payment-api", null);

        assertTrue(dao.created.isEmpty());
    }

    @Test
    void linksTheFilesToTheCallByItsStart() {
        service.attachFiles(execution, restAnswer(7000, 7100L, 200), "42-level");

        assertEquals(7000, dao.linkedStart);
        assertEquals("42-level", dao.linkedLevel);
    }
}
