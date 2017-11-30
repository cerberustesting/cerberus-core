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
package org.cerberus.crud.utils;

import org.cerberus.database.DatabaseSpring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class RequestDbUtils {

    @FunctionalInterface
    public interface SqlFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    @FunctionalInterface
    public interface VoidSqlFunction<T> {
        void apply(T t) throws SQLException;
    }

    public static <T> T executeQuery(DatabaseSpring databaseSpring, String query, VoidSqlFunction<PreparedStatement> functionPrepareStatement,
                                     SqlFunction<ResultSet, T> functionResultSet) throws SQLException {
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query);
        ) {
            functionPrepareStatement.apply(preStat);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    return functionResultSet.apply(resultSet);
                }
            }
        }

        return null;
    }


    public static <T> List<T> executeQueryList(DatabaseSpring databaseSpring, String query, SqlFunction<PreparedStatement, Void> functionPrepareStatement,
                                               SqlFunction<ResultSet, T> functionResultSet) throws SQLException {
        List<T> res = new LinkedList<>();

        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query);
        ) {
            functionPrepareStatement.apply(preStat);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.next()) {
                    res.add(functionResultSet.apply(resultSet));
                }
            }
        }

        return res;
    }
}
