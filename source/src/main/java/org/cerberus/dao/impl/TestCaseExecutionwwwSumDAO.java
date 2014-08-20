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
import org.cerberus.dao.ITestCaseExecutionwwwSumDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.StatisticSummary;
import org.cerberus.entity.TestCaseExecutionwwwSum;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 2.0.0
 */
@Repository
public class TestCaseExecutionwwwSumDAO implements ITestCaseExecutionwwwSumDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here.
     * <p>
     * And even more explanations to follow in consecutive paragraphs separated
     * by HTML paragraph breaks.
     *
     * @param variable Description text text text.
     */
    @Override
    public void register(long runID, StatisticSummary summary) {

        final String query = "INSERT INTO testcaseexecutionwwwsum(ID, tot_nbhits, tot_tps, tot_size, nb_rc2xx, nb_rc3xx, nb_rc4xx, "
                + "nb_rc5xx, img_nb, img_tps, img_size_tot, img_size_max, js_nb, js_tps, js_size_tot, js_size_max, css_nb, css_tps, "
                + "css_size_tot, css_size_max, img_size_max_url, js_size_max_url, css_size_max_url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, runID);
                preStat.setInt(2, summary.getTotNbHits());
                preStat.setInt(3, summary.getTotTps());
                preStat.setInt(4, summary.getTotSize());
                preStat.setInt(5, summary.getNbRc2xx());
                preStat.setInt(6, summary.getNbRc3xx());
                preStat.setInt(7, summary.getNbRc4xx());
                preStat.setInt(8, summary.getNbRc5xx());
                preStat.setInt(9, summary.getImgNb());
                preStat.setInt(10, summary.getImgTps());
                preStat.setInt(11, summary.getImgSizeTot());
                preStat.setInt(12, summary.getImgSizeMax());
                preStat.setInt(13, summary.getJsNb());
                preStat.setInt(14, summary.getJsTps());
                preStat.setInt(15, summary.getJsSizeTot());
                preStat.setInt(16, summary.getJsSizeMax());
                preStat.setInt(17, summary.getCssNb());
                preStat.setInt(18, summary.getCssTps());
                preStat.setInt(19, summary.getCssSizeTot());
                preStat.setInt(20, summary.getCssSizeMax());
                preStat.setString(21, summary.getImgSizeMaxUrl());
                preStat.setString(22, summary.getJsSizeMaxUrl());
                preStat.setString(23, summary.getCssSizeMaxUrl());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionwwwSumDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionwwwSumDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionwwwSumDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public List<TestCaseExecutionwwwSum> getAllDetailsFromTCEwwwSum(int id) {
        List<TestCaseExecutionwwwSum> executionwwwSums = new ArrayList<TestCaseExecutionwwwSum>();
        String query = " select tot_nbhits,tot_tps,tot_size,nb_rc2xx,nb_rc3xx,nb_rc4xx"
                + " ,nb_rc5xx,img_nb,img_tps,img_size_tot,img_size_max,js_nb,js_tps,"
                + " js_size_tot,js_size_max,css_nb,css_tps,css_size_tot,css_size_max"
                + " from testcaseexecutionwwwsum where id = ?";
        TestCaseExecutionwwwSum tcewwwsumToAdd;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));
                ResultSet rs = preStat.executeQuery();
                try {
                    if (rs.first()) {
//TODO factory
                        tcewwwsumToAdd = new TestCaseExecutionwwwSum();
                        tcewwwsumToAdd.setTot_nbhits(rs.getInt(1));
                        tcewwwsumToAdd.setTot_tps(rs.getInt(2));
                        tcewwwsumToAdd.setTot_size(rs.getInt(3));
                        tcewwwsumToAdd.setNb_rc2xx(rs.getInt(4));
                        tcewwwsumToAdd.setNb_rc3xx(rs.getInt(5));
                        tcewwwsumToAdd.setNb_rc4xx(rs.getInt(6));
                        tcewwwsumToAdd.setNb_rc5xx(rs.getInt(7));
                        tcewwwsumToAdd.setImg_nb(rs.getInt(8));
                        tcewwwsumToAdd.setImg_tps(rs.getInt(9));
                        tcewwwsumToAdd.setImg_size_tot(rs.getInt(10));
                        tcewwwsumToAdd.setImg_size_max(rs.getInt(11));
                        tcewwwsumToAdd.setJs_nb(rs.getInt(12));
                        tcewwwsumToAdd.setJs_tps(rs.getInt(13));
                        tcewwwsumToAdd.setJs_size_tot(rs.getInt(14));
                        tcewwwsumToAdd.setJs_size_max(rs.getInt(15));
                        tcewwwsumToAdd.setCss_nb(rs.getInt(16));
                        tcewwwsumToAdd.setCss_tps(rs.getInt(17));
                        tcewwwsumToAdd.setCss_size_tot(rs.getInt(18));
                        tcewwwsumToAdd.setCss_size_max(rs.getInt(19));
                        executionwwwSums.add(tcewwwsumToAdd);
                    }
                } catch (SQLException ex) {
                    MyLogger.log(TestCaseExecutionwwwSumDAO.class.getName(), Level.FATAL, ex.toString());
                } finally {
                    rs.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionwwwSumDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionwwwSumDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionwwwSumDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return executionwwwSums;
    }
}
