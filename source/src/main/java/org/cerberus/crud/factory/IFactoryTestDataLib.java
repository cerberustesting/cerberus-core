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
package org.cerberus.crud.factory;

import java.sql.Timestamp;
import org.cerberus.crud.entity.TestDataLib;

/**
 *
 * @author vertigo17
 */
public interface IFactoryTestDataLib {

    /**
     *
     * @param testDataLibID
     * @param name
     * @param system
     * @param country
     * @param environment
     * @param group
     * @param type
     * @param database
     * @param script
     * @param servicePath
     * @param method
     * @param envelope
     * @param description
     * @param creator
     * @param Created
     * @param LastModifier
     * @param LastModified
     * @return a TestData
     */
    TestDataLib create(Integer testDataLibID, String name, String system, String environment,
            String country, String group, String type,
            String database, String script, String servicePath,
            String method, String envelope, String description,
            String creator, Timestamp Created, String LastModifier, Timestamp LastModified);

}
