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
import org.cerberus.util.StringUtil;
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
                        result = this.loadInvariantFromResultSet(resultSet);
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
    public Invariant findInvariantByIdSort(String idName, Integer sort) throws CerberusException {
        boolean throwException = true;
        Invariant result = null;
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.sort = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, idName);
                preStat.setInt(2, sort);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        throwException = false;
                        result = this.loadInvariantFromResultSet(resultSet);
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
                        result.add(this.loadInvariantFromResultSet(resultSet));
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
                        result.add(this.loadInvariantFromResultSet(resultSet));
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
    public List<Invariant> findInvariantListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch, String PublicPrivateFilter) {
        List<Invariant> invariantList = new ArrayList<Invariant>();
        StringBuilder searchSQL = new StringBuilder();
        searchSQL.append(" where 1=1 ");

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM invariant ");

        if (!searchTerm.equals("") && !individualSearch.equals("")) {
            searchSQL.append(" and ");
            searchSQL.append(getSearchString(searchTerm));
            searchSQL.append(" and ");
            searchSQL.append(individualSearch);
        } else if (!individualSearch.equals("")) {
            searchSQL.append(" and `");
            searchSQL.append(individualSearch);
            searchSQL.append("`");
        } else if (!searchTerm.equals("")) {
            searchSQL.append(" and ");
            searchSQL.append(getSearchString(searchTerm));
        }
        if (!(PublicPrivateFilter.equalsIgnoreCase(""))) {
            searchSQL.append(" and ");
            searchSQL.append(PublicPrivateFilter);
        }

        query.append(searchSQL);
        query.append(" order by `");
        query.append(column);
        query.append("` ");
        query.append(dir);
        if (amount > 0) {
            query.append(" limit ");
            query.append(start);
            query.append(" , ");
            query.append(amount);
        }

        Invariant invariantData;

        MyLogger.log(InvariantDAO.class.getName(), Level.DEBUG, query.toString());

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        invariantList.add(this.loadInvariantFromResultSet(resultSet));
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
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, e.toString());
            }
        }

        return invariantList;
    }

    @Override
    public Integer getNumberOfInvariant(String searchTerm, String PublicPrivateFilter) throws CerberusException {
        boolean throwException = true;
        Integer result = 0;
        
        StringBuilder searchSQL = new StringBuilder();
        if (!(PublicPrivateFilter.equalsIgnoreCase(""))) {
            searchSQL.append(" and ");
            searchSQL.append(PublicPrivateFilter);
        }
        if (!(searchTerm.equalsIgnoreCase(""))) {
            searchSQL.append(" and ");
            searchSQL.append(getSearchString(searchTerm));
        }
        
        String query = "SELECT count(*) FROM invariant i  WHERE 1=1 " + searchSQL.toString();
        
        MyLogger.log(InvariantDAO.class.getName(), Level.DEBUG, query.toString());

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        throwException = false;
                        result = resultSet.getInt(1);
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
    public void createInvariant(Invariant invariant) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO invariant (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, invariant.getIdName());
                preStat.setString(2, invariant.getValue());
                preStat.setInt(3, invariant.getSort());
                preStat.setString(4, invariant.getDescription());
                preStat.setString(5, invariant.getVeryShortDesc());
                preStat.setString(6, invariant.getGp1());
                preStat.setString(7, invariant.getGp2());
                preStat.setString(8, invariant.getGp3());

                preStat.executeUpdate();
                throwExcep = false;

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
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteInvariant(Invariant invariant) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM invariant WHERE idname = ? and `value` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, invariant.getIdName());
                preStat.setString(2, invariant.getValue());

                throwExcep = preStat.executeUpdate() > 0;
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
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }
    @Override
    public void updateInvariant(Invariant invariant) throws CerberusException{
        boolean throwExcep = false;
        final String query = "UPDATE invariant SET sort = ?, Description = ?, VeryShortDesc = ?, gp1 = ?, gp2 = ?, gp3 = ?  WHERE idname = ? and `value` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, invariant.getSort());
                preStat.setString(2, invariant.getDescription());
                preStat.setString(3, invariant.getVeryShortDesc());
                preStat.setString(4, invariant.getGp1());
                preStat.setString(5, invariant.getGp2());
                preStat.setString(6, invariant.getGp3());
                preStat.setString(7, invariant.getIdName());
                preStat.setString(8, invariant.getValue());

                throwExcep = preStat.executeUpdate() == 0;
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
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    private Invariant loadInvariantFromResultSet(ResultSet resultSet) throws SQLException {
        String idName = resultSet.getString("idName");
        int sort = resultSet.getInt("sort");
        String description = resultSet.getString("Description");
        String veryShortDesc = resultSet.getString("VeryShortDesc");
        String gp1 = resultSet.getString("gp1");
        String gp2 = resultSet.getString("gp2");
        String gp3 = resultSet.getString("gp3");
        String value = resultSet.getString("value");
        return factoryInvariant.create(idName, value, sort, description, veryShortDesc, gp1, gp2, gp3);
    }

    private String getSearchString(String searchTerm) {
        if (StringUtil.isNullOrEmpty(searchTerm))  {
            return "";
        } else {
            StringBuilder gSearch = new StringBuilder();
            gSearch.append(" (`idname` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `value` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `sort` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `description` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `veryshortdesc` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `gp1` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `gp2` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `gp3` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%') ");
            return gSearch.toString();
        }
    }
}
