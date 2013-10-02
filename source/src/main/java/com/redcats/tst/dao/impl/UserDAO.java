package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IUserDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryUser;
import com.redcats.tst.factory.impl.FactoryUser;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.util.ParameterParserUtil;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
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
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
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
        final String query = "INSERT INTO user (Login, Password, Name, Request, ReportingFavorite, DefaultIP, DefaultSystem, Team) VALUES (?, SHA(?), ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                preStat.setString(1, user.getLogin());
                preStat.setString(2, user.getPassword());
                preStat.setString(3, user.getName());
                preStat.setString(4, user.getRequest());
                preStat.setString(5, user.getReportingFavorite());
                preStat.setString(6, user.getDefaultIP());
                preStat.setString(7, user.getDefaultSystem());
                preStat.setString(8, user.getTeam());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        user.setUserID(resultSet.getInt(1));
                        bool = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
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
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
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
        final String query = "UPDATE user SET Login = ?, Name = ?, Request = ?, ReportingFavorite = ?, DefaultIP = ?, Team = ?, DefaultSystem = ?  WHERE userid = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, user.getLogin());
                preStat.setString(2, user.getName());
                preStat.setString(3, user.getRequest());
                preStat.setString(4, user.getReportingFavorite());
                preStat.setString(5, user.getDefaultIP());
                preStat.setString(6, user.getTeam());
                preStat.setString(7, user.getDefaultSystem());
                preStat.setInt(8, user.getUserID());

                bool = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
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
    public User updateUserPassword(User user, String password) throws CerberusException {
        boolean res = false;
        final String sql = "UPDATE User SET Password = SHA(?) , Request = ? WHERE Login LIKE ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, password);
                preStat.setString(2, "N");
                preStat.setString(3, user.getLogin());

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
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
            return this.findUserByKey(user.getLogin());
        } else {
            return user;
        }

    }

    @Override
    public boolean verifyPassword(User user, String password) {
        boolean bool = false;
        final String sql = "SELECT Password, SHA(?) AS currentPassword FROM User WHERE Login LIKE ?";

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
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
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
        String reportingFavorite = ParameterParserUtil.parseStringParam(rs.getString("reportingFavorite"), "");
        String defaultIP = ParameterParserUtil.parseStringParam(rs.getString("defaultIP"), "");
        String defaultSystem = ParameterParserUtil.parseStringParam(rs.getString("defaultSystem"), "");

        //TODO remove when working in test with mockito and autowired
        factoryUser = new FactoryUser();
        return factoryUser.create(userID, login, password, request, name, team, reportingFavorite, defaultIP, defaultSystem);
    }
}
