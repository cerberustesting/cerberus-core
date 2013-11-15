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

import org.cerberus.entity.CountryEnvironmentApplication;
import org.cerberus.factory.IFactoryCountryEnvironmentApplication;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryCountryEnvironmentApplication implements IFactoryCountryEnvironmentApplication {

    @Override
    public CountryEnvironmentApplication create(String system, String country, String environment, String application, String ip, String url, String urlLogin) {
        CountryEnvironmentApplication countryEnvironmentApplication = new CountryEnvironmentApplication();
        countryEnvironmentApplication.setSystem(system);
        countryEnvironmentApplication.setCountry(country);
        countryEnvironmentApplication.setEnvironment(environment);
        countryEnvironmentApplication.setApplication(application);
        countryEnvironmentApplication.setIp(ip);
        countryEnvironmentApplication.setUrl(url);
        countryEnvironmentApplication.setUrlLogin(urlLogin);
        return countryEnvironmentApplication;
    }
}
