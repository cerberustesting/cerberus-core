/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;


import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.TestCase;
import com.redcats.tst.log.MyLogger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 */
@Repository
public class TCEwwwDetDAOImpl implements ITCEwwwDetDAO {
   
    @Autowired
    DatabaseSpring databaseSpring;

    @Override
    public List<TestcaseExecutionwwwDet> getListOfDetail(int execId) {
       List<TestcaseExecutionwwwDet> list = null;
        
       final String query = "SELECT * FROM testcaseexecutionwwwdet WHERE execID = ?";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, String.valueOf(execId));
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList();
                try {
                    while (resultSet.next()) {
                        TestcaseExecutionwwwDet detail = new TestcaseExecutionwwwDet();
                         detail.setId(resultSet.getString("ID")==null?0:resultSet.getInt("ID"));       
                         detail.setExecID(resultSet.getString("EXECID")==null?0:resultSet.getInt("EXECID"));
                         detail.setStart(resultSet.getString("START")==null?"":resultSet.getString("START"));
                         detail.setUrl(resultSet.getString("URL")==null?"":resultSet.getString("URL"));
                         detail.setEnd(resultSet.getString("END")==null?"":resultSet.getString("END"));
                         detail.setExt(resultSet.getString("EXT")==null?"":resultSet.getString("EXT"));
                         detail.setStatusCode(resultSet.getInt("StatusCode")==0?0:resultSet.getInt("StatusCode"));
                         detail.setMethod(resultSet.getString("Method")==null?"":resultSet.getString("Method"));
                         detail.setBytes(resultSet.getString("Bytes")==null?0:resultSet.getInt("Bytes"));
                         detail.setTimeInMillis(resultSet.getString("TimeInMillis")==null?0:resultSet.getInt("TimeInMillis"));
                         detail.setReqHeader_Host(resultSet.getString("ReqHeader_Host")==null?"":resultSet.getString("ReqHeader_Host"));
                         detail.setResHeader_ContentType(resultSet.getString("ResHeader_ContentType")==null?"":resultSet.getString("ResHeader_ContentType"));

                        list.add(detail);
                    }
                } catch (SQLException exception) {
                    //TODO logger ERROR
                    //error on resultSet.getString
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                //TODO logger ERROR
                //preStat.executeQuery();
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            //TODO logger ERROR
            //conn.prepareStatement(query);
        } finally {
            databaseSpring.disconnect();
        }

        return list;
    }
   
    
    
    @Override
    public List<TestCaseExecutionwwwSumHistoric> getHistoricForParameter(TestCase testcase, String parameter) {
       List<TestCaseExecutionwwwSumHistoric> historic = new ArrayList();
       String test = testcase.getTest();
       String tc = testcase.getTestCase();
       String country = testcase.getCountryList().get(0);
        StringBuilder query = new StringBuilder();
        query.append(" select start, ");
        query.append(parameter);
        query.append(" from testcaseexecutionwwwsum a join testcaseexecution b on a.id=b.id where test = '");
        query.append(test);
        query.append("' and testcase = '");
        query.append(tc);
        query.append("' and country = '");
        query.append(country);
        query.append("' limit 100");
        

        databaseSpring.connect();
        ResultSet rs = null;
        try {
            rs = databaseSpring.query(query.toString());

            while (rs.next()) {
                TestCaseExecutionwwwSumHistoric histoToAdd = new TestCaseExecutionwwwSumHistoric();
                histoToAdd.setStart(rs.getString(1));
                histoToAdd.setParameter(rs.getString(2));
                
                historic.add(histoToAdd);
            }

        } catch (SQLException ex) {
            MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.FATAL, ex.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TCEwwwDetDAOImpl.class.getName(), Level.INFO, "Exception closing ResultSet: " + ex.toString());
            }
        }
        databaseSpring.disconnect();

        return historic;
    }
    
}
