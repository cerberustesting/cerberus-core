/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package org.cerberus.factory;

import org.cerberus.entity.Campaign;

/**
 *
 * @author memiks
 */
public interface IFactoryCampaign {

    /**
     * @param campaignID Technical ID of the Campaign.
     * @param campaign Id name of the Campaign
     * @param description Description of the Campaign.
     * @return Campaign Object
     */
    Campaign create(Integer campaignID, String campaign, String description);
}
