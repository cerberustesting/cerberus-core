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
package org.cerberus.crud.dao;

import java.util.List;
import org.cerberus.crud.entity.TestCase;

import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
public interface ITestCaseCountryDAO {

    TestCaseCountry findTestCaseCountryByKey(String test, String testcase, String country) throws CerberusException;

    List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testcase);

    void insertTestCaseCountry(TestCaseCountry testCaseCountry) throws CerberusException;

    void deleteTestCaseCountry(TestCaseCountry tcc) throws CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @param country
     * @return
     */
    AnswerItem<TestCaseCountry> readByKey(String test, String testCase, String country);

    /**
     *
     * @param system
     * @param test
     * @param testCase
     * @param testCaseList
     * @return
     */
    AnswerList<TestCaseCountry> readByVarious1(List<String> system, String test, String testCase, List<TestCase> testCaseList);

    /**
     *
     * @param object
     * @return
     */
    Answer create(TestCaseCountry object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(TestCaseCountry object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(TestCaseCountry object);

}
