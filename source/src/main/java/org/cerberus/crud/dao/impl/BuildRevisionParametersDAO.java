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
package org.cerberus.crud.dao.impl;

import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IBuildRevisionParametersDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.BuildRevisionParameters;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.factory.IFactoryBuildRevisionParameters;
import org.cerberus.crud.factory.impl.FactoryBuildRevisionParameters;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

@Repository
public class BuildRevisionParametersDAO implements IBuildRevisionParametersDAO {

    @Autowired
    private IFactoryBuildRevisionParameters factoryBuildRevisionParameters;

    private static final Logger LOG = Logger.getLogger(BuildRevisionParametersDAO.class);

    private final String OBJECT_NAME = "BuildRevisionParameters";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

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
                        list.add(this.loadFromResultSet(resultSet));
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

        String query = "SELECT * FROM buildrevisionparameters WHERE application "
                + "IN (SELECT application FROM application WHERE system = ?) ";
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
                        list.add(this.loadFromResultSet(resultSet));
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

        String query = "SELECT max(build) FROM buildrevisionparameters WHERE application "
                + "IN (SELECT application FROM application WHERE system = ?) "
                + "AND build!='NONE' AND build IS NOT NULL";

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

        String query = "SELECT max(revision) FROM buildrevisionparameters WHERE application "
                + "IN (SELECT application FROM application WHERE system = ?) "
                + "AND build = ?";

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
        String query = "INSERT INTO buildrevisionparameters (`Build`,`Revision`,`Release`,`Link` , `Application`, "
                + "`releaseOwner`, `Project`, `BugIDFixed`, `TicketIDFixed` , `Subject`) VALUES (?,?,?,?,?,?,?,?,?,?)";

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
                preStat.setString(8, brp.getBugIdFixed());
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
        final String query = "UPDATE buildrevisionparameters SET build = ?, revision = ?, application = ?,"
                + "`release` = ?, project = ?, ticketidfixed = ?, bugidfixed = ?, `subject` = ?, releaseowner = ?,"
                + "link = ? WHERE id = ?";

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
                preStat.setString(7, brp.getBugIdFixed());
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
                        brp = this.loadFromResultSet(resultSet);
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

