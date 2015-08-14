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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.cerberus.dao.IDocumentationDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Documentation;
import org.cerberus.factory.IFactoryDocumentation;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    public Documentation findDocumentationByKey(String docTable, String docField, String docValue, String lang) {
        Documentation result = null;
        final String query = "SELECT * FROM documentation d WHERE d.doctable = ? AND d.docfield = ? AND d.DocValue = ? AND Lang = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
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
                        result = factoryDocumentation.create(docTable, docField, docValue, docLabel, description);
                    }else{
                        return null;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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

    @Override
    public List<Documentation> findDocumentationsWithNotEmptyValueAndDescription(String docTable, String docField, String lang) {
        List<Documentation> result = new ArrayList<Documentation>();
        final String query = "SELECT DocValue, DocDesc, DocLabel FROM documentation where DocTable = ? and docfield = ? and Lang = ? and docValue IS NOT NULL and length(docValue) > 1 AND length(docdesc) > 1";

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

                        result.add(factoryDocumentation.create(docTable, docField, docValue, docLabel, description));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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

    @Override
    public List<Documentation> findDocumentationsWithEmptyValueAndNotEmptyDescription(String docTable, String docField, String lang) {
        List<Documentation> result = new ArrayList<Documentation>();
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

                        result.add(factoryDocumentation.create(docTable, docField, "", docLabel, description));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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

    @Override
    public String findLabelFromTableAndField(String docTable, String docField, String lang) {
        final String query = "SELECT DocLabel FROM documentation where DocTable = ? and docfield = ? and Lang = ? and length(docvalue)=0 and length(docdesc) > 1";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setMaxRows(1);
            try {
                preStat.setString(1, docTable);
                preStat.setString(2, docField);
                preStat.setString(3, lang);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        return resultSet.getString("DocLabel");
                    }
                } catch (SQLException exception) {
                    MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return null;
    }

    
    
    
    @Override
    public String findDescriptionFromTableFieldAndValue(String docTable, String docField, String docValue, String lang) {
        final String query = "SELECT DocDesc FROM documentation where DocTable = ? and DocField = ? and DocValue = ? and length(docdesc) > 1";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setMaxRows(1);
            try {
                preStat.setString(1, docTable);
                preStat.setString(2, docField);
                preStat.setString(3, docValue);
                preStat.setString(4, lang);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        return resultSet.getString("DocDesc");
                    }
                } catch (SQLException exception) {
                    MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return null;
    }

    @Override
    public List<Documentation> findAll(String lang) {

        List<Documentation> result = new ArrayList<Documentation>();
        final String query = "SELECT DocTable, DocField, DocValue, DocLabel, DocDesc FROM documentation where Lang = ?";

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

                        result.add(factoryDocumentation.create(table, field, value, label, description));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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

    @Override
    public List<Documentation> findAllWithEmptyDocValue(String lang) {
        List<Documentation> result = new ArrayList<Documentation>();
        final String query = "SELECT DocTable, DocField, DocValue, DocLabel, DocDesc FROM documentation where Lang = ? and docValue=''";

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
                        String description = "";

                        result.add(factoryDocumentation.create(table, field, value, label, description));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DocumentationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
