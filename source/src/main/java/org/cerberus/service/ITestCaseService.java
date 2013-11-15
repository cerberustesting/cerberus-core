/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.service;

import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCase;
import org.cerberus.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 * @author tbernardes
 */
public interface ITestCaseService {

    /**
     *
     * @param test
     * @param testCase
     * @return
     */
    TCase findTestCaseByKey(String test, String testCase) throws CerberusException;
    
    TCase findTestCaseByKeyWithDependency (String test, String testCase) throws CerberusException;

    List<TCase> findTestCaseByTest(String test);

    List<TCase> findTestCaseActiveByCriteria(String test, String application, String country);

    boolean updateTestCaseInformation(TestCase testCase);

    boolean updateTestCaseInformationCountries(TestCase tc);

    boolean createTestCase(TestCase testCase);

    /**
     * @since 0.9.1
     */
    List<TCase> findTestCaseByAllCriteria(TCase tCase, String text, String system);

    /**
     * @since 0.9.1
     */
    List<String> findUniqueDataOfColumn(String column);
}
