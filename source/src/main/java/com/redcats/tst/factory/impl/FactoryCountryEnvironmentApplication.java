/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.CountryEnvironmentApplication;
import com.redcats.tst.factory.IFactoryCountryEnvironmentApplication;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryCountryEnvironmentApplication implements IFactoryCountryEnvironmentApplication {

    @Override
    public CountryEnvironmentApplication create(String country, String environment, String application, String ip, String url, String urlLogin) {
        CountryEnvironmentApplication countryEnvironmentApplication = new CountryEnvironmentApplication();
        countryEnvironmentApplication.setCountry(country);
        countryEnvironmentApplication.setEnvironment(environment);
        countryEnvironmentApplication.setApplication(application);
        countryEnvironmentApplication.setIp(ip);
        countryEnvironmentApplication.setUrl(url);
        countryEnvironmentApplication.setUrlLogin(urlLogin);

        return countryEnvironmentApplication;
    }

}
