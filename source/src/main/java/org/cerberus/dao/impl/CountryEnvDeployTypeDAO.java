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

package org.cerberus.dao.impl;

import org.apache.log4j.Logger;
import org.cerberus.dao.ICountryEnvDeployTypeDAO;
import org.cerberus.database.DatabaseSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CountryEnvDeployTypeDAO implements ICountryEnvDeployTypeDAO {

    private static final Logger LOG = Logger.getLogger(CountryEnvDeployTypeDAO.class);

    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public List<String> findJenkinsAgentByKey(String system, String country, String env, String deploy) {
        List<String> list = null;
        final String query = "SELECT jenkinsagent "
                + " FROM countryenvdeploytype "
                + " WHERE country = ?"
                + " AND `system` = ?"
                + " AND environment = ?"
                + " AND deploytype = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, country);
                preStat.setString(2, system);
                preStat.setString(3, env);
                preStat.setString(4, deploy);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();
                    while (resultSet.next()) {
                        list.add(resultSet.getString("jenkinsagent"));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception);
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception);
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return list;
    }
}
