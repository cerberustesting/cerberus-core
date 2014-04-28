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
import org.cerberus.dao.IInvariantRobotDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.InvariantRobot;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryInvariantRobot;
import org.cerberus.factory.impl.FactoryInvariantRobot;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 * @since 0.9.2
 */
@Repository
public class InvariantRobotDAO implements IInvariantRobotDAO{

    /**
     * Bean of the DatabaseSpring, Spring automatically links. Establishes
     * connection to database and return it to allow perform queries and
     * updates.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    /**
     * Bean of the IFactoryRobot, Spring automatically links. Creates new
     * objects {@link InvariantRobot}
     */
    @Autowired
    private IFactoryInvariantRobot factoryInvariantRobot;
    
    /**
     * Finds the InvariantRobot by the name. </p> Access to database to return the
     * {@link InvariantRobot} given by the unique name.<br/> If no InvariantRobot
     * found with the given name, returns CerberusException with
     * {@link MessageGeneralEnum#NO_DATA_FOUND}.<br/> If an SQLException occur,
     * returns null in the InvariantRobot object and writes the error on the logs.
     *
     * @param id id of the InvariantRobot to find
     * @return object InvariantRobot if exist
     * @throws CerberusException when Robot does not exist
     * @since 0.9.2
     */
    @Override
    public InvariantRobot findInvariantRobotByKey(Integer id) throws CerberusException {
       InvariantRobot result = null;
        final String query = "SELECT * FROM invariantrobot a WHERE a.id = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, id);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadInvariantRobotFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;
    }

    /**
     * Finds all InvariantRobot that exists. </p> Access to database to return all
     * existing {@link InvariantRobot}.<br/> If no InvariantRobot found, returns a
     * empty {@literal List<InvariantRobot>}.<br/> If an SQLException occur,
     * returns null in the list object and writes the error on the logs.
     *
     * @return list of InvariantRobot
     * @throws CerberusException
     * @since 0.9.2
     */
    @Override
    public List<InvariantRobot> findAllInvariantRobot() throws CerberusException {
        List<InvariantRobot> list = null;
        final String query = "SELECT * FROM invariantrobot a";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<InvariantRobot>();
                    while (resultSet.next()) {
                        InvariantRobot invariantRobot = this.loadInvariantRobotFromResultSet(resultSet);
                        list.add(invariantRobot);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    /**
     * Updates the information based on the object InvariantRobot. </p> Access to
     * database to update InvariantRobot information given by the object
     * InvariantRobot.
     * <br/> If an SQLException occur, throw CerberusException.
     *
     * @param invariantRobot object InvariantRobot to update
     * @throws CerberusException
     * @since 0.9.2
     */
    @Override
    public void updateInvariantRobot(InvariantRobot invariantRobot) throws CerberusException {
        final String query = "UPDATE invariantrobot SET platform = ?, os=?, browser = ?, `version` = ? WHERE id = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, invariantRobot.getPlatform());
                preStat.setString(2, invariantRobot.getOs());
                preStat.setString(3, invariantRobot.getBrowser());
                preStat.setString(4, invariantRobot.getVersion());
                preStat.setInt(5, invariantRobot.getId());
                
                preStat.executeUpdate();
                } catch (SQLException exception) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        
    }

    @Override
    public void createInvariantRobot(InvariantRobot invariantRobot) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO invariantrobot (`platform`, `os`, `browser`, `version`) ");
        query.append("VALUES (?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, invariantRobot.getPlatform());
                preStat.setString(2, invariantRobot.getOs());
                preStat.setString(3, invariantRobot.getBrowser());
                preStat.setString(4, invariantRobot.getVersion());
                
                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteInvariantRobot(InvariantRobot invariantRobot) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM invariantrobot WHERE id = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, invariantRobot.getId());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }
    
    /**
     * Uses data of ResultSet to create object {@link InvariantRobot}
     *
     * @param rs ResultSet relative to select from table InvariantRobot
     * @return object {@link InvariantRobot}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryInvariantRobot
     */
    private InvariantRobot loadInvariantRobotFromResultSet(ResultSet rs) throws SQLException {
        Integer id = ParameterParserUtil.parseIntegerParam(rs.getString("id"), 0);
        String platform = ParameterParserUtil.parseStringParam(rs.getString("platform"), "");
        String os = ParameterParserUtil.parseStringParam(rs.getString("os"), "");
        String browser = ParameterParserUtil.parseStringParam(rs.getString("browser"), "");
        String version = ParameterParserUtil.parseStringParam(rs.getString("version"), "");
        
        //TODO remove when working in test with mockito and autowired
        factoryInvariantRobot = new FactoryInvariantRobot();
        return factoryInvariantRobot.create(id, platform, os, browser, version);
    }

    @Override
    public List<InvariantRobot> findInvariantRobotListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        List<InvariantRobot> invariantRobotList = new ArrayList<InvariantRobot>();
        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM invariantrobot ");

        gSearch.append(" where (`platform` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `os` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `browser` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `version` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !individualSearch.equals("")) {
            searchSQL.append(gSearch.toString());
            searchSQL.append(" and ");
            searchSQL.append(individualSearch);
        } else if (!individualSearch.equals("")) {
            searchSQL.append(" where `");
            searchSQL.append(individualSearch);
            searchSQL.append("`");
        } else if (!searchTerm.equals("")) {
            searchSQL.append(gSearch.toString());
        }

        query.append(searchSQL);
        query.append("order by `");
        query.append(column);
        query.append("` ");
        query.append(dir);
        query.append(" limit ");
        query.append(start);
        query.append(" , ");
        query.append(amount);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        invariantRobotList.add(this.loadInvariantRobotFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, e.toString());
            }
        }

        return invariantRobotList;
    }

    @Override
    public Integer getNumberOfInvariantRobotPerCriteria(String searchTerm, String inds) {
        Integer result = 0;
        StringBuilder query = new StringBuilder();
        StringBuilder gSearch = new StringBuilder();
        String searchSQL = "";

        query.append("SELECT count(*) FROM invariantrobot");

        gSearch.append(" where (`platform` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `os` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `browser` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `version` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !inds.equals("")) {
            searchSQL = gSearch.toString() + " and " + inds;
        } else if (!inds.equals("")) {
            searchSQL = " where " + inds;
        } else if (!searchTerm.equals("")) {
            searchSQL = gSearch.toString();
        }

        query.append(searchSQL);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    if (resultSet.first()) {
                        result = resultSet.getInt(1);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantRobotDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;
    }
    
}
