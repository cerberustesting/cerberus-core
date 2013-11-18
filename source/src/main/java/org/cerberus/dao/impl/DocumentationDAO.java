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
package org.cerberus.dao.impl;

import org.cerberus.dao.IDocumentationDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Documentation;
import org.cerberus.factory.IFactoryDocumentation;
import org.cerberus.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author bcivel
 */
@Repository
public class DocumentationDAO implements IDocumentationDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryDocumentation factoryDocumentation;

    @Override
    public Documentation findDocumentationByKey(String docTable, String docField) {
        Documentation result = null;
        final String query = "SELECT * FROM documentation d WHERE d.doctable = ? AND d.docfield = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, docTable);
                preStat.setString(2, docField);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        String docValue = resultSet.getString("DocValue");
                        String docLabel = resultSet.getString("DocLabel");
                        String description = resultSet.getString("DocDesc");
                        result = factoryDocumentation.create(docTable, docField, docValue, docLabel, description);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }
}
