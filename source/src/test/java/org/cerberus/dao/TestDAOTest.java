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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.dao;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cerberus.dao.impl.TestDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.factory.impl.FactoryTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

/**
 * {Insert class description here}
 *
 * @author Benoit CIVEL
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class TestDAOTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement statement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private DatabaseSpring databaseSpring;
    @Mock
    private FactoryTest factoryTest;
    @InjectMocks
    private TestDAO testDAO;

    @Test
    public void testCanFindAllTest() throws SQLException {
        List<org.cerberus.entity.Test> listOfTest;
        String test = "Test";
        String description = "Test Description";
        String active = "Y";
        String automated = "Y";

        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("test")).thenReturn(test);
        when(resultSet.getString("description")).thenReturn(description);
        when(resultSet.getString("active")).thenReturn(active);
        when(resultSet.getString("automated")).thenReturn(automated);

        listOfTest = testDAO.findAllTest();

        assertTrue(listOfTest.size() > 0);

    }
}
