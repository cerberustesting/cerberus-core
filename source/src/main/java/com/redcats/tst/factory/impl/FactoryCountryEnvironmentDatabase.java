/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.CountryEnvironmentDatabase;
import com.redcats.tst.factory.IFactoryCountryEnvironmentDatabase;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryCountryEnvironmentDatabase implements IFactoryCountryEnvironmentDatabase {

    @Override
    public CountryEnvironmentDatabase create(String database, String environment, String country, String connectionPoolName) {
        CountryEnvironmentDatabase ced = new CountryEnvironmentDatabase();
        ced.setCountry(country);
        ced.setEnvironment(environment);
        ced.setDatabase(database);
        ced.setConnectionPoolName(connectionPoolName);
        return ced;
    }

}
