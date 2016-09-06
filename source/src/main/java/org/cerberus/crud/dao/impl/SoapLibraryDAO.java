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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ISoapLibraryDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.SoapLibrary;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactorySoapLibrary;
import org.cerberus.log.MyLogger;
import org.cerberus.util.answer.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author cte
 */
@Repository
public class SoapLibraryDAO implements ISoapLibraryDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactorySoapLibrary factorySoapLib;

    private static final Logger LOG = Logger.getLogger(ParameterDAO.class);

    private final int MAX_ROW_SELECTED = 100000;
    private final String SQL_DUPLICATED_CODE = "23000";

    private final String OBJECT_NAME = "SqlLibrary";
    @Override
    public SoapLibrary findSoapLibraryByKey(String name) throws CerberusException {
        boolean throwEx = false;
        SoapLibrary result = null;
        final String query = "SELECT * FROM soaplibrary  WHERE NAME = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, name);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String type = resultSet.getString("Type");
                        String envelope = resultSet.getString("Envelope");
                        String description = resultSet.getString("Description");
                        String servicePath = resultSet.getString("servicePath");
                        String parsingAnswer = resultSet.getString("parsingAnswer");
                        String method = resultSet.getString("method");
                        result = this.factorySoapLib.create(type, name, envelope, description, servicePath, parsingAnswer, method);
                    } else {
                        throwEx = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.SQLLIB_NOT_FOUND));
        }
        return result;
    }

    @Override
    public void createSoapLibrary(SoapLibrary soapLibrary) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO soaplibrary (`Type`, `Name`, `Envelope`, `Description`, `ServicePath`, `ParsingAnswer`, `Method`) ");
        query.append("VALUES (?,?,?,?,?,?,?);");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, soapLibrary.getType());
                preStat.setString(2, soapLibrary.getName());
                preStat.setString(3, soapLibrary.getEnvelope());
                preStat.setString(4, soapLibrary.getDescription());
                preStat.setString(5, soapLibrary.getServicePath());
                preStat.setString(6, soapLibrary.getParsingAnswer());
                preStat.setString(7, soapLibrary.getMethod());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateSoapLibrary(SoapLibrary soapLibrary) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update soaplibrary set `envelope`=?, `description`=?, `type`=?, 'servicePath'=?, 'parsingAnswer'=?, 'method'=?  where `name`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, soapLibrary.getEnvelope());
                preStat.setString(2, soapLibrary.getDescription());
                preStat.setString(3, soapLibrary.getType());
                preStat.setString(4, soapLibrary.getName());
                preStat.setString(5, soapLibrary.getServicePath());
                preStat.setString(6, soapLibrary.getParsingAnswer());
                preStat.setString(7, soapLibrary.getMethod());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteSoapLibrary(SoapLibrary soapLibrary) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("delete from soaplibrary where `Name`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, soapLibrary.getName());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<SoapLibrary> findAllSoapLibrary() {
        List<SoapLibrary> list = null;
        final String query = "SELECT * FROM SoapLibrary";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<SoapLibrary>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadSoapLibraryFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<SoapLibrary> findSoapLibraryListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        List<SoapLibrary> soapLibraryList = new ArrayList<SoapLibrary>();
        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM soaplibrary ");

        gSearch.append(" where (`type` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `name` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `envelope` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !individualSearch.equals("")) {
            searchSQL.append(gSearch.toString());
            searchSQL.append(" and ");
            searchSQL.append(individualSearch);
        } else if (!individualSearch.equals("")) {
            searchSQL.append(" where `");
            searchSQL.append(individualSearch);
            searchSQL.append("`");
        } else if (!searchTerm.equals("")) {
            searchSQL.append(gSearch.toString());
        }

        query.append(searchSQL);
        query.append("order by `");
        query.append(column);
        query.append("` ");
        query.append(dir);
        query.append(" limit ");
        query.append(start);
        query.append(" , ");
        query.append(amount);

        SoapLibrary soapLibrary;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        soapLibraryList.add(this.loadSoapLibraryFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return soapLibraryList;
    }

    @Override
    public void updateSoapLibrary(String name, String columnName, String value) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update soaplibrary set `");
        query.append(columnName);
        query.append("`=? where `name`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, value);
                preStat.setString(2, name);

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public Integer getNumberOfSoapLibraryPerCrtiteria(String searchTerm, String inds) {
        Integer result = 0;
        StringBuilder query = new StringBuilder();
        StringBuilder gSearch = new StringBuilder();
        String searchSQL = "";

        query.append("SELECT count(*) FROM soaplibrary");

        gSearch.append(" where (`name` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `type` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `envelope` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !inds.equals("")) {
            searchSQL = gSearch.toString() + " and " + inds;
        } else if (!inds.equals("")) {
            searchSQL = " where " + inds;
        } else if (!searchTerm.equals("")) {
            searchSQL = gSearch.toString();
        }

        query.append(searchSQL);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    if (resultSet.first()) {
                        result = resultSet.getInt(1);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SoapLibraryDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;

    }

    @Override
    public Answer create(SoapLibrary object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO soaplibrary (`Name`, `Type`, `ServicePath`, `Method`, `Envelope`, `ParsingAnswer`, `Description`) ");
        query.append("VALUES (?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, object.getName());
                preStat.setString(2, object.getType());
                preStat.setString(3, object.getServicePath());
                preStat.setString(4, object.getMethod());
                preStat.setString(5, object.getEnvelope());
                preStat.setString(6, object.getParsingAnswer());
                preStat.setString(7, object.getDescription());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(SoapLibrary object) {
        MessageEvent msg = null;
        final String query = "UPDATE soaplibrary SET Type = ?, `ServicePath` = ?, `Method` = ?, Envelope = ?, ParsingAnswer = ?, Description = ? WHERE Name = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, object.getType());
                preStat.setString(2, object.getServicePath());
                preStat.setString(3, object.getMethod());
                preStat.setString(4, object.getEnvelope());
                preStat.setString(5, object.getParsingAnswer());
                preStat.setString(6, object.getDescription());
                preStat.setString(7, object.getName());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(SoapLibrary object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM soaplibrary WHERE name = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, object.getName());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    private SoapLibrary loadSoapLibraryFromResultSet(ResultSet resultSet) throws SQLException {
        String type = resultSet.getString("type");
        String name = resultSet.getString("name");
        String envelope = resultSet.getString("envelope");
        String description = resultSet.getString("description");
        String servicePath = resultSet.getString("servicePath");
        String parsingAnswer = resultSet.getString("parsingAnswer");
        String method = resultSet.getString("method");
        return this.factorySoapLib.create(type, name, envelope, description, servicePath, parsingAnswer, method);
    }
}
