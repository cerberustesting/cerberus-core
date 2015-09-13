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
import org.cerberus.dao.IProjectDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.entity.Project;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryProject;
import org.cerberus.log.MyLogger;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author bcivel
 */
@Repository
public class ProjectDAO implements IProjectDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryProject factoryProject;

    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem readByKey(String project) {
        AnswerItem ans = new AnswerItem();
        Project result = null;
        String idProject;
        String vcCode;
        String description;
        final String query = "SELECT * FROM project WHERE idproject = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, project);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Project Lib").replace("%OPERATION%", "SELECT"));

                        ans.setItem(result);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ProjectDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<Project> projectList = new ArrayList<Project>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM project ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`idproject` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `VCCode` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `Description` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `active` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `dateCre` like '%").append(searchTerm).append("%')");
        }
        if (!StringUtil.isNullOrEmpty(individualSearch)) {
            searchSQL.append(" and (`").append(individualSearch).append("`)");
        }
        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(column)) {
            query.append("order by `").append(column).append("` ").append(dir);
        }
        if (!(amount == 0)) {
            query.append(" limit ").append(start).append(" , ").append(amount);
        } else {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        }
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        projectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Project Lib").replace("%OPERATION%", "SELECT"));
                    response = new AnswerList(projectList, nrTotalRows);

                } catch (SQLException exception) {
                    MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        response.setResultMessage(msg);
        response.setDataList(projectList);
        return response;
    }

    @Override
    public Answer create(Project project) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO project (`idproject`, `VCCode`, `Description`, `active` ) ");
        query.append("VALUES (?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, project.getIdProject());
                preStat.setString(2, project.getCode());
                preStat.setString(3, project.getDescription());
                preStat.setString(4, project.getActive());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Project").replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_DUPLICATE_ERROR);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Project").replace("%OPERATION%", "INSERT"));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query - INSERT Project"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ProjectDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(Project project) {
        MessageEvent msg = null;
        final String query = "DELETE FROM project WHERE idproject = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, project.getIdProject());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Project").replace("%OPERATION%", "DELETE"));
            } catch (SQLException exception) {
                MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ProjectDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(Project project) {
        MessageEvent msg = null;
        final String query = "UPDATE project SET VCCode = ?, Description = ?, active = ?  WHERE idproject = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, project.getCode());
                preStat.setString(2, project.getDescription());
                preStat.setString(3, project.getActive());
                preStat.setString(4, project.getIdProject());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Project").replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ProjectDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Project loadFromResultSet(ResultSet resultSet) throws SQLException {
        String idProject = resultSet.getString("idproject") == null ? "" : resultSet.getString("idproject");
        String vcCode = resultSet.getString("VCCode") == null ? "" : resultSet.getString("VCCode");
        String description = resultSet.getString("Description") == null ? "" : resultSet.getString("Description");
        String active = resultSet.getString("active") == null ? "" : resultSet.getString("active");
        String dateCreation = resultSet.getString("dateCre") == null ? "" : resultSet.getString("dateCre");
        return factoryProject.create(idProject, vcCode, description, active, dateCreation);
    }
}
