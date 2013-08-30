package com.redcats.tst.refactor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public interface Database {

    Connection connect();

    void disconnect();

    boolean execute(String sql);

    LinkedList<String> getColStringFromColumn(String sql, String columnName);

    int getLastId();

    boolean isConnected();

    boolean isConnected(boolean tryConnect);

    ResultSet query(String sql);

    ResultSet queryRC(String sql) throws SQLException;

    boolean queryBoolean(String sql);

    void update(String string);
}
