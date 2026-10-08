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
package org.cerberus.core.crud.dao;

import java.util.Date;
import java.util.List;
import org.cerberus.core.crud.entity.TestCaseExecutionServiceCall;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

public interface ITestCaseExecutionServiceCallDAO {

    Answer create(TestCaseExecutionServiceCall object);

    /**
     * Links a recorded call to the level under which its files were stored.
     *
     * @param exeId execution of the call
     * @param service name of the service
     * @param start start of the call (epoch milliseconds)
     * @param fileLevel level of the files in testcaseexecutionfile
     */
    void setFileLevel(long exeId, String service, long start, String fileLevel);

    /**
     * @param service name of the service
     * @param from start of the period (included)
     * @param to end of the period (included)
     * @return the calls of the service over the period, oldest first
     */
    AnswerList<TestCaseExecutionServiceCall> readByService(String service, Date from, Date to);

    /**
     * @param from start of the period (included)
     * @param to end of the period (included)
     * @return the services called over the period with their number of calls
     */
    List<String[]> readServicesWithCalls(Date from, Date to);
}
