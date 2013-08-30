package com.redcats.tst.refactor;

import com.redcats.tst.log.MyLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.Level;

public class TestCountryTable {

    private LinkedList<Country> countries;
    private LinkedList<TestCountryResult> results;
    private LinkedList<Test> tests;

    public TestCountryTable() {

        this.tests = new LinkedList<Test>();
        this.countries = new LinkedList<Country>();
        this.results = new LinkedList<TestCountryResult>();
    }

    public void addCountry(Country country) {

        if (this.countries != null) {
            this.countries.add(country);
        }
    }

    public void addResult(TestCountryResult result) {

        try {
            this.results.add(result);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            this.results = new LinkedList<TestCountryResult>();
            this.addResult(result);
        }
    }

    public void addTest(Test test) {

        if (this.tests != null) {
            this.tests.add(test);
        }
    }

    public void fetchCountries(Connection conn, Options options) {
    }

    public void fetchResults(Connection conn, Options options) {

        String opts = options.generateSQL(true, true, false);
        String from = " from  testcasecountry tcc, testcaseexecution te, testcase tc, test t ";
        String whereEquals = " WHERE t.test = tc.test and tc.test = te.test and tc.testcase = te.testcase and te.test = tcc.test and te.testcase = tcc.testcase and te.country = tcc.country   ";
        String selectTCC = "Select tcc.test, tcc.testcase, tcc.country  from  test t, testcase tc, testcasecountry tcc  WHERE  tcc.test = t.test and tcc.test = tc.test and tcc.testcase = tc.testcase ";
        StringBuilder whereTCC = new StringBuilder();

        Iterator<Country> itCountryTCC = this.countries.iterator();
        Boolean first = true;
        while (itCountryTCC.hasNext()) {
            Country itCountryTCCNext = itCountryTCC.next();
            if (first) {
                whereTCC.append("AND ( ");
            } else {
                whereTCC.append(" OR ");
            }
            first = false;
            whereTCC.append(" tcc.country = '" + itCountryTCCNext.getName()
                    + "' ");
        }
        if (!first) {
            whereTCC.append(" )");
        }

        PreparedStatement statementTCC = null;
        ResultSet rsTCC = null;
        try {
            statementTCC = conn.prepareStatement("? ? ?");
            statementTCC.setString(1, selectTCC);
            statementTCC.setString(2, whereTCC.toString());
            statementTCC.setString(3, opts);
            // System.out.println ( selectTCC + whereTCC + opts ) ;
            rsTCC = statementTCC.executeQuery();
            while (rsTCC.next()) {
                TestCountryResult tcr = new TestCountryResult();
                tcr.setCountry(this.getSpecificCountry(rsTCC.getString("Country")));
                tcr.setTest(this.getSpecificTest(rsTCC.getString("Test"),
                        rsTCC.getString("Testcase")));
                tcr.updateCountryStatistics();
                this.results.add(tcr);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statementTCC != null) {
                    statementTCC.close();
                }
                if (rsTCC != null) {
                    rsTCC.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCountryTable.class.getName(), Level.INFO, "Exception closing Statement or ResultSet: " + ex.toString());
            }
        }
        opts = options.generateSQL(true, true, true);
        first = true;
        String preSelect = "select te1.id, te1.test, te1.testcase, te1.controlstatus, te1.end, te1.country from testcaseexecution te1 join (";
        String select = "SELECT max(te.id) AS ID1  ";
        StringBuilder where = new StringBuilder();
        where.append(whereEquals);
        Iterator<Test> itTest = this.tests.iterator();
        while (itTest.hasNext()) {
            Test itTestNext = itTest.next();
            Iterator<Country> itCountry = this.countries.iterator();
            while (itCountry.hasNext()) {
                Country itCountryNext = itCountry.next();

                if (first) {
                    where.append("AND ( ");
                } else {
                    where.append(" OR ");
                }
                first = false;
                where.append(" (te.country = '" + itCountryNext.getName()
                        + "' AND te.test = '" + itTestNext.getTest()
                        + "' AND te.testcase = '" + itTestNext.getTestcase()
                        + "'" + opts + ") ");

            }
        }
        if (!first) {
            where.append(" ) ");
        }
        String groupby = " group by te.test, te.testcase, te.country ) as te2 ON te1.id = ID1 ";
        // System.out.println ( preSelect + select + from + where + groupby ) ;

        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement("? ? ? ? ?");
            statement.setString(1, preSelect);
            statement.setString(2, select);
            statement.setString(3, from);
            statement.setString(4, where.toString());
            statement.setString(5, groupby);
            // System.out.println ( select + from + where + groupby ) ;
            rs = statement.executeQuery();
            while (rs.next()) {
                TestCountryResult tcr = this.findResults(
                        this.getSpecificCountry(rs.getString("Country")),
                        this.getSpecificTest(rs.getString("Test"),
                        rs.getString("Testcase")));
                if (tcr != null) {
                    tcr.setDate(rs.getString("End"));
                    tcr.setoK(rs.getString("ControlStatus"));
                    tcr.setId(rs.getInt("ID"));
                    tcr.updateExecutionStatistics();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCountryTable.class.getName(), Level.INFO, "Exception closing Statement or ResultSet: " + ex.toString());
            }
        }
    }

    public void fetchTests(Connection conn, Options options) {

        String select = "SELECT DISTINCT " + options.getTestcaseTable()
                + ".test, " + options.getTestcaseTable() + ".testcase, "
                + options.getTestcaseTable() + ".application, "
                + options.getTestcaseTable() + ".description, "
                + options.getTestcaseTable() + ".priority, "
                + options.getTestcaseTable() + ".status , "
                + options.getTestcaseTable() + ".comment , "
                + options.getTestcaseTable() + ".behaviororvalueexpected , "
                + options.getTestcaseTable() + ".group , "
                + options.getTestcaseTable() + ".BugID , "
                + options.getTestcaseTable() + ".TargetBuild , "
                + options.getTestcaseTable() + ".TargetRev ";
        String from = " FROM test " + options.getTestTable() + ", testcase "
                + options.getTestcaseTable();
        String where = " WHERE " + options.getTestcaseTable() + ".test = "
                + options.getTestTable() + ".test and "
                + options.getTestcaseTable() + ".group is not NULL and "
                + options.getTestcaseTable() + ".group not in ('PRIVATE') ";
        String opts = options.generateSQL(true, true, false);
        // System.out.println ( select.toString ( ) + from.toString ( ) +
        // where.toString ( ) + opts.toString ( ) ) ;

        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement("? ? ? ?");
            statement.setString(1, select);
            statement.setString(2, from);
            statement.setString(3, where);
            statement.setString(4, opts);
            rs = statement.executeQuery();
            while (rs.next()) {
                Test t = new Test();
                t.setApplication(rs.getString("Application"));
                t.setDescription(rs.getString("Description"));
                t.setPriority(rs.getString("Priority"));
                t.setStatus(rs.getString("Status"));
                t.setComment(rs.getString("Comment"));
                t.setGroup(rs.getString("Group"));
                t.setBehaviorOrValueExpected(rs.getString("BehaviorOrValueExpected"));
                t.setBugID(rs.getString("BugID"));
                t.setTargetBuild(rs.getString("TargetBuild"));
                t.setTargetRev(rs.getString("TargetRev"));
                t.setTest(rs.getString("Test"));
                t.setTestcase(rs.getString("Testcase"));
                this.tests.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCountryTable.class.getName(), Level.INFO, "Exception closing Statement or ResultSet: " + ex.toString());
            }
        }

    }

    public TestCountryResult findResults(Country country, Test test) {

        try {
            Iterator<TestCountryResult> it = this.results.iterator();
            while (it.hasNext()) {
                TestCountryResult element = it.next();
                if ((element.getCountry() == country)
                        && (element.getTest() == test)) {
                    return element;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LinkedList<Country> getCountries() {

        return this.countries;
    }

    public LinkedList<TestCountryResult> getResult() {

        return this.results;
    }

    private Country getSpecificCountry(String string) {

        try {
            Iterator<Country> it = this.countries.iterator();
            while (it.hasNext()) {
                Country country = it.next();
                if (country.getName().compareTo(string) == 0) {
                    return country;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Test getSpecificTest(String string, String testcase) {

        try {
            Iterator<Test> it = this.tests.iterator();
            while (it.hasNext()) {
                Test test = it.next();
                if ((test.getTest().compareTo(string) == 0)
                        && (test.getTestcase().compareTo(testcase) == 0)) {
                    return test;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LinkedList<Test> getTests() {

        return this.tests;
    }

    public void setCountries(LinkedList<Country> countries) {

        this.countries = countries;
    }

    public void setResult(LinkedList<TestCountryResult> result) {

        this.results = result;
    }

    public void setTests(LinkedList<Test> tests) {

        this.tests = tests;
    }
}
