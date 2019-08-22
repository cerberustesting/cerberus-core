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

import org.cerberus.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionQueueDep;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class FactoryTestCaseExecutionQueueDep implements IFactoryTestCaseExecutionQueueDep {


    @Override
    public TestCaseExecutionQueueDep create(long id, long exeQueueId, String environment, String country, String tag, String type, String depTest, String depTestCase
            , String depEvent, String status, Timestamp releaseDate, String comment, long exeId, long queueId, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        TestCaseExecutionQueueDep testCaseDep = new TestCaseExecutionQueueDep();

        testCaseDep.setId(id);
        testCaseDep.setExeQueueId(exeQueueId);
        testCaseDep.setEnvironment(environment);
        testCaseDep.setCountry(country);
        testCaseDep.setTag(tag);
        testCaseDep.setType(type);
        testCaseDep.setDepTest(depTest);
        testCaseDep.setDepTestCase(depTestCase);
        testCaseDep.setDepEvent(depEvent);
        testCaseDep.setStatus(status);
        testCaseDep.setReleaseDate(releaseDate);
        testCaseDep.setComment(comment);
        testCaseDep.setExeId(exeId);
        testCaseDep.setQueueId(queueId);
        testCaseDep.setUsrCreated(usrCreated);
        testCaseDep.setDateCreated(dateCreated);
        testCaseDep.setUsrModif(usrModif);
        testCaseDep.setDateModif(dateModif);

        return testCaseDep;
    }
}
