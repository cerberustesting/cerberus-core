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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IApplicationDAO;
import org.cerberus.crud.dao.ICampaignDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryCampaign;
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
    @Autowired
    private IApplicationDAO applicationDAO;

    private static final Logger LOGGER = Logger.getLogger(CampaignDAO.class);

    @Override
    public List<Campaign> findAll() throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaign c";

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
                    MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignDAO.class.getName(), Level.WARN, e.toString());
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
        final String query = "SELECT * FROM campaign c WHERE c.campaignID = ?";

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
                    MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignDAO.class.getName(), Level.WARN, e.toString());
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
        final String query = "SELECT * FROM campaign c WHERE c.campaign = ?";

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
                    MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignDAO.class.getName(), Level.WARN, e.toString());
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
        final StringBuffer query = new StringBuffer("SELECT * FROM campaign c WHERE 1=1 ");

        if (campaignID != null) {
            query.append(" AND c.campaignID = ?");
        }
        if (campaign != null && !"".equals(campaign.trim())) {
            query.append(" AND c.campaign LIKE ?");
        }
        if (description != null && !"".equals(description.trim())) {
            query.append(" AND c.description LIKE ?");
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
                    MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignList;
    }

    @Override
    public boolean updateCampaign(Campaign campaign) {
        final StringBuffer query = new StringBuffer("UPDATE `campaign` SET campaign=?, Description=? WHERE campaignID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, campaign.getCampaign());
            preStat.setString(2, campaign.getDescription());
            preStat.setInt(3, campaign.getCampaignID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public boolean createCampaign(Campaign campaign) {
        final StringBuffer query = new StringBuffer("INSERT INTO `campaign` (`campaign`, `Description`) VALUES (?, ?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, campaign.getCampaign());
            preStat.setString(2, campaign.getDescription());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public boolean deleteCampaign(Campaign campaign) {
        final StringBuffer query = new StringBuffer("DELETE FROM `campaign` WHERE campaignID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setInt(1, campaign.getCampaignID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    private Campaign loadCampaignFromResultSet(ResultSet rs) throws SQLException {
        Integer campaignId = ParameterParserUtil.parseIntegerParam(rs.getString("campaignID"), -1);
        String campaign = ParameterParserUtil.parseStringParam(rs.getString("campaign"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("Description"), "");

        return factoryCampaign.create(campaignId, campaign, description);
    }

    @Override
    public List<TestCaseWithExecution> getCampaignTestCaseExecutionForEnvCountriesBrowserTag(String tag) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("select * from ( select tc.*, tce.Start, tce.End, tce.ID as statusExecutionID, tce.ControlStatus, tce.ControlMessage, tce.Environment, tce.Country, tce.Browser ")
                .append("from testcase tc ")
                .append("left join testcaseexecution tce ")
                .append("on tce.Test = tc.Test ")
                .append("and tce.TestCase = tc.TestCase ")
                .append("where tce.tag = ? ");

        query.append(" order by test, testcase, ID desc) as tce, application app ")
                .append("where tce.application = app.application ")
                .append("group by tce.test, tce.testcase, tce.Environment, tce.Browser, tce.Country ").toString();

        List<TestCaseWithExecution> testCaseWithExecutionList = new ArrayList<TestCaseWithExecution>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseWithExecutionList.add(this.loadTestCaseWithExecutionFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testCaseWithExecutionList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testCaseWithExecutionList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testCaseWithExecutionList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testCaseWithExecutionList;
    }

    public TestCaseWithExecution loadTestCaseWithExecutionFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseWithExecution testCaseWithExecution = new TestCaseWithExecution();

        testCaseWithExecution.setTest(resultSet.getString("Test"));
        testCaseWithExecution.setTestCase(resultSet.getString("TestCase"));
        testCaseWithExecution.setApplication(resultSet.getString("Application"));
        testCaseWithExecution.setProject(resultSet.getString("Project"));
        testCaseWithExecution.setTicket(resultSet.getString("Ticket"));
        testCaseWithExecution.setShortDescription(resultSet.getString("Description"));
        testCaseWithExecution.setDescription(resultSet.getString("BehaviorOrValueExpected"));
        testCaseWithExecution.setPriority(resultSet.getInt("Priority"));
        testCaseWithExecution.setStatus(resultSet.getString("Status"));
        testCaseWithExecution.setActive(resultSet.getString("TcActive"));
        testCaseWithExecution.setGroup(resultSet.getString("Group"));
        testCaseWithExecution.setOrigin(resultSet.getString("Origine"));
        testCaseWithExecution.setRefOrigin(resultSet.getString("RefOrigine"));
        testCaseWithExecution.setHowTo(resultSet.getString("HowTo"));
        testCaseWithExecution.setComment(resultSet.getString("Comment"));
        testCaseWithExecution.setFromSprint(resultSet.getString("FromBuild"));
        testCaseWithExecution.setFromRevision(resultSet.getString("FromRev"));
        testCaseWithExecution.setToSprint(resultSet.getString("ToBuild"));
        testCaseWithExecution.setToRevision(resultSet.getString("ToRev"));
        testCaseWithExecution.setBugID(resultSet.getString("BugID"));
        testCaseWithExecution.setTargetSprint(resultSet.getString("TargetBuild"));
        testCaseWithExecution.setTargetRevision(resultSet.getString("TargetRev"));
        testCaseWithExecution.setCreator(resultSet.getString("Creator"));
        testCaseWithExecution.setImplementer(resultSet.getString("Implementer"));
        testCaseWithExecution.setLastModifier(resultSet.getString("LastModifier"));
        testCaseWithExecution.setRunQA(resultSet.getString("activeQA"));
        testCaseWithExecution.setRunUAT(resultSet.getString("activeUAT"));
        testCaseWithExecution.setRunPROD(resultSet.getString("activePROD"));
        testCaseWithExecution.setFunction(resultSet.getString("function"));
        String start = resultSet.getString("Start");
        if (start.endsWith(".0")) {
            testCaseWithExecution.setStart(start.replace(".0", ""));
        } else {
            testCaseWithExecution.setStart(start);
        }
        try { // Managing the case where the date is 0000-00-00 00:00:00 inside MySQL
            testCaseWithExecution.setEnd(resultSet.getString("End"));
        } catch (SQLException e) {
            testCaseWithExecution.setEnd("0000-00-00 00:00:00");
        }
        testCaseWithExecution.setStatusExecutionID(resultSet.getLong("statusExecutionID"));
        testCaseWithExecution.setControlStatus(resultSet.getString("ControlStatus"));
        testCaseWithExecution.setControlMessage(resultSet.getString("ControlMessage"));
        testCaseWithExecution.setEnvironment(resultSet.getString("Environment"));
        testCaseWithExecution.setCountry(resultSet.getString("Country"));
        testCaseWithExecution.setBrowser(resultSet.getString("Browser"));

        testCaseWithExecution.setApplicationObject(applicationDAO.loadFromResultSet(resultSet));

        return testCaseWithExecution;
    }

}
