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
package org.cerberus.crud.factory.impl;

import org.cerberus.crud.entity.TestCaseDep;
import org.cerberus.crud.factory.IFactoryTestCaseDep;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class FactoryTestCaseDep implements IFactoryTestCaseDep {

    @Override
    public TestCaseDep create(Long id, String test, String testCase, String depTest, String depTestCase, String depEvent, String type, String active, String description
            , String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        TestCaseDep dep = new TestCaseDep();

        dep.setId(id);
        dep.setTest(test);
        dep.setTestCase(testCase);
        dep.setDepTest(depTest);
        dep.setDepTestCase(depTestCase);
        dep.setType(type);
        dep.setDepEvent(depEvent);
        dep.setActive(active);
        dep.setDescription(description);
        dep.setUsrCreated(usrCreated);
        dep.setDateCreated(dateCreated);
        dep.setUsrModif(usrModif);
        dep.setDateModif(dateModif);

        return dep;
    }


}
