/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.crud.factory.impl;

import org.cerberus.core.crud.entity.CountryEnvDeployType;
import org.cerberus.core.crud.factory.IFactoryCountryEnvDeployType;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class FactoryCountryEnvDeployType implements IFactoryCountryEnvDeployType {

    CountryEnvDeployType countryEnvDeployType;

    @Override
    public CountryEnvDeployType create(String system, String country, String environment, String deployType, String jenkinsAgent) {
        countryEnvDeployType = new CountryEnvDeployType();
        countryEnvDeployType.setSystem(system);
        countryEnvDeployType.setCountry(country);
        countryEnvDeployType.setEnvironment(environment);
        countryEnvDeployType.setDeployType(deployType);
        countryEnvDeployType.setJenkinsAgent(jenkinsAgent);

        return countryEnvDeployType;
    }

}
