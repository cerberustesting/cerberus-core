/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ICountryEnvLinkDAO;
import com.redcats.tst.entity.CountryEnvLink;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ICountryEnvLinkService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class CountryEnvLinkService implements ICountryEnvLinkService {

    @Autowired
    ICountryEnvLinkDAO countryEnvLinkDao;

    @Override
    public List<CountryEnvLink> findCountryEnvLinkByCriteria(String system, String country, String environment) throws CerberusException {
        return countryEnvLinkDao.findCountryEnvLinkByCriteria(system, country, environment);
    }
}
