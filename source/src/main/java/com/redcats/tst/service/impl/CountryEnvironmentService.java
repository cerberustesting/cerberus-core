package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IApplicationDAO;
import com.redcats.tst.dao.ICountryEnvParamDAO;
import com.redcats.tst.dao.ICountryEnvironmentParametersDAO;
import com.redcats.tst.entity.CountryEnvParam;
import com.redcats.tst.entity.CountryEnvironmentApplication;
import com.redcats.tst.entity.Environment;
import com.redcats.tst.entity.TCase;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryEnvironment;
import com.redcats.tst.service.ICountryEnvironmentService;
import com.redcats.tst.service.ITestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
@Service
public class CountryEnvironmentService implements ICountryEnvironmentService {

    @Autowired
    private IApplicationDAO applicationDAO;
    @Autowired
    private ICountryEnvironmentParametersDAO countryEnvironmentParametersDAO;
    @Autowired
    private ICountryEnvParamDAO countryEnvParamDAO;
    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private IFactoryEnvironment factoryEnvironment;

    @Override
    public List<String[]> getEnvironmentAvailable(String test, String testCase, String country) {
        try {
            List<String[]> list = null;

            TCase tc = this.testCaseService.findTestCaseByKey(test, testCase);

            if (tc != null) {
                list = this.countryEnvironmentParametersDAO.getEnvironmentAvailable(country, tc.getApplication());
            }

            return list;
        } catch (CerberusException ex) {
            Logger.getLogger(CountryEnvironmentService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
