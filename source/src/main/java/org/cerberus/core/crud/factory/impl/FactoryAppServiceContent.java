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

import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.factory.IFactoryAppServiceContent;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * @author vertigo
 */
@Service
public class FactoryAppServiceContent implements IFactoryAppServiceContent {

    @Override
    public AppServiceContent create(String service, String key,
                                    String value, boolean isActive, int sort, String description,
                                    String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        AppServiceContent newObject = new AppServiceContent();
        newObject.setService(service);
        newObject.setKey(key);
        newObject.setValue(value);
        newObject.setActive(isActive);
        newObject.setInherited(false);
        newObject.setSort(sort);
        newObject.setDescription(description);
        newObject.setUsrCreated(usrCreated);
        newObject.setUsrModif(usrModif);
        newObject.setDateCreated(dateCreated);
        newObject.setDateModif(dateModif);
        return newObject;
    }

    @Override
    public AppServiceContent create(String service) {
        AppServiceContent newObject = new AppServiceContent();
        newObject.setService(service);
        return newObject;
    }

}
