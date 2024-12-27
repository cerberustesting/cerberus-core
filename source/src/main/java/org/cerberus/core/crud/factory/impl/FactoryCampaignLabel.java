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

import org.cerberus.core.crud.factory.IFactoryCampaignLabel;
import java.sql.Timestamp;
import org.cerberus.core.crud.entity.CampaignLabel;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryCampaignLabel implements IFactoryCampaignLabel {

    @Override
    public CampaignLabel create(Integer campaignLabelID, String campaign, Integer LabelId,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        CampaignLabel newObject = new CampaignLabel();
        newObject.setCampaignLabelID(campaignLabelID);
        newObject.setCampaign(campaign);
        newObject.setLabelId(LabelId);
        newObject.setUsrCreated(usrCreated);
        newObject.setUsrModif(usrModif);
        newObject.setDateCreated(dateCreated);
        newObject.setDateModif(dateModif);
        return newObject;
    }

}
