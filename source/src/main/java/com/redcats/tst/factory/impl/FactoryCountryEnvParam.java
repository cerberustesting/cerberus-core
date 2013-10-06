/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.CountryEnvParam;
import com.redcats.tst.factory.IFactoryCountryEnvParam;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class FactoryCountryEnvParam implements IFactoryCountryEnvParam {

    CountryEnvParam countryEnvParam;
    
    @Override
    public CountryEnvParam create(String system, String country, String environment, String build, String revision, String chain,
    String distribList, String eMailBodyRevision, String type, String eMailBodyChain, 
    String eMailBodyDisableEnvironment, boolean active, boolean maintenanceAct, String maintenanceStr, 
    String maintenanceEnd) {
        countryEnvParam = new CountryEnvParam();
        countryEnvParam.setSystem(system);
        countryEnvParam.setCountry(country);
        countryEnvParam.setEnvironment(environment);
        countryEnvParam.setBuild(build);
        countryEnvParam.setRevision(revision);
        countryEnvParam.setChain(chain);
        countryEnvParam.setDistribList(distribList);
        countryEnvParam.seteMailBodyRevision(eMailBodyRevision);
        countryEnvParam.setType(type);
        countryEnvParam.seteMailBodyChain(eMailBodyChain);
        countryEnvParam.seteMailBodyDisableEnvironment(eMailBodyDisableEnvironment);
        countryEnvParam.setActive(active);
        countryEnvParam.setMaintenanceAct(maintenanceAct);
        countryEnvParam.setMaintenanceStr(maintenanceStr);
        countryEnvParam.setMaintenanceEnd(maintenanceEnd);
        
        return countryEnvParam;
    }
    
}
