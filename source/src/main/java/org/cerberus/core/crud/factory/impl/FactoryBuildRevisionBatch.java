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
import org.cerberus.core.crud.entity.BuildRevisionBatch;
import org.cerberus.core.crud.factory.IFactoryBuildRevisionBatch;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryBuildRevisionBatch implements IFactoryBuildRevisionBatch {

    @Override
    public BuildRevisionBatch create(long id, String system, String country, String environment, String build, String revision,
            String batch, Timestamp dateBatch) {
        BuildRevisionBatch newObject = new BuildRevisionBatch();
        newObject.setId(id);
        newObject.setSystem(system);
        newObject.setCountry(country);
        newObject.setEnvironment(environment);
        newObject.setBuild(build);
        newObject.setRevision(revision);
        newObject.setBatch(batch);
        newObject.setDateBatch(dateBatch);
        return newObject;
    }
    
    @Override
    public BuildRevisionBatch create(String system, String country, String environment, String build, String revision,
            String batch) {
        BuildRevisionBatch newObject = new BuildRevisionBatch();
        newObject.setSystem(system);
        newObject.setCountry(country);
        newObject.setEnvironment(environment);
        newObject.setBuild(build);
        newObject.setRevision(revision);
        newObject.setBatch(batch);
        return newObject;
    }


}
