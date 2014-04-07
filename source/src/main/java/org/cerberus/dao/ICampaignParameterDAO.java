/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package org.cerberus.dao;

import java.util.List;
import org.cerberus.entity.Campaign;
import org.cerberus.entity.CampaignContent;
import org.cerberus.entity.CampaignParameter;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author memiks
 */
public interface ICampaignParameterDAO {

    List<CampaignParameter> findAll() throws CerberusException;

    CampaignParameter findCampaignParameterByKey(Integer campaignparameterID) throws CerberusException;

    List<CampaignContent> findCampaignParametersByCampaign(Campaign campaign) throws CerberusException;

    boolean updateCampaignName(CampaignParameter campaignParameter);

    boolean updateParameter(CampaignParameter campaignParameter);

    boolean updateValue(CampaignParameter campaignParameter);

    boolean createCampaignParameter(CampaignParameter campaignParameter);

    List<CampaignParameter> findCampaignParameterByCriteria(Campaign campaign, Integer campaignparameterID, String parameter, String value);

//    List<String> findUniqueDataOfColumn(String column);
}
