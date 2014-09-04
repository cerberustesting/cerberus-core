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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.TCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 25/11/2013
 * @since 0.9.1
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class TestCaseManualExecutionDTOTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement statement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private DatabaseSpring databaseSpring;
    @InjectMocks
    private TestCaseManualExecutionDTO testCaseManualExecutionDTO;

    /**
     * @throws SQLException
     */
    @Test
    public void testFindTestCaseManualExecutionWhenReturnNoLine() throws SQLException {
        TCase tCase = new TCase();

        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<TestCaseManualExecution> list = testCaseManualExecutionDTO.findTestCaseManualExecution(tCase, "", "", "", "", "");
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testFindTestCaseManualExecutionWhenReturnOneLine() throws SQLException {
        TCase tCase = new TCase();
        String test = "Test";
        String testCase = "TestCase";
        String value = "Value";
        String howTo = "HowTo";
        String application = "Application";
        String type = "Type";
        String url = "www.com";
        String system = "System";
        String build = "Build";
        String revision = "Revision";
        String status = "Status";
        Timestamp date = new Timestamp(new Date().getTime());

        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("test")).thenReturn(test);
        when(resultSet.getString("testcase")).thenReturn(testCase);
        when(resultSet.getString("behaviororvalueexpected")).thenReturn(value);
        when(resultSet.getString("howto")).thenReturn(howTo);
        when(resultSet.getString("application")).thenReturn(application);
        when(resultSet.getString("type")).thenReturn(type);
        when(resultSet.getString("url")).thenReturn(url);
        when(resultSet.getString("system")).thenReturn(system);
        when(resultSet.getString("build")).thenReturn(build);
        when(resultSet.getString("revision")).thenReturn(revision);
        when(resultSet.getString("controlstatus")).thenReturn(status);
        when(resultSet.getTimestamp("end")).thenReturn(date);

        List<TestCaseManualExecution> list = testCaseManualExecutionDTO.findTestCaseManualExecution(tCase, "", "", "", "", "");
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());

        TestCaseManualExecution tcme = list.get(0);
        Assert.assertEquals(test, tcme.getTest());
        Assert.assertEquals(testCase, tcme.getTestCase());
        Assert.assertEquals(value, tcme.getValueExpected());
        Assert.assertEquals(howTo, tcme.getHowTo());
        Assert.assertEquals(application, tcme.getApplication());
        Assert.assertEquals(type, tcme.getAppType());
        Assert.assertEquals(url, tcme.getUrl());
        Assert.assertEquals(system, tcme.getSystem());
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testFindTestCaseManualExecutionWhenReturnMoreLines() throws SQLException {
        TCase tCase = new TCase();
        String test = "Test";
        String testCase = "TestCase";
        String value = "Value";
        String howTo = "HowTo";
        String application = "Application";
        String type = "Type";
        String url = "www.com";
        String system = "System";
        String build = "Build";
        String revision = "Revision";
        String status = "Status";
        Timestamp date = new Timestamp(new Date().getTime());

        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getString("test")).thenReturn(test);
        when(resultSet.getString("testcase")).thenReturn(testCase);
        when(resultSet.getString("behaviororvalueexpected")).thenReturn(value);
        when(resultSet.getString("howto")).thenReturn(howTo);
        when(resultSet.getString("application")).thenReturn(application);
        when(resultSet.getString("type")).thenReturn(type);
        when(resultSet.getString("url")).thenReturn(url);
        when(resultSet.getString("system")).thenReturn(system);
        when(resultSet.getString("build")).thenReturn(build);
        when(resultSet.getString("revision")).thenReturn(revision);
        when(resultSet.getString("controlstatus")).thenReturn(status);
        when(resultSet.getTimestamp("end")).thenReturn(date);

        List<TestCaseManualExecution> list = testCaseManualExecutionDTO.findTestCaseManualExecution(tCase, "", "", "", "", "");
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());

        TestCaseManualExecution tcme = list.get(1);
        Assert.assertEquals(test, tcme.getTest());
        Assert.assertEquals(testCase, tcme.getTestCase());
        Assert.assertEquals(value, tcme.getValueExpected());
        Assert.assertEquals(howTo, tcme.getHowTo());
        Assert.assertEquals(application, tcme.getApplication());
        Assert.assertEquals(type, tcme.getAppType());
        Assert.assertEquals(url, tcme.getUrl());
        Assert.assertEquals(system, tcme.getSystem());
    }

    /**
     * This test intent to validate that no error occur
     *
     * @throws SQLException
     */
    @Test
    public void testFindTestCaseManualExecutionWhenValuesNull() throws SQLException {
        TCase tCase = new TCase();

        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("test")).thenReturn(null);
        when(resultSet.getString("testcase")).thenReturn(null);
        when(resultSet.getString("behaviororvalueexpected")).thenReturn(null);
        when(resultSet.getString("howto")).thenReturn(null);
        when(resultSet.getString("application")).thenReturn(null);
        when(resultSet.getString("type")).thenReturn(null);
        when(resultSet.getString("url")).thenReturn(null);
        when(resultSet.getString("system")).thenReturn(null);
        when(resultSet.getString("build")).thenReturn(null);
        when(resultSet.getString("revision")).thenReturn(null);
        when(resultSet.getString("controlstatus")).thenReturn(null);
        when(resultSet.getTimestamp("end")).thenReturn(null);

        List<TestCaseManualExecution> list = testCaseManualExecutionDTO.findTestCaseManualExecution(tCase, "", "", "", "", "");
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
    }
}
