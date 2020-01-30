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
package org.cerberus.engine.entity;

import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.cerberus.crud.entity.TestCaseExecution;
import org.springframework.stereotype.Component;

/**
 *
 * @author bcivel
 */
@Component
public class ExecutionUUID {

    private HashMap<String, TestCaseExecution> executionHashMap;

    @PostConstruct
    public void init() {
        executionHashMap = new HashMap<>();
    }

    public HashMap getExecutionUUIDList() {
        return executionHashMap;
    }

    public void setExecutionUUID(String UUID, TestCaseExecution execution) {
        executionHashMap.put(UUID, execution);
    }

    public void removeExecutionUUID(String uuid) {
        executionHashMap.remove(uuid);
    }

    public long getExecutionID(String uuid) {
        TestCaseExecution t = (TestCaseExecution) executionHashMap.get(uuid);
        return t.getId();
    }

    public TestCaseExecution getTestCaseExecution(String uuid) {
        return (TestCaseExecution) executionHashMap.get(uuid);
    }

    public int size() {
        return executionHashMap.size();
    }
}
