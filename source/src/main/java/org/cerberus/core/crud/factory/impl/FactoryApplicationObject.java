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

import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.factory.IFactoryApplicationObject;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryApplicationObject implements IFactoryApplicationObject {

    @Override
    public ApplicationObject create(int ID, String application, String object, String value, String screenshotfilename,
            String xOffset, String yOffset,
            String usrcreated, String datecreated, String usrmodif, String datemodif) {
        ApplicationObject ao = new ApplicationObject();
        ao.setID(ID);
        ao.setApplication(application);
        ao.setObject(object);
        ao.setValue(value);
        ao.setScreenshotFilename(screenshotfilename);
        ao.setXOffset(xOffset);
        ao.setYOffset(yOffset);
        ao.setUsrCreated(usrcreated);
        ao.setDateCreated(datecreated);
        ao.setUsrModif(usrmodif);
        ao.setDateModif(datemodif);
        return ao;
    }

    @Override
    public ApplicationObject create(String application, String object) {
        ApplicationObject ao = new ApplicationObject();
        ao.setApplication(application);
        ao.setObject(object);
        return ao;
    }
}
