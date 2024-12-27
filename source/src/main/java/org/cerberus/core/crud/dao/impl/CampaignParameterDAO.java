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
package org.cerberus.core.crud.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ICampaignParameterDAO;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.factory.IFactoryCampaignParameter;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author memiks
 */
@Repository
public class CampaignParameterDAO implements ICampaignParameterDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCampaignParameter factoryCampaignParameter;

    private static final Logger LOG = LogManager.getLogger(CampaignParameterDAO.class);

    private final String OBJECT_NAME = "CampaignParameter";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    /**
     * Declare SQL queries used by this {@link CampaignParameter}
     *
     * @author Aurelien Bourdon
     */
    private static interface Query {

        /**
         * Get list of {@link CampaignParameter} associated with the given
         * {@link Campaign}'s name
         */
        String READ_BY_KEY = "SELECT * FROM `campaignparameter` WHERE `campaigncontentID` = ?";

        /**
         * Create a new {@link CampaignParameter}
         */
        String CREATE = "INSERT INTO `campaignparameter` (`campaign`,`Parameter`,`Value`) VALUES (?, ?, ?)";

        /**
         * Remove an existing {@link CampaignParameter}
         */
        String DELETE = "DELETE FROM `campaignparameter` WHERE `campaign` = ? AND `parameter` = ? AND `value` = ?";

        /**
         * Remove all {@link CampaignParameter} of a {@link Campaign}
         */
        String DELETE_BY_CAMPAIGN = "DELETE FROM `campaignparameter` WHERE `campaign` = ?";
    }

    @Override
    public List<CampaignParameter> findAll() throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaignparameter c";
        List<CampaignParameter> campaignParameterList = new ArrayList<>();
        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            try (ResultSet resultSet = preStat.executeQuery();) {
                while (resultSet.next()) {
                    campaignParameterList.add(this.loadFromResultSet(resultSet));
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                campaignParameterList = null;
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            campaignParameterList = null;
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

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
            preStat.setInt(1, campaignparameterID);
            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    campaignParameterResult = this.loadFromResultSet(resultSet);
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
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

        List<CampaignParameter> campaignParameterList = new ArrayList<>();

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            preStat.setString(1, campaign);
            try (ResultSet resultSet = preStat.executeQuery();) {
                while (resultSet.next()) {
                    campaignParameterList.add(this.loadFromResultSet(resultSet));
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                campaignParameterList = null;
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            campaignParameterList = null;
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParameterList;
    }

    @Override
    public boolean updateCampaignParameter(CampaignParameter campaignParameter) {
        final StringBuilder query = new StringBuilder("UPDATE `campaignparameter` SET campaign=?, `Parameter`=?, `Value`=? WHERE campaignparameterID=?");

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {
            preStat.setString(1, campaignParameter.getCampaign());
            preStat.setString(2, campaignParameter.getParameter());
            preStat.setString(3, campaignParameter.getValue());
            preStat.setInt(4, campaignParameter.getCampaignparameterID());
            return (preStat.executeUpdate() == 1);
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return false;
    }

    @Override
    public boolean createCampaignParameter(CampaignParameter campaignParameter) {
        final StringBuilder query = new StringBuilder("INSERT INTO `campaignparameter` (`campaign`, `Parameter`, `Value`) VALUES (?, ?, ?);");

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {
            preStat.setString(1, campaignParameter.getCampaign());
            preStat.setString(2, campaignParameter.getParameter());
            preStat.setString(3, campaignParameter.getValue());
            return (preStat.executeUpdate() == 1);
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return false;
    }

    @Override
    public List<CampaignParameter> findCampaignParameterByCriteria(Integer campaignparameterID, String campaign, String parameter, String value) throws CerberusException {
        boolean throwEx = false;
        final StringBuilder query = new StringBuilder("SELECT * FROM campaignparameter c WHERE 1=1 ");

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
        List<CampaignParameter> campaignParametersList = new ArrayList<>();

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

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

            try (ResultSet resultSet = preStat.executeQuery();) {
                while (resultSet.next()) {
                    campaignParametersList.add(this.loadFromResultSet(resultSet));
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                campaignParametersList = null;
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            campaignParametersList = null;
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParametersList;
    }

    @Override
    public boolean deleteCampaignParameter(CampaignParameter campaignParameter) {
        final StringBuilder query = new StringBuilder("DELETE FROM `campaignparameter` WHERE campaignparameterID=?");

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {
            preStat.setInt(1, campaignParameter.getCampaignparameterID());
            return (preStat.executeUpdate() == 1);
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return false;
    }

    @Override
    public AnswerList<CampaignParameter> readByCampaignByCriteria(String campaign, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        AnswerList<CampaignParameter> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CampaignParameter> campaignParameterList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM campaignparameter ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`campaignparameterid` like ?");
            searchSQL.append(" or `campaign` like ?");
            searchSQL.append(" or `parameter` like ?");
            searchSQL.append(" or `value` like ?)");
        }
        if (!StringUtil.isEmptyOrNull(individualSearch)) {
            searchSQL.append(" and (`?`)");
        }
        if (!StringUtil.isEmptyOrNull(campaign)) {
            searchSQL.append(" and (`campaign` = ? )");
        }
        query.append(searchSQL);

        if (!StringUtil.isEmptyOrNull(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement();) {

            int i = 1;
            if (!StringUtil.isEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            if (!StringUtil.isEmptyOrNull(individualSearch)) {
                preStat.setString(i++, individualSearch);
            }
            if (!StringUtil.isEmptyOrNull(campaign)) {
                preStat.setString(i++, campaign);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    campaignParameterList.add(this.loadFromResultSet(resultSet));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (resultSet != null && resultSet.next()) {
                    nrTotalRows = resultSet.getInt(1);
                }

                if (campaignParameterList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    response = new AnswerList<>(campaignParameterList, nrTotalRows);
                } else if (campaignParameterList.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    response = new AnswerList<>(campaignParameterList, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    response = new AnswerList<>(campaignParameterList, nrTotalRows);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(campaignParameterList);
        return response;
    }

    @Override
    public AnswerList<CampaignParameter> readByCampaign(String campaign) {
        AnswerList<CampaignParameter> answer = new AnswerList<>();
        MessageEvent msg;
        List<CampaignParameter> result = new ArrayList<>();

        final String query = "SELECT * FROM campaignparameter  WHERE campaign = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.campaign : " + campaign);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            preStat.setString(1, campaign);

            try (ResultSet resultSet = preStat.executeQuery();) {
                while (resultSet.next()) {
                    result.add(this.loadFromResultSet(resultSet));
                }
                if (result.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                result.clear();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        answer.setTotalRows(result.size());
        answer.setDataList(result);
        answer.setResultMessage(msg);
        return answer;
    }

    private CampaignParameter loadFromResultSet(ResultSet rs) throws SQLException {
        Integer campaignparameterID = ParameterParserUtil.parseIntegerParam(rs.getString("campaignparameterID"), -1);
        String campaign = ParameterParserUtil.parseStringParam(rs.getString("campaign"), "");
        String parameter = ParameterParserUtil.parseStringParam(rs.getString("Parameter"), "");
        String value = ParameterParserUtil.parseStringParam(rs.getString("Value"), "");

        return factoryCampaignParameter.create(campaignparameterID, campaign, parameter, value);
    }

    @Override
    public Answer deleteByCampaign(String key) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.DELETE_BY_CAMPAIGN)) {
            // Prepare and execute query
            preStat.setString(1, key);
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "DELETE_BY_CAMPAIGN");
        } catch (Exception e) {
            LOG.warn("Unable to delete campaign parameter by campaign: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer delete(CampaignParameter object) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + Query.DELETE);
            LOG.debug("SQL.param.campaign : " + object.getCampaign());
            LOG.debug("SQL.param.paraameter : " + object.getParameter());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.DELETE)) {
            // Prepare and execute query
            preStat.setString(1, object.getCampaign());
            preStat.setString(2, object.getParameter());
            preStat.setString(3, object.getValue());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "DELETE");
        } catch (Exception e) {
            LOG.warn("Unable to delete campaign parameter: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer update(CampaignParameter object) {
        Answer ans = new Answer();
        MessageEvent msg = null;
        String query = "UPDATE `campaignparameter` SET `value` = ? WHERE `campaign` = ? AND `parameter` = ?";

        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.campaign : " + object.getCampaign());
            LOG.debug("SQL.param.paraameter : " + object.getParameter());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setString(1, object.getValue());
            preStat.setString(2, object.getCampaign());
            preStat.setString(3, object.getParameter());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "UPDATE");
        } catch (Exception e) {
            LOG.warn("Unable to create campaign parameter: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer create(CampaignParameter object) {
        Answer ans = new Answer();
        MessageEvent msg = null;
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + Query.CREATE);
            LOG.debug("SQL.param.campaign : " + object.getCampaign());
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.CREATE)) {
            // Prepare and execute query
            preStat.setString(1, object.getCampaign());
            preStat.setString(2, object.getParameter());
            preStat.setString(3, object.getValue());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create campaign content: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }
}
