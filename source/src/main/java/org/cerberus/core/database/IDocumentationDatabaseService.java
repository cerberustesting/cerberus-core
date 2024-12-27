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

import java.util.ArrayList;

/**
 * @author ryltar
 */
public interface IDocumentationDatabaseService {

    /**
     * @param sqlString String that contains the SQL that will be executed
     *                  against the Cerberus database.
     * @return "OK" if the SQL executed correctly and a string with the error
     * when not executed.
     */
    String exeSQL(String sqlString);

    /**
     * This method call exeSQL with documentation script right after
     * DocumentationDatabaseService singleton is created
     */
    void init();

    /**
     * @return an array of string that contain all the SQL instructions to
     * refresh documentation table
     */
    ArrayList<String> getSqlDocumentation();
}
