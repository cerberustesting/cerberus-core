/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.CountryEnvironmentApplication;

/**
 *
 * @author bcivel
 */
public interface IFactoryCountryEnvironmentApplication {
    
    CountryEnvironmentApplication create(String country,String environment,String application,String ip,
            String url,String urlLogin);
}
