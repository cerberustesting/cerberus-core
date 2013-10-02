/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IDeployTypeDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.DeployType;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryDeployType;
import com.redcats.tst.factory.impl.FactoryDeployType;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.util.ParameterParserUtil;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DeployTypeDAO implements IDeployTypeDAO {
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryDeployType factoryDeployType;

    @Override
    public DeployType findDeployTypeByKey(String deploytype) throws CerberusException {
        boolean throwEx = false;
        DeployType result = null;
        final String query = "SELECT * FROM DeployType a WHERE a.deploytype = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, deploytype);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (!(resultSet.first())) {
                        throwEx = true;
                    }
                    result = this.loadDeployTypeFromResultSet(resultSet);

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
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public List<DeployType> findAllDeployType() {
        List<DeployType> list = null;
        final String query = "SELECT * FROM application a ORDER BY a.sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<DeployType>();
                    while (resultSet.next()) {
                        DeployType deployType = this.loadDeployTypeFromResultSet(resultSet);
                        list.add(deployType);
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

    private DeployType loadDeployTypeFromResultSet(ResultSet rs) throws SQLException {
        String deployType = ParameterParserUtil.parseStringParam(rs.getString("deployType"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("description"), "");

        //TODO remove when working in test with mockito and autowired
        factoryDeployType = new FactoryDeployType();
        return factoryDeployType.create(deployType, description);
    }
}
