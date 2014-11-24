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
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
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

    @Override
    public List<BuildRevisionParameters> findBuildRevisionParametersByCriteria(String system, String build, String revision) {
        List<BuildRevisionParameters> list = null;

        String query = "SELECT * FROM buildrevisionparameters WHERE application " +
                "IN (SELECT application FROM application WHERE system = ?) ";
        if (!StringUtil.isNullOrEmpty(build)) {
            query += " AND build = ? ";
        }
        if (!StringUtil.isNullOrEmpty(revision)) {
            query += " AND revision = ? ";
        }
        query += "ORDER BY build DESC, revision DESC, application ASC, `release` ASC";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                int i = 2;
                if (!StringUtil.isNullOrEmpty(build)) {
                    preStat.setString(i, build);
                    i++;
                }
                if (!StringUtil.isNullOrEmpty(revision)) {
                    preStat.setString(i, revision);
                }

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

    @Override
    public String getMaxBuildBySystem(String system) {
        String build = null;

        String query = "SELECT max(build) FROM buildrevisionparameters WHERE application " +
                "IN (SELECT application FROM application WHERE system = ?) " +
                "AND build!='NONE' AND build IS NOT NULL";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        build = resultSet.getString(1);
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
        return build;
    }

    @Override
    public String getMaxRevisionBySystemAndBuild(String system, String build) {
        String revision = null;

        String query = "SELECT max(revision) FROM buildrevisionparameters WHERE application " +
                "IN (SELECT application FROM application WHERE system = ?) " +
                "AND build = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                preStat.setString(2, build);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        revision = resultSet.getString(1);
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
        return revision;
    }

    @Override
    public void insertBuildRevisionParameters(BuildRevisionParameters brp) {
        String query = "INSERT INTO buildrevisionparameters (`Build`,`Revision`,`Release`,`Link` , `Application`, " +
                "`releaseOwner`, `Project`, `BugIDFixed`, `TicketIDFixed` , `Subject`) VALUES (?,?,?,?,?,?,?,?,?,?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                preStat.setString(1, brp.getBuild());
                preStat.setString(2, brp.getRevision());
                preStat.setString(3, brp.getRelease());
                preStat.setString(4, brp.getLink());
                preStat.setString(5, brp.getApplication());
                preStat.setString(6, brp.getReleaseOwner());
                preStat.setString(7, brp.getProject());
                preStat.setString(8, brp.getBudIdFixed());
                preStat.setString(9, brp.getTicketIdFixed());
                preStat.setString(10, brp.getSubject());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        brp.setId(resultSet.getInt(1));
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
    }

    @Override
    public boolean deleteBuildRevisionParameters(int id) {
        boolean bool = false;
        final String query = "DELETE FROM buildrevisionparameters WHERE id = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, id);

                bool = preStat.executeUpdate() > 0;
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
        return bool;
    }

    @Override
    public boolean updateBuildRevisionParameters(BuildRevisionParameters brp) {
        boolean bool = false;
        final String query = "UPDATE buildrevisionparameters SET build = ?, revision = ?, application = ?," +
                "`release` = ?, project = ?, ticketidfixed = ?, bugidfixed = ?, `subject` = ?, releaseowner = ?," +
                "link = ? WHERE id = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, brp.getBuild());
                preStat.setString(2, brp.getRevision());
                preStat.setString(3, brp.getApplication());
                preStat.setString(4, brp.getRelease());
                preStat.setString(5, brp.getProject());
                preStat.setString(6, brp.getTicketIdFixed());
                preStat.setString(7, brp.getBudIdFixed());
                preStat.setString(8, brp.getSubject());
                preStat.setString(9, brp.getReleaseOwner());
                preStat.setString(10, brp.getLink());
                preStat.setInt(11, brp.getId());

                bool = preStat.executeUpdate() > 0;
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
        return bool;
    }

    @Override
    public BuildRevisionParameters findBuildRevisionParametersByKey(int id) {
        BuildRevisionParameters brp = null;

        String query = "SELECT * FROM buildrevisionparameters WHERE id = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, id);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        brp = this.loadBuildRevisionParametersFromResultSet(resultSet);
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
        return brp;
    }

    private BuildRevisionParameters loadBuildRevisionParametersFromResultSet(ResultSet rs) throws SQLException {
        BuildRevisionParameters brp = new BuildRevisionParameters();

        brp.setId(ParameterParserUtil.parseIntegerParam(rs.getString("ID"), -1));
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
