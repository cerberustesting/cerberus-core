/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.factory.impl;

import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.springframework.stereotype.Service;
import org.cerberus.crud.factory.IFactoryCountryEnvironmentParameters;

/**
 * @author bcivel
 */
@Service
public class FactoryCountryEnvironmentParameters implements IFactoryCountryEnvironmentParameters {

    @Override
    public CountryEnvironmentParameters create(String system, String country, String environment, String application, String ip, String domain, String url, String urlLogin, String var1, String var2, String var3, String var4, int poolSize, String mobileActivity, String mobilePackage) {
        CountryEnvironmentParameters object = new CountryEnvironmentParameters();
        object.setSystem(system);
        object.setCountry(country);
        object.setEnvironment(environment);
        object.setApplication(application);
        object.setIp(ip);
        object.setDomain(domain);
        object.setUrl(url);
        object.setUrlLogin(urlLogin);
        object.setVar1(var1);
        object.setVar2(var2);
        object.setVar3(var3);
        object.setVar4(var4);
        object.setPoolSize(poolSize);
        object.setMobileActivity(mobileActivity);
        object.setMobilePackage(mobilePackage);
        return object;
    }
}
