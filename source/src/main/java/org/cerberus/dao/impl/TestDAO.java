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
import org.cerberus.dao.ITestDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Test;
import org.cerberus.factory.IFactoryTest;
import org.cerberus.log.MyLogger;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
       
        return findTestByCriteria(new Test());
    }

    public List<Test> findTestByCriteria(Test test) {
        List<Test> result = null;
        StringBuilder query = new StringBuilder("SELECT Test, Description, Active, Automated, TDateCrea FROM test ");

        StringBuilder whereClause = new StringBuilder("WHERE 1=1 ");
        
        List<String> parameters = new ArrayList<String>();
        
        Connection connection = this.databaseSpring.connect();
        try {
            if(test.getTest() != null && !"".equals(test.getTest().trim())) {
                whereClause.append("AND Test LIKE ? ");
                parameters.add(test.getTest());
            }

            if(test.getDescription()!= null && !"".equals(test.getDescription().trim())) {
                whereClause.append("AND Description LIKE ? ");
                parameters.add(test.getDescription());
            }

            if(test.getActive()!= null && !"".equals(test.getActive().trim())) {
                whereClause.append("AND Active LIKE ? ");
                parameters.add(test.getActive());
            }

            if(test.getAutomated()!= null && !"".equals(test.getAutomated().trim())) {
                whereClause.append("AND Automated LIKE ? ");
                parameters.add(test.getAutomated());
            }

            if(test.gettDateCrea()!= null && !"".equals(test.gettDateCrea().trim())) {
                whereClause.append("AND TDateCrea LIKE ? ");
                parameters.add(test.gettDateCrea());
            }
            if(parameters.size() > 0) {
                query.append(whereClause);
            }

            MyLogger.log(TestDAO.class.getName(), Level.DEBUG, "Query : Test.findTestByCriteria : " + query.toString());
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            if(parameters.size() > 0) {
                int index = 0;
                for (String parameter : parameters) {
                    index++;
                    preStat.setString(index, ParameterParserUtil.wildcardIfEmpty(parameter));
                }
            }
            try {
                
                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<Test>();
                try {
                    while (resultSet.next()) {
                        if(resultSet != null) {
                            result.add(this.loadTestFromResultSet(resultSet));
                        }
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
                MyLogger.log(TestDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;    
    }
    
    @Override
    public boolean createTest(Test test) {
        boolean res = false;
        final String sql = "INSERT INTO test (Test, Description, Active, Automated, TDateCrea) VALUES (?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, test.getTest());
                preStat.setString(2, test.getDescription());
                preStat.setString(3, test.getActive());
                preStat.setString(4, test.getAutomated());
                preStat.setString(5, DateUtil.getMySQLTimestampTodayDeltaMinutes(0));

                res = preStat.executeUpdate() > 0;
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
                MyLogger.log(TestDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean deleteTest(Test test) {
        boolean res = false;
        final String sql = "DELETE FROM test where Test = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, test.getTest());

                res = preStat.executeUpdate() > 0;
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
                MyLogger.log(TestDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;
    }

    private Test loadTestFromResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet == null) {
            return null;
        }

        String test = resultSet.getString("test") == null ? "" : resultSet.getString("test");
        String description = resultSet.getString("description") == null ? "" : resultSet.getString("description");
        String active = resultSet.getString("active") == null ? "" : resultSet.getString("active");
        String automated = resultSet.getString("automated") == null ? "" : resultSet.getString("automated");

        String tcactive;
        try {
            tcactive = resultSet.getString("tdatecrea") == null ? "" : resultSet.getString("tdatecrea");
        } catch (java.sql.SQLException e) {
            MyLogger.log(TestDAO.class.getName(), Level.WARN, e.toString());
            tcactive = DateUtil.getMySQLTimestampTodayDeltaMinutes(0);
        }

        return factoryTest.create(test, description, active, automated, tcactive);
    }

    @Override
    public Test findTestByKey(String test) {
        Test result = null;
        StringBuilder query = new StringBuilder("SELECT Test, Description, Active, Automated, TDateCrea FROM test ");
        query.append(" where test = ?");
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            
            try {
                preStat.setString(1, test);
                ResultSet resultSet = preStat.executeQuery();
                
                try {
                    if (resultSet.next()) {
                        result = loadTestFromResultSet(resultSet);
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
                MyLogger.log(TestDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result; 
    }
}
