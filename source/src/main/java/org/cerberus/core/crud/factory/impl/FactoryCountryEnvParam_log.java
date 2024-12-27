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

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.CountryEnvParam_log;
import org.cerberus.core.crud.factory.IFactoryCountryEnvParam_log;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryCountryEnvParam_log implements IFactoryCountryEnvParam_log {

    @Override
    public CountryEnvParam_log create(long id, String system, String country, String environment, String build, String revision,
            int chain, String description, Timestamp datecre, String creator) {
        CountryEnvParam_log newCountryEnvParamLog = new CountryEnvParam_log();
        newCountryEnvParamLog.setId(id);
        newCountryEnvParamLog.setSystem(system);
        newCountryEnvParamLog.setCountry(country);
        newCountryEnvParamLog.setEnvironment(environment);
        newCountryEnvParamLog.setBuild(build);
        newCountryEnvParamLog.setRevision(revision);
        newCountryEnvParamLog.setChain(chain);
        newCountryEnvParamLog.setDescription(description);
        newCountryEnvParamLog.setDatecre(datecre);
        newCountryEnvParamLog.setCreator(creator);
        return newCountryEnvParamLog;
    }
    
    @Override
    public CountryEnvParam_log create(String system, String country, String environment, String build, String revision,
            int chain, String description, String creator) {
        CountryEnvParam_log newCountryEnvParamLog = new CountryEnvParam_log();
        newCountryEnvParamLog.setSystem(system);
        newCountryEnvParamLog.setCountry(country);
        newCountryEnvParamLog.setEnvironment(environment);
        newCountryEnvParamLog.setBuild(build);
        newCountryEnvParamLog.setRevision(revision);
        newCountryEnvParamLog.setChain(chain);
        newCountryEnvParamLog.setDescription(description);
        newCountryEnvParamLog.setCreator(creator);
        return newCountryEnvParamLog;
    }


}
