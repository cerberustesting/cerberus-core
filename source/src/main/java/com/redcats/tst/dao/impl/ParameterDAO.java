package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IParameterDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.Parameter;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryParameter;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 22/03/2013
 * @since 2.0.0
 */
@Repository
public class ParameterDAO implements IParameterDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryParameter factoryParameter;

    @Override
    public Parameter findParameterByKey(String key) throws CerberusException {
        boolean throwExep = false;
        Parameter result = null;
        final String query = "SELECT * FROM parameter p WHERE p.param = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, key);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String value = resultSet.getString("value");
                        String desc = resultSet.getString("description");
                        result = factoryParameter.create(key, value, desc);
                    } else {
                        throwExep = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ParameterDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ParameterDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ParameterDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExep) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
            mes.setDescription(mes.getDescription() + " Parameter not defined : " + key);
            throw new CerberusException(mes);
        }
        return result;
    }

    @Override
    public List<Parameter> findAllParameter() throws CerberusException {
        boolean throwExep = true;
        List<Parameter> result = null;
        Parameter paramet = null;
        final String query = "SELECT * FROM parameter p ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<Parameter>();
                    while (resultSet.next()) {
                        String param = resultSet.getString("param");
                        String value = resultSet.getString("value");
                        String desc = resultSet.getString("description");
                        paramet = factoryParameter.create(param, value, desc);
                        result.add(paramet);
                        throwExep = false;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ParameterDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ParameterDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ParameterDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExep) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
            mes.setDescription(mes.getDescription() + " Parameter table empty.");
            throw new CerberusException(mes);
        }
        return result;
    }

    @Override
    public void updateParameter(Parameter parameter) throws CerberusException {

        final String query = "UPDATE parameter SET Value = ? WHERE param = ? ;";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, parameter.getValue());
                preStat.setString(2, parameter.getParam());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(ParameterDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ParameterDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }


}
