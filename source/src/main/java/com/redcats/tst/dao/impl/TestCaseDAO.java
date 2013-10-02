package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestCaseDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.TCase;
import com.redcats.tst.entity.TestCase;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryTCase;
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
 * Used to manage TestCase table
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/12/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseDAO implements ITestCaseDAO {

    /**
     * Class used to manage connection.
     *
     * @see com.redcats.tst.database.DatabaseSpring
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTCase factoryTestCase;

    /**
     * Get summary information of all test cases of one group.
     * <p/>
     * Used to display list of test cases on drop-down list
     *
     * @param test Name of test group.
     * @return List with a list of 3 strings (name of test case, type of
     *         application, description of test case).
     */
    @Override
    public List<TCase> findTestCaseByTest(String test) {
        List<TCase> list = null;
        final String query = "SELECT TestCase, Application, Description FROM testcase WHERE test = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TCase>();

                    while (resultSet.next()) {
                        String testCase = resultSet.getString("TestCase");
                        String application = resultSet.getString("Application");
                        String project = resultSet.getString("Project");
                        String ticket = resultSet.getString("Ticket");
                        String description = resultSet.getString("Description");
                        String behavior = resultSet.getString("BehaviorOrValueExpected");
                        int priority = resultSet.getInt("Priority");
                        String status = resultSet.getString("Status");
                        String active = resultSet.getString("TcActive");
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
                        list.add(factoryTestCase.create(test, testCase, origin, refOrigin, creator, implementer,
                                lastModifier, project, ticket, application, runQA, runUAT, runPROD, priority, group,
                                status, description, behavior, howTo, active, fromSprint, fromRevision, toSprint,
                                toRevision, status, bugID, targetSprint, targetRevision, comment, null, null, null, null));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
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
     * @param test     Name of test group.
     * @param testCase Name of test case.
     * @return TestCase object or null.
     * @see com.redcats.tst.entity.TestCase
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
                    if (resultSet.first()) {
                        String application = resultSet.getString("Application");
                        String project = resultSet.getString("Project");
                        String ticket = resultSet.getString("Ticket");
                        String description = resultSet.getString("Description");
                        String behavior = resultSet.getString("BehaviorOrValueExpected");
                        int priority = resultSet.getInt("Priority");
                        String status = resultSet.getString("Status");
                        String active = resultSet.getString("TcActive");
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
                        result = factoryTestCase.create(test, testCase, origin, refOrigin, creator, implementer,
                                lastModifier, project, ticket, application, runQA, runUAT, runPROD, priority, group,
                                status, description, behavior, howTo, active, fromSprint, fromRevision, toSprint,
                                toRevision, status, bugID, targetSprint, targetRevision, comment, null, null, null, null);
                    } else {
                        throwExcep = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
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
        final String sql = "UPDATE Testcase tc SET tc.Application = ?, tc.Project = ?, tc.BehaviorOrValueExpected = ?, tc.activeQA = ?, tc.activeUAT = ?, tc.activePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.TcActive = ?, tc.Description = ?, tc.Group = ?, tc.HowTo = ?, tc.Comment = ?, tc.Ticket = ?, tc.FromBuild = ?, "
                + "tc.FromRev = ?, tc.ToBuild = ?, tc.ToRev = ?, tc.BugID = ?, tc.TargetBuild = ?, tc.Implementer = ?, tc.LastModifier = ?, tc.TargetRev = ? "
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
                preStat.setString(8, testCase.getStatus());
                preStat.setString(9, testCase.isActive() ? "Y" : "N");
                preStat.setString(10, testCase.getShortDescription());
                preStat.setString(11, testCase.getGroup());
                preStat.setString(12, testCase.getComment());
                preStat.setString(13, testCase.getTicket());
                preStat.setString(14, testCase.getFromSprint());
                preStat.setString(15, testCase.getFromRevision());
                preStat.setString(16, testCase.getToSprint());
                preStat.setString(17, testCase.getToRevision());
                preStat.setString(18, testCase.getBugID());
                preStat.setString(19, testCase.getTargetSprint());
                preStat.setString(20, testCase.getImplementer());
                preStat.setString(21, testCase.getLastModifier());
                preStat.setString(22, testCase.getTargetRevision());
                preStat.setString(23, testCase.getTest());
                preStat.setString(24, testCase.getTestCase());

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
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
        final String sql_count = "SELECT Country FROM TestCaseCountry WHERE Test = ? AND TestCase = ?";
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
                            final String sql_delete = "DELETE FROM TestCaseCountry WHERE Test = ? AND TestCase = ? AND Country = ?";

                            PreparedStatement preStat2 = connection.prepareStatement(sql_delete);
                            try {
                                preStat2.setString(1, tc.getTest());
                                preStat2.setString(2, tc.getTestCase());
                                preStat2.setString(3, rsCount.getString("Country"));

                                preStat2.executeUpdate();
                            } catch (SQLException exception) {
                                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
                            } finally {
                                preStat2.close();
                            }
                        }
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    rsCount.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }

            res = true;
            for (int i = 0; i < tc.getCountryList().size() && res; i++) {
                if (!countriesDB.contains(tc.getCountryList().get(i))) {
                    final String sql_insert = "INSERT INTO Testcasecountry (test, testcase, country) VALUES (?, ?, ?)";

                    PreparedStatement preStat2 = connection.prepareStatement(sql_insert);
                    try {
                        preStat2.setString(1, tc.getTest());
                        preStat2.setString(2, tc.getTestCase());
                        preStat2.setString(3, tc.getCountryList().get(i));

                        res = preStat2.executeUpdate() > 0;
                    } catch (SQLException exception) {
                        MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
                    } finally {
                        preStat2.close();
                    }
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
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
    public boolean createTestCase(TestCase testCase) {
        throw new UnsupportedOperationException("Not supported yet.");
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
                        String testCase = resultSet.getString("TestCase");
                        String tcapplication = resultSet.getString("Application");
                        String project = resultSet.getString("Project");
                        String ticket = resultSet.getString("Ticket");
                        String description = resultSet.getString("Description");
                        String behavior = resultSet.getString("BehaviorOrValueExpected");
                        Integer priority = resultSet.getInt("Priority");
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
                        list.add(factoryTestCase.create(test, testCase, origin, refOrigin, creator, implementer,
                                lastModifier, project, ticket, tcapplication, runQA, runUAT, runPROD, priority, group,
                                status, description, behavior, howTo, tcactive, fromSprint, fromRevision, toSprint,
                                toRevision, status, bugID, targetSprint, targetRevision, comment, null, null, null, null));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, exception.toString());
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
}
