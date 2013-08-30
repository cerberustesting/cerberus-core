package com.redcats.tst.dao;

import com.redcats.tst.entity.CountryEnvironmentApplication;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface ICountryEnvironmentParametersDAO {

    CountryEnvironmentApplication findCountryEnvironmentParameterByKey(String country, String environment, String application) throws CerberusException;

    public List<String[]> getEnvironmentAvailable(String country, String application);
}
