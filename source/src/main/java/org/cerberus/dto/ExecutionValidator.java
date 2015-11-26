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
package org.cerberus.dto;

import org.cerberus.crud.entity.TestCaseExecution;

/**
 *
 * @author cerberus
 */
public class ExecutionValidator {

    private TestCaseExecution execution;
    private boolean runQA;
    private boolean runUAT;
    private boolean runPROD;
    private boolean valid;
    private String message;

    public void setExecution(TestCaseExecution execution) {
        this.execution = execution;
    }

    public void setRunQA(String runQA) {
        this.runQA = runQA.equals("Y");
    }

    public void setRunUAT(String runUAT) {
        this.runUAT = runUAT.equals("Y");
    }

    public void setRunPROD(String runPROD) {
        this.runPROD = runPROD.equals("Y");
    }

    public void setValid(boolean bool) {
        this.valid = bool;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TestCaseExecution getExecution() {
        return this.execution;
    }

    public boolean getRunQA() {
        return this.runQA;
    }
    
    public boolean getRunUAT() {
        return this.runUAT;
    }
    
    public boolean getRunPROD() {
        return this.runPROD;
    }
    
    public boolean isValid() {
        return this.valid;
    }

    public String getMessage() {
        return this.message;
    }

}
