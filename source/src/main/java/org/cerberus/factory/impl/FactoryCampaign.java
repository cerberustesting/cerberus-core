/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package org.cerberus.factory.impl;

import org.cerberus.entity.Campaign;
import org.cerberus.factory.IFactoryCampaign;

/**
 *
 * @author memiks
 */
public class FactoryCampaign implements IFactoryCampaign {

    @Override
    public Campaign create(Integer campaignID, String campaign, String description) {
        return new Campaign(campaignID, campaign, description);
    }

}
