/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.servlet.testCase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.refactor.TestCaseCountryProperties;
import org.cerberus.refactor.TestCaseStepAction;
import org.cerberus.refactor.TestCaseStepActionControl;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet implementation class DuplicateTest
 */
@WebServlet(name = "DuplicateTestCase", urlPatterns = {"/DuplicateTestCase"})
public class DuplicateTestCase extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ApplicationContext appContext;

    @Autowired
    private DatabaseSpring database;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
        this.database = appContext.getBean(DatabaseSpring.class);

        String test = request.getParameter("Test");
        String testCase = request.getParameter("TestCase");
        String newTest = request.getParameter("editTest");
        String newTestCase = request.getParameter("editTestCase");

        try {
            if (testCaseService.findTestCaseByKey(test, testCase) != null) {
                if (this.duplicateTestRunner(request.getUserPrincipal().getName(), test, testCase, newTest, newTestCase)) {

                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                    try {
                        logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/DuplicateTestCase", "CREATE", "Duplicate testcase From : ['" + test + "'|'" + testCase + "'] To : ['" + newTest + "'|'" + newTestCase + "']", "", ""));
                    } catch (CerberusException ex) {
                        Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
                    }

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
        } catch (CerberusException exception) {
            request.getSession().setAttribute("flashMessage", exception.getMessageError());
            response.sendRedirect("TestCase.jsp?Load=Load&Test=" + test + "&TestCase=" + testCase);
        }
    }

    private Boolean duplicateActions(String test, String testCase, String newTest, String newTestCase) {

        Connection connection = this.database.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement("SELECT * FROM testcasestepaction WHERE test LIKE ? AND testcase LIKE ?");
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet rs = preStat.executeQuery();
                try {
                    TestCaseStepAction insert = appContext.getBean(TestCaseStepAction.class);
                    while (rs.next()) {
                        insert.importResultSet(rs);

                        insert.setTest(newTest);
                        insert.setTestcase(newTestCase);

                        insert.insert();

                    }
                    return true;
                } catch (SQLException e) {
                    MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated actions" + e.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.WARN, e.toString());
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

        Connection connection = this.database.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement("SELECT * FROM testcasestepactioncontrol WHERE test LIKE ? AND testcase LIKE ?");
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet rs = preStat.executeQuery();
                try {
                    TestCaseStepActionControl insert = appContext.getBean(TestCaseStepActionControl.class);

                    while (rs.next()) {
                        insert.importResultSet(rs);

                        insert.setTest(newTest);
                        insert.setTestcase(newTestCase);

                        insert.insert();
                    }
                    return true;
                } catch (SQLException e) {
                    MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated controls" + e.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    /*
     * `testcasecountry`.`Test`, `testcasecountry`.`TestCase`,
     * `testcasecountry`.`Country`
     */
    private Boolean duplicateCountries(String test, String testCase, String newTest, String newTestCase) {

        Connection connection = this.database.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement("SELECT * FROM testcasecountry WHERE test LIKE ? AND testcase LIKE ?");
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet rs = preStat.executeQuery();
                try {
                    while (rs.next()) {
                        PreparedStatement preStat2 = connection.prepareStatement("INSERT INTO testcasecountry (`Test`,`Testcase`,`Country`) VALUES (?, ?, ?)");
                        try {
                            preStat2.setString(1, newTest);
                            preStat2.setString(2, newTestCase);
                            preStat2.setString(3, rs.getString("Country"));

                            preStat2.executeUpdate();
                        } catch (SQLException e) {
                            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, e.toString());
                        } finally {
                            preStat2.close();
                        }
                    }
                    return true;
                } catch (SQLException e) {
                    MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated countries" + e.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    private Boolean duplicateProperties(String test, String testCase, String newTest, String newTestCase) {

        Connection connection = this.database.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement("SELECT * FROM testcasecountryproperties WHERE test LIKE ? AND testcase LIKE ?");
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet rs = preStat.executeQuery();
                try {
                    TestCaseCountryProperties insert = appContext.getBean(TestCaseCountryProperties.class);
                    while (rs.next()) {
                        insert.importResultSet(rs);

                        insert.setTest(newTest);
                        insert.setTestcase(newTestCase);

                        insert.insert();
                    }
                    return true;
                } catch (SQLException e) {
                    MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated properties" + e.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.WARN, e.toString());
            }
        }

        return false;
    }

    /*
     * `testcasestep`.`Test`, `testcasestep`.`TestCase`, `testcasestep`.`Step`,
     * `testcasestep`.`Description`
     */
    private Boolean duplicateStep(String test, String testCase, String newTest, String newTestCase) {

        Connection connection = this.database.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement("SELECT * FROM testcasestep WHERE test LIKE ? AND testcase LIKE ?");
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet rs = preStat.executeQuery();
                try {
                    while (rs.next()) {
                        PreparedStatement preStat2 = connection.prepareStatement("INSERT INTO testcasestep (`Test`,`Testcase`,`Step`,`Description`) VALUES (?, ?, ?, ?)");
                        try {
                            preStat2.setString(1, newTest);
                            preStat2.setString(2, newTestCase);
                            preStat2.setString(3, rs.getString("Step"));
                            preStat2.setString(4, rs.getString("Description"));

                            preStat2.executeUpdate();
                        } catch (SQLException e) {
                            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, e.toString());
                        } finally {
                            preStat2.close();
                        }
                    }
                    return true;
                } catch (SQLException e) {
                    MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated step" + e.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.WARN, e.toString());
            }
        }

        return false;
    }

    /*
     * `test`.`Test`, `test`.`Description`, `test`.`Active`, `test`.`Automated`,
     * `test`.`Origine`
     */
    private Boolean duplicateTest(String test, String newTest) {

        Connection connection = this.database.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement("SELECT * FROM test WHERE test LIKE ?");
            try {
                preStat.setString(1, test);

                ResultSet rs = preStat.executeQuery();
                try {
                    while (rs.next()) {
                        PreparedStatement preStat2 = connection.prepareStatement("INSERT INTO test (`Test`,`Description`,`Active`,`Automated`) VALUES (?, ?, ?, ?)");
                        try {
                            preStat2.setString(1, newTest);
                            preStat2.setString(2, rs.getString("Description"));
                            preStat2.setString(3, rs.getString("Active"));
                            preStat2.setString(4, rs.getString("Automated"));

                            preStat2.executeUpdate();
                        } catch (SQLException e) {
                            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, e.toString());
                        } finally {
                            preStat2.close();
                        }
                    }
                    return true;
                } catch (SQLException e) {
                    MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated test" + e.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.WARN, e.toString());
            }
        }

        return false;
    }

    private Boolean duplicateTestCase(String creator, String test, String testCase, String newTest, String newTestCase) {

        Connection connection = this.database.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement("SELECT * FROM testcase WHERE test LIKE ? AND testcase LIKE ?");
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet rs = preStat.executeQuery();
                try {
                    while (rs.next()) {
                        PreparedStatement preStat2 = connection.prepareStatement("INSERT INTO testcase (`Test`,`Testcase`,`Application`,`Project`,`Description`,`BehaviorOrValueExpected`," +
                                "`activeQA`,`activeUAT`,`activePROD`,`Priority`,`Status`,`TcActive`,`Group`,`Origine`,`RefOrigine`,`HowTo`, `Creator`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        try {
                            preStat2.setString(1, newTest);
                            preStat2.setString(2, newTestCase);
                            preStat2.setString(3, rs.getString("Application"));
                            preStat2.setString(4, rs.getString("Project"));
                            preStat2.setString(5, rs.getString("Description"));
                            preStat2.setString(6, rs.getString("BehaviorOrValueExpected"));
                            preStat2.setString(7, rs.getString("activeQA"));
                            preStat2.setString(8, rs.getString("activeUAT"));
                            preStat2.setString(9, rs.getString("activePROD"));
                            preStat2.setString(10, rs.getString("Priority"));
                            preStat2.setString(11, "STANDBY");
                            preStat2.setString(12, rs.getString("TcActive"));
                            preStat2.setString(13, rs.getString("Group"));
                            preStat2.setString(14, rs.getString("Origine"));
                            preStat2.setString(15, rs.getString("RefOrigine"));
                            preStat2.setString(16, rs.getString("HowTo"));
                            preStat2.setString(17, creator);

                            preStat2.executeUpdate();
                        } catch (SQLException e) {
                            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, e.toString());
                        } finally {
                            preStat2.close();
                        }
                    }
                    return true;
                } catch (SQLException e) {
                    MyLogger.log(DuplicateTestCase.class.getName(), Level.FATAL, "Error inserting duplicated step" + e.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(DuplicateTestCase.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DuplicateTestCase.class.getName(), Level.WARN, e.toString());
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
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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
