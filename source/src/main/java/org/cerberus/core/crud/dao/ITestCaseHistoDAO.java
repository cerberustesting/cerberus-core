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
package org.cerberus.core.crud.dao;

import java.util.Date;
import org.cerberus.core.crud.entity.TestCaseHisto;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author Vertigo17
 */
public interface ITestCaseHistoDAO {

    /**
     *
     * @param test
     * @param testCase
     * @param version
     * @return
     * @throws CerberusException
     */
    public TestCaseHisto readByKey(String test, String testCase, int version) throws CerberusException;

    /**
     *
     * @param test
     * @param Testcase
     * @return
     */
    public AnswerList<TestCaseHisto> readByTestCase(String test, String Testcase);

    /**
     *
     * @param from
     * @param to
     * @return
     */
    public AnswerList<TestCaseHisto> readByDate(Date from, Date to);

    /**
     *
     * @param testCaseHisto
     * @return
     */
    public Answer create(TestCaseHisto testCaseHisto);

}
