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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.IUserDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.factory.IFactoryUser;
import org.cerberus.crud.factory.impl.FactoryUser;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Benoit Dumont
 */
@Repository
public class UserDAO implements IUserDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryUser factoryUser;

    @Override
    public User findUserByKey(String login) {
        User result = null;
        final String query = "SELECT * FROM user u WHERE u.login = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, login);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadUserFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public List<User> findAllUser() {
        List<User> list = null;
        final String query = "SELECT * FROM user ORDER BY userid";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<User>();
                    while (resultSet.next()) {
                        User user = this.loadUserFromResultSet(resultSet);
                        list.add(user);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public boolean insertUser(User user) {
        boolean bool = false;
        final String query = "INSERT INTO user (Login, Password, Name, Request, ReportingFavorite, RobotHost, DefaultSystem, Team, Language, Email) VALUES (?, SHA(?), ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                preStat.setString(1, user.getLogin());
                preStat.setString(2, user.getPassword());
                preStat.setString(3, user.getName());
                preStat.setString(4, user.getRequest());
                preStat.setString(5, user.getReportingFavorite());
                preStat.setString(6, user.getRobotHost());
                preStat.setString(7, user.getDefaultSystem());
                preStat.setString(8, user.getTeam());
                preStat.setString(9, user.getLanguage());
                preStat.setString(10, user.getEmail());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        user.setUserID(resultSet.getInt(1));
                        bool = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    @Override
    public boolean deleteUser(User user) {
        boolean bool = false;
        final String query = "DELETE FROM user WHERE userid = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, user.getUserID());

                bool = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    @Override
    public boolean updateUser(User user) {
        boolean bool = false;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE user SET Login = ?, Name = ?, Request = ?, ReportingFavorite = ?, RobotHost = ?,");
        query.append("Team = ?, Language = ?, DefaultSystem = ?, Email= ? , robotPort = ?, ");
        query.append("robotPlatform = ?, ");
        query.append("robotBrowser = ?, robotVersion = ? , robot = ?   WHERE userid = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, user.getLogin());
                preStat.setString(2, user.getName());
                preStat.setString(3, user.getRequest());
                preStat.setString(4, user.getReportingFavorite());
                preStat.setString(5, user.getRobotHost());
                preStat.setString(6, user.getTeam());
                preStat.setString(7, user.getLanguage());
                preStat.setString(8, user.getDefaultSystem());
                preStat.setString(9, user.getEmail());
                preStat.setString(10, String.valueOf(user.getRobotPort()));
                preStat.setString(11, user.getRobotPlatform());
                preStat.setString(12, user.getRobotBrowser());
                preStat.setString(13, user.getRobotVersion());
                preStat.setString(14, user.getRobot());
                preStat.setInt(15, user.getUserID());

                bool = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    @Override
    public AnswerItem<User> updateUserPassword(User user, String password){
        AnswerItem<User> answer = new AnswerItem<User>();
        MessageEvent msg = null;
        boolean res = false;
        final String sql = "UPDATE user SET Password = SHA(?) , Request = ? WHERE Login LIKE ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, password);
                preStat.setString(2, "N");
                preStat.setString(3, user.getLogin());

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Update Password - Unable to execute query"));        
            } finally {
                if(preStat != null){
                    preStat.close();
                }                
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Update Password - Unable to execute query"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        if (res) {
            answer.setItem(this.findUserByKey(user.getLogin()));
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "User").replace("%OPERATION%", "Update password"));
        } else {
            answer.setItem(user);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "User").
                    replace("%OPERATION%", "Update Password").replace("%REASON%", "Your password was not updated. "
                            + "Please contact your Cerberus' administrator to learn more information."));
        }
        
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        boolean bool = false;
        final String sql = "SELECT Password, SHA(?) AS currentPassword FROM user WHERE Login LIKE ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, password);
                preStat.setString(2, user.getLogin());
                ResultSet rs = preStat.executeQuery();
                try {
                    if (rs.first()) {
                        bool = rs.getString("Password").equals(rs.getString("currentPassword"));
                    }
                } catch (SQLException ex) {
                    MyLogger.log(UserDAO.class.getName(), Level.FATAL, ex.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return bool;
    }

    private User loadUserFromResultSet(ResultSet rs) throws SQLException {
        int userID = ParameterParserUtil.parseIntegerParam(rs.getString("userid"), 0);
        String login = ParameterParserUtil.parseStringParam(rs.getString("login"), "");
        String password = ParameterParserUtil.parseStringParam(rs.getString("password"), "");
        String request = ParameterParserUtil.parseStringParam(rs.getString("request"), "");
        String name = ParameterParserUtil.parseStringParam(rs.getString("name"), "");
        String team = ParameterParserUtil.parseStringParam(rs.getString("team"), "");
        String language = ParameterParserUtil.parseStringParam(rs.getString("language"), "");
        String reportingFavorite = ParameterParserUtil.parseStringParam(rs.getString("reportingFavorite"), "");
        String robotHost = ParameterParserUtil.parseStringParam(rs.getString("robotHost"), "");
        String defaultSystem = ParameterParserUtil.parseStringParam(rs.getString("defaultSystem"), "");
        String email = ParameterParserUtil.parseStringParam(rs.getString("email"), "");
        String robotPort = ParameterParserUtil.parseStringParam(rs.getString("robotPort"), "");
        String robotPlatform = ParameterParserUtil.parseStringParam(rs.getString("robotPlatform"), "");
        String robotBrowser = ParameterParserUtil.parseStringParam(rs.getString("robotBrowser"), "");
        String robotVersion = ParameterParserUtil.parseStringParam(rs.getString("robotVersion"), "");
        String robot = ParameterParserUtil.parseStringParam(rs.getString("robot"), "");
        //TODO remove when working in test with mockito and autowired
        factoryUser = new FactoryUser();
        return factoryUser.create(userID, login, password, request, name, team, language, reportingFavorite, robotHost, robotPort, robotPlatform, robotBrowser, robotVersion, robot, defaultSystem, email, null, null);
    }

    @Override
    public List<User> findTestDataListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        List<User> result = new ArrayList<User>();
        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM user ");

        gSearch.append(" where (`login` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `name` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `team` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `defaultSystem` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `email` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `request` like '%");
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
        if (amount != -1) {
            query.append(" limit ");
            query.append(start);
            query.append(" , ");
            query.append(amount);
        }

        User user;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        result.add(this.loadUserFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, e.toString());
            }
        }

        return result;
    }

    @Override
    public Integer getNumberOfUserPerCriteria(String searchTerm, String inds) {
        Integer result = 0;
        StringBuilder query = new StringBuilder();
        StringBuilder gSearch = new StringBuilder();
        String searchSQL = "";

        query.append("SELECT count(*) FROM `user` ");

        gSearch.append(" where (`login` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `name` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `team` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `defaultSystem` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `email` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `request` like '%");
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
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;

    }

    @Override
    public List<User> findAllUserBySystem(String system) {
        List<User> list = null;
        final String query = "SELECT * " +
                "FROM `user` u, usersystem us " +
                "WHERE u.login = us.login " +
                "AND us.system = ? " +
                "ORDER BY u.login";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<User>();
                    while (resultSet.next()) {
                        User user = this.loadUserFromResultSet(resultSet);
                        list.add(user);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }
}
