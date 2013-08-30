/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ICountryEnvironmentParametersDAO;
import com.redcats.tst.entity.CountryEnvironmentApplication;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ICountryEnvironmentApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bcivel
 */
@Service
public class CountryEnvironmentApplicationService implements ICountryEnvironmentApplicationService {

    @Autowired
    ICountryEnvironmentParametersDAO countryEnvironmentParametersDao;

    @Override
    public CountryEnvironmentApplication findCountryEnvironmentParameterByKey(String country, String environment, String application) throws CerberusException {
        CountryEnvironmentApplication cea = this.countryEnvironmentParametersDao.findCountryEnvironmentParameterByKey(country, environment, application);
        return cea;
    }

    @Override
    public List<String[]> getEnvironmentAvailable(String country, String application) {
        return countryEnvironmentParametersDao.getEnvironmentAvailable(country, application);
    }
}
