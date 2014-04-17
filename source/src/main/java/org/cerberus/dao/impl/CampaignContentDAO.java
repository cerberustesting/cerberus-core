/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.ICampaignContentDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.CampaignContent;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCampaignContent;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author memiks
 */
@Repository
public class CampaignContentDAO implements ICampaignContentDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCampaignContent factoryCampaignContent;

    @Override
    public List<CampaignContent> findAll() throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaigncontent c";

        List<CampaignContent> campaignContentList = new ArrayList<CampaignContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignContentList.add(this.loadCampaignContentFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignContentList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignContentList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignContentList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignContentList;
    }

    @Override
    public CampaignContent findCampaignContentByKey(Integer campaigncontentID) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaigncontent c WHERE c.campaigncontentID = ?";

        CampaignContent campaignContent = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, campaigncontentID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        campaignContent = this.loadCampaignContentFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignContent;
    }

    @Override
    public List<CampaignContent> findCampaignContentByCampaignName(String campaign) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaigncontent c WHERE c.campaign = ?";

        List<CampaignContent> campaignContentList = new ArrayList<CampaignContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, campaign);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignContentList.add(this.loadCampaignContentFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignContentList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignContentList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignContentList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignContentList;
    }

    @Override
    public List<CampaignContent> findCampaignContentsByTestBattery(String testBattery) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaigncontent c WHERE c.testbattery = ?";

        List<CampaignContent> campaignContentList = new ArrayList<CampaignContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, testBattery);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignContentList.add(this.loadCampaignContentFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignContentList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignContentList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignContentList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignContentList;
    }

    @Override
    public List<CampaignContent> findCampaignContentByCriteria(String campaign, Integer campaignContentID, String testBattery) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("SELECT * FROM CampaignParameter c WHERE ");

        if (campaignContentID != null) {
            query.append(" c.campaignContentID = ?");
        }
        if (campaign != null && !"".equals(campaign.trim())) {
            query.append(" c.campaign LIKE ?");
        }
        if (testBattery != null && !"".equals(testBattery.trim())) {
            query.append(" c.testBattery LIKE ?");
        }

        // " c.campaignID = ? AND c.campaign LIKE ? AND c.description LIKE ?";
        List<CampaignContent> campaignContentsList = new ArrayList<CampaignContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int index = 1;
            if (campaignContentID != null) {
                preStat.setInt(index, campaignContentID);
                index++;
            }
            if (campaign != null && !"".equals(campaign.trim())) {
                preStat.setString(index, "%" + campaign.trim() + "%");
                index++;
            }
            if (testBattery != null && !"".equals(testBattery.trim())) {
                preStat.setString(index, "%" + testBattery.trim() + "%");
                index++;
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignContentsList.add(this.loadCampaignContentFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignContentsList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignContentsList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignContentsList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignContentsList;
    }

    @Override
    public boolean updateCampaignContent(CampaignContent campaignContent) {
        final StringBuffer query = new StringBuffer("UPDATE `campaigncontent` SET campaign=?, testbattery=? WHERE campaigncontentID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, campaignContent.getCampaign());
            preStat.setString(2, campaignContent.getTestbattery());
            preStat.setInt(3, campaignContent.getCampaigncontentID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public boolean createCampaignContent(CampaignContent campaignContent) {
        final StringBuffer query = new StringBuffer("INSERT INTO `campaigncontent` (`campaign`, `testbattery`) VALUES (?, ?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, campaignContent.getCampaign());
            preStat.setString(2, campaignContent.getTestbattery());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public boolean deleteCampaignContent(CampaignContent campaignContent) {
        final StringBuffer query = new StringBuffer("DELETE FROM `campaigncontent` WHERE campaigncontentID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setInt(1, campaignContent.getCampaigncontentID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    private CampaignContent loadCampaignContentFromResultSet(ResultSet rs) throws SQLException {
        Integer campaigncontentID = ParameterParserUtil.parseIntegerParam(rs.getString("campaigncontentID"), -1);
        String testbattery = ParameterParserUtil.parseStringParam(rs.getString("testbattery"), "");
        String campaign = ParameterParserUtil.parseStringParam(rs.getString("campaign"), "");

        return factoryCampaignContent.create(campaigncontentID, testbattery, campaign);
    }

}
