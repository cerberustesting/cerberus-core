/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao;

import com.redcats.tst.entity.CountryEnvLink;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 * @author bcivel
 */
public interface ICountryEnvLinkDAO {

    List<CountryEnvLink> findCountryEnvLinkByCriteria(String system, String country, String environment) throws CerberusException;
}
