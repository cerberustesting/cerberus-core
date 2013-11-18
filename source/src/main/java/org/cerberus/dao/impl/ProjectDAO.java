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

import org.cerberus.dao.IProjectDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Project;
import org.cerberus.factory.IFactoryProject;
import org.cerberus.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bcivel
 */
@Repository
public class ProjectDAO implements IProjectDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryProject factoryProject;

    @Override
    public Project findProjectByKey(String project) {
        Project result = null;
        String idProject;
        String vcCode;
        String description;
        final String query = "SELECT * FROM project WHERE idproject = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, project);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        idProject = resultSet.getString("idproject") == null ? "" : resultSet.getString("idproject");
                        vcCode = resultSet.getString("VCCode") == null ? "" : resultSet.getString("VCCode");
                        description = resultSet.getString("Description") == null ? "" : resultSet.getString("Description");
                        String active = resultSet.getString("active") == null ? "" : resultSet.getString("active");
                        String dateCreation = resultSet.getString("dateCreation") == null ? "" : resultSet.getString("dateCreation");
                        result = factoryProject.create(idProject, vcCode, description, active, dateCreation);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ProjectDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return result;
    }

    @Override
    public List<Project> findAllProject() {
        List<Project> result = null;
        String idProject;
        String vcCode;
        String description;
        final String query = "SELECT * FROM project ORDER BY idproject";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<Project>();

                    while (resultSet.next()) {
                        idProject = resultSet.getString("idproject") == null ? "" : resultSet.getString("idproject");
                        vcCode = resultSet.getString("VCCode") == null ? "" : resultSet.getString("VCCode");
                        description = resultSet.getString("Description") == null ? "" : resultSet.getString("Description");
                        String active = resultSet.getString("active") == null ? "" : resultSet.getString("active");
                        String dateCreation = resultSet.getString("datecre") == null ? "" : resultSet.getString("datecre");
                        result.add(factoryProject.create(idProject, vcCode, description, active, dateCreation));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ProjectDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return result;
    }


}
