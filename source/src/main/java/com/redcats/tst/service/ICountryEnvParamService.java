/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.CountryEnvParam;
import com.redcats.tst.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvParamService {

    CountryEnvParam findCountryEnvParamByKey(String country, String environment) throws CerberusException;
}
