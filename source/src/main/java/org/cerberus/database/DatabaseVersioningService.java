/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.MyVersion;
import org.cerberus.crud.service.IMyVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class DatabaseVersioningService implements IDatabaseVersioningService {

    private static final Logger LOG = LogManager.getLogger(DatabaseVersioningService.class);
    private int sqlVersion;

    @Autowired
    private IMyVersionService MyversionService;
    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public String exeSQL(String SQLString) {
        LOG.info("Starting Execution of '" + SQLString + "'");

        try (Connection connection = this.databaseSpring.connect();
                Statement preStat = connection.createStatement();) {
            preStat.execute(SQLString);
            LOG.info("'" + SQLString + "' Executed successfully.");
        } catch (Exception exception1) {
            LOG.error(exception1.toString(), exception1);
            return exception1.toString();
        }
        return "OK";
    }

    @Override
    public boolean isDatabaseUptodate() {
        // Get version from the database
        MyVersion MVersion;
        MVersion = MyversionService.findMyVersionByKey("database");
        if (MVersion != null) {
            // compare both to see if version is uptodate.
            if (getSqlVersion() == MVersion.getValue()) {
                return true;
            }
            LOG.info("Database needs an upgrade - Script : " + getSqlVersion() + " Database : " + MVersion.getValue());
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
            } else {
                file = new File(resource.toURI());

                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file));
                    String line = reader.readLine();
                    String sqlLine = "";
                    while (line != null) {
                        if (!((line.startsWith("--")) || line.isEmpty())) {
                            // Line is not empty and does not start with --
                            if (line.startsWith(" ")) {
                                sqlLine += line;
                            } else {
                                // This is a new SQL Instruction;
                                if (!sqlLine.isEmpty()) {
                                    result.add(sqlLine);
                                }
                                sqlLine = line;
                            }
                        }
                        // read next line
                        line = reader.readLine();
                    }
                    if (!sqlLine.isEmpty()) {
                        result.add(sqlLine);
                    }
                    reader.close();
                } catch (IOException e) {
                    LOG.error(e, e);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }

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
