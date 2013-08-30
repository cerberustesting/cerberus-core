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
     * application, description of test case).
     */
    @Override
    public List<TCase> findTestCaseByTest(String test) {
        List<TCase> list = null;
        final String query = "SELECT TestCase, Application, Description FROM testcase WHERE test = ?";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, test);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
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
            this.databaseSpring.disconnect();
        }

        return list;
    }

    /**
     * Get test case information.
     * <p/>
     * Use {@link #loadTestCase(java.sql.ResultSet)} to convert data from
     * database to object TestCase.
     *
     * @param test Name of test group.
     * @param testCase Name of test case.
     * @return TestCase object or null.
     * @see com.redcats.tst.entity.TestCase
     */
    @Override
    public TCase findTestCaseByKey(String test, String testCase) throws CerberusException {
        boolean throwExcep = false;
        TCase result = null;
        final String query = "SELECT * FROM testcase WHERE test = ? AND testcase = ?";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, testCase);
            try {
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
                    }else{
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
            this.databaseSpring.disconnect();
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public boolean updateTestCaseInformation(TestCase testCase) {
        final String sql = "UPDATE Testcase tc SET tc.Application = ?, tc.Project = ?, tc.BehaviorOrValueExpected = ?, tc.activeQA = ?, tc.activeUAT = ?, tc.activePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.TcActive = ?, tc.Description = ?, tc.Group = ?, tc.HowTo = ?, tc.Comment = ?, tc.Ticket = ?, tc.FromBuild = ?, "
                + "tc.FromRev = ?, tc.ToBuild = ?, tc.ToRev = ?, tc.BugID = ?, tc.TargetBuild = ?, tc.Implementer = ?, tc.LastModifier = ?, tc.TargetRev = ? "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        ArrayList<String> al = new ArrayList<String>();
        al.add(testCase.getApplication());
        al.add(testCase.getProject());
        al.add(testCase.getDescription());
        al.add(testCase.isRunQA() ? "Y" : "N");
        al.add(testCase.isRunUAT() ? "Y" : "N");
        al.add(testCase.isRunPROD() ? "Y" : "N");
        al.add(Integer.toString(testCase.getPriority()));
        al.add(testCase.getStatus());
        al.add(testCase.isActive() ? "Y" : "N");
        al.add(testCase.getShortDescription());
        al.add(testCase.getGroup());
        al.add(testCase.getHowTo());
        al.add(testCase.getComment());
        al.add(testCase.getTicket());
        al.add(testCase.getFromSprint());
        al.add(testCase.getFromRevision());
        al.add(testCase.getToSprint());
        al.add(testCase.getToRevision());
        al.add(testCase.getBugID());
        al.add(testCase.getTargetRevision());
        al.add(testCase.getImplementer());
        al.add(testCase.getLastModifier());
        al.add(testCase.getTargetRevision());
        al.add(testCase.getTest());
        al.add(testCase.getTestCase());

        databaseSpring.connect();
        boolean res = databaseSpring.update(sql, al) > 0;
        databaseSpring.disconnect();

        return res;
    }

    @Override
    public boolean updateTestCaseInformationCountries(TestCase tc) {
        databaseSpring.connect();
        ArrayList<String> countriesDB = new ArrayList<String>();

        final String sql_count = "SELECT Country FROM TestCaseCountry WHERE Test = ? AND TestCase = ?";
        ArrayList<String> al_count = new ArrayList<String>();
        al_count.add(tc.getTest());
        al_count.add(tc.getTestCase());

        //split into delete and insert method
        ResultSet rsCount = databaseSpring.query(sql_count, al_count);
        try {
            while (rsCount.next()) {
                countriesDB.add(rsCount.getString("Country"));
                if (!tc.getCountryList().contains(rsCount.getString("Country"))) {
                    final String sql_delete = "DELETE FROM TestCaseCountry WHERE Test = ? AND TestCase = ? AND Country = ?";
                    ArrayList<String> al_delete = new ArrayList<String>();
                    al_delete.add(tc.getTest());
                    al_delete.add(tc.getTestCase());
                    al_delete.add(rsCount.getString("Country"));
                    databaseSpring.update(sql_delete, al_delete);
                }
            }
        } catch (SQLException ex) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.FATAL, ex.toString());
        }

        boolean res = true;
        for (int i = 0; i < tc.getCountryList().size() && res; i++) {
            if (!countriesDB.contains(tc.getCountryList().get(i))) {
                final String sql_insert = "INSERT INTO Testcasecountry (test, testcase, country) Values (?, ?, ?)";
                ArrayList<String> al_insert = new ArrayList<String>();
                al_insert.add(tc.getTest());
                al_insert.add(tc.getTestCase());
                al_insert.add(tc.getCountryList().get(i));
                res = databaseSpring.update(sql_insert, al_insert) > 0;
            }
        }

        databaseSpring.disconnect();

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
                + "WHERE tc.test=tcc.test and tc.testcase=tcc.testcase "
                + "and tc.test = ? and tc.application = ? and tcc.country = ? and tc.tcactive = ? ";
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, application);
            preStat.setString(3, country);
            preStat.setString(4, active);
            try {
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
            this.databaseSpring.disconnect();
        }

        return list;
    }
}
