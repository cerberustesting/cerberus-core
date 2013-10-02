package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ISqlLibraryDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.SqlLibrary;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactorySqlLibrary;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
@Repository
public class SqlLibraryDAO implements ISqlLibraryDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactorySqlLibrary factorySqlLib;

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
    public SqlLibrary findSqlLibraryByKey(String name) throws CerberusException {
        boolean throwEx = false;
        SqlLibrary result = null;
        final String query = "SELECT * FROM sqllibrary  WHERE NAME = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, name);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String type = resultSet.getString("Type");
                        String script = resultSet.getString("Script");
                        String description = resultSet.getString("Description");
                        result = factorySqlLib.create(type, name, script, description);
                    } else {
                        throwEx = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(SqlLibraryDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(SqlLibraryDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(SqlLibraryDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(SqlLibraryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.SQLLIB_NOT_FOUND));
        }
        return result;
    }
}
