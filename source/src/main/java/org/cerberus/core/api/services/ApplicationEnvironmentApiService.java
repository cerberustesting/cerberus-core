/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.core.api.services;

import java.security.Principal;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.InvalidRequestException;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Service;

/**
 * @author mlombard
 */
@AllArgsConstructor
@Service
public class ApplicationEnvironmentApiService {

    ICountryEnvironmentParametersService applicationEnvironmentService;
    private static final Logger LOG = LogManager.getLogger(ApplicationEnvironmentApiService.class);

    public CountryEnvironmentParameters readByKey(String system, String application, String country, String environment) throws CerberusException {
        AnswerItem<CountryEnvironmentParameters> cep = this.applicationEnvironmentService.readByKey(system, country, environment, application);
        if (cep.getItem() == null) {
            LOG.debug("not exist.");
            throw new EntityNotFoundException(CountryEnvironmentParameters.class, "system", system, "application", application, "country", country, "environment", environment);
        }
        LOG.debug("Exist.");
//        CountryEnvironmentParameters applicationEnv = this.applicationEnvironmentService.convert();
//        LOG.debug(applicationEnv.toString());
//        if (applicationEnv == null) {
//            throw new EntityNotFoundException(Application.class, "application environment", system, country, environment, application);
//        }
        return cep.getItem();
    }

    public CountryEnvironmentParameters updateApplicationEnvironmentPATCH(String system, String applicationId, String countryId, String environmentId,
            CountryEnvironmentParameters newApplicationEnvironment, Principal principal, String login) throws CerberusException {

        AnswerItem<CountryEnvironmentParameters> cep = this.applicationEnvironmentService.readByKey(system, countryId, environmentId, applicationId);
        if (cep.getItem() == null) {
            LOG.debug("not exist.");
            throw new EntityNotFoundException(CountryEnvironmentParameters.class, "system", system, "application", applicationId, "country", countryId, "environment", environmentId);
        }
        LOG.debug("Exist.");

        newApplicationEnvironment.setUsrModif(login != null ? login : "");

        newApplicationEnvironment.setSystem(system);
        newApplicationEnvironment.setCountry(countryId);
        newApplicationEnvironment.setEnvironment(environmentId);
        newApplicationEnvironment.setApplication(applicationId);

        newApplicationEnvironment.setVar1(newApplicationEnvironment.getVar1() == null ? cep.getItem().getVar1() : newApplicationEnvironment.getVar1());
        newApplicationEnvironment.setVar2(newApplicationEnvironment.getVar2() == null ? cep.getItem().getVar2() : newApplicationEnvironment.getVar2());
        newApplicationEnvironment.setVar3(newApplicationEnvironment.getVar3() == null ? cep.getItem().getVar3() : newApplicationEnvironment.getVar3());
        newApplicationEnvironment.setVar4(newApplicationEnvironment.getVar4() == null ? cep.getItem().getVar4() : newApplicationEnvironment.getVar4());

        newApplicationEnvironment.setSecret1(newApplicationEnvironment.getSecret1()== null ? cep.getItem().getSecret1(): newApplicationEnvironment.getSecret1());
        newApplicationEnvironment.setSecret2(newApplicationEnvironment.getSecret2()== null ? cep.getItem().getSecret2(): newApplicationEnvironment.getSecret2());
        
        newApplicationEnvironment.setIp(newApplicationEnvironment.getIp() == null ? cep.getItem().getIp() : newApplicationEnvironment.getIp());
        newApplicationEnvironment.setDomain(newApplicationEnvironment.getDomain() == null ? cep.getItem().getDomain() : newApplicationEnvironment.getDomain());
        newApplicationEnvironment.setUrl(newApplicationEnvironment.getUrl() == null ? cep.getItem().getUrl() : newApplicationEnvironment.getUrl());
        newApplicationEnvironment.setUrlLogin(newApplicationEnvironment.getUrlLogin() == null ? cep.getItem().getUrlLogin() : newApplicationEnvironment.getUrlLogin());

        newApplicationEnvironment.setPoolSize(0 == newApplicationEnvironment.getPoolSize() ? cep.getItem().getPoolSize() : newApplicationEnvironment.getPoolSize());

        applicationEnvironmentService.update(newApplicationEnvironment);

        return this.applicationEnvironmentService.readByKey(system, countryId, environmentId, applicationId).getItem();
    }

    public CountryEnvironmentParameters updateApplicationEnvironmentPUT(String system, String applicationId, String countryId, String environmentId,
            CountryEnvironmentParameters newApplicationEnvironment, Principal principal, String login) throws CerberusException {

        AnswerItem<CountryEnvironmentParameters> cep = this.applicationEnvironmentService.readByKey(system, countryId, environmentId, applicationId);
        if (cep.getItem() == null) {
            LOG.debug("not exist.");
            throw new EntityNotFoundException(CountryEnvironmentParameters.class, "system", system, "application", applicationId, "country", countryId, "environment", environmentId);
        }
        LOG.debug("Exist.");

        newApplicationEnvironment.setUsrModif(login != null ? login : "");
        
        newApplicationEnvironment.setSystem(system);
        newApplicationEnvironment.setCountry(countryId);
        newApplicationEnvironment.setEnvironment(environmentId);
        newApplicationEnvironment.setApplication(applicationId);

        try {
            applicationEnvironmentService.convert(applicationEnvironmentService.update(newApplicationEnvironment));
        } catch (CerberusException e) {
            throw new InvalidRequestException("Missing data from Entity resulting : " + e.getMessage());
        }

        return this.applicationEnvironmentService.readByKey(system, countryId, environmentId, applicationId).getItem();
    }

}
