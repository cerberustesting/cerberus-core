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
package org.cerberus.core.api.services;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.stereotype.Service;

/**
 * @author mlombard
 */
@AllArgsConstructor
@Service
public class ApplicationApiService {

    IApplicationService applicationService;
    private static final Logger LOG = LogManager.getLogger(ApplicationApiService.class);

    public Application readByKey(String idApplication) throws CerberusException {
        Application application = null;
        application = this.applicationService.convert(this.applicationService.readByKey(idApplication));
        if (application == null) {
            throw new EntityNotFoundException(Application.class, "application", idApplication);
        }
        return application;
    }

    public Application readByKeyWithDependency(String idApplication) throws CerberusException {
        Application application = null;
        application = this.applicationService.readByKeyWithDependency(idApplication);
        if (application == null) {
            throw new EntityNotFoundException(Application.class, "application", idApplication);
        }
        return application;
    }

}
