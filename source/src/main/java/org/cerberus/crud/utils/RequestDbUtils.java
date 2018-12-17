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
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;

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
                                     SqlFunction<ResultSet, T> functionResultSet) throws CerberusException {
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query);
        ) {
            functionPrepareStatement.apply(preStat);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    return functionResultSet.apply(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR), exception);
        }

        return null;
    }


    public static <T> T executeUpdate(DatabaseSpring databaseSpring, String query, VoidSqlFunction<PreparedStatement> functionPrepareStatement) throws CerberusException {
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query);
        ) {
            functionPrepareStatement.apply(preStat);
            preStat.executeUpdate();
        } catch (SQLException exception) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR), exception);
        }

        return null;
    }

    public static <T> List<T> executeQueryList(DatabaseSpring databaseSpring, String query, VoidSqlFunction<PreparedStatement> functionPrepareStatement,
                                               SqlFunction<ResultSet, T> functionResultSet) throws CerberusException {
        List<T> res = new LinkedList<>();

        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query);
        ) {
            functionPrepareStatement.apply(preStat);

            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    res.add(functionResultSet.apply(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR), exception);
        }

        return res;
    }
}
