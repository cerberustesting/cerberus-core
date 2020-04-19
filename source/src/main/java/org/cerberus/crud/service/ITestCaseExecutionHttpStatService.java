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
package org.cerberus.crud.service;

import java.util.Date;
import java.util.List;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONObject;

/**
 *
 * @author vertigo
 */
public interface ITestCaseExecutionHttpStatService {

    /**
     *
     * @param object
     * @return
     */
    Answer create(TestCaseExecutionHttpStat object);

    /**
     *
     * @param tce
     * @param har
     * @return
     */
    Answer saveStat(TestCaseExecution tce, JSONObject har);

    /**
     *
     * @param controlStatus
     * @param testcases
     * @param from
     * @param to
     * @param system
     * @param countries
     * @param environments
     * @param robotDecli
     * @return
     */
    AnswerList<TestCaseExecutionHttpStat> readByCriteria(String controlStatus, List<TestCase> testcases, Date from, Date to, List<String> system, List<String> countries, List<String> environments, List<String> robotDecli);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionHttpStat convert(AnswerItem<TestCaseExecutionHttpStat> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecutionHttpStat> convert(AnswerList<TestCaseExecutionHttpStat> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
