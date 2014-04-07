/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package org.cerberus.dao;

import java.util.List;
import org.cerberus.entity.Campaign;
import org.cerberus.entity.CampaignContent;
import org.cerberus.entity.TestBattery;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author memiks
 */
public interface ICampaignContentDAO {


    List<CampaignContent> findAll() throws CerberusException;

    CampaignContent findCampaignContentByKey(Integer campaignID) throws CerberusException;

    CampaignContent findCampaignContentByCampaignName(String campaign) throws CerberusException;

    List<CampaignContent> findCampaignContentsByTestBattery(String description) throws CerberusException;

    boolean updateCampaignName(CampaignContent campaign);

    boolean updateTestBattery(CampaignContent campaign);

    boolean createCampaignContent(CampaignContent campaign);

    List<CampaignContent> findCampaignContentByCriteria(Campaign campaign, Integer campaignContentID, TestBattery testBattery);

//    List<String> findUniqueDataOfColumn(String column);
}
