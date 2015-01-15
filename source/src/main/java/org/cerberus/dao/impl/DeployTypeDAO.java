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
import org.cerberus.dao.IDeployTypeDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.DeployType;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryDeployType;
import org.cerberus.factory.impl.FactoryDeployType;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
        final String query = "SELECT * FROM deploytype a WHERE a.deploytype = ? ";

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
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
        final String query = "SELECT * FROM deploytype";

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
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
