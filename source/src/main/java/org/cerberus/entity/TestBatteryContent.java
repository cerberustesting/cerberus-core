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
package org.cerberus.entity;

import java.io.Serializable;

/**
 *
 * @author memiks
@Entity
@Table(catalog = "cerberus", schema = "", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"testbattery", "Test", "TestCase"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TestBatteryContent.findAll", query = "SELECT t FROM TestBatteryContent t"),
    @NamedQuery(name = "TestBatteryContent.findByTestbatterycontentID", query = "SELECT t FROM TestBatteryContent t WHERE t.testbatterycontentID = :testbatterycontentID")})
 */
public class TestBatteryContent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer testbatterycontentID;
    private TestCase testCase;
    private TestBattery testbattery;

    public TestBatteryContent() {
    }

    public TestBatteryContent(Integer testbatterycontentID) {
        this.testbatterycontentID = testbatterycontentID;
    }

    public Integer getTestbatterycontentID() {
        return testbatterycontentID;
    }

    public void setTestbatterycontentID(Integer testbatterycontentID) {
        this.testbatterycontentID = testbatterycontentID;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public TestBattery getTestbattery() {
        return testbattery;
    }

    public void setTestbattery(TestBattery testbattery) {
        this.testbattery = testbattery;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (testbatterycontentID != null ? testbatterycontentID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TestBatteryContent)) {
            return false;
        }
        TestBatteryContent other = (TestBatteryContent) object;
        if ((this.testbatterycontentID == null && other.testbatterycontentID != null) || (this.testbatterycontentID != null && !this.testbatterycontentID.equals(other.testbatterycontentID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.cerberus.entity.TestBatteryContent[ testbatterycontentID=" + testbatterycontentID + " ]";
    }

}
