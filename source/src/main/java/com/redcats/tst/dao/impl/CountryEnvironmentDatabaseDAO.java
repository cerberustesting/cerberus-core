package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ICountryEnvironmentDatabaseDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.CountryEnvironmentDatabase;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryCountryEnvironmentDatabase;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String database, String environment, String country) throws CerberusException {
        CountryEnvironmentDatabase result = null;
        final String query = "SELECT * FROM countryenvironmentdatabase ced WHERE ced.database = ? AND ced.environment = ? AND ced.country = ?";
        boolean throwEx=false;
        
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, database);
            preStat.setString(2, environment);
            preStat.setString(3, country);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        String connectionPoolName = resultSet.getString("ConnectionPoolName");
                        result = factoryCountryEnvironmentDatabase.create(database, environment, country, connectionPoolName);
                    }else{
                        throwEx=true;
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
            this.databaseSpring.disconnect();
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
        return result;
    }
}
