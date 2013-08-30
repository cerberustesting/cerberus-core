package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IMyVersionDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.MyVersion;
import com.redcats.tst.factory.IFactoryMyversion;
import com.redcats.tst.factory.impl.FactoryMyversion;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {Insert class description here}
 *
 * @author Benoit Dumont
 * @version 1.0, 09/06/2013
 * @since 2.0.0
 */
@Repository
public class MyVersionDAO implements IMyVersionDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryMyversion factoryMyversion;

    @Override
    public MyVersion findMyVersionByKey(String key) {
        MyVersion result = new MyVersion();
        final String query = "SELECT mv.value FROM myversion mv WHERE mv.`key` = ? ";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, key);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        factoryMyversion = new FactoryMyversion();
                        result = factoryMyversion.create(key, Integer.valueOf(resultSet.getString("value")));
                    }
                } catch (SQLException exception) {
                    result = null;
                    MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                result = null;
                MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return result;
    }

    @Override
    public boolean updateMyVersion(MyVersion myVersion) {
        boolean result = false;
        final String query = "UPDATE myversion SET value = ? WHERE `key` = ? ";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setInt(1, myVersion.getValue());
            preStat.setString(2, myVersion.getKey());
            try {
                result = preStat.execute();
            } catch (SQLException exception) {
                MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return result;
    }
}
