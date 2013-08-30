/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.CountryEnvironmentDatabase;

/**
 *
 * @author bcivel
 */
public interface IFactoryCountryEnvironmentDatabase{
    
    CountryEnvironmentDatabase create(String database,String environment,String country,
            String connectionPoolName);
}
