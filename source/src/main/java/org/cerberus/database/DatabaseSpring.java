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
package org.cerberus.database;

import org.cerberus.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database class, allow to get Connections defined on glassfish.
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
@Repository
public class DatabaseSpring {

    /**
     * Object autowired by Spring, linked to glassfish for getting connection.
     */
    @Autowired
    private DataSource dataSource;

    /**
     * Create connection.
     * <p/>
     * If the connection doesn't exist, one will be created through the
     * DataSource object. If the connection exists, is reused without the
     * necessity of creating another. Then update the status.
     *
     * @return Connection Object with the connection created by DataSource
     *         object.
     */
    public Connection connect() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException exception) {
            MyLogger.log(DatabaseSpring.class.getName(), Level.ERROR, "Cannot connect to datasource jdbc/cerberus" + System.getProperty("org.cerberus.environment") + " : " + exception.toString());
        }

        return null;
    }

    public Connection connect(final String connection) {
        try {
            InitialContext ic = new InitialContext();
            MyLogger.log(DatabaseSpring.class.getName(), Level.INFO, "connecting to jdbc/" + connection);
            return ((DataSource) ic.lookup("jdbc/" + connection)).getConnection();
        } catch (SQLException ex) {
            MyLogger.log(DatabaseSpring.class.getName(), Level.ERROR, ex.toString());
        } catch (NamingException ex) {
            MyLogger.log(DatabaseSpring.class.getName(), Level.FATAL, ex.toString());
        }
        return null;
    }
}
