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
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseExecutionWWWSumDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.StatisticSummary;
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
public class TestCaseExecutionWWWSumDAO implements ITestCaseExecutionWWWSumDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @param variable Description text text text.
     */
    @Override
    public void register(long runID, StatisticSummary summary) {

        final String query = "INSERT INTO testcaseexecutionwwwsum(ID, tot_nbhits, tot_tps, tot_size, nb_rc2xx, nb_rc3xx, nb_rc4xx, " +
                "nb_rc5xx, img_nb, img_tps, img_size_tot, img_size_max, js_nb, js_tps, js_size_tot, js_size_max, css_nb, css_tps, " +
                "css_size_tot, css_size_max, img_size_max_url, js_size_max_url, css_size_max_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                MyLogger.log(TestCaseExecutionWWWSumDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionWWWSumDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionWWWSumDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }
}
