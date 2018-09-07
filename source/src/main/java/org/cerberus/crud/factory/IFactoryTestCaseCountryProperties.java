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
package org.cerberus.crud.factory;

import org.cerberus.crud.entity.TestCaseCountryProperties;

/**
 * @author bcivel
 * @author FNogueira
 */
public interface IFactoryTestCaseCountryProperties {

    /**
     *
     * @param test
     * @param testCase
     * @param country
     * @param property
     * @param description
     * @param type
     * @param database
     * @param value1
     * @param value2
     * @param length
     * @param rowLimit
     * @param nature
     * @param retryNb
     * @param retryPeriod
     * @param cacheExpire
     * @param rank
     * @return
     */
    TestCaseCountryProperties create(String test, String testCase, String country, String property, String description,
            String type, String database, String value1, String value2, String length, int rowLimit, String nature, int retryNb, 
            int retryPeriod, int cacheExpire, int rank);

}
