package com.redcats.tst.database;

import com.redcats.tst.log.MyLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
     * Connection created.
     */
    private Connection connection;
    /**
     * Status of connection.
     */
    private boolean connected;
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
        if (!this.connected) {
            try {
                this.connection = this.dataSource.getConnection();
//                MyLogger.log(DatabaseSpring.class.getName(), Level.INFO, "Connecting to datasource : jdbc/cerberus" +  System.getProperty("org.cerberus.environment"));
                this.connected = true;
            } catch (SQLException exception) {
                MyLogger.log(DatabaseSpring.class.getName(), Level.ERROR, "Cannot connect to datasource jdbc/cerberus" +  System.getProperty("org.cerberus.environment") + " : " + exception.toString());
                return null;
            }
        }
        return this.connection;
    }

    public Connection connect(final String connection) {
        try {
            if (!this.connected) {
                InitialContext ic = new InitialContext();
                MyLogger.log(DatabaseSpring.class.getName(), Level.INFO, "connecting to jbdc/"+connection );
                this.connection = ((DataSource) ic.lookup("jdbc/" + connection)).getConnection();
                this.connected = true;
                MyLogger.log(DatabaseSpring.class.getName(), Level.INFO, "connected to jbdc/"+connection );
                
            }
        } catch (SQLException ex) {
            MyLogger.log(DatabaseSpring.class.getName(), Level.ERROR, ex.toString());
            return null;
        } catch (NamingException ex) {
            MyLogger.log(DatabaseSpring.class.getName(), Level.FATAL, ex.toString());
            return null;
        }
        return this.connection;
    }

    /**
     * Close connection.
     * <p/>
     * If connection exist, close it and update status.
     */
    public void disconnect() {
        if (this.connected) {
            try {
                this.connection.close();
//                MyLogger.log(DatabaseSpring.class.getName(), Level.INFO, "Disconnecting from datasource : jdbc/cerberus" +  System.getProperty("org.cerberus.environment"));
                this.connected = false;
            } catch (SQLException exception) {
                MyLogger.log(DatabaseSpring.class.getName(), Level.INFO, exception.toString());
            }
        }
    }
    
    /**TO CLEAN*/
  @SuppressWarnings("all")
    public ResultSet query(final String sql) {
        if (this.connected) {
            try {
                PreparedStatement stmt = this.connection.prepareStatement(sql);
                return stmt.executeQuery();
            } catch (SQLException ex) {
                MyLogger.log(DatabaseSpring.class.getName(), Level.FATAL, ex.toString());
            }
        }
        return null;
    }

    @SuppressWarnings("all")
    public ResultSet query(final String prepareStmt, final ArrayList<String> values) {
        if (this.connected) {
            try {
                PreparedStatement stmt = this.connection.prepareStatement(prepareStmt);
                for (int i = 1; i <= values.size(); i++) {
                    stmt.setString(i, values.get(i - 1));
                }
                return stmt.executeQuery();
            } catch (SQLException ex) {
                MyLogger.log(DatabaseSpring.class.getName(), Level.FATAL, ex.toString());
            }
        }
        return null;
    }

    @SuppressWarnings("all")
    public ResultSet queryRC(final String sql) throws SQLException {
        if (this.connected) {
            final PreparedStatement sqlStatement = this.connection.prepareStatement(sql);
            return sqlStatement.executeQuery();
        }
        return null;
    }

    public boolean execute(final String sql) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings("all")
    public void update(final String sql) {
        if (this.connected) {
            PreparedStatement stmt = null;
            try {
                stmt = this.connection.prepareStatement(sql);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                MyLogger.log(DatabaseSpring.class.getName(), Level.FATAL, ex.toString());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException ex) {
                    MyLogger.log(DatabaseSpring.class.getName(), Level.INFO, "Exception on close Statement in execute" + ex.toString());
                }
            }
        }
    }

    @SuppressWarnings("all")
    public int update(final String prepareStmt, final ArrayList<String> values) {
        if (this.connected) {
            PreparedStatement stmt = null;
            try {
                stmt = this.connection.prepareStatement(prepareStmt);
                for (int i = 1; i <= values.size(); i++) {
                    stmt.setString(i, values.get(i - 1));
                }
                return stmt.executeUpdate();
            } catch (SQLException ex) {
                MyLogger.log(DatabaseSpring.class.getName(), Level.FATAL, ex.toString());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException ex) {
                    MyLogger.log(DatabaseSpring.class.getName(), Level.INFO, "Exception on close Statement in update" + ex.toString());
                }
            }
        }
        return -1;
    }
    
    @SuppressWarnings("all")
    public Boolean existsResults(String sql) {
        if (this.connected) {
            PreparedStatement stmt = null;
            ResultSet rsExists = null;
            try {
                stmt = this.connection.prepareStatement("select count(*) as NumberCount from " + sql);
                rsExists = stmt.executeQuery();
                rsExists.next();
                if (rsExists.getString("NumberCount").compareTo("0") != 0) {
                    return true;
                }
            } catch (SQLException ex) {
                MyLogger.log(DatabaseSpring.class.getName(), Level.FATAL, ex.toString());
            } finally {
                try {
                    stmt.close();
                    rsExists.close();
                    this.disconnect();
                } catch (SQLException ex) {
                    MyLogger.log(DatabaseSpring.class.getName(), Level.INFO, "Exception on close Statement in existsResults" + ex.toString());
                }
            }
        }
        return false;
    }
}
