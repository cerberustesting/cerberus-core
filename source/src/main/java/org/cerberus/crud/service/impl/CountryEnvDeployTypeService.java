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

package org.cerberus.crud.service.impl;

import org.cerberus.crud.dao.ICountryEnvDeployTypeDAO;
import org.cerberus.crud.service.ICountryEnvDeployTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryEnvDeployTypeService implements ICountryEnvDeployTypeService {

    @Autowired
    private ICountryEnvDeployTypeDAO countryEnvDeployTypeDAO;

    @Override
    public List<String> findJenkinsAgentByKey(String system, String country, String env, String deploy) {
        return this.countryEnvDeployTypeDAO.findJenkinsAgentByKey(system, country, env, deploy);
    }
}
