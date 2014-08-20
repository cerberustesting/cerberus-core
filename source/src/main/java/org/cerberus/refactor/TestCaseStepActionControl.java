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
package org.cerberus.refactor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestCaseStepActionControl {

    private static final String COLUMNS = "`Test`, `TestCase`, `Step`, `Sequence`, `Control`, `Type`, `ControlValue`, `ControlProperty`, `ControlDescription`, `Fatal`";
    private static final String TABLE = "testcasestepactioncontrol";
    private Integer control;
    private String controlProperty;
    private String controlValue;
    private Integer sequence;
    private Integer step;
    private String test;
    private String testcase;
    private String type;
    private Boolean fatal;
    private String controlDescription;
    @Autowired
    private DatabaseSpring databaseSpring;

    public TestCaseStepActionControl() {

        this.test = new String();
        this.testcase = new String();
        this.step = 0;
        this.sequence = 0;
        this.control = 0;
        this.type = new String();
        this.controlValue = new String();
        this.controlProperty = new String();
        this.fatal = true;
    }

    public Boolean isFatal() {
        return fatal;
    }

    public void setFatal(Boolean fatal) {
        this.fatal = fatal;
    }

    public Integer getControl() {

        return this.control;
    }

    public void setControl(Integer control) {

        this.control = control;
    }

    public String getControlProperty() {

        return this.controlProperty;
    }

    public void setControlProperty(String controlProperty) {

        this.controlProperty = controlProperty;
    }

    public String getControlValue() {

        return this.controlValue;
    }

    public String getControlDescription() {
        return controlDescription;
    }

    public void setControlDescription(String controlDescription) {
        this.controlDescription = controlDescription;
    }

    public void setControlValue(String controlValue) {

        this.controlValue = controlValue;
    }

    public Integer getSequence() {

        return this.sequence;
    }

    public void setSequence(Integer sequence) {

        this.sequence = sequence;
    }

    public Integer getStep() {

        return this.step;
    }

    public void setStep(Integer step) {

        this.step = step;
    }

    public String getTest() {

        return this.test;
    }

    public void setTest(String test) {

        this.test = test;
    }

    public String getTestcase() {

        return this.testcase;
    }

    public void setTestcase(String testcase) {

        this.testcase = testcase;
    }

    public String getType() {

        return this.type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public void importResultSet(ResultSet rs) {

        try {
            this.setTest(rs.getString("Test"));
            this.setTestcase(rs.getString("Testcase"));
            this.setStep(rs.getInt("Step"));
            this.setSequence(rs.getInt("Sequence"));
            this.setControl(rs.getInt("Control"));
            this.setType(rs.getString("Type"));
            this.setControlValue(rs.getString("ControlValue"));
            this.setControlProperty(rs.getString("ControlProperty"));
            this.setControlDescription(rs.getString("ControlDescription"));
            this.setFatal(rs.getString("Fatal").compareTo("Y") == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert() {

        String lfatal;
        if (this.fatal) {
            lfatal = "Y";
        } else {
            lfatal = "N";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + TestCaseStepActionControl.TABLE + " ( "
                + TestCaseStepActionControl.COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.test);
                preStat.setString(2, this.testcase);
                preStat.setString(3, this.step.toString());
                preStat.setString(4, this.sequence.toString());
                preStat.setString(5, this.control.toString());
                preStat.setString(6, this.type);
                preStat.setString(7, this.controlValue);
                preStat.setString(8, this.controlProperty);
                preStat.setString(9, this.controlDescription);
                preStat.setString(10, lfatal);

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControl.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControl.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControl.class.getName(), Level.WARN, e.toString());
            }
        }

    }

    public void update() {
        String lfatal;
        if (this.fatal) {
            lfatal = "Y";
        } else {
            lfatal = "N";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE " + TestCaseStepActionControl.TABLE + " SET ");

        sql.append(" Type = ? ,");
        sql.append(" ControlValue = ?,");
        sql.append(" ControlProperty = ? ,");
        sql.append(" ControlDescription = ? ,");
        sql.append(" Fatal = ? ");

        sql.append(" WHERE Test = ? AND TestCase = ? AND Step = ? AND Sequence = ? AND Control = ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.type);
                preStat.setString(2, this.controlValue);
                preStat.setString(3, this.controlProperty);
                preStat.setString(4, this.controlDescription);
                preStat.setString(5, lfatal);
                preStat.setString(6, this.test);
                preStat.setString(7, this.testcase);
                preStat.setString(8, this.step.toString());
                preStat.setString(9, this.sequence.toString());
                preStat.setString(10, this.control.toString());

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControl.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControl.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControl.class.getName(), Level.WARN, e.toString());
            }
        }
    }
}
