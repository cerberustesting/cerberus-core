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
package org.cerberus.core.crud.factory;

import org.cerberus.core.crud.entity.CountryEnvParam;

/**
 *
 * @author bcivel
 */
public interface IFactoryCountryEnvParam {

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param description
     * @param build
     * @param revision
     * @param chain
     * @param distribList
     * @param eMailBodyRevision
     * @param type
     * @param eMailBodyChain
     * @param eMailBodyDisableEnvironment
     * @param active
     * @param maintenanceAct
     * @param maintenanceStr
     * @param maintenanceEnd
     * @param envGp
     * @return
     */
    CountryEnvParam create(String system, String country, String environment, String description, String build, String revision, String chain,
            String distribList, String eMailBodyRevision, String type, String eMailBodyChain,
            String eMailBodyDisableEnvironment, boolean active, boolean maintenanceAct, String maintenanceStr, String maintenanceEnd, String envGp);
    
    /**
     *
     * @param system
     * @param country
     * @param active
     * @return
     */
    CountryEnvParam create(String system, String country, boolean active);

    /**
     *
     * @param system
     * @param country
     * @param env
     * @return
     */
    CountryEnvParam create(String system, String country, String env);
}
