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
package org.cerberus.core.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.ITestCaseExecutionSysVerDAO;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.crud.entity.TestCaseExecutionSysVer;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionSysVer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 2.0.0
 */
@Repository
public class TestCaseExecutionSysVerDAO implements ITestCaseExecutionSysVerDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer;

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionSysVerDAO.class);

    private final String OBJECT_NAME = "TestCaseExecution System Version";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public void insertTestCaseExecutionSysVer(TestCaseExecutionSysVer testCaseExecutionSysVer) throws CerberusException {
        final String query = "INSERT INTO testcaseexecutionsysver (id, `system`, build, revision) "
                + "VALUES (?, ?, ?, ?)";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + testCaseExecutionSysVer.getID());
            LOG.debug("SQL.param.system : " + testCaseExecutionSysVer.getSystem());
            LOG.debug("SQL.param.build : " + testCaseExecutionSysVer.getBuild());
            LOG.debug("SQL.param.revision : " + testCaseExecutionSysVer.getRevision());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, testCaseExecutionSysVer.getID());
                preStat.setString(2, testCaseExecutionSysVer.getSystem());
                preStat.setString(3, testCaseExecutionSysVer.getBuild());
                preStat.setString(4, testCaseExecutionSysVer.getRevision());
                preStat.executeUpdate();

            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
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
    public List<TestCaseExecutionSysVer> findTestCaseExecutionSysVerById(long id) {
        List<TestCaseExecutionSysVer> result = null;
        TestCaseExecutionSysVer resultData;
        final String query = "SELECT * FROM testcaseexecutionsysver WHERE id = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));

                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        String system = resultSet.getString("system");
                        String build = resultSet.getString("build");
                        String revision = resultSet.getString("revision");
                        resultData = factoryTestCaseExecutionSysVer.create(id, system, build, revision);
                        result.add(resultData);
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
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
