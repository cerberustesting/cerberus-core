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

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.Tag;

/**
 * @author vertigo
 */
public interface IFactoryTag {

    /**
     *
     * @param id
     * @param tag
     * @param description
     * @param comment
     * @param campaign
     * @param dateEndQueue
     * @param nbExe
     * @param nbExeUsefull
     * @param nbOK
     * @param usrCreated
     * @param dateCreated
     * @param nbKO
     * @param nbFA
     * @param usrModif
     * @param nbPE
     * @param nbQU
     * @param nbQE
     * @param nbCA
     * @param nbNA
     * @param nbWE
     * @param nbNE
     * @param environmentList
     * @param countryList
     * @param robotDecliList
     * @param systemList
     * @param applicationList
     * @param reqEnvironmentList
     * @param reqCountryList
     * @param BrowserstackBuildHash
     * @param lambdaTestBuild
     * @param xRayTestExecution
     * @param xRayURL
     * @param dateModif
     * @param ciResult
     * @param ciScoreThreshold
     * @param ciScore
     * @return
     */
    Tag create(long id, String tag, String description, String comment, String campaign, Timestamp dateEndQueue,
            int nbExe, int nbExeUsefull, int nbOK, int nbKO, int nbFA, int nbNA, int nbNE, int nbWE, int nbPE, int nbQU, int nbQE, int nbCA,
            int ciScore, int ciScoreThreshold, String ciResult,
            String environmentList, String countryList, String robotDecliList, String systemList, String applicationList,
            String reqEnvironmentList, String reqCountryList, String BrowserstackBuildHash, String lambdaTestBuild, String xRayTestExecution, String xRayURL,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif);

    /**
     * Return Tag object with only tag defined
     *
     * @param tag
     * @return
     */
    Tag create(String tag);

}
