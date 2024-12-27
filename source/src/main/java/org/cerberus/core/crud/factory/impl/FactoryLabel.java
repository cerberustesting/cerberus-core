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
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.factory.IFactoryLabel;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryLabel implements IFactoryLabel {

    @Override
    public Label create(Integer id, String system, String label, String type, String color, Integer parentLabelID, String reqType, String reqStatus, String reqCriticity, String description, String longDesc, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        Label labelObject = new Label();
        labelObject.setColor(color);
        labelObject.setDateCreated(dateCreated);
        labelObject.setDateModif(dateModif);
        labelObject.setId(id);
        labelObject.setLabel(label);
        labelObject.setType(type);
        labelObject.setParentLabelID(parentLabelID);
        labelObject.setSystem(system);
        labelObject.setUsrCreated(usrCreated);
        labelObject.setUsrModif(usrModif);
        labelObject.setDescription(description);
        labelObject.setLongDescription(longDesc);
        labelObject.setRequirementCriticity(reqCriticity);
        labelObject.setRequirementStatus(reqStatus);
        labelObject.setRequirementType(reqType);
        
        return labelObject;
    }

}
