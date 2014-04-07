/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package org.cerberus.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.ICampaignDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Campaign;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCampaign;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author memiks
 */
@Repository
public class CampaignDAO implements ICampaignDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCampaign factoryCampaign;


    @Override
    public List<Campaign> findAll() throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT c FROM Campaign c";

        List<Campaign> campaignList = new ArrayList<Campaign>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignList.add(this.loadCampaignFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                    campaignList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                campaignList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            campaignList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignList;
    }

    @Override
    public Campaign findCampaignByKey(Integer campaignID) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT c FROM Campaign c WHERE c.campaignID = ?";

        Campaign campaign = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, campaignID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        campaign = this.loadCampaignFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaign;
    }

    @Override
    public Campaign findCampaignByCampaignName(String campaign) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT c FROM Campaign c WHERE c.campaign = ?";

        Campaign campaignResult = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, campaign);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        campaignResult = this.loadCampaignFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignResult;
    }

    @Override
    public List<Campaign> findCampaignByCriteria(Integer campaignID, String campaign, String description) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("SELECT c FROM Campaign c WHERE ");

        if (campaignID != null) {
            query.append(" c.campaignID = ?");
        }
        if (campaign != null && !"".equals(campaign.trim())) {
            query.append(" c.campaign LIKE ?");
        }
        if (description != null && !"".equals(description.trim())) {
            query.append(" c.description LIKE ?");
        }

        // " c.campaignID = ? AND c.campaign LIKE ? AND c.description LIKE ?";
        List<Campaign> campaignList = new ArrayList<Campaign>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int index = 1;
            if (campaignID != null) {
                preStat.setInt(index, campaignID);
                index++;
            }
            if (campaign != null && !"".equals(campaign.trim())) {
                preStat.setString(index, "%" + campaign.trim() + "%");
                index++;
            }
            if (description != null && !"".equals(description.trim())) {
                preStat.setString(index, "%" + description.trim() + "%");
                index++;
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignList.add(this.loadCampaignFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                    campaignList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                campaignList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            campaignList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignList;
    }

    @Override
    public List<Campaign> findCampaignsByCampaignDescription(String description) throws CerberusException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean updateCampaignName(Campaign campaign) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean updateCampaignDescription(Campaign campaign) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean createCampaign(Campaign campaign) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Campaign loadCampaignFromResultSet(ResultSet rs) throws SQLException {
        Integer campaignId = ParameterParserUtil.parseIntegerParam(rs.getString("campaignID"), -1);
        String campaign = ParameterParserUtil.parseStringParam(rs.getString("campaign"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("Description"), "");

        return factoryCampaign.create(campaignId, campaign, description);
    }

}
