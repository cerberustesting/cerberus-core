/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
import org.cerberus.dao.IInvariantDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Invariant;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryInvariant;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
@Repository
public class InvariantDAO implements IInvariantDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryInvariant factoryInvariant;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @param variable Description text text text.
     * @return Description text text text.
     */
    @Override
    public Invariant findInvariantByIdValue(String idName, String value) throws CerberusException {
        boolean throwException = true;
        Invariant result = null;
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.value = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, idName);
                preStat.setString(2, value);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        throwException = false;
                        int sort = resultSet.getInt("sort");
                        int id = resultSet.getInt("id");
                        String description = resultSet.getString("Description");
                        String gp1 = resultSet.getString("gp1");
                        String gp2 = resultSet.getString("gp2");
                        String gp3 = resultSet.getString("gp3");
                        result = factoryInvariant.create(idName, value, sort, id, description, gp1, gp2, gp3);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, exception.toString());
                } catch (NullPointerException ex) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Resultset");
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } catch (NullPointerException ex) {
                MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Statement");
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } catch (NullPointerException ex) {
            MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Connection");
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, "Connection already closed!");
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public List<Invariant> findListOfInvariantById(String idName) throws CerberusException {
        boolean throwException = true;
        List<Invariant> result = null;
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? ORDER BY sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, idName);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<Invariant>();

                    while (resultSet.next()) {
                        throwException = false;
                        int sort = resultSet.getInt("sort");
                        int id = resultSet.getInt("id");
                        String description = resultSet.getString("Description");
                        String gp1 = resultSet.getString("gp1");
                        String gp2 = resultSet.getString("gp2");
                        String gp3 = resultSet.getString("gp3");
                        String value = resultSet.getString("value");
                        result.add(factoryInvariant.create(idName, value, sort, id, description, gp1, gp2, gp3));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public List<Invariant> findInvariantByIdGp1(String idName, String gp) throws CerberusException {
        boolean throwException = true;
        List<Invariant> result = null;
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.gp1 = ? ORDER BY sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, idName);
                preStat.setString(2, gp);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<Invariant>();

                    while (resultSet.next()) {
                        throwException = false;
                        int sort = resultSet.getInt("sort");
                        int id = resultSet.getInt("id");
                        String description = resultSet.getString("Description");
                        String gp1 = resultSet.getString("gp1");
                        String gp2 = resultSet.getString("gp2");
                        String gp3 = resultSet.getString("gp3");
                        String value = resultSet.getString("value");
                        result.add(factoryInvariant.create(idName, value, sort, id, description, gp1, gp2, gp3));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }
}
