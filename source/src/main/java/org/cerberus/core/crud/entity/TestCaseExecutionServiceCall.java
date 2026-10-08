/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.crud.entity;

import lombok.Data;

/**
 * One call of a registered service made during an execution (table testcaseexecutionservicecall).
 */
@Data
public class TestCaseExecutionServiceCall {

    private long id;
    private long exeId;
    private long start;          // epoch milliseconds
    private String service;
    private String application;
    private String type;         // REST, SOAP, KAFKA...
    private String method;
    private int httpCode;        // 0 when the protocol has none
    private int durationMs;
    private int responseSize;
    private String status;       // result code of the call : OK, FA...
    private String system;
    private String test;
    private String testcase;
    private String country;
    private String environment;
    private String robotDecli;
    private String fileLevel;    // level of the recorded request / response files (testcaseexecutionfile), null when not recorded
}
