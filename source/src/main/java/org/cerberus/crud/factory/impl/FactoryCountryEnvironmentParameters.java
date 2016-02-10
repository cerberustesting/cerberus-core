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
    public CountryEnvironmentParameters create(String system, String country, String environment, String application, String ip, String domain, String url, String urlLogin) {
        CountryEnvironmentParameters object = new CountryEnvironmentParameters();
        object.setSystem(system);
        object.setCountry(country);
        object.setEnvironment(environment);
        object.setApplication(application);
        object.setIp(ip);
        object.setDomain(domain);
        object.setUrl(url);
        object.setUrlLogin(urlLogin);
        return object;
    }
}
