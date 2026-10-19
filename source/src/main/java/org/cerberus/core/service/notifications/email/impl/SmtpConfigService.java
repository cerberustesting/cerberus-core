/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.service.notifications.email.impl;

import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.notifications.email.ISmtpConfigService;
import org.cerberus.core.service.notifications.email.entity.SmtpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmtpConfigService implements ISmtpConfigService {

    @Autowired
    private IParameterService parameterService;

    @Override
    public SmtpConfig read(String system) throws CerberusException {
        return new SmtpConfig(
                parameterService.findParameterByKey("cerberus_smtp_host", system).getValue(),
                Integer.parseInt(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue()),
                parameterService.findParameterByKey("cerberus_smtp_username", system).getValue(),
                parameterService.findParameterByKey("cerberus_smtp_password", system).getValue(),
                parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true));
    }
}
