/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ICountryEnvParamDAO;
import com.redcats.tst.entity.CountryEnvParam;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ICountryEnvParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class CountryEnvParamService implements ICountryEnvParamService {

    @Autowired
    ICountryEnvParamDAO countryEnvParamDao;

    @Override
    public CountryEnvParam findCountryEnvParamByKey(String system, String country, String environment) throws CerberusException {
        return countryEnvParamDao.findCountryEnvParamByKey(system, country, environment);
    }
}
