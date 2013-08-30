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
    public Environment loadParameters(String country, String environment, String application, String browserPath,
                                      String seleniumIP, String seleniumPort, String path, String devURL, String devLogin) {
        try {
            Environment env = null;

            CountryEnvParam countEnvParam = this.countryEnvParamDAO.findCountryEnvParamByKey(country, environment);

            CountryEnvironmentApplication cea = this.countryEnvironmentParametersDAO.findCountryEnvironmentParameterByKey(country, environment, application);
            try {
                String ip = environment.equalsIgnoreCase("DEV") ? "" : cea.getIp();
                String url = environment.equalsIgnoreCase("DEV") ? devURL : cea.getUrl();
                String urlLogin = environment.equalsIgnoreCase("DEV") ? devLogin : cea.getUrlLogin();
                String build = countEnvParam.getBuild();
                String revision = countEnvParam.getRevision();
                boolean active = countEnvParam.isActive();
                String typeApplication;
                typeApplication = this.applicationDAO.findApplicationByKey(application).getType();
                boolean maintenance = countEnvParam.isMaintenanceAct();
                String maintenanceStr = countEnvParam.getMaintenanceStr();
                String maintenanceEnd = countEnvParam.getMaintenanceEnd();

                env = factoryEnvironment.create(environment, ip, url, urlLogin, build, revision, active, typeApplication, seleniumIP,
                        seleniumPort, browserPath, path, maintenance, maintenanceStr, maintenanceEnd);
            } catch (CerberusException ex) {
                Logger.getLogger(CountryEnvironmentService.class.getName()).log(Level.SEVERE, null, ex);
            }

            return env;
        } catch (CerberusException ex) {
            Logger.getLogger(CountryEnvironmentService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

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
