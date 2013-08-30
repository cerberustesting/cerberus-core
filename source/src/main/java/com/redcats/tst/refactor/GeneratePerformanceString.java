/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author bcivel
 */
public class GeneratePerformanceString {

    public String gps(Connection conn, String test, String testcase, String country) throws SQLException {
        
        StringBuilder data = new StringBuilder();
        List<String> dates = new ArrayList();
        StringBuilder datas = new StringBuilder();
        PreparedStatement stmt = conn.prepareStatement("select id, `Start` as startdate, "
                + " UNIX_TIMESTAMP(End)-UNIX_TIMESTAMP(Start) as duration , substr(`start`,1, 16) as sub, "
                + " description, SLA "
                + " from testcaseexecution a join testcase b "
                + " on a.test=b.test and a.testcase=b.testcase "
                + " where a.test = ? and a.testcase = ? and a.controlstatus = ?"
                + " and a.country = ? order by `Start` desc"
                + " limit 200");
        stmt.setString(1, test);
        stmt.setString(2, testcase);
        stmt.setString(4, country);
        stmt.setString(3, "OK");

        ResultSet rs_executiondetail = stmt.executeQuery();
        List<Float> maxList = new ArrayList();
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
        
        rs_executiondetail.close();
        data.append("/k/");
        
        String firstDate = Collections.min(dates);
        
        PreparedStatement stmt2 = conn.prepareStatement("select id, `Start` as startdate, "
                + " UNIX_TIMESTAMP(End)-UNIX_TIMESTAMP(Start) as duration , substr(`start`,1, 16) as sub, "
                + " description, SLA "
                + " from testcaseexecution a join testcase b "
                + " on a.test=b.test and a.testcase=b.testcase "
                + " where a.test = ? and a.testcase = ? and a.controlstatus = ?"
                + " and a.country = ? and `Start` > ? order by `Start` desc"
                + " limit 200");
        stmt2.setString(1, test);
        stmt2.setString(2, testcase);
        stmt2.setString(4, country);
        stmt2.setString(3, "KO");
        stmt2.setString(5, firstDate);

        ResultSet rs_executiondetail2 = stmt2.executeQuery();
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
        rs_executiondetail2.close();
        
        stmt.close();
        stmt2.close();
        if (maxList.size()!=1){
        datas.append(Collections.max(maxList));
        datas.append("/d/");
        datas.append(data);}
        
        return datas.toString();
    }
    
    
    

}
