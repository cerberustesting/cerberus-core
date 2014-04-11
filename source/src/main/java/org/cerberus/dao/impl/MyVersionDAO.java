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
package org.cerberus.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.cerberus.dao.IMyVersionDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MyVersion;
import org.cerberus.factory.IFactoryMyversion;
import org.cerberus.factory.impl.FactoryMyversion;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Benoit Dumont
 * @version 1.0, 09/06/2013
 * @since 2.0.0
 */
@Repository
public class MyVersionDAO implements IMyVersionDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryMyversion factoryMyversion;

    @Override
    public MyVersion findMyVersionByKey(String key) {
        MyVersion result = new MyVersion();
        final String query = "SELECT mv.value FROM myversion mv WHERE mv.`key` = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, key);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        factoryMyversion = new FactoryMyversion();
                        result = factoryMyversion.create(key, Integer.valueOf(resultSet.getString("value")));
                    }
                } catch (SQLException exception) {
                    result = null;
                    MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                result = null;
                MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(MyVersionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public boolean updateMyVersion(MyVersion myVersion) {
        boolean result = false;
        final String query = "UPDATE myversion SET value = ? WHERE `key` = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, myVersion.getValue());
                preStat.setString(2, myVersion.getKey());

                result = preStat.execute();
            } catch (SQLException exception) {
                MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(MyVersionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }
}
