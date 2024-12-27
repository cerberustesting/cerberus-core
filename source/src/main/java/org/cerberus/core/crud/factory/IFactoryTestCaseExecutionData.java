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
package org.cerberus.core.crud.factory;

import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestCaseExecutionData;

/**
 *
 * @author bcivel
 */
public interface IFactoryTestCaseExecutionData {

    TestCaseExecutionData create(long id, String property, int index, String description, String value, String type, int rank, String value1, String value2, String value3,
            String rC, String rMessage, long start, long end, long startLong, long endLong, MessageEvent message, int retrynb, int retryperiod,
            String database, String value1Init, String value2Init,String value3Init, String LengthInit, String Length, int rowLimit, String nature,
            String sytem, String environment, String country, String dataLib, String jsonResult, String fromCache);

}