    @Override
    public AnswerItem readByKeyTech(int id) {
        AnswerItem ans = new AnswerItem();
        BuildRevisionParameters result = null;
        final String query = "SELECT * FROM `buildrevisionparameters` WHERE `id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, id);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerItem readLastBySystem(String system) {
        AnswerItem ans = new AnswerItem();
        BuildRevisionParameters result = null;
        StringBuilder query = new StringBuilder();

        query.append("SELECT * from buildrevisionparameters brp ");
        query.append("	left outer join buildrevisioninvariant bri1 on brp.build = bri1.versionname and bri1.level=1 and bri1.`System` = ? ");
        query.append("	left outer join buildrevisioninvariant bri2 on brp.revision = bri2.versionname and bri2.level=2 and bri2.`System` = ? ");
        query.append("WHERE 1=1 ");
        query.append("	and application in (SELECT application FROM application WHERE `System` = ? ) ");
        query.append("ORDER BY bri1.seq desc, bri2.seq desc, brp.datecre desc limit 1; ");

        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, system);
                preStat.setString(2, system);
                preStat.setString(3, system);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT LAST"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList readByVarious1ByCriteria(String system, String application, String build, String revision, int start, int amount,
            String column, String dir, String searchTerm, String individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<BuildRevisionParameters> brpList = new ArrayList<BuildRevisionParameters>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM buildrevisionparameters ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`id` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `Build` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `Revision` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `Release` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `Application` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `Project` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `TicketIDFixed` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `BugIDFixed` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `Link` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `ReleaseOwner` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `datecre` like '%").append(searchTerm).append("%')");
            searchSQL.append(" or `jenkinsbuildid` like '%").append(searchTerm).append("%')");
            searchSQL.append(" or `mavengroupid` like '%").append(searchTerm).append("%')");
            searchSQL.append(" or `mavenartifactid` like '%").append(searchTerm).append("%')");
            searchSQL.append(" or `mavenversion` like '%").append(searchTerm).append("%')");
        }
        if (!StringUtil.isNullOrEmpty(individualSearch)) {
            searchSQL.append(" and (`").append(individualSearch).append("`)");
        }
        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" and application in (SELECT application FROM application WHERE `System` = '").append(system).append("' )");
        }
        if (!StringUtil.isNullOrEmpty(application)) {
            searchSQL.append(" and (`Application`='").append(application).append("' )");
        }
        if (!StringUtil.isNullOrEmpty(build)) {
            searchSQL.append(" and (`Build`='").append(build).append("' )");
        }
        if (!StringUtil.isNullOrEmpty(revision)) {
            searchSQL.append(" and (`Revision`='").append(revision).append("' )");
        }
        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        brpList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (brpList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(brpList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(brpList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        response.setDataList(brpList);
        return response;
    }

    @Override
    public Answer create(BuildRevisionParameters brp) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO buildrevisionparameters (`Build`,`Revision`,`Release`,`Link` , `Application`, `releaseOwner`, `Project`");
        query.append(" , `BugIDFixed`, `TicketIDFixed` , `Subject`, `jenkinsbuildid`, `mavengroupid`, `mavenartifactid`, `mavenversion`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, brp.getBuild());
                preStat.setString(2, brp.getRevision());
                preStat.setString(3, brp.getRelease());
                preStat.setString(4, brp.getLink());
                preStat.setString(5, brp.getApplication());
                preStat.setString(6, brp.getReleaseOwner());
                preStat.setString(7, brp.getProject());
                preStat.setString(8, brp.getBugIdFixed());
                preStat.setString(9, brp.getTicketIdFixed());
                preStat.setString(10, brp.getSubject());
                preStat.setString(11, brp.getJenkinsBuildId());
                preStat.setString(12, brp.getMavenGroupId());
                preStat.setString(13, brp.getMavenArtifactId());
                preStat.setString(14, brp.getMavenVersion());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(BuildRevisionParameters brp) {
        MessageEvent msg = null;
        final String query = "DELETE FROM buildrevisionparameters WHERE id = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, brp.getId());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(BuildRevisionParameters brp) {
        MessageEvent msg = null;
        final String query = "UPDATE buildrevisionparameters SET build = ?, revision = ?, application = ?,"
                + "`release` = ?, project = ?, ticketidfixed = ?, bugidfixed = ?, `subject` = ?, releaseowner = ?,"
                + " link = ?, jenkinsbuildid = ?, mavengroupid = ?, mavenartifactid = ?, mavenversion = ? "
                + "  WHERE id = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
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
                preStat.setString(7, brp.getBugIdFixed());
                preStat.setString(8, brp.getSubject());
                preStat.setString(9, brp.getReleaseOwner());
                preStat.setString(10, brp.getLink());
                preStat.setString(11, brp.getJenkinsBuildId());
                preStat.setString(12, brp.getMavenGroupId());
                preStat.setString(13, brp.getMavenArtifactId());
                preStat.setString(14, brp.getMavenVersion());
                preStat.setInt(15, brp.getId());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public BuildRevisionParameters loadFromResultSet(ResultSet rs) throws SQLException {
        int iD = rs.getInt("ID");
        String build = ParameterParserUtil.parseStringParam(rs.getString("build"), "");
        String revision = ParameterParserUtil.parseStringParam(rs.getString("revision"), "");
        String release = ParameterParserUtil.parseStringParam(rs.getString("release"), "");
        String application = ParameterParserUtil.parseStringParam(rs.getString("application"), "");
        String project = ParameterParserUtil.parseStringParam(rs.getString("project"), "");
        String ticketIdFixed = ParameterParserUtil.parseStringParam(rs.getString("ticketidfixed"), "");
        String budIdFixed = ParameterParserUtil.parseStringParam(rs.getString("bugidfixed"), "");
        String link = ParameterParserUtil.parseStringParam(rs.getString("link"), "");
        String releaseOwner = ParameterParserUtil.parseStringParam(rs.getString("releaseowner"), "");
        String subject = ParameterParserUtil.parseStringParam(rs.getString("subject"), "");
        Timestamp dateCreation = rs.getTimestamp("datecre");
        String jenkinsBuildId = ParameterParserUtil.parseStringParam(rs.getString("jenkinsbuildid"), "");
        String mavenGroupId = ParameterParserUtil.parseStringParam(rs.getString("mavengroupid"), "");
        String mavenArtifactId = ParameterParserUtil.parseStringParam(rs.getString("mavenartifactid"), "");
        String mavenVersion = ParameterParserUtil.parseStringParam(rs.getString("mavenversion"), "");

        factoryBuildRevisionParameters = new FactoryBuildRevisionParameters();
        return factoryBuildRevisionParameters.create(iD, build, revision, release, application, project, ticketIdFixed, budIdFixed, link, releaseOwner, subject, dateCreation, jenkinsBuildId, mavenGroupId, mavenArtifactId, mavenVersion);
    }

}
