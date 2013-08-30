package com.redcats.tst.refactor;

import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

public class DbMysqlController implements Database {

    private java.sql.Connection connection;
    private Boolean connected;

    @Override
    public Connection connect() {
        if (!this.isConnected()) {
            try {
                InitialContext ic = new InitialContext();
                if (System.getProperty("env") == null) {
                    MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, "Property not defined. Cannot connect to database.");
                    connected = false;
                } else {
                    MyLogger.log(DbMysqlController.class.getName(), Level.INFO, "Connecting to jdbc/cerberus".concat(System.getProperty("env")));
                    this.connection = ((DataSource) ic.lookup("jdbc/cerberus".concat(System.getProperty("env")))).getConnection();
                    connected = true;
                }
                connected = true;
            } catch (SQLException ex) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, ex.toString());
            } catch (NamingException ex) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, ex.toString());
            }
        }
        return this.connection;
    }

    public Connection connect(String connection) {
        try {
            InitialContext ic = new InitialContext();
            MyLogger.log(DbMysqlController.class.getName(), Level.INFO, connection);
            this.connection = ((DataSource) ic.lookup("jdbc/" + connection)).getConnection();
            connected = true;
        } catch (SQLException ex) {
            MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, ex.toString());
            return null;
        } catch (NamingException ex) {
            MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, ex.toString());
            return null;
        }
        return this.connection;
    }

    @Override
    public void disconnect() {
        if (this.isConnected()) {
            try {
                MyLogger.log(DbMysqlController.class.getName(), Level.INFO, "Disconnecting...");
                this.connection.close();
                this.connected = false;
            } catch (final SQLException ex) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, ex.toString());
            }
        }
    }

    @SuppressWarnings("all")
    @Override
    public boolean execute(final String sql) {
        if (this.isConnected(true)) {
            PreparedStatement _sqlStatement = null;
            try {
                _sqlStatement = this.connection.prepareStatement(sql);
                _sqlStatement.execute();
                return true;
            } catch (final Exception e) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, e.toString());
            } finally {
                try {
                    _sqlStatement.close();
                } catch (SQLException ex) {
                    MyLogger.log(DbMysqlController.class.getName(), Level.INFO, ex.toString());
                }
            }
        }
        return false;
    }

    @Override
    public LinkedList<String> getColStringFromColumn(final String sql, final String columnName) {
        final LinkedList<String> _col = new LinkedList<String>();
        final ResultSet _rs = this.query(sql);
        try {
            while (_rs.next()) {
                _col.add(_rs.getString(columnName));
            }
        } catch (final Exception e) {
            MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, e.toString());
        } finally {
            try {
                _rs.close();
            } catch (SQLException ex) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, ex.toString());
            }
        }
        return _col;
    }

    @Override
    public int getLastId() {
        if (this.isConnected()) {
            final ResultSet rs = this.query("SELECT LAST_INSERT_ID()");
            try {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (final Exception e) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, e.toString());
            } finally {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    MyLogger.log(DbMysqlController.class.getName(), Level.INFO, ex.toString());
                }
            }
        }
        return -1;
    }

    @Override
    public boolean isConnected() {
        if (this.connection != null) {
            try {
                return this.connected;
            } catch (final Exception e) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, e.toString());
            }
        }
        return false;
    }

    @Override
    public boolean isConnected(final boolean tryConnect) {
        if (!this.isConnected()) {
            if (tryConnect) {
                this.connect();
                this.connected = true;
            }
        }
        return this.isConnected();
    }

    @SuppressWarnings("all")
    @Override
    public ResultSet query(final String sql) {
        if (this.isConnected(true)) {
            try {
                PreparedStatement sqlStatement = this.connection.prepareStatement(sql);
                return sqlStatement.executeQuery();
            } catch (final Exception e) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, e.toString());
            }
        }
        return null;
    }

    @SuppressWarnings("all")
    @Override
    public ResultSet queryRC(final String sql) throws SQLException {
        if (this.isConnected(true)) {
            final PreparedStatement sqlStatement = this.connection.prepareStatement(sql);
            return sqlStatement.executeQuery();
        }
        return null;
    }

    @SuppressWarnings("all")
    public ResultSet query(final String prepareStmt, final ArrayList<String> values) {
        if (this.isConnected(true)) {
            try {
                PreparedStatement stmt = this.connection.prepareStatement(prepareStmt);
                for (int i = 1; i <= values.size(); i++) {
                    stmt.setString(i, values.get(i - 1));
                }
                return stmt.executeQuery();
            } catch (SQLException ex) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, ex.toString());
            }
        }
        return null;
    }

    @Override
    public boolean queryBoolean(final String sql) {

        try {
            return this.query(sql).first();
        } catch (final Exception e) {
            MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, e.toString());
        }
        return false;
    }

    @Override
    public void update(final String string) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings("all")
    public int update(final String prepareStmt, final ArrayList<String> values) {
        if (this.isConnected(true)) {
            PreparedStatement stmt = null;
            try {
                stmt = this.connection.prepareStatement(prepareStmt);
                for (int i = 1; i <= values.size(); i++) {
                    stmt.setString(i, values.get(i - 1));
                }
                return stmt.executeUpdate();
            } catch (SQLException ex) {
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, ex.toString());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException ex) {
                    MyLogger.log(DbMysqlController.class.getName(), Level.INFO, "Exception on close Statement in update" + ex.toString());
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("all")
    public Boolean existsResults(String sql) {
        if (this.isConnected(true)) {
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
                MyLogger.log(DbMysqlController.class.getName(), Level.FATAL, ex.toString());
            } finally {
                try {
                    stmt.close();
                    rsExists.close();
                    this.disconnect();
                } catch (SQLException ex) {
                    MyLogger.log(DbMysqlController.class.getName(), Level.INFO, "Exception on close Statement in existsResults" + ex.toString());
                }
            }
        }
        return false;
    }
}
