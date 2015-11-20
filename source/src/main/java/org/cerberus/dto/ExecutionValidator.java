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

/**
 *
 * @author cerberus
 */
public class ExecutionValidator {

    private String test;
    private String testCase;
    private String country;
    private String environment;
    private String application;
    private String system;
    private boolean valid;
    private String message;

    public void setTest(String test) {
        this.test = test;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setApplication(String application) {
        this.application = application;
    }
    
    public void setSystem(String system) {
        this.system = system;
    }
    
    public void setValid(boolean bool) {
        this.valid = bool;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public String getTest() {
        return this.test;
    }

    public String getTestCase() {
        return this.testCase;
    }

    public String getCountry() {
        return this.country;
    }

    public String getEnvironment() {
        return this.environment;
    }

    public String getApplication() {
        return this.application;
    }
    
    public String getSystem() {
        return this.system;
    }
    
    public boolean isValid() {
        return this.valid;
    }
    
    public String getMessage() {
        return this.message;
    }
    
}
