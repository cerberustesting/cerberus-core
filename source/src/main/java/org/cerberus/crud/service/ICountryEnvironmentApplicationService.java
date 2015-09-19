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
import org.cerberus.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvironmentApplicationService {
    
    CountryEnvironmentApplication findCountryEnvironmentParameterByKey(String system, String country, String environment, String application) throws CerberusException;

    public List<String[]> getEnvironmentAvailable(String country, String application);
    
    List<CountryEnvironmentApplication> findCountryEnvironmentApplicationByCriteria(CountryEnvironmentApplication countryEnvironmentParameter) throws CerberusException;

    public List<String> getDistinctEnvironmentNames() throws CerberusException;
    
    /**
     * Find all countryEnvironmentApplication by System
     * @param system
     * @return
     * @throws CerberusException 
     */
    List<CountryEnvironmentApplication> findAll(String system) throws CerberusException;
    
    /**
     * Update countryEnvironmentApplication
     * @param cea
     * @throws CerberusException 
     */
    void update(CountryEnvironmentApplication cea) throws CerberusException;
    
    /**
     * Delete countryEnvironmentApplication
     * @param cea
     * @throws CerberusException 
     */
    void delete(CountryEnvironmentApplication cea) throws CerberusException;
    
    /**
     * Create countryEnvironmentApplication
     * @param cea
     * @throws CerberusException 
     */
    void create(CountryEnvironmentApplication cea) throws CerberusException;
    
    /**
     * Find List of CountryEnvironmentApplication by Criteria
     * @param start row number of the resulset where start the List (limit(start,amount)) 
     * @param amount number of row returned
     * @param column column used for the sort (sort by column dir >become> sort by country asc)
     * @param dir asc or desc
     * @param searchTerm 
     * @param individualSearch
     * @return 
     */
    public List<CountryEnvironmentApplication> findListByCriteria(String system, String country, String env, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     * Find the number of CountryEnvironmentApplication found respecting the search criteria
     * @param searchTerm
     * @param inds
     * @return 
     */
    public Integer countPerCriteria(String searchTerm, String inds);

    public List<CountryEnvironmentApplication> findListByCriteria(String system, String country, String environment);
}
