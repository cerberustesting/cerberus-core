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

import org.cerberus.crud.entity.TestCaseExecutionQueueDep;

import java.sql.Timestamp;

/**
 * @author vertigo
 */
public interface IFactoryTestCaseExecutionQueueDep {

    /**
     *
     * @param id
     * @param exeQueueId
     * @param environment
     * @param country
     * @param tag
     * @param type
     * @param depTest
     * @param depTestCase
     * @param depEvent
     * @param status
     * @param releaseDate
     * @param comment
     * @param exeId
     * @param queueId
     * @param usrCreated
     * @param dateCreated
     * @param usrModif
     * @param dateModif
     * @return
     */
    TestCaseExecutionQueueDep create(long id, long exeQueueId, String environment, String country, String tag, String type, String depTest, String depTestCase
            , String depEvent, String status, Timestamp releaseDate, String comment, long exeId, long queueId, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif);

}
