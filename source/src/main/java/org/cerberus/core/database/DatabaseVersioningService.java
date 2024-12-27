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
package org.cerberus.core.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.MyVersion;
import org.cerberus.core.crud.service.IMyVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * @author vertigo
 */
@Service
public class DatabaseVersioningService implements IDatabaseVersioningService {

    private static final Logger LOG = LogManager.getLogger(DatabaseVersioningService.class);
    private int sqlVersion;

    @Autowired
    private IMyVersionService myVersionService;
    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public String exeSQL(String sqlString) {
        LOG.info("Starting Execution of '{}'", sqlString);

        try (Connection connection = this.databaseSpring.connect();
             Statement preStat = connection.createStatement()) {
            preStat.execute(sqlString);
            LOG.info("'{}'  Executed successfully.", sqlString);
        } catch (Exception exception1) {
            LOG.error(exception1.toString(), exception1);
            return exception1.toString();
        }
        return "OK";
    }

    @Override
    public boolean isDatabaseUpToDate() {
        // Get version from the database
        MyVersion myVersion;
        myVersion = myVersionService.findMyVersionByKey("database");
        if (myVersion != null) {
            // compare both to see if version is uptodate.
            if (getSqlVersion() == myVersion.getValue()) {
                return true;
            }
            LOG.info("Database needs an upgrade - Script : {} Database : {}", getSqlVersion(), myVersion.getValue());
        }
        return false;
    }

    @Override
    public int getSqlVersion() {
        if (sqlVersion == 0) {
            getSQLScriptFromFile();
        }
        return sqlVersion;
    }

    @Override
    public ArrayList<String> getSQLScriptFromFile() {
        ArrayList<String> result = new ArrayList<>();
        File file = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource("database.sql");
            if (resource == null) {
                LOG.error("file not found");
                return result;
            }

            file = new File(resource.toURI());

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                StringBuilder sqlLine = new StringBuilder();
                while (line != null) {
                    if (!((line.startsWith("--")) || line.isEmpty())) {
                        // Line is not empty and does not start with --
                        if (line.startsWith(" ")) {
                            sqlLine.append(line);
                        } else {
                            // This is a new SQL Instruction;
                            if (sqlLine.length() > 0) {
                                result.add(sqlLine.toString());
                            }
                            sqlLine = new StringBuilder(line);
                        }
                    }
                    // read next line
                    line = reader.readLine();
                }
                if (sqlLine.length() > 0) {
                    result.add(sqlLine.toString());
                }
            } catch (IOException e) {
                LOG.error(e, e);
            }
        } catch (Exception ex) {
            LOG.error(ex, ex);
        }
        if (!result.isEmpty()) {
            sqlVersion = result.size();
        }
        return result;
    }

}
