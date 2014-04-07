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
import org.cerberus.dao.ICountryEnvironmentParametersDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.CountryEnvironmentApplication;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCountryEnvironmentApplication;
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
public class CountryEnvironmentParametersDAO implements ICountryEnvironmentParametersDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCountryEnvironmentApplication factoryCountryEnvironmentApplication;

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
    public CountryEnvironmentApplication findCountryEnvironmentParameterByKey(String system, String country, String environment, String application) throws CerberusException {
        boolean throwException = false;
        CountryEnvironmentApplication result = null;
        final String query = "SELECT * FROM countryenvironmentparameters WHERE `system` = ? AND country = ? AND environment = ? AND application = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                preStat.setString(2, country);
                preStat.setString(3, environment);
                preStat.setString(4, application);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    String ip = "";
                    String url = "";
                    String urlLogin = "";
                    if (resultSet.first()) {
                        ip = resultSet.getString("IP");
                        url = resultSet.getString("URL");
                        urlLogin = resultSet.getString("URLLOGIN");
                        result = factoryCountryEnvironmentApplication.create(system, country, environment, application, ip, url, urlLogin);
                    } else {
                        throwException = true;
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
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public List<String[]> getEnvironmentAvailable(String country, String application) {
        List<String[]> list = null;
        final String query = "SELECT ce.Environment Environment, ce.Build Build, ce.Revision Revision "
                + "FROM countryenvironmentparameters cea, countryenvparam ce, invariant i "
                + "WHERE ce.system = cea.system AND ce.country = cea.country AND ce.environment = cea.environment "
                + "AND cea.Application = ? AND cea.country= ? "
                + "AND ce.active='Y' AND i.idname = 'ENVIRONMENT' AND i.Value = ce.Environment "
                + "ORDER BY i.sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, application);
                preStat.setString(2, country);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String[]>();

                    while (resultSet.next()) {
                        String[] array = new String[3];
                        array[0] = resultSet.getString("Environment");
                        array[1] = resultSet.getString("Build");
                        array[2] = resultSet.getString("Revision");


                        list.add(array);
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
        return list;
    }
}
