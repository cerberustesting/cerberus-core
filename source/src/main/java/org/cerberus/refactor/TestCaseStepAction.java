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

import org.cerberus.database.DatabaseSpring;
import org.cerberus.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class TestCaseStepAction implements DatabaseCRUD {

    private static final String COLUMNS = "`Test`, `TestCase`, `Step`, `Sequence`, `Action`, `Object`, `Property`";
    private static final String TABLE = "testcasestepaction";
    private String action;
    private String object;
    private String property;
    private Integer sequence;
    private Integer step;
    private String test;
    private String testcase;
    @Autowired
    private DatabaseSpring databaseSpring;

    public TestCaseStepAction() {

        this.property = new String();
        this.test = new String();
        this.testcase = new String();
        this.step = 0;
        this.sequence = 0;
        this.action = new String();
        this.object = new String();
        this.property = new String();

    }

    public String getAction() {

        return this.action;
    }

    public void setAction(String action) {

        this.action = action;
    }

    public String getObject() {

        return this.object;
    }

    public void setObject(String object) {

        this.object = object;
    }

    public String getProperty() {

        return this.property;
    }

    public void setProperty(String property) {

        this.property = property;
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

    @Override
    public void importResultSet(ResultSet rs) {

        try {
            this.setTest(rs.getString("Test"));
            this.setTestcase(rs.getString("Testcase"));
            this.setStep(rs.getInt("Step"));
            this.setSequence(rs.getInt("Sequence"));
            this.setAction(rs.getString("Action"));
            this.setObject(rs.getString("Object"));
            this.setProperty(rs.getString("Property"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insert() {

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + TestCaseStepAction.TABLE + " ( " + TestCaseStepAction.COLUMNS + ") ");
        sql.append(" VALUES ( ?,?,?,?,?,?,?) ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.test);
                preStat.setString(2, this.testcase);
                preStat.setString(3, this.step.toString());
                preStat.setString(4, this.sequence.toString());
                preStat.setString(5, this.action);
                preStat.setString(6, this.object);
                preStat.setString(7, this.property);

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepAction.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepAction.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepAction.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public void update() {

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE " + TestCaseStepAction.TABLE + " SET ");

        sql.append(" Action = ? ,");
        sql.append(" Object = ?,");
        sql.append(" Property = ? ");

        sql.append(" WHERE Test = ? AND TestCase = ? AND Step = ? AND Sequence = ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.action);
                preStat.setString(2, this.object);
                preStat.setString(3, this.property);
                preStat.setString(4, this.test);
                preStat.setString(5, this.testcase);
                preStat.setString(6, this.step.toString());
                preStat.setString(7, this.sequence.toString());

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepAction.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepAction.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepAction.class.getName(), Level.WARN, e.toString());
            }
        }
    }
}
