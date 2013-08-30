package com.redcats.tst.dao;

import com.redcats.tst.entity.CountryEnvironmentDatabase;
import com.redcats.tst.exception.CerberusException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface ICountryEnvironmentDatabaseDAO {

    CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String database, String environment, String countryCode) throws CerberusException;
}
