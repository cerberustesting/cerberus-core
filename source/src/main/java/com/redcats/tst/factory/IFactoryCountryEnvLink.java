/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.CountryEnvLink;

/**
 *
 * @author bcivel
 */
public interface IFactoryCountryEnvLink {

    CountryEnvLink create(String system, String country, String environment, String systemLink, String countryLink, String environmentLink);
}
