package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ICountryEnvironmentParametersDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.CountryEnvironmentApplication;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryCountryEnvironmentApplication;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
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
                    MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, exception.toString());
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
                + "AND ce.active='Y' AND i.id = 5 AND i.Value = ce.Environment "
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
                    MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, exception.toString());
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
