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

import org.cerberus.core.crud.entity.Environment;
import org.cerberus.core.crud.factory.IFactoryEnvironment;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryEnvironment implements IFactoryEnvironment {

    @Override
    public Environment create(String env, String ip, String url, String urlLogin, String build, String revision, boolean active,
                              String typeApplication, String seleniumIp, String seleniumPort, String seleniumBrowser, String path,
                              boolean maintenance, String maintenanceStr, String maintenanceEnd) {

        Environment newEnvironment = new Environment();
        newEnvironment.setEnv(env);
        newEnvironment.setIp(ip);
        newEnvironment.setUrl(url);
        newEnvironment.setUrlLogin(urlLogin);
        newEnvironment.setBuild(build);
        newEnvironment.setRevision(revision);
        newEnvironment.setActive(active);
        newEnvironment.setTypeApplication(typeApplication);
        newEnvironment.setSeleniumIp(seleniumIp);
        newEnvironment.setSeleniumPort(seleniumPort);
        newEnvironment.setSeleniumBrowser(seleniumBrowser);
        newEnvironment.setPath(path);
        newEnvironment.setMaintenance(maintenance);
        newEnvironment.setMaintenanceStr(maintenanceStr);
        newEnvironment.setMaintenanceEnd(maintenanceEnd);

        return newEnvironment;
    }

}
