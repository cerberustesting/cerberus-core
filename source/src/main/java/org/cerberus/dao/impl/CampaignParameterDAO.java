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
import org.cerberus.dao.ICampaignParameterDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.CampaignParameter;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCampaignParameter;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author memiks
 */
@Repository
public class CampaignParameterDAO implements ICampaignParameterDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCampaignParameter factoryCampaignParameter;

    @Override
    public List<CampaignParameter> findAll() throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaignparameter c";

        List<CampaignParameter> campaignParameterList = new ArrayList<CampaignParameter>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignParameterList.add(this.loadCampaignParameterFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignParameterList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignParameterList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignParameterList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParameterList;
    }

    @Override
    public CampaignParameter findCampaignParameterByKey(Integer campaignparameterID) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaignparameter c WHERE c.campaignparameterID = ?";

        CampaignParameter campaignParameterResult = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, campaignparameterID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        campaignParameterResult = this.loadCampaignParameterFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParameterResult;
    }

    @Override
    public List<CampaignParameter> findCampaignParametersByCampaign(String campaign) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaignparameter c WHERE c.campaign = ?";

        List<CampaignParameter> campaignParameterList = new ArrayList<CampaignParameter>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, campaign);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignParameterList.add(this.loadCampaignParameterFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignParameterList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignParameterList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignParameterList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParameterList;
    }

    @Override
    public boolean updateCampaignParameter(CampaignParameter campaignParameter) {
        final StringBuffer query = new StringBuffer("UPDATE `campaignparameter` SET campaign=?, `Parameter`=?, `Value`=? WHERE campaignparameterID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, campaignParameter.getCampaign());
            preStat.setString(2, campaignParameter.getParameter());
            preStat.setString(3, campaignParameter.getValue());
            preStat.setInt(4, campaignParameter.getCampaignparameterID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public boolean createCampaignParameter(CampaignParameter campaignParameter) {
        final StringBuffer query = new StringBuffer("INSERT INTO `campaignparameter` (`campaign`, `Parameter`, `Value`) VALUES (?, ?, ?);");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, campaignParameter.getCampaign());
            preStat.setString(2, campaignParameter.getParameter());
            preStat.setString(3, campaignParameter.getValue());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public List<CampaignParameter> findCampaignParameterByCriteria(Integer campaignparameterID, String campaign, String parameter, String value) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("SELECT * FROM campaignparameter c WHERE 1=1 ");

        if (campaignparameterID != null) {
            query.append(" AND c.campaignparameterID = ?");
        }
        if (campaign != null && !"".equals(campaign.trim())) {
            query.append(" AND c.campaign LIKE ?");
        }
        if (parameter != null && !"".equals(parameter.trim())) {
            query.append(" AND c.parameter LIKE ?");
        }

        if (value != null && !"".equals(value.trim())) {
            query.append(" AND c.value LIKE ?");
        }

        // " c.campaignID = ? AND c.campaign LIKE ? AND c.description LIKE ?";
        List<CampaignParameter> campaignParametersList = new ArrayList<CampaignParameter>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int index = 1;
            if (campaignparameterID != null) {
                preStat.setInt(index, campaignparameterID);
                index++;
            }
            if (campaign != null && !"".equals(campaign.trim())) {
                preStat.setString(index, "%" + campaign.trim() + "%");
                index++;
            }
            if (parameter != null && !"".equals(parameter.trim())) {
                preStat.setString(index, "%" + parameter.trim() + "%");
                index++;
            }
            if (value != null && !"".equals(value.trim())) {
                preStat.setString(index, "%" + value.trim() + "%");
                index++;
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignParametersList.add(this.loadCampaignParameterFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignParametersList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignParametersList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignParametersList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParametersList;
    }

    @Override
    public boolean deleteCampaignParameter(CampaignParameter campaignParameter) {
        final StringBuffer query = new StringBuffer("DELETE FROM `campaignparameter` WHERE campaignparameterID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setInt(1, campaignParameter.getCampaignparameterID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    private CampaignParameter loadCampaignParameterFromResultSet(ResultSet rs) throws SQLException {
        Integer campaignparameterID = ParameterParserUtil.parseIntegerParam(rs.getString("campaignparameterID"), -1);
        String campaign = ParameterParserUtil.parseStringParam(rs.getString("campaign"), "");
        String parameter = ParameterParserUtil.parseStringParam(rs.getString("Parameter"), "");
        String value = ParameterParserUtil.parseStringParam(rs.getString("Value"), "");

        return factoryCampaignParameter.create(campaignparameterID, campaign, parameter, value);
    }
}
