/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.CountryEnvironmentApplication;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvironmentApplicationService {
    
    CountryEnvironmentApplication findCountryEnvironmentParameterByKey(String country, String environment, String application) throws CerberusException;

    public List<String[]> getEnvironmentAvailable(String country, String application);
}
