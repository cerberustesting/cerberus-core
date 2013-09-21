package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IApplicationDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.Application;
import com.redcats.tst.entity.MessageEventEnum;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryApplication;
import com.redcats.tst.factory.impl.FactoryApplication;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.util.ParameterParserUtil;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
@Repository
public class ApplicationDAO implements IApplicationDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryApplication factoryApplication;

    @Override
    public Application findApplicationByKey(String application) throws CerberusException {
        boolean throwEx = false;
        Application result = null;
        final String query = "SELECT * FROM application a WHERE a.application = ? ";
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, application);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (!(resultSet.first())) {
                        throwEx = true;
                    }
                    result = this.loadApplicationFromResultSet(resultSet);

                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    /**
     * Find all existing applications
     *
     * @return list of applications
     */
    @Override
    public List<Application> findAllApplication() throws CerberusException {
        List<Application> list = null;
        final String query = "SELECT * FROM application a ORDER BY a.sort";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<Application>();
                    while (resultSet.next()) {
                        Application app = this.loadApplicationFromResultSet(resultSet);
                        list.add(app);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return list;
    }

    /**
     * Find all existing applications
     *
     * @return list of applications
     */
    @Override
    public List<Application> findApplicationBySystem(String System) throws CerberusException {
        List<Application> list = null;
        final String query = "SELECT * FROM application a WHERE `System` like ? ORDER BY a.sort";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, System);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<Application>();
                    while (resultSet.next()) {
                        Application app = this.loadApplicationFromResultSet(resultSet);
                        list.add(app);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return list;
    }

    private Application loadApplicationFromResultSet(ResultSet rs) throws SQLException {
        String application = ParameterParserUtil.parseStringParam(rs.getString("application"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("description"), "");
        String internal = ParameterParserUtil.parseStringParam(rs.getString("internal"), "");
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
        return factoryApplication.create(application, description, internal, sort, type, system
                , subsystem, svnUrl, deployType, mavenGroupId, bugTrackerUrl, bugTrackerNewUrl);
    }

    @Override
    public boolean updateApplication(Application application) throws CerberusException {
        boolean bool = false;
        final String query = "UPDATE application SET description = ?, internal = ?, sort = ?, `type` = ?, `system` = ?, SubSystem = ?, svnurl = ?, BugTrackerUrl = ?, BugTrackerNewUrl = ?, deploytype = ?, mavengroupid = ?  WHERE Application = ?";
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, application.getDescription());
            preStat.setString(2, application.getInternal());
            preStat.setInt(3, application.getSort());
            preStat.setString(4, application.getType());
            preStat.setString(5, application.getSystem());
            preStat.setString(6, application.getSubsystem());
            preStat.setString(7, application.getSvnurl());
            preStat.setString(8, application.getBugTrackerUrl());
            preStat.setString(9, application.getBugTrackerNewUrl());
            preStat.setString(10, application.getDeploytype());
            preStat.setString(11, application.getMavengroupid());
            preStat.setString(12, application.getApplication());
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

}
