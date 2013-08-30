package com.redcats.tst.servlet.testCase;

import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.refactor.DbMysqlController;
import com.redcats.tst.refactor.TestCaseCountryProperties;
import com.redcats.tst.refactor.TestCaseStepAction;
import com.redcats.tst.refactor.TestCaseStepActionControl;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.impl.LogEventService;
import com.redcats.tst.service.impl.UserService;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet implementation class DuplicateTest
 */
public class DuplicateTestCase extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final DbMysqlController db;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DuplicateTestCase() {

        super();
        this.db = new DbMysqlController();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String test = request.getParameter("Test");
        String testCase = request.getParameter("TestCase");
        String newTest = request.getParameter("editTest");
        String newTestCase = request.getParameter("editTestCase");
        if (this.db.existsResults("test t, testcase tc where t.test = tc.test  and t.test like '"
                + test
                + "' and tc.testcase like '"
                + testCase + "'")) {
            if (this.duplicateTestRunner(request.getUserPrincipal().getName(), test, testCase, newTest, newTestCase)) {

                /**
                 * Adding Log entry.
                 */
                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                try {
                    logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/DuplicateTestCase", "CREATE", "Duplicate testcase From : ['" + test + "'|'" + testCase + "'] To : ['" + newTest + "'|'" + newTestCase + "']", "", ""));
                } catch (CerberusException ex) {
                    Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
                }

                this.db.disconnect();
                response.sendRedirect("TestCase.jsp?Load=Load&Test="
                        + newTest + "&TestCase="
                        + newTestCase);
            }
        } else {
            request.getSession().setAttribute("flashMessage",
                    "Error Duplicating Test");
            response.sendRedirect("TestCase.jsp?Load=Load&Test="
                    + test + "&TestCase=" + testCase);
        }
    }

    private Boolean duplicateActions(String test, String testCase, String newTest, String newTestCase) {

        ResultSet rs = this.db.query("select * from testcasestepaction "
                + "where test like '" + test + "' and testcase like '"
                + testCase + "'");
        try {
            TestCaseStepAction insert = new TestCaseStepAction();
            if (rs != null) {
                while (rs.next()) {
                    insert.importResultSet(rs);

                    insert.setTest(newTest);
                    insert.setTestcase(newTestCase);

                    insert.insert();

                }
            }
            return true;
        } catch (SQLException e) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated actions" + e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.INFO, "Exception closing ResultSet" + ex.toString());
            }
        }
        return false;
    }

    /*
     * `testcasestepactioncontrol`.`Test`,
     * `testcasestepactioncontrol`.`TestCase`,
     * `testcasestepactioncontrol`.`Step`,
     * `testcasestepactioncontrol`.`Sequence`,
     * `testcasestepactioncontrol`.`Control`,
     * `testcasestepactioncontrol`.`Type`,
     * `testcasestepactioncontrol`.`ControlValue`,
     * `testcasestepactioncontrol`.`ControlProperty`
     */
    private Boolean duplicateControls(String test, String testCase, String newTest, String newTestCase) {

        ResultSet rs = this.db.query("select * from testcasestepactioncontrol "
                + "where test like '" + test
                + "' and testcase like '" + testCase + "'");
        try {
            TestCaseStepActionControl insert = new TestCaseStepActionControl();

            if (rs != null) {
                while (rs.next()) {
                    insert.importResultSet(rs);

                    insert.setTest(newTest);
                    insert.setTestcase(newTestCase);

                    insert.insert();
                }
            }
            return true;
        } catch (SQLException e) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated controls: " + e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.INFO, "Exception closing ResultSet: " + ex.toString());
            }
        }
        return false;
    }

    /*
     * `testcasecountry`.`Test`, `testcasecountry`.`TestCase`,
     * `testcasecountry`.`Country`
     */
    private Boolean duplicateCountries(String test, String testCase, String newTest, String newTestCase) {

        ResultSet rs = this.db.query("select * from testcasecountry "
                + "where test like '" + test + "' and testcase like '"
                + testCase + "'");
        try {
            if (rs != null) {
                while (rs.next()) {
                    this.db.execute("INSERT INTO TestCaseCountry (`Test`,`Testcase`,`Country`) "
                            + " VALUES ( '"
                            + newTest
                            + "', '"
                            + newTestCase
                            + "','"
                            + rs.getString("Country")
                            + "' )");
                }
            }
            return true;
        } catch (SQLException e) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated countries: " + e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.INFO, "Exception closing ResultSet: " + ex.toString());
            }
        }
        return false;
    }

    private Boolean duplicateProperties(String test, String testCase, String newTest, String newTestCase) {

        ResultSet rs = this.db.query("select * from testcasecountryproperties "
                + "where test like '" + test
                + "' and testcase like '" + testCase + "'");
        try {
            TestCaseCountryProperties insert = new TestCaseCountryProperties();

            if (rs != null) {
                while (rs.next()) {
                    insert.importResultSet(rs);

                    insert.setTest(newTest);
                    insert.setTestcase(newTestCase);

                    insert.insert();
                }

            }
            return true;
        } catch (SQLException e) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated properties: " + e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.INFO, "Exception closing ResultSet: " + ex.toString());
            }
        }
        return false;
    }

    /*
     * `testcasestep`.`Test`, `testcasestep`.`TestCase`, `testcasestep`.`Step`,
     * `testcasestep`.`Description`
     */
    private Boolean duplicateStep(String test, String testCase, String newTest, String newTestCase) {

        ResultSet rs = this.db.query("select * from testcasestep "
                + "where test like '" + test + "' and testcase like '"
                + testCase + "'");
        try {
            if (rs != null) {
                while (rs.next()) {
                    this.db.execute("INSERT INTO testcasestep (`Test`,`Testcase`,`Step`,`Description`)"
                            + " VALUES ( '"
                            + newTest
                            + "','"
                            + newTestCase
                            + "','"
                            + rs.getString("Step")
                            + "','" + rs.getString("Description") + "' )");
                }
            }
            return true;
        } catch (SQLException e) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated step: " + e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.INFO, "Exception closing ResultSet: " + ex.toString());
            }
        }
        return false;
    }

    /*
     * `test`.`Test`, `test`.`Description`, `test`.`Active`, `test`.`Automated`,
     * `test`.`Origine`
     */
    private Boolean duplicateTest(String test, String newTest) {

        ResultSet rs = this.db.query("select * from test "
                + "where test like '" + test.toString() + "'");
        try {
            if (rs != null) {
                while (rs.next()) {
                    this.db.execute("INSERT INTO Test (`Test`,`Description`,`Active`,`Automated`)"
                            + " VALUES ( '"
                            + newTest.toString()
                            + "','"
                            + rs.getString("Description")
                            + "','"
                            + rs.getString("Active")
                            + "','"
                            + rs.getString("Automated") + "' )");
                }
            }
            return true;
        } catch (SQLException e) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated Test: " + e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.INFO, "Exception closing ResultSet: " + ex.toString());
            }
        }
        return false;
    }

    private Boolean duplicateTestCase(String creator, String test, String testCase, String newTest, String newTestCase) {

        ResultSet rs = this.db.query(" select * from testcase "
                + " where test like '" + test + "'  and testcase like '"
                + testCase + "'");
        try {
            if (rs != null) {
                while (rs.next()) {
                    String description = rs.getString("Description");
                    if (description != null) {
                        description = description.replace("'", "\\'");
                    }
                    String behavior = rs.getString("BehaviorOrValueExpected");
                    if (behavior != null) {
                        behavior = behavior.replace("'", "\\'");
                    }
                    String howto = rs.getString("HowTo");
                    if (howto != null) {
                        howto = howto.replace("'", "\\'");
                    }
                    this.db.execute("INSERT INTO TestCase (`Test`,`Testcase`,`Application`,`Project`,`Description`,`BehaviorOrValueExpected`,`activeQA`,`activeUAT`,`activePROD`,`Priority`,`Status`,`TcActive`,`Group`,`Origine`,`RefOrigine`,`HowTo`, `Creator`)"
                            + " VALUES ( '"
                            + newTest
                            + "','"
                            + newTestCase
                            + "','"
                            + rs.getString("Application")
                            + "','"
                            + rs.getString("Project")
                            + "','"
                            + description
                            + "','"
                            + behavior
                            + "','"
                            + rs.getString("activeQA")
                            + "','"
                            + rs.getString("activeUAT")
                            + "','"
                            + rs.getString("activePROD")
                            + "','"
                            + rs.getString("Priority")
                            + "','"
                            + "STANDBY"
                            // + rs.getString ( "Status" )
                            + "','"
                            + rs.getString("TcActive")
                            + "','"
                            + rs.getString("Group")
                            + "','"
                            + rs.getString("Origine")
                            + "','"
                            + rs.getString("RefOrigine")
                            + "','"
                            + howto
                            + "','"
                            + creator
                            + "' )");
                }
            }
            return true;
        } catch (SQLException e) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated TestCase: " + e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.INFO, "Exception closing ResultSet: " + ex.toString());
            }
        }
        return false;
    }

    public Boolean duplicateTestRunner(String creator, String test, String testCase, String newTest, String newTestCase) {

        // System.out.println ( "New Test : " + this.newTest +
        // " New Testcase : " + this.newTestCase ) ;
        // System.out.println ( "Test : " + this.test + " Testcase : " +
        // this.testCase ) ;
        Boolean run = true;
        run = this.duplicateTest(test, newTest);
        if (run) {
            run = this.duplicateTestCase(creator, test, testCase, newTest, newTestCase);
        }
        if (run) {
            run = this.duplicateCountries(test, testCase, newTest, newTestCase);
        }
        if (run) {
            run = this.duplicateStep(test, testCase, newTest, newTestCase);
        }
        if (run) {
            run = this.duplicateActions(test, testCase, newTest, newTestCase);
        }
        if (run) {
            run = this.duplicateProperties(test, testCase, newTest, newTestCase);
        }
        if (run) {
            run = this.duplicateControls(test, testCase, newTest, newTestCase);
        }

        return run;
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
