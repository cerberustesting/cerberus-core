package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ICountryEnvLinkDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.CountryEnvLink;
import com.redcats.tst.factory.IFactoryCountryEnvLink;
import com.redcats.tst.log.MyLogger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 2.0.0
 */
@Repository
public class CountryEnvLinkDAO implements ICountryEnvLinkDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCountryEnvLink factoryCountryEnvLink;

    @Override
    public List<CountryEnvLink> findCountryEnvLinkByCriteria(String system, String country, String environment) {
        List<CountryEnvLink> result = null;
        CountryEnvLink resultData;
        final String query = "SELECT * FROM CountryEnvLink WHERE `system` = ? and country = ? and environment = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                preStat.setString(2, country);
                preStat.setString(3, environment);

                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<CountryEnvLink>();
                try {
                    while (resultSet.next()) {
                        String systemLink = resultSet.getString("systemLink");
                        String countryLink = resultSet.getString("countryLink");
                        String environmentLink = resultSet.getString("environmentLink");
                        resultData = factoryCountryEnvLink.create(system, country, environment, systemLink, countryLink, environmentLink);
                        result.add(resultData);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }
}
