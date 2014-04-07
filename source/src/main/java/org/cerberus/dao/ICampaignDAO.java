/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package org.cerberus.dao;

import java.util.List;
import org.cerberus.entity.Campaign;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author memiks
 */
public interface ICampaignDAO {

    List<Campaign> findAll() throws CerberusException;

    Campaign findCampaignByKey(Integer campaignID) throws CerberusException;

    Campaign findCampaignByCampaignName(String campaign) throws CerberusException;

    List<Campaign> findCampaignsByCampaignDescription(String description) throws CerberusException;

    boolean updateCampaignName(Campaign campaign);

    boolean updateCampaignDescription(Campaign campaign);

    boolean createCampaign(Campaign campaign);

    List<Campaign> findCampaignByCriteria(Integer campaignID, String campaign, String description) throws CerberusException;

//    List<String> findUniqueDataOfColumn(String column);
}
