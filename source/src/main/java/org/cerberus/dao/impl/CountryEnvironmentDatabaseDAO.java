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
import org.cerberus.dao.ICountryEnvironmentDatabaseDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.CountryEnvironmentDatabase;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCountryEnvironmentDatabase;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
@Repository
public class CountryEnvironmentDatabaseDAO implements ICountryEnvironmentDatabaseDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCountryEnvironmentDatabase factoryCountryEnvironmentDatabase;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @param variable Description text text text.
     * @return Description text text text.
     */
    @Override
    public CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String system, String country, String environment, String database) throws CerberusException {
        CountryEnvironmentDatabase result = null;
        final String query = "SELECT * FROM countryenvironmentdatabase ced WHERE ced.database = ? AND ced.environment = ? AND ced.country = ? AND ced.system = ?";
        boolean throwEx = false;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, database);
                preStat.setString(2, environment);
                preStat.setString(3, country);
                preStat.setString(4, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        String connectionPoolName = resultSet.getString("ConnectionPoolName");
                        result = factoryCountryEnvironmentDatabase.create(database, environment, country, connectionPoolName);
                    } else {
                        throwEx = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CountryEnvironmentDatabaseDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CountryEnvironmentDatabaseDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CountryEnvironmentDatabaseDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CountryEnvironmentDatabaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
        return result;
    }
}
