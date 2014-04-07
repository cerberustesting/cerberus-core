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

import org.apache.log4j.Level;
import org.cerberus.dao.ICountryEnvParamDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.CountryEnvParam;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCountryEnvParam;
import org.cerberus.log.MyLogger;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author bcivel
 */
@Repository
public class CountryEnvParamDAO implements ICountryEnvParamDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCountryEnvParam factoryCountryEnvParam;

    @Override
    public CountryEnvParam findCountryEnvParamByKey(String system, String country, String environment) throws CerberusException {
        CountryEnvParam result = null;
        boolean throwex = false;
        StringBuilder query = new StringBuilder();
        query.append("SELECT `system`, country, environment, Build, Revision,chain, distriblist, eMailBodyRevision, type,eMailBodyChain, eMailBodyDisableEnvironment,  active, maintenanceact, ");
        query.append("maintenancestr, maintenanceend FROM countryenvparam WHERE `system` = ? AND country = ? AND environment = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, system);
                preStat.setString(2, country);
                preStat.setString(3, environment);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        result = this.loadCountryEnvParamFromResultSet(resultSet);
                    } else {
                        throwex = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwex) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    private CountryEnvParam loadCountryEnvParamFromResultSet(ResultSet resultSet) throws SQLException {
        String system = resultSet.getString("System");
        String count = resultSet.getString("Country");
        String env = resultSet.getString("Environment");
        String build = resultSet.getString("Build");
        String revision = resultSet.getString("Revision");
        String chain = resultSet.getString("chain");
        String distribList = resultSet.getString("distribList");
        String eMailBodyRevision = resultSet.getString("eMailBodyRevision");
        String type = resultSet.getString("type");
        String eMailBodyChain = resultSet.getString("eMailBodyChain");
        String eMailBodyDisableEnvironment = resultSet.getString("eMailBodyDisableEnvironment");
        boolean active = StringUtil.parseBoolean(resultSet.getString("active"));
        boolean maintenanceAct = StringUtil.parseBoolean(resultSet.getString("maintenanceact"));
        String maintenanceStr = resultSet.getString("maintenancestr");
        String maintenanceEnd = resultSet.getString("maintenanceend");
        return factoryCountryEnvParam.create(system, count, env, build, revision, chain, distribList, eMailBodyRevision,
                type, eMailBodyChain, eMailBodyDisableEnvironment, active, maintenanceAct, maintenanceStr, maintenanceEnd);
    }
}
