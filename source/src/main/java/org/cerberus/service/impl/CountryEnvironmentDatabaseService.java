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
import org.cerberus.dao.ICountryEnvironmentDatabaseDAO;
import org.cerberus.entity.CountryEnvironmentDatabase;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ICountryEnvironmentDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class CountryEnvironmentDatabaseService implements ICountryEnvironmentDatabaseService {

    @Autowired
    private ICountryEnvironmentDatabaseDAO countryEnvironmentDatabaseDao;

    @Override
    public CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String system, String country, String environment, String database) throws CerberusException {
        return countryEnvironmentDatabaseDao.findCountryEnvironmentDatabaseByKey(system, country, environment, database);
    }

    @Override
    public List<CountryEnvironmentDatabase> findAll(String system) throws CerberusException {
        return countryEnvironmentDatabaseDao.findAll(system);
    }

    @Override
    public void update(CountryEnvironmentDatabase ced) throws CerberusException {
        countryEnvironmentDatabaseDao.update(ced);
    }

    @Override
    public void delete(CountryEnvironmentDatabase ced) throws CerberusException {
        countryEnvironmentDatabaseDao.delete(ced);
    }

    @Override
    public void create(CountryEnvironmentDatabase ced) throws CerberusException {
        countryEnvironmentDatabaseDao.create(ced);
    }

    @Override
    public List<CountryEnvironmentDatabase> findListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer count(String searchTerm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
