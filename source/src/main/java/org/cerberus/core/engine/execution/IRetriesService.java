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
package org.cerberus.core.engine.execution;

import java.util.Map;
import org.cerberus.core.crud.entity.TestCaseExecution;

public interface IRetriesService {

    /**
     * Retry management, in case the result is not (OK or NE), we execute the
     * job again reducing the retry to 1. Map has 2 values:
     * <br>"AlreadyExecuted" will have nb of already executed <br>"Retry" will
     * be 1 if we need to retry a new execution
     *
     * @param tCExecution
     * @return
     */
    Map<String, Integer> manageRetries(TestCaseExecution tCExecution);

}
