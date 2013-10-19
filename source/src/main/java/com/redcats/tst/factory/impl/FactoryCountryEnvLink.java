/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.CountryEnvLink;
import com.redcats.tst.factory.IFactoryCountryEnvLink;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class FactoryCountryEnvLink implements IFactoryCountryEnvLink {

    CountryEnvLink countryEnvLink;
    
    @Override
    public CountryEnvLink create(String system, String country, String environment, String systemLink, String countryLink, String environmentLink) {
        countryEnvLink = new CountryEnvLink();
        countryEnvLink.setSystem(system);
        countryEnvLink.setCountry(country);
        countryEnvLink.setEnvironment(environment);
        countryEnvLink.setSystemLink(systemLink);
        countryEnvLink.setCountryLink(countryLink);
        countryEnvLink.setEnvironmentLink(environmentLink);
        
        return countryEnvLink;
    }
    
}
