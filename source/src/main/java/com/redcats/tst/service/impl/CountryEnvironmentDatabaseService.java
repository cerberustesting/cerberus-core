/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ICountryEnvironmentDatabaseDAO;
import com.redcats.tst.entity.CountryEnvironmentDatabase;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ICountryEnvironmentDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class CountryEnvironmentDatabaseService implements ICountryEnvironmentDatabaseService {

    @Autowired
    private ICountryEnvironmentDatabaseDAO countryEnvironmentDatabaseDao;

    @Override
    public CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String country, String environment, String database) throws CerberusException {
        return countryEnvironmentDatabaseDao.findCountryEnvironmentDatabaseByKey(database, environment, country);
    }
}
