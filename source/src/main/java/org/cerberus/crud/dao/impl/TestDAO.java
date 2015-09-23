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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.Test;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTest;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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

    private static final Logger LOG = Logger.getLogger(LogEventDAO.class);

    private final int MAX_ROW_SELECTED = 100000;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here.
     * <p>
     * And even more explanations to follow in consecutive paragraphs separated
     * by HTML paragraph breaks.
     *
     * @param variable Description text text text.
     * @return Description text text text.
     */
    @Override
    public List<Test> findAllTest() {

        return findTestByCriteria(new Test());
    }

    @Override
    public AnswerItem readByKey(String test) {
        AnswerItem ans = new AnswerItem();
        Test result = null;
        final String query = "SELECT * FROM `test` WHERE `test` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadTestFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Test").replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }
    
    @Override
    public List<Test> findTestByCriteria(Test test) {
        List<Test> result = null;
        StringBuilder query = new StringBuilder("SELECT Test, Description, Active, Automated, TDateCrea FROM test ");

        StringBuilder whereClause = new StringBuilder("WHERE 1=1 ");

        List<String> parameters = new ArrayList<String>();

        Connection connection = this.databaseSpring.connect();
        try {
            if (test.getTest() != null && !"".equals(test.getTest().trim())) {
                whereClause.append("AND Test LIKE ? ");
                parameters.add(test.getTest());
            }

            if (test.getDescription() != null && !"".equals(test.getDescription().trim())) {
                whereClause.append("AND Description LIKE ? ");
                parameters.add(test.getDescription());
            }

            if (test.getActive() != null && !"".equals(test.getActive().trim())) {
                whereClause.append("AND Active LIKE ? ");
                parameters.add(test.getActive());
            }

            if (test.getAutomated() != null && !"".equals(test.getAutomated().trim())) {
                whereClause.append("AND Automated LIKE ? ");
                parameters.add(test.getAutomated());
            }

            if (test.gettDateCrea() != null && !"".equals(test.gettDateCrea().trim())) {
                whereClause.append("AND TDateCrea LIKE ? ");
                parameters.add(test.gettDateCrea());
            }
            if (parameters.size() > 0) {
                query.append(whereClause);
            }

            MyLogger.log(TestDAO.class.getName(), Level.DEBUG, "Query : Test.findTestByCriteria : " + query.toString());
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            if (parameters.size() > 0) {
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
                        if (resultSet != null) {
                            result.add(this.loadTestFromResultSet(resultSet));
                        }
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public boolean createTest(Test test) throws CerberusException {
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
                MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.GUI_ERROR_INSERTING_DATA);
                mes.setDescription(mes.getDescription().replace("%DETAILS%", exception.toString()));
                throw new CerberusException(mes);
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.GUI_ERROR_INSERTING_DATA);
            mes.setDescription(mes.getDescription().replace("%DETAILS%", exception.toString()));
            throw new CerberusException(mes);
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
                MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public Answer update(Test test) {
        MessageEvent msg = null;
        final String query = "UPDATE test SET description = ?, active = ?, automated = ? WHERE test = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test.getDescription());
                preStat.setString(2, test.getActive());
                preStat.setString(3, test.getAutomated());
                preStat.setString(4, test.getTest());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test").replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    private Test loadTestFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet == null) {
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
                    MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public List<Test> findListOfTestBySystems(List<String> systems) {
        List<Test> result = null;
        StringBuilder query = new StringBuilder("SELECT t.Test, t.Description, t.Active, t.Automated, t.TDateCrea FROM test t ");
        query.append("JOIN testcase tc ON t.test=tc.test ");
        query.append("JOIN application a ON tc.application=a.application ");
        query.append("WHERE a.system IN (");

        Connection connection = this.databaseSpring.connect();
        try {
            for (int a = 0; a < systems.size(); a++) {
                if (a != systems.size() - 1) {
                    query.append(" ? , ");
                }
                query.append("? ) GROUP BY t.test");
            }
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            for (int a = 0; a < systems.size(); a++) {
                preStat.setString(a + 1, ParameterParserUtil.wildcardIfEmpty(systems.get(a)));
            }
            try {
                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<Test>();
                try {
                    while (resultSet.next()) {
                        result.add(this.loadTestFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Test> testList = new ArrayList<Test>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM test ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`test` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `description` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `active` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `automated` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or `tdatecrea` like '%").append(searchTerm).append("%')");
        }
        if (!StringUtil.isNullOrEmpty(individualSearch)) {
            searchSQL.append(" and (`").append(individualSearch).append("`)");
        }
        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(colName)) {
            query.append("order by `").append(colName).append("` ").append(dir);
        }
        if (amount != 0) {
            query.append(" limit ").append(start).append(" , ").append(amount);
        } else {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testList.add(this.loadTestFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test").replace("%OPERATION%", "SELECT"));
                    response = new AnswerList(testList, nrTotalRows);

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        return response;
    }
}
