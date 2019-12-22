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
package org.cerberus.crud.factory.impl;

import java.sql.Timestamp;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.factory.IFactoryApplication;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryApplication implements IFactoryApplication {

    @Override
    public Application create(String application, String description
            , int sort, String type, String system, String subsystem
            , String svnurl, int poolSize, String deploytype, String mavengroupid
            , String bugTrackerUrl, String bugTrackerNewUrl,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        Application newObject = new Application();
        newObject.setApplication(application);
        newObject.setDeploytype(deploytype);
        newObject.setDescription(description);
        newObject.setMavengroupid(mavengroupid);
        newObject.setSort(sort);
        newObject.setSubsystem(subsystem);
        newObject.setSvnurl(svnurl);
        newObject.setPoolSize(poolSize);
        newObject.setSystem(system);
        newObject.setType(type);
        newObject.setBugTrackerUrl(bugTrackerUrl);
        newObject.setBugTrackerNewUrl(bugTrackerNewUrl);
        newObject.setUsrModif(usrModif);
        newObject.setUsrCreated(usrCreated);
        newObject.setDateModif(dateModif);
        newObject.setDateCreated(dateCreated);
        return newObject;
    }

    @Override
    public Application create(String application) {
        Application newObject = new Application();
        newObject.setApplication(application);
        return newObject;
    }


}
