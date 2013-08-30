/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao;

import com.redcats.tst.entity.CountryEnvParam;
import com.redcats.tst.exception.CerberusException;

/**
 * @author bcivel
 */
public interface ICountryEnvParamDAO {

    CountryEnvParam findCountryEnvParamByKey(String country, String environment) throws CerberusException;
}
