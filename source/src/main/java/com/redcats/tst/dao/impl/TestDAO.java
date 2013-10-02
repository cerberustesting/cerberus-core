package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.Test;
import com.redcats.tst.factory.IFactoryTest;
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
 * @version 1.0, 19/Dez/2012
 * @since 2.0.0
 */
@Repository
public class TestDAO implements ITestDAO {
    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTest factoryTest;

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
    public List<Test> findAllTest() {
        List<Test> result = null;
        final String query = "SELECT Test, Description, active, automated, tdatecrea FROM test";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<Test>();
                try {
                    while (resultSet.next()) {
                        Test testToAdd = null;
                        String test = resultSet.getString("test") == null ? "" : resultSet.getString("test");
                        String description = resultSet.getString("description") == null ? "" : resultSet.getString("description");
                        String active = resultSet.getString("active") == null ? "" : resultSet.getString("active");
                        String automated = resultSet.getString("automated") == null ? "" : resultSet.getString("automated");
//                        String tdatecrea = resultSet.getString("tdatecrea") == null ? "" : resultSet.getString("tdatecrea");
                        testToAdd = factoryTest.create(test, description, active, automated, "");
                        result.add(testToAdd);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }
}
