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
import org.cerberus.dao.IBuildRevisionParametersDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.BuildRevisionParameters;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BuildRevisionParametersDAO implements IBuildRevisionParametersDAO {

    private static final Logger LOG = Logger.getLogger(BuildRevisionParametersDAO.class);
    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public List<BuildRevisionParameters> findBuildRevisionParametersFromMaxRevision(String build, String revision, String lastBuild, String lastRevision) {
        List<BuildRevisionParameters> list = null;
        String query = "SELECT * from ( "
                + "SELECT Application, max(`Release`) rel "
                + " from buildrevisionparameters "
                + " where build = ? ";

        if (lastBuild.equalsIgnoreCase(build)) {
            query += " and revision > '" + lastRevision + "'";
        }

        query += " and revision <= ?"
                + " and `release` REGEXP '^-?[0-9]+$' "
                + "GROUP BY Application  ORDER BY Application) as al "
                + "JOIN buildrevisionparameters brp "
                + " ON brp.application=al.application and brp.release=al.rel and brp.build = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, build);
                preStat.setString(2, revision);
                preStat.setString(3, build);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<BuildRevisionParameters>();
                    while (resultSet.next()) {
                        list.add(this.loadBuildRevisionParametersFromResultSet(resultSet));
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

    private BuildRevisionParameters loadBuildRevisionParametersFromResultSet(ResultSet rs) throws SQLException {
        BuildRevisionParameters brp = new BuildRevisionParameters();

        brp.setBuild(ParameterParserUtil.parseStringParam(rs.getString("build"), ""));
        brp.setRevision(ParameterParserUtil.parseStringParam(rs.getString("revision"), ""));
        brp.setRelease(ParameterParserUtil.parseStringParam(rs.getString("release"), ""));
        brp.setApplication(ParameterParserUtil.parseStringParam(rs.getString("application"), ""));
        brp.setProject(ParameterParserUtil.parseStringParam(rs.getString("project"), ""));
        brp.setTicketIdFixed(ParameterParserUtil.parseStringParam(rs.getString("ticketidfixed"), ""));
        brp.setBudIdFixed(ParameterParserUtil.parseStringParam(rs.getString("bugidfixed"), ""));
        brp.setLink(ParameterParserUtil.parseStringParam(rs.getString("link"), ""));
        brp.setReleaseOwner(ParameterParserUtil.parseStringParam(rs.getString("releaseowner"), ""));
        brp.setSubject(ParameterParserUtil.parseStringParam(rs.getString("subject"), ""));
        brp.setDateCreation(ParameterParserUtil.parseStringParam(rs.getString("datecre"), ""));
        brp.setJenkinsBuildId(ParameterParserUtil.parseStringParam(rs.getString("jenkinsbuildid"), ""));
        brp.setMavenGroupId(ParameterParserUtil.parseStringParam(rs.getString("mavengroupid"), ""));
        brp.setMavenArtifactId(ParameterParserUtil.parseStringParam(rs.getString("mavenartifactid"), ""));
        brp.setMavenVersion(ParameterParserUtil.parseStringParam(rs.getString("mavenversion"), ""));

        return brp;
    }
}
