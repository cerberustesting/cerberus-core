package com.redcats.tst.service;

import com.redcats.tst.entity.Environment;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface ICountryEnvironmentService {

    Environment loadParameters(String country, String environment, String application,
                               String browserPath, String seleniumIP, String seleniumPort, String path,
                               String devURL, String devLogin);

    public List<String[]> getEnvironmentAvailable(String test, String testCase, String country);
}
