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

package org.cerberus.dao;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.cerberus.dao.impl.UserDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.User;
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
 * @version 1.0, 04/07/2013
 * @since 0.9.0
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class UserDAOTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement statement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private DatabaseSpring databaseSpring;
    @InjectMocks
    private UserDAO userDAO;

    @Test
    public void testInsertUser() throws SQLException {
        int id = 99999;

        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        when(statement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.first()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(id);

        User user = new User();

        boolean bool = userDAO.insertUser(user);

        Assert.assertEquals(true, bool);
        Assert.assertEquals(id, user.getUserID());
    }

    @Test
    public void testInsertUserWhenFailToInsert() throws SQLException {
        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);
        when(statement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.first()).thenReturn(false);

        User user = new User();

        boolean bool = userDAO.insertUser(user);

        Assert.assertEquals(false, bool);
        Assert.assertEquals(new User(), user);
    }

    @Test
    public void testDeleteUser() throws SQLException {
        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        User user = new User();

        boolean bool = userDAO.deleteUser(user);

        Assert.assertEquals(true, bool);
    }

    @Test
    public void testDeleteUserWhenFailToDelete() throws SQLException {
        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);

        User user = new User();

        boolean bool = userDAO.deleteUser(user);

        Assert.assertEquals(false, bool);
    }

    @Test
    public void testUpdateUser() throws SQLException {
        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        User user = new User();

        boolean bool = userDAO.updateUser(user);

        Assert.assertEquals(true, bool);
    }

    @Test
    public void testUpdateUserWhenFailToUpdate() throws SQLException {
        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);

        User user = new User();

        boolean bool = userDAO.updateUser(user);

        Assert.assertEquals(false, bool);
    }
}
