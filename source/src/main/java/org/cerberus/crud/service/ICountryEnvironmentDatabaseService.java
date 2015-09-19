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
package org.cerberus.crud.service;

import java.util.List;
import org.cerberus.crud.entity.CountryEnvironmentApplication;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvironmentDatabaseService {

    CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String system, String country, String environment, String database) throws CerberusException;

    /**
     * Find all countryEnvironmentDatabase by System
     * @param system
     * @return
     * @throws CerberusException 
     */
    List<CountryEnvironmentDatabase> findAll(String system) throws CerberusException;
    
    /**
     * Update countryEnvironmentDatabase
     * @param ced
     * @throws CerberusException 
     */
    void update(CountryEnvironmentDatabase ced) throws CerberusException;
    
    /**
     * Delete countryEnvironmentDatabase
     * @param ced
     * @throws CerberusException 
     */
    void delete(CountryEnvironmentDatabase ced) throws CerberusException;
    
    /**
     * Create countryEnvironmentDatabase
     * @param ced
     * @throws CerberusException 
     */
    void create(CountryEnvironmentDatabase ced) throws CerberusException;
    
    /**
     * Find List of CountryEnvironmentDatabase by Criteria
     * @param start row number of the resulset where start the List (limit(start,amount)) 
     * @param amount number of row returned
     * @param column column used for the sort (sort by column dir >become> sort by country asc)
     * @param dir asc or desc
     * @param searchTerm 
     * @param individualSearch
     * @return 
     */
    public List<CountryEnvironmentDatabase> findListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     * Find the number of CountryEnvironmentDatabase found respecting the search criteria
     * @param searchTerm
     * @return 
     */
    public Integer count(String searchTerm);

    public List<CountryEnvironmentDatabase> findListByCriteria(String system, String country, String environment)  throws CerberusException;
}
