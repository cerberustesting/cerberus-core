/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IDocumentationDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.Documentation;
import com.redcats.tst.factory.IFactoryDocumentation;
import com.redcats.tst.log.MyLogger;
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
