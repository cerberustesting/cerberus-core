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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, login);
            try {
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
            this.databaseSpring.disconnect();
        }
        return result;
    }

    @Override
    public List<User> findAllUser() {
        List<User> list = null;
        final String query = "SELECT * FROM user ORDER BY userid";
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
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
            this.databaseSpring.disconnect();
        }
        return list;
    }

    @Override
    public boolean insertUser(User user) {
        boolean bool = false;
        final String query = "INSERT INTO user (Login, Password, Name, Request, ReportingFavorite, DefaultIP, DefaultSystem, Team) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preStat.setString(1, user.getLogin());
            preStat.setString(2, user.getPassword());
            preStat.setString(3, user.getName());
            preStat.setString(4, user.getRequest());
            preStat.setString(5, user.getReportingFavorite());
            preStat.setString(6, user.getDefaultIP());
            preStat.setString(7, user.getDefaultSystem());
            preStat.setString(8, user.getTeam());
            try {
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
            this.databaseSpring.disconnect();
        }
        return bool;
    }

    @Override
    public boolean deleteUser(User user) {
        boolean bool = false;
        final String query = "DELETE FROM user WHERE userid = ?";
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setInt(1, user.getUserID());
            try {
                int res = preStat.executeUpdate();
                bool = res > 0;
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return bool;
    }

    @Override
    public boolean updateUser(User user) {
        boolean bool = false;
        final String query = "UPDATE user SET Login = ?, Name = ?, Request = ?, ReportingFavorite = ?, DefaultIP = ?, Team = ?, DefaultSystem = ?  WHERE userid = ?";
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, user.getLogin());
            preStat.setString(2, user.getName());
            preStat.setString(3, user.getRequest());
            preStat.setString(4, user.getReportingFavorite());
            preStat.setString(5, user.getDefaultIP());
            preStat.setString(6, user.getTeam());
            preStat.setString(7, user.getDefaultSystem());
            preStat.setInt(8, user.getUserID());
            try {
                int res = preStat.executeUpdate();
                bool = res > 0;
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return bool;
    }

    @Override
    public User updateUserPassword(User user, String password) throws CerberusException {
        final String sql = "UPDATE User SET Password = SHA(?) , Request = ? WHERE Login LIKE ?";
        ArrayList<String> al = new ArrayList<String>();
        al.add(password);
        al.add("N");
        al.add(user.getLogin());

        databaseSpring.connect();
        boolean res = databaseSpring.update(sql, al) > 0;
        databaseSpring.disconnect();
        if (res) {
            return this.findUserByKey(user.getLogin());
        } else {
            return user;
        }

    }

    @Override
    public boolean verifyPassword(User user, String password) {
        boolean bool = false;
        final String sql = "SELECT Password, SHA(?) as currentPassword FROM User WHERE Login LIKE ?";
        ArrayList<String> al = new ArrayList<String>();
        al.add(password);
        al.add(user.getLogin());

        databaseSpring.connect();
        ResultSet rs = databaseSpring.query(sql, al);
        try {
            if (rs.first()) {
                bool = rs.getString("Password").equals(rs.getString("currentPassword"));
            }
            rs.close();
        } catch (SQLException ex) {
            MyLogger.log(UserDAO.class.getName(), Level.FATAL, ex.toString());
        }
        databaseSpring.disconnect();

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
