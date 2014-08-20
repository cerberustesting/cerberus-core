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
public class TestCaseCountryProperties {

    private static final String COLUMNS = "`Test`, `TestCase`, `Country`, `Property`, `Type`, `Database`, `Value1`, `Value2`, `Length`, `RowLimit`, `Nature`";
    private static final String TABLE = "testcasecountryproperties";
    private String country;
    private Integer length;
    private String nature;
    private String property;
    private Integer rowlimit;
    private String test;
    private String testcase;
    private String type;
    private String database;
    private String value1;
    private String value2;

    @Autowired
    private DatabaseSpring databaseSpring;

    public TestCaseCountryProperties() {

        this.country = new String();
        this.length = 0;
        this.nature = new String();
        this.property = new String();
        this.rowlimit = 0;
        this.test = new String();
        this.testcase = new String();
        this.type = new String();
        this.value1 = new String();
        this.value2 = new String();
        this.database = new String();

    }

    public String getCountry() {

        return this.country;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public Integer getLength() {

        return this.length;
    }

    public void setLength(Integer length) {

        this.length = length;
    }

    public String getNature() {

        return this.nature;
    }

    public void setNature(String nature) {

        this.nature = nature;
    }

    public String getProperty() {

        return this.property;
    }

    public void setProperty(String property) {

        this.property = property;
    }

    public Integer getRowlimit() {

        return this.rowlimit;
    }

    public void setRowlimit(Integer rowlimit) {

        this.rowlimit = rowlimit;
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

    public String getDatabase() {

        return this.database;
    }

    public void setDatabase(String database) {

        this.database = database;
    }

    public String getValue1() {

        return this.value1;
    }

    public void setValue1(String value1) {

        this.value1 = value1;
    }

    public String getValue2() {

        return this.value2;
    }

    public void setValue2(String value2) {

        this.value2 = value2;
    }

    public void importResultSet(ResultSet rs) {

        try {
            this.setTest(rs.getString("Test"));
            this.setTestcase(rs.getString("Testcase"));
            this.setCountry(rs.getString("Country"));
            this.setProperty(rs.getString("Property"));
            this.setValue1(rs.getString("Value1"));
            this.setValue2(rs.getString("Value2"));
            this.setType(rs.getString("Type"));
            this.setRowlimit(rs.getInt("RowLimit"));
            this.setLength(rs.getInt("Length"));
            this.setNature(rs.getString("Nature"));
            this.setDatabase(rs.getString("Database"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert() {

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + TestCaseCountryProperties.TABLE + " ( " + TestCaseCountryProperties.COLUMNS + ") ");
        sql.append(" VALUES ( ?,?,?,?,?,?,?,?,?,?,?) ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.test);
                preStat.setString(2, this.testcase);
                preStat.setString(3, this.country);
                preStat.setString(4, this.property);
                preStat.setString(5, this.type);
                preStat.setString(6, this.database);
                preStat.setString(7, this.value1);
                preStat.setString(8, this.value2);
                preStat.setString(9, this.length.toString());
                preStat.setString(10, this.rowlimit.toString());
                preStat.setString(11, this.nature);

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryProperties.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryProperties.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryProperties.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    public void update() {

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE " + TestCaseCountryProperties.TABLE + " SET ");

        sql.append(" Type = ? ,");
        sql.append(" Database = ? ,");
        sql.append(" Value1 = ?,");
        sql.append(" Value2 = ?,");
        sql.append(" Length = ?, ");
        sql.append(" RowLimit = ?, ");
        sql.append(" Nature = ? ");

        sql.append(" WHERE Test = ? AND TestCase = ? AND Country = ? AND Property = ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.type);
                preStat.setString(2, this.database);
                preStat.setString(3, this.value1);
                preStat.setString(4, this.value2);
                preStat.setString(5, this.length.toString());
                preStat.setString(6, this.rowlimit.toString());
                preStat.setString(7, this.nature);
                preStat.setString(8, this.test);
                preStat.setString(9, this.testcase);
                preStat.setString(10, this.country);
                preStat.setString(11, this.property);

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryProperties.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryProperties.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryProperties.class.getName(), Level.WARN, e.toString());
            }
        }
    }
}
