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
package org.cerberus.core.database.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.database.dao.ICerberusInformationDAO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@Repository
public class CerberusInformationDAO implements ICerberusInformationDAO {

    @Autowired
    private DatabaseSpring databaseSpring;

    private static final Logger LOG = LogManager.getLogger(CerberusInformationDAO.class);

    @Override
    public AnswerItem<HashMap<String, String>> getDatabaseInformation() {
        AnswerItem<HashMap<String, String>> ans = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        HashMap<String, String> cerberusInformation = new HashMap<>();


        try (Connection connection = this.databaseSpring.connect()) {

            DatabaseMetaData metaData = connection.getMetaData();

            cerberusInformation.put("DatabaseProductName", metaData.getDatabaseProductName());
            cerberusInformation.put("DatabaseProductVersion", metaData.getDatabaseProductVersion());
            cerberusInformation.put("DatabaseMajorVersion", Integer.toString(metaData.getDatabaseMajorVersion()));
            cerberusInformation.put("DatabaseMinorVersion", Integer.toString(metaData.getDatabaseMinorVersion()));

            cerberusInformation.put("DriverName", metaData.getDriverName());
            cerberusInformation.put("DriverVersion", metaData.getDriverVersion());
            cerberusInformation.put("DriverMajorVersion", Integer.toString(metaData.getDriverMajorVersion()));
            cerberusInformation.put("DriverMinorVersion", Integer.toString(metaData.getDriverMinorVersion()));

            cerberusInformation.put("JDBCMajorVersion", Integer.toString(metaData.getJDBCMajorVersion()));
            cerberusInformation.put("JDBCMinorVersion", Integer.toString(metaData.getJDBCMinorVersion()));

            ans.setItem(cerberusInformation);

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString(), exception);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }
}
