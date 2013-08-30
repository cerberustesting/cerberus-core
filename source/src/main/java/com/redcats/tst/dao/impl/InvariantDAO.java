package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IInvariantDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.Invariant;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryInvariant;
import com.redcats.tst.log.MyLogger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
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
    public Invariant findInvariantByIdValue(String idName, String value) throws CerberusException{
        boolean throwException = true;
        Invariant result = null;
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.value = ?";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, idName);
            preStat.setString(2, value);
            try {
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
            this.databaseSpring.disconnect();
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

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, idName);
            try {
                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<Invariant>();
                try {
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
            this.databaseSpring.disconnect();
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

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, idName);
            preStat.setString(2, gp);
            try {
                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<Invariant>();
                try {
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
            this.databaseSpring.disconnect();
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }
}
