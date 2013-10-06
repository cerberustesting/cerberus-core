/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.CountryEnvironmentDatabase;
import com.redcats.tst.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvironmentDatabaseService {

    CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String system, String country, String environment, String database) throws CerberusException;
}
