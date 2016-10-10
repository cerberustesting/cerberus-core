/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
            , String svnurl, String deploytype, String mavengroupid
            , String bugTrackerUrl, String bugTrackerNewUrl) {
        Application newApplication = new Application();
        newApplication.setApplication(application);
        newApplication.setDeploytype(deploytype);
        newApplication.setDescription(description);
        newApplication.setMavengroupid(mavengroupid);
        newApplication.setSort(sort);
        newApplication.setSubsystem(subsystem);
        newApplication.setSvnurl(svnurl);
        newApplication.setSystem(system);
        newApplication.setType(type);
        newApplication.setBugTrackerUrl(bugTrackerUrl);
        newApplication.setBugTrackerNewUrl(bugTrackerNewUrl);
        return newApplication;
    }

    @Override
    public Application create(String application) {
        Application newApplication = new Application();
        newApplication.setApplication(application);
        return newApplication;
    }


}
