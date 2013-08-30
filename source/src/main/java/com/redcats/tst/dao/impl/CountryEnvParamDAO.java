/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ICountryEnvParamDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.CountryEnvParam;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryCountryEnvParam;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.util.StringUtil;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public CountryEnvParam findCountryEnvParamByKey(String country, String environment) throws CerberusException {
        CountryEnvParam result = null;
        boolean throwex = false;
        StringBuilder query = new StringBuilder();
        query.append("SELECT country, environment, Build, Revision,chain, distriblist, eMailBodyRevision, type,eMailBodyChain, eMailBodyDisableEnvironment,  active, maintenanceact, ");
        query.append("maintenancestr, maintenanceend FROM countryenvparam WHERE country = ? AND environment = ?");

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query.toString());
            preStat.setString(1, country);
            preStat.setString(2, environment);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        result = this.loadCountryEnvParamFromResultSet(resultSet);
                    } else {
                        throwex = true;
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
            this.databaseSpring.disconnect();
        }
        if (throwex) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    private CountryEnvParam loadCountryEnvParamFromResultSet(ResultSet resultSet) throws SQLException {
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
        return factoryCountryEnvParam.create(count, env, build, revision, chain, distribList, eMailBodyRevision,
                type, eMailBodyChain, eMailBodyDisableEnvironment, active, maintenanceAct, maintenanceStr, maintenanceEnd);
    }
}
