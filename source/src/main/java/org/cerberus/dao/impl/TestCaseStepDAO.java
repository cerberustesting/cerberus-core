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
import org.apache.log4j.Logger;
import org.cerberus.dao.ITestCaseStepDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseStep;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTCase;
import org.cerberus.factory.IFactoryTestCaseStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 29/12/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseStepDAO implements ITestCaseStepDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStep factoryTestCaseStep;
    @Autowired
    private IFactoryTCase factoryTestCase;

    private static final Logger LOG = Logger.getLogger(TestCaseStepDAO.class);

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
    public List<TestCaseStep> findTestCaseStepByTestCase(String test, String testcase) {
        List<TestCaseStep> list = null;
        final String query = "SELECT * FROM testcasestep WHERE test = ? AND testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        int step = resultSet.getInt("Step");
                        String description = resultSet.getString("Description");
                        String useStep = resultSet.getString("useStep");
                        String useStepTest = resultSet.getString("useStepTest");
                        String useStepTestCase = resultSet.getString("useStepTestCase");
                        Integer useStepStep = resultSet.getInt("useStepStep");
                        String inLibrary = resultSet.getString("inlibrary");
                        list.add(factoryTestCaseStep.create(test, testcase, step, description, useStep, useStepTest, useStepTestCase, useStepStep, inLibrary));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public List<String> getLoginStepFromTestCase(String countryCode, String application) {
        List<String> list = null;
        final String query = "SELECT tc.testcase FROM testcasecountry t, testcase tc WHERE t.country = ? AND t.test = 'Pre Testing' "
                + "AND tc.application = ? AND tc.tcActive = 'Y' AND t.test = tc.test AND t.testcase = tc.testcase ORDER BY testcase ASC";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, countryCode);
                preStat.setString(2, application);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<String>();
                try {
                    while (resultSet.next()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found active Pretest : " + resultSet.getString("testcase"));
                        }
                        list.add(resultSet.getString("testcase"));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public void insertTestCaseStep(TestCaseStep testCaseStep) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO `testcasestep` (`Test`,`TestCase`,`Step`,`Description`,`useStep`,`useStepTest`,`useStepTestCase`,`useStepStep`, `inLibrary`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseStep.getTest());
                preStat.setString(2, testCaseStep.getTestCase());
                preStat.setInt(3, testCaseStep.getStep());
                preStat.setString(4, testCaseStep.getDescription());
                preStat.setString(5, testCaseStep.getUseStep() == null ? "N" : testCaseStep.getUseStep());
                preStat.setString(6, testCaseStep.getUseStepTest() == null ? "" : testCaseStep.getUseStepTest());
                preStat.setString(7, testCaseStep.getUseStepTestCase() == null ? "" : testCaseStep.getUseStepTestCase());
                preStat.setInt(8, testCaseStep.getUseStepStep() == null ? 0 : testCaseStep.getUseStepStep());
                preStat.setString(9, testCaseStep.getInLibrary()== null ? "N" : testCaseStep.getInLibrary());
                
                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public TestCaseStep findTestCaseStep(String test, String testcase, Integer step) {
        TestCaseStep result = null;
        final String query = "SELECT * FROM testcasestep WHERE test = ? AND testcase = ? AND step = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, step);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String description = resultSet.getString("Description");
                        String useStep = resultSet.getString("useStep");
                        String useStepTest = resultSet.getString("useStepTest");
                        String useStepTestCase = resultSet.getString("useStepTestCase");
                        Integer useStepStep = resultSet.getInt("useStepStep");
                        String inLibrary = resultSet.getString("inlibrary");
                        result = factoryTestCaseStep.create(test, testcase, step, description, useStep, useStepTest, useStepTestCase, useStepStep, inLibrary);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return result;
    }

    @Override
    public void deleteTestCaseStep(TestCaseStep tcs) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM testcasestep WHERE test = ? and testcase = ? and step = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tcs.getTest());
                preStat.setString(2, tcs.getTestCase());
                preStat.setInt(3, tcs.getStep());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateTestCaseStep(TestCaseStep tcs) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE testcasestep SET ");
        query.append(" `Description` = ?,`useStep`=?,`useStepTest`=?,`useStepTestCase`=?,`useStepStep`=?,");
        query.append(" `inlibrary` = ? WHERE Test = ? AND TestCase = ? AND step = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, tcs.getDescription());
                preStat.setString(2, tcs.getUseStep() == null ? "N" : tcs.getUseStep());
                preStat.setString(3, tcs.getUseStepTest() == null ? "" : tcs.getUseStepTest());
                preStat.setString(4, tcs.getUseStepTestCase() == null ? "" : tcs.getUseStepTestCase());
                preStat.setInt(5, tcs.getUseStepStep() == null ? 0 : tcs.getUseStepStep());
                preStat.setString(6, tcs.getInLibrary()== null ? "N" : tcs.getInLibrary());
                preStat.setString(7, tcs.getTest());
                preStat.setString(8, tcs.getTestCase());
                preStat.setInt(9, tcs.getStep());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<TestCaseStep> getTestCaseStepUsingStepInParamter(String test, String testCase, int step) throws CerberusException {
        List<TestCaseStep> list = new ArrayList<TestCaseStep>();
        final String query = "SELECT * FROM testcasestep WHERE usestep='Y' AND usesteptest = ? AND usesteptestcase = ? AND usestepstep = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                preStat.setInt(3, step);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("Test");
                        String tc = resultSet.getString("TestCase");
                        int s = resultSet.getInt("Step");
                        String description = resultSet.getString("Description");
                        String useStep = resultSet.getString("useStep");
                        String useStepTest = resultSet.getString("useStepTest");
                        String useStepTestCase = resultSet.getString("useStepTestCase");
                        Integer useStepStep = resultSet.getInt("useStepStep");
                        String inLibrary = resultSet.getString("inlibrary");
                        list.add(factoryTestCaseStep.create(t, tc, s, description, useStep, useStepTest, useStepTestCase, useStepStep, inLibrary));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseStep> getTestCaseStepUsingTestCaseInParamter(String test, String testCase) throws CerberusException {
        List<TestCaseStep> list = null;
        final String query = "SELECT * FROM testcasestep WHERE usestep='Y' AND usesteptest = ? AND usesteptestcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("Test");
                        String tc = resultSet.getString("TestCase");
                        int s = resultSet.getInt("Step");
                        String description = resultSet.getString("Description");
                        String useStep = resultSet.getString("useStep");
                        String useStepTest = resultSet.getString("useStepTest");
                        String useStepTestCase = resultSet.getString("useStepTestCase");
                        Integer useStepStep = resultSet.getInt("useStepStep");
                        String inLibrary = resultSet.getString("inLibrary");
                        list.add(factoryTestCaseStep.create(t, tc, s, description, useStep, useStepTest, useStepTestCase, useStepStep, inLibrary));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }
    
    @Override
    public List<TestCaseStep> getStepUsedAsLibraryInOtherTestCaseByApplication(String application) throws CerberusException{
    List<TestCaseStep> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.usesteptest, tcs.usesteptestcase,tcs.usestepstep, tcs2.description FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join testcasestep tcs2 on tcs.test=tcs2.test and tcs.testcase=tcs2.testcase and tcs.step=tcs2.step ");
        query.append("where tcs.usestep = 'Y' and tc.application = ?  ");
        query.append("group by tcs.usesteptest, tcs.usesteptestcase, tcs.usestepstep ");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, application);
                
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("usesteptest");
                        String tc = resultSet.getString("usesteptestcase");
                        int s = resultSet.getInt("usestepstep");
                        String description = resultSet.getString("description");
                        list.add(factoryTestCaseStep.create(t, tc, s, description, null, null, null, 0, null));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;

    }

    @Override
    public List<TestCaseStep> getStepLibraryBySystem(String system) throws CerberusException {
        List<TestCaseStep> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.test, tcs.testcase,tcs.step, tcs.description FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join application app  on tc.application=app.application ");
        query.append("where tcs.inlibrary = 'Y' and app.system = ?  ");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, system);
                
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("test");
                        String tc = resultSet.getString("testcase");
                        int s = resultSet.getInt("step");
                        String description = resultSet.getString("description");
                        list.add(factoryTestCaseStep.create(t, tc, s, description, null, null, null, 0, null));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
}

    @Override
    public List<TestCaseStep> getStepLibraryBySystemTest(String system, String test) throws CerberusException {
        List<TestCaseStep> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.test, tcs.testcase,tcs.step, tcs.description, tc.description as tcdesc FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join application app  on tc.application=app.application ");
        query.append("where tcs.inlibrary = 'Y' and app.system = ? and tcs.test = ? ");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, system);
                preStat.setString(2, test);
                
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("test");
                        String tc = resultSet.getString("testcase");
                        int s = resultSet.getInt("step");
                        String description = resultSet.getString("description");
                        String tcdesc = resultSet.getString("tcdesc");
                        TCase tcToAdd = factoryTestCase.create(t, tc, tcdesc);
                        TestCaseStep tcsToAdd = factoryTestCaseStep.create(t, tc, s, description, null, null, null, 0, null);
                        tcsToAdd.setTestCaseObj(tcToAdd);
                        list.add(tcsToAdd);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseStep> getStepLibraryBySystemTestTestCase(String system, String test, String testCase) throws CerberusException {
        List<TestCaseStep> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.test, tcs.testcase,tcs.step, tcs.description FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join application app  on tc.application=app.application ");
        query.append("where tcs.inlibrary = 'Y' and app.system = ? and tcs.test = ? and tcs.testcase = ?");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, system);
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("test");
                        String tc = resultSet.getString("testcase");
                        int s = resultSet.getInt("step");
                        String description = resultSet.getString("description");
                        list.add(factoryTestCaseStep.create(t, tc, s, description, null, null, null, 0, null));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }
}
