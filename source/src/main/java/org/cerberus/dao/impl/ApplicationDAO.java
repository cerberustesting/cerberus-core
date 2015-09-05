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
import org.cerberus.dao.IApplicationDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Application;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryApplication;
import org.cerberus.factory.impl.FactoryApplication;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@Repository
public class ApplicationDAO implements IApplicationDAO {

    /**
     * Bean of the DatabaseSpring, Spring automatically links. Establishes
     * connection to database and return it to allow perform queries and
     * updates.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    /**
     * Bean of the IFactoryApplication, Spring automatically links. Creates new
     * objects {@link Application}
     */
    @Autowired
    private IFactoryApplication factoryApplication;

    private final String SQL_DUPLICATED_CODE = "23000";

    /**
     *
     * @param application - idApplication
     * @return ans - AnswerIterm
     */
    @Override
    public AnswerItem readByKey(String application) {
        AnswerItem ans = new AnswerItem();
        Application result = null;
        final String query = "SELECT * FROM `application` WHERE `application` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, application);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Application").replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    /**
     * Finds the Application by the name. </p> Access to database to return the
     * {@link Application} given by the unique name.<br/> If no application
     * found with the given name, returns CerberusException with
     * {@link MessageGeneralEnum#NO_DATA_FOUND}.<br/> If an SQLException occur,
     * returns null in the application object and writes the error on the logs.
     *
     * @param application name of the Application to find
     * @return object application if exist
     * @throws CerberusException when Application does not exist
     * @since 0.9.0
     */
    @Override
    public Application readByKey_Deprecated(String application) throws CerberusException {
        Application result = null;
        final String query = "SELECT * FROM application a WHERE a.application = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, application);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;
    }

