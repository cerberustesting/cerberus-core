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
import org.cerberus.core.config.cerberus.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusEventException;

/**
 * Database class, allow to get Connections defined on glassfish.
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
@Repository
public class DatabaseSpring {

    private static final Logger LOG = LogManager.getLogger(DatabaseSpring.class);
    // Object autowired by Spring, linked to glassfish for getting connection.
    @Autowired
    private DataSource dataSource;
    private boolean onTransaction = false;
    private Connection conn;

    /**
     * Create connection.
     * <p/>
     * If the connection doesn't exist, one will be created through the
     * DataSource object. If the connection exists, is reused without the
     * necessity of creating another. Then update the status.
     *
     * @return Connection Object with the connection created by DataSource
     * object.
     */
    public Connection connect() {
        try {
            if (onTransaction) { //if the connection is in a transaction, it will return the current connection
                return this.conn;
            }
            return this.dataSource.getConnection();
        } catch (SQLException exception) {
            LOG.warn("Cannot connect to datasource jdbc/cerberus{} : {}", System.getProperty(Property.ENVIRONMENT), exception.toString());
        }

        return null;
    }

    public void closeConnection() {
        //if the connection is in a transaction, it will not be close, it 
        //will be closed when the user calls the endTransaction
        if (onTransaction) {
            try {
                //automatically commits the changes
                this.conn.commit();
            } catch (SQLException ex) {
                LOG.warn("Exception closing connection : {}", ex.toString(), ex);
            }
        }
        if (this.conn != null) {
            try {
                this.conn.close();
            } catch (SQLException ex) {
                LOG.warn("Can't end/close the connection to datasource jdbc/cerberus{} : {}", System.getProperty(Property.ENVIRONMENT), ex.toString());
            }
        }

    }

    public void beginTransaction() {
        onTransaction = true;
        try {
            this.conn = this.dataSource.getConnection();
            this.conn.setAutoCommit(false);
        } catch (SQLException exception) {
            LOG.warn("Cannot connect to datasource jdbc/cerberus{} : {}", System.getProperty(Property.ENVIRONMENT), exception.toString());
        }
    }

    private void endTransaction(boolean success) {
        onTransaction = false;
        try {
            if (success) {
                this.conn.commit();
            } else {
                this.conn.rollback();
            }

            this.conn.close();

        } catch (SQLException ex) {
            LOG.warn("Can't end/close the connection to datasource jdbc/cerberus{} : {}", System.getProperty(Property.ENVIRONMENT), ex.toString());
        }
    }

    public void commitTransaction() {
        endTransaction(true);

    }

    public void abortTransaction() {
        endTransaction(false);
    }

    public Connection connect(final String connection) throws CerberusEventException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_GENERIC);

        try {
            InitialContext ic = new InitialContext();
            String conName = "jdbc/" + connection;
            LOG.info("connecting to '{}'", conName);
            DataSource ds = (DataSource) ic.lookup(conName);
            return ds.getConnection();

        } catch (SQLException ex) {
            LOG.warn(ex.toString(), ex);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL);
            msg
                    .resolveDescription("JDBC", "jdbc/" + connection)
                    .resolveDescription("ERROR", ex.toString());
            throw new CerberusEventException(msg);

        } catch (NamingException ex) {
            InitialContext ic;
            try {
                ic = new InitialContext();
                String conName = "java:/comp/env/jdbc/" + connection;
                LOG.info("failed with '" + "jdbc/" + connection + "' --> connecting to '{}'", conName);
                DataSource ds = (DataSource) ic.lookup(conName);
                return ds.getConnection();

            } catch (NamingException | SQLException ex1) {

                LOG.warn("failed connection with 'java:/comp/env/jdbc/" + connection + "'", ex1.toString());
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL);
                msg
                        .resolveDescription("JDBC", "java:/comp/env/jdbc/" + connection)
                        .resolveDescription("ERROR", ex1.toString());
                throw new CerberusEventException(msg);
            }
        }
    }

    public boolean isOnTransaction() {
        return onTransaction;
    }
}
