/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author bcivel
 */
@Repository
public class EnvironmentStatisticsDAOImpl implements IEnvironmentStatisticsDAO {

    @Autowired
    private DatabaseSpring databaseSpring;

    private static final Logger LOG = LogManager.getLogger(EnvironmentStatisticsDAOImpl.class);

    private final String OBJECT_NAME = "Environment Statistics";
    private final int MAX_ROW_SELECTED = 1000;

    @Override
    public AnswerList<BuildRevisionStatisticsEnv> getEnvironmentStatistics(List<String> system) {
        AnswerList<BuildRevisionStatisticsEnv> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<BuildRevisionStatisticsEnv> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT distinct c.system, c.Build, c.Revision, PROD.cnt PROD, UAT.cnt UAT, QA.cnt QA, DEV.cnt DEV, isys.sort, c.system, bri1.seq, bri2.seq ");
        query.append("FROM `countryenvparam` c ");
        query.append("  JOIN invariant isys ON isys.value=c.system and isys.idname='SYSTEM' ");
        query.append("  LEFT OUTER JOIN ( SELECT Build, Revision, count(*) cnt from countryenvparam ");
        query.append("    JOIN invariant i ON i.value=Environment and i.idname='ENVIRONMENT' where gp1='PROD' and build is not null and build<>'' and Active='Y' and ");
        query.append(SqlUtil.generateInClause("`System`", system));
        query.append("    GROUP BY Build, Revision) as PROD on PROD.Build=c.Build and PROD.Revision=c.Revision ");
        query.append("  LEFT OUTER JOIN ( select Build, Revision, count(*) cnt from countryenvparam ");
        query.append("    JOIN invariant i ON i.value=Environment and i.idname='ENVIRONMENT' where gp1='UAT' and build is not null and build<>'' and Active='Y' and ");
        query.append(SqlUtil.generateInClause("`System`", system));
        query.append("    GROUP BY Build, Revision) as UAT on UAT.Build=c.Build and UAT.Revision=c.Revision ");
        query.append("  LEFT OUTER JOIN ( select Build, Revision, count(*) cnt from countryenvparam ");
        query.append("    JOIN invariant i ON i.value=Environment and i.idname='ENVIRONMENT' where gp1='QA' and build is not null and build<>'' and Active='Y' and ");
        query.append(SqlUtil.generateInClause("`System`", system));
        query.append("    GROUP BY Build, Revision) as QA on QA.Build=c.Build and QA.Revision=c.Revision ");
        query.append("  LEFT OUTER JOIN ( select Build, Revision, count(*) cnt from countryenvparam ");
        query.append("    JOIN invariant i ON i.value=Environment and i.idname='ENVIRONMENT' where gp1='DEV' and build is not null and build<>'' and Active='Y' and ");
        query.append(SqlUtil.generateInClause("`System`", system));
        query.append("    GROUP BY Build, Revision) as DEV on DEV.Build=c.Build and DEV.Revision=c.Revision ");
        query.append("	left outer join buildrevisioninvariant bri1 on c.build = bri1.versionname and bri1.level=1 and bri1.`System`=c.`System` ");
        query.append("	left outer join buildrevisioninvariant bri2 on c.revision = bri2.versionname and bri2.level=2  and bri2.`System`=c.`System` ");
        query.append("WHERE c.build is not null and c.build not in ('','NA') and Active='Y' and ");
        query.append(SqlUtil.generateInClause("c.`System`", system));
        query.append("ORDER BY  isys.sort asc, c.`system` asc, bri1.seq asc, bri2.seq asc;");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                for (int j = 0; j < 5; j++) {
                    for (String string : system) {
                        preStat.setString(i++, string);
                    }
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else if (objectList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(objectList, nrTotalRows);
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
        response.setDataList(objectList);
        return response;
    }

    private BuildRevisionStatisticsEnv loadFromResultSet(ResultSet rs) throws SQLException {
        BuildRevisionStatisticsEnv newBRStat = new BuildRevisionStatisticsEnv();
        newBRStat.setSystem(ParameterParserUtil.parseStringParam(rs.getString("system"), ""));
        newBRStat.setBuild(ParameterParserUtil.parseStringParam(rs.getString("build"), ""));
        newBRStat.setRevision(ParameterParserUtil.parseStringParam(rs.getString("revision"), ""));
        newBRStat.setNbEnvDEV(ParameterParserUtil.parseIntegerParam(rs.getString("DEV"), 0));
        newBRStat.setNbEnvQA(ParameterParserUtil.parseIntegerParam(rs.getString("QA"), 0));
        newBRStat.setNbEnvUAT(ParameterParserUtil.parseIntegerParam(rs.getString("UAT"), 0));
        newBRStat.setNbEnvPROD(ParameterParserUtil.parseIntegerParam(rs.getString("PROD"), 0));
        return newBRStat;
    }

}
