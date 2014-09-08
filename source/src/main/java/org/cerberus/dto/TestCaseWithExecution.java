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

import org.cerberus.entity.Application;
import org.cerberus.entity.TCase;

/**
 *
 * @author memiks
 */
public class TestCaseWithExecution extends TCase {

    private long statusExecutionID;
    private String environment;
    private String country;
    private String browser;
    private Long start;
    private Long end;
    private String controlStatus;
    private String controlMessage;
    private Application applicationObject;

    public long getStatusExecutionID() {
        return statusExecutionID;
    }

    public void setStatusExecutionID(long statusExecutionID) {
        this.statusExecutionID = statusExecutionID;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getControlStatus() {
        return controlStatus;
    }

    public void setControlStatus(String controlStatus) {
        this.controlStatus = controlStatus;
    }

    public String getControlMessage() {
        return controlMessage;
    }

    public void setControlMessage(String controlMessage) {
        this.controlMessage = controlMessage;
    }

    public Application getApplicationObject() {
        return applicationObject;
    }

    public void setApplicationObject(Application applicationObject) {
        this.applicationObject = applicationObject;
    }

}
