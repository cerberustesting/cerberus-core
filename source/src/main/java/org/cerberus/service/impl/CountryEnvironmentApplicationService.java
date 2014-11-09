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
package org.cerberus.service.impl;

import java.util.List;

import org.cerberus.dao.ICountryEnvironmentParametersDAO;
import org.cerberus.entity.CountryEnvironmentApplication;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ICountryEnvironmentApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class CountryEnvironmentApplicationService implements ICountryEnvironmentApplicationService {

    @Autowired
    ICountryEnvironmentParametersDAO countryEnvironmentParametersDao;

    @Override
    public CountryEnvironmentApplication findCountryEnvironmentParameterByKey(String system, String country, String environment, String application) throws CerberusException {
        CountryEnvironmentApplication cea = this.countryEnvironmentParametersDao.findCountryEnvironmentParameterByKey(system, country, environment, application);
        return cea;
    }

    @Override
    public List<String[]> getEnvironmentAvailable(String country, String application) {
        return countryEnvironmentParametersDao.getEnvironmentAvailable(country, application);
    }

    @Override
    public List<CountryEnvironmentApplication> findCountryEnvironmentApplicationByCriteria(CountryEnvironmentApplication countryEnvironmentParameter) throws CerberusException {
        return countryEnvironmentParametersDao.findCountryEnvironmentApplicationByCriteria(countryEnvironmentParameter);
    }

    @Override
    public List<String> getDistinctEnvironmentNames() throws CerberusException {
        return countryEnvironmentParametersDao.getDistinctEnvironmentNames();
    }

    @Override
    public List<CountryEnvironmentApplication> findAll(String system) throws CerberusException {
        return countryEnvironmentParametersDao.findAll(system);
    }

    @Override
    public void update(CountryEnvironmentApplication cea) throws CerberusException {
        countryEnvironmentParametersDao.update(cea);
    }

    @Override
    public void delete(CountryEnvironmentApplication cea) throws CerberusException {
        countryEnvironmentParametersDao.delete(cea);
    }

    @Override
    public void create(CountryEnvironmentApplication cea) throws CerberusException {
        countryEnvironmentParametersDao.create(cea);
    }

    @Override
    public List<CountryEnvironmentApplication> findListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer count(String searchTerm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
