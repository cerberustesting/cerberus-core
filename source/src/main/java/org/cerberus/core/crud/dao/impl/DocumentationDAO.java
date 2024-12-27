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
package org.cerberus.core.crud.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IDocumentationDAO;
import org.cerberus.core.crud.entity.Documentation;
import org.cerberus.core.crud.factory.IFactoryDocumentation;
import org.cerberus.core.database.DatabaseSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bcivel
 */
@Repository
public class DocumentationDAO implements IDocumentationDAO {

    private static final Logger LOG = LogManager.getLogger(DocumentationDAO.class);

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryDocumentation factoryDocumentation;

    @Override
    public Documentation findDocumentationByKey(String docTable, String docField, String docValue, String lang) {
        Documentation result = null;
        final String query = "SELECT * FROM documentation d WHERE d.doctable = ? AND d.docfield = ? AND d.DocValue = ? AND Lang = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                preStat.setString(1, docTable);
                preStat.setString(2, docField);
                preStat.setString(3, docValue);
                preStat.setString(4, lang);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String docLabel = resultSet.getString("DocLabel");
                        String description = resultSet.getString("DocDesc");
                        String docAnchor = resultSet.getString("DocAnchor");
                        result = factoryDocumentation.create(docTable, docField, docValue, docLabel, description, docAnchor);
                    } else {
                        return null;
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return result;
    }

    @Override
    public List<Documentation> findDocumentationsWithNotEmptyValueAndDescription(String docTable, String docField, String lang) {
        List<Documentation> result = new ArrayList<>();
        final String query = "SELECT DocValue, DocDesc, DocLabel, DocAnchor FROM documentation where DocTable = ? and docfield = ? and Lang = ? and docValue IS NOT NULL and length(docValue) > 1 AND length(docdesc) > 1";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, docTable);
                preStat.setString(2, docField);
                preStat.setString(3, lang);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        String docLabel = resultSet.getString("DocLabel");
                        String description = resultSet.getString("DocDesc");
                        String docValue = resultSet.getString("DocValue");
                        String docAnchor = resultSet.getString("DocAnchor");

                        result.add(factoryDocumentation.create(docTable, docField, docValue, docLabel, description, docAnchor));
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return result;
    }

    @Override
    public List<Documentation> findDocumentationsWithEmptyValueAndNotEmptyDescription(String docTable, String docField, String lang) {
        List<Documentation> result = new ArrayList<>();
        final String query = "SELECT DocDesc, DocLabel FROM documentation where DocTable = ? and docfield = ? and Lang = ? and length(docvalue)=0 and length(docdesc) > 1";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, docTable);
                preStat.setString(2, docField);
                preStat.setString(3, lang);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        String docLabel = resultSet.getString("DocLabel");
                        String description = resultSet.getString("DocDesc");
                        String docAnchor = resultSet.getString("DocAnchor");

                        result.add(factoryDocumentation.create(docTable, docField, "", docLabel, description, docAnchor));
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return result;
    }

    @Override
    public String findLabelFromTableAndField(String docTable, String docField, String lang) {
        final String query = "SELECT DocLabel FROM documentation where DocTable = ? and docfield = ? and Lang = ? and length(docvalue)=0 and length(docdesc) > 1";


        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

            preStat.setMaxRows(1);
            preStat.setString(1, docTable);
            preStat.setString(2, docField);
            preStat.setString(3, lang);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    return resultSet.getString("DocLabel");
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return null;
    }


    @Override
    public String findDescriptionFromTableFieldAndValue(String docTable, String docField, String docValue, String lang) {
        final String query = "SELECT DocDesc FROM documentation where DocTable = ? and DocField = ? and DocValue = ? and Lang = ? and length(docdesc) > 1";


        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

            preStat.setMaxRows(1);
            preStat.setString(1, docTable);
            preStat.setString(2, docField);
            preStat.setString(3, docValue);
            preStat.setString(4, lang);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    return resultSet.getString("DocDesc");
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }

        return null;
    }

    @Override
    public List<Documentation> findAll(String lang) {

        List<Documentation> result = new ArrayList<>();
        final String query = "SELECT DocTable, DocField, DocValue, DocLabel, DocDesc, DocAnchor FROM documentation where Lang = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, lang);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        String table = resultSet.getString("DocTable");
                        String field = resultSet.getString("DocField");
                        String value = resultSet.getString("DocValue");
                        String label = resultSet.getString("DocLabel");
                        String description = resultSet.getString("DocDesc");
                        String docAnchor = resultSet.getString("DocAnchor");

                        result.add(factoryDocumentation.create(table, field, value, label, description, docAnchor));
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return result;
    }

    @Override
    public List<Documentation> findAllWithEmptyDocValue(String lang) {
        List<Documentation> result = new ArrayList<>();
        final String query = "SELECT DocTable, DocField, DocValue, DocLabel, DocDesc, DocAnchor FROM documentation where Lang = ? and docValue='' ORDER BY DocTable, DocField, DocValue asc";

        Connection connection = this.databaseSpring.connect();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.lang : " + lang);
        }

        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, lang);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        String table = resultSet.getString("DocTable");
                        String field = resultSet.getString("DocField");
                        String value = resultSet.getString("DocValue");
                        String label = resultSet.getString("DocLabel");
                        String description = resultSet.getString("DocDesc");
                        String anchor = resultSet.getString("DocAnchor");

                        result.add(factoryDocumentation.create(table, field, value, label, description, anchor));
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return result;
    }

}
