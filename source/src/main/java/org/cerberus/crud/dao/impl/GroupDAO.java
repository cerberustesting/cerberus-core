/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IGroupDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.UserGroup;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.factory.IFactoryGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GroupDAO implements IGroupDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryGroup factoryGroup;
    
    private static final Logger LOG = Logger.getLogger(GroupDAO.class);
    
    @Override
    public List<UserGroup> findGroupByUser(User user) {
        List<UserGroup> result = null;
        final String query = "SELECT * FROM `usergroup` u WHERE u.login = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, user.getLogin());

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<UserGroup>();
                    while (resultSet.next()) {
                        UserGroup group = factoryGroup.create(resultSet.getString("login"), resultSet.getString("groupName"));
                        result.add(group);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return result;
    }
}
