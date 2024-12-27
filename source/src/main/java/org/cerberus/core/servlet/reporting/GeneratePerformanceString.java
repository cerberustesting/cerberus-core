/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.servlet.reporting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "GeneratePerformanceString", urlPatterns = {"/GeneratePerformanceString"}) // TODO to delete ?
public class GeneratePerformanceString {
	
	private static final Logger LOG = LogManager.getLogger(GeneratePerformanceString.class);

    public String gps(Connection conn, String test, String testcase, String country) throws SQLException {
        
        StringBuilder data = new StringBuilder();
        List<String> dates = new ArrayList<>();
        StringBuilder datas = new StringBuilder();
        
        try(PreparedStatement stmt = conn.prepareStatement("select id, `Start` as startdate, "
                + " UNIX_TIMESTAMP(End)-UNIX_TIMESTAMP(Start) as duration , substr(`start`,1, 16) as sub, "
                + " description "
                + " from testcaseexecution a join testcase b "
                + " on a.test=b.test and a.testcase=b.testcase "
                + " where a.test = ? and a.testcase = ? and a.controlstatus = ?"
                + " and a.country = ? order by `Start` desc"
                + " limit 200");
        		PreparedStatement stmt2 = conn.prepareStatement("select id, `Start` as startdate, "
                        + " UNIX_TIMESTAMP(End)-UNIX_TIMESTAMP(Start) as duration , substr(`start`,1, 16) as sub, "
                        + " description "
                        + " from testcaseexecution a join testcase b "
                        + " on a.test=b.test and a.testcase=b.testcase "
                        + " where a.test = ? and a.testcase = ? and a.controlstatus = ?"
                        + " and a.country = ? and `Start` > ? order by `Start` desc"
                        + " limit 200");){
        	stmt.setString(1, test);
            stmt.setString(2, testcase);
            stmt.setString(4, country);
            stmt.setString(3, "OK");
            String firstDate = Collections.min(dates);
            stmt2.setString(1, test);
            stmt2.setString(2, testcase);
            stmt2.setString(4, country);
            stmt2.setString(3, "KO");
            stmt2.setString(5, firstDate);

            try(ResultSet rs_executiondetail = stmt.executeQuery();
            		ResultSet rs_executiondetail2 = stmt2.executeQuery();){
            	List<Float> maxList = new ArrayList<>();
                if (rs_executiondetail.first()) {
                    do {
                        maxList.add(rs_executiondetail.getFloat("duration"));
                        data.append(rs_executiondetail.getString("sub"));
                        dates.add(rs_executiondetail.getString("startdate"));
                        data.append(",");
                        data.append(rs_executiondetail.getString("duration"));
                        data.append(",");
                        data.append(rs_executiondetail.getString("id"));
                        if (!rs_executiondetail.isLast()) {
                            data.append("/p/");
                        }
                    } while (rs_executiondetail.next());
                }
                data.append("/k/");
                if (rs_executiondetail2.first()) {
                    do {
                        maxList.add(rs_executiondetail2.getFloat("duration"));
                        data.append(rs_executiondetail2.getString("sub"));
                        data.append(",");
                        data.append(rs_executiondetail2.getString("duration"));
                        data.append(",");
                        data.append(rs_executiondetail2.getString("id"));
                        if (!rs_executiondetail2.isLast()) {
                            data.append("/p/");
                        }

                    } while (rs_executiondetail2.next());
                }
                if (maxList.size()!=1){
                datas.append(Collections.max(maxList));
                datas.append("/d/");
                datas.append(data);}
            }catch(SQLException e) {
            	LOG.warn(e.toString());
            }
        }catch(SQLException e) {
        	LOG.warn(e.toString());
        }
        return datas.toString();
    }
}
