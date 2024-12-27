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
import java.util.ArrayList;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.factory.IFactoryTag;
import org.springframework.stereotype.Service;

/**
 * @author vertigo17
 */
@Service
public class FactoryTag implements IFactoryTag {

    @Override
    public Tag create(long id, String tag, String description, String comment, String campaign, Timestamp dateEndQueue,
            int nbExe, int nbExeUsefull, int nbOK, int nbKO, int nbFA, int nbNA, int nbNE, int nbWE, int nbPE, int nbQU, int nbQE, int nbCA,
            int ciScore, int ciScoreThreshold, String ciResult,
            String environmentList, String countryList, String robotDecliList, String systemList, String applicationList,
            String reqEnvironmentList, String reqCountryList, String browserstackBuildHash, String lambdaTestBuild, String xRayTestExecution, String xRayURL,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        Tag newObject = new Tag();
        newObject.setId(id);
        newObject.setTag(tag);
        newObject.setDescription(description);
        newObject.setComment(comment);
        newObject.setCampaign(campaign);
        newObject.setDateEndQueue(dateEndQueue);

        newObject.setNbExe(nbExe);
        newObject.setNbExeUsefull(nbExeUsefull);
        newObject.setNbOK(nbOK);
        newObject.setNbKO(nbKO);
        newObject.setNbFA(nbFA);
        newObject.setNbNA(nbNA);
        newObject.setNbNE(nbNE);
        newObject.setNbWE(nbWE);
        newObject.setNbPE(nbPE);
        newObject.setNbQU(nbQU);
        newObject.setNbQE(nbQE);
        newObject.setNbCA(nbCA);
        newObject.setCiScore(ciScore);
        newObject.setCiScoreThreshold(ciScoreThreshold);
        newObject.setCiResult(ciResult);

        newObject.setEnvironmentList(environmentList);
        newObject.setCountryList(countryList);
        newObject.setRobotDecliList(robotDecliList);
        newObject.setSystemList(systemList);
        newObject.setApplicationList(applicationList);
        newObject.setReqCountryList(reqCountryList);
        newObject.setReqEnvironmentList(reqEnvironmentList);
        newObject.setBrowserstackBuildHash(browserstackBuildHash);
        newObject.setLambdaTestBuild(lambdaTestBuild);
        newObject.setXRayTestExecution(xRayTestExecution);
        newObject.setXRayURL(xRayURL);

        newObject.setUsrModif(usrModif);
        newObject.setUsrCreated(usrCreated);
        newObject.setDateModif(dateModif);
        newObject.setDateCreated(dateCreated);
        newObject.setExecutionsNew(new ArrayList<>());

        return newObject;
    }

    @Override
    public Tag create(String tag) {
        Tag newObject = new Tag();
        newObject.setTag(tag);
        return newObject;
    }

}