    /**
     * Finds all Applications that exists. </p> Access to database to return all
     * existing {@link Application}.<br/> If no application found, returns a
     * empty {@literal List<Application>}.<br/> If an SQLException occur,
     * returns null in the list object and writes the error on the logs.
     *
     * @return list of applications
     * @throws CerberusException
     * @since 0.9.0
     */
    @Override
    public List<Application> readAll_Deprecated() throws CerberusException {
        List<Application> list = null;
        final String query = "SELECT * FROM application a ORDER BY a.Application asc, a.sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<Application>();
                    while (resultSet.next()) {
                        Application app = this.loadFromResultSet(resultSet);
                        list.add(app);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    /**
     * Finds Applications of the given system. </p> Access to database to return
     * a list of {@link Application} filtering by system.<br/> If no application
     * found, returns a empty {@literal List<Application>}.<br/> If an
     * SQLException occur, returns null in the list object and writes the error
     * on the logs.
     *
     * @param system name of the System to filter
     * @return list of applications
     * @throws CerberusException
     * @since 0.9.0
     */
    @Override
    public List<Application> readBySystem_Deprecated(String system) throws CerberusException {
        List<Application> list = null;
        final String query = "SELECT * FROM application a WHERE `System` LIKE ? ORDER BY a.sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<Application>();
                    while (resultSet.next()) {
                        Application app = this.loadFromResultSet(resultSet);
                        list.add(app);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public AnswerList readBySystemByCriteria(String system, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<Application> applicationList = new ArrayList<Application>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM application ");

        searchSQL.append(" where 1=1 ");

        if (!searchTerm.equals("")) {
            searchSQL.append(" and (`application` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `description` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `sort` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `type` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `System` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `Subsystem` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `svnURL` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `bugtrackerurl` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `bugtrackernewurl` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `deploytype` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `mavengroupid` like '%").append(searchTerm).append("%')");
        }
        if (!individualSearch.equals("")) {
            searchSQL.append(" and (`").append(individualSearch).append("`)");
        }
        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" and (`System`='").append(system).append("' )");
        }

        MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Search Term : " + searchSQL.toString());

        query.append(searchSQL);
        query.append(" order by `").append(column).append("` ").append(dir);
        query.append(" limit ").append(start).append(" , ").append(amount);

        MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Execute query : " + query.toString());

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        applicationList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Application").replace("%OPERATION%", "SELECT"));
                    response = new AnswerList(applicationList, nrTotalRows);

                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        response.setResultMessage(msg);
        response.setDataList(applicationList);
        return response;
    }

    @Override
    public Answer create(Application application) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO application (`application`, `description`, `sort`, `type`, `system`, `SubSystem`, `svnurl`, `BugTrackerUrl`, `BugTrackerNewUrl`, `deploytype`, `mavengroupid` ) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, application.getApplication());
                preStat.setString(2, application.getDescription());
                preStat.setInt(3, application.getSort());
                preStat.setString(4, application.getType());
                preStat.setString(5, application.getSystem());
                preStat.setString(6, application.getSubsystem());
                preStat.setString(7, application.getSvnurl());
                preStat.setString(8, application.getBugTrackerUrl());
                preStat.setString(9, application.getBugTrackerNewUrl());
                preStat.setString(10, application.getDeploytype());
                preStat.setString(11, application.getMavengroupid());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Application").replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_DUPLICATE_ERROR);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Application").replace("%OPERATION%", "INSERT"));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(Application application) {
        MessageEvent msg = null;
        final String query = "DELETE FROM application WHERE application = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, application.getApplication());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Application").replace("%OPERATION%", "DELETE"));
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(Application application) {
        MessageEvent msg = null;
        final String query = "UPDATE application SET description = ?, sort = ?, `type` = ?, `system` = ?, SubSystem = ?, svnurl = ?, BugTrackerUrl = ?, BugTrackerNewUrl = ?, deploytype = ?, mavengroupid = ?  WHERE Application = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, application.getDescription());
                preStat.setInt(2, application.getSort());
                preStat.setString(3, application.getType());
                preStat.setString(4, application.getSystem());
                preStat.setString(5, application.getSubsystem());
                preStat.setString(6, application.getSvnurl());
                preStat.setString(7, application.getBugTrackerUrl());
                preStat.setString(8, application.getBugTrackerNewUrl());
                preStat.setString(9, application.getDeploytype());
                preStat.setString(10, application.getMavengroupid());
                preStat.setString(11, application.getApplication());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Application").replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return new Answer(msg);
    }

    /**
     *
     * @return @throws CerberusException
     * @since 0.9.1
     */
    @Override
    public List<String> readDistinctSystem() {
        List<String> list = null;
        final String query = "SELECT DISTINCT a.system FROM application a ORDER BY a.system ASC";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();
                    while (resultSet.next()) {
                        list.add(resultSet.getString("system"));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public Application loadFromResultSet(ResultSet rs) throws SQLException {
        String application = ParameterParserUtil.parseStringParam(rs.getString("application"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("description"), "");
        int sort = ParameterParserUtil.parseIntegerParam(rs.getString("sort"), 0);
        String type = ParameterParserUtil.parseStringParam(rs.getString("type"), "");
        String system = ParameterParserUtil.parseStringParam(rs.getString("system"), "");
        String subsystem = ParameterParserUtil.parseStringParam(rs.getString("subsystem"), "");
        String svnUrl = ParameterParserUtil.parseStringParam(rs.getString("svnurl"), "");
        String deployType = ParameterParserUtil.parseStringParam(rs.getString("deploytype"), "");
        String mavenGroupId = ParameterParserUtil.parseStringParam(rs.getString("mavengroupid"), "");
        String bugTrackerUrl = ParameterParserUtil.parseStringParam(rs.getString("bugtrackerurl"), "");
        String bugTrackerNewUrl = ParameterParserUtil.parseStringParam(rs.getString("bugtrackernewurl"), "");

        //TODO remove when working in test with mockito and autowired
        factoryApplication = new FactoryApplication();
        return factoryApplication.create(application, description, sort, type, system, subsystem, svnUrl, deployType, mavenGroupId, bugTrackerUrl, bugTrackerNewUrl);
    }
}
