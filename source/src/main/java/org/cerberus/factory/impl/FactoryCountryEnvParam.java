/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.factory.impl;

import org.cerberus.entity.CountryEnvParam;
import org.cerberus.factory.IFactoryCountryEnvParam;
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
