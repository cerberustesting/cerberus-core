/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.dao;

import java.util.List;

import org.cerberus.entity.CountryEnvironmentApplication;
import org.cerberus.exception.CerberusException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface ICountryEnvironmentParametersDAO {

    CountryEnvironmentApplication findCountryEnvironmentParameterByKey(String system, String country, String environment, String application) throws CerberusException;

    List<String[]> getEnvironmentAvailable(String country, String application);
    
    List<CountryEnvironmentApplication> findCountryEnvironmentApplicationByCriteria(CountryEnvironmentApplication countryEnvironmentParameter) throws CerberusException;

    List<String> getDistinctEnvironmentNames() throws CerberusException;
    
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

}
