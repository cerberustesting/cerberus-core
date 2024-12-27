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
package org.cerberus.core.crud.entity;

import java.sql.Timestamp;

/**
 * @author bcivel
 */
public class Test {

    private String test;
    private String description;
    private boolean isActive;
    private String parentTest;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String TEST_PRETESTING = "Pre Testing"; // Test that contain all testcases that will automaticly executed before the testcase.
    public static final String TEST_POSTTESTING = "Post Testing"; // Test that contain all testcases that will be automaticly executed after the testcase.

    public boolean getActive() {
        return this.isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getParentTest() {
        return parentTest;
    }

    public void setParentTest(String parentTest) {
        this.parentTest = parentTest;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTest() {
        return this.test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getUsrCreated() {
        return usrCreated;
    }

    public void setUsrCreated(String usrCreated) {
        this.usrCreated = usrCreated;
    }

    public String getUsrModif() {
        return usrModif;
    }

    public void setUsrModif(String usrModif) {
        this.usrModif = usrModif;
    }

    public Timestamp getDateModif() {
        return dateModif;
    }

    public void setDateModif(Timestamp dateModif) {
        this.dateModif = dateModif;
    }

}
