/*
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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTCase;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.impl.ApplicationService;
import org.cerberus.crud.service.impl.InvariantService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Used to manage TestCase table
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/12/2012
 * @since 0.9.0
 */
@Repository
public class TestCaseDAO implements ITestCaseDAO {

    /**
     * Class used to manage connection.
     *
     * @see org.cerberus.database.DatabaseSpring
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTCase factoryTestCase;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private InvariantService invariantService;

    /**
     * Get summary information of all test cases of one group.
     * <p/>
     * Used to display list of test cases on drop-down list
     *
     * @param test Name of test group.
     * @return List with a list of 3 strings (name of test case, type of
     * application, description of test case).
     */
    @Override
    public List<TCase> findTestCaseByTest(String test) {
        List<TCase> list = null;
        final String query = "SELECT * FROM testcase WHERE test = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TCase>();

                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    /**
     * Get test case information.
     *
     * @param test Name of test group.
     * @param testCase Name of test case.
     * @return TestCase object or null.
     * @throws org.cerberus.exception.CerberusException
     * @see org.cerberus.crud.entity.TestCase
     */
    @Override
    public TCase findTestCaseByKey(String test, String testCase) throws CerberusException {
        boolean throwExcep = false;
        TCase result = null;
        final String query = "SELECT * FROM testcase WHERE test = ? AND testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        result = this.loadTestCaseFromResultSet(resultSet);
                    } else {
                        result = null;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public boolean updateTestCaseInformation(TestCase testCase) {
        boolean res = false;
        final String sql = "UPDATE testcase tc SET tc.Application = ?, tc.Project = ?, tc.BehaviorOrValueExpected = ?, tc.activeQA = ?, tc.activeUAT = ?, tc.activePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.TcActive = ?, tc.Description = ?, tc.Group = ?, tc.HowTo = ?, tc.Comment = ?, tc.Ticket = ?, tc.FromBuild = ?, "
                + "tc.FromRev = ?, tc.ToBuild = ?, tc.ToRev = ?, tc.BugID = ?, tc.TargetBuild = ?, tc.Implementer = ?, tc.LastModifier = ?, tc.TargetRev = ?, tc.`function` = ? "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, testCase.getApplication());
                preStat.setString(2, testCase.getProject());
                preStat.setString(3, testCase.getDescription());
                preStat.setString(4, testCase.isRunQA() ? "Y" : "N");
                preStat.setString(5, testCase.isRunUAT() ? "Y" : "N");
                preStat.setString(6, testCase.isRunPROD() ? "Y" : "N");
                preStat.setString(7, Integer.toString(testCase.getPriority()));
                preStat.setString(8, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(9, testCase.isActive() ? "Y" : "N");
                preStat.setString(10, ParameterParserUtil.parseStringParam(testCase.getShortDescription(), ""));
                preStat.setString(11, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(12, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(13, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(14, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(15, ParameterParserUtil.parseStringParam(testCase.getFromSprint(), ""));
                preStat.setString(16, ParameterParserUtil.parseStringParam(testCase.getFromRevision(), ""));
                preStat.setString(17, ParameterParserUtil.parseStringParam(testCase.getToSprint(), ""));
                preStat.setString(18, ParameterParserUtil.parseStringParam(testCase.getToRevision(), ""));
                preStat.setString(19, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(20, ParameterParserUtil.parseStringParam(testCase.getTargetSprint(), ""));
                preStat.setString(21, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(22, ParameterParserUtil.parseStringParam(testCase.getLastModifier(), ""));
                preStat.setString(23, ParameterParserUtil.parseStringParam(testCase.getTargetRevision(), ""));
                preStat.setString(24, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(25, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(26, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean updateTestCaseInformationCountries(TestCase tc) {
        boolean res = false;
        final String sql_count = "SELECT Country FROM testcasecountry WHERE Test = ? AND TestCase = ?";
        ArrayList<String> countriesDB = new ArrayList<String>();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql_count);
            try {
                preStat.setString(1, tc.getTest());
                preStat.setString(2, tc.getTestCase());
                ResultSet rsCount = preStat.executeQuery();
                try {
                    while (rsCount.next()) {
                        countriesDB.add(rsCount.getString("Country"));
                        if (!tc.getCountryList().contains(rsCount.getString("Country"))) {
                            final String sql_delete = "DELETE FROM testcasecountry WHERE Test = ? AND TestCase = ? AND Country = ?";

                            PreparedStatement preStat2 = connection.prepareStatement(sql_delete);
                            try {
                                preStat2.setString(1, tc.getTest());
                                preStat2.setString(2, tc.getTestCase());
                                preStat2.setString(3, rsCount.getString("Country"));

                                preStat2.executeUpdate();
                            } catch (SQLException exception) {
                                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                            } finally {
                                preStat2.close();
                            }
                        }
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    rsCount.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

            res = true;
            for (int i = 0; i < tc.getCountryList().size() && res; i++) {
                if (!countriesDB.contains(tc.getCountryList().get(i))) {
                    final String sql_insert = "INSERT INTO testcasecountry (test, testcase, country) VALUES (?, ?, ?)";

                    PreparedStatement preStat2 = connection.prepareStatement(sql_insert);
                    try {
                        preStat2.setString(1, tc.getTest());
                        preStat2.setString(2, tc.getTestCase());
                        preStat2.setString(3, tc.getCountryList().get(i));

                        res = preStat2.executeUpdate() > 0;
                    } catch (SQLException exception) {
                        MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    } finally {
                        preStat2.close();
                    }
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean createTestCase(TCase testCase) {
        boolean res = false;

        final StringBuffer sql = new StringBuffer("INSERT INTO `testcase` ")
                .append(" ( `Test`, `TestCase`, `Application`, `Project`, `Ticket`, ")
                .append("`Description`, `BehaviorOrValueExpected`, ")
                .append("`ChainNumberNeeded`, `Priority`, `Status`, `TcActive`, ")
                .append("`Group`, `Origine`, `RefOrigine`, `HowTo`, `Comment`, ")
                .append("`FromBuild`, `FromRev`, `ToBuild`, `ToRev`, ")
                .append("`BugID`, `TargetBuild`, `TargetRev`, `Creator`, ")
                .append("`Implementer`, `LastModifier`, `function`, `activeQA`, `activeUAT`, `activePROD`) ")
                .append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ? ); ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(2, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));
                preStat.setString(3, ParameterParserUtil.parseStringParam(testCase.getApplication(), ""));
                preStat.setString(4, ParameterParserUtil.parseStringParam(testCase.getProject(), ""));
                preStat.setString(5, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(6, ParameterParserUtil.parseStringParam(testCase.getShortDescription(), ""));
                preStat.setString(7, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(8, null);
                preStat.setString(9, Integer.toString(testCase.getPriority()));
                preStat.setString(10, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(11, testCase.getActive() != null && !testCase.getActive().equals("Y") ? "N" : "Y");
                preStat.setString(12, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(13, "");
                preStat.setString(14, "");
                preStat.setString(15, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(16, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(17, ParameterParserUtil.parseStringParam(testCase.getFromSprint(), ""));
                preStat.setString(18, ParameterParserUtil.parseStringParam(testCase.getFromRevision(), ""));
                preStat.setString(19, ParameterParserUtil.parseStringParam(testCase.getToSprint(), ""));
                preStat.setString(20, ParameterParserUtil.parseStringParam(testCase.getToRevision(), ""));
                preStat.setString(21, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(22, ParameterParserUtil.parseStringParam(testCase.getTargetSprint(), ""));
                preStat.setString(23, ParameterParserUtil.parseStringParam(testCase.getTargetRevision(), ""));
                preStat.setString(24, ParameterParserUtil.parseStringParam(testCase.getCreator(), ""));
                preStat.setString(25, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(26, ParameterParserUtil.parseStringParam(testCase.getLastModifier(), ""));
                preStat.setString(27, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(28, testCase.getRunQA() != null && !testCase.getRunQA().equals("Y") ? "N" : "Y");
                preStat.setString(29, testCase.getRunUAT() != null && !testCase.getRunUAT().equals("Y") ? "N" : "Y");
                preStat.setString(30, testCase.getRunPROD() != null && !testCase.getRunPROD().equals("N") ? "Y" : "N");

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<TCase> findTestCaseByCriteria(String test, String application, String country, String active) {
        List<TCase> list = null;
        final String query = "SELECT tc.* FROM testcase tc JOIN testcasecountry tcc "
                + "WHERE tc.test=tcc.test AND tc.testcase=tcc.testcase "
                + "AND tc.test = ? AND tc.application = ? AND tcc.country = ? AND tc.tcactive = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, application);
                preStat.setString(3, country);
                preStat.setString(4, active);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TCase> findTestCaseByCampaignName(String campaign) {
        List<TCase> list = null;
        final String query = new StringBuilder("select tc.* ")
                .append("from testcase tc ")
                .append("inner join testbatterycontent tbc ")
                .append("on tbc.Test = tc.Test ")
                .append("and tbc.TestCase = tc.TestCase ")
                .append("inner join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ")
                .append("where cc.campaign = ? ")
                .toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, campaign);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    /**
     * @since 0.9.1
     */
    @Override
    public List<TCase> findTestCaseByCriteria(TCase testCase, String text, String system) {
        List<TCase> list = null;
        String query = new StringBuilder()
                .append("SELECT t2.* FROM testcase t2 LEFT OUTER JOIN application a ON a.application=t2.application ")
                .append(" WHERE (t2.test LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.test", testCase.getTest()))
                .append(") AND (t2.project LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.project", testCase.getProject()))
                .append(") AND (t2.ticket LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.ticket", testCase.getTicket()))
                .append(") AND (t2.bugid LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.bugid", testCase.getBugID()))
                .append(") AND (t2.origine LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.origine", testCase.getOrigin()))
                .append(") AND (a.system LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("a.system", system))
                .append(") AND (t2.application LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.application", testCase.getApplication()))
                .append(") AND (t2.priority LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfMinusOne("t2.priority", testCase.getPriority()))
                .append(") AND (t2.status LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.status", testCase.getStatus()))
                .append(") AND (t2.group LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.group", testCase.getGroup()))
                .append(") AND (t2.activePROD LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.activePROD", testCase.getRunPROD()))
                .append(") AND (t2.activeUAT LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.activeUAT", testCase.getRunUAT()))
                .append(") AND (t2.activeQA LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.activeQA", testCase.getRunQA()))
                .append(") AND (t2.description LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.description", text))
                .append(" OR t2.howto LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.howto", text))
                .append(" OR t2.behaviororvalueexpected LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.behaviororvalueexpected", text))
                .append(" OR t2.comment LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.comment", text))
                .append(") AND (t2.TcActive LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.TcActive", testCase.getActive()))
                .append(") AND (t2.frombuild LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.frombuild", testCase.getFromSprint()))
                .append(") AND (t2.fromrev LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.fromrev", testCase.getFromRevision()))
                .append(") AND (t2.tobuild LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.tobuild", testCase.getToSprint()))
                .append(") AND (t2.torev LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.torev", testCase.getToRevision()))
                .append(") AND (t2.targetbuild LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.targetbuild", testCase.getTargetSprint()))
                .append(") AND (t2.targetrev LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.targetrev", testCase.getTargetRevision()))
                .append(") AND (t2.testcase LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.testcase", testCase.getTestCase()))
                .append(") AND (t2.function LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.function", testCase.getFunction()))
                .append(")").toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    /**
     * @since 0.9.1
     */
    private TCase loadTestCaseFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("Test");
        String testCase = resultSet.getString("TestCase");
        String tcapplication = resultSet.getString("Application");
        String project = resultSet.getString("Project");
        String ticket = resultSet.getString("Ticket");
        String description = resultSet.getString("Description");
        String behavior = resultSet.getString("BehaviorOrValueExpected");
        int priority = resultSet.getInt("Priority");
        String status = resultSet.getString("Status");
        String tcactive = resultSet.getString("TcActive");
        String group = resultSet.getString("Group");
        String origin = resultSet.getString("Origine");
        String refOrigin = resultSet.getString("RefOrigine");
        String howTo = resultSet.getString("HowTo");
        String comment = resultSet.getString("Comment");
        String fromSprint = resultSet.getString("FromBuild");
        String fromRevision = resultSet.getString("FromRev");
        String toSprint = resultSet.getString("ToBuild");
        String toRevision = resultSet.getString("ToRev");
        String bugID = resultSet.getString("BugID");
        String targetSprint = resultSet.getString("TargetBuild");
        String targetRevision = resultSet.getString("TargetRev");
        String creator = resultSet.getString("Creator");
        String implementer = resultSet.getString("Implementer");
        String lastModifier = resultSet.getString("LastModifier");
        String runQA = resultSet.getString("activeQA");
        String runUAT = resultSet.getString("activeUAT");
        String runPROD = resultSet.getString("activePROD");
        String function = resultSet.getString("function");
        String dateCrea = "";
        try { // Managing the case where the date is 0000-00-00 00:00:00 inside MySQL
            dateCrea = resultSet.getString("tcdatecrea");
        } catch (SQLException e) {
            dateCrea = "-- unknown --";
        }

        return factoryTestCase.create(test, testCase, origin, refOrigin, creator, implementer,
                lastModifier, project, ticket, function, tcapplication, runQA, runUAT, runPROD, priority, group,
                status, description, behavior, howTo, tcactive, fromSprint, fromRevision, toSprint,
                toRevision, status, bugID, targetSprint, targetRevision, comment, dateCrea);
    }

    @Override
    public List<String> findUniqueDataOfColumn(String column) {
        List<String> list = null;
        final String query = "SELECT DISTINCT tc." + column + " FROM testcase tc ORDER BY tc." + column + " ASC";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<String>();
                try {
                    while (resultSet.next()) {
                        list.add(resultSet.getString(1));

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class
                            .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();

                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class
                    .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public boolean deleteTestCase(TCase testCase) {
        boolean bool = false;
        final String query = "DELETE FROM testcase WHERE test = ? AND testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testCase.getTest());
                preStat.setString(2, testCase.getTestCase());

                bool = preStat.executeUpdate() > 0;

            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class
                        .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class
                    .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    @Override
    public void updateTestCaseField(TCase tc, String columnName, String value) {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update testcase set `");
        query.append(columnName);
        query.append("`=? where `test`=? and `testcase`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, value);
                preStat.setString(2, tc.getTest());
                preStat.setString(3, tc.getTestCase());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class
                    .getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }

    }

    /**
     * @param testCase
     * @param system
     * @return
     * @since 1.0.2
     */
    @Override
    public List<TCase> findTestCaseByGroupInCriteria(TCase testCase, String system) {
        List<TCase> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT t2.* FROM testcase t2 LEFT OUTER JOIN application a ON a.application=t2.application WHERE 1=1");
        if (!StringUtil.isNull(testCase.getTest())) {
            query.append(" AND t2.test IN (");
            query.append(testCase.getTest());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getProject())) {
            query.append(" AND t2.project IN (");
            query.append(testCase.getProject());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTicket())) {
            query.append(" AND t2.ticket IN (");
            query.append(testCase.getTicket());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTicket())) {
            query.append(" AND t2.ticket IN (");
            query.append(testCase.getTicket());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getBugID())) {
            query.append(" AND t2.bugid IN (");
            query.append(testCase.getBugID());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getOrigin())) {
            query.append(" AND t2.origine IN (");
            query.append(testCase.getOrigin());
            query.append(") ");
        }
        if (!StringUtil.isNull(system)) {
            query.append(" AND a.system IN (");
            query.append(system);
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getApplication())) {
            query.append(" AND t2.application IN (");
            query.append(testCase.getApplication());
            query.append(") ");
        }
        if (testCase.getPriority() != -1) {
            query.append(" AND t2.priority IN (");
            query.append(testCase.getPriority());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getStatus())) {
            query.append(" AND t2.status IN (");
            query.append(testCase.getStatus());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getGroup())) {
            query.append(" AND t2.group IN (");
            query.append(testCase.getGroup());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getRunPROD())) {
            query.append(" AND t2.activePROD IN (");
            query.append(testCase.getRunPROD());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getRunUAT())) {
            query.append(" AND t2.activeUAT IN (");
            query.append(testCase.getRunUAT());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getRunQA())) {
            query.append(" AND t2.activeQA IN (");
            query.append(testCase.getRunQA());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getShortDescription())) {
            query.append(" AND t2.description LIKE '%");
            query.append(testCase.getShortDescription());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getHowTo())) {
            query.append(" AND t2.howto LIKE '%");
            query.append(testCase.getHowTo());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getDescription())) {
            query.append(" AND t2.behaviororvalueexpected LIKE '%");
            query.append(testCase.getDescription());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getComment())) {
            query.append(" AND t2.comment LIKE '%");
            query.append(testCase.getComment());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getActive())) {
            query.append(" AND t2.TcActive IN (");
            query.append(testCase.getActive());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getFromSprint())) {
            query.append(" AND t2.frombuild IN (");
            query.append(testCase.getFromSprint());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getFromRevision())) {
            query.append(" AND t2.fromrev IN (");
            query.append(testCase.getFromRevision());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getToSprint())) {
            query.append(" AND t2.tobuild IN (");
            query.append(testCase.getToSprint());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getToRevision())) {
            query.append(" AND t2.torev IN (");
            query.append(testCase.getToRevision());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTargetSprint())) {
            query.append(" AND t2.targetbuild IN (");
            query.append(testCase.getTargetSprint());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTargetRevision())) {
            query.append(" AND t2.targetrev IN (");
            query.append(testCase.getTargetRevision());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTestCase())) {
            query.append(" AND t2.testcase IN (");
            query.append(testCase.getTestCase());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getFunction())) {
            query.append(" AND t2.function IN (");
            query.append(testCase.getFunction());
            query.append(") ");
        }
        query.append(" ORDER BY t2.test, t2.testcase");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public void updateTestCase(TCase testCase) throws CerberusException {
        final String sql = "UPDATE testcase tc SET tc.Application = ?, tc.Project = ?, tc.BehaviorOrValueExpected = ?, tc.activeQA = ?, tc.activeUAT = ?, tc.activePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.TcActive = ?, tc.Description = ?, tc.Group = ?, tc.HowTo = ?, tc.Comment = ?, tc.Ticket = ?, tc.FromBuild = ?, "
                + "tc.FromRev = ?, tc.ToBuild = ?, tc.ToRev = ?, tc.BugID = ?, tc.TargetBuild = ?, tc.Implementer = ?, tc.LastModifier = ?, tc.TargetRev = ?, tc.`function` = ? "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, testCase.getApplication());
                preStat.setString(2, testCase.getProject());
                preStat.setString(3, testCase.getDescription());
                preStat.setString(4, testCase.getRunQA().equals("Y") ? "Y" : "N");
                preStat.setString(5, testCase.getRunUAT().equals("Y") ? "Y" : "N");
                preStat.setString(6, testCase.getRunPROD().equals("Y") ? "Y" : "N");
                preStat.setString(7, Integer.toString(testCase.getPriority()));
                preStat.setString(8, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(9, testCase.getActive().equals("Y") ? "Y" : "N");
                preStat.setString(10, ParameterParserUtil.parseStringParam(testCase.getShortDescription(), ""));
                preStat.setString(11, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(12, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(13, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(14, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(15, ParameterParserUtil.parseStringParam(testCase.getFromSprint(), ""));
                preStat.setString(16, ParameterParserUtil.parseStringParam(testCase.getFromRevision(), ""));
                preStat.setString(17, ParameterParserUtil.parseStringParam(testCase.getToSprint(), ""));
                preStat.setString(18, ParameterParserUtil.parseStringParam(testCase.getToRevision(), ""));
                preStat.setString(19, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(20, ParameterParserUtil.parseStringParam(testCase.getTargetSprint(), ""));
                preStat.setString(21, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(22, ParameterParserUtil.parseStringParam(testCase.getLastModifier(), ""));
                preStat.setString(23, ParameterParserUtil.parseStringParam(testCase.getTargetRevision(), ""));
                preStat.setString(24, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(25, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(26, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public List<TCase> findTestCaseByTestSystems(String test, List<String> systems) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMaxNumberTestCase(String test) {
        String max = "";
        final String sql = "SELECT  Max( Testcase ) + 0 as MAXTC FROM testcase where test = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, test);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        max = resultSet.getString("MAXTC");
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return max;
    }

    @Override
    public List<TCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries) {
        List<TCase> list = null;
        final StringBuilder query = new StringBuilder("select tc.* ")
                .append("from testcase tc ")
                .append("inner join testcasecountry tcc ")
                .append("on tcc.Test = tc.Test ")
                .append("and tcc.TestCase = tc.TestCase ")
                .append("inner join testbatterycontent tbc ")
                .append("on tbc.Test = tc.Test ")
                .append("and tbc.TestCase = tc.TestCase ")
                .append("inner join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ")
                .append("where cc.campaign = ? ");

        query.append(" and tcc.Country in (");
        for (int i = 0; i < countries.length; i++) {
            query.append("?");
            if (i < countries.length - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int index = 1;
                preStat.setString(index, campaign);
                index++;

                for (String c : countries) {
                    preStat.setString(index, c);
                    index++;
                }

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TCase> findTestCaseByTestSystem(String test, String system) {
        List<TCase> list = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM testcase tc join application app on tc.application=app.application ");
        sb.append(" WHERE tc.test = ? and app.system = ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sb.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TCase>();

                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TCase> findTestCaseByCriteria(String testClause, String projectClause, String appClause, String activeClause, String priorityClause, String statusClause, String groupClause, String targetBuildClause, String targetRevClause, String creatorClause, String implementerClause, String functionClause, String campaignClause, String batteryClause) {
        List<TCase> list = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM testcase tc join application app on tc.application=app.application ")
           .append("left join testbatterycontent tbc ")
           .append("on tbc.Test = tc.Test ")
           .append("and tbc.TestCase = tc.TestCase ")
           .append("left join campaigncontent cc ")
           .append("on cc.testbattery = tbc.testbattery ");
        sb.append(" WHERE 1=1 ");
        sb.append(testClause);
        sb.append(projectClause);
        sb.append(appClause);
        sb.append(activeClause);
        sb.append(priorityClause);
        sb.append(statusClause);
        sb.append(groupClause);
        sb.append(targetBuildClause);
        sb.append(targetRevClause);
        sb.append(creatorClause);
        sb.append(implementerClause);
        sb.append(functionClause);
        sb.append(campaignClause);
        sb.append(batteryClause);
        sb.append(" GROUP BY tc.test, tc.testcase ");
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sb.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TCase>();

                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }
    
    @Override
    public String findSystemOfTestCase(String test, String testcase) throws CerberusException{
    String result = "";
        final String sql = "SELECT system from application a join testcase tc on tc.application=a.Application where tc.test= ? and tc.testcase= ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        result = resultSet.getString("system");
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public AnswerList findNumberOfTestCasePerTCStatus(String system, int startPosition, int length, String columnName, String sort, String searchTerm, String individualSearch) {
//        try {
//            AnswerList response = new AnswerList();
//            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
//            List<TCase> tcList = new ArrayList<TCase>();
//            StringBuilder gSearch = new StringBuilder();
//            StringBuilder searchSQL = new StringBuilder();
//            
//            List<Application> appliList = applicationService.readBySystem_Deprecated(system);
//            String inSQL = SqlUtil.getInSQLClause(appliList);
//
//            if (!(inSQL.equalsIgnoreCase(""))) {
//                inSQL = " and application " + inSQL + " ";
//            } else {
//                inSQL = " and application in ('') ";
//            }
//            
//            List<Invariant> myInvariants = invariantService.findInvariantByIdGp1("TCSTATUS", "Y");
//            StringBuilder SQL = new StringBuilder();
//            StringBuilder SQLa = new StringBuilder();
//            StringBuilder SQLb = new StringBuilder();
//            StringBuilder query = new StringBuilder();
//            
//            SQLa.append("SELECT SELECT SQL_CALC_FOUND_ROWS t.application, count(*) as TOTAL ");
//            SQLb.append(" FROM testcase t ");
//            for (Invariant i : myInvariants) {
//                i.getSort();
//                SQLa.append(", Col");
//                SQLa.append(String.valueOf(i.getSort()));
//                SQLb.append(" LEFT JOIN (SELECT g.application, count(*) as Col");
//                SQLb.append(String.valueOf(i.getSort()));
//                SQLb.append(" FROM testcase g WHERE Status = '");
//                SQLb.append(i.getValue());
//                SQLb.append("' ");
//                SQLb.append(inSQL);
//                SQLb.append(" GROUP BY g.application) Tab");
//                SQLb.append(String.valueOf(i.getSort()));
//                SQLb.append(" ON Tab");
//                SQLb.append(String.valueOf(i.getSort()));
//                SQLb.append(".application=t.application ");
//            }
//            SQLb.append(" WHERE 1=1 ");
//            SQLb.append(inSQL.replace("application", "t.application"));
//            SQLb.append(" and (`application` like '%");
//            SQLb.append(searchTerm);
//            SQLb.append("%')");
//            SQLb.append(" GROUP BY t.application ");
//            SQLb.append("order by `");
//            SQLb.append(columnName);
//            SQLb.append("` ");
//            SQLb.append(sort);
//            SQLb.append(" limit ");
//            SQLb.append(startPosition);
//            SQLb.append(" , ");
//            SQLb.append(length);
//            
//            SQL.append(SQLa);
//            SQL.append(SQLb);
//            MyLogger.log(TestCaseDAO.class.getName(), Level.DEBUG, " SQL1 : " + SQL.toString());
//
//            Connection connection = this.databaseSpring.connect();
//            try {
//                PreparedStatement preStat = connection.prepareStatement(SQL.toString());
//                try {
//                    ResultSet resultSet = preStat.executeQuery();
//                    try {
//                        //gets the data
//                        while (resultSet.next()) {
//                            projectList.add(this.loadProjectFromResultSet(resultSet));
//                        }
//                        
//                        //get the total number of rows
//                        resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
//                        int nrTotalRows = 0;
//                        
//                        if (resultSet != null && resultSet.next()) {
//                            nrTotalRows = resultSet.getInt(1);
//                        }
//                        
//                        response = new AnswerList(projectList, nrTotalRows);
//                        
//                    } catch (SQLException exception) {
//                        MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
//                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
//                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
//                        
//                    } finally {
//                        if (resultSet != null) {
//                            resultSet.close();
//                        }
//                    }
//
//                } catch (SQLException exception) {
//                    MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
//                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
//                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
//                } finally {
//                    if (preStat != null) {
//                        preStat.close();
//                    }
//                }
//
//            } catch (SQLException exception) {
//                MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
//                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
//                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
//            } finally {
//                try {
//                    if (!this.databaseSpring.isOnTransaction()) {
//                        if (connection != null) {
//                            connection.close();
//                        }
//                    }
//                } catch (SQLException ex) {
//                    MyLogger.log(ProjectDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
//                }
//            }
//            response.setResultMessage(msg);
//            response.setDataList(projectList);
//            return response;
//        } catch (CerberusException ex) {
//            Logger.getLogger(TestCaseDAO.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
    return null;}
}
