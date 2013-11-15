package org.cerberus.dao;

import org.cerberus.dao.impl.UserDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.User;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.*;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 04/07/2013
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
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
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(1);
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
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(0);
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
        when(statement.executeUpdate(anyString())).thenReturn(1);

        User user = new User();

        boolean bool = userDAO.deleteUser(user);

        Assert.assertEquals(true, bool);
    }

    @Test
    public void testDeleteUserWhenFailToDelete() throws SQLException {
        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate(anyString())).thenReturn(0);

        User user = new User();

        boolean bool = userDAO.deleteUser(user);

        Assert.assertEquals(false, bool);
    }

    @Test
    public void testUpdateUser() throws SQLException {
        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate(anyString())).thenReturn(1);

        User user = new User();

        boolean bool = userDAO.updateUser(user);

        Assert.assertEquals(true, bool);
    }

    @Test
    public void testUpdateUserWhenFailToUpdate() throws SQLException {
        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate(anyString())).thenReturn(0);

        User user = new User();

        boolean bool = userDAO.updateUser(user);

        Assert.assertEquals(false, bool);
    }
}
