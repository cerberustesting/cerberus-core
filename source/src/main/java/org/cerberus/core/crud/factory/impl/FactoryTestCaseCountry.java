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
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.factory.IFactoryTestCaseCountry;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseCountry implements IFactoryTestCaseCountry {

    @Override
    public TestCaseCountry create(String test, String testCase, String country) {
        return TestCaseCountry.builder()
                .test(test)
                .testcase(testCase)
                .country(country)
                .testCaseCountryProperty(new ArrayList<>())
                .build();
    }

    @Override
    public TestCaseCountry create(String test, String testCase, String country, Timestamp dateCreated, String usrCreated, Timestamp dateModif, String usrModif) {
        return TestCaseCountry.builder()
                .test(test)
                .testcase(testCase)
                .country(country)
                .testCaseCountryProperty(new ArrayList<>())
                .dateCreated(dateCreated)
                .usrCreated(usrCreated == null ? "" : usrCreated)
                .dateModif(dateModif)
                .usrModif(usrModif == null ? "" : usrModif)
                .build();
    }
}
